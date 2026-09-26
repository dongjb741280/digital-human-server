package cn.iocoder.yudao.module.digital.dal;

import cn.iocoder.yudao.module.digital.entity.TbAiDhVideoLayerTrack;
import cn.iocoder.yudao.module.digital.entity.TbAiInteractHuman;
import lombok.Data;

import java.util.List;

/**
 * @author lkm
 * @ClassName:
 * @Description:
 * @date 2024-09-21-11:13
 */
@Data
public class TbAiInteractHumanDo extends TbAiInteractHuman {

    private List<TbAiDhVideoLayerTrackDo> layerInfo;

    private Integer pageNo ;

    private Integer pageSize ;
}
