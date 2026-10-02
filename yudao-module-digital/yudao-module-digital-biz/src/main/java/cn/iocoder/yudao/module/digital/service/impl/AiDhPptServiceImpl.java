package cn.iocoder.yudao.module.digital.service.impl;

import cn.iocoder.yudao.module.digital.dal.AiAgentDO;
import cn.iocoder.yudao.module.digital.dal.AiCopyWritePptRecordDO;
import cn.iocoder.yudao.module.digital.dal.mysql.AiAgentMapper;
import cn.iocoder.yudao.module.digital.dal.mysql.CommonMapper;
import cn.iocoder.yudao.module.digital.framework.file.core.client.s3.S3FileClient;
import cn.iocoder.yudao.module.digital.framework.file.core.client.s3.S3FileClientConfig;
import cn.iocoder.yudao.module.digital.service.AiDhPptService;
import cn.iocoder.yudao.module.digital.service.CallPythonService;
import cn.iocoder.yudao.module.digital.service.MinioClientService;
import cn.iocoder.yudao.module.digital.service.copywriting.CopywritingManagementService;
import cn.iocoder.yudao.module.digital.util.*;
import cn.iocoder.yudao.module.digital.vo.CopywritingPptReqVO;
import com.alibaba.fastjson.JSONArray;
import com.alibaba.fastjson.JSONObject;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang3.StringUtils;
import org.springframework.stereotype.Service;

import javax.annotation.Resource;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.util.*;
import java.util.concurrent.ConcurrentHashMap;

/**
 * Project:AiDhPptServiceImpl
 * Author:lilj
 * Date:2024/7/22
 * Description:
 */
@Service
@Slf4j
public class AiDhPptServiceImpl implements AiDhPptService {

    @Resource
    private CallPythonService callPythonService;
    @Resource
    private CopywritingManagementService copywritingManagementService;
    @Resource
    private CommonMapper commonMapper;

    @Resource
    private AiAgentMapper aiAgentMapper;

    @Resource
    private MinioClientService minioClientService;

    // PPT→PDF→图片转换需要落本地临时文件，用系统临时目录即可，无需 SFTP/共享盘
    private final String sourcePath = System.getProperty("java.io.tmpdir");

    // ppt-master 异步任务上下文：jobId -> 提交参数 + 落库结果（内存态，幂等落库用）
    private static class PptMasterJobContext {
        String title;
        String docName;
        String oprStaff;
        String pptId;
        String copywriteId;
    }
    private final Map<String, PptMasterJobContext> pptMasterJobs = new ConcurrentHashMap<>();

    /**
     * 按 smart_id 查出智能体人设(agent_role)，塞进请求里，供 LLM 作为 system prompt 使用
     */
    private void attachAgentRole(JSONObject jsonObject) {
        String smartId = jsonObject.getString("smart_id");
        if (StringUtils.isBlank(smartId)) {
            return;
        }
        // 注意：AiAgentDO 继承 TenantBaseDO，但 tb_ai_dh_copywrite_agent 表没有 tenant_id 等字段，
        // 不能用 selectById（会拼 tenant_id 等列报错），改用 selectListAll 后按 id 过滤。
        AiAgentDO agent = aiAgentMapper.selectListAll().stream()
                .filter(a -> smartId.equals(a.getId()))
                .findFirst().orElse(null);
        if (agent != null && StringUtils.isNotBlank(agent.getAgentRole())) {
            jsonObject.put("agentRole", agent.getAgentRole());
        }
    }


    @Override
    public JSONObject updateppt(JSONObject jsonObject) throws Exception {

        //调用python接口
        String interName = "/updateppt";
        JSONObject getPpt = callPythonService.callToPPtPythonPost(jsonObject,interName);
        log.info("调用python接口返回结果：" + getPpt.toJSONString());
        return getPpt;
    }

    @Override
    public JSONObject getppt() throws Exception {

        //调用python接口
        JSONObject jsonObject = new JSONObject();
        String interName = "/getppt";
        JSONObject getPpt = callPythonService.callToPPtPythonGet(jsonObject,interName);
        return getPpt;
    }

