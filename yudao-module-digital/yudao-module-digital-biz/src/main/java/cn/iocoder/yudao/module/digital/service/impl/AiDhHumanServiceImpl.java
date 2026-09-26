package cn.iocoder.yudao.module.digital.service.impl;

import cn.hutool.core.collection.CollUtil;
import cn.iocoder.yudao.framework.common.pojo.PageResult;
import cn.iocoder.yudao.framework.mybatis.core.query.LambdaQueryWrapperX;
import cn.iocoder.yudao.module.digital.adaptor.AiDhHumanAdaptor;
import cn.iocoder.yudao.module.digital.dal.AiDhHumanDO;
import cn.iocoder.yudao.module.digital.dal.SftpConfigDO;
import cn.iocoder.yudao.module.digital.dal.mysql.AiDhHumanMapper;
import cn.iocoder.yudao.module.digital.framework.file.config.DigitalAbilityConfig;
import cn.iocoder.yudao.module.digital.service.AiDhHumanService;
import cn.iocoder.yudao.module.digital.service.CallPythonService;
import cn.iocoder.yudao.module.digital.service.MinioClientService;
import cn.iocoder.yudao.module.digital.service.SftpConfigService;
import cn.iocoder.yudao.module.digital.util.EncryptUtil;
import cn.iocoder.yudao.module.digital.util.FtpUtils;
import cn.iocoder.yudao.module.digital.util.SFtpOper;
import cn.iocoder.yudao.module.digital.util.SequenceUtils;
import cn.iocoder.yudao.module.digital.vo.*;
import com.alibaba.fastjson.JSONObject;
import com.jcraft.jsch.ChannelSftp;
import com.jcraft.jsch.JSchException;
import com.jcraft.jsch.SftpException;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.codec.binary.Base64;
import org.apache.commons.lang3.StringUtils;
import org.apache.commons.net.ftp.FTPClient;
import org.apache.commons.net.ftp.FTPFile;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.core.io.ClassPathResource;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.util.StreamUtils;
import org.springframework.web.multipart.MultipartFile;

import javax.annotation.Resource;
import java.io.*;
import java.util.*;
import java.util.stream.Collectors;

/**
 * @program: ai_digital
 * @description: 数字人
 * @author: huangyx
 * @create: 2024-07-13 15:10
 **/
@Service("aiDhHumanService")
@Slf4j
public class AiDhHumanServiceImpl implements AiDhHumanService {


    @Resource
    private AiDhHumanMapper aiDhHumanMapper;

    @Resource
    private SftpConfigService sftpConfigService;

    @Resource
    private CallPythonService callPythonService;

    @Resource
    private DigitalAbilityConfig digitalAbilityConfig;

    @Autowired
    private MinioClientService minioClientService;

