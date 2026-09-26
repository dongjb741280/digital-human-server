package cn.iocoder.yudao.module.digital.controller.agent;

import cn.iocoder.yudao.framework.common.exception.enums.GlobalErrorCodeConstants;
import cn.iocoder.yudao.framework.common.pojo.CommonResult;
import cn.iocoder.yudao.module.digital.dal.CommparaDo;
import cn.iocoder.yudao.module.digital.service.AiCommparaService;
import cn.iocoder.yudao.module.digital.service.AiWorkflowProcessService;
import cn.iocoder.yudao.module.digital.service.AiagentService;
import cn.iocoder.yudao.module.digital.vo.AiInteractAgentVo;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.google.gson.Gson;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.extern.slf4j.Slf4j;
import org.json.JSONArray;
import org.json.JSONObject;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.servlet.mvc.method.annotation.SseEmitter;

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
@Tag(name = "流程编排")
@RestController
@RequestMapping("/digital-api/system/workflow")
//@Validated
@Slf4j
public class AiWorkflowProcessController {
    @Resource
    private AiWorkflowProcessService aiWorkflowProcessService;
    @Resource
    private AiagentService aiagentService;
    @Resource
    private AiCommparaService aiCommparaService;

    @PostMapping("/getDialogueWorkflowDelTask")
    @Operation(summary = "查询待办任务")
    public CommonResult<List<HashMap>> getDialogueWorkflowDelTask(String taskId) {
        List<HashMap> result = new ArrayList<>();
        try {
            Long loginUserId = getLoginUserId();
            result = aiWorkflowProcessService.getDialogueWorkflowDelTask(taskId,loginUserId);
        } catch (Exception e) {
            return error(GlobalErrorCodeConstants.UNKNOWN);
        }
        return CommonResult.success(result);
    }

    @PostMapping("/updateDialogueWorkflowDelTask")
    @Operation(summary = "任务状态变更接口")
    public CommonResult<HashMap> updateDialogueWorkflowDelTask(@RequestBody Map<String, Object> map){
        HashMap result = new HashMap();
        try {
            Long loginUserId = getLoginUserId();
            map.put("loginUserId",loginUserId);
            result = aiWorkflowProcessService.updateDialogueWorkflowDelTask(map);
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
            result = aiagentService.getDialogueWorkflowQaInfo(aiInteractAgent);
        } catch (Exception e) {
            return error(GlobalErrorCodeConstants.UNKNOWN);
        }
        return CommonResult.success(result);
    }

    @PostMapping("/getDigitalHumansStream")
    @Operation(summary = "数字人实时互动流式接口")
    public SseEmitter getDigitalHumansStream(@RequestBody AiInteractAgentVo aiInteractAgent,
                                             @RequestParam("uniqueRequestId") String uniqueRequestId) {
        SseEmitter emitter = new SseEmitter(0L);
        new Thread(() -> {
            try {
                Long loginUserId = getLoginUserId();
                aiInteractAgent.setUser(loginUserId + "");
                // 用配置的智能体 id 覆盖前端传入的 id（前端 id 可能是旧值）
                List<CommparaDo> agentList = aiCommparaService.getPptRecordDetail("human_agent_id");
                if (agentList.size() > 0) {
                    aiInteractAgent.setId(agentList.get(0).getAttrValue());
                }
                // 流式逐块返回
                String audioUrl = aiagentService.streamDialogue(aiInteractAgent, chunk -> {
                    try {
                        emitter.send(SseEmitter.event().name("message").data(chunk));
                    } catch (Exception e) {
                        log.error("send sse chunk error", e);
                    }
                });
                // 语音播报：答案合成音频地址通过独立事件返回
                if (audioUrl != null && !audioUrl.isEmpty()) {
                    emitter.send(SseEmitter.event().name("audio").data(audioUrl));
                }
                emitter.complete();
            } catch (Exception e) {
                log.error("getDigitalHumansStream error", e);
                try {
                    emitter.completeWithError(e);
                } catch (Exception ex) {
                    log.error("completeWithError error", ex);
                }
            }
        }).start();
        return emitter;
    }
}
