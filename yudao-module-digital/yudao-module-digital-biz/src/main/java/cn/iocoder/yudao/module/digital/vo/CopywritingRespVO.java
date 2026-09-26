package cn.iocoder.yudao.module.digital.vo;


import com.baomidou.mybatisplus.annotation.TableId;
import io.swagger.v3.oas.annotations.media.Schema;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Schema(description = "文案管理 - 文案列表")
@Data
@NoArgsConstructor
@AllArgsConstructor
public class CopywritingRespVO {

    /**
     * ID
     */
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
     * pptId
     */
    private String mainPptId;

    /**
     * 操作时间
     */
    private String oprTime;

    /**
     * 操作工号
     */
    private String oprStaff;
}
