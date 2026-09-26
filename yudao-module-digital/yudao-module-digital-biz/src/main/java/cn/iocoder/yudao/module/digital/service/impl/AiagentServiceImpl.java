package cn.iocoder.yudao.module.digital.service.impl;

import cn.iocoder.yudao.framework.common.exception.enums.GlobalErrorCodeConstants;
import cn.iocoder.yudao.module.digital.dal.AiAgentDO;
import cn.iocoder.yudao.module.digital.dal.CommparaDo;
import cn.iocoder.yudao.module.digital.dal.mysql.AiAgentMapper;
import cn.iocoder.yudao.module.digital.dal.mysql.CommonMapper;
import cn.iocoder.yudao.module.digital.dal.mysql.TbAiInteractAgentMapper;
import cn.iocoder.yudao.module.digital.service.AiCommparaService;
import cn.iocoder.yudao.module.digital.service.AiagentService;
import cn.iocoder.yudao.module.digital.util.FormatQuestionUtil;
import cn.iocoder.yudao.module.digital.vo.AiAgentRespVO;
import cn.iocoder.yudao.module.digital.dal.AiInteractAgentDo;
import cn.iocoder.yudao.module.digital.vo.AiInteractAgentVo;
import com.alibaba.fastjson.JSON;
import com.alibaba.fastjson.JSONObject;
import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import lombok.extern.slf4j.Slf4j;
import org.json.JSONException;
import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Service;

import javax.annotation.Resource;
import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.nio.charset.StandardCharsets;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;
import java.util.function.Consumer;

import cn.hutool.http.HttpRequest;
import cn.hutool.http.HttpResponse;
import cn.hutool.json.JSONUtil;
import org.springframework.util.StringUtils;

import java.util.regex.Matcher;
import java.util.regex.Pattern;

import com.alibaba.fastjson.JSONArray;

import static cn.iocoder.yudao.framework.common.pojo.CommonResult.error;

/**
 * @BelongsProject: yudao
 * @BelongsPackage: cn.iocoder.yudao.module.digital.service.impl
 * @Author: zhaowang
 * @CreateTime: 2024-07-17  17:08
 * @Description: TODO
 * @Version: 1.0
 */
@Slf4j
@Service("aiagentService")
public class AiagentServiceImpl implements AiagentService {
    private final static String MODE_STREAMING = "streaming";
    private final static String MODE_BLOCKING = "blocking";

    @Resource
    private AiAgentMapper aiAgentMapper;

    @Resource
    private TbAiInteractAgentMapper aiInteractAgentMapper;

    @Resource
    private AiCommparaService aiCommparaService;

    @Resource
    private CommonMapper commonMapper;

    @Override
    public List<AiAgentRespVO> getAiAgentList() {
        List<AiAgentDO> agentDOList = aiAgentMapper.selectListAll();
        List<AiAgentRespVO> retList = agentDOList.stream().map(AiAgentDO -> {
            AiAgentRespVO  retVO = new AiAgentRespVO();
            retVO.setId(AiAgentDO.getId());
            retVO.setAgentName(AiAgentDO.getAgentName());
            retVO.setAgentRole(AiAgentDO.getAgentRole() == null ? "" : AiAgentDO.getAgentRole());
            return retVO;
        }).collect(Collectors.toList());
        return retList;
    }

    @Override
    public List<AiAgentRespVO> getInteractAgentList(String agentType) {
        QueryWrapper<AiInteractAgentDo> queryWrapper = new QueryWrapper<>();
        queryWrapper.eq("agent_type",agentType).eq("agent_state","1");
        List<AiInteractAgentDo> agentList = aiInteractAgentMapper.selectList(queryWrapper);
        List<AiAgentRespVO> retList = agentList.stream().map(AiAgentDO -> {
            AiAgentRespVO  retVO = new AiAgentRespVO();
            retVO.setId(AiAgentDO.getId());
            retVO.setAgentName(AiAgentDO.getAgentName());
            retVO.setAgentRole(AiAgentDO.getAgentType() == null ? "" : AiAgentDO.getAgentType());
            return retVO;
        }).collect(Collectors.toList());
        return retList;
    }

    @Override
    public List<Map<String, Object>> getLanguagePracticeInfo(AiInteractAgentVo aiInteractAgent) throws JSONException {
        String answerInfo = question(aiInteractAgent);
        return FormatQuestionUtil.subjectResult(answerInfo);
    }

