package cn.iocoder.yudao.module.digital.dal;

import cn.iocoder.yudao.framework.tenant.core.db.TenantBaseDO;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import lombok.*;

import java.time.LocalDateTime;


/**
 * @program: ai_digital
 * @description: 数字人表用户 DO
 * @author: huangyx
 * @create: 2024-07-13 14:38
 **/
@TableName(value = "tb_ai_dh_copywrite_agent", autoResultMap = true)
@Data
@EqualsAndHashCode(callSuper = true)
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class AiAgentDO extends TenantBaseDO {

    /**
     * ID
     */
    @TableId
    private String id;
    /**
     * 智能体名称
     */
    private String agentName;

    /**
     * 智能体角色
     */
    private String agentRole;

    /**
     * 操作时间
     */
    private LocalDateTime oprTime;

    /**
     * 操作工号
     */
    private String oprStaff;


}
