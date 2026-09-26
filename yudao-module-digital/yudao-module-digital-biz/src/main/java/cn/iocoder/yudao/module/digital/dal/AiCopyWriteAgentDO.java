package cn.iocoder.yudao.module.digital.dal;


import cn.iocoder.yudao.framework.tenant.core.db.TenantBaseDO;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import lombok.*;

/**
 * @program: ai_digital
 * @description:  文案管理主表DO
 * @author: zhaoao
 * @create: 2024-07-15 14:38
 **/
@TableName(value = "tb_ai_dh_copywrite", autoResultMap = true) // 由于 SQL Server 的 system_user 是关键字，所以使用 system_users
@Data
//@EqualsAndHashCode(callSuper = true)
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class AiCopyWriteAgentDO {

    /**
     * ID
     */
    @TableId
    private String id;

    /**
     * 文案名称
     */
    private String copywriteName;


    /**
     * 制作方式 0：生成课件文案 1：生成简单文案
     */
    private String makeType;

    /**
     * 是否分享加入公共库 1：是，0：否'
     */
    private String copywriteShare;

    /**
     * '对应的智能体ID'
     */
    private String copywriteRole;

    /**
     * 主题描述：提示词
     */
    private String copywriteDesc;

    /**
     * 文案标题
     */
    private String copywriteTitle;

    /**
     * 字数要求
     */
    private String copywriteWc;

    /**
     * 文案内容
     */
    private String copywriteContent;

    /**
     * PPT模板ID
     */
    private String copywritePpt;

    /**
     * 文案最终生成路径
     */
    private String copywriteUrl;

    /**
     * 制作结果 1：未执行 2：执行中 3：执行失败 4：执行成功
     */
    private String copywriteStatus;

    /**
     * 操作时间
     */
    private String oprTime;

    /**
     * 操作工号
     */
    private String oprStaff;


    /**
     * pptId
     */
    private String mainPptId;




}
