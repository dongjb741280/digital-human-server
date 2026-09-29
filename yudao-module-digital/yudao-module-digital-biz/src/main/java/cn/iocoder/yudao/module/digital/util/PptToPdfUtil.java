package cn.iocoder.yudao.module.digital.util;



import lombok.extern.slf4j.Slf4j;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.util.concurrent.TimeUnit;

@Slf4j
public class PptToPdfUtil {

    /**
     * 利用libreOffice将office文档转换成pdf
     * @param inputFile  目标文件地址
     * @param pdfFile    输出文件夹
     * @return
     */
    public static boolean convert2PDF(String inputFile, String pdfFile){
        long start = System.currentTimeMillis();
        String command;
        boolean flag;
        String osName = System.getProperty("os.name");
        // 每次用独立 profile，避免与 GUI/并发实例抢锁导致 soffice 卡死（历史 120s 超时根因）；
        // 滤镜去掉 writer_pdf_Export，改自动检测（对 pptx 会正确走 impress_pdf_Export）
        String userInstallation = "file://" + System.getProperty("java.io.tmpdir") + "lo_profile_" + System.currentTimeMillis();
        if (osName.contains("Windows")) {
            command = "cmd /c start soffice --headless --invisible -env:UserInstallation=" + userInstallation + " --convert-to pdf " + inputFile + " --outdir " + pdfFile;
        }else {
            command = "/Applications/LibreOffice.app/Contents/MacOS/soffice --headless --invisible -env:UserInstallation=" + userInstallation + " --convert-to pdf " + inputFile + " --outdir " + pdfFile;
        }
        flag = executeCommand(command);
        long end = System.currentTimeMillis();

        log.debug("用时:{} ms", end - start);
        return flag;
    }


    /**
     * 执行command指令
     * @param command
     * @return
     */
    public static boolean executeCommand(String command) {
        log.info("开始进行转化.......");
        Process process;
        try {
            log.debug("convertOffice2PDF cmd : {}", command);
            process = Runtime.getRuntime().exec(command);
        } catch (IOException e) {
            log.error(" convertOffice2PDF {} error", command, e);
            return false;
        }
        // 关键：读取子进程 stdout/stderr，否则管道缓冲写满会阻塞 soffice；输出同时留作诊断
        drainStream(process.getInputStream(), "stdout");
        drainStream(process.getErrorStream(), "stderr");
        int exitStatus = 0;
        try {
            boolean finished = process.waitFor(120, TimeUnit.SECONDS);
            if (!finished) {
                process.descendants().forEach(ProcessHandle::destroyForcibly);
                process.destroyForcibly();
                log.error("convertOffice2PDF 超时，已强制终止: {}", command);
                return false;
            }
            exitStatus = process.exitValue();
            log.debug("exitStatus----" + exitStatus);
        } catch (InterruptedException e) {
            Thread.currentThread().interrupt();
            process.destroyForcibly();
            log.error("InterruptedException  convertOffice2PDF {}", command, e);
            return false;
        }
        if (exitStatus != 0) {
            log.error("convertOffice2PDF cmd exitStatus {}", exitStatus);
        } else {
            log.debug("convertOffice2PDF cmd exitStatus {}", exitStatus);
        }
        process.destroy();
        log.info("转化结束.......");
        return true;
    }

    private static void drainStream(InputStream in, String tag) {
        Thread t = new Thread(() -> {
            try (BufferedReader reader = new BufferedReader(new InputStreamReader(in))) {
                String line;
                while ((line = reader.readLine()) != null) {
                    log.info("soffice[{}]: {}", tag, line);
                }
            } catch (IOException ignored) {
            }
        });
        t.setDaemon(true);
        t.start();
    }

    public static void main(String[] args) {
        try {
            // 调用转换方法，传入源PPTX文件路径和目标PDF文件路径
            String sourcePath = "E:\\digital\\ai_digital\\yudao-module-digital\\yudao-module-digital-biz\\src\\main\\resources\\file";
            convert2PDF(sourcePath+"\\"+"1724378887.00289_45.pptx", sourcePath);
            //System.out.println("PPT转PDF成功！");
        } catch (Exception e) {
            e.printStackTrace();
            //System.out.println("PPT转PDF失败：" + e.getMessage());
        }
    }


}
