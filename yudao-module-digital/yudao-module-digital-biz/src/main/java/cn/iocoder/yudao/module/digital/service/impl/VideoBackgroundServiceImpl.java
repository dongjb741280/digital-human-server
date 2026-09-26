package cn.iocoder.yudao.module.digital.service.impl;

import cn.iocoder.yudao.module.digital.adaptor.AiDhHumanVideoAdaptor;
import cn.iocoder.yudao.module.digital.dal.*;
import cn.iocoder.yudao.module.digital.dal.mysql.*;
import cn.iocoder.yudao.module.digital.entity.*;
import cn.iocoder.yudao.module.digital.service.VideoBackgroundService;
import cn.iocoder.yudao.module.digital.util.SequenceUtils;
import cn.iocoder.yudao.module.digital.vo.AiDhHumanVideoRespVO;
import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.baomidou.mybatisplus.core.toolkit.StringUtils;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.BeanUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import javax.annotation.Resource;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.*;
import java.util.stream.Collectors;

/**
 * @author lkm
 * @ClassName:
 * @Description:
 * @date 2024-09-12-14:16
 */
@Service
@Slf4j
public class VideoBackgroundServiceImpl implements VideoBackgroundService {

    @Autowired
    private TbAiDhVideoMaterialMapper tbAiDhVideoMaterialMapper;
    @Autowired
    private TbAiDhVideoBackgroundMapper tbAiDhVideoBackgroundMapper;
    @Autowired
    private TbAiDhVideoLayerTrackMapper tbAiDhVideoLayerTrackMapper;
    @Autowired
    private TbAiInteractHumanMapper tbAiInteractHumanMapper;
    @Resource
    private TbAiInteractAgentMapper aiInteractAgentMapper;
    @Resource
    private AiDhHumanMapper aiDhHumanMapper;
    @Override
    public HashMap materialQuery(HashMap hashMap)  throws Exception {
        Integer pageNum = hashMap.get("pageNum") == null ? 1 : (Integer) hashMap.get("pageNum");
        Integer pageSize = hashMap.get("pageSize") == null ? 10 : (Integer) hashMap.get("pageSize");
        Page<TbAiDhVideoMaterial> pageParm = new Page<>(pageNum, pageSize);
        QueryWrapper<TbAiDhVideoMaterial> queryWrapper = new QueryWrapper<>();
        if (!StringUtils.isEmpty((String) hashMap.get("materialName"))) {
            queryWrapper.like("material_name", hashMap.get("materialName"));
        }
        if (!StringUtils.isEmpty((String) hashMap.get("bgShare"))) {
            queryWrapper.eq("bg_share", hashMap.get("bgShare"));
        }
        if (!StringUtils.isEmpty((String) hashMap.get("materialType"))) {
            queryWrapper.eq("material_type", hashMap.get("materialType"));
        }
        queryWrapper.eq("deleted","0");
        queryWrapper.orderByDesc("create_time");
        tbAiDhVideoMaterialMapper.selectPage(pageParm, queryWrapper);
        HashMap res = new HashMap();
        res.put("total", pageParm.getTotal());
        res.put("data", pageParm.getRecords());
        return res;
    }

    @Override
    public HashMap materialDel(HashMap hashMap)  throws Exception {
        TbAiDhVideoMaterial tbAiDhVideoMaterial=new TbAiDhVideoMaterial();
        tbAiDhVideoMaterial.setId((String)hashMap.get("id"));
        tbAiDhVideoMaterial.setDeleted(true);
        tbAiDhVideoMaterial.setUpdater((String)hashMap.get("staffId"));
        tbAiDhVideoMaterial.setUpdateTime(new Date());
        tbAiDhVideoMaterialMapper.updateById(tbAiDhVideoMaterial);
        HashMap res = new HashMap();
        res.put("data", 0);
        return res;
    }

    @Override
    public void insertMaterial(TbAiDhVideoMaterial tbAiDhVideoMaterial) throws Exception  {
        tbAiDhVideoMaterialMapper.insert(tbAiDhVideoMaterial);
    }

    @Override
    public HashMap backgroundQuery(HashMap hashMap)  throws Exception {
        Integer pageNum = hashMap.get("pageNum") == null ? 1 : (Integer) hashMap.get("pageNum");
        Integer pageSize = hashMap.get("pageSize") == null ? 10 : (Integer) hashMap.get("pageSize");
        Page<TbAiDhVideoBackground> pageParm = new Page<>(pageNum, pageSize);
        QueryWrapper<TbAiDhVideoBackground> queryWrapper = new QueryWrapper<>();
        if (!StringUtils.isEmpty((String) hashMap.get("bgName"))) {
            queryWrapper.like("bg_name", hashMap.get("bgName"));
        }
        if (!StringUtils.isEmpty((String) hashMap.get("bgShare"))) {
            queryWrapper.eq("bg_share", hashMap.get("bgShare"));
        }
        if (!StringUtils.isEmpty((String) hashMap.get("bgType"))) {
            queryWrapper.eq("bg_type", hashMap.get("bgType"));
        }
        queryWrapper.eq("deleted","0");
        queryWrapper.orderByDesc("create_time");
        tbAiDhVideoBackgroundMapper.selectPage(pageParm, queryWrapper);
        HashMap res = new HashMap();
        res.put("total", pageParm.getTotal());
        res.put("data", pageParm.getRecords());
        return res;
    }

