package cn.iocoder.yudao.module.digital.service.impl;

import cn.iocoder.yudao.framework.mybatis.core.query.LambdaQueryWrapperX;
import cn.iocoder.yudao.module.digital.adaptor.AiDhHumanVideoAdaptor;
import cn.iocoder.yudao.module.digital.dal.*;
import cn.iocoder.yudao.module.digital.dal.mysql.*;
import cn.iocoder.yudao.module.digital.entity.TbAiDhVideoBackground;
import cn.iocoder.yudao.module.digital.entity.TbAiDhVideoMaterial;
import cn.iocoder.yudao.module.digital.entity.TbAiDhVoice;
import cn.iocoder.yudao.module.digital.service.AiDhHumanService;
import cn.iocoder.yudao.module.digital.service.AiDhHumanVideoService;
import cn.iocoder.yudao.module.digital.service.ITbAiDhVoiceService;
import cn.iocoder.yudao.module.digital.service.MinioClientService;
import cn.iocoder.yudao.module.digital.service.copywriting.CopywritingManagementService;
import cn.iocoder.yudao.module.digital.util.*;
import cn.iocoder.yudao.module.digital.vo.*;
import com.alibaba.fastjson.JSONObject;
import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.codec.binary.Base64;
import org.apache.commons.lang3.StringUtils;
import org.springframework.beans.BeanUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

import javax.annotation.Resource;
import java.io.*;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.*;
import java.util.stream.Collectors;


@Service
@Slf4j
public class AiDhHumanVideoServiceImpl implements AiDhHumanVideoService {


    @Resource
    private AiDhHumanVideoMapper aiDhHumanVideoMapper;

    @Resource
    private AiDhVideoLayerTrackMapper aiDhVideoLayerTrackMapper ;

    @Resource
    private CommonMapper commonMapper;


    @Resource
    private AiDhHumanMapper aiDhHumanMapper;


    @Autowired
    private TbAiDhVideoBackgroundMapper tbAiDhVideoBackgroundMapper;

    @Autowired
    private TbAiDhVideoMaterialMapper tbAiDhVideoMaterialMapper;

    @Resource
    private MinioClientService minioClientService;

    @Resource
    private CopywritingManagementService copywritingManagementService;

    @Resource
    private AiDhHumanService aiDhHumanService;

    @Resource
    private ITbAiDhVoiceService iTbAiDhVoiceService;

    @Value("${digital-ability.outputVoicehumanUrl:''}")
    private String outputVoicehumanUrl;

    @Value("${digital-ability.museTalkdownLocalPath:''}")
    private String museTalkdownLocalPath;

    @Value("${digital-ability.voiceAddHUmanresultDir:''}")
    private String voiceAddHUmanresultDir;

    @Value("${digital-ability.voiceAddHUmanInterFaceUrl:''}")
    private String voiceAddHUmanInterFaceUrl;

    @Value("${digital-ability.videoAddImagePath:''}")
    private String videoAddImagePath;

    @Value("${digital-ability.videoAddImageInterFaceUrl:''}")
    private String videoAddImageInterFaceUrl;

    @Value("${digital-ability.mergeDirectory:''}")
    private String mergeDirectory;

    @Value("${digital-ability.mergeVoiceDirectory:''}")
    private String mergeVoiceDirectory;


    @Value("${digital-ability.mergeOutputFilePath:''}")
    private String mergeOutputFilePath;

    @Value("${digital-ability.mergeVideoInterFaceUrl:''}")
    private String mergeVideoInterFaceUrl;


    @Value("${digital-ability.resolutionAndAspectInterfaceUrl:''}")
    private String resolutionAndAspectInterfaceUrl;

    @Value("${digital-ability.resolutionAndAspectOutputPath:''}")
    private String resolutionAndAspectOutputPath;

    @Value("${digital-ability.captionsInterfaceUrl:''}")
    private String captionsInterfaceUrl;

    @Value("${digital-ability.captionsOutputPath:''}")
    private String captionsOutputPath;

    @Value("${digital-ability.firstFrameInterfaceUrl:''}")
    private String firstFrameInterfaceUrl;

    @Value("${digital-ability.firstFrameOutputPath:''}")
    private String firstFrameOutputPath;

    @Value("${digital-ability.voicePPtRecordUrl:''}")
    private String voicePPtRecordUrl;

    @Value("${digital-ability.splitHumanOutPutUrl:''}")
    private String splitHumanOutPutUrl;

    @Value("${digital-ability.splitHumanInterfaceUrl:''}")
    private String splitHumanInterfaceUrl;

    @Value("${digital-ability.minioEndpoint:''}")
    private String minioEndpoint;

    @Value("${digital-ability.minioBucket:''}")
    private String minioBucket;

    @Value("${digital-ability.defaultImageUrl:''}")
    private String defaultImageUrl;


    private static final String VOICE_ADD_HUMAN_TYPE = "voice_add_human";

    private static final String VIDEO_ADD_IMAGE_TYPE = "video_add_image";

    private static final String MERGE_VIDEO_TYPE = "merge_video";

    private static final String RESOLUTION_ASPECT_TYPE = "resolution_aspect";

    private static final String ADD_CAPTIONS_TYPE = "add_captions";

    private static final String FIRST_FRAME_TYPE = "first_frame";

    private static final String TXT_2_VIDEO_TYPE = "txt_2_voice";


    private static final String SPLIT_VIDEO_TYPE = "split_video_type";


    private static final String SUCCESS_VIDEO_STATUS = "4";

    private static final String ERROR_VIDEO_STATUS = "3";


    private static final String RUN_VIDEO_STATUS = "2";

    private static final int TOTAL_CAN_RUN_NUM = 3;




    /**
     * 保存数字人视频接口
     */
    public Map<String,Object> saveAiDhHumanVideo(AiDhHumanVideoSaveVO aiDhHumanVideoSaveVO){
        Map<String,Object> result = new HashMap<>();
        Map<String,String> params = new HashMap<>();
        String id = aiDhHumanVideoSaveVO.getId();
        boolean isUpdate = false;
        if(null != id && !"".equals(id)){ //说明是正式保存
            isUpdate = true;
        }else{ //存为草稿
            id = String.valueOf(SequenceUtils.getSeq());
            isUpdate = false;
        }
        params.put("id",id);
        params.put("videoStatus","1"); // 状态为未执行

        LocalDateTime now = LocalDateTime.now();
        // 定义日期时间格式
        DateTimeFormatter formatter = DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss");
        // 使用定义的格式器格式化当前的 LocalDateTime
        String formattedDateTime = now.format(formatter);
        params.put("optTime",formattedDateTime);
        //主表保存
        AiDhHumanVideoDO applyData = AiDhHumanVideoAdaptor.saveCovDo.apply(aiDhHumanVideoSaveVO, params);
        String videoSave = aiDhHumanVideoSaveVO.getVideoSave();
        int flag = 0;
        if(!isUpdate){ //草稿
            flag = aiDhHumanVideoMapper.insert(applyData);
            //保存图层明细 layerInfo
            List<AiDhVideoLayerTrackDO> layerInfo = aiDhHumanVideoSaveVO.getLayerInfo();
            for (AiDhVideoLayerTrackDO layer : layerInfo){
                layer.setSceneId(id);
                layer.setSceneType("1");
                layer.setCreator(aiDhHumanVideoSaveVO.getOprStaff());
                layer.setCreateTime(new Date());
                aiDhVideoLayerTrackMapper.insert(layer);
            }
        }else if (isUpdate){ // 正式保存
            flag = aiDhHumanVideoMapper.updateById(applyData);
            QueryWrapper<AiDhVideoLayerTrackDO> queryWrapper = new QueryWrapper<>();
            queryWrapper.eq("scene_id", id);
            //删除图层明细
            aiDhVideoLayerTrackMapper.delete(queryWrapper);
            //插入图层明细
            List<AiDhVideoLayerTrackDO> layerInfo = aiDhHumanVideoSaveVO.getLayerInfo();
            for (AiDhVideoLayerTrackDO layer : layerInfo){
                layer.setSceneId(id);
                layer.setSceneType("1");
                layer.setCreator(applyData.getOprStaff());
                layer.setCreateTime(new Date());
                aiDhVideoLayerTrackMapper.insert(layer);
            }
        }

        aiDhHumanVideoSaveVO.setId(id);

        if("1".equals(videoSave) && flag > 0 ){ //是正式保存且保存成功
            String finalId = id;
            Thread thread = new Thread() {
                @Override
                public void run() {
                    //更新状态
                    updateMakeVideoStatus (finalId ,RUN_VIDEO_STATUS);
                    //视频制作-先屏蔽
                    makeVideo(aiDhHumanVideoSaveVO);
                }
            };
            thread.start();
        }
        result.put("id",id);
        return result;
    }

