package cn.iocoder.yudao.module.digital.service.impl;

import cn.iocoder.yudao.module.digital.dal.SftpConfigDO;
import cn.iocoder.yudao.module.digital.dal.mysql.CommonMapper;
import cn.iocoder.yudao.module.digital.dal.mysql.TbAiDhVoiceMapper;
import cn.iocoder.yudao.module.digital.entity.TbAiDhVoice;
import cn.iocoder.yudao.module.digital.service.AiDhHumanVideoService;
import cn.iocoder.yudao.module.digital.service.ITbAiDhVoiceService;
import cn.iocoder.yudao.module.digital.service.MinioClientService;
import cn.iocoder.yudao.module.digital.service.PptRecordDetailService;
import cn.iocoder.yudao.module.digital.service.SftpConfigService;
import cn.iocoder.yudao.module.digital.util.AbilityShareClient;
import cn.iocoder.yudao.module.digital.util.EncryptUtil;
import cn.iocoder.yudao.module.digital.util.SFtpOper;
import com.alibaba.fastjson.JSONObject;
import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.baomidou.mybatisplus.core.toolkit.StringUtils;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.jcraft.jsch.ChannelSftp;
import com.jcraft.jsch.SftpATTRS;
import com.jcraft.jsch.SftpException;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

import javax.annotation.Resource;
import java.net.URLEncoder;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.text.SimpleDateFormat;
import java.util.*;

/**
 * <p>
 * 服务实现类
 * </p>
 *
 * @author author
 * @since 2024-07-17
 */
@Service
@Slf4j
public class TbAiDhVoiceServiceImpl implements ITbAiDhVoiceService {
    @Autowired
    private TbAiDhVoiceMapper tbAiDhVoiceMapper;
    @Resource
    private SftpConfigService sftpConfigService;

    @Resource
    private PptRecordDetailService pptRecordDetailService;

    @Resource
    private AiDhHumanVideoService aiDhHumanVideoService;

    @Autowired
    private CommonMapper commonMapper;

    @Autowired
    private MinioClientService minioClientService;

    @Value("${digital-ability.minioBucket:''}")
    private String minioBucket;

    @Value("${voicePaths.voiceModelUrl}")
    private String voiceModelUrl;
    @Value("${voicePaths.voiceGptUrl}")
    private String voiceGptUrl;
    @Value("${voicePaths.voiceWavUrl}")
    private String voiceWavUrl;
    @Value("${voicePaths.voiceSampleUrl}")
    private String voiceSampleUrl;
    @Value("${voicePaths.downLocalPath}")
    private String downLocalPath;
    @Value("${voicePaths.slicerOptPath}")
    private String slicerOptPath;
    @Value("${voicePaths.asrOptPath}")
    private String asrOptPath;
    @Value("${voiceURLs.trainPath}")
    private String trainPath;
    @Value("${voiceURLs.clonePath}")
    private String clonePath;


    @Value("${voiceURLs.cloneGetPath}")
    private String cloneGetPath;

    @Value("${voiceURLs.voice2TxtUrl}")
    private String voice2TxtUrl;

    @Value("${digital-ability.minioEndpoint}")
    private String minioEndpoint;
    @Value("${digital-ability.minioBucket}")
    private String aidigital;



