package cn.iocoder.yudao.module.digital.service;

import cn.iocoder.yudao.module.digital.entity.TbAiDhVoice;
import com.baomidou.mybatisplus.extension.service.IService;
import org.springframework.web.multipart.MultipartFile;

import java.util.HashMap;
import java.util.Map;

/**
 * <p>
 *  服务类
 * </p>
 *
 * @author author
 * @since 2024-07-17
 */
public interface ITbAiDhVoiceService  {

    HashMap voiceList(HashMap hashMap) throws Exception;

    HashMap voiceDel(HashMap hashMap) throws Exception;

    HashMap voiceSave(HashMap hashMap) throws Exception;

    void updateVoice(HashMap hashMap);

    String callVoiceClone(HashMap hashMap);

    void updateVoiceFinall(HashMap hashMap);


    HashMap voice2Txt(Map<String,Object> params) throws Exception;
}