    /**
     * ppt播报制作的流程进行处理
     */
    public void makeVideo(AiDhHumanVideoSaveVO aiDhHumanVideoSaveVO){
        //调整比例 0 取消
        aiDhHumanVideoSaveVO.setIsAdjust("0");
        //添加字幕 0 取消
        aiDhHumanVideoSaveVO.setIsCaptions("1");

        String voiceId = aiDhHumanVideoSaveVO.getVoiceId();
        //每段ppt生成语音
        String copywriteId = aiDhHumanVideoSaveVO.getCopywriteId();
        CopywritingReqVO copywritingReqVO = new CopywritingReqVO();
        copywritingReqVO.setId(copywriteId);
        CopywritingRespVO copywritingRespVO = copywritingManagementService.copywritingGetOne(copywritingReqVO);
        String mainPptId = copywritingRespVO.getMainPptId();
        Map<String,Object> params = new HashMap<>();
        params.put("mainPptId",mainPptId);
        List<Map> pptRecordDetailList = commonMapper.getPptRecordDetail(params);
        int execOrder = 0 ;

        String videoId = aiDhHumanVideoSaveVO.getId();

        //String batch = "1736952156649";
        String batch = null;

        if(null == batch){
            //作为当前批次
             batch = System.currentTimeMillis()+"";
            //ppt中文文本转语音
            for(int i=0;i<pptRecordDetailList.size();i++){
                params.put("batchNum",batch);
                params.put("modelId",mainPptId);
                params.put("modelNum",i);
                params.put("modelType",TXT_2_VIDEO_TYPE);
                params.put("execStatus","");
                params.put("videoId",videoId);
                params.put("execOrder",execOrder);
                commonMapper.insertVideoExecLog(params);
                execOrder++;
            }

            //进行视频拼接按照声音长度处理
            params.put("batchNum",batch);
            params.put("modelId",mainPptId);
            params.put("modelNum","");
            params.put("modelType",SPLIT_VIDEO_TYPE);
            params.put("execStatus","");
            params.put("videoId",videoId);
            params.put("execOrder",execOrder);
            commonMapper.insertVideoExecLog(params);
            execOrder++;

            //给每段ppt调用musetalk对嘴型
            for(int i=0;i<pptRecordDetailList.size();i++){
                params.put("batchNum",batch);
                params.put("modelId",mainPptId);
                params.put("modelNum",i);
                params.put("modelType",VOICE_ADD_HUMAN_TYPE);
                params.put("execStatus","");
                params.put("videoId",videoId);
                params.put("execOrder",execOrder);
                commonMapper.insertVideoExecLog(params);
                execOrder++;
            }

            //给每段ppt进行背景替换
            for(int i=0;i<pptRecordDetailList.size();i++){
                params.put("batchNum",batch);
                params.put("modelId",mainPptId);
                params.put("modelNum",i);
                params.put("modelType",VIDEO_ADD_IMAGE_TYPE);
                params.put("execStatus","");
                params.put("videoId",videoId);
                params.put("execOrder",execOrder);
                commonMapper.insertVideoExecLog(params);
                execOrder++;
            }

            //进行大视频的合并
            params.put("batchNum",batch);
            params.put("modelId",videoId);
            params.put("modelNum","");
            params.put("modelType",MERGE_VIDEO_TYPE);
            params.put("execStatus","");
            params.put("videoId",videoId);
            params.put("execOrder",execOrder);
            commonMapper.insertVideoExecLog(params);
            execOrder++;

            // 视频的分辨率和画面比例调整
            if("1".equals(aiDhHumanVideoSaveVO.getIsAdjust())){
                params.put("batchNum",batch);
                params.put("modelId",videoId);
                params.put("modelNum","");
                params.put("modelType",RESOLUTION_ASPECT_TYPE);
                params.put("execStatus","");
                params.put("videoId",videoId);
                params.put("execOrder",execOrder);
                commonMapper.insertVideoExecLog(params);
                execOrder++;
            }

            //视频获取第一帧
            params.put("batchNum",batch);
            params.put("modelId",videoId);
            params.put("modelNum","");
            params.put("modelType",FIRST_FRAME_TYPE);
            params.put("execStatus","");
            params.put("videoId",videoId);
            params.put("execOrder",execOrder);
            commonMapper.insertVideoExecLog(params);
            execOrder++;

            //增加字幕
            if("1".equals(aiDhHumanVideoSaveVO.getIsCaptions())){
                params.put("batchNum",batch);
                params.put("modelId",videoId);
                params.put("modelNum","");
                params.put("modelType",ADD_CAPTIONS_TYPE);
                params.put("execStatus","");
                params.put("videoId",videoId);
                params.put("execOrder",execOrder);
                commonMapper.insertVideoExecLog(params);
                execOrder++;
            }
        }


        /******将以上写入到流程日志中********/

        //开始执行流程
        makePptVideoStart(videoId,batch,null);

    }

    //执行ppt文稿播报全流程开始
    @Override
    public void makePptVideoStart(String videoId,String batchNum,String modelId){

        //查询最新的执行到哪个节点
        Map<String,Object> params = new HashMap<>();
        params.put("batchNum",batchNum);
        params.put("videoId",videoId);
        AiDhHumanVideoSaveVO aiDhHumanVideoSaveVO = new AiDhHumanVideoSaveVO();
        aiDhHumanVideoSaveVO.setId(videoId);
        List<Map> execLogDetailList = commonMapper.getvideoExecLogDetail(params);
        if(execLogDetailList.size() == 0 ){ //说明整体流程执行完了
            updateMakeVideoStatus (videoId,SUCCESS_VIDEO_STATUS);
        }else{ //执行下一个流程
            Map<String,Object> execLogDetail = execLogDetailList.get(0);
            String modelType = (String) execLogDetail.get("model_type");

            if(modelType.equals(TXT_2_VIDEO_TYPE)){ //ppt声音文字转语音
                String modelNum = (String) execLogDetail.get("model_num");
                AiDhHumanVideoDO aiDhHumanVideoDO = aiDhHumanVideoMapper.selectById(videoId);
                modelId = (String) execLogDetail.get("model_id");
                makePptTxt2Voice(aiDhHumanVideoDO,batchNum,modelId,modelNum);
            }else if(modelType.equals(SPLIT_VIDEO_TYPE)){ //视频裁剪拼接
                modelId = (String) execLogDetail.get("model_id");
                splitVideoData(videoId,batchNum,modelId);
            }else if (modelType.equals(VOICE_ADD_HUMAN_TYPE)){ //配音对嘴型
                String modelNum = (String) execLogDetail.get("model_num");
                modelId = (String) execLogDetail.get("model_id");
                AiDhHumanVideoDO aiDhHumanVideoDO = aiDhHumanVideoMapper.selectById(videoId);
                voiceAddHuman(aiDhHumanVideoDO,batchNum,modelId,modelNum);
            }else if(modelType.equals(VIDEO_ADD_IMAGE_TYPE)){ //视频背景替换--同时进行多个

                //查询正在运行的个数
                params.put("modelType",VIDEO_ADD_IMAGE_TYPE);
                int execHumanAddImageNum = commonMapper.getExecHumanAddImageNum(params);
                //未执行的明细
                List<Map> notExecHumanAddImage = commonMapper.getNotExecHumanAddImageNum(params);
                int canRunNum = TOTAL_CAN_RUN_NUM - execHumanAddImageNum ;
                int i = 1;
                if(i <= TOTAL_CAN_RUN_NUM){ //说明有空闲可以执行
                    AiDhHumanVideoDO aiDhHumanVideoDO = aiDhHumanVideoMapper.selectById(videoId);
                    for (Map map : notExecHumanAddImage){
                        if(canRunNum >= i ){
                            String modelNum = (String) map.get("model_num");
                            modelId = (String) map.get("model_id");
                            videoAddImageLayerTrack(aiDhHumanVideoDO,batchNum,modelId,modelNum);
                            i++;
                        }else{
                            break;
                        }
                    }
                }
            }else if(modelType.equals(MERGE_VIDEO_TYPE)){ //视频合并

                Map<String,Object> statusParams = new HashMap<>();
                statusParams.put("batchNum",batchNum);
                statusParams.put("videoId",videoId);
                statusParams.put("modelType",VIDEO_ADD_IMAGE_TYPE);
                statusParams.put("execStatus","3");
                List<Map> videoExecLogStaus = commonMapper.getVideoExecLog(statusParams);
                if(videoExecLogStaus.size() > 0 ){
                    return;
                }
                AiDhHumanVideoDO aiDhHumanVideoDO = aiDhHumanVideoMapper.selectById(videoId);
                modelId = (String) execLogDetail.get("model_id");
                videoMerge(aiDhHumanVideoDO,batchNum,modelId);
            }else if(modelType.equals(RESOLUTION_ASPECT_TYPE)){ //视频的分辨率和画面比例接口
                String execOrder =  execLogDetail.get("exec_order")+"";
                modelId = (String) execLogDetail.get("model_id");
                AiDhHumanVideoDO aiDhHumanVideoDO = aiDhHumanVideoMapper.selectById(videoId);
                videoResolutionAspect(aiDhHumanVideoDO,batchNum,modelId,execOrder);
            }else if(modelType.equals(FIRST_FRAME_TYPE)){ //获取第一帧图片
                //将上一步的 tmp_final_video_url 放到 video表中
                String execOrder =  execLogDetail.get("exec_order")+"";
                params.put("batchNum",batchNum);
                params.put("videoId",videoId);
                params.put("execOrder",(Integer.parseInt(execOrder) - 1)+"");
                modelId = (String) execLogDetail.get("model_id");
                List<Map> lastvideoExecLogList = commonMapper.getVideoExecLog(params);

                String videoOutPathAll = lastvideoExecLogList.get(0).get("tmp_final_video_url")+"";
                params.put("videoOutPath",videoOutPathAll);
                commonMapper.updateVideoUrl(params);

                AiDhHumanVideoDO aiDhHumanVideoDO = aiDhHumanVideoMapper.selectById(videoId);
                makeVideoFrame(aiDhHumanVideoDO,batchNum,modelId);
            }else if(modelType.equals(ADD_CAPTIONS_TYPE)){ //增加字幕
                String execOrder = execLogDetail.get("exec_order")+"";
                modelId =  execLogDetail.get("model_id")+"";
                AiDhHumanVideoDO aiDhHumanVideoDO = aiDhHumanVideoMapper.selectById(videoId);
                videoAddCaptions(aiDhHumanVideoDO,batchNum,modelId,execOrder);
            }
        }

    }

