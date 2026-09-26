package cn.iocoder.yudao.module.digital.vo;


import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import io.swagger.v3.oas.annotations.media.Schema;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * @program: ai_digital
 * @description:  ppt明细记录表
 * @author: zhaoao
 * @create: 2024-07-15 14:38
 **/
@Schema(description = "文案管理 - ppt列表")
@Data
@NoArgsConstructor
@AllArgsConstructor
public class CopyWritePptRecordRespVO {
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




}
