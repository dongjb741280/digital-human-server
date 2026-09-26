package cn.iocoder.yudao.module.digital.service;

import cn.iocoder.yudao.module.digital.dal.TbAiInteractHumanDo;
import cn.iocoder.yudao.module.digital.entity.TbAiDhVideoBackground;
import cn.iocoder.yudao.module.digital.entity.TbAiDhVideoMaterial;

import java.util.HashMap;
import java.util.Map;

/**
 * @author lkm
 * @ClassName:
 * @Description:
 * @date 2024-09-12-14:15
 */
public interface VideoBackgroundService {
    HashMap materialQuery(HashMap hashMap) throws Exception;

    HashMap materialDel(HashMap hashMap)throws Exception;

    void insertMaterial(TbAiDhVideoMaterial tbAiDhVideoMaterial)throws Exception;

    HashMap backgroundQuery(HashMap hashMap)throws Exception;

    HashMap backgroundDel(HashMap hashMap)throws Exception;

    void insertBackground(TbAiDhVideoBackground tbAiDhVideoBackground)throws Exception;

    Map<String, Object> composeSave(TbAiInteractHumanDo tbAiInteractHumanDo);

    Map<String, Object> composePage(TbAiInteractHumanDo tbAiInteractHumanDo);

    Map<String, Object> composeDel(TbAiInteractHumanDo tbAiInteractHumanDo);

    TbAiInteractHumanDo composeQueOne(String id);
}
