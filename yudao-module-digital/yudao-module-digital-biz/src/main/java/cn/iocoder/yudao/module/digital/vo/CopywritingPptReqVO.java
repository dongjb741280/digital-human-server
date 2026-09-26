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
public class CopywritingPptReqVO extends PageParam {


    @Schema(description = "操作人", example = "yudao")
    private String oprStaff;

    @Schema(description = "pptid", example = "yudao")
    private String id;

    @Schema(description = "文案 id", example = "yudao")
    private String copywriteId;

    @Schema(description = "ppt文件名", example = "yudao")
    private String fileName;

    @Schema(description = "ppt文件路径", example = "yudao")
    private String filePath;

    @Schema(description = "ppt文件大小", example = "yudao")
    private String fileSize;

    @Schema(description = "ppt文件格式", example = "yudao")
    private String fileFormat;


}