    @Override
    public JSONObject generateBody(JSONObject jsonObject) throws Exception {

        //调用python接口
        String interName = "/generate_body";
        attachAgentRole(jsonObject);
        JSONObject getPpt = callPythonService.callToPPtPythonPost(jsonObject,interName);
        return getPpt;
    }

    @Override
    public JSONObject updatePptNote(JSONObject jsonObject) throws Exception {

        //调用python接口
        String interName = "/updateppt";
        JSONObject getPpt = callPythonService.callToPPtPythonPost(jsonObject,interName);
        return getPpt;
    }

    @Override
    public JSONObject generatePpt(JSONObject jsonObject) throws Exception {

        //调用python接口
        String interName = "/generate_ppt";
        String oprStaffId = jsonObject.getString("user");
        attachAgentRole(jsonObject);

        String copywriteId = null;
        String pptId = null;
        JSONArray slides = null;

        JSONObject getPpt = callPythonService.callToPPtPythonPost(jsonObject,interName);
        log.info("调用python接口返回结果：" + getPpt);
        if(getPpt != null){
            if("0000".equals(getPpt.getString("code"))) {
                JSONObject data = getPpt.getJSONObject("data");
                String pptUrl = data.getString("pptUrl");
                String recordDesc = data.getString("recordDesc");
                JSONObject notesMap = data.getJSONObject("notesMap");
                slides = data.getJSONArray("slides");
                // 落文案主表 + PPT 记录（明细表里的 pptId 用 PPT 记录 id）
                String copywriteContent = jsonObject.getString("text") != null ? jsonObject.getString("text") : jsonObject.getString("content");
                copywriteId = copywritingManagementService.copywritingCreate(jsonObject.getString("doc_name"), jsonObject.getString("title"), copywriteContent, oprStaffId);
                pptId = copywritingManagementService.copywritingCreatePPT(copywriteId, pptUrl, recordDesc, oprStaffId);
                //图片由 server 侧 LibreOffice 渲染（soffice→PDF→PDFBox→PNG），保证与 .pptx 版式/字体一致
                try {
                    List<String> imageUrls = renderPptToImages(pptUrl, pptId);
                    for (int i = 0; i < imageUrls.size(); i++) {
                        String imageUrl = imageUrls.get(i);
                        String notesMapString = notesMap.getString(String.valueOf(i));
                        Map<String, Object> params = new HashMap<>();
                        params.put("pptId",pptId);
                        params.put("pptNum",i);
                        params.put("pptImageUrl",imageUrl);
                        params.put("pptImageWords",notesMapString);
                        params.put("pptSlideContent", slides != null && i < slides.size() ? slides.getJSONObject(i).toJSONString() : "");
                        params.put("pptSlideElements", "");
                        params.put("pptVoiceUrl","");
                        params.put("pptVoiceLength","");
                        params.put("pptVoiceHumanUrl","");
                        params.put("pptVideoImageUrl","");
                        params.put("oprTime", DateUtils.formatDate(new Date(), DateUtils.FORMAT_YYYYMMDD24HHMMSS));
                        params.put("oprStaff",oprStaffId);
                        commonMapper.insertPptRecordDetail(params);
                    }
                } catch (Exception e) {
                    log.error("ppt生成图片异常异常", e);
                    JSONObject errorResult = new JSONObject();
                    errorResult.put("code", "9999");
                    errorResult.put("msg","ppt生成图片异常" );
                    return errorResult;
                }



            } else {
                JSONObject errorResult = new JSONObject();
                errorResult.put("code", "9999");
                String errMsg = getPpt.getString("msg");
                errorResult.put("msg", errMsg == null ? "PPT生成失败" : errMsg);
                return errorResult;
            }
        } else {
            JSONObject errorResult = new JSONObject();
            errorResult.put("code", "9999");
            errorResult.put("msg", "PPT生成服务无响应");
            return errorResult;
        }
        JSONObject result = new JSONObject();
        result.put("code", "0000");
        result.put("msg", "ppt生成pdf成功");
        JSONObject data = new JSONObject();
        data.put("pptId", pptId);
        data.put("copywriteId", copywriteId);
        data.put("slides", slides);
        result.put("data", data);
        return result;
    }

