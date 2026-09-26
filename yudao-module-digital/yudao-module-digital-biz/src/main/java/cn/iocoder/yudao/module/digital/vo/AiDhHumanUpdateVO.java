package cn.iocoder.yudao.module.digital.vo;

import cn.iocoder.yudao.framework.common.pojo.PageParam;
import io.swagger.v3.oas.annotations.media.Schema;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.NoArgsConstructor;

@Schema(description = "数字人形象管理 - 数字人形象")
@Data
@NoArgsConstructor
@AllArgsConstructor
@EqualsAndHashCode(callSuper = true)
public class AiDhHumanUpdateVO extends PageParam {

    @Schema(description = "id", example = "yudao")
    private String id;
    @Schema(description = "形象名称", example = "yudao")
    private String humanName;

    /*@Schema(description = "背景替换", example = "yudao")
    private String humanBg;*/

    @Schema(description = "是否分享", example = "yudao")
    private String humanShare;

    /*@Schema(description = "视频文件", example = "yudao")
    private MultipartFile file;*/
}