    @Override
    public HashMap backgroundDel(HashMap hashMap) throws Exception  {
        TbAiDhVideoBackground tbAiDhVideoBackground=new TbAiDhVideoBackground();
        tbAiDhVideoBackground.setId((String)hashMap.get("id"));
        tbAiDhVideoBackground.setDeleted(true);
        tbAiDhVideoBackground.setUpdater((String)hashMap.get("staffId"));
        tbAiDhVideoBackground.setUpdateTime(new Date());
        tbAiDhVideoBackgroundMapper.updateById(tbAiDhVideoBackground);
        HashMap res = new HashMap();
        res.put("data", 0);
        return res;
    }

    @Override
    public void insertBackground(TbAiDhVideoBackground tbAiDhVideoBackground) throws Exception {
        tbAiDhVideoBackgroundMapper.insert(tbAiDhVideoBackground);
    }

    @Override
    public Map<String, Object> composeSave(TbAiInteractHumanDo tbAiInteractHumanDo) {
        Map<String,Object> result = new HashMap<>();
        Map<String,String> params = new HashMap<>();
        String id = tbAiInteractHumanDo.getId();
        boolean isUpdate = false;
        if(null != id && !"".equals(id)){
            isUpdate = true;
        }else{
            id = String.valueOf(SequenceUtils.getSeq());
            isUpdate = false;
        }
        tbAiInteractHumanDo.setId(id);
        tbAiInteractHumanDo.setReleaseState("1");

        LocalDateTime now = LocalDateTime.now();
        tbAiInteractHumanDo.setCreateTime(new Date());
        //主表保存
        String releaseState = tbAiInteractHumanDo.getReleaseState();
        int flag = 0;
        if(!isUpdate){
            flag = tbAiInteractHumanMapper.insert(tbAiInteractHumanDo);
            //保存图层明细 layerInfo
            List<TbAiDhVideoLayerTrackDo> layerInfo = tbAiInteractHumanDo.getLayerInfo();
            for (TbAiDhVideoLayerTrackDo layer : layerInfo){
                layer.setSceneId(id);
                layer.setSceneType("2");
                layer.setCreator(tbAiInteractHumanDo.getCreator());
                layer.setCreateTime(new Date());
                tbAiDhVideoLayerTrackMapper.insert(layer);
            }
        }else if (isUpdate){
            TbAiInteractHuman temp=new TbAiInteractHumanDo();
            BeanUtils.copyProperties(tbAiInteractHumanDo,temp);
            temp.setCreator(null);
            temp.setCreateTime(null);
            temp.setUpdater(tbAiInteractHumanDo.getCreator());
            temp.setUpdateTime(new Date());
            flag = tbAiInteractHumanMapper.updateById(temp);
            QueryWrapper<TbAiDhVideoLayerTrack> queryWrapper = new QueryWrapper<>();
            queryWrapper.eq("scene_id", id);
            //删除图层明细
            tbAiDhVideoLayerTrackMapper.delete(queryWrapper);
            //插入图层明细
            List<TbAiDhVideoLayerTrackDo> layerInfo = tbAiInteractHumanDo.getLayerInfo();
            for (TbAiDhVideoLayerTrackDo layer : layerInfo){
                layer.setSceneId(id);
                layer.setSceneType("2");
                layer.setCreator(tbAiInteractHumanDo.getCreator());
                layer.setCreateTime(new Date());
                tbAiDhVideoLayerTrackMapper.insert(layer);
            }
        }

        tbAiInteractHumanDo.setId(id);

        if("1".equals(releaseState) && flag > 0 ){ //是正式保存且保存成功
            String finalId = id;
            Thread thread = new Thread() {
                @Override
                public void run() {
                    //更新状态
                    TbAiInteractHuman tempUpdate=new TbAiInteractHuman();
                    tempUpdate.setId(finalId);
                    tempUpdate.setReleaseState("2");
                    tbAiInteractHumanMapper.updateById(tempUpdate);
                    //交互合成
                    //
                }
            };
            thread.start();
        }
        result.put("id",id);
        return result;
    }

