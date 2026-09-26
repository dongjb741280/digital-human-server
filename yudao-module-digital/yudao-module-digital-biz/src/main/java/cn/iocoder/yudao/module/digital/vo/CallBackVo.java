package cn.iocoder.yudao.module.digital.vo;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.io.Serializable;

/**
 * @program: ai_digital
 * @description:
 * @author: huangyx
 * @create: 2024-07-22 09:54
 **/
@Schema(description = "数字人 - 回调vo")
@Data
@NoArgsConstructor
@AllArgsConstructor
public class CallBackVo implements Serializable {

    private String id;
    /**
     * 1 视频和抠图  2 图片
     */
    private String operType;

    /**
     * 3：执行失败 4：执行成功
     */
    private String status;
}
