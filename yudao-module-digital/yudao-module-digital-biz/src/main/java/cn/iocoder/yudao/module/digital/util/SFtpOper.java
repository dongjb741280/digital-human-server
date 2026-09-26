package cn.iocoder.yudao.module.digital.util;

import com.jcraft.jsch.*;
import lombok.extern.slf4j.Slf4j;
import org.apache.poi.util.IOUtils;

import java.io.*;
import java.util.ArrayList;
import java.util.List;
import java.util.Properties;

@Slf4j
public class SFtpOper {


    public static final String NO_FILE = "No such file";

    private ChannelSftp sftp = null;

    private Session sshSession = null;

    private String username;

    private String password;

    private String host;

    private int port;

    public SFtpOper(String username, String password, String host, int port) {
        this.username = username;
        this.password = password;
        this.host = host;
        this.port = port;
    }

    /**
     * 连接sftp服务器
     *
     * @return ChannelSftp sftp类型
     * @throws
     */
    public ChannelSftp connect() throws JSchException {
        log.info("SSFtpUtil-->connect--ftp连接开始>>>>>>");
        JSch jsch = new JSch();
        try {
            jsch.getSession(username, host, port);
            sshSession = jsch.getSession(username, host, port);
            /*log.info("sftp---Session created.");*/
            sshSession.setPassword(password);
            Properties properties = new Properties();
            properties.put("StrictHostKeyChecking", "no");
            sshSession.setConfig(properties);
            sshSession.connect();
            /*log.info("sftp---Session connected.");*/
            Channel channel = sshSession.openChannel("sftp");
            channel.connect();
            /*log.info("Opening Channel.");*/
            sftp = (ChannelSftp) channel;
            /*log.info("sftp---Connected to {}", host);*/
        } catch (JSchException e) {
            throw new JSchException("SSFtpUtil-->connect异常" + e.getMessage());
        }
        return sftp;
    }

    /**
     * 载单个文件
     *
     * @param directory      ：远程下载目录(以路径符号结束)
     * @param remoteFileName FTP服务器文件名称 如：xxx.txt ||xxx.txt.zip
     * @param localFile      本地文件路径 如 D:\\xxx.txt
     * @return
     * @throws JSchException
     */
    public File downloadFile(String directory, String remoteFileName, String localFile) throws JSchException {
        log.info(">>>>>>>>SFtpUtil-->downloadFile--ftp下载文件,{},开始>>>>>>>>>>>>>", remoteFileName);
        connect();
        File file = null;
        OutputStream output = null;
        try {
            file = new File(localFile);
            if (file.exists()) {
                file.delete();
            }
            file.createNewFile();
            sftp.cd(directory);
            output = new FileOutputStream(file);
            sftp.get(remoteFileName, output);
            log.info("===DownloadFile: {} success from sftp.", remoteFileName);
        } catch (SftpException e) {
            if (NO_FILE.equals(e.toString())) {
                log.info(
                        ">>>>>>>>SFtpUtil-->downloadFile--ftpDownLoadFileFailure,{},{},不存在>>>>>>>>>>>>>", directory, remoteFileName);
                throw new JSchException("SFtpUtil-->downloadFile--ftpDownLoadFileFailure" + directory + remoteFileName + "不存在");
            }
            throw new JSchException("ftp目录或者文件异常，检查ftp目录和文件" + e.toString());
        } catch (FileNotFoundException e) {
            throw new JSchException("本地目录异常，请检查" + file.getPath() + e.getMessage());
        } catch (IOException e) {
            throw new JSchException("CreateLoaclFileFailure" + file.getPath() + e.getMessage());
        } finally {
            if (output != null) {
                try {
                    output.close();
                } catch (IOException e) {
                    throw new JSchException("Close stream error." + e.getMessage());
                }
            }
            disconnect();
        }

        log.info(">>>>>>>>SFtpUtil-->downloadFile--ftpDownloadFileEnd>>>>>>>>>>>>>");
        return file;
    }

