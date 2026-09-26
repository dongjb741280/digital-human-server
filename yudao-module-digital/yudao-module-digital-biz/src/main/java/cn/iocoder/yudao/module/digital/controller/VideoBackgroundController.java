package cn.iocoder.yudao.module.digital.controller;

import cn.iocoder.yudao.framework.common.exception.enums.GlobalErrorCodeConstants;
import cn.iocoder.yudao.framework.common.pojo.CommonResult;
import cn.iocoder.yudao.module.digital.dal.SftpConfigDO;
import cn.iocoder.yudao.module.digital.dal.TbAiInteractHumanDo;
import cn.iocoder.yudao.module.digital.entity.TbAiDhVideoBackground;
import cn.iocoder.yudao.module.digital.entity.TbAiDhVideoMaterial;
import cn.iocoder.yudao.module.digital.service.MinioClientService;
import cn.iocoder.yudao.module.digital.service.VideoBackgroundService;
import cn.iocoder.yudao.module.digital.util.EncryptUtil;
import cn.iocoder.yudao.module.digital.util.SFtpOper;
import cn.iocoder.yudao.module.digital.vo.AiDhHumanVideoReqVO;
import cn.iocoder.yudao.module.digital.vo.AiDhHumanVideoSaveVO;
import com.alibaba.fastjson.JSONObject;
import com.jcraft.jsch.ChannelSftp;
import io.swagger.v3.oas.annotations.Operation;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import java.io.InputStream;
import java.text.SimpleDateFormat;
import java.time.LocalDateTime;
import java.util.Date;
import java.util.HashMap;
import java.util.Map;
import java.util.UUID;

import static cn.iocoder.yudao.framework.common.pojo.CommonResult.error;
import static cn.iocoder.yudao.framework.common.pojo.CommonResult.success;
import static cn.iocoder.yudao.framework.security.core.util.SecurityFrameworkUtils.getLoginUserId;

/**
 * @author lkm
 * @ClassName:
 * @Description:
 * @date 2024-09-12-14:14
 */
