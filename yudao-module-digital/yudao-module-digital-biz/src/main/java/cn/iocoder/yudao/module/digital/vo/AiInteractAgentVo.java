package cn.iocoder.yudao.module.digital.vo;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.experimental.Accessors;

import java.io.Serializable;
import java.time.LocalDateTime;

/**
 * <p>
 * ai数字人互动智能体配置表
 * </p>
 *
 * @author zhaowang
 * @since 2024-09-12
 */
@Data
@EqualsAndHashCode(callSuper = false)
@Accessors(chain = true)
public class AiInteractAgentVo {

    /**
     * 主键
     */
    private String id;

    /**
     * agent类别：1、智能问答，2、对话演练，3、知识检索
     */
    private String agentType;

    /**
     * 智能体名称
     */
    private String agentName;

    /**
     * 智能体API地址
     */
    private String agentApiUrl;

    /**
     * 智能体API秘钥
     */
    private String agentApiKey;

    /**
     * 变量值：允许传入 App 定义的各变量值。 inputs 参数包含了多组键值对（Key/Value pairs），每组的键对应一个特定变量，每组的值则是该变量的具体值。 默认 {}
     */
    private String inputs;

    /**
     * 响应模式：streaming-流式模式，blocking-阻塞模式
     */
    private String responseMode;

    /**
     * 用户标识：用于定义终端用户的身份，方便检索、统计。 由开发者定义规则，需保证用户标识在应用内唯一
     */
    private String user;

    /**
     * 会话 ID（选填），需要基于之前的聊天记录继续对话，必须传之前消息的 conversation_id
     */
    private String conversationId;

    /**
     * 发布状态：0-停用、1-再用
     */
    private String agentState;

    /**
     * 创建者
     */
    private String creator;

    /**
     * 创建时间
     */
    private LocalDateTime createTime;

    /**
     * 更新者
     */
    private String updater;

    /**
     * 更新时间
     */
    private LocalDateTime updateTime;

    /**
     * 是否删除
     */
    private Boolean deleted;

    /**
     * 租户编号
     */
    private Long tenantId;

    private String query;

    private Object inputObj;

    private String humanInteractUrl;

    private String type;

    private String answerInfo;

    private Boolean interrupt;//是否支持被打断
}
