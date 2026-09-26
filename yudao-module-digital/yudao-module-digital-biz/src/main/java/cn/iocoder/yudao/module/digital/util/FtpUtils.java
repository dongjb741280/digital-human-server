package cn.iocoder.yudao.module.digital.util;

import lombok.extern.slf4j.Slf4j;
import org.apache.commons.net.ftp.FTP;
import org.apache.commons.net.ftp.FTPClient;
import org.apache.commons.net.ftp.FTPFile;
import org.apache.commons.net.ftp.FTPReply;

import java.io.*;

@Slf4j
public class FtpUtils {

    private String username;

    private String password;

    private String hostname;

    private int port;

    public FtpUtils(String username, String password, String hostname, int port) {
        this.username = username;
        this.password = password;
        this.hostname = hostname;
        this.port = port;
    }

    /**
     * 初始化FTP服务器
     *
     * @return
     */
    public FTPClient getFtpClient() {
        FTPClient ftpClient = new FTPClient();
        ftpClient.setControlEncoding("UTF-8");
        try {
            //设置连接超时时间
            ftpClient.setDataTimeout(1000 * 120);
            log.info("连接FTP服务器中:" + hostname + ":" + port);
            //连接ftp服务器
            ftpClient.connect(hostname, port);
            //登录ftp服务器
            ftpClient.login(username, password);
            // 是否成功登录服务器
            int replyCode = ftpClient.getReplyCode();
            if (FTPReply.isPositiveCompletion(replyCode)) {
                log.info("连接FTP服务器成功:" + hostname + ":" + port);
            } else {
                log.error("连接FTP服务器失败:" + hostname + ":" + port);
                closeFtpClient(ftpClient);
            }
        } catch (IOException e) {
            log.error("连接ftp服务器异常", e);
        }
        return ftpClient;
    }


    /**
     * 上传文件
     *
     * @param pathName    路径
     * @param fileName    文件名
     * @param inputStream 输入文件流
     * @return
     */
    public boolean uploadFileToFtp(String pathName, String fileName, InputStream inputStream) {
        boolean isSuccess = false;
        FTPClient ftpClient = getFtpClient();
        try {
            if (ftpClient.isConnected()) {
                log.info("开始上传文件到FTP,文件名称:" + fileName);
                //设置上传文件类型为二进制，否则将无法打开文件
                ftpClient.setFileType(FTP.BINARY_FILE_TYPE);
                //路径切换，如果目录不存在创建目录
                if (!ftpClient.changeWorkingDirectory(pathName)) {
                    boolean flag = this.changeAndMakeWorkingDir(ftpClient, pathName);
                    if (!flag) {
                        log.error("路径切换(创建目录)失败");
                        return false;
                    }
                }
                //设置被动模式，文件传输端口设置(如上传文件夹成功，不能上传文件，注释这行，否则报错refused:connect)
                ftpClient.enterLocalPassiveMode();
                ftpClient.storeFile(fileName, inputStream);
                inputStream.close();
                ftpClient.logout();
                isSuccess = true;
                log.info(fileName + "文件上传到FTP成功");
            } else {
                log.error("FTP连接建立失败");
            }
        } catch (Exception e) {
            log.error(fileName + "文件上传异常", e);

        } finally {
            closeFtpClient(ftpClient);
            closeStream(inputStream);
        }
        return isSuccess;
    }