    @Override
    public Map<String, Object> composePage(TbAiInteractHumanDo tbAiInteractHumanDo) {
        Page<TbAiInteractHuman> pageParm = new Page<>(tbAiInteractHumanDo.getPageNo(), tbAiInteractHumanDo.getPageSize());
        QueryWrapper<TbAiInteractHuman> queryWrapper=new QueryWrapper<>();
        String releaseState = tbAiInteractHumanDo.getReleaseState();
        if(null != releaseState && !"".equals(releaseState)){
            queryWrapper.eq("release_state", releaseState);
        }
        queryWrapper.eq("deleted", "0");
        queryWrapper.orderByDesc("create_time");
        tbAiInteractHumanMapper.selectPage(pageParm, queryWrapper);

        HashMap res=new HashMap();

        res.put("list",pageParm.getRecords());
        res.put("total",pageParm.getTotal());

        return res;
    }

    @Override
    public Map<String, Object> composeDel(TbAiInteractHumanDo tbAiInteractHumanDo) {
        TbAiInteractHuman tbAiInteractHuman=new TbAiInteractHuman();
        tbAiInteractHuman.setId(tbAiInteractHumanDo.getId());
        tbAiInteractHuman.setDeleted(true);
        tbAiInteractHuman.setUpdater(tbAiInteractHuman.getUpdater());
        tbAiInteractHuman.setUpdateTime(new Date());
        tbAiInteractHumanMapper.updateById(tbAiInteractHuman);
        TbAiDhVideoLayerTrack tbAiDhVideoLayerTrack=new TbAiDhVideoLayerTrack();
        tbAiDhVideoLayerTrack.setUpdater(tbAiInteractHuman.getUpdater());
        tbAiDhVideoLayerTrack.setUpdateTime(new Date());
        tbAiDhVideoLayerTrack.setDeleted(true);
        QueryWrapper<TbAiDhVideoLayerTrack> queryWrapper = new QueryWrapper<>();
        queryWrapper.eq("scene_type", "2");
        queryWrapper.eq("scene_id", tbAiInteractHumanDo.getId());
        tbAiDhVideoLayerTrackMapper.update(tbAiDhVideoLayerTrack,queryWrapper);
        return null;
    }

    @Override
    public TbAiInteractHumanDo composeQueOne(String id) {
        TbAiInteractHumanDo tbAiInteractHumanDo=new TbAiInteractHumanDo();
        List<TbAiDhVideoLayerTrackDo> list=new ArrayList<>();
        tbAiInteractHumanDo.setLayerInfo(list);
        TbAiInteractHuman tbAiInteractHuman = tbAiInteractHumanMapper.selectById(id);
        BeanUtils.copyProperties(tbAiInteractHuman,tbAiInteractHumanDo);
        QueryWrapper<TbAiDhVideoLayerTrack> queryWrapper = new QueryWrapper<>();
        queryWrapper.eq("scene_type","2");
        queryWrapper.eq("scene_id",id);
        List<TbAiDhVideoLayerTrack> tbAiDhVideoLayerTracks = tbAiDhVideoLayerTrackMapper.selectList(queryWrapper);
        for (int i = 0; i < tbAiDhVideoLayerTracks.size(); i++) {
            TbAiDhVideoLayerTrack tbAiDhVideoLayerTrack = tbAiDhVideoLayerTracks.get(i);
            TbAiDhVideoLayerTrackDo tbAiDhVideoLayerTrackDo=new TbAiDhVideoLayerTrackDo();
            BeanUtils.copyProperties(tbAiDhVideoLayerTrack,tbAiDhVideoLayerTrackDo);
            String layerType =tbAiDhVideoLayerTrack.getLayerType();
            if(layerType.equals("1")){ // 数字人的页面
                String humanId = tbAiDhVideoLayerTrack.getLayerTypeId();
                AiDhHumanDO aiDhHumanDO = aiDhHumanMapper.selectById(humanId);
                tbAiDhVideoLayerTrackDo.setSrc(aiDhHumanDO.getHumanGenerateUrl());
            }else if (layerType.equals("4")){ //  对话Agent
                String agentId = tbAiDhVideoLayerTrack.getLayerTypeId();
                AiInteractAgentDo aiInteractAgentDo = aiInteractAgentMapper.selectById(agentId);
                tbAiDhVideoLayerTrackDo.setSrc(aiInteractAgentDo.getAgentApiUrl());
            }else if (layerType.equals("6")){ // 背景图片
                String bgId = tbAiDhVideoLayerTrack.getLayerTypeId();
                TbAiDhVideoBackground tbAiDhVideoBackground = tbAiDhVideoBackgroundMapper.selectById(bgId);
                tbAiDhVideoLayerTrackDo.setSrc(tbAiDhVideoBackground.getBgUrl());
            }
            list.add(tbAiDhVideoLayerTrackDo);

        }
        return tbAiInteractHumanDo;
    }
}
