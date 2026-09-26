package cn.iocoder.yudao.module.digital.vo;

import cn.iocoder.yudao.framework.common.pojo.CommonResult;
import cn.iocoder.yudao.framework.common.pojo.PageParam;
import cn.iocoder.yudao.framework.common.pojo.PageResult;
import com.alibaba.fastjson.JSONObject;
import io.swagger.v3.oas.annotations.media.Schema;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.NoArgsConstructor;

import java.util.ArrayList;
import java.util.List;

@Schema(description = "数字人视频管理 - 数字人视频查询")
@Data
@NoArgsConstructor
@AllArgsConstructor
@EqualsAndHashCode(callSuper = true)
public class AiDhHumanVideoReqVO extends PageParam {

    @Schema(description = "类型查询", example = "yudao")
    private String queryType;

    private String staffId;

    private Integer pageNum ;

    private Integer pageSize ;

    private String videoSave;



}