    /**
     * 创建数字人形象
     *
     * @param aiDhHumanSaveVO 用户信息
     */
    @Override
    @Transactional(rollbackFor = Exception.class)
    public String createAiDhHuman(AiDhHumanSaveVO aiDhHumanSaveVO) throws Exception {
        SftpConfigDO sftpConfigDOParam = new SftpConfigDO();
        sftpConfigDOParam.setSftpSystem("0");
        SftpConfigDO sftpConfigDOData = sftpConfigService.selectBySftpSystem(sftpConfigDOParam);
        String backInfo = chekftpConfig(sftpConfigDOData);
        if (backInfo != null) {
            log.error(backInfo);
        }
        Map<String, String> param = new HashMap<String, String>();
        if ("1".equals(aiDhHumanSaveVO.getHumanBg())) {
            param.put("sftpUploadGeneratePath", "avatar/" + aiDhHumanSaveVO.getId() + "/matting");
        }
        AiDhHumanRespVO aiDhHumanRespVO = selectById(aiDhHumanSaveVO.getId());
        aiDhHumanMapper.update(AiDhHumanAdaptor.updatevo.apply(aiDhHumanSaveVO, param),
                new LambdaQueryWrapperX<AiDhHumanDO>()
                        .eqIfPresent(AiDhHumanDO::getId, aiDhHumanSaveVO.getId())
        );

        //背景替换 0：保留 1：去除背景
        if ("0".equals(aiDhHumanSaveVO.getHumanBg())) {
            //2、异步调用Python抠图
            JSONObject params = new JSONObject();
            params.put("host", sftpConfigDOData.getSftpIp());
            params.put("username", sftpConfigDOData.getSftpAccount());
            params.put("password", sftpConfigDOData.getSftpPassword());
            params.put("port", sftpConfigDOData.getSftpPort());
            params.put("id", aiDhHumanSaveVO.getId());
            params.put("remote_path", "avatar/"+aiDhHumanSaveVO.getId()+"/original");
            params.put("file_name", aiDhHumanRespVO.getFileName());
            params.put("remote_image_path", "avatar/"+aiDhHumanSaveVO.getId()+"/image");
            log.info("callPythonChangeImega params: {}", JSONObject.toJSONString(params));
            callPythonService.callPythonChangeImega(params);
        } else if ("1".equals(aiDhHumanSaveVO.getHumanBg())) {
            //3、调用Python视频和抠图
            JSONObject params = new JSONObject();
            params.put("host", sftpConfigDOData.getSftpIp());
            params.put("username", sftpConfigDOData.getSftpAccount());
            params.put("password", sftpConfigDOData.getSftpPassword());
            params.put("port", sftpConfigDOData.getSftpPort());
            params.put("id", aiDhHumanSaveVO.getId());
            params.put("remote_path", "avatar/"+aiDhHumanSaveVO.getId()+"/original");
            params.put("file_name", aiDhHumanRespVO.getFileName());
            params.put("remote_image_path",  "avatar/"+aiDhHumanSaveVO.getId()+"/image");
            params.put("remote_viedo_path", "avatar/"+aiDhHumanSaveVO.getId()+"/matting");
            log.info("callPythonChangeImegaAndViedo params: {}", JSONObject.toJSONString(params));
            callPythonService.callPythonChangeImegaAndViedo(params);
        }
        return "success";
    }


    @Override
    @Transactional(rollbackFor = Exception.class)
    public AiDhHumanRespVO uploadViedo(AiDhHumanSaveVO aiDhHumanSaveVO, Long userId) throws Exception {
        SftpConfigDO sftpConfigDOParam = new SftpConfigDO();
        sftpConfigDOParam.setSftpSystem("0");
        SftpConfigDO sftpConfigDOData = sftpConfigService.selectBySftpSystem(sftpConfigDOParam);
        String backInfo = chekftpConfig(sftpConfigDOData);
        if (backInfo != null) {
            log.error(backInfo);
        }
        String id = String.valueOf(SequenceUtils.getSeq());
        String sftpUploadPath = sftpConfigDOData.getSftpUploadPath();
        String sftpUploadImegaPath = sftpConfigDOData.getSftpUploadPath();
        String sftpUploadNewViedoPath = sftpConfigDOData.getSftpUploadPath();
        if (!sftpUploadPath.endsWith("/")) {
            sftpUploadPath = sftpUploadPath + "/";
            sftpUploadImegaPath = sftpUploadImegaPath + "/";
        }
        MultipartFile file = aiDhHumanSaveVO.getFile();
        Map<String, String> param = new HashMap<String, String>();
        param.put("id", String.valueOf(id));
        sftpUploadPath = "avatar/" + id + "/original";
        sftpUploadImegaPath = "avatar/" + id + "/image";
        param.put("sftpUploadViedoPath", sftpUploadPath);
        param.put("sftpUploadImegaPath", sftpUploadImegaPath);

        /*param.put("staffId", String.valueOf(userId));
        param.put("fileName", file.getOriginalFilename());
        param.put("imageName", id + "_rgba.png");*/

        String fileName = file.getOriginalFilename();
        String format = fileName.substring(fileName.lastIndexOf("."));
        String newFileName = id + format;
        param.put("staffId", String.valueOf(userId));
        param.put("fileName",newFileName);
        //修改 插入的时候默认图片，好前端显示
        param.put("imageName", "123.png");
        //1、视频上传到 MinIO
        uploadVideo(sftpConfigDOData, file.getInputStream(), id, newFileName, sftpUploadPath);
        //1.1、上传默认占位图，处理中记录封面可显示
        uploadDefaultImage(sftpUploadImegaPath);

        int insert = aiDhHumanMapper.insert(AiDhHumanAdaptor.saveCovDo.apply(aiDhHumanSaveVO, param));
        AiDhHumanRespVO aiDhHumanRespVO = new AiDhHumanRespVO();
        aiDhHumanRespVO.setId(id);
        aiDhHumanRespVO.setHumanViedoUrl(sftpUploadPath);
        return aiDhHumanRespVO;
    }

