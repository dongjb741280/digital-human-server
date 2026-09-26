package cn.iocoder.yudao.module.digital.service.impl;

import cn.iocoder.yudao.module.digital.dal.mysql.TbAiDhAosterDesignMapper;
import cn.iocoder.yudao.module.digital.dal.mysql.TbAiDhFontTemplateMapper;
import cn.iocoder.yudao.module.digital.entity.TbAiDhAosterDesign;
import cn.iocoder.yudao.module.digital.entity.TbAiDhFontTemplate;
import cn.iocoder.yudao.module.digital.entity.TbAiDhVoice;
import cn.iocoder.yudao.module.digital.service.AosterDesignService;
import cn.iocoder.yudao.module.digital.util.DateUtils;
import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.baomidou.mybatisplus.core.toolkit.StringUtils;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

import javax.annotation.Resource;
import java.util.Date;
import java.util.List;

@Service
public class AosterDesignServiceImpl implements AosterDesignService {

    @Resource
    private TbAiDhFontTemplateMapper tbAiDhFontTemplateMapper;

    @Resource
    private TbAiDhAosterDesignMapper tbAiDhAosterDesignMapper;

    @Value("${digital-ability.minioEndpoint:''}")
    private String minioEndpoint;

    @Value("${digital-ability.minioBucket:''}")
    private String minioBucket;

    //获取字体模板
    @Override
    public List<TbAiDhFontTemplate> getFontTemplate() throws Exception {
        QueryWrapper<TbAiDhFontTemplate> queryWrapper = new QueryWrapper<>();
        List<TbAiDhFontTemplate> tbAiDhFontTemplates = tbAiDhFontTemplateMapper.selectList(queryWrapper);
        tbAiDhFontTemplates.forEach(tbAiDhFontTemplate -> {
            tbAiDhFontTemplate.setFontPath(minioEndpoint + "/" + minioBucket + "/" + tbAiDhFontTemplate.getFontPath());
        });
        return tbAiDhFontTemplates;
    }

    //插入模板
    @Override
    public int insertAosterDesign(TbAiDhAosterDesign tbAiDhAosterDesign) {
        tbAiDhAosterDesign.setCreateTime(DateUtils.getDateToString(new Date(),DateUtils.DATE_TIME_NO_HORI_PATTERN));
        return tbAiDhAosterDesignMapper.insert(tbAiDhAosterDesign);
    }

    //获取海报模板列表
    @Override
    public List<TbAiDhAosterDesign> getAosterDesignList(TbAiDhAosterDesign tbAiDhAosterDesign) throws Exception {
        QueryWrapper<TbAiDhAosterDesign> queryWrapper = new QueryWrapper<>();

        String isTemplate = tbAiDhAosterDesign.getIsTemplate();
        if (!StringUtils.isEmpty(tbAiDhAosterDesign.getIsTemplate())) {
            queryWrapper.eq("is_template", isTemplate);
        }
        if (!StringUtils.isEmpty(tbAiDhAosterDesign.getDesignName())) {
            queryWrapper.like("design_name", tbAiDhAosterDesign.getDesignName());
        }
        queryWrapper.orderByDesc("create_time");
        List<TbAiDhAosterDesign> aosterDesign = tbAiDhAosterDesignMapper.selectList(queryWrapper);
        aosterDesign.forEach(tbAiDhFontTemplate -> {
            tbAiDhFontTemplate.setDesignUrl(tbAiDhFontTemplate.getDesignUrl());
        });
        return aosterDesign;
    }

    //删除海报模板
    @Override
    public int deleteTbAiDhAosterDesign(TbAiDhAosterDesign tbAiDhAosterDesign) throws Exception {
        return tbAiDhAosterDesignMapper.deleteById(tbAiDhAosterDesign.getId());
    }

    //修改海报模板
    @Override
    public int updateAosterDesignList(TbAiDhAosterDesign tbAiDhAosterDesign) throws Exception {
        tbAiDhAosterDesign.setUpdateTime(DateUtils.getDateToString(new Date(),DateUtils.DATE_TIME_NO_HORI_PATTERN));
        return tbAiDhAosterDesignMapper.updateById(tbAiDhAosterDesign);
    }
}