    /**
     * ppt备注进行转语音
     */
    public void makePptTxt2Voice(AiDhHumanVideoDO aiDhHumanVideoDO,String batchNum,String modelId,String modelNum){

        DateTimeFormatter formatter = DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss");
        LocalDateTime now = LocalDateTime.now();
        String formattedDateTime = now.format(formatter);
        log.info("声音克隆开始时间=="+formattedDateTime+"=======");
        //每段ppt生成语音
        String copywriteId = aiDhHumanVideoDO.getCopywriteId();
        CopywritingReqVO copywritingReqVO = new CopywritingReqVO();
        copywritingReqVO.setId(copywriteId);
        CopywritingRespVO copywritingRespVO = copywritingManagementService.copywritingGetOne(copywritingReqVO);
        String mainPptId = copywritingRespVO.getMainPptId();
        Map<String,Object> params = new HashMap<>();
        params.put("mainPptId",mainPptId);
        List<Map> pptRecordDetailList = commonMapper.getPptRecordDetail(params);

        HashMap<String,Object> voiceParam = new HashMap<>();
        String sampleVoiceId = aiDhHumanVideoDO.getVoiceId();

        voiceParam.put("voiceId",sampleVoiceId);
        Map<String,Object> data = pptRecordDetailList.get(Integer.parseInt(modelNum));
        String pptImagesWords = data.get("ppt_image_words")+"";
        pptImagesWords = pptImagesWords.replace("\\n", "");

        String pptId = data.get("ppt_id")+"";
        String pptNum = data.get("ppt_num")+"";

        String humanVideoId = aiDhHumanVideoDO.getId();

        //进行每段文字进行配音
        String voiceId = batchNum+"_"+pptId+"_"+pptNum+"_"+humanVideoId; // 用pptid+pptnum作为文件名

        voiceParam.put("voiceReferConent",pptImagesWords);
        String voicePPtRecordUrlStr = voicePPtRecordUrl;
        voicePPtRecordUrlStr = voicePPtRecordUrlStr.replace("pptBatchPptIdPptNum",voiceId);
        voiceParam.put("voicePPtRecordUrl",voicePPtRecordUrlStr);
        voiceParam.put("batchpptIdnum",voiceId);

        String pptVoiceUrl = data.get("ppt_voice_url")+"";
        String length = data.get("ppt_voice_length")+"";
        if(StringUtils.isNotBlank(pptVoiceUrl) && !"null".equals(pptVoiceUrl)
        && StringUtils.isNotBlank(length) && !"null".equals(length)){
            HashMap<String, Object> voiceParams = new HashMap<>();
            voiceParams.put("voiceId",voiceId);
            voiceParams.put("voiceStatus","4");
            voiceParams.put("voiceSampleUrl",pptVoiceUrl);
            voiceParams.put("voiceType","2");
            voiceParams.put("length",Long.parseLong(length));
            iTbAiDhVoiceService.updateVoiceFinall(voiceParams);
        }else{
            iTbAiDhVoiceService.callVoiceClone(voiceParam);
        }


        now = LocalDateTime.now();
        formattedDateTime = now.format(formatter);
        log.info("声音克隆结束时间=="+formattedDateTime+"=======");

    }


    //将视频裁剪成和音频长度一致
    public void splitVideoData(String videoId,String batchNum,String pptId){

        DateTimeFormatter formatter = DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss");
        LocalDateTime now = LocalDateTime.now();
        String formattedDateTime = now.format(formatter);
        log.info("裁剪视频开始时间=="+formattedDateTime+"=======");

        int voiceLength = 0 ; //声音总长度
        List<Integer> pptNums = new ArrayList<>();
        List<Integer> pptvoiceLengths = new ArrayList<>();


        AiDhHumanVideoSaveVO aiDhHumanVideoDO = this.selectById(videoId);
        //数字人参考ID
        String humanId = aiDhHumanVideoDO.getHumanId();
        AiDhHumanRespVO aiDhHumanRespVO = aiDhHumanService.selectById(humanId);

        //优先使用抠像后的视频（绿幕），没有则退回原始视频
        String humanGenerateUrl = aiDhHumanRespVO.getHumanGenerateUrl();
        if (StringUtils.isBlank(humanGenerateUrl)) {
            humanGenerateUrl = aiDhHumanRespVO.getHumanOrgUrl();
        }
        String fileName = aiDhHumanRespVO.getFileName();



        Map<String,Object> params = new HashMap<>();
        params.put("modelType",TXT_2_VIDEO_TYPE);
        params.put("batchNum",batchNum);
        params.put("modelId",pptId);
        params.put("videoId",videoId);

        //获取语音-视频信息
        List<Map> pptRecordDetailList = commonMapper.getVideoExecLog(params);


        for(int i=0;i<pptRecordDetailList.size();i++){
            int pptVoiceLength = parseIntSafe(pptRecordDetailList.get(i).get("ppt_voice_length"));
            voiceLength+= pptVoiceLength;
            pptvoiceLengths.add(pptVoiceLength);
            pptNums.add(parseIntSafe(pptRecordDetailList.get(i).get("model_num")));
        }
        if(0 == voiceLength){
            try {
                throw new Exception("音频总长度为0");
            } catch (Exception e) {
                throw new RuntimeException(e);
            }finally {
                updateMakeVideoStatus(videoId,ERROR_VIDEO_STATUS);
            }
        }
        if(!humanGenerateUrl.endsWith("/")){
            humanGenerateUrl =   humanGenerateUrl + "/";
        }

        Map<String,Object> request = new HashMap<>();
        request.put("humanGenerateUrl",humanGenerateUrl);
        request.put("fileName",fileName);
        request.put("voiceLength",voiceLength);
        request.put("pptvoiceLengths",pptvoiceLengths);
        request.put("pptNums",pptNums);
        request.put("pptBatch",batchNum);
        request.put("videoId",videoId);
        request.put("pptId",pptId);
        request.put("outPutPath","video/"+videoId+"/"+batchNum+"/split/");

        callMakePptVoice2VideoPythonInterface(request);

        now = LocalDateTime.now();
        formattedDateTime = now.format(formatter);
        log.info("裁剪视频结束时间=="+formattedDateTime+"=======");

    }

    /** 安全解析 int：null / "null" / 空串 / 非数字均返回 0，避免 NumberFormatException。 */
    private int parseIntSafe(Object value) {
        if (value == null) {
            return 0;
        }
        String s = value.toString().trim();
        if (s.isEmpty() || "null".equals(s)) {
            return 0;
        }
        try {
            return Integer.parseInt(s);
        } catch (NumberFormatException e) {
            return 0;
        }
    }

    //用参考视频加给每段音频配音调用musetalk
    public void voiceAddHuman(AiDhHumanVideoDO aiDhHumanVideoDO,String batchNum,String modelId,String modelNum) {
        DateTimeFormatter formatter = DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss");
        LocalDateTime now = LocalDateTime.now();
        String formattedDateTime = now.format(formatter);
        log.info("调用musetalk开始时间=="+formattedDateTime+"=======");

        String videoId = aiDhHumanVideoDO.getId();
        Map<String,Object> params = new HashMap<>();
        params.put("modelType",TXT_2_VIDEO_TYPE);
        params.put("batchNum",batchNum);
        params.put("modelId",modelId);
        params.put("videoId",videoId);

        //获取语音-视频信息
        List<Map> voiceAndSplitHumanList = commonMapper.getVideoExecLog(params);


        Map<String,Object> data = voiceAndSplitHumanList.get(Integer.parseInt(modelNum));
        //声音存放路径
        String pptVoiceUrl = data.get("ppt_voice_url")+"";

        String pptBatchPptIdPptNum = batchNum+"_"+modelId+"_"+modelNum + "_"+videoId;
        //声音加视频 配音后存储的位置
        String output_voicehuman_url = outputVoicehumanUrl;

        Map<String,Object> request = new HashMap<>();
        request.put("batch_pptid_pagenum",pptBatchPptIdPptNum);
        String museTalkdownLocalPathTmp = museTalkdownLocalPath;
        request.put("downLocalPath",museTalkdownLocalPathTmp.replace("pptBatchPptIdPptNum",pptBatchPptIdPptNum));
        String ppt_voice_split_human_url = data.get("ppt_voice_split_human_url")+"";
        Path path = Paths.get(ppt_voice_split_human_url);
        Path parentPath = path.getParent();
        String splitfileName = path.getFileName().toString();
        String parentPathStr = parentPath.toString();
        if(!parentPathStr.endsWith("/")){
            parentPathStr =  parentPathStr.toString() + "/";
        }
        parentPathStr = parentPathStr.replace("\\","/");
        request.put("human_generate_url",parentPathStr);
        request.put("human_video_name",splitfileName);


        request.put("ppt_voice_url",pptVoiceUrl);

        request.put("result_dir","video/"+videoId+"/"+batchNum+"/lipsync/");
        request.put("output_vid_name",modelNum+".mp4");
        //request.put("output_voicehuman_url",output_voicehuman_url);


        callVoiceAddHumanPythonInterface(request);

        now = LocalDateTime.now();
        formattedDateTime = now.format(formatter);
        log.info("调用musetalk结束时间=="+formattedDateTime+"=======");

    }


    //videoAddImage,,给每段配音的视频进行替换背景,人物位置
    public void videoAddImage(AiDhHumanVideoDO aiDhHumanVideoDO,String batchNum,String modelId,String modelNum){
        DateTimeFormatter formatter = DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss");
        LocalDateTime now = LocalDateTime.now();
        String formattedDateTime = now.format(formatter);
        log.info("替换背景,人物位置开始时间=="+formattedDateTime+"=======");


        String videoId = aiDhHumanVideoDO.getId();
        Map<String,Object> params = new HashMap<>();
        params.put("mainPptId",modelId);

        //取到ppt对应图片
        List<Map> pptRecordDetailList = commonMapper.getPptRecordDetail(params);

        //取到配音之后的每段视频
        params.put("modelType",VOICE_ADD_HUMAN_TYPE);
        params.put("batchNum",batchNum);
        params.put("modelId",modelId);
        params.put("videoId",videoId);
        List<Map> videoExecLogList = commonMapper.getVideoExecLog(params);



        Map<String,Object> imageData = pptRecordDetailList.get(Integer.parseInt(modelNum));
        Map<String,Object> voiceAddHumanData = videoExecLogList.get(Integer.parseInt(modelNum));

        // 配音之后的每段视频
        String voice_human_url = voiceAddHumanData.get("ppt_voice_human_url")+"";
        String ppt_image_url = imageData.get("ppt_image_url")+"";

        String pptBatchPptIdPptNum = batchNum+"_"+modelId+"_"+modelNum+"_"+videoId;

        Map<String,Object> request = new HashMap<>();

        request.put("video_path",voice_human_url);
        request.put("voiceId",pptBatchPptIdPptNum);
        request.put("ppt_background_path",ppt_image_url);
        request.put("position",aiDhHumanVideoDO.getCharacterPosition());

        String videoAddImagePathStr = videoAddImagePath;
        videoAddImagePathStr = videoAddImagePathStr.replace("pptBatchPptIdPptNum",pptBatchPptIdPptNum);
        request.put("out_path",videoAddImagePathStr+pptBatchPptIdPptNum+".mp4");

        callVideoAddImagePythonInterface(request);

        now = LocalDateTime.now();
        formattedDateTime = now.format(formatter);
        log.info("替换背景,人物位置结束时间=="+formattedDateTime+"=======");

    }


