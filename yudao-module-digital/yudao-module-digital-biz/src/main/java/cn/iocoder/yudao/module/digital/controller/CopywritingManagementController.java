package cn.iocoder.yudao.module.digital.controller;


import cn.hutool.core.collection.CollUtil;
import cn.iocoder.yudao.framework.common.exception.enums.GlobalErrorCodeConstants;
import cn.iocoder.yudao.framework.common.pojo.CommonResult;
import cn.iocoder.yudao.framework.common.pojo.PageResult;
import cn.iocoder.yudao.framework.common.util.date.DateUtils;
import cn.iocoder.yudao.module.digital.dal.AiCopyWritePptRecordDO;
import cn.iocoder.yudao.module.digital.dal.SftpConfigDO;
import cn.iocoder.yudao.module.digital.service.MinioClientService;
import cn.iocoder.yudao.module.digital.service.SftpConfigService;
import cn.iocoder.yudao.module.digital.service.copywriting.CopywritingManagementService;
import cn.iocoder.yudao.module.digital.util.EncryptUtil;
import cn.iocoder.yudao.module.digital.util.FtpUtils;
import cn.iocoder.yudao.module.digital.util.SFtpOper;
import cn.iocoder.yudao.module.digital.util.SequenceUtils;
import cn.iocoder.yudao.module.digital.vo.*;
import com.alibaba.fastjson.JSONObject;
import com.jcraft.jsch.*;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang3.StringUtils;
import org.apache.commons.net.ftp.FTPClient;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import javax.annotation.Resource;
import javax.servlet.http.HttpServletResponse;

import java.io.*;
import java.net.URLEncoder;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.Properties;
import java.util.UUID;

import static cn.iocoder.yudao.framework.common.pojo.CommonResult.success;
import static cn.iocoder.yudao.framework.security.core.util.SecurityFrameworkUtils.getLoginUserId;

@Tag(name = "文案管理")
@RestController
@RequestMapping("/digital-api/system/copyWritManage")
@Validated
@Slf4j
public class CopywritingManagementController {


    @Resource
    private CopywritingManagementService copywritingManagementService;

    @Resource
    private SftpConfigService sftpConfigService;

    @Resource
    private MinioClientService minioClientService;

    @PostMapping("/page")
    @Operation(summary = "获得文案分页列表")
    public CommonResult<PageResult<CopywritingRespVO>> getCopywritingPage(@RequestBody CopywritingReqVO pageReqVO) {
        // 获得用户分页列表

        PageResult<CopywritingRespVO> pageResult = copywritingManagementService.copywritingListQuery(pageReqVO);
        if (CollUtil.isEmpty(pageResult.getList())) {
            return success(new PageResult<>(pageResult.getTotal()));
        }
        return success(new PageResult<>(pageResult.getList(),pageResult.getTotal()));
    }

    @PostMapping("/delete")
    @Operation(summary = "删除文案")
    public CommonResult deleteCopywriting(@RequestBody CopywritingUpdateReqVO pageReqVO) {
        try {
            copywritingManagementService.copywritingDelete(pageReqVO);
        }catch (Exception e){
            log.error("删除文案处理异常",e);
            return CommonResult.error(GlobalErrorCodeConstants.UNKNOWN.getCode(),"删除文案处理异常");
        }
        return success(true);
    }
    //查看文案列表
    @PostMapping("/getOne")
    @Operation(summary = "查看文案列表")
    public CommonResult<CopywritingRespVO> getCopywritingOne(@RequestBody CopywritingReqVO pageReqVO) {
        CopywritingRespVO copywritingRespVO = copywritingManagementService.copywritingGetOne(pageReqVO);
        return success(copywritingRespVO);
    }

    @PostMapping("/update")
    @Operation(summary = "更新文案")
    public CommonResult updateCopywriting(@RequestBody CopywritingUpdateReqVO copywritingUpdateReqVO) {
        try {
            copywritingManagementService.copywritingUpdate(copywritingUpdateReqVO);
        }catch (Exception e){
            log.error("更新文案处理异常",e);
            return CommonResult.error(GlobalErrorCodeConstants.UNKNOWN.getCode(),"更新文案处理异常");
        }
        return success(true);
    }

    @PostMapping("/pagePPT")
    @Operation(summary = "获得文案ppt分页列表")
    public CommonResult<PageResult<CopyWritePptRecordRespVO>> getCopywritingPPTPage(@RequestBody CopywritingReqVO pageReqVO) {
        // 获得用户分页列表

        PageResult<CopyWritePptRecordRespVO> pageResult = copywritingManagementService.copywritingListPPT(pageReqVO);
        if (CollUtil.isEmpty(pageResult.getList())) {
            return success(new PageResult<>(pageResult.getTotal()));
        }
        return success(new PageResult<>(pageResult.getList(),pageResult.getTotal()));
    }