    /**
     * 上传单个文件
     *
     * @param directory      ：远程下载目录(以路径符号结束)
     * @param uploadFilePath 要上传的文件 如：D:\\test\\xxx.txt
     * @param fileName       FTP服务器文件名称 如：xxx.txt ||xxx.txt.zip
     * @throws JSchException
     */
    public boolean uploadFile(String directory, String fileName, String uploadFilePath) throws JSchException {

        boolean tag = false;
        log.info(">>>>>>>>SFtpUtil-->uploadFile--ftp上传文件开始>>>>>>>>>>>>>");
        connect();
        try {
            sftp.cd(directory);
        } catch (SftpException e) {
            try {
                sftp.mkdir(directory);
                sftp.cd(directory);
            } catch (SftpException e1) {
                throw new JSchException("ftp创建文件路径失败，路径为" + directory);
            }

        }
        File file = new File(uploadFilePath);
        try (FileInputStream in = new FileInputStream(file)) {
            sftp.put(in, fileName);
            tag = true;
        } catch (Exception e) {
            throw new JSchException("文件不存在-->" + uploadFilePath);
        } finally {
            disconnect();
        }
        log.info(">>>>>>>>SFtpUtil-->uploadFile--ftp上传文件结束>>>>>>>>>>>>>");
        return tag;
    }

    public boolean uploadFile(String directory, File file) throws JSchException {
        boolean tag = false;
        if (null == file) {
            log.error("文件不能为空");
            return tag;
        }

        log.info(">>>>>>>>SFtpUtil-->uploadFile--ftp上传文件开始>>>>>>>>>>>>>");
        connect();
        try {
            sftp.cd(directory);
        } catch (SftpException e) {
            try {
                sftp.mkdir(directory);
                sftp.cd(directory);
            } catch (SftpException e1) {
                throw new JSchException("ftp创建文件路径失败，路径为" + directory);
            }

        }
        try (FileInputStream in = new FileInputStream(file)) {
            String fileName = file.getName();
            sftp.put(in, fileName);
            tag = true;
        } catch (Exception e) {
            throw new JSchException("文件不存在-->" + file.getPath());
        } finally {
            disconnect();
        }
        log.info(">>>>>>>>SFtpUtil-->uploadFile--ftp上传文件结束>>>>>>>>>>>>>");
        return tag;
    }

    public boolean uploadFileContent(String directory, String fileName, byte[] bytes) throws JSchException {
        log.info(">>>>>>>>SFtpUtil-->uploadFile--uploadFileContent>>>>>>>>>>>>>");
        boolean tag = false;

        connect();
        OutputStream put = null;
        try {
            sftp.cd(directory);
        } catch (SftpException e) {
            try {
                sftp.mkdir(directory);
                sftp.cd(directory);
            } catch (SftpException e1) {
                log.error("ftp create directory failed,the dir.{}", directory, e);
            }

        }
        try {
            put = sftp.put(fileName);
            put.write(bytes);

            tag = true;
        } catch (SftpException | IOException e) {
            log.error("Close stream error.", e);
        } finally {
            if (put != null) {
                try {
                    put.close();
                } catch (IOException e) {
                    log.error("Close stream error.", e);
                }
            }
            disconnect();
        }
        log.info(">>>>>>>>SFtpUtil-->uploadFile--ftp--->uploadFileContent-->End>>>>>>>>>>>>>");
        return tag;
    }

