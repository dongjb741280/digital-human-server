package cn.iocoder.yudao.module.digital.controller;

import cn.iocoder.yudao.framework.common.exception.enums.GlobalErrorCodeConstants;
import cn.iocoder.yudao.framework.common.pojo.CommonResult;
import cn.iocoder.yudao.module.digital.service.AiDhPptService;
import com.alibaba.fastjson.JSONObject;
import com.alibaba.nacos.shaded.io.grpc.netty.shaded.io.netty.util.internal.StringUtil;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang3.StringUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.nio.charset.StandardCharsets;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import static cn.iocoder.yudao.framework.common.pojo.CommonResult.error;
import static cn.iocoder.yudao.framework.common.pojo.CommonResult.success;

/**
 * Project:AiDhPptController
 * Author:lilj
 * Date:2024/7/22
 * Description:
 */
@Tag(name = "ppt接口调用")
@RestController
@RequestMapping("/digital-api/system/aiDhPpt")
@Validated
@Slf4j
public class AiDhPptController {

    @Autowired
    private AiDhPptService aiDhPptService;

    private static Map<String, String> fileMap = new HashMap<>();

    /**
     * @autor: llj
     * @time: 2024/7/22
     * @description: 模板查询
     * @param:
     * @return: 0000成功  9999失败
     */
    @PostMapping("/getppt")
    @Operation(summary = "模板查询")
    public CommonResult<List<Map>> getppt() {
        log.info("getppt 模板查询方法：");
        JSONObject result = null;
        try {
            result = aiDhPptService.getppt();
            if(result != null){
                if("0000".equals(result.getString("code"))){
                    log.info("调用 getppt success：" + result);
                    List<Map> lists = JSONObject.parseArray(result.getJSONArray("data").toJSONString(), Map.class);

                    return success(lists);
                }else {
                    log.info("调用 getppt error：" + result);

                    return CommonResult.error(GlobalErrorCodeConstants.UNKNOWN.getCode(), result.getString("msg"));
                }

            }else{
                log.info("调用 getppt error：" + result);
                return CommonResult.error(GlobalErrorCodeConstants.UNKNOWN);

            }
        } catch (Exception e) {
            log.info("调用 getppt error：" + e.getMessage());
            return CommonResult.error(GlobalErrorCodeConstants.UNKNOWN);
        }
    }

    /**
     * @autor: llj
     * @time: 2024/7/22
     * @description: 生成大纲文体
     * @param:
     * @return: 0000成功  9999失败
     */
    @PostMapping("/generate_body")
    @Operation(summary = "生成大纲文体")
    public CommonResult<JSONObject> generateBody(@RequestBody JSONObject reqParam) {
        log.info("generateBody 生成大纲文体方法：" + reqParam);
        JSONObject result = null;
        try {
            if(reqParam.containsKey("fileId") && !StringUtil.isNullOrEmpty(reqParam.getString("fileId"))){
                String  fileConent = fileMap.get(reqParam.getString("fileId"));
                if(!StringUtil.isNullOrEmpty(fileConent)){
                    reqParam.put("fileConent",fileConent);
                }
            }
            result = aiDhPptService.generateBody(reqParam);
            if(result != null){
                if("0000".equals(result.getString("code"))){
                    log.info("调用 generateBody success：" + result);
                    JSONObject data = result.getJSONObject("data");
                    return success(data);
                }else {
                    log.info("调用 generateBody error：" + result);

                    return CommonResult.error(GlobalErrorCodeConstants.UNKNOWN.getCode(), result.getString("msg"));
                }
            }else{
                log.info("调用 generateBody error：" + result);
                return CommonResult.error(GlobalErrorCodeConstants.UNKNOWN);

            }
        } catch (Exception e) {
            log.info("调用 generateBody error：" + e.getMessage());
            return error(GlobalErrorCodeConstants.UNKNOWN);
        }
    }

    /**
     * @autor: llj
     * @time: 2024/7/22
     * @description: 生成ppt
     * @param:
     * @return: 0000成功  9999失败
     */
    @PostMapping("/generate_ppt")
    @Operation(summary = "生成ppt")
    public CommonResult<JSONObject> generatePpt(@RequestBody JSONObject reqParam) {
        log.info("generatePpt 生成ppt方法：" + reqParam);
        JSONObject result = null;
        try {
            result = aiDhPptService.generatePpt(reqParam);

            if(result != null){
                if("0000".equals(result.getString("code"))){
                    log.info("调用 generatePpt success：" + result);
                    //JSONObject data = result.getJSONObject("data");
                    JSONObject data = new JSONObject();
                    return success(data);
                }else {
                    log.info("调用 generatePpt error：" + result);

                    return CommonResult.error(GlobalErrorCodeConstants.UNKNOWN.getCode(), result.getString("msg"));
                }
            }else{
                log.info("调用 generatePpt error：" + result);
                return CommonResult.error(GlobalErrorCodeConstants.UNKNOWN);

            }
        } catch (Exception e) {
            log.info("调用 generatePpt error：" + e.getMessage());
            return error(GlobalErrorCodeConstants.UNKNOWN);
        }

    }

