package cn.iocoder.yudao.module.digital.controller;

import cn.hutool.core.collection.CollUtil;
import cn.iocoder.yudao.framework.common.pojo.CommonResult;
import cn.iocoder.yudao.framework.common.pojo.PageResult;
import cn.iocoder.yudao.framework.common.result.CustomResult;
import cn.iocoder.yudao.framework.common.result.ResultUtil;
import cn.iocoder.yudao.module.digital.dal.SftpConfigDO;
import cn.iocoder.yudao.module.digital.service.AiDhHumanService;
import cn.iocoder.yudao.module.digital.service.MinioClientService;
import cn.iocoder.yudao.module.digital.service.SftpConfigService;
import cn.iocoder.yudao.module.digital.util.EncryptUtil;
import cn.iocoder.yudao.module.digital.util.SFtpOper;
import cn.iocoder.yudao.module.digital.vo.*;
import com.alibaba.fastjson.JSONObject;
import com.jcraft.jsch.ChannelSftp;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.io.FileUtils;
import org.apache.commons.lang3.StringUtils;
import org.springframework.core.io.ByteArrayResource;
import org.springframework.core.io.UrlResource;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import javax.annotation.Resource;
import javax.servlet.http.HttpServletResponse;
import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.Iterator;
import java.util.Vector;

import static cn.iocoder.yudao.framework.common.pojo.CommonResult.success;
import static cn.iocoder.yudao.framework.security.core.util.SecurityFrameworkUtils.getLoginUserId;

@Tag(name = "数字人")
@RestController
@RequestMapping("/digital-api/system/aiDhHuman")
@Validated
@Slf4j
public class AiDhHumanController {

    /** 示例视频在 MinIO 中的固定对象键，不依赖 tb_ai_dh_human 记录 */
    private static final String EXAMPLE_VIDEO_KEY = "asset/example/123.mp4";

    @Resource
    private AiDhHumanService aiDhHumanService;

    @Resource
    private SftpConfigService sftpConfigService;

    @Resource
    private MinioClientService minioClientService;

    @PostMapping("/create")
    @Operation(summary = "新增数字人")
    /*@PreAuthorize("@ss.hasPermission('digital:aiDhHuman:create')")*/
    public CommonResult<Boolean> createUser(@RequestBody AiDhHumanSaveVO reqVO) throws Exception {
        if (StringUtils.isEmpty(reqVO.getId())){
            throw new Exception("缺少id参数");
        }
        aiDhHumanService.createAiDhHuman(reqVO);
        return success(true);
    }


    @PostMapping("/uploadViedo")
    @Operation(summary = "新增数字人")
    /*@PreAuthorize("@ss.hasPermission('digital:aiDhHuman:uploadViedo')")*/
    public CommonResult<AiDhHumanRespVO> uploadViedo(@RequestParam("file") MultipartFile file,
                                                     @RequestParam("data") String jsonData) throws Exception {
        // 将JSON字符串转换回DigitalPersonVO对象
        AiDhHumanSaveVO reqVO = JSONObject.parseObject(jsonData, AiDhHumanSaveVO.class);
        reqVO.setFile(file);
        AiDhHumanRespVO aiDhHumanRespVO = aiDhHumanService.uploadViedo(reqVO, getLoginUserId());
        return success(aiDhHumanRespVO);
    }


    @PostMapping("/page")
    @Operation(summary = "获得数字人分页列表")
    /*@PreAuthorize("@ss.hasPermission('digital:aiDhHuman:list')")*/
    public CommonResult<PageResult<AiDhHumanRespVO>> getUserPage(@RequestBody AiDhHumanReqVO pageReqVO) {
        // 获得用户分页列表
        PageResult<AiDhHumanRespVO> pageResult = aiDhHumanService.getAiDhHumanPage(pageReqVO, getLoginUserId());
        if (CollUtil.isEmpty(pageResult.getList())) {
            return success(new PageResult<>(pageResult.getTotal()));
        }
        return success(new PageResult<>(pageResult.getList(),pageResult.getTotal()));
    }