    //videoAddImageLayerTrack,进行图层的叠加处理
    public void videoAddImageLayerTrack(AiDhHumanVideoDO aiDhHumanVideoDO,String batchNum,String modelId,String modelNum){
        DateTimeFormatter formatter = DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss");
        LocalDateTime now = LocalDateTime.now();
        String formattedDateTime = now.format(formatter);
        log.info("图层处理位置开始时间=="+formattedDateTime+"=======");
        String videoId = aiDhHumanVideoDO.getId();
        QueryWrapper<AiDhVideoLayerTrackDO> queryWrapper = new QueryWrapper<>();
        queryWrapper.eq("scene_id", videoId);
        queryWrapper.orderByAsc("layer_order");

        List<Map<String,Object>> paramList = new ArrayList<>();
        Map<String,Object> paramData = new HashMap<>();
        Map<String,Object> params = new HashMap<>();
        List<AiDhVideoLayerTrackDO> aiDhVideoLayerTrackList = aiDhVideoLayerTrackMapper.selectList(queryWrapper);

        int voiceLength = 0 ;
        String ppt_background_path = "" ;
        for (AiDhVideoLayerTrackDO aiDhVideoLayerTrackDO : aiDhVideoLayerTrackList){
            paramData = new HashMap<>();
            String layerType = aiDhVideoLayerTrackDO.getLayerType();
            if("1".equals(layerType)){ // 数字人

                //取配音后的值
                params.put("modelType",VOICE_ADD_HUMAN_TYPE);
                params.put("batchNum",batchNum);
                params.put("modelId",modelId);
                params.put("videoId",videoId);
                params.put("modelNum",modelNum);
                List<Map> videoExecLogList = commonMapper.getVideoExecLog(params);
                Map<String,Object> videoHumanOne = videoExecLogList.get(0);
                String voice_human_url = videoHumanOne.get("ppt_voice_human_url")+"";
                //minio 视频 地址用minio地址
                paramData.put("layer_path",voice_human_url);

                //取每段音频长度
                params.put("modelType",TXT_2_VIDEO_TYPE);
                params.put("batchNum",batchNum);
                params.put("modelId",modelId);
                params.put("videoId",videoId);
                params.put("modelNum",modelNum);
                videoExecLogList = commonMapper.getVideoExecLog(params);
                Map<String,Object> videoVoiceOne = videoExecLogList.get(0);
                String length = videoVoiceOne.get("ppt_voice_length")+"";

                voiceLength = Integer.parseInt(length) ;

                //设置图层参数
                getLayerDetailParams(paramData,aiDhVideoLayerTrackDO);
                paramList.add(paramData);

            }else if ("2".equals(layerType)){ // 前景-装饰
                String materialId = aiDhVideoLayerTrackDO.getLayerTypeId();
                TbAiDhVideoMaterial tbAiDhVideoMaterial = tbAiDhVideoMaterialMapper.selectById(materialId);
                String materialUrl = tbAiDhVideoMaterial.getMaterialUrl();
                materialUrl = MinioClientService.toObjectKey(materialUrl);

                paramData.put("layer_path",materialUrl);

                //设置图层参数
                getLayerDetailParams(paramData,aiDhVideoLayerTrackDO);
                paramList.add(paramData);


            }else if ("3".equals(layerType)){ // PPT-图片

                String copywriteId = aiDhVideoLayerTrackDO.getLayerTypeId();
                CopywritingReqVO copywritingReqVO = new CopywritingReqVO();
                copywritingReqVO.setId(copywriteId);
                CopywritingRespVO copywritingRespVO = copywritingManagementService.copywritingGetOne(copywritingReqVO);
                String mainPptId = copywritingRespVO.getMainPptId();
                params.put("mainPptId",mainPptId);
                //取到ppt对应图片
                List<Map> pptRecordDetailList = commonMapper.getPptRecordDetail(params);
                Map<String,Object> imageData = pptRecordDetailList.get(Integer.parseInt(modelNum));

                String ppt_image_url =  imageData.get("ppt_image_url")+"";
                ppt_image_url = MinioClientService.toObjectKey(ppt_image_url);
                //minio 视频 地址用minio地址
                paramData.put("layer_path",ppt_image_url);

                //设置图层参数
                getLayerDetailParams(paramData,aiDhVideoLayerTrackDO);
                paramList.add(paramData);

            }else if ("4".equals(layerType)){ // 对话Agent

            }else if ("5".equals(layerType)){ // 互动画布容器

            }else if ("6".equals(layerType)){ // 背景图片--单独取出来放到外面
                String bgId = aiDhVideoLayerTrackDO.getLayerTypeId();
                TbAiDhVideoBackground tbAiDhVideoBackground = tbAiDhVideoBackgroundMapper.selectById(bgId);

                String bgUrl = tbAiDhVideoBackground.getBgUrl();
                ppt_background_path = MinioClientService.toObjectKey(bgUrl);

                //minio 视频 地址用minio地址
                //paramData.put("layer_path",tbAiDhVideoBackground.getBgUrl());
                //设置图层参数
                //getLayerDetailParams(paramData,aiDhVideoLayerTrackDO);
                //paramList.add(paramData);
            } else if ("7".equals(layerType)) { //背景是动态视频的

                String bgId = aiDhVideoLayerTrackDO.getLayerTypeId();
                TbAiDhVideoBackground tbAiDhVideoBackground = tbAiDhVideoBackgroundMapper.selectById(bgId);

                String bgUrl = tbAiDhVideoBackground.getBgUrl();
                String background_path = MinioClientService.toObjectKey(bgUrl);

                //minio 视频 地址用minio地址
                paramData.put("layer_path",background_path);
                //设置图层参数
                getLayerDetailParams(paramData,aiDhVideoLayerTrackDO);

                //背景是动态视频的改成 layerName
                paramData.put("layerName","bg");
                paramData.put("boxw","1920");
                paramData.put("boxh","1080");


                paramList.add(paramData);
            }
        }
        String pptBatchPptIdPptNum = batchNum+"_"+modelId+"_"+modelNum+"_"+videoId;

        Map<String,Object> request = new HashMap<>();
        request.put("voiceId",pptBatchPptIdPptNum);
        request.put("videoOutPath","video/"+videoId+"/"+batchNum+"/composite/"+modelNum+".mp4");
        request.put("layerList",paramList);
        request.put("voiceLength",voiceLength);
        request.put("canvas_width","");
        request.put("canvas_height","");
        request.put("ppt_background_path",ppt_background_path.equals("")?defaultImageUrl:ppt_background_path);
        log.info("request"+request);

        //更新状态为执行中（需在调用 Python 前设置，否则同步调用期间的回调会把状态置为成功，随后又被这里覆盖成执行中）
        Map<String,Object> paUpdate = new HashMap<>();
        paUpdate.put("modelType",VIDEO_ADD_IMAGE_TYPE);
        paUpdate.put("batchNum",batchNum);
        paUpdate.put("modelId",modelId);
        paUpdate.put("modelNum",modelNum);
        paUpdate.put("videoId",videoId);
        paUpdate.put("execStatus","3");
        commonMapper.updateVideoExecLog(paUpdate);

        callLayerVideoPythonInterface(request);

        now = LocalDateTime.now();
        formattedDateTime = now.format(formatter);
        log.info("图层处理结束时间=="+formattedDateTime+"=======");

    }


    //将小视频合并成大视频
    public void videoMerge(AiDhHumanVideoDO aiDhHumanVideoDO,String batchNum,String modelId){

        DateTimeFormatter formatter = DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss");
        LocalDateTime now = LocalDateTime.now();
        String formattedDateTime = now.format(formatter);
        log.info("将小视频合并成大视频开始时间=="+formattedDateTime+"=======");

        String videoId = aiDhHumanVideoDO.getId();

        Map<String,Object> params = new HashMap<>();
        //取到背景替换之后的每段视频
        params.put("modelType",VIDEO_ADD_IMAGE_TYPE);
        params.put("batchNum",batchNum);
        //params.put("modelId",videoId);
        params.put("videoId",videoId);
        List<Map> videoExecLogList = commonMapper.getVideoExecLog(params);


        Map<String,Object> request = new HashMap<>();
        String batch_video_id = batchNum + "_" + videoId;
        request.put("batch_video_id",batch_video_id);

        String mergeDirectoryStr = mergeDirectory;
        mergeDirectoryStr = mergeDirectoryStr.replace("batchVideoId",batch_video_id);
        request.put("mergedirectory",mergeDirectoryStr);
        String mergeFileName = batch_video_id+".mp4";

        String mergeVoiceDirectoryStr = mergeVoiceDirectory;
        mergeVoiceDirectoryStr = mergeVoiceDirectoryStr.replace("batchVideoId",batch_video_id);
        request.put("mergevoicedirectory",mergeVoiceDirectoryStr);
        String mergevoiceName = batch_video_id+".wav";

        String mergefile = mergeDirectoryStr + mergeFileName;
        request.put("mergefile",mergefile);

        String mergevoice = mergeVoiceDirectoryStr + mergevoiceName;
        request.put("mergevoice",mergevoice);

        String mergeOutputFile = "video/"+videoId+"/"+batchNum+"/merged.mp4";
        request.put("output_file",mergeOutputFile);


        List<String> videoPaths = new ArrayList<>();
        for(int i=0;i<videoExecLogList.size();i++) {
            Map<String, Object> data = videoExecLogList.get(i);
            // 配音之后的每段视频
            String ppt_video_image_url = data.get("ppt_video_image_url") + "";
            videoPaths.add(ppt_video_image_url);
        }
        request.put("videoPaths",videoPaths);

        //将每段声音拿过来
        params.put("modelType",TXT_2_VIDEO_TYPE);
        params.put("batchNum",batchNum);
        //params.put("modelId",modelId);
        params.put("videoId",videoId);
        videoExecLogList = commonMapper.getVideoExecLog(params);
        List<String> voicePaths = new ArrayList<>();
        for(int i=0;i<videoExecLogList.size();i++) {
            Map<String, Object> data = videoExecLogList.get(i);
            // 配音之后的每段视频
            String voiceUrl = data.get("ppt_voice_url") + "";
            voicePaths.add(voiceUrl);
        }
        request.put("voicePaths",voicePaths);
        //调用合并接口
        callMergeVideoPythonInterface(request);

        now = LocalDateTime.now();
        formattedDateTime = now.format(formatter);
        log.info("将小视频合并成大视频结束时间=="+formattedDateTime+"=======");

    }

