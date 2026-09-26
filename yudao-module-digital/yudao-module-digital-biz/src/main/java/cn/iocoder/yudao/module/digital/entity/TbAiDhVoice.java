package cn.iocoder.yudao.module.digital.entity;

import com.baomidou.mybatisplus.annotation.TableName;
import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableId;
import java.io.Serializable;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.experimental.Accessors;

/**
 * <p>
 *
 * </p>
 *
 * @author author
 * @since 2024-07-25
 */
@Data
@EqualsAndHashCode(callSuper = false)
@Accessors(chain = true)
@TableName("tb_ai_dh_voice")
public class TbAiDhVoice implements Serializable {

    private static final long serialVersionUID = 1L;

    @TableId(value = "id", type = IdType.AUTO)
    private String id;

    private String voiceName;

    private String voiceOrgUrl;

    private String voiceSex;

    private String voiceLabel;

    private String voiceShare;

    private String voiceReferConent;

    private String voiceSampleUrl;

    private String voiceModelUrl;

    private String voiceGptUrl;

    private String voiceWavUrl;

    private String voiceWavText;

    private String voiceModelName;

    private String voiceGptName;

    private String voiceWavName;

    private String voiceStatus;

    private String oprTime;

    private String oprStaff;


}