    @PostMapping("/deletePPT")
    @Operation(summary = "删除PPT")
    public CommonResult deleteCopywritingPPT(@RequestBody CopywritingPptReqVO copywritingPptReqVO) {
        try {
            copywritingManagementService.copywritingPPTDelete(copywritingPptReqVO);
        }catch (Exception e){
            log.error("删除文案处理异常",e);
            return CommonResult.error(GlobalErrorCodeConstants.UNKNOWN.getCode(),"删除文案处理异常");
        }
        return success(true);
    }

    //ppt 上传接口
    @PostMapping("/uploadPPT")
    @Operation(summary = "ppt 上传接口")
    public CommonResult uploadPPT( @RequestPart("pptFIle") MultipartFile file,
                                   @RequestParam("id") String id,
                                   @RequestParam("oprStaff") String oprStaff) {
        // 检查文件是否为空
        if (file.isEmpty()) {
            return CommonResult.error(GlobalErrorCodeConstants.UNKNOWN.getCode(),"请选择文件进行上传");
        }
        // 验证文件类型
        String originalFilename = file.getOriginalFilename();
        String fileExtension ="ppt";
        if (originalFilename != null && originalFilename.contains(".")) {
            int lastDotIndex = originalFilename.lastIndexOf('.');
            fileExtension = originalFilename.substring(lastDotIndex + 1).toLowerCase();
         }

        if (!originalFilename.endsWith(".ppt") && !originalFilename.endsWith(".pptx")) {
            return CommonResult.error(GlobalErrorCodeConstants.UNKNOWN.getCode(),"仅支持.ppt和.pptx格式的文件");
        }
        SftpConfigDO sftpConfigDOParam = new SftpConfigDO();
        sftpConfigDOParam.setSftpSystem("3");
        SftpConfigDO sftpConfigDOData = sftpConfigService.selectBySftpSystem(sftpConfigDOParam);
        //重命名文件 防止文件名重复
        String newFileName = SequenceUtils.getSeq() + "_" + originalFilename;
        //上传 ppt
        try {
            uploadFile(sftpConfigDOData,file.getInputStream(),newFileName);
        } catch (IOException e) {
             log.error("文件上传失败",e);
            return CommonResult.error(GlobalErrorCodeConstants.UNKNOWN.getCode(),"文件上传失败");
        }
        //保存 ppt 信息
        String fileName = file.getOriginalFilename();
        long fileSize = file.getSize();
        CopywritingPptReqVO copywritingPptReqVO = new CopywritingPptReqVO();
        copywritingPptReqVO.setCopywriteId(id);
        copywritingPptReqVO.setOprStaff(oprStaff);
        copywritingPptReqVO.setFileName(fileName);
        copywritingPptReqVO.setFilePath(MinioClientService.toObjectKey(sftpConfigDOData.getSftpUploadPath())+newFileName);
        copywritingPptReqVO.setFileSize(String.valueOf(fileSize));
        copywritingPptReqVO.setFileFormat(fileExtension);
        try {
            copywritingManagementService.copywritingUploadPPT(copywritingPptReqVO);
        }catch (Exception e){
            log.error("保存 ppt 信息异常",e);
            return CommonResult.error(GlobalErrorCodeConstants.UNKNOWN.getCode(),"保存ppt信息失败");
        }
        return success(true);
    }
//
//    //下载ppt
//    @PostMapping("/downloadPPT")
//    @Operation(summary = "下载ppt")
//    public void downloadPPT(HttpServletResponse response, @RequestBody CopywritingPptReqVO copywritingPptReqVO) {
//
//        AiCopyWritePptRecordDO aiCopyWritePptRecordDO = copywritingManagementService.copywritingDownloadPPT(copywritingPptReqVO);
//        if (aiCopyWritePptRecordDO == null) {
//            return;
//        }
//        String filePath = aiCopyWritePptRecordDO.getPptUrl();
//        if (filePath == null) {
//            return;
//        }
//        try {
//            //获取下载文件的配置信息
//            SftpConfigDO sftpConfigDOParam = new SftpConfigDO();
//            sftpConfigDOParam.setSftpSystem("1");
//            SftpConfigDO sftpConfigDO = sftpConfigService.selectBySftpSystem(sftpConfigDOParam);
//
//            FtpUtils ftpModel = new FtpUtils(sftpConfigDO.getSftpAccount(), sftpConfigDO.getSftpPassword(),
//                    sftpConfigDO.getSftpIp(), sftpConfigDO.getSftpPort());
//            FTPClient ftp = ftpModel.getFtpClient();//获取链接
//
//            ftp.enterLocalPassiveMode();
//            //通过 ftp 下载文件
//
//
//            File file = new File(filePath);
//            if (file.exists()) {
//                response.setContentType("application/octet-stream");
//                response.setHeader("Content-Disposition", "attachment; filename=" + URLEncoder.encode(aiCopyWritePptRecordDO.getRecordDesc(), "UTF-8"));
//                response.setHeader("Content-Length", String.valueOf(file.length()));
//                try (FileInputStream fis = new FileInputStream(file);
//                     OutputStream os = response.getOutputStream()) {
//                    byte[] buffer = new byte[1024];
//                     }
//            }
//        }
//        catch (Exception e) {
//            log.error("下载ppt异常",e);
//        }
//    }
//