    @Override
    public Map getInteractiveDialogueInfo(AiInteractAgentVo aiInteractAgent) {
        String answerInfo = question(aiInteractAgent);
        if (!answerInfo.isEmpty()){
            String humanInteractUrl = "";
            String type = "";
            List<CommparaDo> resultList = aiCommparaService.getPptRecordDetail("human_interact_url");
            if (resultList.size()>0){
                humanInteractUrl = resultList.get(0).getAttrValue();
                type = resultList.get(0).getParamCode1();
                aiInteractAgent.setHumanInteractUrl(humanInteractUrl);
                aiInteractAgent.setAnswerInfo(answerInfo);
                aiInteractAgent.setType(type);
                sendMsgForHuman(aiInteractAgent);
            }
        }
        Map map = new HashMap();
        map.put("answerInfo",answerInfo);
        return map;
    }

    @Override
    public Map getDialogueHumanQaInfo(AiInteractAgentVo aiInteractAgent) {
        Map map = new HashMap();
        //根据问题搜索资源：
        List<HashMap> urlMaps = commonMapper.getAiImageAndVideoUrl();
        List<Map> urlList = new ArrayList<>();
        String answerInfo = "";
        if (urlMaps.size()>0){
            for (HashMap map1 : urlMaps){
                Map map2 =new HashMap();
                String[] keywords = map1.get("keyword").toString().split("\\|");
                Boolean checkRes = true;
                for (String keyword : keywords) {
                    if (!aiInteractAgent.getQuery().contains(keyword)) {
                        checkRes = false;
                    }
                }
                if(checkRes){
                    map2.put("dataType",map1.get("data_type"));
                    map2.put("dataName",map1.get("data_name"));
                    map2.put("dataUrl",map1.get("data_url"));
                    urlList.add(map2);
                }
            }
        }
        if(urlList.size()>0){
//            if (urlList.get(0).get("dataType")!=null && urlList.get(0).get("dataType").equals("3")){
//                answerInfo = urlList.get(0).get("dataUrl")+"";
//                map.put("isLinks",false);
//                urlList = new ArrayList<>();
//            }else {
//                answerInfo = "您好，已经为您搜索到以下内容。";
                map.put("isLinks",true);
//            }
//            map.put("answerInfo",answerInfo);
        }else{
            map.put("isLinks",false);
        }
        if (aiInteractAgent.getType()!=null && aiInteractAgent.getType().equals("2")){ //打断
            answerInfo = "";
        }else{
            answerInfo = question(aiInteractAgent);
        }
        map.put("answerInfo",answerInfo);
//        if (!answerInfo.isEmpty()){
            String humanInteractUrl = "";
            String type = "";
            Boolean interrupt = true;
            List<CommparaDo> resultList = aiCommparaService.getPptRecordDetail("human_interact_url");
            if (resultList.size()>0){
                humanInteractUrl = resultList.get(0).getAttrValue();
                type = resultList.get(0).getParamCode1();
                interrupt = Boolean.valueOf(resultList.get(0).getParamCode2());
                aiInteractAgent.setHumanInteractUrl(humanInteractUrl);
                aiInteractAgent.setAnswerInfo(answerInfo);
                aiInteractAgent.setType(type);
                aiInteractAgent.setInterrupt(interrupt);
                try{
                    sendMsgForHuman(aiInteractAgent);
                }catch (Exception e){
                    log.error("调用数字人互动接口异常：{}",e.getMessage());
                }
            }
//        }
        map.put("urlList",urlList);
        return map;
    }

    @Override
    public Map getDialogueWorkflowQaInfo(AiInteractAgentVo aiInteractAgent) {
        Map map = new HashMap();
        String answerInfo = "";
        if (aiInteractAgent.getType()!=null && aiInteractAgent.getType().equals("2")){ //打断
            answerInfo = "";
        }else{
            answerInfo = questionByWorkflow(aiInteractAgent);
        }
        map.put("isLinks",false);
        map.put("answerInfo",JSONObject.parse(answerInfo));

        String humanInteractUrl = "";
        String type = "";
        Boolean interrupt = true;
        List<CommparaDo> resultList = aiCommparaService.getPptRecordDetail("human_interact_url");
        if (resultList.size()>0){
            humanInteractUrl = resultList.get(0).getAttrValue();
            type = resultList.get(0).getParamCode1();
            interrupt = Boolean.valueOf(resultList.get(0).getParamCode2());
            aiInteractAgent.setHumanInteractUrl(humanInteractUrl);
            JSONObject answerObj = JSONObject.parseObject(answerInfo);
            aiInteractAgent.setAnswerInfo(answerObj.getString("answer"));
            aiInteractAgent.setType(type);
            aiInteractAgent.setInterrupt(interrupt);
            try{

                sendMsgForHuman(aiInteractAgent);
            }catch (Exception e){
                log.error("调用数字人互动接口异常：{}",e.getMessage());
            }
        }
        return map;
    }