    @Override
    public JSONObject generatePptMaster(JSONObject jsonObject) throws Exception {
        JSONObject getPpt = callPythonService.callToPPtPythonPostLong(jsonObject, "/generate_ppt_master");
        log.info("调用python接口返回结果：" + getPpt);
        if (getPpt == null || !"0000".equals(getPpt.getString("code"))) {
            return pptMasterError(getPpt);
        }
        return buildPptMasterResult(jsonObject, getPpt.getJSONObject("data"));
    }

    @Override
    public JSONObject submitPptMaster(JSONObject jsonObject) throws Exception {
        JSONObject getPpt = callPythonService.callToPPtPythonPost(jsonObject, "/generate_ppt_master/submit");
        log.info("调用python接口返回结果：" + getPpt);
        if (getPpt == null || !"0000".equals(getPpt.getString("code"))) {
            return pptMasterError(getPpt);
        }
        String jobId = getPpt.getJSONObject("data").getString("jobId");
        PptMasterJobContext ctx = new PptMasterJobContext();
        ctx.title = jsonObject.getString("title") != null ? jsonObject.getString("title") : jsonObject.getString("topic");
        ctx.docName = jsonObject.getString("doc_name") != null ? jsonObject.getString("doc_name") : ctx.title;
        ctx.oprStaff = jsonObject.getString("user");
        pptMasterJobs.put(jobId, ctx);

        JSONObject result = new JSONObject();
        result.put("code", "0000");
        result.put("msg", "ppt-master 已提交");
        JSONObject data = new JSONObject();
        data.put("jobId", jobId);
        result.put("data", data);
        return result;
    }

    @Override
    public JSONObject getPptMasterStatus(String jobId) throws Exception {
        JSONObject getPpt = callPythonService.callPptMasterStatus(jobId);
        log.info("调用python接口返回结果：" + getPpt);
        if (getPpt == null || !"0000".equals(getPpt.getString("code"))) {
            return pptMasterError(getPpt);
        }
        JSONObject job = getPpt.getJSONObject("data");
        String status = job.getString("status");

        JSONObject result = new JSONObject();
        result.put("code", "0000");
        JSONObject resultData = new JSONObject();
        resultData.put("status", status);
        resultData.put("progress", job.get("progress"));

        if ("failed".equals(status)) {
            resultData.put("msg", job.getString("error"));
        } else if ("success".equals(status)) {
            JSONObject data = job.getJSONObject("result");
            String pptUrl = data.getString("pptUrl");
            String recordDesc = data.getString("recordDesc");
            String summary = data.getString("summary");

            PptMasterJobContext ctx = pptMasterJobs.get(jobId);
            if (ctx != null) {
                synchronized (ctx) {
                    if (ctx.pptId == null) {
                        JSONObject persisted = persistPptMaster(pptUrl, recordDesc, ctx.title, ctx.docName, ctx.oprStaff);
                        ctx.pptId = persisted.getString("pptId");
                        ctx.copywriteId = persisted.getString("copywriteId");
                    }
                }
                resultData.put("pptId", ctx.pptId);
                resultData.put("copywriteId", ctx.copywriteId);
            }
            resultData.put("pptUrl", pptUrl);
            resultData.put("recordDesc", recordDesc);
            resultData.put("summary", summary);
        }
        result.put("data", resultData);
        return result;
    }

    private JSONObject pptMasterError(JSONObject getPpt) {
        JSONObject errorResult = new JSONObject();
        errorResult.put("code", "9999");
        String errMsg = getPpt != null ? getPpt.getString("msg") : "ppt-master 生成服务无响应";
        errorResult.put("msg", errMsg == null ? "ppt-master 生成失败" : errMsg);
        return errorResult;
    }