    @Override
    public HashMap voiceList(HashMap hashMap) throws Exception {
        Integer pageNum = hashMap.get("pageNum") == null ? 1 : (Integer) hashMap.get("pageNum");
        Integer pageSize = hashMap.get("pageSize") == null ? 10 : (Integer) hashMap.get("pageSize");
        Page<TbAiDhVoice> pageParm = new Page<>(pageNum, pageSize);
        QueryWrapper<TbAiDhVoice> queryWrapper = new QueryWrapper<>();
        if (!StringUtils.isEmpty((String) hashMap.get("voiceName"))) {
            queryWrapper.like("voice_name", hashMap.get("voiceName"));
        }
        if (!StringUtils.isEmpty((String) hashMap.get("voiceStatus"))) {
            queryWrapper.eq("voice_status", hashMap.get("voiceStatus"));
        }
        queryWrapper.eq("voice_share", hashMap.get("voiceShare"));
        queryWrapper.orderByDesc("opr_time");
        tbAiDhVoiceMapper.selectPage(pageParm, queryWrapper);
        HashMap res = new HashMap();
        res.put("total", pageParm.getTotal());
        List resultList = new ArrayList();
        for (TbAiDhVoice voice: pageParm.getRecords()){
            voice.setVoiceSampleUrl(minioEndpoint+"/"+aidigital+"/"+voice.getVoiceSampleUrl());
            resultList.add(voice);
        }
        res.put("data", resultList);
        return res;
    }

    @Override
    public HashMap voiceDel(HashMap hashMap) throws Exception {
        HashMap res = new HashMap();
        String id = (String) hashMap.get("id");
        if (!StringUtils.isEmpty(id)) {
            TbAiDhVoice voice = tbAiDhVoiceMapper.selectById(id);
            if (voice != null) {
                deleteVoiceMinioFiles(voice);
            }
            tbAiDhVoiceMapper.deleteById(id);
        }
        return res;
    }

    private void deleteVoiceMinioFiles(TbAiDhVoice voice) {
        try {
            minioClientService.initMinioClient();
            deleteMinioObject(voice.getVoiceSampleUrl());
            deleteMinioObject(voice.getVoiceWavName());
            deleteMinioObject(toMinioKey(voice.getVoiceOrgUrl()));
        } catch (Exception e) {
            log.error("删除声音 MinIO 文件失败, voiceId={}: {}", voice.getId(), e.getMessage());
        }
    }

    private void deleteMinioObject(String key) {
        if (StringUtils.isEmpty(key)) {
            return;
        }
        try {
            minioClientService.minioDelete(key);
        } catch (Exception e) {
            log.warn("删除 MinIO 对象失败, key={}: {}", key, e.getMessage());
        }
    }

    private String toMinioKey(String url) {
        if (StringUtils.isEmpty(url)) {
            return null;
        }
        String marker = "/" + minioBucket + "/";
        int idx = url.indexOf(marker);
        return idx >= 0 ? url.substring(idx + marker.length()) : null;
    }

