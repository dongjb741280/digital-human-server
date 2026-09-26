package cn.iocoder.yudao.module.digital.controller;


import cn.iocoder.yudao.framework.common.exception.enums.GlobalErrorCodeConstants;
import cn.iocoder.yudao.framework.common.pojo.CommonResult;
import cn.iocoder.yudao.module.digital.service.AiDhPptService;
import cn.iocoder.yudao.module.digital.service.PptRecordDetailService;
import com.alibaba.fastjson.JSONObject;
import io.swagger.v3.oas.annotations.Operation;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang3.StringUtils;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import javax.annotation.Resource;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import static cn.iocoder.yudao.framework.common.pojo.CommonResult.error;
import static cn.iocoder.yudao.framework.common.pojo.CommonResult.success;

@RestController
@RequestMapping("/digital-api/system/aiPptRecordDetail")
@Slf4j
public class PptRecordDetailController {

    @Resource
    private PptRecordDetailService pptRecordDetailService;

    @Resource
    private AiDhPptService aiDhPptService;

    @PostMapping("/getPptRecordDetail")
    @Operation(summary = "查询ppt图片和备注信息")
    public CommonResult getPptRecordDetail(@RequestBody Map<String, String> map) {

        try {
            String pptId = map.get("pptId");
            List<Map> pptRecordDetailList = pptRecordDetailService.getPptRecordDetail(pptId);
            return success(pptRecordDetailList);
        } catch (Exception e) {
            return error(GlobalErrorCodeConstants.UNKNOWN);
        }

    }

    @PostMapping("/updatePptRecordDetail")
    @Operation(summary = "更新ppt图片和备注信息")
    public CommonResult<Boolean> updatePptRecordDetail(@RequestBody Map<String, String> map) throws Exception {

        try {
            String pptId = map.get("pptId");
            String pptNum = map.get("pptNum");
            String pptImageWords = map.get("pptImageWords");
            if(StringUtils.isEmpty(pptId) || StringUtils.isEmpty(pptNum) || StringUtils.isEmpty(pptImageWords)){
                return error(GlobalErrorCodeConstants.BAD_REQUEST);
            }

            Map<String, Object> paramsReq = new HashMap<>();
            paramsReq.put("mainPptId", pptId);
            paramsReq.put("pptNum", Integer.valueOf(pptNum).intValue());

            List<Map> pptRecordDetailList = pptRecordDetailService.getPptRecordDetail(paramsReq);
            log.info("pptRecordDetailList size:" + pptRecordDetailList.size());
            String pptImagesWords = "";
            if(pptRecordDetailList.size() >0){
                Map<String, Object> data = pptRecordDetailList.get(0);
                pptImagesWords = data.get("ppt_image_words") + "";
            }

            if(StringUtils.isEmpty(pptImagesWords) ){
                return error(GlobalErrorCodeConstants.BAD_REQUEST);
            }

            if( pptImageWords.equals(pptImagesWords)){
                return success(true);
            }

            Map<String, Object> params = new HashMap<>();
            params.put("pptId", pptId);
            params.put("pptNum", Integer.valueOf(pptNum).intValue());
            params.put("pptImageWords", pptImageWords);

            boolean resultTag = pptRecordDetailService.updatePptRecordDetail(params);

            return success(resultTag);

        } catch (Exception e) {
            log.error("更新ppt图片和备注信息失败", e);
            return error(GlobalErrorCodeConstants.UNKNOWN);
        }

    }

}
