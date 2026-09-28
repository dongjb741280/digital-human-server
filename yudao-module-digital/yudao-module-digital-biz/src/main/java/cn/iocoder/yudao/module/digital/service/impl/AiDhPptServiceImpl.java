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

        JSONObject getPpt = callPythonService.callToPPtPythonPost(jsonObject,interName);
        log.info("调用python接口返回结果：" + getPpt);
        if(getPpt != null){
            if("0000".equals(getPpt.getString("code"))) {
                JSONObject data = getPpt.getJSONObject("data");
                String pptUrl = data.getString("pptUrl");
                String recordDesc = data.getString("recordDesc");
                JSONArray images = data.getJSONArray("images");
                JSONObject notesMap = data.getJSONObject("notesMap");
                JSONArray slides = data.getJSONArray("slides");
                // 落文案主表 + PPT 记录（明细表里的 pptId 用 PPT 记录 id）
                String copywriteContent = jsonObject.getString("text") != null ? jsonObject.getString("text") : jsonObject.getString("content");
                copywriteId = copywritingManagementService.copywritingCreate(jsonObject.getString("doc_name"), jsonObject.getString("title"), copywriteContent, oprStaffId);
                pptId = copywritingManagementService.copywritingCreatePPT(copywriteId, pptUrl, recordDesc, oprStaffId);
                //图片由 Python 侧渲染并上传，这里直接落明细表
                try {
                    for (int i = 0; i < images.size(); i++) {
                        String imageUrl = images.getString(i);
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
        result.put("data", data);
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
