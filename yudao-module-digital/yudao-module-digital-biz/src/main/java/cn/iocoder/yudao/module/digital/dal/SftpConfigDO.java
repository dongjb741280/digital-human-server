package cn.iocoder.yudao.module.digital.dal;

import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import com.fhs.core.trans.vo.TransPojo;
import lombok.*;

import java.io.Serializable;

/**
 * @program: ai_digital
 * @description: ftp配置文件
 * @author: huangyx
 * @create: 2024-07-13 16:20
 **/
@TableName(value = "tb_s_sftp_config", autoResultMap = true) // 由于 SQL Server 的 system_user 是关键字，所以使用 system_users
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class SftpConfigDO implements Serializable, TransPojo {

    @TableId
    private String sftpId;

    /**
     * 加密后的账号
     */
    private String sftpAccount;

    /**
     * 加密后的密码
     */
    private String sftpPassword;

    /**
     * 端口
     */
    private Integer sftpPort;

    /**
     * 模式 0:ftp 1:sftp
     */
    private String sftpMode;

    /**
     * 项目标识
     */
    private String sftpSystem;

    /**
     * 下载路径
     */
    private String sftpDownPath;

    /**
     * 备份路径
     */
    private String sftpBackPath;

    /**
     * 上传路径
     */
    private String sftpUploadPath;

    /**
     * 服务IP
     */
    private String sftpIp;

    /**
     * 文件名称
     */
    private String fileName;

    /**
     *
     */
    private String ext1;

    /**
     *
     */
    private String ext2;

    /**
     *
     */
    private String ext3;
}