    @Override
    public HashMap voiceSave(HashMap hashMap) throws Exception {
        HashMap res = new HashMap();
        TbAiDhVoice tbAiDhVoice = new TbAiDhVoice();
        String s = UUID.randomUUID().toString();
        SimpleDateFormat sdf=new SimpleDateFormat("yyyyMMddHHmmss");
        String id=s.substring(0,2)+sdf.format(new Date());
        tbAiDhVoice.setId(id);
        tbAiDhVoice.setVoiceName((String) hashMap.get("voiceName"));
        tbAiDhVoice.setVoiceLabel((String) hashMap.get("voiceLabel"));
        tbAiDhVoice.setVoiceOrgUrl((String) hashMap.get("voiceOrgUrl"));
        tbAiDhVoice.setVoiceSex((String) hashMap.get("voiceSex"));
        tbAiDhVoice.setVoiceStatus("1");
        tbAiDhVoice.setVoiceShare("0");
        tbAiDhVoice.setVoiceReferConent("您好，我是数智人助手，欢迎您来到数智人助手平台，我可以为你制作营销、培训类的数字人视频、卡片，提供专业的语音服务。");
        tbAiDhVoice.setOprTime(new SimpleDateFormat("yyyy-MM-dd HH:mm:ss").format(new Date()));
        tbAiDhVoice.setOprStaff(Long.toString((Long) hashMap.get("oprStaff")));
        //填充调用python信息
        String voiceOrgUrl = (String) hashMap.get("voiceOrgUrl");
        tbAiDhVoice.setVoiceModelUrl(voiceModelUrl.replace("temp",id));
        tbAiDhVoice.setVoiceGptUrl(voiceGptUrl.replace("temp",id));
        tbAiDhVoice.setVoiceWavUrl(voiceWavUrl.replace("temp",id));
        tbAiDhVoice.setVoiceSampleUrl(voiceSampleUrl.replace("temp",id));
        int insert = tbAiDhVoiceMapper.insert(tbAiDhVoice);
        //一部调用python 生成声音的接口
        Thread thread = new Thread() {
            @Override
            public void run() {
                //TODO
                //调用python生成声音的接口   参数和调用步骤
                try {
                    log.info("开始调用声音训练接口  python voice train");
                    JSONObject params = new JSONObject();
                    params.put("voiceId",id);
                    SftpConfigDO sftpConfigDOParam = new SftpConfigDO();
                    sftpConfigDOParam.setSftpSystem("0");
                    SftpConfigDO sftpConfigDO = sftpConfigService.selectBySftpSystem(sftpConfigDOParam);
                    params.put("hostIp",sftpConfigDO.getSftpIp());
                    params.put("port",sftpConfigDO.getSftpPort());
                    params.put("username", EncryptUtil.decrypt(sftpConfigDO.getSftpAccount()));
                    params.put("password",EncryptUtil.decrypt(sftpConfigDO.getSftpPassword()));
                    params.put("fileOrgPath",tbAiDhVoice.getVoiceOrgUrl().replace(minioEndpoint+"/","").replace(aidigital+"/",""));
                    params.put("downLocalPath",downLocalPath+id+voiceOrgUrl.substring(voiceOrgUrl.lastIndexOf("/")));
                    params.put("slicerOptPath",slicerOptPath+id);
                    params.put("asrOptPath",asrOptPath+id);
                    params.put("voiceModelUrl",tbAiDhVoice.getVoiceModelUrl());
                    params.put("voiceGptUrl",tbAiDhVoice.getVoiceGptUrl());
                    params.put("voiceWavUrl",tbAiDhVoice.getVoiceWavUrl());


                    //需要提前创建一些目录
                    sftpCreateDirs(sftpConfigDO,params.get("voiceModelUrl")+"");
                    sftpCreateDirs(sftpConfigDO,params.get("voiceGptUrl")+"");
                    sftpCreateDirs(sftpConfigDO,params.get("voiceWavUrl")+"");



                    Map<String, String> headers = new HashMap<>();
                    headers.put("Content-Type", "application/json;charset=UTF-8");  //传参格式
                    headers.put("Accept", "application/json");
                    log.info("声音训练接口参数：{}",JSONObject.toJSONString(params));
                    AbilityShareClient.doPost(trainPath, headers, JSONObject.toJSONString(params));
                    log.info("声音训练接口  python voice train success");
                } catch (Exception e) {
                    log.error("call Python train Voice:{}",e);
                }


            }
        };
        thread.start();
        res.put("data", 0);
        return res;
    }

    @Override
    public void updateVoice(HashMap hashMap) {
        TbAiDhVoice tbAiDhVoice=new TbAiDhVoice();
        tbAiDhVoice.setId((String)hashMap.get("voiceId"));
        tbAiDhVoice.setVoiceStatus((String)hashMap.get("voiceStatus"));
        tbAiDhVoice.setVoiceGptName((String)hashMap.get("gptName"));
        tbAiDhVoice.setVoiceModelName((String)hashMap.get("sovitsName"));
        tbAiDhVoice.setVoiceWavName((String)hashMap.get("wavName"));
        tbAiDhVoice.setVoiceWavText((String)hashMap.get("promptText"));
        tbAiDhVoiceMapper.updateById(tbAiDhVoice);
        if ("2".equals((String)hashMap.get("voiceStatus"))) {
            Thread thread = new Thread() {
                @Override
                public void run() {
                    //调用python生成声音克隆
                    callVoiceClone(hashMap);
                }
            };
            thread.start();
        }
    }

