package cn.iocoder.yudao.module.digital.dal.mysql;


import cn.iocoder.yudao.framework.common.pojo.PageResult;
import cn.iocoder.yudao.framework.mybatis.core.mapper.BaseMapperX;
import cn.iocoder.yudao.module.digital.dal.AiCopyWriteAgentDO;
import cn.iocoder.yudao.module.digital.dal.AiCopyWritePptRecordDO;
import cn.iocoder.yudao.module.digital.vo.CopywritingReqVO;
import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import org.apache.ibatis.annotations.Mapper;

@Mapper
public interface AiCopyWritePptRecordMapper extends BaseMapperX<AiCopyWritePptRecordDO> {

    /**
     * 通过 oprStaff 和 copywriteShare 查询 AiCopyWriteAgentDO 表，返回所有匹配的记录列表
     *
     * @param copywritingReqVO 字段值
     * @return 匹配的记录列表
     */
    default PageResult<AiCopyWritePptRecordDO> selectByCopyWritingPPTPage( CopywritingReqVO copywritingReqVO) {
        return this.selectPage(copywritingReqVO,  new LambdaQueryWrapper<AiCopyWritePptRecordDO>()
                .eq(AiCopyWritePptRecordDO::getCopywriteId, copywritingReqVO.getId()));
    }


}
