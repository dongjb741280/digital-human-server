package cn.iocoder.yudao.module.digital.controller;

import cn.iocoder.yudao.framework.common.exception.enums.GlobalErrorCodeConstants;
import cn.iocoder.yudao.framework.common.pojo.CommonResult;
import cn.iocoder.yudao.framework.common.pojo.PageResult;
import cn.iocoder.yudao.framework.common.result.CustomResult;
import cn.iocoder.yudao.framework.common.result.Pager;
import cn.iocoder.yudao.framework.common.result.ResultUtil;
import cn.iocoder.yudao.module.digital.dal.AiDhHumanVideoDO;
import cn.iocoder.yudao.module.digital.dal.SftpConfigDO;
import cn.iocoder.yudao.module.digital.service.AiDhHumanService;
import cn.iocoder.yudao.module.digital.service.AiDhHumanVideoService;
import cn.iocoder.yudao.module.digital.service.MinioClientService;
import cn.iocoder.yudao.module.digital.service.SftpConfigService;
import cn.iocoder.yudao.module.digital.util.EncryptUtil;
import cn.iocoder.yudao.module.digital.util.SFtpOper;
import cn.iocoder.yudao.module.digital.vo.*;
import com.jcraft.jsch.ChannelSftp;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.io.FileUtils;
import org.springframework.core.io.ByteArrayResource;
import org.springframework.core.io.UrlResource;

import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;

import javax.annotation.Resource;
import javax.servlet.http.HttpServletResponse;
import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.*;

import static cn.iocoder.yudao.framework.common.pojo.CommonResult.error;
import static cn.iocoder.yudao.framework.common.pojo.CommonResult.success;
import static cn.iocoder.yudao.framework.security.core.util.SecurityFrameworkUtils.getLoginUserId;


@RestController
@RequestMapping("/digital-api/system/aiDhHumanVideo")
@Slf4j
public class AiDhHumanVideoController {

    @Resource
    private AiDhHumanVideoService aiDhHumanVideoService;

    @Resource
    private SftpConfigService sftpConfigService;

    @Resource
    private MinioClientService minioClientService;

    @PostMapping("/save")
    public CommonResult<Map> saveAiDhHumanVideo(@RequestBody AiDhHumanVideoSaveVO reqVO) {
        String staffId = getLoginUserId() + "";
        try {
            reqVO.setOprStaff(staffId);
            Map<String, Object> stringObjectMap = aiDhHumanVideoService.saveAiDhHumanVideo(reqVO);
             return success(stringObjectMap);
        } catch (Exception e) {
            e.printStackTrace();
            return error(GlobalErrorCodeConstants.UNKNOWN);
        }
    }

    @PostMapping("/page")
    public CommonResult getAiDhHumanVideoPage(@RequestBody AiDhHumanVideoReqVO pageReqVO) {
        String staffId = getLoginUserId() + "";
        // 获得用户分页列表
        try {
            pageReqVO.setStaffId(staffId);
            Map<String, Object> aiDhHumanVideoPage = aiDhHumanVideoService.getAiDhHumanVideoPage(pageReqVO);
            return success(aiDhHumanVideoPage);
        } catch (Exception e) {
            e.printStackTrace();
            return error(GlobalErrorCodeConstants.UNKNOWN);
        }
    }

    @PostMapping("/getVideoOne")
    public CommonResult getVideoOne(@RequestBody Map<String, Object> map) {
        AiDhHumanVideoSaveVO aiDhHumanVideoDO = null;
        String id = map.get("id") + "";
        try {
            aiDhHumanVideoDO = aiDhHumanVideoService.selectById(id);
            aiDhHumanVideoDO.setFirstFrame(null);
            aiDhHumanVideoDO.setVideoUrl(null);
        } catch (Exception e) {
            log.error("获取视频详情失败，id: {}", id, e);
            return CommonResult.error(GlobalErrorCodeConstants.UNKNOWN.getCode(), e.getMessage());
        }
        return success(aiDhHumanVideoDO);
    }


