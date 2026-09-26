package cn.iocoder.yudao.module.digital.service;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

public interface AiWorkflowProcessService {
    List<HashMap> getDialogueWorkflowDelTask(String taskId,Long loginUserId);

    HashMap updateDialogueWorkflowDelTask(Map<String, Object> map);
}