    private JSONObject buildPptMasterResult(JSONObject req, JSONObject data) {
        String pptUrl = data.getString("pptUrl");
        String recordDesc = data.getString("recordDesc");
        String title = req.getString("title") != null ? req.getString("title") : req.getString("topic");
        String docName = req.getString("doc_name") != null ? req.getString("doc_name") : title;
        String oprStaffId = req.getString("user");
        JSONObject persisted = persistPptMaster(pptUrl, recordDesc, title, docName, oprStaffId);

        JSONObject result = new JSONObject();
        result.put("code", "0000");
        result.put("msg", "ppt-master 生成成功");
        JSONObject resultData = new JSONObject();
        resultData.put("pptId", persisted.getString("pptId"));
        resultData.put("copywriteId", persisted.getString("copywriteId"));
        resultData.put("pptUrl", pptUrl);
        resultData.put("recordDesc", recordDesc);
        resultData.put("summary", data.getString("summary"));
        result.put("data", resultData);
        return result;
    }

    private JSONObject persistPptMaster(String pptUrl, String recordDesc, String title, String docName, String oprStaffId) {
        // ppt-master 一键直出，无正文/大纲，文案主表内容用主题占位
        String copywriteId = copywritingManagementService.copywritingCreate(docName, title, title, oprStaffId);
        String pptId = copywritingManagementService.copywritingCreatePPT(copywriteId, pptUrl, recordDesc, oprStaffId);

        // 预览图：复用 soffice→PDF→PNG 渲染（与 .pptx 版式一致）。失败不阻断主产物，仅记录日志。
        try {
            List<String> imageUrls = renderPptToImages(pptUrl, pptId);
            for (int i = 0; i < imageUrls.size(); i++) {
                Map<String, Object> params = new HashMap<>();
                params.put("pptId", pptId);
                params.put("pptNum", i);
                params.put("pptImageUrl", imageUrls.get(i));
                params.put("pptImageWords", "");
                params.put("pptSlideContent", "");
                params.put("pptSlideElements", "");
                params.put("pptVoiceUrl", "");
                params.put("pptVoiceLength", "");
                params.put("pptVoiceHumanUrl", "");
                params.put("pptVideoImageUrl", "");
                params.put("oprTime", DateUtils.formatDate(new Date(), DateUtils.FORMAT_YYYYMMDD24HHMMSS));
                params.put("oprStaff", oprStaffId);
                commonMapper.insertPptRecordDetail(params);
            }
        } catch (Exception e) {
            log.error("ppt-master 生成图片异常", e);
        }

        JSONObject persisted = new JSONObject();
        persisted.put("pptId", pptId);
        persisted.put("copywriteId", copywriteId);
        return persisted;
    }

