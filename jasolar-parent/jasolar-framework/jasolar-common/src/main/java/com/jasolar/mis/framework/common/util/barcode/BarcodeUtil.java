package com.jasolar.mis.framework.common.util.barcode;

import com.google.zxing.BarcodeFormat;
import com.google.zxing.Writer;
import com.google.zxing.WriterException;
import com.google.zxing.client.j2se.MatrixToImageWriter;
import com.google.zxing.common.BitMatrix;
import com.google.zxing.oned.Code128Writer;
import com.google.zxing.oned.EAN13Writer;
import com.google.zxing.qrcode.QRCodeWriter;
import lombok.extern.slf4j.Slf4j;

import javax.imageio.ImageIO;
import java.awt.image.BufferedImage;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.net.URI;
import java.net.URL;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.Base64;

/**
 * 条形码工具类
 */
@Slf4j
public class BarcodeUtil {

    /**
     * 生成Code128条形码图片文件
     * @param content 条形码内容
     * @param width 图片宽度
     * @param height 图片高度
     * @param fileName 保存路径
     */
    public static void generateCode128Image(String content, int width, int height, String fileName) {
        try {
            Code128Writer writer = new Code128Writer();
            BitMatrix bitMatrix = writer.encode(content, BarcodeFormat.CODE_128, width, height);

            // 使用URI来获取路径，避免路径中的不合法字符
            URL resourcesUrl = BarcodeUtil.class.getClassLoader().getResource("");
            if (resourcesUrl == null) {
                throw new IOException("无法获取资源路径");
            }

            // 将URL转换为URI，然后获取路径
            URI resourcesUri = resourcesUrl.toURI();
            String resourcesPath = Paths.get(resourcesUri).toString();

            // 构建文件路径
            Path filePath = Paths.get(resourcesPath, "images", fileName);

            // 确保 images 目录存在
            Files.createDirectories(filePath.getParent());
            // 写入文件
            MatrixToImageWriter.writeToPath(bitMatrix, "PNG", filePath);
            log.info("生成条形码图片成功：{}", filePath);
        } catch (Exception ex) {
            log.error("生成条形码图片失败", ex);
            throw new RuntimeException(ex);
        }
    }

    /**
     * 生成EAN-13条形码图片文件
     * @param content 条形码内容(12位数字+1位校验码或13位数字)
     * @param width 图片宽度
     * @param height 图片高度
     * @param filePath 保存路径
     */
    public static void generateEAN13Image(String content, int width, int height, String filePath) throws WriterException, IOException {
        EAN13Writer writer = new EAN13Writer();
        BitMatrix bitMatrix = writer.encode(content, BarcodeFormat.EAN_13, width, height);
        MatrixToImageWriter.writeToPath(bitMatrix, "PNG", Paths.get(filePath));
    }

    /**
     * 生成QR二维码图片文件
     * @param content 二维码内容
     * @param width 图片宽度
     * @param height 图片高度
     * @param filePath 保存路径
     */
    public static void generateQRCodeImage(String content, int width, int height, String filePath) throws WriterException, IOException {
        QRCodeWriter writer = new QRCodeWriter();
        BitMatrix bitMatrix = writer.encode(content, BarcodeFormat.QR_CODE, width, height);
        MatrixToImageWriter.writeToPath(bitMatrix, "PNG", Paths.get(filePath));
    }

    /**
     * 生成Code128条形码并返回Base64编码
     * @param content 条形码内容
     * @param width 图片宽度
     * @param height 图片高度
     * @return Base64编码的图片数据
     */
    public static String generateCode128Base64(String content, int width, int height) {
        try {
            Code128Writer writer = new Code128Writer();
            BitMatrix bitMatrix = writer.encode(content, BarcodeFormat.CODE_128, width, height);
            return convertToBase64(bitMatrix);
        } catch (Exception ex) {
            log.error("生成条形码图片失败", ex);
            throw new RuntimeException(ex);
        }

    }

    /**
     * 生成条形码并返回BufferedImage对象
     * @param content 内容
     * @param format 条形码格式
     * @param width 宽度
     * @param height 高度
     * @return BufferedImage对象
     */
    public static BufferedImage generateBarcodeImage(String content, BarcodeFormat format, int width, int height) throws WriterException {
        Writer writer;
        switch (format) {
            case EAN_13:
                writer = new EAN13Writer();
                break;
            case QR_CODE:
                writer = new QRCodeWriter();
                break;
            case CODE_128:
            default:
                writer = new Code128Writer();
                break;
        }
        BitMatrix bitMatrix = writer.encode(content, format, width, height);
        return MatrixToImageWriter.toBufferedImage(bitMatrix);
    }

    private static String convertToBase64(BitMatrix bitMatrix) throws IOException {
        BufferedImage image = MatrixToImageWriter.toBufferedImage(bitMatrix);
        ByteArrayOutputStream baos = new ByteArrayOutputStream();
        ImageIO.write(image, "PNG", baos);
        return Base64.getEncoder().encodeToString(baos.toByteArray());
    }

    /**
     * 验证EAN-13校验码
     * @param ean13Code 13位EAN码
     * @return 是否有效
     */
    public static boolean validateEAN13(String ean13Code) {
        if (ean13Code == null || ean13Code.length() != 13) {
            return false;
        }

        try {
            int sum = 0;
            for (int i = 0; i < 12; i++) {
                int digit = Character.getNumericValue(ean13Code.charAt(i));
                sum += (i % 2 == 0) ? digit : digit * 3;
            }
            int checksum = (10 - (sum % 10)) % 10;
            int lastDigit = Character.getNumericValue(ean13Code.charAt(12));
            return checksum == lastDigit;
        } catch (NumberFormatException e) {
            return false;
        }
    }
}