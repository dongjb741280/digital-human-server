package cn.iocoder.yudao.module.digital.controller;


import cn.iocoder.yudao.framework.common.exception.enums.GlobalErrorCodeConstants;
import cn.iocoder.yudao.framework.common.pojo.CommonResult;
import cn.iocoder.yudao.module.digital.dal.CommparaDo;
import cn.iocoder.yudao.module.digital.service.AiCommparaService;
import io.swagger.v3.oas.annotations.Operation;
import lombok.extern.slf4j.Slf4j;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import javax.annotation.Resource;
import java.util.List;
import java.util.Map;

import static cn.iocoder.yudao.framework.common.pojo.CommonResult.error;
import static cn.iocoder.yudao.framework.common.pojo.CommonResult.success;

@RestController
@RequestMapping("/digital-api/system/aiCommpara")
@Slf4j
public class AiCommparaController {


    @Resource
    private AiCommparaService aiCommparaService;

    @PostMapping("/getCommparaByParaCode")
    @Operation(summary = "获取码表信息")
    public CommonResult getCommparaByParaCode(@RequestBody Map<String, String> map) {
        try {
            String paraCode = map.get("paraCode");
            List<CommparaDo> resultList = aiCommparaService.getPptRecordDetail(paraCode);
            return success(resultList);
        } catch (Exception e) {
            return error(GlobalErrorCodeConstants.UNKNOWN);
        }

    }
}
