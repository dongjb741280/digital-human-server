package cn.iocoder.yudao.module.digital.util;

import java.util.Random;


public class SequenceUtils {

    /**
     * 序列生成工具类
     * huangcm
     *
     * @return seq
     */
    public static long getSeq() {
        //当前系统时间戳精确到毫秒
        Long simple = System.currentTimeMillis();
        //三位随机数，为变量赋随机值100-999;
        int random = new Random().nextInt(900) + 100;
        return Long.parseLong(simple.toString() + random);
    }

    public static long getSeqForInt() {
        //当前系统时间戳精确到毫秒
        Long simple = System.currentTimeMillis();
        //三位随机数，为变量赋随机值100-999;
        int random = new Random().nextInt(900) + 100;
        return Long.parseLong(simple.toString() + random);
    }
}
