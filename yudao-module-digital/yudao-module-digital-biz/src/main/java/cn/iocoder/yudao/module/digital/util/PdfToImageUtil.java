package cn.iocoder.yudao.module.digital.util;



import org.apache.pdfbox.pdmodel.PDDocument;
import org.apache.pdfbox.rendering.PDFRenderer;

import javax.imageio.ImageIO;
import java.awt.image.BufferedImage;
import java.io.*;
import java.util.ArrayList;
import java.util.List;

/**
 * @Author: 公众号：知识浅谈
 * @Description: pdf转图片工具类
 * @Version: 1.0
 */
public class PdfToImageUtil {
    /*dpi 越大图片越清晰*/
    private static final Integer DPI = 100;

    /*转换后图片类型  jpg*/
    private static final String IMG_TYPE = "png";

    /**
     * PDF转图片(多页)
     * @param fileContent pdf文件的二进制流
     * @return 图片文件的二进制流
     * @throws Exception
     */
    public static List<byte[]> pdfToImage(byte[] fileContent) throws Exception {
        List<byte[]> imageList = new ArrayList<byte[]>();
        PDDocument document=null;
        try{
            document = PDDocument.load(fileContent);
            PDFRenderer pdfRenderer = new PDFRenderer(document);
            for (int i = 0; i < document.getNumberOfPages(); i++) {
                BufferedImage bufferedImage = pdfRenderer.renderImageWithDPI(i, DPI);
                ByteArrayOutputStream out = new ByteArrayOutputStream();
                ImageIO.write(bufferedImage, IMG_TYPE, out);
                imageList.add(out.toByteArray());
            }
        }finally {
            document.close();
        }
        return imageList;
    }


    /**
     * PDF转图片(单页)
     * @param fileContent pdf文件的二进制流
     * @return 图片文件的二进制流
     * @throws Exception
     */
    public static byte[] pdfToImageOne(byte[] fileContent) throws Exception {
        byte[] image = null;
        PDDocument document=null;
        try{
            document = PDDocument.load(fileContent);
            PDFRenderer pdfRenderer = new PDFRenderer(document);
            BufferedImage bufferedImage = pdfRenderer.renderImageWithDPI(0, DPI);
            ByteArrayOutputStream out = new ByteArrayOutputStream();
            ImageIO.write(bufferedImage, IMG_TYPE, out);
            image = out.toByteArray();
        }finally {
            document.close();
        }
        return image;
    }

    public static Boolean saveImage(byte[] imageBytes, String savePath,String fileName) throws Exception {
        try {
            BufferedImage image = ImageIO.read(new ByteArrayInputStream(imageBytes));
            File directory = new File(savePath);
            if(!directory.exists()){
                directory.mkdirs();
            }
            File file1 = new File(directory,fileName);
            return ImageIO.write(image, IMG_TYPE, file1);
        } catch (IOException e) {
            System.out.println("保存文件错误");
            return false;
        }
    }

    public static void main(String[] args) {
        try {
            File file = new File("D:\\test\\1722256848.054918.pdf");
            FileInputStream fileInputStream = new FileInputStream(file);
            byte[] pdfBytes = new byte[(int) file.length()];
            fileInputStream.read(pdfBytes);
            fileInputStream.close();

            List<byte[]>  list = PdfToImageUtil.pdfToImage(pdfBytes);
            int i = 0;
            for (byte[] bytes : list){
                String fileStr = i+ "test.png";
                saveImage(bytes,"D:\\test\\",fileStr);
                i++;
                System.out.println(fileStr);
            }

        }catch (Exception e){
            System.out.println("pdf转图片异常{}" + e.getMessage());
        }

    }


}