    /**
     * 上传单个文件
     *
     * @param directory ：远程下载目录(以路径符号结束)
     * @param directory 要上传的文件 如：D:\\test\\xxx.txt ,ftp的文件名;
     * @param fileName  in
     * @throws JSchException
     */
    public boolean uploadFile(String directory, String fileName, InputStream in) throws JSchException {
        log.info(">>>>>>>>SFtpUtil-->uploadFile--ftpuploafFileBegin>>>>>>>>>>>>>");
        boolean tag = false;

        connect();
        try {
            sftp.cd(directory);
        } catch (SftpException e) {
            try {
                /*sftp.mkdir(directory);
                sftp.cd(directory);*/
                List<String> directories = new ArrayList<>();
                String[] parts = directory.split("/");
                for (String part : parts) {
                    if (!part.isEmpty()) {
                        directories.add(part);
                    }
                }
                StringBuilder currentPath = new StringBuilder();
                for (String dir : directories) {
                    currentPath.append("/").append(dir);
                    try {
                        sftp.cd(currentPath.toString()); // Try to enter the directory
                    } catch (SftpException a) {
                        if (a.id == ChannelSftp.SSH_FX_NO_SUCH_FILE) {
                            sftp.mkdir(currentPath.toString()); // Directory does not exist, so create it
                            sftp.cd(currentPath.toString()); // Enter the newly created directory
                        } else {
                            throw e; // Re-throw the exception if it is not about missing directory
                        }
                    }
                }
            } catch (SftpException e1) {
                log.error("ftp create directory failed,the dir.{}", directory, e);
            }
        }
        try {
            OutputStream put = sftp.put(fileName);
            IOUtils.copy(in,put);
            put.close();
            tag = true;
        } catch (SftpException | IOException e) {
            log.error("Close stream error.", e);
        } finally {
            if (in != null) {
                try {
                    in.close();
                } catch (IOException e) {
                    log.error("Close stream error.", e);
                }
            }
            disconnect();
        }
        log.info(">>>>>>>>SFtpUtil-->uploadFile--ftp--->UploafFile-->End>>>>>>>>>>>>>");
        return tag;
    }


    public boolean uploadFileVoice(String directory, String fileName, InputStream in) throws JSchException {
        log.info(">>>>>>>>SFtpUtil-->uploadFile--ftpuploafFileBegin>>>>>>>>>>>>>");
        boolean tag = false;

        connect();
        try {
            sftp.cd(directory);
        } catch (SftpException e) {
            try {
                sftp.mkdir(directory);
                sftp.cd(directory);
            } catch (SftpException e1) {
                log.error("ftp create directory failed,the dir.{}", directory, e);
            }

        }
        try {
            OutputStream put = sftp.put(fileName);
            IOUtils.copy(in,put);
            put.close();
            tag = true;
        } catch (SftpException | IOException e) {
            log.error("Close stream error.", e);
        } finally {
            if (in != null) {
                try {
                    in.close();
                } catch (IOException e) {
                    log.error("Close stream error.", e);
                }
            }
            disconnect();
        }
        log.info(">>>>>>>>SFtpUtil-->uploadFile--ftp--->UploafFile-->End>>>>>>>>>>>>>");
        return tag;
    }

    public boolean uploadFilePhone(String directory, String fileName, FileInputStream in) throws JSchException {
        log.info(">>>>>>>>SFtpUtil-->uploadFile--ftpuploafFileBegin>>>>>>>>>>>>>");
        boolean tag = false;

        connect();
        try {
            sftp.cd(directory);
        } catch (SftpException e) {
            try {
                sftp.mkdir(directory);
                sftp.cd(directory);
            } catch (SftpException e1) {
                log.error("ftp create directory failed,the dir.{}", directory, e);
            }

        }
        try {
            OutputStream put = sftp.put(fileName);
            IOUtils.copy(in,put);
            put.close();
            tag = true;
        } catch (SftpException | IOException e) {
            log.error("Close stream error.", e);
        } finally {
            if (in != null) {
                try {
                    in.close();
                } catch (IOException e) {
                    log.error("Close stream error.", e);
                }
            }
            disconnect();
        }
        log.info(">>>>>>>>SFtpUtil-->uploadFile--ftp--->UploafFile-->End>>>>>>>>>>>>>");
        return tag;
    }

    /**
     * 关闭连接
     */
    public void disconnect() {
        if (this.sftp != null) {
            if (this.sftp.isConnected()) {
                this.sftp.disconnect();
                this.sftp = null;
                log.info("sftp is closed already");
            }
        }
        if (this.sshSession != null) {
            if (this.sshSession.isConnected()) {
                this.sshSession.disconnect();
                this.sshSession = null;
                log.info("sshSession is closed already");
            }
        }
    }

    public InputStream getInput(String sftpPath) throws Exception{
        return sftp.get(sftpPath);
    }
}