    /**
     * @autor: llj
     * @time: 2024/7/22
     * @description: 大纲生成接口
     * @param:
     * @return: 0000成功  9999失败
     */
    @PostMapping("/pilgrimage")
    @Operation(summary = "ppt转图片接口")
    public CommonResult<JSONObject> pilgrimage(@RequestBody JSONObject reqParam) {
        log.info("ppttoimage ppt转图片接口：" + reqParam);
        JSONObject result = null;
        try {
            result = aiDhPptService.pilgrimage(reqParam);
            if(result != null){
                if("0000".equals(result.getString("code"))){
                    log.info("调用 ppttoimage success：" + result);
                    JSONObject data = new JSONObject();
                    return success(data);
                }else {
                    log.info("调用 ppttoimage error：" + result);

                    return CommonResult.error(GlobalErrorCodeConstants.UNKNOWN.getCode(), result.getString("msg"));
                }
            }else{
                log.info("调用 ppttoimage error：" + result);
                return CommonResult.error(GlobalErrorCodeConstants.UNKNOWN);

            }
        } catch (Exception e) {
            log.info("调用 ppttoimage error：" + e.getMessage());
            return error(GlobalErrorCodeConstants.UNKNOWN);
        }
    }

    @PostMapping("/generate_outline")
    @Operation(summary = "大纲生成接口")
    public CommonResult<JSONObject> generateOutline(@RequestBody JSONObject reqParam) {
        log.info("generateOutline 大纲生成接口方法：" + reqParam);
        JSONObject result = null;
        try {

            if(reqParam.containsKey("fileId") && !StringUtil.isNullOrEmpty(reqParam.getString("fileId"))){
                String  fileConent = fileMap.get(reqParam.getString("fileId"));
                if(!StringUtil.isNullOrEmpty(fileConent)){
                    reqParam.put("fileConent",fileConent);
                }
            }

            result = aiDhPptService.generateOutline(reqParam);
            if(result != null){
                if("0000".equals(result.getString("code"))){
                    log.info("调用 generateOutline success：" + result);
                    JSONObject data = result.getJSONObject("data");
                    return success(data);
                }else {
                    log.info("调用 generateOutline error：" + result);

                    return CommonResult.error(GlobalErrorCodeConstants.UNKNOWN.getCode(), result.getString("msg"));
                }
            }else{
                log.info("调用 generateOutline error：" + result);
                return CommonResult.error(GlobalErrorCodeConstants.UNKNOWN);

            }
        } catch (Exception e) {
            log.info("调用 generateOutline error：" + e.getMessage());
            return error(GlobalErrorCodeConstants.UNKNOWN);
        }
    }

    @PostMapping("/uploadFile")
    @Operation(summary = "文档上传接口")
    public CommonResult uploadFile( @RequestPart("file") MultipartFile file,
                                    @RequestParam("id") String id) {
        // 检查文件是否为空
        if (file.isEmpty()) {
            return CommonResult.error(GlobalErrorCodeConstants.UNKNOWN.getCode(),"请选择文件进行上传");
        }
        // 验证文件类型
        String originalFilename = file.getOriginalFilename();
        String fileExtension ="docx";
        if (originalFilename != null && originalFilename.contains(".")) {
            int lastDotIndex = originalFilename.lastIndexOf('.');
            fileExtension = originalFilename.substring(lastDotIndex + 1).toLowerCase();
        }

        if (!originalFilename.endsWith(".docx") && !originalFilename.endsWith(".txt")) {
            return CommonResult.error(GlobalErrorCodeConstants.UNKNOWN.getCode(),"仅支持.docx和.txt格式的文件");
        }

        // 读取文件内容
        try {
            StringBuilder fileContent = new StringBuilder();
            InputStream inputStream = file.getInputStream();
            BufferedReader bufferedReader = new BufferedReader(new InputStreamReader(inputStream, StandardCharsets.UTF_8));
            String line;
            while ((line = bufferedReader.readLine()) != null) {
                fileContent.append(line).append("\n");
            }
            bufferedReader.close();
            inputStream.close();

            if(fileMap.keySet().size() > 10){
                fileMap.clear();
            }
            fileMap.put(id,fileContent.toString());

        } catch (IOException e) {
            e.printStackTrace();
            return CommonResult.error(GlobalErrorCodeConstants.UNKNOWN.getCode(), "文件读取失败");
        }

        return success(true);
    }

}