    public boolean uploadFileByteToFtp(String path, String fileName, byte[] inByte) {
        boolean isSuccess = false;
        FTPClient ftpClient = getFtpClient();
        try {
            if (ftpClient.isConnected()) {
                log.info("开始上传文件到FTP,文件名称:" + fileName);
                //设置上传文件类型为二进制，否则将无法打开文件
                ftpClient.setFileType(FTP.BINARY_FILE_TYPE);
                //路径切换，如果目录不存在创建目录
                if (!ftpClient.changeWorkingDirectory(path)) {
                    boolean flag = this.changeAndMakeWorkingDir(ftpClient, path);
                    if (!flag) {
                        log.error("路径切换(创建目录)失败");
                        return false;
                    }
                }
                //设置被动模式，文件传输端口设置(如上传文件夹成功，不能上传文件，注释这行，否则报错refused:connect)
                ftpClient.enterLocalPassiveMode();
                // 将字节数组包装为输入流
                ByteArrayInputStream byteArrayInputStream = new ByteArrayInputStream(inByte);
                // 上传文件
                isSuccess = ftpClient.storeFile(fileName, byteArrayInputStream);
                ftpClient.logout();
                log.info(fileName + "文件上传到FTP成功");
            } else {
                log.error("FTP连接建立失败");
            }
        } catch (Exception e) {
            log.error(fileName + "文件上传异常", e);

        } finally {
            closeFtpClient(ftpClient);
        }
        return isSuccess;
    }

    /**
     * 删除文件
     *
     * @param pathName 路径
     * @param fileName 文件名
     * @return
     */
    public boolean deleteFile(String pathName, String fileName) {
        boolean flag = false;
        FTPClient ftpClient = getFtpClient();
        try {
            log.info("开始删除文件");
            if (ftpClient.isConnected()) {
                //路径切换
                ftpClient.changeWorkingDirectory(pathName);
                ftpClient.enterLocalPassiveMode();
                ftpClient.dele(fileName);
                ftpClient.logout();
                flag = true;
                log.info("删除文件成功");
            } else {
                log.info("删除文件失败");
            }
        } catch (Exception e) {
            log.error(fileName + "文件删除异常", e);
        } finally {
            closeFtpClient(ftpClient);
        }
        return flag;
    }

    /**
     * 关闭FTP连接
     *
     * @param ftpClient
     */
    public void closeFtpClient(FTPClient ftpClient) {
        if (ftpClient.isConnected()) {
            try {
                ftpClient.disconnect();
            } catch (IOException e) {
                log.error("关闭FTP连接异常", e);
            }
        }
    }

    /**
     * 关闭文件流
     *
     * @param closeable
     */
    public void closeStream(Closeable closeable) {
        if (null != closeable) {
            try {
                closeable.close();
            } catch (IOException e) {
                log.error("关闭文件流异常", e);
            }
        }
    }

    /**
     * 路径切换（没有则创建）
     *
     * @param ftpClient FTP服务器
     * @param path      路径
     */
    public Boolean changeAndMakeWorkingDir(FTPClient ftpClient, String path) {
        boolean flag = false;
        try {
            String[] path_array = path.split("/");
            for (String s : path_array) {
                boolean b = ftpClient.changeWorkingDirectory(s);
                if (!b) {
                    ftpClient.makeDirectory(s);
                    ftpClient.changeWorkingDirectory(s);
                }
            }
            flag = true;
        } catch (IOException e) {
            log.error("路径切换异常", e);
        }
        return flag;
    }

    /**
     * 从FTP下载到本地文件夹
     *
     * @param ftpClient      FTP服务器
     * @param pathName       路径
     * @param targetFileName 文件名
     * @param localPath      本地路径
     * @return
     */
    public boolean downloadFile(FTPClient ftpClient, String pathName, String targetFileName, String localPath) {
        boolean flag = false;
        OutputStream os = null;
        try {
            System.out.println("开始下载文件");
            //切换FTP目录
            ftpClient.changeWorkingDirectory(pathName);
            ftpClient.enterLocalPassiveMode();
            FTPFile[] ftpFiles = ftpClient.listFiles();
            for (FTPFile file : ftpFiles) {
                String ftpFileName = file.getName();
                if (targetFileName.equalsIgnoreCase(ftpFileName)) {
                    File localFile = new File(localPath + targetFileName);
                    os = new FileOutputStream(localFile);
                    ftpClient.retrieveFile(file.getName(), os);
                    os.close();
                }
            }
            ftpClient.logout();
            flag = true;
            log.info("下载文件成功");
        } catch (Exception e) {
            log.error("下载文件失败", e);
        } finally {
            closeFtpClient(ftpClient);
            closeStream(os);
        }
        return flag;
    }

}
