package cn.iocoder.yudao.module.digital.service;

import cn.iocoder.yudao.module.digital.vo.AiAgentRespVO;
import cn.iocoder.yudao.module.digital.dal.AiInteractAgentDo;
import cn.iocoder.yudao.module.digital.vo.AiInteractAgentVo;
import org.json.JSONException;

import java.util.List;
import java.util.Map;
import java.util.function.Consumer;

/**
 * @BelongsProject: yudao
 * @BelongsPackage: cn.iocoder.yudao.module.digital.service
 * @Author: zhaowang
 * @CreateTime: 2024-07-17  17:07
 * @Description: TODO 智能体相关服务接口
 * @Version: 1.0
 */
public interface AiagentService {
    List<AiAgentRespVO> getAiAgentList();

    List<AiAgentRespVO> getInteractAgentList(String agentType);

    List<Map<String, Object>> getLanguagePracticeInfo(AiInteractAgentVo aiInteractAgent) throws JSONException;

    Map getInteractiveDialogueInfo(AiInteractAgentVo aiInteractAgent);

    Map getDialogueHumanQaInfo(AiInteractAgentVo aiInteractAgent);

    Map getDialogueWorkflowQaInfo(AiInteractAgentVo aiInteractAgent);

    String streamDialogue(AiInteractAgentVo aiInteractAgent, Consumer<String> onChunk);
}
