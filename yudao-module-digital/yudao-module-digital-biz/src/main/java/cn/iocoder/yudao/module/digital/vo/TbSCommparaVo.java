package cn.iocoder.yudao.module.digital.vo;

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
 * @author zhaowang
 * @since 2024-09-13
 */
@Data
@EqualsAndHashCode(callSuper = false)
@Accessors(chain = true)
public class TbSCommparaVo {

    private Integer id;

    private String paraCode;

    private String attrCode;

    private String attrValue;

    private Integer orderNo;

    /**
     * 配用字段1
     */
    private String paramCode1;

    /**
     * 配用字段2
     */
    private String paramCode2;

    /**
     * 配用字段3
     */
    private String paramCode3;

    /**
     * 配用字段4
     */
    private String paramCode4;

    private String remark;

    /**
     * 是否在用， 0：在用， 1 不可用
     */
    private Integer removeTag;


}