    @Override
    public JSONObject savePptEdit(JSONObject jsonObject) throws Exception {
        String pptId = jsonObject.getString("pptId");
        JSONArray slides = jsonObject.getJSONArray("slides");
        if (slides != null) {
            for (int i = 0; i < slides.size(); i++) {
                JSONObject s = slides.getJSONObject(i);
                Map<String, Object> params = new HashMap<>();
                params.put("pptId", pptId);
                params.put("pptNum", s.getInteger("pptNum"));
                params.put("pptSlideElements", s.getString("content"));
                commonMapper.updatePptSlideElements(params);
            }
        }
        JSONObject result = new JSONObject();
        result.put("code", "0000");
        result.put("msg", "保存成功");
        return result;
    }
    @Override
    public JSONObject regeneratePpt(JSONObject jsonObject) throws Exception {
        String pptRecordId = jsonObject.getString("pptId");
        String templateId = jsonObject.getString("templateId");
        JSONObject pyReq = new JSONObject();
        pyReq.put("title", jsonObject.getString("title"));
        pyReq.put("slides", jsonObject.getJSONArray("slides"));
        pyReq.put("pptId", templateId);
        JSONObject getPpt = callPythonService.callToPPtPythonPost(pyReq, "/regenerate_ppt");
        if (getPpt == null || !"0000".equals(getPpt.getString("code"))) {
            JSONObject errorResult = new JSONObject();
            errorResult.put("code", "9999");
            errorResult.put("msg", getPpt != null ? getPpt.getString("msg") : "PPT重新生成服务无响应");
            return errorResult;
        }
        String pptUrl = getPpt.getJSONObject("data").getString("pptUrl");
        Map<String, Object> urlParams = new HashMap<>();
        urlParams.put("pptId", pptRecordId);
        urlParams.put("pptUrl", pptUrl);
        commonMapper.updatePptRecordUrl(urlParams);
        JSONObject result = new JSONObject();
        result.put("code", "0000");
        result.put("msg", "重新生成成功");
        result.put("data", getPpt.getJSONObject("data"));
        return result;
    }
    @Override
    public JSONObject generateOutline(JSONObject jsonObject) throws Exception {

        //调用python接口
        String interName = "/generate_outline";
        attachAgentRole(jsonObject);
        JSONObject getPpt = callPythonService.callToPPtPythonPost(jsonObject,interName);
        return getPpt;
    }

    /**
     * 下载 .pptx -> soffice 转 PDF -> PDFBox 转每页 PNG -> 上传 MinIO，返回每页图片 URL。
     * 让预览 PNG 与 .pptx 版式/字体一致（替代引擎 Pillow 渲染）。
     */
    private List<String> renderPptToImages(String pptKey, String pptId) throws Exception {
        String fileName = pptKey.substring(pptKey.lastIndexOf('/') + 1);
        String filePrefix = fileName.substring(0, fileName.lastIndexOf('.'));
        File pptFile = new File(sourcePath, fileName);

        minioClientService.initMinioClient();
        byte[] pptBytes = minioClientService.minioDownload(MinioClientService.toObjectKey(pptKey));
        try (FileOutputStream fos = new FileOutputStream(pptFile)) {
            fos.write(pptBytes);
        }

        boolean ok = PptToPdfUtil.convert2PDF(pptFile.getAbsolutePath(), sourcePath);
        if (!ok) {
            Files.deleteIfExists(pptFile.toPath());
            throw new RuntimeException("ppt转pdf失败");
        }
        File pdfFile = new File(sourcePath, filePrefix + ".pdf");
        List<byte[]> pngList;
        try (FileInputStream fis = new FileInputStream(pdfFile)) {
            byte[] pdfBytes = new byte[(int) pdfFile.length()];
            fis.read(pdfBytes);
            pngList = PdfToImageUtil.pdfToImage(pdfBytes);
        }

        minioClientService.initMinioClient();
        List<String> urls = new ArrayList<>();
        for (int i = 0; i < pngList.size(); i++) {
            String fileStr = pptId + "_" + i + ".png";
            urls.add(minioClientService.minioUpload(pngList.get(i), fileStr, "png"));
        }

        Files.deleteIfExists(pdfFile.toPath());
        Files.deleteIfExists(pptFile.toPath());
        return urls;
    }