    private void uploadVideo(SftpConfigDO sftpConfigDO, InputStream inputStream, String id, String fileName, String sftpUploadPath) {
        try {
            minioClientService.initMinioClient();
            String key = MinioClientService.toObjectKey(sftpUploadPath) + "/" + fileName;
            minioClientService.minioUploadStream(inputStream, key, MinioClientService.extension(fileName));
        } catch (Exception e) {
            throw new RuntimeException(e);
        }
    }

    private void uploadDefaultImage(String imagePath) {
        try {
            byte[] placeholder;
            try (InputStream in = new ClassPathResource("placeholder/123.png").getInputStream()) {
                placeholder = StreamUtils.copyToByteArray(in);
            }
            minioClientService.initMinioClient();
            minioClientService.minioUpload(placeholder, imagePath + "/123.png", "png");
        } catch (Exception e) {
            log.error("上传默认占位图失败: {}", e.getMessage(), e);
        }
    }

    /**
     * 获得数字人形象分页列表
     *
     * @param reqVO 分页条件
     * @return 分页列表
     */
    @Override
    public PageResult<AiDhHumanRespVO> getAiDhHumanPage(AiDhHumanReqVO reqVO, Long userId) {
        AiDhHumanQueryVO queryDO = null;
        if ("0".equals(reqVO.getQueryType())) {
            queryDO = AiDhHumanAdaptor.queryCovDoForAll.apply(String.valueOf(userId), reqVO);
        } else {
            queryDO = AiDhHumanAdaptor.queryCovDoForHumanShare.apply(reqVO);
        }
        PageResult<AiDhHumanDO> aiDhHumanPageResult = aiDhHumanMapper.selectPage(queryDO);
        if (CollUtil.isEmpty(aiDhHumanPageResult.getList())) {
            return new PageResult<>(aiDhHumanPageResult.getTotal());
        }
        SftpConfigDO sftpConfigDOParam = new SftpConfigDO();
        sftpConfigDOParam.setSftpSystem("0");
        SftpConfigDO sftpConfigDOData = sftpConfigService.selectBySftpSystem(sftpConfigDOParam);
        String backInfo = chekftpConfig(sftpConfigDOData);
        List<AiDhHumanRespVO> retList = aiDhHumanPageResult.getList().stream().map(aiDhHumanDO -> {
            AiDhHumanRespVO retVO = new AiDhHumanRespVO();
            retVO.setId(aiDhHumanDO.getId());
            retVO.setHumanName(aiDhHumanDO.getHumanName());
            retVO.setHumanStatus(aiDhHumanDO.getHumanStatus());
            //retVO.setHumanImageUrl(downLoadImage(sftpConfigDOData, aiDhHumanDO));
            retVO.setHumanImageUrl(digitalAbilityConfig.getMinioConfig().get("imageUrl")+"/"+MinioClientService.toObjectKey(aiDhHumanDO.getFirstFrame())+"/"+aiDhHumanDO.getImageName());
            //一个一个从ftp下载并且转换成base64
            return retVO;
        }).collect(Collectors.toList());
        return new PageResult<>(retList, aiDhHumanPageResult.getTotal());
    }

