package cn.iocoder.yudao.module.digital.service.impl;

import cn.iocoder.yudao.module.digital.dal.mysql.AiAgentMapper;
import cn.iocoder.yudao.module.digital.dal.mysql.TbAiWorkflowOrderInfoMapper;
import cn.iocoder.yudao.module.digital.dal.mysql.TbAiWorkflowWorkTaskMapper;
import cn.iocoder.yudao.module.digital.entity.TbAiWorkflowOrderInfo;
import cn.iocoder.yudao.module.digital.entity.TbAiWorkflowWorkTask;
import cn.iocoder.yudao.module.digital.service.AiWorkflowProcessService;
import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import javax.annotation.Resource;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

@Service
@Slf4j
public class AiWorkflowProcessServiceImpl implements AiWorkflowProcessService {
    @Resource
    private AiAgentMapper aiAgentMapper;

    @Resource
    private TbAiWorkflowWorkTaskMapper workflowWorkTaskMapper;
    @Resource
    private TbAiWorkflowOrderInfoMapper workflowOrderInfoMapper;
    @Override
    public List<HashMap> getDialogueWorkflowDelTask(String taskId,Long loginUserId) {
        return aiAgentMapper.getDialogueWorkflowDelTask(taskId);
    }

    @Override
    public HashMap updateDialogueWorkflowDelTask(Map<String, Object> map) {
        String taskId = map.get("taskId")==null?"":map.get("taskId").toString();
        String taskNode = map.get("taskNode")==null?"":map.get("taskNode").toString();
        QueryWrapper<TbAiWorkflowWorkTask> queryWrapper = new QueryWrapper<>();
        queryWrapper.eq("task_id",taskId);
        TbAiWorkflowWorkTask workflowWorkTask = new TbAiWorkflowWorkTask();
        workflowWorkTask.setTaskId(taskId);

        if (workflowWorkTaskMapper.selectCount(queryWrapper)>0){
            if (!taskNode.isEmpty() && taskNode.equals("1")){//下一环境营业员选择任务，返回话术
                workflowWorkTask.setTaskNode("2");
                workflowWorkTaskMapper.updateById(workflowWorkTask);
            }else if(!taskNode.isEmpty() && taskNode.equals("2")){//下一环境营业员反馈拜访结果
                workflowWorkTask.setTaskNode("3");
                workflowWorkTaskMapper.updateById(workflowWorkTask);
            }else if(!taskNode.isEmpty() && taskNode.equals("3")){//下一环境营业员填报订单内容
                workflowWorkTask.setTaskNode("4");
                workflowWorkTask.setTaskState("2");
                workflowWorkTaskMapper.updateById(workflowWorkTask);

                //生成订单信息
                TbAiWorkflowOrderInfo workflowOrderInfo = new TbAiWorkflowOrderInfo();
                workflowOrderInfo.setOrderId(System.currentTimeMillis()+"");
                workflowOrderInfo.setTaskId(taskId);
                workflowOrderInfo.setOrderName(map.get("orderName")+"");
                workflowOrderInfo.setCustName(map.get("custName")+"");
                workflowOrderInfo.setCustCertNo(map.get("custCertNo")+"");
                workflowOrderInfo.setCustPhone(map.get("custPhone")+"");
                workflowOrderInfo.setCustAddress(map.get("custAddress")+"");
                workflowOrderInfoMapper.insert(workflowOrderInfo);
            }
        }
        HashMap result = new HashMap();
        result.put("resultCode","0000");
        return result;
    }
}