    @GetMapping("/getOneVideoIO")
    public ResponseEntity<org.springframework.core.io.Resource> getAiDhHumanVideoIo(@RequestParam("id") String id, HttpServletResponse response) {
        AiDhHumanVideoSaveVO aiDhHumanVideoDO = aiDhHumanVideoService.selectById(id);
        SftpConfigDO sftpConfigDOParam = new SftpConfigDO();
        sftpConfigDOParam.setSftpSystem("5");
        SftpConfigDO sftpConfigDOData = sftpConfigService.selectBySftpSystem(sftpConfigDOParam);
        return downLoadViedoSFTP(id,sftpConfigDOData, aiDhHumanVideoDO, response);
    }

    private ResponseEntity<org.springframework.core.io.Resource> downLoadViedoSFTP(String id,SftpConfigDO sftpConfigDO, AiDhHumanVideoSaveVO aiDhHumanVideoDO, HttpServletResponse response) {
        try {
            minioClientService.initMinioClient();
            String key = MinioClientService.toObjectKey(aiDhHumanVideoDO.getVideoUrl()) + "/" + aiDhHumanVideoDO.getVideoFinalName();
            byte[] bytes = minioClientService.minioDownload(key);
            ByteArrayResource resource = new ByteArrayResource(bytes);
            return ResponseEntity.ok()
                    .contentType(MediaType.valueOf("video/mp4"))
                    .body(resource);
        } catch (Exception e) {
            throw new RuntimeException(e);
        }
    }


    public File convertInputStreamToFile(InputStream inputStream, String filePath) {
        File file = new File(filePath);
        try {
            FileUtils.copyInputStreamToFile(inputStream, file);
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
        return file;
    }

    @PostMapping("/update")
    public CommonResult updateAiDhHuman( @RequestBody AiDhHumanVideoSaveVO reqVO) {
        String staffId = getLoginUserId()+"";
        try {
            reqVO.setOprStaff(staffId);
            aiDhHumanVideoService.updateAiDhHumanVideo(reqVO);
        } catch (Exception e) {
            return error(GlobalErrorCodeConstants.UNKNOWN);
        }
        return  success(null);
    }

    @PostMapping("/delete")
    public CommonResult deleteAiDhHumanVideo(@RequestBody Map<String,Object> params) {
        try {
            aiDhHumanVideoService.deleteAiDhHumanVideo(params);
        } catch (Exception e) {
            return error(GlobalErrorCodeConstants.UNKNOWN);
        }
        return  success(null);
    }



//    @PostMapping("/updateRecordVoiceHuman")
//    public CommonResult updateRecordVoiceHuman(@RequestBody Map<String,Object> params) {
//        try {
//            int i = aiDhHumanVideoService.updateRecordVoiceHuman(params);
//        } catch (Exception e) {
//            return error(GlobalErrorCodeConstants.UNKNOWN);
//        }
//        return  success(null);
//    }

    //视频配音之后的回调接口 + 视频替换背景之后的回调接口
    @PostMapping("/updateRecordVideo")
    public CommonResult updateRecordVideoImage(@RequestBody Map<String,Object> params) {
        try {
            int i = aiDhHumanVideoService.updateRecordVideoImage(params);
        } catch (Exception e) {
            return error(GlobalErrorCodeConstants.UNKNOWN);
        }
        return  success(null);
    }

    //通过文案id查找第一张图片
    @PostMapping("/getFirstPptImageByCopywriteId")
    public CommonResult getFirstPptImageByCopywriteId(@RequestBody Map<String,Object> params) {
        Map<String, Object> result = new HashMap<>();
        try {
            result= aiDhHumanVideoService.getFirstPptImageByCopywriteId(params);
        } catch (Exception e) {
            return error(GlobalErrorCodeConstants.UNKNOWN);
        }
        return  success(result);
    }

}