    private String downLoadImage(SftpConfigDO sftpConfigDO, AiDhHumanDO aiDhHumanDO) {
        try{
            if ("0".equals(sftpConfigDO.getSftpMode())) {
                //ftp
                return downLoadImageFTP(sftpConfigDO, aiDhHumanDO);
            } else if ("1".equals(sftpConfigDO.getSftpMode())) {
                //sftp
                return downLoadImageSFTP(sftpConfigDO, aiDhHumanDO);
            }
            return null;
        }catch (Exception e){
            log.error("downLoadImage error",e);
            return null;
        }
    }

    private String downLoadImageFTP(SftpConfigDO sftpConfigDO, AiDhHumanDO aiDhHumanDO) {
        FTPClient ftp = null;
        // 1、获取用户名和密码
        String sftpAccount = sftpConfigDO.getSftpAccount();
        String decryptSftpAccount = null;
        try {
            decryptSftpAccount = EncryptUtil.decrypt(sftpAccount);
        } catch (Exception e) {
        }
        String sftpPassword = sftpConfigDO.getSftpPassword();
        String decryptSftpPassword = null;
        try {
            decryptSftpPassword = EncryptUtil.decrypt(sftpPassword);
        } catch (Exception e) {
        }
        FtpUtils ftpModel = new FtpUtils(decryptSftpAccount, decryptSftpPassword,
                sftpConfigDO.getSftpIp(), sftpConfigDO.getSftpPort());
        ftp = ftpModel.getFtpClient();//获取链接
        try {
            ftp.changeWorkingDirectory(aiDhHumanDO.getFirstFrame());//到对应的路径下
            ftp.enterLocalPassiveMode();
            FTPFile[] ftpFiles = ftp.listFiles();
            if (null == ftpFiles || 0 == ftpFiles.length) {
                log.error("相关文件服务器目标路径" + aiDhHumanDO.getFirstFrame() + "没有文件");
                return null;
            }
            String directoryFileName = aiDhHumanDO.getImageName();
            for (FTPFile file : ftpFiles) {
                String ftpFileName = file.getName();
                if (".".equals(ftpFileName) || "..".equals(ftpFileName)) {
                    continue;
                }
                // 只扫描文件
                /*if (!file.isFile()) {
                    continue;
                }*/
                // 只扫描解析指定文件
                if (-1 == ftpFileName.indexOf(directoryFileName)) {
                    continue;
                }
                InputStream inputStream = ftp.retrieveFileStream(ftpFileName);
                return getImageBase64String(inputStream);
            }
        } catch (Exception e) {
        } finally {
            if (ftp != null) {
                try {
                    ftp.disconnect();
                } catch (IOException e) {
                }
            }
        }
        return null;
    }

    private String getImageBase64String(InputStream inputStream) throws Exception {
        ByteArrayOutputStream byteArrayOutputStream = new ByteArrayOutputStream();
        byte[] buffer = new byte[1024];
        int bytesRead;

        while ((bytesRead = inputStream.read(buffer)) != -1) {
            byteArrayOutputStream.write(buffer, 0, bytesRead);
        }

        byte[] imageBytes = byteArrayOutputStream.toByteArray();
        String base64Image = Base64.encodeBase64String(imageBytes);
        return base64Image;
    }