    @Override
    public String callVoiceClone(HashMap hashMap) {
        try {


            log.info("开始调用声音克隆接口  callVoiceClone ");
            TbAiDhVoice tbAiDhVoiceInfo = tbAiDhVoiceMapper.selectById((String) hashMap.get("voiceId"));
            JSONObject params = new JSONObject();

            //唯一编码
            String voiceId = tbAiDhVoiceInfo.getId()+"_voice";
            params.put("voiceId",voiceId);

            //切割出来的中文文字
            String voice_wav_text = tbAiDhVoiceInfo.getVoiceWavText();


            //要转的中文文字
            String voiceReferConent = tbAiDhVoiceInfo.getVoiceReferConent();
            params.put("voice_refer_conent",voiceReferConent);


            String voiceModelName = tbAiDhVoiceInfo.getVoiceModelName();
            String voiceGptName = tbAiDhVoiceInfo.getVoiceGptName();
            String voiceWavName = tbAiDhVoiceInfo.getVoiceWavName();

            params.put("sovits_name",voiceModelName);
            params.put("gpt_name",voiceGptName);

            //切割出来的声音音频地址及文本
            params.put("voice_wav_text",voice_wav_text);
            params.put("wav_name",voiceWavName);

            //声音克隆制作 voiceType =1  ppt流程中制作生成2 ，ppt制作时 voiceType=3
            String voiceType = "1";
            params.put("voice_type",voiceType); //声音克隆这的为类型为 1

            double speed_factor = 1.0;

            voiceType = hashMap.get("voiceType")+"";
            if(null == voiceType || "".equals(voiceType) || "null".equals(voiceType)){
                voiceType = "1";
            }

            //在主机上的临时复制文件
            String batchpptIdnum = (String)hashMap.get("batchpptIdnum") ;
            //说明是生成语音那的
            if(null != batchpptIdnum && !"".equals(batchpptIdnum) && !"null".equals(batchpptIdnum)){
                //String voicePPtRecordUrl = (String)hashMap.get("voicePPtRecordUrl") ;
                //params.put("voice_sample_url",voicePPtRecordUrl);
                voiceReferConent = (String)hashMap.get("voiceReferConent");
                voiceId = batchpptIdnum;
                voiceType = "2"; //ppt加工为类型为 2
                params.put("voiceId",batchpptIdnum);
                params.put("voice_refer_conent",voiceReferConent);
                params.put("voice_type",voiceType);
            }

            //单独ppt制作合成声音
            if("3".equals(voiceType)){

                String pptId = hashMap.get("pptId")+"";
                String pptNum = hashMap.get("pptNum")+"";
                String pptImageWords = hashMap.get("pptImageWords")+"";

                //语速设置
                speed_factor = Double.parseDouble(hashMap.get("speedFactor")+"");

                voiceId = pptId+"_"+pptNum;
                voiceType = "3";
                voiceReferConent = pptImageWords;
                params.put("voiceId",voiceId);
                params.put("voice_refer_conent",voiceReferConent);
                params.put("voice_type",voiceType);
            }
            //v2版本需要增加的参数
            params.put("text_lang","zh");
            params.put("prompt_lang","zh");
            params.put("text_split_method","cut5");
            params.put("batch_size","10");
            params.put("media_type","wav");
            params.put("streaming_mode","false");


            String encodedVoiceWavText = URLEncoder.encode(voice_wav_text, "UTF-8").replace("+", "%20");
            String encodedText = URLEncoder.encode(voiceReferConent, "UTF-8").replace("+", "%20");

            //拼接get请求（用局部变量，避免污染 @Value 注入的 cloneGetPath 单例字段）
            String url = cloneGetPath + "?text_lang=zh&prompt_lang=zh&text_split_method=cut5&batch_size=1&media_type=wav&streaming_mode=false"
                    + "&voiceId=" + voiceId
                    + "&prompt_text=" + encodedVoiceWavText
                    + "&ref_audio_path=" + voiceWavName
                    + "&text=" + encodedText
                    + "&voice_type=" + voiceType
                    + "&sovits_weights_path=" + voiceModelName
                    + "&gpt_weights_path=" + voiceGptName
                    + "&speed_factor=" + speed_factor
                    + "&bucket_name=" + minioBucket;


            Map<String, String> headers = new HashMap<>();
            headers.put("Content-Type", "application/json;charset=UTF-8");  //传参格式
            headers.put("Accept", "application/json");
            //AbilityShareClient.doPost(clonePath, headers, JSONObject.toJSONString(params));
            String resultData = AbilityShareClient.doGet(url, headers);
            JSONObject jsonObject = JSONObject.parseObject(resultData);

            String code = jsonObject.getString("code");

            String outputFile = "";

            if("0000".equals(code)){
                outputFile = jsonObject.getString("outputFile");
                outputFile = minioEndpoint+"/"+minioBucket+"/"+outputFile;
            }
            log.info("声音克隆接口  callVoiceClone success");
            return outputFile;
        } catch (Exception e) {
            log.error("call Python Voice Clone:{}",e);
        }
        return null;
    }