    //视频的分辨率和画面比例接口
    public void videoResolutionAspect(AiDhHumanVideoDO aiDhHumanVideoDO,String batchNum,String modelId,String execOrder){

        DateTimeFormatter formatter = DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss");
        LocalDateTime now = LocalDateTime.now();
        String formattedDateTime = now.format(formatter);
        log.info("分辨率和画面比例接口开始时间=="+formattedDateTime+"=======");

        Map<String,Object> params = new HashMap<>();
        String id = aiDhHumanVideoDO.getId();


        //取到上一步的 tmp_final_video_url
        // params.put("modelType",MERGE_VIDEO_TYPE);
        //params.put("modelId",modelId);

        params.put("batchNum",batchNum);
        params.put("videoId",id);
        params.put("execOrder",(Integer.parseInt(execOrder) - 1)+"");
        List<Map> videoExecLogList = commonMapper.getVideoExecLog(params);

        String isAdjust = aiDhHumanVideoDO.getIsAdjust(); // 0:不调整 1：调整
        if("1".equals(isAdjust)){
            Map<String,Object> request = new HashMap<>();

            String batch_video_id = batchNum + "_" + id;
            request.put("voiceId",batch_video_id);
            request.put("videoPath",videoExecLogList.get(0).get("tmp_final_video_url")+"");

            String resolutionAndAspectOutputPathStr = resolutionAndAspectOutputPath;
            resolutionAndAspectOutputPathStr = resolutionAndAspectOutputPathStr.replace("batchVideoId",batch_video_id);
            request.put("videoOutPath",resolutionAndAspectOutputPathStr+batch_video_id+".mp4");
            request.put("resolution",aiDhHumanVideoDO.getResolutionRatio());
            request.put("aspectRatio",aiDhHumanVideoDO.getAspectRatio());

            //调用调整画面及分辨率接口
            callResolutionAndAspectPythonInterface(request);

        }else{ // 更新 merge_video_adjust_url 字段信息,将adjust字段更新

            params.put("batchNum",batchNum);
            params.put("modelId",id);
            params.put("videoId",id);
            params.put("modelNum",null);
            params.put("modelType",RESOLUTION_ASPECT_TYPE);
            params.put("execStatus","1");
            params.put("adjustVideoOutPutDir",videoExecLogList.get(0).get("tmp_final_video_url")+"");
            commonMapper.updateVideoExecLog(params);

            //执行下一步流程
            Thread thread = new Thread() {
                @Override
                public void run() {
                    makePptVideoStart(batchNum,id,id);
                }
            };
            thread.start();
        }
        now = LocalDateTime.now();
        formattedDateTime = now.format(formatter);
        log.info("分辨率和画面比例接口结束时间=="+formattedDateTime+"=======");

    }

    //视频增加字幕接口
    public void videoAddCaptions(AiDhHumanVideoDO aiDhHumanVideoDO,String batchNum,String mainPptId,String execOrder){

        DateTimeFormatter formatter = DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss");
        LocalDateTime now = LocalDateTime.now();
        String formattedDateTime = now.format(formatter);
        log.info("视频增加字幕接口开始时间=="+formattedDateTime+"=======");

        Map<String,Object> params = new HashMap<>();
        String id = aiDhHumanVideoDO.getId();
        params.put("videoId",id);
        params.put("batchNum",batchNum);
        params.put("execOrder",(Integer.parseInt(execOrder) - 2)+"");
        List<Map> videoExecLogList = commonMapper.getVideoExecLog(params);
        Map<String,Object> videoDetail = commonMapper.getAiDhHumanVideoDetail(params);


        String copywriteId = aiDhHumanVideoDO.getCopywriteId();
        CopywritingReqVO copywritingReqVO = new CopywritingReqVO();
        copywritingReqVO.setId(copywriteId);
        CopywritingRespVO copywritingRespVO = copywritingManagementService.copywritingGetOne(copywritingReqVO);
        mainPptId = copywritingRespVO.getMainPptId();
        videoDetail.put("mainPptId",mainPptId);

        List<Map> pptRecordDetail = commonMapper.getPptRecordDetail(videoDetail);

        // 取每段语音时长，用于字幕时间轴
        Map<String,Object> voiceParams = new HashMap<>();
        voiceParams.put("modelType",TXT_2_VIDEO_TYPE);
        voiceParams.put("batchNum",batchNum);
        voiceParams.put("videoId",id);
        List<Map> voiceExecLogList = commonMapper.getVideoExecLog(voiceParams);

        // 按页构建字幕时间轴
        List<Map<String,Object>> captions = new ArrayList<>();
        long cumulativeMs = 0;
        for (int i = 0; i < pptRecordDetail.size(); i++) {
            Map data = pptRecordDetail.get(i);
            String text = data.get("ppt_image_words")+"";
            long lengthMs = 0;
            if (i < voiceExecLogList.size()) {
                try {
                    lengthMs = Long.parseLong(voiceExecLogList.get(i).get("ppt_voice_length")+"");
                } catch (Exception e) {
                    lengthMs = 0;
                }
            }
            Map<String,Object> cap = new HashMap<>();
            cap.put("text",text);
            cap.put("start",cumulativeMs);
            cap.put("end",cumulativeMs + lengthMs);
            captions.add(cap);
            cumulativeMs += lengthMs;
        }

        Map<String,Object> request = new HashMap<>();
        String batch_video_id = batchNum + "_" + id;
        request.put("videoId",batch_video_id);
        request.put("minioVideoPath",videoDetail.get("video_url"));

        params.put("paramKey","fontSize");
        List<Map> commonParamData = commonMapper.getCommonParam(params);
        request.put("fontSize",Integer.parseInt(commonParamData.get(0).get("paramValue")+""));
        params.put("paramKey","fontColor");
        commonParamData = commonMapper.getCommonParam(params);
        request.put("fontColor",commonParamData.get(0).get("paramValue")+"");
        request.put("captions",captions);

        //增加字幕
        callCaptionsPythonInterface(request);


        now = LocalDateTime.now();
        formattedDateTime = now.format(formatter);
        log.info("视频增加字幕接口结束时间=="+formattedDateTime+"=======");


        //执行下一步流程
        Thread thread = new Thread() {
            @Override
            public void run() {
                makePptVideoStart(batchNum,id,id);
            }
        };
        thread.start();
        log.info("视频增加字幕接口结束时间=="+formattedDateTime+"=======");

    }

    //获取视频第一帧图片的接口
    public void makeVideoFrame(AiDhHumanVideoDO aiDhHumanVideoDO,String batchNum,String mainPptId){
        DateTimeFormatter formatter = DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss");
        LocalDateTime now = LocalDateTime.now();
        String formattedDateTime = now.format(formatter);
        log.info("获取视频第一帧图片的接口开始时间=="+formattedDateTime+"=======");

        Map<String,Object> params = new HashMap<>();
        String id = aiDhHumanVideoDO.getId();
        params.put("videoId",id);
        Map<String,Object> videoDetail = commonMapper.getAiDhHumanVideoDetail(params);

        Map<String,Object> request = new HashMap<>();


        String batch_video_id = batchNum + "_" + id;
        request.put("voiceId",batch_video_id);
        String videoFinalName = videoDetail.get("video_final_name")+"";
        request.put("videoPath",videoDetail.get("video_url"));


        //获取视频第一帧图片
        callFirstFramePythonInterface(request);


        now = LocalDateTime.now();
        formattedDateTime = now.format(formatter);
        log.info("获取视频第一帧图片的结束开始时间=="+formattedDateTime+"=======");


        //执行下一步流程
        Thread thread = new Thread() {
            @Override
            public void run() {
                makePptVideoStart(batchNum,id,id);
            }
        };
        thread.start();

    }

    //获取图层明细参数
    private Map<String,Object> getLayerDetailParams(Map<String,Object> paramData,AiDhVideoLayerTrackDO aiDhVideoLayerTrackDO){
        paramData.put("layerLock",aiDhVideoLayerTrackDO.getLayerLock());
        paramData.put("layerTypeId",aiDhVideoLayerTrackDO.getLayerTypeId());
        paramData.put("layerName",aiDhVideoLayerTrackDO.getLayerName());
        paramData.put("width",aiDhVideoLayerTrackDO.getWidth());
        paramData.put("height",aiDhVideoLayerTrackDO.getHeight());
        paramData.put("boxx",aiDhVideoLayerTrackDO.getBoxx());
        paramData.put("boxy",aiDhVideoLayerTrackDO.getBoxy());
        paramData.put("boxw",aiDhVideoLayerTrackDO.getBoxw());
        paramData.put("boxh",aiDhVideoLayerTrackDO.getBoxh());
        paramData.put("isShow",aiDhVideoLayerTrackDO.getIsShow());
        paramData.put("transparency",aiDhVideoLayerTrackDO.getTransparency());
        paramData.put("layerType",aiDhVideoLayerTrackDO.getLayerType());
        paramData.put("layerOrder",aiDhVideoLayerTrackDO.getLayerOrder());
        return paramData;
    }

    //视频制作状态更新的接口
    @Override
    public void updateMakeVideoStatus (String videoId,String videoStatus){
        Map<String,Object> params = new HashMap<>();
        params.put("id",videoId);
        params.put("videoStatus",videoStatus);
        commonMapper.updateMakeVideoStatus(params);
    }