    private String downLoadImageSFTP(SftpConfigDO sftpConfigDO, AiDhHumanDO aiDhHumanDO) {
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

        Vector vector = null; //vector.size()
        try {
            sftp = sFtpOper.connect();//获取链接
            sftp.cd(aiDhHumanDO.getFirstFrame());//到对应的路径下
            vector = sftp.ls(aiDhHumanDO.getFirstFrame());
            if (null == vector || 0 == vector.size()) {
                log.error("相关文件服务器目标路径" + aiDhHumanDO.getFirstFrame() + "没有文件");
                return null;
            }
            String directoryFileName = aiDhHumanDO.getImageName();
            Iterator iterator = vector.iterator();
            while (iterator.hasNext()) {
                ChannelSftp.LsEntry fileInfo = (ChannelSftp.LsEntry) iterator.next();
                String sftpFilename = fileInfo.getFilename();
                if (".".equals(sftpFilename) || "..".equals(sftpFilename)) {
                    continue;
                }

                // 只扫描解析指定文件
                if (-1 == sftpFilename.indexOf(directoryFileName)) {
                    continue;
                }

                InputStream inputStream = sftp.get(sftpFilename);
                return getImageBase64String(inputStream);
            }
        } catch (Exception e) {
            throw new RuntimeException(e);
        } finally {
            if (sftp != null) {
                sftp.disconnect();
            }
        }
        return null;
    }


    private String chekftpConfig(SftpConfigDO sftpConfigDO) {
        if (StringUtils.isBlank(sftpConfigDO.getSftpAccount())) {
            log.error("TbSSftpConfig, 没有配置账号");
            return "没有配置账号";
        }
        if (StringUtils.isBlank(sftpConfigDO.getSftpPassword())) {
            log.error("TbSSftpConfig, 没有配置密码");
            return "没有配置密码";
        }

        if (StringUtils.isBlank(sftpConfigDO.getSftpIp())) {
            log.error("TbSSftpConfig, 没有配置ip");
            return "没有配置ip";
        }

        if (StringUtils.isBlank(sftpConfigDO.getSftpMode())) {
            log.error("TbSSftpConfig, 没有配置模式");
            return "没有配置模式";
        }
        if (StringUtils.isBlank(sftpConfigDO.getSftpUploadPath())) {
            log.error("TbSSftpConfig, 没有配置上传目录");
            return "没有配置上传目录";
        }
        return null;
    }

    private void uploadImage(SftpConfigDO sftpConfigDO, byte[] imageInByte, String id, String fileName) {
        try {
            minioClientService.initMinioClient();
            String key = "avatar/" + id + "/image/" + fileName;
            minioClientService.minioUpload(imageInByte, key, MinioClientService.extension(fileName));
        } catch (Exception e) {
            throw new RuntimeException(e);
        }
    }

    private void uploadImageViaFTP(SftpConfigDO sftpConfigDO, byte[] imageInByte, String id, String fileName) {
        FTPClient ftp = null;
        try {
            // 1、获取用户名和密码
            String sftpAccount = sftpConfigDO.getSftpAccount();
            String decryptSftpAccount = EncryptUtil.decrypt(sftpAccount);
            String sftpPassword = sftpConfigDO.getSftpPassword();
            String decryptSftpPassword = EncryptUtil.decrypt(sftpPassword);
            FtpUtils ftpModel = new FtpUtils(decryptSftpAccount, decryptSftpPassword,
                    sftpConfigDO.getSftpIp(), sftpConfigDO.getSftpPort());
            ftp = ftpModel.getFtpClient();//获取链接
            String sftpUploadPath = sftpConfigDO.getSftpUploadPath();
            if (!sftpUploadPath.endsWith("/")) {
                sftpUploadPath = sftpUploadPath + "/";
            }
            sftpUploadPath = sftpUploadPath + id + "/image";
            boolean fileReceipt = ftpModel.uploadFileByteToFtp(sftpUploadPath, fileName, imageInByte);
        } catch (Exception e) {
            throw new RuntimeException(e);
        } finally {
            if (ftp != null) {
                try {
                    ftp.disconnect();
                } catch (IOException e) {
                    throw new RuntimeException(e);
                }
            }
        }


    }

