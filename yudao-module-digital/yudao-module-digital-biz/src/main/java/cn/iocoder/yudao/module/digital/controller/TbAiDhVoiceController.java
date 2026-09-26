package cn.iocoder.yudao.module.digital.controller;


import cn.iocoder.yudao.framework.common.exception.enums.GlobalErrorCodeConstants;
import cn.iocoder.yudao.framework.common.pojo.CommonResult;
import cn.iocoder.yudao.framework.common.result.CustomResult;
import cn.iocoder.yudao.framework.common.result.ResultUtil;
import cn.iocoder.yudao.module.digital.dal.SftpConfigDO;
import cn.iocoder.yudao.module.digital.entity.TbAiDhVideoMaterial;
import cn.iocoder.yudao.module.digital.service.ITbAiDhVoiceService;
import cn.iocoder.yudao.module.digital.service.MinioClientService;
import cn.iocoder.yudao.module.digital.service.SftpConfigService;
import cn.iocoder.yudao.module.digital.util.EncryptUtil;
import cn.iocoder.yudao.module.digital.util.SFtpOper;
import com.alibaba.fastjson.JSONObject;
import com.jcraft.jsch.*;
import io.swagger.v3.oas.annotations.Operation;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.core.io.InputStreamResource;
import org.springframework.core.io.UrlResource;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import javax.annotation.Resource;
import javax.servlet.ServletException;
import javax.servlet.ServletOutputStream;
import javax.servlet.http.HttpServletResponse;
import java.io.BufferedInputStream;
import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.text.SimpleDateFormat;
import java.util.*;

import static cn.iocoder.yudao.framework.common.pojo.CommonResult.error;
import static cn.iocoder.yudao.framework.common.pojo.CommonResult.success;
import static cn.iocoder.yudao.framework.common.util.collection.CollectionUtils.convertList;
import static cn.iocoder.yudao.framework.security.core.util.SecurityFrameworkUtils.getLoginUserId;

/**
 * <p>
 * 前端控制器
 * </p>
 *
 * @author author
 * @since 2024-07-17
 */
@RestController
@RequestMapping("/digital-api/system/voiceManager")
@Slf4j
public class TbAiDhVoiceController {
    @Autowired
    private ITbAiDhVoiceService tbAiDhVoiceService;

    @Resource
    private SftpConfigService sftpConfigService;
    @Autowired
    private MinioClientService minioClientService;

    /**
     * @autor: lkm
     * @time: 2024/7/16 18:43
     * @description: 声音列表
     * @param:
     * @return: 0000成功  9999失败
     */
    @PostMapping("/voiceList")
    @Operation(summary = "获得声音分页列表")
    public CommonResult<HashMap> voiceList(@RequestBody HashMap hashMap) {
        log.info("进入voiceList 声音列表查询方法：" + JSONObject.toJSONString(hashMap));
        HashMap result = null;
        try {
            result = tbAiDhVoiceService.voiceList(hashMap);
            log.info("调用 voiceList success：" + JSONObject.toJSONString(hashMap));
        } catch (Exception e) {
            log.info("调用 voiceList error：" + e.getMessage());
            return error(GlobalErrorCodeConstants.UNKNOWN);
        }
        //return ResultUtil.success(result);
        return success(result);
    }


    /*@GetMapping("/page")
    @Operation(summary = "获得用户分页列表")
    @PreAuthorize("@ss.hasPermission('system:user:list')")
    public CommonResult<PageResult<UserRespVO>> getUserPage(@Valid UserPageReqVO pageReqVO) {
        // 获得用户分页列表
        PageResult<AdminUserDO> pageResult = userService.getUserPage(pageReqVO);
        if (CollUtil.isEmpty(pageResult.getList())) {
            return success(new PageResult<>(pageResult.getTotal()));
        }
        // 拼接数据
        Map<Long, DeptDO> deptMap = deptService.getDeptMap(
                convertList(pageResult.getList(), AdminUserDO::getDeptId));
        return success(new PageResult<>(UserConvert.INSTANCE.convertList(pageResult.getList(), deptMap),
                pageResult.getTotal()));
    }*/