    @Override
    public void updateVoiceFinall(HashMap hashMap) {

        String voiceType = hashMap.get("voiceType")+"";
        if("1".equals(voiceType)){
            TbAiDhVoice tbAiDhVoice=new TbAiDhVoice();
            String voiceId=(String)hashMap.get("voiceId");
            tbAiDhVoice.setId(voiceId.replace("_voice",""));
            tbAiDhVoice.setVoiceStatus((String)hashMap.get("voiceStatus"));
            tbAiDhVoice.setVoiceSampleUrl((String)hashMap.get("voiceSampleUrl"));
            tbAiDhVoiceMapper.updateById(tbAiDhVoice);
        }else if("2".equals(voiceType)){ //更新ppt每页的语音地址
            Map<String,Object> params = new HashMap<>();
            String batchPptidPagenum = hashMap.get("voiceId")+"";
            String voiceStatus = hashMap.get("voiceStatus")+"";
            String voiceSampleUrl = hashMap.get("voiceSampleUrl")+"";
            String length = resolveVoiceLength(hashMap.get("length")+"", voiceSampleUrl);
            String[] s = batchPptidPagenum.split("_");
            params.put("batchNum",s[0]);
            params.put("videoId",s[3]);
            params.put("length",length);
            params.put("pptVoiceUrl",voiceSampleUrl);
            params.put("modelId",s[1]);
            params.put("modelNum",s[2]);
            params.put("modelType","txt_2_voice");
            if("4".equals(voiceStatus)){ // 成功
                params.put("execStatus","1");

                //更新日志执行状态
                commonMapper.updateVideoExecLog(params);

                //执行下一步
                Thread thread = new Thread() {
                    @Override
                    public void run() {
                        aiDhHumanVideoService.makePptVideoStart(s[3],s[0],s[1]);
                    }
                };
                thread.start();
            }else{ //失败
                params.put("execStatus","2");
                //更新执行日志状态
                commonMapper.updateVideoExecLog(params);
                //更新整体流程失败状态
                aiDhHumanVideoService.updateMakeVideoStatus(s[3],"3");
            }

        }else if("3".equals(voiceType)){ //每页ppt生成语音
            Map<String,Object> params = new HashMap<>();
            String voiceIdStr = hashMap.get("voiceId")+"";
            String voiceStatus = hashMap.get("voiceStatus")+"";
            String length = hashMap.get("length")+"";
            String voiceSampleUrl = hashMap.get("voiceSampleUrl")+"";
            String[] s = voiceIdStr.split("_");

            if("4".equals(voiceStatus)){ //成功
                params.put("length",length);
                params.put("pptVoiceUrl",voiceSampleUrl);
                params.put("pptId",s[0]);
                params.put("pptNum",s[1]);
                pptRecordDetailService.updatePptRecordUrlAndLength(params);
            }


        }

    }

