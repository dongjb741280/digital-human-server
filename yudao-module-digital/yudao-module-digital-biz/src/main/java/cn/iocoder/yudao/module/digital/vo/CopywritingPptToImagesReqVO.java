package cn.iocoder.yudao.module.digital.vo;


import cn.iocoder.yudao.framework.common.pojo.PageParam;
import io.swagger.v3.oas.annotations.media.Schema;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.NoArgsConstructor;

@Schema(description = "文案管理 - 上传 ppt 文件转图片")
@Data
@NoArgsConstructor
@AllArgsConstructor
public class CopywritingPptToImagesReqVO {

    @Schema(description = "流水号", example = "yudao")
    private String uuid;

    @Schema(description = "文案id", example = "yudao")
    private String copywrite_id;

    @Schema(description = "ppt文件路径", example = "yudao")
    private String ppt_path;

    @Schema(description = "pptid", example = "yudao")
    private String ppt_id;

    @Schema(description = "操作人", example = "yudao")
    private String user;



}