    @PostMapping("/voiceDel")
    @Operation(summary = "删除声音")
    public CommonResult<HashMap> voiceDel(@RequestBody HashMap hashMap) {
        log.info("进入voiceDel 声音删除方法：" + JSONObject.toJSONString(hashMap));
        HashMap result = null;
        try {
            result = tbAiDhVoiceService.voiceDel(hashMap);
            log.info("调用 voiceDel success：" + JSONObject.toJSONString(hashMap));
        } catch (Exception e) {
            log.info("调用 voiceDel error：" + e.getMessage());
            return error(GlobalErrorCodeConstants.UNKNOWN);
        }
        return success(result);
    }

    @PostMapping("/voiceSave")
    @Operation(summary = "声音保存")
    public CommonResult<HashMap> voiceSave(@RequestBody HashMap hashMap) {
        log.info("进入voiceSave 声音信息保存方法：" + JSONObject.toJSONString(hashMap));
        HashMap result = null;
        try {
            Long loginUserId = getLoginUserId();
            hashMap.put("oprStaff", loginUserId);
            result = tbAiDhVoiceService.voiceSave(hashMap);
            log.info("调用 voiceSave success：" + JSONObject.toJSONString(hashMap));
        } catch (Exception e) {
            log.info("调用 voiceSave error：", e);
            return error(GlobalErrorCodeConstants.UNKNOWN);
        }
        return success(result);
    }

    @PostMapping("/voiceUpload")
    @Operation(summary = "声音流保存")
    public CommonResult<HashMap> voiceUpload(@RequestParam("file") MultipartFile file) {
        log.info("进入voiceSave 声音流保存方法：");
        HashMap result = new HashMap();
        try {
            //1.获取源文件的输入流
            /*InputStream is = file.getInputStream();
            //2.获取源文件类型，文件后缀名
            String originalFileName = file.getOriginalFilename();
            //3.定义上传后的目标文件名(为了避免文件名称重复，此时使用UUID)
            Long loginUserId = getLoginUserId();
            String date = new SimpleDateFormat("yyyyMMddHHmmss").format(new Date());
            SftpConfigDO sftpConfigDOParam = new SftpConfigDO();
            sftpConfigDOParam.setSftpSystem("0");
            SftpConfigDO sftpConfigDO = sftpConfigService.selectBySftpSystem(sftpConfigDOParam);
            ChannelSftp sftp = null;
            // 1、获取用户名和密码
            String sftpAccount = sftpConfigDO.getSftpAccount();
            String decryptSftpAccount = null;
            try {
                decryptSftpAccount = EncryptUtil.decrypt(sftpAccount);
            } catch (Exception e) {
                throw new RuntimeException(e);
            }
            String sftpPassword = sftpConfigDO.getSftpPassword();
            String decryptSftpPassword = null;
            try {
                decryptSftpPassword = EncryptUtil.decrypt(sftpPassword);
            } catch (Exception e) {
                throw new RuntimeException(e);
            }
            SFtpOper sFtpOper = new SFtpOper(decryptSftpAccount, decryptSftpPassword,
                    sftpConfigDO.getSftpIp(), sftpConfigDO.getSftpPort());
            String sftpPath = sftpConfigDO.getSftpUploadPath();
            int i = originalFileName.lastIndexOf(".");
            originalFileName = originalFileName.substring(i);
            String newFileName = Long.toString(loginUserId) + "_" + date + "_" + originalFileName;
            sFtpOper.uploadFileVoice(sftpPath, newFileName, is);
            result.put("voiceOrgUrl", sftpPath + newFileName);*/

            SimpleDateFormat sdf=new SimpleDateFormat("yyyyMMddHHmmss");
            String fileName = file.getOriginalFilename();
            int i = fileName.lastIndexOf(".");
            String suffix=fileName.substring(i+1);
            fileName=fileName.substring(0,i)+"_"+sdf.format(new Date())+"."+suffix;
            String suffixType="";
            switch (suffix){
                case "png":suffixType="1";break;
                case "jpg":suffixType="2";break;
                case "mp4":suffixType="3";break;
            }
            minioClientService.initMinioClient();
            String url = minioClientService.minioUpload(file.getBytes(), "voice/sample/" + fileName, suffix);
            result.put("voiceOrgUrl", url);
            /*String newFileName = "temp_"+date+"."+originalFileName;
            //4.通过上传路径得到上传的文件夹
            File filePath = new File(voicePath);
            //4.1.若目标文件夹不存在，则创建
            if(!filePath.exists()){ //判断目标文件夹是否存在
                filePath.mkdirs();//4.2.不存在，则创建文件夹
            }
            //5.根据目标文件夹和目标文件名新建目标文件（上传后的文件）
            File newFile = new File(filePath,newFileName);  //空的目标文件
            //6.根据目标文件的新建其输出流对象
            FileOutputStream os = new FileOutputStream(newFile);
            //7.完成输入流到输出流的复制
            IOUtils.copy(is,os);
            //8.关闭流(先开后关)
            os.close();
            is.close();
            sFtpOper.uploadFile(sftpConfigDO.getSftpUploadPath(),"temp.mp3",newFile.getPath());*/
            log.info("调用 voiceUpload success：");
        } catch (Exception e) {
            log.info("调用 voiceUpload error：" + e.getMessage());
            return error(GlobalErrorCodeConstants.UNKNOWN);
        }
        return success(result);
    }