    /**
     * Python 声音克隆回调可能不返回音频时长，此时从 MinIO 的 wav 文件解析时长（毫秒）。
     */
    private String resolveVoiceLength(String length, String voiceSampleUrl) {
        if (StringUtils.isNotBlank(length) && !"null".equals(length)) {
            return length;
        }
        try {
            minioClientService.initMinioClient();
            byte[] wav = minioClientService.minioDownload(voiceSampleUrl);
            long ms = readWavDurationMs(wav);
            if (ms > 0) {
                return String.valueOf(ms);
            }
        } catch (Exception e) {
            log.error("解析音频时长失败 voiceSampleUrl:{}", voiceSampleUrl, e);
        }
        return length;
    }

    private long readWavDurationMs(byte[] wav) {
        int byteRate = 0;
        int dataSize = 0;
        int pos = 12; // 跳过 "RIFF" + 长度 + "WAVE"
        while (pos + 8 <= wav.length) {
            if (wav[pos] == 'f' && wav[pos + 1] == 'm' && wav[pos + 2] == 't' && wav[pos + 3] == ' ') {
                byteRate = readIntLE(wav, pos + 16);
            } else if (wav[pos] == 'd' && wav[pos + 1] == 'a' && wav[pos + 2] == 't' && wav[pos + 3] == 'a') {
                dataSize = readIntLE(wav, pos + 4);
                break;
            }
            int chunkSize = readIntLE(wav, pos + 4);
            pos += 8 + chunkSize + (chunkSize & 1);
        }
        if (byteRate <= 0 || dataSize <= 0) {
            return 0;
        }
        return (long) dataSize * 1000 / byteRate;
    }

    private int readIntLE(byte[] b, int off) {
        return (b[off] & 0xFF) | ((b[off + 1] & 0xFF) << 8) | ((b[off + 2] & 0xFF) << 16) | ((b[off + 3] & 0xFF) << 24);
    }

    //语音转文字
    @Override
    public HashMap voice2Txt(Map<String, Object> params) throws Exception {
        Map<String, String> headers = new HashMap<>();
        headers.put("Content-Type", "application/json;charset=UTF-8");  //传参格式
        headers.put("Accept", "application/json");

        params.put("bucket_name",minioBucket);
        String s = AbilityShareClient.doPost(voice2TxtUrl, headers, JSONObject.toJSONString(params));
        JSONObject jsonObject = JSONObject.parseObject(s);
        String text = jsonObject.get("text")+"";

        List<Map> orgChangeField = commonMapper.getOrgChangeField();

        for (Map<String,Object> map : orgChangeField){
            text = text.replace(map.get("orgField")+"",map.get("changeField")+"");
        }
        HashMap result = new HashMap();
        result.put("text",text);
        return result;
    }

    private String sftpCreateDirs(SftpConfigDO sftpConfigDO,String directory) {
        // MinIO 无目录概念，无需创建目录
        return null;
    }
    public void createDirectory(String directory,ChannelSftp sftp) throws  Exception {
        String[] dirs = directory.split("/");
        String currentDir = "";
        for (String dir : dirs) {
            if (dir.isEmpty()) {
                continue;
            }
            currentDir += "/" + dir;
            if (!isDirectoryExist(currentDir,sftp)) {
                sftp.mkdir(currentDir);
            }
        }
    }
    public boolean isDirectoryExist(String directory,ChannelSftp sftp) {
        try {
            SftpATTRS attrs = sftp.lstat(directory);
            return attrs.isDir();
        } catch (SftpException e) {
            return false;
        }
    }
}
