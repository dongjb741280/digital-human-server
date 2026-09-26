package cn.iocoder.yudao.module.digital.entity;

import com.baomidou.mybatisplus.annotation.TableName;
import com.baomidou.mybatisplus.annotation.IdType;
import java.time.LocalDateTime;
import com.baomidou.mybatisplus.annotation.TableId;
import java.io.Serializable;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.experimental.Accessors;

/**
 * <p>
 * 流程编排场景待办任务定义表
 * </p>
 *
 * @author zhaowang
 * @since 2024-10-23
 */
@Data
@EqualsAndHashCode(callSuper = false)
@Accessors(chain = true)
@TableName("tb_ai_workflow_work_task")
public class TbAiWorkflowWorkTask implements Serializable {

    private static final long serialVersionUID = 1L;

    /**
     * 任务ID
     */
    @TableId(value = "task_id", type = IdType.AUTO)
    private String taskId;

    /**
     * 任务名称
     */
    private String taskName;

    /**
     * 批次ID
     */
    private String batchId;

    /**
     * 流程标识
     */
    private String flowId;

    /**
     * 流程名称
     */
    private String flowName;

    /**
     * 任务类型：1 校区走访、2 商客营销
     */
    private String taskType;

    /**
     * 任务级别：1 普通、2 紧急
     */
    private String taskLevel;

    /**
     * 任务状态：0、未开始，1 进行中，2 已完成
     */
    private String taskState;

    /**
     * 当前任务节点：1 待选择任务、2 待反馈沟通结果、3 待提交订单
     */
    private String taskNode;

    /**
     * 任务开始时间
     */
    private LocalDateTime startTime;

    /**
     * 任务结束时间
     */
    private LocalDateTime endTime;

    /**
     * 备注
     */
    private String remark;

    /**
     * 预留字段1
     */
    private String reserve1;

    /**
     * 预留字段2
     */
    private String reserve2;

    /**
     * 预留字段3
     */
    private String reserve3;

    /**
     * 创建者
     */
    private String creator;

    /**
     * 创建时间
     */
    private LocalDateTime createTime;

    /**
     * 更新者
     */
    private String updater;

    /**
     * 更新时间
     */
    private LocalDateTime updateTime;

    /**
     * 是否删除
     */
    private Boolean deleted;

    /**
     * 租户编号
     */
    private Long tenantId;


}