    @PostMapping("/getOne")
    @Operation(summary = "获得制作人的基本信息详情")
    @Parameter(name = "id", description = "编号", required = true, example = "1024")
    /*@PreAuthorize("@ss.hasPermission('digital:aiDhHuman:query')")*/
    public CommonResult<AiDhHumanRespVO> getAiDhHuman(@RequestParam("id") String id) {
        AiDhHumanRespVO aiDhHumanRespVO = aiDhHumanService.selectById(id);
        return success(aiDhHumanRespVO);
    }

    @GetMapping("/getOneViedo")
    @Operation(summary = "获得制作人的视频流")
    @Parameter(name = "id", description = "编号", required = true, example = "1024")
    public ResponseEntity<org.springframework.core.io.Resource> getAiDhHumanViedo(@RequestParam("id") String id, HttpServletResponse response) {
        AiDhHumanRespVO aiDhHumanRespVO = aiDhHumanService.selectById(id);
        if (aiDhHumanRespVO == null) {
            return ResponseEntity.notFound().build();
        }
        SftpConfigDO sftpConfigDOParam = new SftpConfigDO();
        sftpConfigDOParam.setSftpSystem("0");
        SftpConfigDO sftpConfigDOData = sftpConfigService.selectBySftpSystem(sftpConfigDOParam);
        return downLoadViedoSFTP(sftpConfigDOData, aiDhHumanRespVO, response);
    }

    @GetMapping("/getExampleVideo")
    @Operation(summary = "获取形象复刻/声音复刻示例视频流")
    public ResponseEntity<org.springframework.core.io.Resource> getExampleVideo() {
        try {
            minioClientService.initMinioClient();
            byte[] bytes = minioClientService.minioDownload(EXAMPLE_VIDEO_KEY);
            ByteArrayResource resource = new ByteArrayResource(bytes);
            return ResponseEntity.ok()
                    .contentType(MediaType.valueOf("video/mp4"))
                    .body(resource);
        } catch (Exception e) {
            throw new RuntimeException(e);
        }
    }

    private ResponseEntity<org.springframework.core.io.Resource> downLoadViedoSFTP(SftpConfigDO sftpConfigDO, AiDhHumanRespVO aiDhHumanRespVO, HttpServletResponse response) {
        try {
            minioClientService.initMinioClient();
            String fileName = aiDhHumanRespVO.getFileName();
            String key = aiDhHumanRespVO.getHumanViedoUrl() + "/" + fileName;
            byte[] bytes = minioClientService.minioDownload(key);
            String format = fileName.substring(fileName.lastIndexOf(".") + 1);
            ByteArrayResource resource = new ByteArrayResource(bytes);
            return ResponseEntity.ok()
                    .contentType(MediaType.valueOf("video/" + format))
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

    @GetMapping("/delete")
    @Operation(summary = "删除数字人")
    @Parameter(name = "id", description = "编号", required = true, example = "1024")
    /*@PreAuthorize("@ss.hasPermission('digital:aiDhHuman:delete')")*/
    public CommonResult<Boolean> deleteAiDhHuman(@RequestParam("id") String id) throws Exception{
        if (StringUtils.isEmpty(id)){
            throw new Exception("缺少id参数");
        }
        log.info("删除数字人: {}", id);
        aiDhHumanService.deleteAiDhHuman(id);
        return success(true);
    }

    @PostMapping("/update")
    @Operation(summary = "修改数字人")
    /*@PreAuthorize("@ss.hasPermission('digital:aiDhHuman:update')")*/
    public CommonResult<Boolean> updateAiDhHuman(@RequestBody AiDhHumanUpdateVO reqVO) throws Exception {
        if (StringUtils.isEmpty(reqVO.getId())){
            throw new Exception("缺少id参数");
        }
        aiDhHumanService.updateAiDhHuman(reqVO, getLoginUserId());
        return success(true);
    }

    @PostMapping("/callBackAiDhHuman")
    @Operation(summary = "回调函数")
    public CommonResult<Boolean> callBackAiDhHuman(@RequestBody CallBackVo callBackVo) throws Exception {
        if (StringUtils.isEmpty(callBackVo.getId())){
            throw new Exception("缺少id参数");
        }
        aiDhHumanService.callBackAiDhHuman(callBackVo);
        return success(true);
    }
}