    private void uploadImageViaSFTP(SftpConfigDO sftpConfigDO, byte[] imageInByte, String id, String fileName) {
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

        String sftpUploadPath = sftpConfigDO.getSftpUploadPath();
        if (!sftpUploadPath.endsWith("/")) {
            sftpUploadPath = sftpUploadPath + "/";
        }
        sftpUploadPath = sftpUploadPath + id + "/image";
        boolean fileReceipt = false;
        try {
            fileReceipt = sFtpOper.uploadFileContent(sftpUploadPath, fileName, imageInByte);
        } catch (JSchException e) {
            throw new RuntimeException(e);
        } finally {
            sFtpOper.disconnect();
        }
    }


    private void uploadVideoFTP(SftpConfigDO sftpConfigDO, InputStream inputStream, String id, String fileName, String sftpUploadPath) {
        FTPClient ftp = null;
        // 1、获取用户名和密码
        try {
            String sftpAccount = sftpConfigDO.getSftpAccount();
            String decryptSftpAccount = EncryptUtil.decrypt(sftpAccount);
            String sftpPassword = sftpConfigDO.getSftpPassword();
            String decryptSftpPassword = EncryptUtil.decrypt(sftpPassword);
            FtpUtils ftpModel = new FtpUtils(decryptSftpAccount, decryptSftpPassword,
                    sftpConfigDO.getSftpIp(), sftpConfigDO.getSftpPort());
            ftp = ftpModel.getFtpClient();//获取链接
            boolean fileReceipt = ftpModel.uploadFileToFtp(sftpUploadPath, fileName, inputStream);
        } catch (Exception e) {
            throw new RuntimeException(e);
        } finally {
            if (ftp != null) {
                try {
                    ftp.disconnect();
                } catch (IOException e) {
                    throw new RuntimeException(e);
                }
            }
        }


    }

