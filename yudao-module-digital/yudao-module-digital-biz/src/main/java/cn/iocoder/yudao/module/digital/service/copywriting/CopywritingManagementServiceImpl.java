package cn.iocoder.yudao.module.digital.service.copywriting;

import cn.iocoder.yudao.framework.common.pojo.PageResult;
import cn.iocoder.yudao.framework.common.util.date.DateUtils;
import cn.iocoder.yudao.module.digital.dal.AiCopyWriteAgentDO;
import cn.iocoder.yudao.module.digital.dal.AiCopyWritePptRecordDO;
import cn.iocoder.yudao.module.digital.dal.mysql.AiCopyWriteAgentMapper;
import cn.iocoder.yudao.module.digital.dal.mysql.AiCopyWritePptRecordMapper;
import cn.iocoder.yudao.module.digital.service.AiDhPptService;
import cn.iocoder.yudao.module.digital.service.CallPythonService;
import cn.iocoder.yudao.module.digital.util.SequenceUtils;
import cn.iocoder.yudao.module.digital.vo.*;
import com.alibaba.fastjson.JSON;
import com.alibaba.fastjson.JSONObject;
import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import lombok.Data;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.BeanUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import javax.annotation.Resource;
import java.util.*;
import java.util.stream.Collectors;

@Service("copywritingManagementService")
@Slf4j
public class CopywritingManagementServiceImpl implements CopywritingManagementService{

    @Resource
    private AiCopyWriteAgentMapper aiCopyWriteAgentMapper;

    @Resource
    private AiCopyWritePptRecordMapper aiCopyWritePptRecordMapper;

    @Resource
    private CallPythonService callPythonService;

    @Resource
    private AiDhPptService aiDhPptService;

    @Override
    public PageResult<CopywritingRespVO> copywritingListQuery(CopywritingReqVO copywritingReqVO) {
        PageResult<AiCopyWriteAgentDO>  aiCopyWriteAgentDOPageResult=new PageResult<>();
        String publicLibType=copywritingReqVO.getPublicLibType();
        List<CopywritingRespVO> copywritingRespVOS=new ArrayList<>();
        try{
            if (publicLibType.equals("0")){
                aiCopyWriteAgentDOPageResult = aiCopyWriteAgentMapper.selectByOprStaffAndCopywriteShare(copywritingReqVO);
            }else if (publicLibType.equals("1")){
                aiCopyWriteAgentDOPageResult = aiCopyWriteAgentMapper.selectByCopywriteShare(copywritingReqVO);
            }
            copywritingRespVOS = aiCopyWriteAgentDOPageResult.getList().stream().map(aiCopyWriteAgentDO -> {
                CopywritingRespVO copywritingRespVO1 = new CopywritingRespVO();
                BeanUtils.copyProperties(aiCopyWriteAgentDO,copywritingRespVO1);
                return copywritingRespVO1;
            }).collect(Collectors.toList());
        }catch (Exception e){
            log.error("copywritingListQuery查询处理异常",e);
        }
        return new PageResult<>(copywritingRespVOS, aiCopyWriteAgentDOPageResult.getTotal());
    }

    @Override
    public void copywritingDelete(CopywritingUpdateReqVO copywritingUpdateReqVO) {
        aiCopyWriteAgentMapper.deleteByIdAndOprStaff(copywritingUpdateReqVO.getId(),copywritingUpdateReqVO.getOprStaff());
    }

    @Override
    public CopywritingRespVO copywritingGetOne(CopywritingReqVO copywritingReqVO) {

        AiCopyWriteAgentDO aiCopyWriteAgentDO = aiCopyWriteAgentMapper.selectOne(new LambdaQueryWrapper<AiCopyWriteAgentDO>().eq(AiCopyWriteAgentDO::getId, copywritingReqVO.getId()));
        CopywritingRespVO copywritingRespVO = new CopywritingRespVO();
        BeanUtils.copyProperties(aiCopyWriteAgentDO,copywritingRespVO);
        return copywritingRespVO;
    }