    @GetMapping("/voiceSample")
    public ResponseEntity<InputStreamResource> serveAudioStreamFromSftp(@RequestParam String voiceSampleUrl) {

        SftpConfigDO sftpConfigDOParam = new SftpConfigDO();
        sftpConfigDOParam.setSftpSystem("0");
        SftpConfigDO sftpConfigDO = sftpConfigService.selectBySftpSystem(sftpConfigDOParam);

        InputStream inputStream = null;
        ChannelSftp channelSftp = null;


        String sftpIp = sftpConfigDO.getSftpIp();
        Integer sftpPort = sftpConfigDO.getSftpPort();
        String sftpAccount = sftpConfigDO.getSftpAccount();
        String sftpPassword = sftpConfigDO.getSftpPassword();
        String account = null;
        String password = null;
        try {
            account = EncryptUtil.decrypt(sftpAccount);
        } catch (Exception e) {
            throw new RuntimeException(e);
        }
        try {
            password = EncryptUtil.decrypt(sftpPassword);
        } catch (Exception e) {
            e.printStackTrace();
        }

        try {
            // 创建 JSch 实例
            JSch jsch = new JSch();
            Session session = jsch.getSession(account, sftpIp, sftpPort);
            Properties config = new Properties();
            config.put("StrictHostKeyChecking", "no");
            session.setConfig(config);
            session.setPassword(password);
            session.connect();

            // 打开 SFTP 通道
            channelSftp = (ChannelSftp) session.openChannel("sftp");
            channelSftp.connect();

            // 获取文件输入流
            inputStream = channelSftp.get(voiceSampleUrl);

            InputStreamResource resource = new InputStreamResource(inputStream);
            String filename = "audio";
            return ResponseEntity.ok()
                    //.header(HttpHeaders.CONTENT_DISPOSITION, "inline; filename=\"" + filename + "\"")
                    .contentType(MediaType.valueOf("audio/mpeg"))
                    //.contentLength(getFileSize(channelSftp, SFTP_PATH + "/" + filename))
                    .body(resource);
        } catch (Exception e) {
            e.printStackTrace();
            return ResponseEntity.internalServerError().build();
        } finally {
            /*if (channelSftp != null) {
                channelSftp.disconnect();
            }*/
            if (channelSftp != null) {
                if (channelSftp.isConnected()) {
                    channelSftp.disconnect();
                    channelSftp = null;
                    log.info("sftp is closed already");
                }
            }
            /*if (session != null) {
                if (session.isConnected()) {
                    session.disconnect();
                    session = null;
                    log.info("sshSession is closed already");
                }
            }*/
        }
    }

    private long getFileSize(ChannelSftp channelSftp, String filePath) throws SftpException {
        long fileSize = 0;
        try {
            fileSize = channelSftp.lstat(filePath).getSize();
        } catch (SftpException e) {
            e.printStackTrace();
        }
        return fileSize;
    }

