package cn.iocoder.yudao.module.digital.dal;


import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * @program: ai_digital
 * @description:  ppt明细记录表DO
 * @author: zhaoao
 * @create: 2024-07-15 14:38
 **/
@TableName(value = "tb_ai_dh_copywrite_ppt_record", autoResultMap = true) // 由于 SQL Server 的 system_user 是关键字，所以使用 system_users
@Data
//@EqualsAndHashCode(callSuper = true)
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class AiCopyWritePptRecordDO {
    /**
     * ID
     */
    @TableId
    private String id;
    /**
     * 文案ID
     */
    private String copywriteId;
    /**
     * ppt地址
     */
    private String pptUrl;
    /**
     * 描述
     */
    private String recordDesc;

    /**
     * 0：系统生成 1：自己上传
     */
    private String recordType;
    /**
     * 版本号
     */
    private String recordVersion;
    /**
     * 文档格式
     */
    private String recordFormat;
    /**
     * 文档大小
     */
    private String recordSize;
    /**
     * 操作时间
     */
    private String oprTime;
    /**
     * 操作工号
     */
    private String oprStaff;


    public Long parseRecordVersion() {
        try {
            return Long.parseLong(recordVersion);
        } catch (NumberFormatException e) {
            throw new IllegalArgumentException("Invalid recordVersion format: " + recordVersion, e);
        }
    }


}