    @Override
    public void copywritingUpdate(CopywritingUpdateReqVO copywritingUpdateReqVO) {
        AiCopyWriteAgentDO aiCopyWriteAgentDO=new AiCopyWriteAgentDO();
        aiCopyWriteAgentDO.setId(copywritingUpdateReqVO.getId());
        aiCopyWriteAgentDO.setCopywriteContent(copywritingUpdateReqVO.getCopywriteContent());
        aiCopyWriteAgentDO.setCopywriteTitle(copywritingUpdateReqVO.getCopywriteTitle());
        aiCopyWriteAgentMapper.updateById(aiCopyWriteAgentDO);
    }

    @Override
    public PageResult<CopyWritePptRecordRespVO> copywritingListPPT(CopywritingReqVO copywritingReqVO) {

        PageResult<AiCopyWritePptRecordDO>  aiCopyWritePptRecordDO= aiCopyWritePptRecordMapper.selectByCopyWritingPPTPage(copywritingReqVO);

        List<CopyWritePptRecordRespVO> copywritingRespVOS=new ArrayList<>();
        try{
            copywritingRespVOS = aiCopyWritePptRecordDO.getList().stream().map(aiCopyWritePptRecordDOPageResult -> {
                CopyWritePptRecordRespVO copywritingRespVO = new CopyWritePptRecordRespVO();
                BeanUtils.copyProperties(aiCopyWritePptRecordDOPageResult,copywritingRespVO);
                return copywritingRespVO;
            }).collect(Collectors.toList());
        }catch (Exception e){
            log.error("copywritingListQuery查询处理异常",e);
        }
        return new PageResult<>(copywritingRespVOS, aiCopyWritePptRecordDO.getTotal());
    }

    @Override
    public void copywritingPPTDelete(CopywritingPptReqVO copywritingPptReqVO) {
        //删除 ppt
        aiCopyWritePptRecordMapper.delete(new LambdaQueryWrapper<AiCopyWritePptRecordDO>().eq(AiCopyWritePptRecordDO::getId, copywritingPptReqVO.getId()));
    }

    @Override
    public void copywritingUploadPPT(CopywritingPptReqVO copywritingPptReqVO) {

        try {

            //获取文案 id 下所有 ppt
            List<AiCopyWritePptRecordDO> aiCopyWritePptRecordDOList = aiCopyWritePptRecordMapper.selectList(new LambdaQueryWrapper<AiCopyWritePptRecordDO>().eq(AiCopyWritePptRecordDO::getCopywriteId, copywritingPptReqVO.getId()));
            Optional<AiCopyWritePptRecordDO> maxVersionRecord = aiCopyWritePptRecordDOList.stream()
                    .max(Comparator.comparingLong(AiCopyWritePptRecordDO::parseRecordVersion));

            AiCopyWritePptRecordDO recordWithMaxVersion = maxVersionRecord.orElse(null);
            String recordVersion = recordWithMaxVersion != null ? recordWithMaxVersion.getRecordVersion() : "1";

            AiCopyWritePptRecordDO aiCopyWritePptRecordDO=new AiCopyWritePptRecordDO();
            Long id= SequenceUtils.getSeq();
            aiCopyWritePptRecordDO.setId(String.valueOf(id));
            aiCopyWritePptRecordDO.setCopywriteId(copywritingPptReqVO.getCopywriteId());
            aiCopyWritePptRecordDO.setPptUrl(copywritingPptReqVO.getFilePath());
            aiCopyWritePptRecordDO.setRecordDesc(copywritingPptReqVO.getFileName());
            aiCopyWritePptRecordDO.setRecordFormat(copywritingPptReqVO.getFileFormat());
            aiCopyWritePptRecordDO.setRecordSize(copywritingPptReqVO.getFileSize());
            aiCopyWritePptRecordDO.setRecordType("1");
            aiCopyWritePptRecordDO.setRecordVersion(String.valueOf(Integer.parseInt(recordVersion)+1));
            aiCopyWritePptRecordDO.setOprTime(DateUtils.getNowTime());
            aiCopyWritePptRecordDO.setOprStaff(copywritingPptReqVO.getOprStaff());
            aiCopyWritePptRecordMapper.insert(aiCopyWritePptRecordDO);

            //更新文案主表 pptid
            AiCopyWriteAgentDO aiCopyWriteAgentDO=new AiCopyWriteAgentDO();
            aiCopyWriteAgentDO.setId(copywritingPptReqVO.getCopywriteId());
            aiCopyWriteAgentDO.setMainPptId(String.valueOf(id));
            aiCopyWriteAgentMapper.updateById(aiCopyWriteAgentDO);

            //请求 python 接口
            // ppt 转图片 python 接口
            CopywritingPptToImagesReqVO copywritingPptToImagesReqVO=new CopywritingPptToImagesReqVO();
            copywritingPptToImagesReqVO.setCopywrite_id(copywritingPptReqVO.getCopywriteId());
            copywritingPptToImagesReqVO.setPpt_id(String.valueOf(id));
            copywritingPptToImagesReqVO.setPpt_path(copywritingPptReqVO.getFilePath());
            copywritingPptToImagesReqVO.setUser(copywritingPptReqVO.getOprStaff());
            copywritingPptToImagesReqVO.setUuid(String.valueOf(UUID.randomUUID()));
            //callPythonService.pptToImagesPythonPost(JSONObject.parseObject(JSON.toJSONString(copywritingPptToImagesReqVO)));

            aiDhPptService.pilgrimage(JSONObject.parseObject(JSON.toJSONString(copywritingPptToImagesReqVO)));

        }catch (Exception e){
            log.error("ppt上传异常",e);
        }

    }

