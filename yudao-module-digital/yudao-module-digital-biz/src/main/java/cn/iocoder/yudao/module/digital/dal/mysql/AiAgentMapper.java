package cn.iocoder.yudao.module.digital.dal.mysql;

import cn.iocoder.yudao.framework.mybatis.core.mapper.BaseMapperX;
import cn.iocoder.yudao.module.digital.dal.AiAgentDO;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

@Mapper
public interface AiAgentMapper extends BaseMapperX<AiAgentDO> {
    List<AiAgentDO> selectListAll();

    List<HashMap> getDialogueWorkflowDelTask(@Param("taskId")String taskId);

}
