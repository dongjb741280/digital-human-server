package cn.iocoder.yudao.module.digital.util;



import lombok.extern.slf4j.Slf4j;

import java.io.BufferedReader;
import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;

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
        // 每次用独立 profile，避免与 GUI/并发实例抢锁导致 soffice 卡死；
        // 滤镜去掉 writer_pdf_Export，改自动检测（对 pptx 会正确走 impress_pdf_Export）
        String userInstallation = "file://" + System.getProperty("java.io.tmpdir") + "lo_profile_" + System.currentTimeMillis();
        if (osName.contains("Windows")) {
            command = "cmd /c start soffice --headless --invisible -env:UserInstallation=" + userInstallation + " --convert-to pdf " + inputFile + " --outdir " + pdfFile;
        }else {
            command = "/Applications/LibreOffice.app/Contents/MacOS/soffice --headless --invisible -env:UserInstallation=" + userInstallation + " --convert-to pdf " + inputFile + " --outdir " + pdfFile;
        }
        // 输出 PDF 与输入同名（.pdf 后缀），soffice 生成到 outdir
        String inputName = new File(inputFile).getName();
        String pdfName = inputName.substring(0, inputName.lastIndexOf('.')) + ".pdf";
        File outputPdf = new File(pdfFile, pdfName);
        flag = executeAndWaitForPdf(command, outputPdf);
        long end = System.currentTimeMillis();

        log.debug("用时:{} ms", end - start);
        return flag;
    }


    /**
     * 执行命令：启动 soffice 后轮询 PDF 是否产出，而非等进程退出。
     * macOS + 高版本 JDK 下 soffice 转换完成后进程可能不退出，导致 waitFor 一直超时。
     */
    private static boolean executeAndWaitForPdf(String command, File outputPdf) {
        log.info("开始进行转化.......");
        Process process;
        try {
            log.debug("convertOffice2PDF cmd : {}", command);
            process = Runtime.getRuntime().exec(command);
        } catch (IOException e) {
            log.error(" convertOffice2PDF {} error", command, e);
            return false;
        }
        // 读取子进程 stdout/stderr，防管道缓冲写满阻塞，同时留作诊断
        drainStream(process.getInputStream(), "stdout");
        drainStream(process.getErrorStream(), "stderr");
        long deadline = System.currentTimeMillis() + 30_000;
        long lastSize = -1;
        while (System.currentTimeMillis() < deadline) {
            if (outputPdf.exists() && outputPdf.length() > 0) {
                long size = outputPdf.length();
                if (size == lastSize) {
                    // 大小连续两次一致，认为写入完成
                    process.destroyForcibly();
                    log.info("转化结束.......");
                    return true;
                }
                lastSize = size;
            } else if (!process.isAlive()) {
                log.error("convertOffice2PDF 进程退出且未产出 PDF，exitStatus {}", process.exitValue());
                return false;
            }
            try {
                Thread.sleep(500);
            } catch (InterruptedException e) {
                Thread.currentThread().interrupt();
                process.destroyForcibly();
                return false;
            }
        }
        process.descendants().forEach(ProcessHandle::destroyForcibly);
        process.destroyForcibly();
        log.error("convertOffice2PDF 超时，已强制终止: {}", command);
        return false;
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