    //给参考视频配音的接口
    private void callVoiceAddHumanPythonInterface(Map<String, Object> request) {
        log.info(request.toString());
        log.info(voiceAddHUmanInterFaceUrl);
        Map<String, String> headers = new HashMap<>();
        headers.put("Content-Type", "application/json;charset=UTF-8");  //传参格式
        headers.put("Accept", "application/json");
        try {
            AbilityShareClient.doPostPPt(voiceAddHUmanInterFaceUrl, headers, JSONObject.toJSONString(request));
        } catch (Exception e) {
            log.error("voiceAddHUmanInterFaceUrl:{}",e);
        }
    }

    //给视频替换背景的接口
    private void callVideoAddImagePythonInterface(Map<String, Object> request) {
        log.info(request.toString());
        log.info(videoAddImageInterFaceUrl);
        Map<String, String> headers = new HashMap<>();
        headers.put("Content-Type", "application/json;charset=UTF-8");  //传参格式
        headers.put("Accept", "application/json");
        try {
            AbilityShareClient.doPostPPt(videoAddImageInterFaceUrl, headers, JSONObject.toJSONString(request));
        } catch (Exception e) {
            log.error("voiceAddHUmanInterFaceUrl:{}",e);
        }
    }


    //图层视频加工的接口
    private void callLayerVideoPythonInterface(Map<String, Object> request) {
        log.info(request.toString());
        log.info(videoAddImageInterFaceUrl);
        Map<String, String> headers = new HashMap<>();
        headers.put("Content-Type", "application/json;charset=UTF-8");  //传参格式
        headers.put("Accept", "application/json");
        log.info(JSONObject.toJSONString(request));
        log.info(videoAddImageInterFaceUrl);
        try {
            AbilityShareClient.doPostPPt(videoAddImageInterFaceUrl, headers, JSONObject.toJSONString(request));
        } catch (Exception e) {
            log.error("voiceAddHUmanInterFaceUrl:{}",e);
        }
    }

    //调用视频合并接口
    private void callMergeVideoPythonInterface(Map<String, Object> request) {
        log.info(mergeVideoInterFaceUrl);
        log.info(request.toString());
        Map<String, String> headers = new HashMap<>();
        headers.put("Content-Type", "application/json;charset=UTF-8");  //传参格式
        headers.put("Accept", "application/json");
        try {
            AbilityShareClient.doPostPPt(mergeVideoInterFaceUrl, headers, JSONObject.toJSONString(request));
        } catch (Exception e) {
            log.error("mergeVideoInterFaceUrl:{}",e);
        }
    }

    //调用视频的分辨率和画面比例接口
    private void callResolutionAndAspectPythonInterface(Map<String, Object> request) {
        Map<String, String> headers = new HashMap<>();
        headers.put("Content-Type", "application/json;charset=UTF-8");  //传参格式
        headers.put("Accept", "application/json");
        try {
            AbilityShareClient.doPostPPt(resolutionAndAspectInterfaceUrl, headers, JSONObject.toJSONString(request));
        } catch (Exception e) {
            log.error("mergeVideoInterFaceUrl:{}",e);
        }
    }


    //调用视频增加字幕接口
    private void callCaptionsPythonInterface(Map<String, Object> request) {
        log.info(captionsInterfaceUrl);
        log.info(request.toString());

        Map<String, String> headers = new HashMap<>();
        headers.put("Content-Type", "application/json;charset=UTF-8");  //传参格式
        headers.put("Accept", "application/json");
        try {
            AbilityShareClient.doPostPPt(captionsInterfaceUrl, headers, JSONObject.toJSONString(request));
        } catch (Exception e) {
            log.error("callCaptionsPythonInterface:{}",e);
        }
    }

    //获取视频第一帧图片接口
    private void callFirstFramePythonInterface(Map<String, Object> request) {
        Map<String, String> headers = new HashMap<>();
        log.info(JSONObject.toJSONString(request));
        headers.put("Content-Type", "application/json;charset=UTF-8");  //传参格式
        headers.put("Accept", "application/json");
        try {
            AbilityShareClient.doPostPPt(firstFrameInterfaceUrl, headers, JSONObject.toJSONString(request));
        } catch (Exception e) {
            log.error("firstFrameInterfaceUrlStr:{}",e);
        }
    }

    //将参考视频拼接再设置成每段语音长度的接口
    private void callMakePptVoice2VideoPythonInterface(Map<String, Object> request) {
        Map<String, String> headers = new HashMap<>();
        headers.put("Content-Type", "application/json;charset=UTF-8");  //传参格式
        headers.put("Accept", "application/json");
        log.info(JSONObject.toJSONString(request));
        log.info(splitHumanInterfaceUrl);
        try {
            AbilityShareClient.doPostPPt(splitHumanInterfaceUrl, headers, JSONObject.toJSONString(request));
        } catch (Exception e) {
            log.error("firstFrameInterfaceUrlStr:{}",e);
        }
    }

    /**
     * 删除
     * @param params
     * @return
     */
    @Override
    public int deleteAiDhHumanVideo(Map<String, Object> params) {
        String id = params.get("id")+"";
        // 删除 MinIO 文件（成片、首帧、各中间产物）
        deleteVideoMinioFiles(id);
        // 删除图层明细
        QueryWrapper<AiDhVideoLayerTrackDO> layerQueryWrapper = new QueryWrapper<>();
        layerQueryWrapper.eq("scene_id", id);
        aiDhVideoLayerTrackMapper.delete(layerQueryWrapper);
        // 删除执行日志
        Map<String,Object> execParams = new HashMap<>();
        execParams.put("videoId", id);
        commonMapper.deleteVideoExecLog(execParams);
        // 删除主表
        return aiDhHumanVideoMapper.deleteById(id);
    }

    private void deleteVideoMinioFiles(String id) {
        Set<String> keys = new HashSet<>();
        try {
            AiDhHumanVideoDO video = aiDhHumanVideoMapper.selectById(id);
            if (video != null) {
                addMinioKey(keys, video.getVideoUrl());
                addMinioKey(keys, video.getFirstFrame());
            }
            Map<String,Object> params = new HashMap<>();
            params.put("videoId", id);
            List<Map> execLogs = commonMapper.getVideoExecLog(params);
            for (Map log : execLogs) {
                addMinioKey(keys, log.get("ppt_voice_url")+"");
                addMinioKey(keys, log.get("ppt_voice_split_human_url")+"");
                addMinioKey(keys, log.get("ppt_voice_human_url")+"");
                addMinioKey(keys, log.get("ppt_video_image_url")+"");
                addMinioKey(keys, log.get("merge_video_url")+"");
                addMinioKey(keys, log.get("merge_video_adjust_url")+"");
                addMinioKey(keys, log.get("merge_video_addcaptions_url")+"");
                addMinioKey(keys, log.get("first_frame_url")+"");
                addMinioKey(keys, log.get("tmp_final_video_url")+"");
            }
            minioClientService.initMinioClient();
            for (String key : keys) {
                String objectKey = MinioClientService.toObjectKey(key);
                if (StringUtils.isBlank(objectKey)) {
                    continue;
                }
                try {
                    minioClientService.minioDelete(objectKey);
                    log.info("删除视频 MinIO 文件成功: {}", objectKey);
                } catch (Exception e) {
                    log.warn("删除视频 MinIO 文件失败: {}, 原因: {}", objectKey, e.getMessage());
                }
            }
        } catch (Exception e) {
            log.error("删除视频 MinIO 文件失败, videoId: {}", id, e);
        }
    }

    private void addMinioKey(Set<String> keys, String key) {
        if (StringUtils.isNotBlank(key) && !"null".equals(key)) {
            keys.add(key);
        }
    }

    /**
     * 分页查询
     * @param
     * @return
     */
    @Override
    public Map<String,Object> getAiDhHumanVideoPage(AiDhHumanVideoReqVO reqVO) {

        Page<AiDhHumanVideoDO> pageParm = new Page<>(reqVO.getPageNum(), reqVO.getPageSize());
        QueryWrapper<AiDhHumanVideoDO> queryWrapper=new QueryWrapper<>();
        String videoSave = reqVO.getVideoSave();
        if(null != videoSave && !"".equals(videoSave)){
            queryWrapper.eq("video_save", videoSave);
        }
        queryWrapper.orderByDesc("opr_time");
        aiDhHumanVideoMapper.selectPage(pageParm, queryWrapper);

        HashMap res=new HashMap();

        List<AiDhHumanVideoDO> aiDhHumanVideoPageResult = pageParm.getRecords();

        List<AiDhHumanVideoRespVO> retList =aiDhHumanVideoPageResult.stream().map(aiDhHumanVideoDO -> {
            AiDhHumanVideoRespVO  retVO = new AiDhHumanVideoRespVO();
            retVO.setId(aiDhHumanVideoDO.getId());
            retVO.setVideoName(aiDhHumanVideoDO.getVideoName());
            retVO.setVideoStatus(aiDhHumanVideoDO.getVideoStatus());
            retVO.setVideoSave(aiDhHumanVideoDO.getVideoSave());
            retVO.setOprTime(aiDhHumanVideoDO.getOprTime());
            //一个一个从ftp下载并且转换成base64
            //retVO.setFirstFrame(downLoadImage(sftpConfigDOData,aiDhHumanVideoDO));
            //返回minio的路径
            String minioPath = minioEndpoint+"/"+minioBucket+"/";
            retVO.setFirstFrame(minioPath+aiDhHumanVideoDO.getFirstFrame());
            //返回视频路径
            retVO.setVideoUrl(minioPath+aiDhHumanVideoDO.getVideoUrl());
            return retVO;
        }).collect(Collectors.toList());
        res.put("data",retList);
        res.put("total",pageParm.getTotal());

        return res;
    }