    @GetMapping("/voiceSample2")
    public void voiceSample(@RequestParam String voiceSampleUrl, HttpServletResponse response) throws ServletException, IOException {
        log.info("进入声音样例展示 voiceSample 方法：" + voiceSampleUrl);
        SftpConfigDO sftpConfigDOParam = new SftpConfigDO();
        sftpConfigDOParam.setSftpSystem("0");
        SftpConfigDO sftpConfigDO = sftpConfigService.selectBySftpSystem(sftpConfigDOParam);
        ChannelSftp sftp = null;
        // 1、获取用户名和密码
        String sftpAccount = sftpConfigDO.getSftpAccount();
        String decryptSftpAccount = null;
        try {
            decryptSftpAccount = EncryptUtil.decrypt(sftpAccount);
        } catch (Exception e) {
            throw new RuntimeException(e);
        }
        String sftpPassword = sftpConfigDO.getSftpPassword();
        String decryptSftpPassword = null;
        try {
            decryptSftpPassword = EncryptUtil.decrypt(sftpPassword);
        } catch (Exception e) {
            throw new RuntimeException(e);
        }
        SFtpOper sFtpOper = new SFtpOper(decryptSftpAccount, decryptSftpPassword,
                sftpConfigDO.getSftpIp(), sftpConfigDO.getSftpPort());

        InputStream inputStream = null;
        File file = null;
        try {
            sFtpOper.connect();
            inputStream = sFtpOper.getInput(voiceSampleUrl);
            // 设置ContentType和Header
            response.setContentType("audio/mpeg");
            response.setHeader("Content-Disposition", "attachment;filename=\"audio.mp3\"");
            // 创建输出流
            ServletOutputStream outStream = response.getOutputStream();
            try (InputStream in = new BufferedInputStream(inputStream)) {
                byte[] buffer = new byte[4096];
                int bytesRead;
                while ((bytesRead = in.read(buffer)) != -1) {
                    outStream.write(buffer, 0, bytesRead);
                }
            } finally {
                outStream.flush();
                outStream.close();
            }
            log.info("文件流 下载成功：");
        } catch (Exception e) {
            log.info("文件下载异常" + e.getMessage());
        } finally {
            if (inputStream != null) {
                try {
                    inputStream.close();
                } catch (Exception e) {
                    log.info("文件流关闭异常：" + e.getMessage());
                }
            }
            sFtpOper.disconnect();
        }
    }

    @GetMapping("/voiceSample3")
    public ResponseEntity<org.springframework.core.io.Resource> voiceSample3(@RequestParam String voiceSampleUrl, HttpServletResponse response) throws ServletException, IOException {
        log.info("进入声音样例展示 voiceSample3 方法：" + voiceSampleUrl);
        SftpConfigDO sftpConfigDOParam = new SftpConfigDO();
        sftpConfigDOParam.setSftpSystem("0");
        SftpConfigDO sftpConfigDO = sftpConfigService.selectBySftpSystem(sftpConfigDOParam);
        ChannelSftp sftp = null;
        // 1、获取用户名和密码
        String sftpAccount = sftpConfigDO.getSftpAccount();
        String decryptSftpAccount = null;
        try {
            decryptSftpAccount = EncryptUtil.decrypt(sftpAccount);
        } catch (Exception e) {
            throw new RuntimeException(e);
        }
        String sftpPassword = sftpConfigDO.getSftpPassword();
        String decryptSftpPassword = null;
        try {
            decryptSftpPassword = EncryptUtil.decrypt(sftpPassword);
        } catch (Exception e) {
            throw new RuntimeException(e);
        }
        SFtpOper sFtpOper = new SFtpOper(decryptSftpAccount, decryptSftpPassword,
                sftpConfigDO.getSftpIp(), sftpConfigDO.getSftpPort());
        try {
            int i = voiceSampleUrl.lastIndexOf("/");
            File file = sFtpOper.downloadFile(voiceSampleUrl.substring(0, i + 1), voiceSampleUrl.substring(i + 1), "./tmpVoiceFile.mp3");
            Path videoPath = Paths.get("./");
            Path filePath = videoPath.resolve("tmpVoiceFile.mp3");
            org.springframework.core.io.Resource resource = new UrlResource(filePath.toUri());
            if (resource.exists() || resource.isReadable()) {
                return ResponseEntity.ok()
                        .contentType(MediaType.valueOf("audio/mpeg"))
                        .body(resource);
            } else {
                throw new RuntimeException("Could not read file: " + resource);
            }

        } catch (Exception e) {
            log.info("音频预览异常:" + e.getMessage());
        }
        throw new RuntimeException("voice listen error ");
    }