    public static void main(String[] args) {
        CopywritingPptToImagesReqVO copywritingPptToImagesReqVO=new CopywritingPptToImagesReqVO();

        copywritingPptToImagesReqVO.setUuid(String.valueOf(UUID.randomUUID()));
        JSONObject jsonObject = JSONObject.parseObject(JSON.toJSONString(copywritingPptToImagesReqVO));

        System.out.println(jsonObject.toJSONString());
    }
    @Override
    public AiCopyWritePptRecordDO copywritingDownloadPPT(CopywritingPptReqVO copywritingPptReqVO) {

        return aiCopyWritePptRecordMapper.selectOne(new LambdaQueryWrapper<AiCopyWritePptRecordDO>().eq(AiCopyWritePptRecordDO::getId, copywritingPptReqVO.getId()));
    }

    @Override
    public String copywritingCreate(String copywriteName, String copywriteTitle, String copywriteContent, String oprStaff) {
        AiCopyWriteAgentDO aiCopyWriteAgentDO = new AiCopyWriteAgentDO();
        String id = String.valueOf(SequenceUtils.getSeq());
        aiCopyWriteAgentDO.setId(id);
        aiCopyWriteAgentDO.setCopywriteName(copywriteName);
        aiCopyWriteAgentDO.setCopywriteTitle(copywriteTitle);
        aiCopyWriteAgentDO.setCopywriteContent(copywriteContent);
        aiCopyWriteAgentDO.setCopywriteShare("0");
        aiCopyWriteAgentDO.setOprStaff(oprStaff);
        aiCopyWriteAgentDO.setOprTime(DateUtils.getNowTime());
        aiCopyWriteAgentMapper.insert(aiCopyWriteAgentDO);
        return id;
    }

    @Override
    public String copywritingCreatePPT(String copywriteId, String pptUrl, String recordDesc, String oprStaff) {
        AiCopyWritePptRecordDO record = new AiCopyWritePptRecordDO();
        String id = String.valueOf(SequenceUtils.getSeq());
        record.setId(id);
        record.setCopywriteId(copywriteId);
        record.setPptUrl(pptUrl);
        record.setRecordDesc(recordDesc);
        record.setRecordType("0");
        record.setRecordVersion("1");
        record.setRecordFormat("pptx");
        record.setOprTime(DateUtils.getNowTime());
        record.setOprStaff(oprStaff);
        aiCopyWritePptRecordMapper.insert(record);

        AiCopyWriteAgentDO agent = new AiCopyWriteAgentDO();
        agent.setId(copywriteId);
        agent.setMainPptId(id);
        aiCopyWriteAgentMapper.updateById(agent);
        return id;
    }

}
