package cn.iocoder.yudao.module.digital.entity;


import com.baomidou.mybatisplus.annotation.TableName;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.experimental.Accessors;

@Data
@EqualsAndHashCode(callSuper = false)
@Accessors(chain = true)
@TableName("tb_ai_dh_aoster_design")
public class TbAiDhAosterDesign {

    private Integer id;

    private String designName;

    private String designUrl;

    private String designContent;

    private String createTime;

    private String creator;

    private String updateTime;

    private String isTemplate;

}