    @PostMapping("/downloadPPT")
    @Operation(summary = "下载ppt")
    public void downloadPPT(HttpServletResponse response, @RequestBody CopywritingPptReqVO copywritingPptReqVO) {
        try {
            AiCopyWritePptRecordDO aiCopyWritePptRecordDO = copywritingManagementService.copywritingDownloadPPT(copywritingPptReqVO);
            if (aiCopyWritePptRecordDO == null || StringUtils.isBlank(aiCopyWritePptRecordDO.getPptUrl())) {
                return;
            }

            // 从 MinIO 下载 PPT
            minioClientService.initMinioClient();
            byte[] pptBytes = minioClientService.minioDownload(MinioClientService.toObjectKey(aiCopyWritePptRecordDO.getPptUrl()));

            // 准备响应头信息并发送文件
            response.setContentType("application/octet-stream");
            response.setHeader("Content-Disposition", "attachment; filename=" + URLEncoder.encode(aiCopyWritePptRecordDO.getRecordDesc(), StandardCharsets.UTF_8.name()));
            response.setHeader("Content-Length", String.valueOf(pptBytes.length));
            response.getOutputStream().write(pptBytes);
            response.getOutputStream().flush();
        } catch (Exception e) {
            log.error("下载ppt异常", e);
        }
    }

    private void downloadFile(SftpConfigDO sftpConfigDO, String pathName, String targetFileName, String localPath) {
        FTPClient ftp = null;
        if ("0".equals(sftpConfigDO.getSftpMode())) {
            FtpUtils ftpModel = new FtpUtils(sftpConfigDO.getSftpAccount(), sftpConfigDO.getSftpPassword(),
                    sftpConfigDO.getSftpIp(), sftpConfigDO.getSftpPort());
            ftp = ftpModel.getFtpClient();//获取链接
            boolean fileReceipt = ftpModel.downloadFile(ftp, pathName, targetFileName, localPath);

        }else {

        }
    }

    private void uploadFile(SftpConfigDO sftpConfigDO, InputStream inputStream,  String fileName ){
        try {
            minioClientService.initMinioClient();
            String key = MinioClientService.toObjectKey(sftpConfigDO.getSftpUploadPath()) + "/" + fileName;
            minioClientService.minioUploadStream(inputStream, key, MinioClientService.extension(fileName));
        } catch (Exception e) {
            throw new RuntimeException(e);
        }
    }

    private void uploadFileFTP(SftpConfigDO sftpConfigDO,InputStream inputStream,String fileName,String sftpUploadPath) {
        FTPClient ftp = null;
        // 1、获取用户名和密码
        try{
            String sftpAccount = sftpConfigDO.getSftpAccount();
            String decryptSftpAccount = EncryptUtil.decrypt(sftpAccount);
            String sftpPassword = sftpConfigDO.getSftpPassword();
            String decryptSftpPassword = EncryptUtil.decrypt(sftpPassword);
            FtpUtils ftpModel = new FtpUtils(decryptSftpAccount, decryptSftpPassword,
                    sftpConfigDO.getSftpIp(), sftpConfigDO.getSftpPort());
            ftp = ftpModel.getFtpClient();//获取链接
            boolean fileReceipt = ftpModel.uploadFileToFtp(sftpUploadPath, fileName, inputStream);
        }catch (Exception e) {
            throw new RuntimeException(e);
        }finally {
            if(ftp != null){
                try {
                    ftp.disconnect();
                } catch (IOException e) {
                    throw new RuntimeException(e);
                }
            }
        }


    }
    private void uploadFileSFTP(SftpConfigDO sftpConfigDO,InputStream inputStream,String fileName,String sftpUploadPath) {
        ChannelSftp sftp = null;
        // 1、获取用户名和密码
        try {
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
            sFtpOper.uploadFile(sftpUploadPath, fileName, inputStream);
        } catch (JSchException e) {
            throw new RuntimeException(e);
        }finally {
            if(sftp != null){
                sftp.disconnect();
            }
        }
    }


}