    private void uploadVideoSFTP(SftpConfigDO sftpConfigDO, InputStream inputStream, String id, String fileName, String sftpUploadPath) {
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
        } finally {
            if (sftp != null) {
                sftp.disconnect();
            }
        }
    }


    @Override
    public String createAiDhHumanTest() throws Exception {
        File file = new File("C:\\workSoftWare\\uploadFile\\1111111.mp4");
        InputStream is = new FileInputStream(file);
        SftpConfigDO sftpConfigDOParam = new SftpConfigDO();
        sftpConfigDOParam.setSftpSystem("0");
        SftpConfigDO sftpConfigDOData = sftpConfigService.selectBySftpSystem(sftpConfigDOParam);
        String backInfo = chekftpConfig(sftpConfigDOData);
        if (backInfo != null) {
            log.error(backInfo);
            return backInfo;
        }
        String id = String.valueOf(SequenceUtils.getSeq());
        String sftpUploadPath = sftpConfigDOData.getSftpUploadPath();
        if (!sftpUploadPath.endsWith("/")) {
            sftpUploadPath = sftpUploadPath + "/";
        }
        Map<String, String> param = new HashMap<String, String>();
        param.put("id", String.valueOf(id));
        sftpUploadPath = "avatar/" + id + "/original";
        //2、视频存放到服务器上去
        uploadVideo(sftpConfigDOData, is, id, file.getName(), sftpUploadPath);
        return "success";
    }

    @Override
    public AiDhHumanRespVO selectById(String id) {
        return AiDhHumanAdaptor.doCovAllRespVO.apply(aiDhHumanMapper.selectById(id));
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public void deleteAiDhHuman(String id) {
        AiDhHumanRespVO aiDhHumanRespVO = selectById(id);
        aiDhHumanMapper.deleteById(id);
        /*SftpConfigDO sftpConfigDOParam = new SftpConfigDO();
        sftpConfigDOParam.setSftpSystem("0");
        SftpConfigDO sftpConfigDOData = sftpConfigService.selectBySftpSystem(sftpConfigDOParam);
        deleteAiDhHumanPath(sftpConfigDOData, id, aiDhHumanRespVO);*/
    }


    private void deleteAiDhHumanPath(SftpConfigDO sftpConfigDO, String id, AiDhHumanRespVO aiDhHumanRespVO) {
        if ("0".equals(sftpConfigDO.getSftpMode())) {
            //ftp
            deleteAiDhHumanPathFTP(sftpConfigDO, id, aiDhHumanRespVO);
        } else if ("1".equals(sftpConfigDO.getSftpMode())) {
            //sftp
            deleteAiDhHumanPathSFTP(sftpConfigDO, id, aiDhHumanRespVO);
        }
    }

    private void deleteAiDhHumanPathFTP(SftpConfigDO sftpConfigDO, String id, AiDhHumanRespVO aiDhHumanRespVO) {
        FTPClient ftp = null;
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
        FtpUtils ftpModel = new FtpUtils(decryptSftpAccount, decryptSftpPassword,
                sftpConfigDO.getSftpIp(), sftpConfigDO.getSftpPort());
        ftp = ftpModel.getFtpClient();//获取链接
        try {
            ftp.removeDirectory(aiDhHumanRespVO.getHumanGenerateUrl());
            ftp.removeDirectory(aiDhHumanRespVO.getHumanOrgUrl());
            ftp.removeDirectory(aiDhHumanRespVO.getHumanImageUrl());
        } catch (Exception e) {
            throw new RuntimeException(e);
        } finally {
            if (ftp != null) {
                try {
                    ftp.disconnect();
                } catch (IOException e) {
                    throw new RuntimeException(e);
                }
            }
        }
    }


    private void deleteAiDhHumanPathSFTP(SftpConfigDO sftpConfigDO, String id, AiDhHumanRespVO aiDhHumanRespVO) {
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
            sftp = sFtpOper.connect();//获取链接
            removeDirectory(sftpConfigDO.getSftpUploadPath() + "/" + id, sftp);
        } catch (Exception e) {
            throw new RuntimeException(e);
        } finally {
            if (sftp != null) {
                sftp.disconnect();
            }
        }
    }

    public void removeDirectory(String remotePath, ChannelSftp sftp) throws SftpException {
        Vector<ChannelSftp.LsEntry> entries = sftp.ls(remotePath);
        for (ChannelSftp.LsEntry entry : entries) {
            String filename = entry.getFilename();
            if (!filename.equals(".") && !filename.equals("..")) {
                String filePath = remotePath + "/" + filename;
                if (entry.getAttrs().isDir()) {
                    // Recursively delete subdirectories
                    removeDirectory(filePath, sftp);
                } else {
                    // Delete files
                    sftp.rm(filePath);
                }
            }
        }
        // After deleting all contents, delete the directory itself
        sftp.rmdir(remotePath);
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public void updateAiDhHuman(AiDhHumanUpdateVO reqVO, Long userId) {
        Map<String, String> param = new HashMap<String, String>();
        param.put("staffId", String.valueOf(userId));
        aiDhHumanMapper.update(AiDhHumanAdaptor.updateCovDo.apply(reqVO, param), new LambdaQueryWrapperX<AiDhHumanDO>()
                .eqIfPresent(AiDhHumanDO::getId, reqVO.getId()));

        /*AiDhHumanRespVO aiDhHumanRespVO = selectById(reqVO.getId());
        Map<String,String> param = new HashMap<String, String>();

        SftpConfigDO sftpConfigDOParam = new SftpConfigDO();
        sftpConfigDOParam.setSftpSystem("0");
        SftpConfigDO sftpConfigDOData = sftpConfigService.selectBySftpSystem(sftpConfigDOParam);
        String backInfo = chekftpConfig(sftpConfigDOData);
        if(backInfo != null){
            log.error(backInfo);
        }
        if(!reqVO.getHumanBg().equals(aiDhHumanRespVO.getHumanBg())){
            MultipartFile file = reqVO.getFile();
            //1、视频存放到服务器上去
            try {
                uploadVideo(sftpConfigDOData,file.getInputStream(),reqVO.getId(),file.getName(),aiDhHumanRespVO.getHumanOrgUrl());
            } catch (IOException e) {
                throw new RuntimeException(e);
            }
            aiDhHumanMapper.update(AiDhHumanAdaptor.updateCovDo.apply(reqVO, param),new LambdaQueryWrapperX<AiDhHumanDO>()
                    .eqIfPresent(AiDhHumanDO::getId, reqVO.getId()));
            //背景替换 0：保留 1：去除背景
            if("0".equals(reqVO.getHumanBg())){
                //2、异步调用Python抠图
                JSONObject params = new JSONObject();
                params.put("host",sftpConfigDOData.getSftpIp());
                params.put("username",sftpConfigDOData.getSftpAccount());
                params.put("password",sftpConfigDOData.getSftpPassword());
                params.put("port",sftpConfigDOData.getSftpPort());
                params.put("id",reqVO.getId());
                params.put("remote_path",aiDhHumanRespVO.getHumanOrgUrl());
                params.put("file_name",aiDhHumanRespVO.getFileName());
                params.put("remote_image_path",aiDhHumanRespVO.getHumanImageUrl());
                log.info("callPythonChangeImega params:", JSONObject.toJSONString(params));
                callPythonService.callPythonChangeImega(params);
            } else if ("1".equals(reqVO.getHumanBg())) {
                //3、调用Python视频和抠图
                JSONObject params = new JSONObject();
                params.put("host",sftpConfigDOData.getSftpIp());
                params.put("username",sftpConfigDOData.getSftpAccount());
                params.put("password",sftpConfigDOData.getSftpPassword());
                params.put("port",sftpConfigDOData.getSftpPort());
                params.put("id",reqVO.getId());
                params.put("remote_path",aiDhHumanRespVO.getHumanOrgUrl());
                params.put("file_name",aiDhHumanRespVO.getFileName());
                params.put("remote_image_path",aiDhHumanRespVO.getHumanImageUrl());
                params.put("remote_viedo_path",aiDhHumanRespVO.getHumanOrgUrl());
                log.info("callPythonChangeImegaAndViedo params:", JSONObject.toJSONString(params));
                callPythonService.callPythonChangeImegaAndViedo(params);
            }
        }*/

    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public void callBackAiDhHuman(CallBackVo callBackVo) {
        SftpConfigDO sftpConfigDOParam = new SftpConfigDO();
        sftpConfigDOParam.setSftpSystem("0");
        SftpConfigDO sftpConfigDOData = sftpConfigService.selectBySftpSystem(sftpConfigDOParam);
        String backInfo = chekftpConfig(sftpConfigDOData);
        String sftpUploadPath = sftpConfigDOData.getSftpUploadPath();
        if (!sftpUploadPath.endsWith("/")) {
            sftpUploadPath = sftpUploadPath + "/";
        }
        AiDhHumanDO aiDhHumanDO = AiDhHumanAdaptor.updateStringCovDo.apply(callBackVo.getStatus());
        if("4".equals(callBackVo.getStatus())){
            aiDhHumanDO.setImageName(callBackVo.getId()+"_rgba.png");
            aiDhHumanDO.setFirstFrame("avatar/"+callBackVo.getId()+"/image");
        }
        //1 视频和扣  2 图片
        if ("1".equals(callBackVo.getOperType())) {
            aiDhHumanMapper.update(aiDhHumanDO, new LambdaQueryWrapperX<AiDhHumanDO>()
                    .eqIfPresent(AiDhHumanDO::getId, callBackVo.getId()));
        } else if ("2".equals(callBackVo.getOperType())) {
            // 视频抠图完事，需要更新状态
            aiDhHumanMapper.update(aiDhHumanDO, new LambdaQueryWrapperX<AiDhHumanDO>()
                    .eqIfPresent(AiDhHumanDO::getId, callBackVo.getId()));
        }
    }
}