    /**
     * 查询单个详情
     * @param id
     * @return
     */
    @Override
    public AiDhHumanVideoSaveVO selectById(String id) {
        AiDhHumanVideoSaveVO reqVO = new AiDhHumanVideoSaveVO();
        AiDhHumanVideoDO data = (AiDhHumanVideoDO)aiDhHumanVideoMapper.selectById(id);
        if (data == null) {
            throw new RuntimeException("视频不存在，id: " + id);
        }
        BeanUtils.copyProperties(data, reqVO);
        //图层明细查询
        QueryWrapper<AiDhVideoLayerTrackDO> queryWrapper = new QueryWrapper<>();
        queryWrapper.eq("scene_id", id);
        queryWrapper.orderByAsc("layer_order");
        AiDhVideoLayerTrackRspDO aiDhVideoLayerTrackRspDO = new AiDhVideoLayerTrackRspDO();
        List<AiDhVideoLayerTrackRspDO> aiDhVideoLayerTracksRsp = new ArrayList<>();
        List<AiDhVideoLayerTrackDO> aiDhVideoLayerTracks = aiDhVideoLayerTrackMapper.selectList(queryWrapper);
        for (AiDhVideoLayerTrackDO aiDhVideoLayerTrackDO : aiDhVideoLayerTracks){
            aiDhVideoLayerTrackRspDO = new AiDhVideoLayerTrackRspDO();
            String layerType = aiDhVideoLayerTrackDO.getLayerType();
            BeanUtils.copyProperties(aiDhVideoLayerTrackDO, aiDhVideoLayerTrackRspDO);
            if(layerType.equals("1")){ // 数字人的页面
                String minioPath = minioEndpoint+"/"+minioBucket+"/";
                String humanId = data.getHumanId();
                AiDhHumanDO aiDhHumanDO = aiDhHumanMapper.selectById(humanId);
                aiDhVideoLayerTrackRspDO.setSrc(minioPath+aiDhHumanDO.getFirstFrame()+"/"+aiDhHumanDO.getImageName());
            }else if (layerType.equals("2")){ // 前景-装饰
                String materialId = aiDhVideoLayerTrackDO.getLayerTypeId();
                TbAiDhVideoMaterial tbAiDhVideoMaterial = tbAiDhVideoMaterialMapper.selectById(materialId);
                aiDhVideoLayerTrackRspDO.setSrc(tbAiDhVideoMaterial.getMaterialUrl());

            }else if (layerType.equals("3")){ // ppt

                String copywriteId = aiDhVideoLayerTrackDO.getLayerTypeId();
                CopywritingReqVO copywritingReqVO = new CopywritingReqVO();
                copywritingReqVO.setId(copywriteId);
                CopywritingRespVO copywritingRespVO = copywritingManagementService.copywritingGetOne(copywritingReqVO);
                String mainPptId = copywritingRespVO.getMainPptId();
                Map<String,Object> params = new HashMap<>();
                params.put("mainPptId",mainPptId);
                //取到ppt对应图片
                List<Map> pptRecordDetailList = commonMapper.getPptRecordDetail(params);
                Map<String,Object> imageData = pptRecordDetailList.get(0);
                aiDhVideoLayerTrackRspDO.setSrc(imageData.get("ppt_image_url")+"");

            }else if (layerType.equals("4")){ //  对话Agent

            }else if (layerType.equals("5")){ //  互动画布容器

            }else if (layerType.equals("6")){ // 背景图片
                String bgId = aiDhVideoLayerTrackDO.getLayerTypeId();
                TbAiDhVideoBackground tbAiDhVideoBackground = tbAiDhVideoBackgroundMapper.selectById(bgId);
                aiDhVideoLayerTrackRspDO.setSrc(tbAiDhVideoBackground.getBgUrl());
            }else if (layerType.equals("7")){ // 背景视频
                String bgId = aiDhVideoLayerTrackDO.getLayerTypeId();
                TbAiDhVideoBackground tbAiDhVideoBackground = tbAiDhVideoBackgroundMapper.selectById(bgId);
                aiDhVideoLayerTrackRspDO.setSrc(tbAiDhVideoBackground.getBgUrl());
            }
            aiDhVideoLayerTracksRsp.add(aiDhVideoLayerTrackRspDO);
        }
        reqVO.setLayerRspInfo(aiDhVideoLayerTracksRsp);
        return reqVO;
    }


    public int updateAiDhHumanVideo(AiDhHumanVideoSaveVO reqVO){

        LocalDateTime now = LocalDateTime.now();
        // 定义日期时间格式
        DateTimeFormatter formatter = DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss");
        // 使用定义的格式器格式化当前的 LocalDateTime
        String formattedDateTime = now.format(formatter);
        reqVO.setOprTime(formattedDateTime);

        String videoId = reqVO.getId();

        aiDhHumanVideoMapper.update(AiDhHumanVideoAdaptor.updateCovDo.apply(reqVO),new LambdaQueryWrapperX<AiDhHumanVideoDO>()
                .eqIfPresent(AiDhHumanVideoDO::getId,videoId));

//        QueryWrapper<AiDhVideoLayerTrackDO> queryWrapper = new QueryWrapper<>();
//        queryWrapper.eq("scene_id", videoId);
//
//        //删除图层明细
//        aiDhVideoLayerTrackMapper.delete(queryWrapper);
//
//        //插入图层明细
//        List<AiDhVideoLayerTrackDO> layerInfo = reqVO.getLayerInfo();
//        for (AiDhVideoLayerTrackDO layer : layerInfo){
//            layer.setSceneId(videoId);
//            layer.setSceneType("1");
//            layer.setCreator(reqVO.getOprStaff());
//            layer.setCreateTime(new Date());
//            aiDhVideoLayerTrackMapper.insert(layer);
//        }
        return 1;
    }

    @Override
    public int updateRecordVoiceHuman(Map<String, Object> params) {
        return 0;
    }