@RestController
@RequestMapping("/digital-api/system/videoBackground")
@Slf4j
public class VideoBackgroundController {
    @Autowired
    private VideoBackgroundService videoBackgroundService;
    @Autowired
    private MinioClientService minioClientService;
    /**
    *@autor: lkm
    *@time: 2024/9/12 14:24
    *@description: ai数字人视频素材查询
    *@param:
    *@return: 0000成功  9999失败
    */
    @PostMapping("/materialQuery")
    @Operation(summary = "ai数字人视频素材查询")
    public CommonResult<HashMap> materialQuery(@RequestBody HashMap hashMap) {
        log.info("进入materialQuery ai数字人视频素材查询方法：" + JSONObject.toJSONString(hashMap));
        HashMap result = null;
        try {
            result = videoBackgroundService.materialQuery(hashMap);
            log.info("调用 materialQuery success：" + JSONObject.toJSONString(hashMap));
        } catch (Exception e) {
            log.info("调用 materialQuery error：" + e.getMessage());
            return error(GlobalErrorCodeConstants.UNKNOWN);
        }
        return success(result);
    }
    /**
    *@autor: lkm
    *@time: 2024/9/12 14:33
    *@description: ai数字人视频素材
    *@param:
    *@return: 0000成功  9999失败
    */
    @PostMapping("/materialDel")
    @Operation(summary = "删除数字人视频素材")
    public CommonResult<HashMap> materialDel(@RequestBody HashMap hashMap) {
        log.info("进入materialDel 删除数字人视频素材方法：" + JSONObject.toJSONString(hashMap));
        HashMap result = null;
        try {
            Long loginUserId = getLoginUserId();
            hashMap.put("staffId",Long.toString(loginUserId));
            result = videoBackgroundService.materialDel(hashMap);
            log.info("调用 materialDel success：" + JSONObject.toJSONString(hashMap));
        } catch (Exception e) {
            log.info("调用 materialDel error：" + e.getMessage());
            return error(GlobalErrorCodeConstants.UNKNOWN);
        }
        return success(result);
    }
    /**
    *@autor: lkm
    *@time: 2024/9/12 14:45
    *@description: ai数字人视频上传
    *@param:
    *@return: 0000成功  9999失败
    */
    @PostMapping("/materialUpload")
    @Operation(summary = "ai数字人视频上传")
    public CommonResult<HashMap> materialUpload(@RequestParam("file") MultipartFile file,@RequestParam String type,@RequestParam String bgShare) {
        log.info("materialUpload ai数字人视频上传方法：");
        HashMap result = new HashMap();
        try {
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
            String url = minioClientService.minioUpload(file.getBytes(), "asset/material/" + fileName, suffix);
            TbAiDhVideoMaterial tbAiDhVideoMaterial=new TbAiDhVideoMaterial();
            String s = UUID.randomUUID().toString();
            String id=s.substring(0,2)+sdf.format(new Date());
            tbAiDhVideoMaterial.setId(id);
            tbAiDhVideoMaterial.setMaterialName(fileName);
            tbAiDhVideoMaterial.setMaterialUrl(url);
            tbAiDhVideoMaterial.setMaterialType(type);
            tbAiDhVideoMaterial.setBgFormat(suffixType);
            Long loginUserId = getLoginUserId();
            tbAiDhVideoMaterial.setCreator(Long.toString(loginUserId));
            tbAiDhVideoMaterial.setCreateTime(new Date());
            tbAiDhVideoMaterial.setBgShare(bgShare);
            videoBackgroundService.insertMaterial(tbAiDhVideoMaterial);
            result.put("data", 0);
            result.put("id", tbAiDhVideoMaterial.getId());
            result.put("url", url);
            log.info("调用 materialUpload success：");
        } catch (Exception e) {
            log.info("调用 materialUpload error：" + e.getMessage());
            return error(GlobalErrorCodeConstants.UNKNOWN);
        }
        return success(result);
    }
    /**
    *@autor: lkm
    *@time: 2024/9/12 16:01
    *@description: 背景查询
    *@param:
    *@return: 0000成功  9999失败
    */
    @PostMapping("/backgroundQuery")
    @Operation(summary = "背景查询")
    public CommonResult<HashMap> backgroundQuery(@RequestBody HashMap hashMap) {
        log.info("进入backgroundQuery 背景查询方法：" + JSONObject.toJSONString(hashMap));
        HashMap result = null;
        try {
            result = videoBackgroundService.backgroundQuery(hashMap);
            log.info("调用 backgroundQuery success：" + JSONObject.toJSONString(hashMap));
        } catch (Exception e) {
            log.info("调用 backgroundQuery error：" + e.getMessage());
            return error(GlobalErrorCodeConstants.UNKNOWN);
        }
        return success(result);
    }
    /**
    *@autor: lkm
    *@time: 2024/9/12 16:08
    *@description: 背景删除
    *@param:
    *@return: 0000成功  9999失败
    */
    @PostMapping("/backgroundDel")
    @Operation(summary = "背景删除")
    public CommonResult<HashMap> backgroundDel(@RequestBody HashMap hashMap) {
        log.info("进入backgroundDel 背景删除：" + JSONObject.toJSONString(hashMap));
        HashMap result = null;
        try {
            Long loginUserId = getLoginUserId();
            hashMap.put("staffId",Long.toString(loginUserId));
            result = videoBackgroundService.backgroundDel(hashMap);
            log.info("调用 backgroundDel success：" + JSONObject.toJSONString(hashMap));
        } catch (Exception e) {
            log.info("调用 backgroundDel error：" + e.getMessage());
            return error(GlobalErrorCodeConstants.UNKNOWN);
        }
        return success(result);
    }
    /**
    *@autor: lkm
    *@time: 2024/9/12 16:16
    *@description: 背景上传
    *@param:
    *@return: 0000成功  9999失败
    */
    @PostMapping("/backgroundUpload")
    @Operation(summary = "背景上传")
    public CommonResult<HashMap> backgroundUpload(@RequestParam("file") MultipartFile file,@RequestParam String bgShare) {
        log.info("backgroundUpload 背景上传：");
        HashMap result = new HashMap();
        try {
            SimpleDateFormat sdf=new SimpleDateFormat("yyyyMMddHHmmss");
            String fileName = file.getOriginalFilename();
            int i = fileName.lastIndexOf(".");
            String suffix=fileName.substring(i+1);
            fileName=fileName.substring(0,i)+"_"+sdf.format(new Date())+"."+suffix;
            String suffixType="";
            String bgType="0";
            switch (suffix){
                case "png":suffixType="1";bgType="0";break;
                case "jpg":suffixType="2";bgType="0";break;
                case "mp4":suffixType="3";bgType="1";break;
            }
            minioClientService.initMinioClient();
            String url = minioClientService.minioUpload(file.getBytes(), "asset/background/" + fileName, suffix);
            TbAiDhVideoBackground tbAiDhVideoBackground=new TbAiDhVideoBackground();
            String s = UUID.randomUUID().toString();
            String id=s.substring(0,2)+sdf.format(new Date());
            tbAiDhVideoBackground.setId(id);
            tbAiDhVideoBackground.setBgName(fileName);
            tbAiDhVideoBackground.setBgUrl(url);
            tbAiDhVideoBackground.setBgType(bgType);
            tbAiDhVideoBackground.setBgFormat(suffixType);
            Long loginUserId = getLoginUserId();
            tbAiDhVideoBackground.setCreator(Long.toString(loginUserId));
            tbAiDhVideoBackground.setCreateTime(new Date());
            tbAiDhVideoBackground.setBgShare(bgShare);
            videoBackgroundService.insertBackground(tbAiDhVideoBackground);
            result.put("data", 0);
            result.put("id", tbAiDhVideoBackground.getId());
            result.put("url", url);
            log.info("调用 backgroundUpload success：");
        } catch (Exception e) {
            log.info("调用 backgroundUpload error：" + e.getMessage());
            return error(GlobalErrorCodeConstants.UNKNOWN);
        }
        return success(result);
    }
    /**
    *@autor: lkm
    *@time: 2024/9/21 9:30
    *@description: 数字人互动合成保存
    *@param:
    *@return: 0000成功  9999失败
    */
    @PostMapping("/composeSave")
    public CommonResult<Map> composeSave(@RequestBody TbAiInteractHumanDo tbAiInteractHumanDo) {
        log.info("数字人互动合成保存 composeSave:"+JSONObject.toJSONString(tbAiInteractHumanDo));
        String staffId = getLoginUserId() + "";
        try {
            tbAiInteractHumanDo.setCreator(staffId);
            Map<String, Object> stringObjectMap = videoBackgroundService.composeSave(tbAiInteractHumanDo);
            log.info("数字人互动合成保存 composeSave success");
            return success(stringObjectMap);
        } catch (Exception e) {
            log.info("数字人互动合成保存 composeSave error:"+e.getMessage());
            return error(GlobalErrorCodeConstants.UNKNOWN);
        }
    }
    /**
    *@autor: lkm
    *@time: 2024/9/21 15:25
    *@description: 数字人互动合成列表
    *@param:
    *@return: 0000成功  9999失败
    */
    @PostMapping("/composePage")
    public CommonResult composePage(@RequestBody TbAiInteractHumanDo tbAiInteractHumanDo) {
        log.info("数字人互动合成列表 composePage:"+JSONObject.toJSONString(tbAiInteractHumanDo));
        String staffId = getLoginUserId() + "";
        // 获得用户分页列表
        try {
            tbAiInteractHumanDo.setCreator(staffId);
            Map<String, Object> aiDhHumanVideoPage = videoBackgroundService.composePage(tbAiInteractHumanDo);
            log.info("数字人互动合成列表 composePage success");
            return success(aiDhHumanVideoPage);
        } catch (Exception e) {
            log.info("数字人互动合成列表 composePage error:"+e.getMessage());
            return error(GlobalErrorCodeConstants.UNKNOWN);
        }
    }
    /**
    *@autor: lkm
    *@time: 2024/9/21 15:25
    *@description: 数字人互动删除
    *@param:
    *@return: 0000成功  9999失败
    */
    @PostMapping("/composeDel")
    public CommonResult composeDel(@RequestBody TbAiInteractHumanDo tbAiInteractHumanDo) {
        log.info("数字人互动删除 composeDel:"+JSONObject.toJSONString(tbAiInteractHumanDo));
        String staffId = getLoginUserId() + "";
        try {
            tbAiInteractHumanDo.setUpdater(staffId);
            Map<String, Object> aiDhHumanVideoPage = videoBackgroundService.composeDel(tbAiInteractHumanDo);
            log.info("数字人互动删除 composeDel success");
            return success(aiDhHumanVideoPage);
        } catch (Exception e) {
            log.info("数字人互动删除 composeDel error:"+e.getMessage());
            return error(GlobalErrorCodeConstants.UNKNOWN);
        }
    }
    /**
    *@autor: lkm
    *@time: 2024/9/21 15:59
    *@description: 数字人互动详情
    *@param:
    *@return: 0000成功  9999失败
    */
    @PostMapping("/composeQueOne")
    public CommonResult composeQueOne(@RequestBody Map<String, Object> map) {
        log.info("数字人互动详情 composeQueOne:"+JSONObject.toJSONString(map));
        TbAiInteractHumanDo tbAiInteractHumanDo = null;
        String id = map.get("id") + "";
        try {
            tbAiInteractHumanDo = videoBackgroundService.composeQueOne(id);
            log.info("数字人互动详情 composeQueOne success");
        } catch (Exception e) {
            log.info("数字人互动详情 composeQueOne error:"+e.getMessage());
            return error(GlobalErrorCodeConstants.UNKNOWN);
        }
        return success(tbAiInteractHumanDo);
    }
}
