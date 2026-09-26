package cn.iocoder.yudao.module.digital.vo;


import cn.iocoder.yudao.framework.common.pojo.PageParam;
import io.swagger.v3.oas.annotations.media.Schema;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.NoArgsConstructor;

@Schema(description = "文案管理 - 文案列表")
@Data
@NoArgsConstructor
@AllArgsConstructor
@EqualsAndHashCode(callSuper = true)
public class CopywritingUpdateReqVO extends PageParam {


    @Schema(description = "文案id", example = "yudao")
    private String id;

    @Schema(description = "操作人", example = "yudao")
    private String oprStaff;

    @Schema(description = "文案内容", example = "yudao")
    private String copywriteContent;

    @Schema(description = "文案标题", example = "yudao")
    private String copywriteTitle;
}