    @Override
    public String streamDialogue(AiInteractAgentVo aiInteractAgent, Consumer<String> onChunk) {
        AiInteractAgentDo agent = aiInteractAgentMapper.selectById(aiInteractAgent.getId());
        if (agent == null) {
            return null;
        }
        JSONObject params = new JSONObject();
        Object inputs = aiInteractAgent.getInputObj();
        JSONObject object = (JSONObject) JSON.toJSON(inputs);
        params.put("inputs", object);
        params.put("query", aiInteractAgent.getQuery());
        params.put("response_mode", "streaming");
        params.put("conversation_id", aiInteractAgent.getConversationId());
        params.put("user", aiInteractAgent.getUser());
        params.put("files", new JSONArray());

        HttpRequest httpRequest = HttpRequest.post(agent.getAgentApiUrl())
                .header("Content-Type", "application/json")
                .header("Authorization", "Bearer " + agent.getAgentApiKey())
                .timeout(200000)
                .body(JSONUtil.toJsonStr(params));
        HttpResponse execute = httpRequest.execute();

        StringBuilder fullAnswer = new StringBuilder();
        try (InputStream inputStream = execute.bodyStream();
             BufferedReader bufferedReader = new BufferedReader(new InputStreamReader(inputStream, StandardCharsets.UTF_8))) {
            String line;
            while ((line = bufferedReader.readLine()) != null) {
                if (StringUtils.hasLength(line)) {
                    Matcher matcher = contentPattern.matcher(line);
                    if (matcher.find()) {
                        String content = unescapeJsonString(matcher.group(1));
                        fullAnswer.append(content);
                        onChunk.accept(content);
                    }
                }
            }
        } catch (IOException e) {
            throw new RuntimeException(e);
        }

        // 流式结束后合成语音，返回音频地址供前端播报
        List<CommparaDo> resultList = aiCommparaService.getPptRecordDetail("human_interact_url");
        if (resultList.size() > 0) {
            aiInteractAgent.setHumanInteractUrl(resultList.get(0).getAttrValue());
            aiInteractAgent.setType(resultList.get(0).getParamCode1());
            aiInteractAgent.setInterrupt(Boolean.valueOf(resultList.get(0).getParamCode2()));
            return synthesizeAudio(aiInteractAgent, fullAnswer.toString());
        }
        return null;
    }

    /**
     * 文本合成语音，返回音频地址（供前端播报）。失败返回 null。
     */
    private String synthesizeAudio(AiInteractAgentVo aiInteractAgent, String text) {
        try {
            JSONObject params = new JSONObject();
            params.put("text", text);
            params.put("type", aiInteractAgent.getType());
            params.put("interrupt", aiInteractAgent.getInterrupt());
            HttpRequest httpRequest = HttpRequest.post(aiInteractAgent.getHumanInteractUrl())
                    .header("Content-Type", "application/json")
                    .timeout(30000)
                    .body(JSONUtil.toJsonStr(params));
            HttpResponse execute = httpRequest.execute();
            String body = execute.body();
            JSONObject resp = JSONObject.parseObject(body);
            return resp == null ? null : resp.getString("audioUrl");
        } catch (Exception e) {
            log.error("synthesizeAudio error: {}", e.getMessage());
            return null;
        }
    }

