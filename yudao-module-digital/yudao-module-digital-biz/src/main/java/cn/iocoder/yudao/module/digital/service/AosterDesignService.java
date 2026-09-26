package cn.iocoder.yudao.module.digital.service;

import cn.iocoder.yudao.module.digital.entity.TbAiDhAosterDesign;
import cn.iocoder.yudao.module.digital.entity.TbAiDhFontTemplate;
import org.springframework.web.bind.annotation.RequestBody;

import java.util.List;

public interface AosterDesignService {


    public List<TbAiDhFontTemplate> getFontTemplate() throws Exception;

    public int insertAosterDesign(TbAiDhAosterDesign tbAiDhAosterDesign);

    public List<TbAiDhAosterDesign> getAosterDesignList(TbAiDhAosterDesign tbAiDhAosterDesign) throws Exception;

    public int deleteTbAiDhAosterDesign(TbAiDhAosterDesign tbAiDhAosterDesign) throws Exception;

    public int updateAosterDesignList(TbAiDhAosterDesign tbAiDhAosterDesign) throws Exception;
}
