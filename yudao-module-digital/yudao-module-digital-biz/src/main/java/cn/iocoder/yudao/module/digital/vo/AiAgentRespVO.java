package cn.iocoder.yudao.module.digital.vo;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Schema(description = "智能体 - 智能体列表查询")
@Data
@NoArgsConstructor
@AllArgsConstructor
public class AiAgentRespVO {

    /**
     * 智能体App ID
     */
    private String id;
    /**
     * 智能体名称
     */
    private String agentName;

    /**
     *
     */
    private String agentRole;
}