    @Async
    public void sendMsgForHuman(AiInteractAgentVo aiInteractAgent) {
        StringBuffer answerSb = new StringBuffer();
        long timeElapsed = 0L;
        try {
            JSONObject params = new JSONObject();
            params.put("text",aiInteractAgent.getAnswerInfo());
            params.put("type",aiInteractAgent.getType());
            params.put("interrupt",aiInteractAgent.getType());
            // 发起 HTTP 请求
            long start = System.currentTimeMillis();
            HttpRequest httpRequest = HttpRequest.post(aiInteractAgent.getHumanInteractUrl())
                    .header("Content-Type", "application/json")
                    .timeout(5000)
                    .body(JSONUtil.toJsonStr(params));
            // 执行 HTTP 请求
            HttpResponse execute = httpRequest.execute();
            // 处理响应流
            try (InputStream inputStream = execute.bodyStream();
                 BufferedReader bufferedReader = new BufferedReader(new InputStreamReader(inputStream, StandardCharsets.UTF_8))) {
                String line;
                while ((line = bufferedReader.readLine()) != null) {
                    if (StringUtils.hasLength(line)) {
                        // 输出响应内容
//						System.out.println(line);
                        // 提取内容
                        Matcher matcher = contentPattern.matcher(line);
                        if (matcher.find()) {
                            String content = matcher.group(1);
//							System.out.println(content);
                            answerSb.append(content);
                            // 发送 SSE 事件 （模拟延迟)
//							Thread.sleep(1000);
//							sseEmitter.send(SseEmitter.event().name("answer").data("{" + content + "}"));
                        }
                    }
                }
                System.out.println(answerSb);
            }
            long finish = System.currentTimeMillis();
            timeElapsed = finish - start;
            System.out.println("耗时："+timeElapsed);
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
    }

    private static final Pattern contentPattern = Pattern.compile("\"answer\": \"(.*?)\"}");

    public String question(AiInteractAgentVo aiInteractAgent) {
        StringBuffer answerSb = new StringBuffer();
        long timeElapsed = 0L;
        String state = "";
        try {
            AiInteractAgentDo agent = aiInteractAgentMapper.selectById(aiInteractAgent.getId());
            JSONObject params = new JSONObject();
            Object inputs = aiInteractAgent.getInputObj();
            JSONObject object = (JSONObject) JSON.toJSON(inputs);
            System.out.println(object);

            params.put("inputs",object);
            params.put("query",aiInteractAgent.getQuery());
            params.put("response_mode",agent.getResponseMode());
            params.put("conversation_id",aiInteractAgent.getConversationId());
            params.put("user",aiInteractAgent.getUser());
            params.put("files",new JSONArray());
            // 发起 HTTP 请求
            long start = System.currentTimeMillis();

            HttpRequest httpRequest = HttpRequest.post(agent.getAgentApiUrl())
                    .header("Content-Type", "application/json")
                    .header("Authorization", "Bearer " +agent.getAgentApiKey())
                    .timeout(200000)
                    .body(JSONUtil.toJsonStr(params));
//					.setProxy(new Proxy(Proxy.Type.HTTP, new InetSocketAddress("127.0.0.1", 7890)));

            // 执行 HTTP 请求
            HttpResponse execute = httpRequest.execute();


            // 处理响应流
            try (InputStream inputStream = execute.bodyStream();
                 BufferedReader bufferedReader = new BufferedReader(new InputStreamReader(inputStream, StandardCharsets.UTF_8))) {

                String line;
                while ((line = bufferedReader.readLine()) != null) {
                    if (StringUtils.hasLength(line)) {
                        // 输出响应内容
//						System.out.println(line);
                        if(agent.getResponseMode().equals(MODE_STREAMING)){
                            Matcher matcher = contentPattern.matcher(line);
                            if (matcher.find()) {
                                String content = decodeUnicode(matcher.group(1));
                                //							System.out.println(content);
                                answerSb.append(content);
                                // 发送 SSE 事件 （模拟延迟)
                                //							Thread.sleep(1000);
                                //							sseEmitter.send(SseEmitter.event().name("answer").data("{" + content + "}"));
                            }
                        }else{
                            JSONObject obj = JSONObject.parseObject(decodeUnicode(line));
                            answerSb.append(obj.getString("answer"));
                        }

                    }
                }
                System.out.println(decodeUnicode(answerSb.toString()));
            }
            long finish = System.currentTimeMillis();
            timeElapsed = finish - start;
            System.out.println("耗时："+timeElapsed);
        } catch (IOException e) {
            // 异常处理
            throw new RuntimeException(e);
        }
        return answerSb.toString();
    }


    public String questionByWorkflow(AiInteractAgentVo aiInteractAgent) {
        StringBuffer answerSb = new StringBuffer();
        long timeElapsed = 0L;
        String state = "";
        try {
            AiInteractAgentDo agent = aiInteractAgentMapper.selectById(aiInteractAgent.getId());
            JSONObject params = new JSONObject();
            Object inputs = aiInteractAgent.getInputObj();
            JSONObject object = (JSONObject) JSON.toJSON(inputs);
            System.out.println(object);

            params.put("inputs",object);
            params.put("response_mode",agent.getResponseMode());
            params.put("user",aiInteractAgent.getUser());
            // 发起 HTTP 请求
            long start = System.currentTimeMillis();

            HttpRequest httpRequest = HttpRequest.post(agent.getAgentApiUrl())
                    .header("Content-Type", "application/json")
                    .header("Authorization", "Bearer " +agent.getAgentApiKey())
                    .timeout(200000)
                    .body(JSONUtil.toJsonStr(params));
//					.setProxy(new Proxy(Proxy.Type.HTTP, new InetSocketAddress("127.0.0.1", 7890)));

            // 执行 HTTP 请求
            HttpResponse execute = httpRequest.execute();


            // 处理响应流
            try (InputStream inputStream = execute.bodyStream();
                 BufferedReader bufferedReader = new BufferedReader(new InputStreamReader(inputStream, StandardCharsets.UTF_8))) {

                String line;
                while ((line = bufferedReader.readLine()) != null) {
                    if (StringUtils.hasLength(line)) {
                        // 输出响应内容
//						System.out.println(line);
                        if(agent.getResponseMode().equals(MODE_STREAMING)){
                            Matcher matcher = contentPattern.matcher(line);
                            if (matcher.find()) {
                                String content = decodeUnicode(matcher.group(1));
                                //							System.out.println(content);
                                answerSb.append(content);
                                // 发送 SSE 事件 （模拟延迟)
                                //							Thread.sleep(1000);
                                //							sseEmitter.send(SseEmitter.event().name("answer").data("{" + content + "}"));
                            }
                        }else{
                            JSONObject obj = JSONObject.parseObject(decodeUnicode(line));
                            answerSb.append(obj.getJSONObject("data").getJSONObject("outputs"));
                        }

                    }
                }
                System.out.println(decodeUnicode(answerSb.toString()));
            }
            long finish = System.currentTimeMillis();
            timeElapsed = finish - start;
            System.out.println("耗时："+timeElapsed);
        } catch (IOException e) {
            // 异常处理
            throw new RuntimeException(e);
        }
        return answerSb.toString();
    }

    private static String decodeUnicode(String unicodeString) {
        StringBuilder sb = new StringBuilder();
        for (int i = 0; i < unicodeString.length();) {
            if (unicodeString.charAt(i) == '\\' && unicodeString.charAt(i + 1) == 'u') {
                String unicodeHex = unicodeString.substring(i + 2, i + 6);
                char ch = (char) Integer.parseInt(unicodeHex, 16);
                sb.append(ch);
                i += 6; // 跳过已处理的Unicode编码
            } else {
                sb.append(unicodeString.charAt(i++));
            }
        }
        return sb.toString();
    }

    /**
     * 反转义 JSON 字符串（处理 \n \t \r \\ \" \/ 以及 unicode 转义等），
     * 用于把智能体流式返回的 answer 字段还原成正常文本（否则换行会显示成字面 \n）。
     */
    private static String unescapeJsonString(String s) {
        StringBuilder sb = new StringBuilder();
        for (int i = 0; i < s.length(); i++) {
            char c = s.charAt(i);
            if (c != '\\' || i + 1 >= s.length()) {
                sb.append(c);
                continue;
            }
            char next = s.charAt(i + 1);
            switch (next) {
                case 'n': sb.append('\n'); i++; break;
                case 't': sb.append('\t'); i++; break;
                case 'r': sb.append('\r'); i++; break;
                case 'b': sb.append('\b'); i++; break;
                case 'f': sb.append('\f'); i++; break;
                case '\\': sb.append('\\'); i++; break;
                case '"': sb.append('"'); i++; break;
                case '/': sb.append('/'); i++; break;
                case 'u':
                    if (i + 5 < s.length()) {
                        String hex = s.substring(i + 2, i + 6);
                        sb.append((char) Integer.parseInt(hex, 16));
                        i += 5;
                    } else {
                        sb.append(c);
                    }
                    break;
                default:
                    sb.append(c);
            }
        }
        return sb.toString();
    }
}