    /**
     * @autor: lkm
     * @time: 2024/7/25 15:20
     * @description: python回调接口
     * @param:
     * @return: 0000成功  9999失败
     */
    @PostMapping("/updateVoice")
    public void updateVoice(@RequestBody HashMap hashMap) {
        log.info("调用python回调接口 updateVoice 修改voice信息:" + JSONObject.toJSONString(hashMap));
        try {
            tbAiDhVoiceService.updateVoice(hashMap);
            log.info("调用python回调接口 updateVoice success:");
        } catch (Exception e) {
            log.info("调用python回调接口 updateVoice error:" + e.getMessage());
        }
    }

    /**
     * @autor: lkm
     * @time: 2024/7/25 16:00
     * @description: hashMap 需要提供 voiceId
     * @param:
     * @return: 0000成功  9999失败
     */
    @PostMapping("/callVoiceClone")
    public CommonResult<String> callVoiceClone(@RequestBody HashMap hashMap) {
        log.info("调用声音克隆接口 callVoiceClone：" + JSONObject.toJSONString(hashMap));
        String result = "";
        try {
            result = tbAiDhVoiceService.callVoiceClone(hashMap);
            if("".equals(result)){
                return error(GlobalErrorCodeConstants.UNKNOWN);
            }
            log.info("调用声音克隆接口 callVoiceClone success：");
        } catch (Exception e) {
            log.info("调用声音克隆接口 callVoiceClone error：" + e.getMessage());
            return error(GlobalErrorCodeConstants.UNKNOWN);
        }
        return success(result);
    }

    /**
     * @autor: lkm
     * @time: 2024/7/25 16:07
     * @description: 声音克隆回调
     * @param:
     * @return: 0000成功  9999失败
     */
    @PostMapping("/updateVoiceBypython")
    public void updateVoiceBypython(@RequestBody HashMap hashMap) {
        log.info("调用python回调接口 updateVoiceBypython 最终修改voice信息:" + JSONObject.toJSONString(hashMap));
        try {
            tbAiDhVoiceService.updateVoiceFinall(hashMap);
            log.info("调用python回调接口 updateVoiceBypython 最终修改voice信息 success");
        } catch (Exception e) {
            log.info("调用python回调接口 updateVoiceBypython error:" + e.getMessage());
        }
    }

    //通过上传wav文件，转为文字信息
    @PostMapping("/voice2Txt")
    @Operation(summary = "语音转文字")
    public CommonResult<String> voice2Txt(@RequestParam("file") MultipartFile file) {
        log.info("voice2Txt 语音转文字：");
        HashMap result = new HashMap();
        try {
            SimpleDateFormat sdf=new SimpleDateFormat("yyyyMMddHHmmss");
            String fileName = file.getOriginalFilename();
            int i = fileName.lastIndexOf(".");
            String suffix=fileName.substring(i+1);
            fileName=fileName.substring(0,i)+"_"+sdf.format(new Date())+"."+suffix;
            String minioPath = "voice2Txt/" + fileName;

            //上传到minio
            minioClientService.initMinioClient();
            minioClientService.minioUpload(file.getBytes(), minioPath, suffix);
            //调用接口
            Map<String, Object> params = new HashMap<>();
            params.put("minioPath",minioPath);
            HashMap data = tbAiDhVoiceService.voice2Txt(params);
            return success(data.get("text")+"");
        } catch (Exception e) {
            log.info("调用 materialUpload error：" + e.getMessage());
            return error(GlobalErrorCodeConstants.UNKNOWN);
        }

    }

}