    @Override
    public JSONObject pilgrimage(JSONObject jsonObject) throws Exception {

        //调用python接口
        String interName = "/ppttoimage";
        JSONObject getPpt = callPythonService.callToPPtPythonPost(jsonObject,interName);
        log.info("调用python接口返回结果：" + getPpt);
        if(getPpt != null){
            if("0000".equals(getPpt.getString("code"))) {
                JSONObject notesMap = getPpt.getJSONObject("data");
                String pptPath = jsonObject.getString("ppt_path");
                String pptId = jsonObject.getString("ppt_id");
                String oprStaffId = jsonObject.getString("user");

                //下载ppt
                try {


                    int lastIndex = pptPath.lastIndexOf('/');
                    // 使用substring方法截取从最后一个'/'之后的所有字符
                    String fileName = pptPath.substring(lastIndex + 1);

                    String fileBeforesion = fileName.substring(0, fileName.lastIndexOf("."));
                    String fileExtension = pptPath.substring(pptPath.lastIndexOf("."));

                    // 从 MinIO 下载 PPT 文件到本地
                    minioClientService.initMinioClient();
                    byte[] pptBytes = minioClientService.minioDownload(MinioClientService.toObjectKey(pptPath));
                    try (FileOutputStream fos = new FileOutputStream(sourcePath + "/" + fileName)) {
                        fos.write(pptBytes);
                    }


                    //先把PPT转pdf,再进行pdf转图片
                    boolean convert2PDF = PptToPdfUtil.convert2PDF(sourcePath+"/"+fileName, sourcePath);
                    if(convert2PDF){
                        File file = new File(sourcePath+"/"+fileBeforesion+".pdf");
                        FileInputStream fileInputStream = new FileInputStream(file);
                        byte[] pdfBytes = new byte[(int) file.length()];
                        fileInputStream.read(pdfBytes);
                        fileInputStream.close();

                        List<byte[]>  list = PdfToImageUtil.pdfToImage(pdfBytes);
                        int i = 0;
                        minioClientService.initMinioClient();
                        for (byte[] bytes : list){
                            String fileStr = pptId+"_"+ (i) + ".png";

/*                            PdfToImageUtil.saveImage(bytes,sourcePath,fileStr);
                            System.out.println(fileStr);
                            //进行照片上传到服务器上
                            FileInputStream inputStream = new FileInputStream(sourcePath+"/"+fileStr);
                            uploadFileSFTP(sftpConfigDO,inputStream,fileStr,sftpConfigDO.getSftpUploadPath());*/

                            String type = "png";
                            String imageUrl = minioClientService.minioUpload(bytes, fileStr,  type);

                            //进行落表
                            String notesMapString = notesMap.getString(String.valueOf(i));
                            Map<String, Object> params = new HashMap<>();
                            params.put("pptId",pptId);
                            params.put("pptNum",i);
                            params.put("pptImageUrl",imageUrl);
                            params.put("pptImageWords",notesMapString);
                            params.put("pptVoiceUrl","");
                            params.put("pptVoiceLength","");
                            params.put("pptVoiceHumanUrl","");
                            params.put("pptVideoImageUrl","");
                            params.put("oprTime", DateUtils.formatDate(new Date(), DateUtils.FORMAT_YYYYMMDD24HHMMSS));
                            params.put("oprStaff",oprStaffId);


                            commonMapper.insertPptRecordDetail(params);
                            i++;

                            Files.deleteIfExists(Paths.get(sourcePath+"/"+fileStr));

                        }
                    }else{
                        JSONObject errorResult = new JSONObject();
                        errorResult.put("code", "9999");
                        errorResult.put("msg", "ppt生成pdf异常");
                        return errorResult;

                    }

                    Files.deleteIfExists(Paths.get(sourcePath+"/"+fileBeforesion+".pdf")); // 清理临时文件
                    Files.deleteIfExists(Paths.get(sourcePath+"/"+fileName)); // 清理临时文件

                } catch (Exception e) {
                    log.error("ppt生成图片异常异常", e);
                    JSONObject errorResult = new JSONObject();
                    errorResult.put("code", "9999");
                    errorResult.put("msg", "ppt生成图片异常");
                    return errorResult;
                }


            } else {
                JSONObject errorResult = new JSONObject();
                errorResult.put("code", "9999");
                String errMsg = getPpt.getString("msg");
                errorResult.put("msg", errMsg == null ? "PPT生成失败" : errMsg);
                return errorResult;
            }
        } else {
            JSONObject errorResult = new JSONObject();
            errorResult.put("code", "9999");
            errorResult.put("msg", "PPT生成服务无响应");
            return errorResult;
        }

        JSONObject result = new JSONObject();
        result.put("code", "0000");
        result.put("msg", "ppt生成pdf成功");
        return result;

    }

}