    @Override
    public int updateRecordVideoImage(Map<String, Object> params) {

        String execStatus = params.get("execStatus")+"";
        String videoType = params.get("video_type")+"";

        //配音对嘴型的回调
        if("1".equals(videoType)) {
            String batchPptidPagenum = params.get("batchPptidPagenum")+"";
            String[] s = batchPptidPagenum.split("_");
            if("1".equals(execStatus)){ // 成功
                params.put("batchNum",s[0]);
                params.put("modelId",s[1]);
                params.put("videoId",s[3]);
                params.put("modelNum",s[2]);
                params.put("modelType",VOICE_ADD_HUMAN_TYPE);
                params.put("execStatus",execStatus);
                params.put("pptVoiceHumanUrl",params.get("voiceAddVideoOutPutDir")+"");
                commonMapper.updateVideoExecLog(params);
                //执行下一步
                Thread thread = new Thread() {
                    @Override
                    public void run() {
                        makePptVideoStart(s[3],s[0],s[1]);
                    }
                };
                thread.start();
            }else{ //失败
                params.put("batchNum",s[0]);
                params.put("modelId",s[1]);
                params.put("videoId",s[3]);
                params.put("modelNum",s[2]);
                params.put("modelType",VOICE_ADD_HUMAN_TYPE);
                params.put("execStatus",execStatus);
                //更新执行日志状态
                commonMapper.updateVideoExecLog(params);
                //更新整体流程失败状态
                updateMakeVideoStatus(s[3],ERROR_VIDEO_STATUS);
            }
        } else if("2".equals(videoType)){  //替换背景的回调

            String batchPptidPagenum = params.get("voiceId")+"";
            String[] s = batchPptidPagenum.split("_");
            if("1".equals(execStatus)){ // 成功
                params.put("batchNum",s[0]);
                params.put("modelId",s[1]);
                params.put("videoId",s[3]);
                params.put("modelNum",s[2]);
                params.put("modelType",VIDEO_ADD_IMAGE_TYPE);
                params.put("execStatus",execStatus);
                params.put("videoImageOutPath",params.get("videoOutPath")+"");
                commonMapper.updateVideoExecLog(params);
                //执行下一步
                Thread thread = new Thread() {
                    @Override
                    public void run() {
                        makePptVideoStart(s[3],s[0],s[1]);
                    }
                };
                thread.start();
            }else{ //失败
                params.put("batchNum",s[0]);
                params.put("modelId",s[1]);
                params.put("videoId",s[3]);
                params.put("modelNum",s[2]);
                params.put("modelType",VIDEO_ADD_IMAGE_TYPE);
                params.put("execStatus",execStatus);
                //更新执行日志状态
                commonMapper.updateVideoExecLog(params);
                //更新整体流程失败状态
                updateMakeVideoStatus(s[3],ERROR_VIDEO_STATUS);
            }

        }else if("3".equals(videoType)){  //合并视频之后回调

            String batch_video_id = params.get("batch_video_id")+"";
            String[] s = batch_video_id.split("_");

            if("1".equals(execStatus)){ // 成功
                params.put("batchNum",s[0]);
                params.put("modelId",s[1]);
                params.put("videoId",s[1]);
                params.put("modelNum",null);
                params.put("modelType",MERGE_VIDEO_TYPE);
                params.put("execStatus",execStatus);
                params.put("mergeVideoOutPutDir",params.get("mergeVideoOutPutDir")+"");
                params.put("tmpFinalVideoUrl",params.get("mergeVideoOutPutDir")+"");
                commonMapper.updateVideoExecLog(params);
                //执行下一步
                Thread thread = new Thread() {
                    @Override
                    public void run() {
                        makePptVideoStart(s[1],s[0],s[1]);
                    }
                };
                thread.start();
            }else{ //失败
                params.put("batchNum",s[0]);
                params.put("modelId",s[1]);
                params.put("videoId",s[1]);
                params.put("modelNum",null);
                params.put("modelType",MERGE_VIDEO_TYPE);
                params.put("execStatus",execStatus);
                //更新执行日志状态
                commonMapper.updateVideoExecLog(params);
                //更新整体流程失败状态
                updateMakeVideoStatus(s[1],ERROR_VIDEO_STATUS);
            }

        }else if("4".equals(videoType)){  //视频调整比例及分辨率接口之后回调

            String batch_video_id = params.get("voiceId")+"";
            String[] s = batch_video_id.split("_");

            if("1".equals(execStatus)){ // 成功
                params.put("batchNum",s[0]);
                params.put("modelId",s[1]);
                params.put("videoId",s[1]);
                params.put("modelNum",null);
                params.put("modelType",RESOLUTION_ASPECT_TYPE);
                params.put("execStatus",execStatus);
                params.put("adjustVideoOutPutDir",params.get("videoOutPath")+"");
                params.put("tmpFinalVideoUrl",params.get("videoOutPath")+"");
                commonMapper.updateVideoExecLog(params);
                //执行下一步
                Thread thread = new Thread() {
                    @Override
                    public void run() {
                        makePptVideoStart(s[1],s[0],s[1]);
                    }
                };
                thread.start();
            }else{ //失败
                params.put("batchNum",s[0]);
                params.put("modelId",s[1]);
                params.put("videoId",s[1]);
                params.put("modelNum",null);
                params.put("modelType",RESOLUTION_ASPECT_TYPE);
                params.put("execStatus",execStatus);
                //更新执行日志状态
                commonMapper.updateVideoExecLog(params);
                //更新整体流程失败状态
                updateMakeVideoStatus(s[1],ERROR_VIDEO_STATUS);
            }

        }else if("5".equals(videoType)){  //添加字幕接口之后回调

            String captionsOutPath = params.get("captionsOutPath")+"";
            params.put("captionsOutPath",captionsOutPath);
            String batch_video_id = params.get("batch_video_id")+"";
            String[] s = batch_video_id.split("_");

            if("1".equals(execStatus)){ // 成功

                //成功的话，更新video数据,视频的url
                params.put("videoId",s[1]);
                params.put("videoOutPath",captionsOutPath);
                commonMapper.updateVideoUrl(params);


                params.put("batchNum",s[0]);
                params.put("modelId",s[1]);
                params.put("videoId",s[1]);
                params.put("modelNum",null);
                params.put("modelType",ADD_CAPTIONS_TYPE);
                params.put("execStatus",execStatus);
                params.put("videoAddcaptionsUrl",captionsOutPath);
                params.put("tmpFinalVideoUrl",captionsOutPath);
                commonMapper.updateVideoExecLog(params);
                //执行下一步
                Thread thread = new Thread() {
                    @Override
                    public void run() {
                        makePptVideoStart(s[1],s[0],s[1]);
                    }
                };
                thread.start();
            }else{ //失败
                params.put("batchNum",s[0]);
                params.put("modelId",s[1]);
                params.put("videoId",s[1]);
                params.put("modelNum",null);
                params.put("modelType",FIRST_FRAME_TYPE);
                params.put("execStatus",execStatus);
                //更新执行日志状态
                commonMapper.updateVideoExecLog(params);
                //更新整体流程失败状态
                updateMakeVideoStatus(s[1],ERROR_VIDEO_STATUS);
            }



        }else if("6".equals(videoType)){  //获取第一帧图片之后的回调接口

            String firstFrameOutPathAllPath = params.get("firstFrameOutPath")+"";
            params.put("firstFrame",firstFrameOutPathAllPath);
            String batch_video_id = params.get("batch_video_id")+"";
            String[] s = batch_video_id.split("_");

            if("1".equals(execStatus)){ // 成功
                //成功的话，更新video数据
                params.put("videoId",s[1]);
                commonMapper.updateVideoFirstFrame(params);

                params.put("batchNum",s[0]);
                params.put("modelId",s[1]);
                params.put("videoId",s[1]);
                params.put("modelNum",null);
                params.put("modelType",FIRST_FRAME_TYPE);
                params.put("execStatus",execStatus);
                params.put("firstFrameOutPathAllPath",firstFrameOutPathAllPath);
                commonMapper.updateVideoExecLog(params);
                //执行下一步
                Thread thread = new Thread() {
                    @Override
                    public void run() {
                        makePptVideoStart(s[1],s[0],s[1]);
                    }
                };
                thread.start();
            }else{ //失败
                params.put("batchNum",s[0]);
                params.put("modelId",s[1]);
                params.put("videoId",s[1]);
                params.put("modelNum",null);
                params.put("modelType",FIRST_FRAME_TYPE);
                params.put("execStatus",execStatus);
                //更新执行日志状态
                commonMapper.updateVideoExecLog(params);
                //更新整体流程失败状态
                updateMakeVideoStatus(s[1],ERROR_VIDEO_STATUS);
            }

        }else if("7".equals(videoType)){ //将配音视频裁剪成和音频长度一致之后的回调接口

            String batch_pptid_videoid_list = params.get("batch_pptid_videoid_list")+"";
            batch_pptid_videoid_list = batch_pptid_videoid_list.replace("[","").replace("]","");

            String out_Put_Path_file = params.get("out_Put_Path_file")+"";
            out_Put_Path_file = out_Put_Path_file.replace("[","").replace("]","");

            String batch_pptid_videoid_data[]  = batch_pptid_videoid_list.split(",");
            String out_put_path_file_data[]  =  out_Put_Path_file.split(",");

            if("1".equals(execStatus)){ // 成功
                // pptBatch + '_' + pptId + '_' + videoId + '_'+ str(pptNum)
                for(int i = 0; i < batch_pptid_videoid_data.length; i++){
                    String splitData = batch_pptid_videoid_data[i];
                    String[] s = splitData.split("_");

                    params.put("batchNum",s[0].replace(" ",""));
                    params.put("modelId",s[1].replace(" ",""));
                    params.put("videoId",s[2].replace(" ",""));
                    params.put("modelNum",s[3].replace(" ",""));
                    //视频裁剪合并是一个流程，将每条明细放到语音的加工数据后面
                    params.put("modelType",TXT_2_VIDEO_TYPE);
                    params.put("voicesplithumanPath",out_put_path_file_data[i].replace(" ",""));
                    commonMapper.updateVideoExecLog(params);
                }



                //执行下一步
                String finalBatch_pptid_videoid_list = batch_pptid_videoid_list;
                Thread thread = new Thread() {
                    @Override
                    public void run() {
                        String batch_pptid_videoid_data[]  = finalBatch_pptid_videoid_list.split(",");
                        String splitData = batch_pptid_videoid_data[0];
                        String[] s = splitData.split("_");

                        //更新 split_video_type 状态
                        params.put("batchNum",s[0]);
                        params.put("modelId",s[1]);
                        params.put("videoId",s[2]);
                        params.put("modelNum",null);
                        params.put("modelType",SPLIT_VIDEO_TYPE);
                        params.put("execStatus",execStatus);
                        params.remove("voicesplithumanPath");
                        //更新执行日志状态
                        commonMapper.updateVideoExecLog(params);

                        //执行下一步
                        makePptVideoStart(s[2],s[0],s[1]);
                    }
                };
                thread.start();
            }else{ //失败
                String splitData = batch_pptid_videoid_data[0];
                String[] s = splitData.split("_");
                params.put("batchNum",s[0]);
                params.put("modelId",s[1]);
                params.put("videoId",s[2]);
                params.put("modelNum",null);
                params.put("modelType",SPLIT_VIDEO_TYPE);
                params.put("execStatus",execStatus);
                //更新执行日志状态
                commonMapper.updateVideoExecLog(params);
                //更新整体流程失败状态
                updateMakeVideoStatus(s[3],ERROR_VIDEO_STATUS);
            }
        }
        return 1;
    }

    @Override
    public Map<String,Object> getFirstPptImageByCopywriteId(Map<String, Object> params) {

        CopywritingReqVO copywritingReqVO = new CopywritingReqVO();
        copywritingReqVO.setId(params.get("copywriteId")+"");
        CopywritingRespVO copywritingRespVO = copywritingManagementService.copywritingGetOne(copywritingReqVO);
        String mainPptId = copywritingRespVO.getMainPptId();
        params.put("pptId",mainPptId);

        Map<String,Object> result = new HashMap<>();

        //通过PPTid查询第一张ppt图片
        Map<String, Object> firstPptImage = commonMapper.getFirstPptImage(params);
        if(null != firstPptImage){
            String pptImageUrl = firstPptImage.get("ppt_image_url")+"";
/*            SftpConfigDO sftpConfigDOParam = new SftpConfigDO();
            sftpConfigDOParam.setSftpSystem("0");
            SftpConfigDO sftpConfigDO = sftpConfigService.selectBySftpSystem(sftpConfigDOParam);

            Path path = Paths.get(pptImageUrl);
            Path parentPath = path.getParent();
            String imageName = path.getFileName().toString();
            String parentPathStr = parentPath.toString();
            if(!parentPathStr.endsWith("/")){
                parentPathStr =  parentPathStr.toString() + "/";
            }
            parentPathStr = parentPathStr.replace("\\","/");

            AiDhHumanVideoDO aiDhHumanVideoDO = new AiDhHumanVideoDO();
            aiDhHumanVideoDO.setFirstFrame(parentPathStr);
            aiDhHumanVideoDO.setFrameName(imageName);
            String base64Str = downLoadImage(sftpConfigDO, aiDhHumanVideoDO);
            result.put("imageData",base64Str);*/
            result.put("imageData",pptImageUrl);
        }
        return result;
    }


    private String chekftpConfig(SftpConfigDO sftpConfigDO) {
        if (StringUtils.isBlank(sftpConfigDO.getSftpAccount())) {
            log.error("TbSSftpConfig, 没有配置账号");
            return "没有配置账号";
        }
        if (StringUtils.isBlank(sftpConfigDO.getSftpPassword())) {
            log.error("TbSSftpConfig, 没有配置密码");
            return "没有配置密码";
        }

        if (StringUtils.isBlank(sftpConfigDO.getSftpIp())) {
            log.error("TbSSftpConfig, 没有配置ip");
            return "没有配置ip";
        }

        if (StringUtils.isBlank(sftpConfigDO.getSftpMode())) {
            log.error("TbSSftpConfig, 没有配置模式");
            return "没有配置模式";
        }
        if (StringUtils.isBlank(sftpConfigDO.getSftpUploadPath())) {
            log.error("TbSSftpConfig, 没有配置上传目录");
            return "没有配置上传目录";
        }
        return null;
    }

    private String downLoadImage(SftpConfigDO sftpConfigDO, AiDhHumanVideoDO aiDhHumanVideoDO){
        try {
            minioClientService.initMinioClient();
            String key = MinioClientService.toObjectKey(aiDhHumanVideoDO.getFirstFrame()) + "/" + aiDhHumanVideoDO.getFrameName();
            byte[] bytes = minioClientService.minioDownload(key);
            return Base64.encodeBase64String(bytes);
        } catch (Exception e) {
            throw new RuntimeException(e);
        }
    }

}
