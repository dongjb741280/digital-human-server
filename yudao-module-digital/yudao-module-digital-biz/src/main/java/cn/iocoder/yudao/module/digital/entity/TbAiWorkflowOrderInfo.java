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
 * 流程编排场景订单记录表
 * </p>
 *
 * @author zhaowang
 * @since 2024-10-23
 */
@Data
@EqualsAndHashCode(callSuper = false)
@Accessors(chain = true)
@TableName("tb_ai_workflow_order_info")
public class TbAiWorkflowOrderInfo implements Serializable {

    private static final long serialVersionUID = 1L;

    /**
     * 订单ID
     */
    @TableId(value = "order_id", type = IdType.AUTO)
    private String orderId;

    /**
     * 订单名称
     */
    private String orderName;

    /**
     * 订单类型
     */
    private String orderType;

    /**
     * 订单描述
     */
    private String orderDesc;

    /**
     * 订单状态
     */
    private String orderState;

    /**
     * 流程ID
     */
    private String taskId;

    /**
     * 客户姓名
     */
    private String custName;

    /**
     * 客户证件号
     */
    private String custCertNo;

    /**
     * 客户联系电话
     */
    private String custPhone;

    /**
     * 省编码
     */
    private String custProvinceCode;

    /**
     * 地市编码
     */
    private String custCityCode;

    /**
     * 区县编码
     */
    private String custAreaCode;

    /**
     * 装机地址
     */
    private String custAddress;

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
