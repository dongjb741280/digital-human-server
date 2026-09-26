package cn.iocoder.yudao.module.digital.controller.agent;

import cn.iocoder.yudao.framework.common.exception.enums.GlobalErrorCodeConstants;
import cn.iocoder.yudao.framework.common.pojo.CommonResult;
import cn.iocoder.yudao.module.digital.dal.CommparaDo;
import cn.iocoder.yudao.module.digital.service.AiCommparaService;
import cn.iocoder.yudao.module.digital.service.AiagentService;
import cn.iocoder.yudao.module.digital.vo.AiAgentRespVO;
import cn.iocoder.yudao.module.digital.vo.AiInteractAgentVo;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.extern.slf4j.Slf4j;
import org.springframework.web.bind.annotation.*;

import javax.annotation.Resource;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import static cn.iocoder.yudao.framework.common.pojo.CommonResult.error;
import static cn.iocoder.yudao.framework.security.core.util.SecurityFrameworkUtils.getLoginUserId;

/**
 * @BelongsProject: yudao
 * @BelongsPackage: cn.iocoder.yudao.module.digital.controller.agent
 * @Author: zhaowang
 * @CreateTime: 2024-07-17  17:06
 * @Description: TODO
 * @Version: 1.0
 */
@Tag(name = "智能体")
@RestController
@RequestMapping("/digital-api/system/aiAgent")
//@Validated
@Slf4j
public class AiAgentController {
    @Resource
    private AiagentService aiagentService;
    @Resource
    private AiCommparaService aiCommparaService;

    @PostMapping("/agentList")
    @Operation(summary = "获得智能体列表")
//    @PreAuthorize("@ss.hasPermission('digital:aiDhHuman:list')")
    public CommonResult<List<AiAgentRespVO>> getUserPage() {
        List<AiAgentRespVO> resultList = new ArrayList<>();
        try {
            resultList = aiagentService.getAiAgentList();
        } catch (Exception e) {
            return error(GlobalErrorCodeConstants.UNKNOWN);
        }
        return CommonResult.success(resultList);
    }

    @PostMapping("/getAgentList")
    @Operation(summary = "获得智能体列表")
    public CommonResult<List<AiAgentRespVO>> getAgentList(@RequestParam("agentType") String agentType) {
        List<AiAgentRespVO> resultList = new ArrayList<>();
        try {
            resultList = aiagentService.getInteractAgentList(agentType);
        } catch (Exception e) {
            return error(GlobalErrorCodeConstants.UNKNOWN);
        }
        return CommonResult.success(resultList);
    }

    @PostMapping("/getLanguagePracticeInfo")
    @Operation(summary = "获取话术对练信息")
    public CommonResult<List<Map<String, Object>>> getLanguagePracticeInfo(@RequestBody AiInteractAgentVo aiInteractAgent) {
        try {
            Long loginUserId = getLoginUserId();
            aiInteractAgent.setUser(loginUserId + "");
            List<Map<String, Object>> resultList = aiagentService.getLanguagePracticeInfo(aiInteractAgent);
            return CommonResult.success(resultList);
        } catch (Exception e) {
            return error(GlobalErrorCodeConstants.UNKNOWN);
        }
    }

    @PostMapping("/getInteractiveDialogueInfo")
    @Operation(summary = "获取数字人对话内容")
    public CommonResult<Map> getInteractiveDialogueInfo(@RequestBody AiInteractAgentVo aiInteractAgent) {
        Map result = new HashMap();
        try {
            Long loginUserId = getLoginUserId();
            aiInteractAgent.setUser(loginUserId + "");

            List<CommparaDo> resultList = aiCommparaService.getPptRecordDetail("human_agent_id");
            if (resultList.size() > 0) {
                aiInteractAgent.setId(resultList.get(0).getAttrValue());
            } else {
                return error(GlobalErrorCodeConstants.UNKNOWN.getCode(), "获取智能体信息异常，请检查配置!");
            }
            result = aiagentService.getInteractiveDialogueInfo(aiInteractAgent);
        } catch (Exception e) {
            return error(GlobalErrorCodeConstants.UNKNOWN);
        }
        return CommonResult.success(result);
    }

    @PostMapping("/getDialogueHumanQaInfo")
    @Operation(summary = "获取数字人对话内容-可配置")
    public CommonResult<Map> getDialogueHumanQaInfo(@RequestBody AiInteractAgentVo aiInteractAgent) {
        Map result = new HashMap();
        try {
            Long loginUserId = getLoginUserId();
            aiInteractAgent.setUser(loginUserId + "");
            result = aiagentService.getDialogueHumanQaInfo(aiInteractAgent);
        } catch (Exception e) {
            return error(GlobalErrorCodeConstants.UNKNOWN);
        }
        return CommonResult.success(result);
    }

    @PostMapping("/getDialogueWorkflowQaInfo")
    @Operation(summary = "获取流程编排对话内容")
    public CommonResult<Map> getDialogueWorkflowQaInfo(@RequestBody AiInteractAgentVo aiInteractAgent) {
        Map result = new HashMap();
        try {
            Long loginUserId = getLoginUserId();
            aiInteractAgent.setUser(loginUserId + "");
            result = aiagentService.getDialogueHumanQaInfo(aiInteractAgent);
        } catch (Exception e) {
            return error(GlobalErrorCodeConstants.UNKNOWN);
        }
        return CommonResult.success(result);
    }
}
