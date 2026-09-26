package cn.iocoder.yudao.module.digital.util;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.text.SimpleDateFormat;
import java.util.*;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class FormatQuestionUtil {
    private String question;
    private String answer;
    private String keyPoints;

    public FormatQuestionUtil(String question, String answer, String keyPoints) {
        this.question = question;
        this.answer = answer;
        this.keyPoints = keyPoints;
    }

    // Getters and setters

    @Override
    public String toString() {
        return "QuizQuestion{" +
                "question='" + question + '\'' +
                ", answer='" + answer + '\'' +
                ", keyPoints='" + keyPoints + '\'' +
                '}';
    }

    public static class Parser {
        private static final Pattern PATTERN = Pattern.compile(
                "^\\*\\*问：\\*\\*(.*?)^\\*\\*答：\\*\\*(.*?)^\\*\\*关键信息点：\\*\\*(.*?)",
                Pattern.DOTALL | Pattern.MULTILINE
        );

        public static FormatQuestionUtil parse(String markdown) throws JSONException {
            // 定义正则表达式，匹配连续两个或更多的换行符
            String regex = "\\n{2,}";
            Pattern pattern = Pattern.compile(regex);
            Matcher matcher1 = pattern.matcher(markdown.replaceAll("-",""));
            markdown = matcher1.replaceAll("\n");

            Matcher matcher = PATTERN.matcher(markdown);
            if (matcher.find()) {
                String question = matcher.group(1).trim();
                String answer =  matcher.group(2).trim();
                String keyPoints = matcher.group(3).trim();
                List<String> list = Arrays.asList(matcher.group(3).trim().split("\n"));
                StringBuffer sb = new StringBuffer();
                if (list.size()>1){
                    for (String str:list){
                        sb.append(str).append("、");
                    }
                    if (sb.length() > 0) {
                        // 替换最后一个字符为 '。'
                        sb.setCharAt(sb.length() - 1, '。');
                    }
                    keyPoints = sb.toString();
                }
                return new FormatQuestionUtil(question, answer, keyPoints);
            }
            throw new IllegalArgumentException("Invalid markdown format");
        }
    }

    public static List<Map<String, Object>> subjectResult(String markdownStr) throws JSONException {
        String[] strs = markdownStr.replaceAll("---","").replaceAll(" ","").replaceAll("\\n\\s*\\n", "\n").replaceAll("\\\\n", "\n").split("###");
        List<Map<String, Object>> resultList = new ArrayList<>();
        for (String str:strs){
            if (str!=null && !str.isEmpty()){
                if (str.contains("场景")){
                    //按题目切分
//                    String[] qas = str.split("\\*\\*题目：\\*\\*\n");
                    String[] qas = str.split("\\*\\*问：\\*\\*");
                    for (String qa:qas){
                        if (!qa.contains("场景")){
                            FormatQuestionUtil question = Parser.parse("**问：**\n"+qa);
                            //拼接入题库格式
                            Map<String, Object> questionObj = initObj2(question);
                            resultList.add(questionObj);
                        }
                    }
                }
            }
        }
        System.out.println(resultList);
        return resultList;
    }

    public static  JSONObject initObj(FormatQuestionUtil question) throws JSONException {
        JSONObject initObj = new JSONObject();
        initObj.put("question",question.question);
        initObj.put("answer",question.answer);
        initObj.put("keyPoints",question.keyPoints);
        return initObj;
    }

    public static  Map<String, Object> initObj2(FormatQuestionUtil question) {
        Map<String, Object> initObj = new HashMap<>();
        initObj.put("question",question.question);
        initObj.put("answer",question.answer);
        initObj.put("keyPoints",question.keyPoints);
        return initObj;
    }

    public static void main(String[] args) throws JSONException {
        String markdown = "### 场景一\n\n**问：**\n我正在向一位家庭客户推荐FTTR（光纤到房间）产品，他家有多个房间需要高质量的网络覆盖。我应该如何有效地向他介绍FTTR的优势和适合他的方案？\n\n**答：**\n首先，强调FTTR能够提供高速稳定的Wi-Fi连接，尤其适合大型家庭或同时在线使用多种设备的需求。其次，说明它通过光纤直接接入各个房间，可以避免传统无线路由器带来的信号衰减问题，确保每个角落都有良好的网络体验。最后，提供不同套餐的选择，比如基础版适用于基本上网需求的家庭，高级版则增加智能家居控制等额外功能。\n\n**关键信息点：**\n- 高速稳定网络\n- 光纤直接接入房间\n- 解决信号衰减问题\n- 提供不同套餐选择\n\n### 场景二\n\n**问：**\n我的目标客户是一家小型企业主，他们对于网络的可靠性和安全性有较高要求。在推荐FTTR时，我应该侧重哪些方面来满足他们的需求？\n\n**答：**\n针对企业用户，重点突出FTTR的高可靠性与安全性。FTTR采用光纤作为传输媒介，大大提升了网络的稳定性，减少断线情况。同时，强调其独享带宽的特点，保证业务流量不受其他用户影响。此外，可引入FTTR的网络安全特性，如加密技术，保护企业数据安全。建议根据企业的具体需求，定制化提供支持视频会议、文件共享等功能的解决方案。\n\n**关键信息点：**\n- 高可靠性\n- 独享带宽\n- 强大的网络安全防护\n- 定制化企业级服务\n";
//        String str = "### 场景一: 基本介绍与应用场景\n\n**问：**\\n我想向我的客户提供FTTR产品，应该从哪些方面入手进行推荐？\\n\\n**答：**\\n在向客户推荐FTTR（光纤到户）产品时，可以从以下几个方面入手：\\n1. **高带宽优势**：强调FTTR能提供稳定高速的网络连接，满足家庭或企业中日益增长的数据传输需求。\\n2. **全屋覆盖**：解释FTTR通过光纤直接接入各个房间，确保整个房屋内的Wi-Fi信号强大而均匀，适合大户型和复式楼。\\n3. **安全性**：突出FTTR使用的光信号不会像传统Wi-Fi那样受到干扰，提供了更加安全可靠的网络环境。\\n4. **节能环保**：说明FTTR设备功耗低，使用光纤传输减少能源消耗，符合绿色节能的理念。\\n\\n**关键信息点：** 强调FTTR的高速稳定性、全屋覆盖、安全性及环保特性作为主要卖点。\\n\\n### 场景二: 解决客户痛点与个性化定制\\n\\n**问：**\\n我有一个客户家里有老人和小孩，他们经常反映家里的Wi-Fi不稳定。我该如何利用FTTR来解决这个问题并推荐给他们？\\n\\n**答：**\\n针对您的客户情况，可以这样推荐FTTR：\\n\\n1. **增强稳定性**：FTTR采用光纤直连技术，大幅减少了无线信号衰减和干扰，特别适用于多层住宅，保证每个角落都有稳定的网络连接。\\n2. **无缝漫游体验**：即便老人和小孩在家中移动，也能实现快速、平滑的Wi-Fi切换，避免断线重连带来的不便。\\n3. **智能管理功能**：推荐选择支持智能管理的FTTR套餐，如自动优化网络设置、远程诊断故障等功能，方便老年人操作，提高生活质量。\\n4. **长期投资回报**：对比传统解决方案的成本和维护成本，说明FTTR长期来看能够节省开支，提升整体生活品质。\\n\\n**关键信息点：** 针对不稳定网络的问题，重点突出FTTR的稳定性和智能化管理功能，同时强调其对提高生活质量的价值。\n";
//        System.out.println(str.replaceAll("\\\\n", "\n"));

        subjectResult(markdown);

    }
}
