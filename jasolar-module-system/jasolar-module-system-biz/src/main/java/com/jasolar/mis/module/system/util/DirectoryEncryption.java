package com.jasolar.mis.module.system.util;

import lombok.extern.slf4j.Slf4j;

import javax.crypto.Cipher;
import javax.crypto.SecretKey;
import javax.crypto.SecretKeyFactory;
import javax.crypto.spec.GCMParameterSpec;
import javax.crypto.spec.PBEKeySpec;
import javax.crypto.spec.SecretKeySpec;
import java.io.*;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.nio.file.StandardCopyOption;
import java.security.SecureRandom;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.UUID;
import java.util.stream.Stream;
import java.util.zip.ZipEntry;
import java.util.zip.ZipInputStream;
import java.util.zip.ZipOutputStream;

@Slf4j
public class DirectoryEncryption {
    private static final String ALGORITHM = "AES/GCM/NoPadding";
    private static final int GCM_TAG_LENGTH = 128;
    private static final int GCM_IV_LENGTH = 12;
    private static final int SALT_LENGTH = 32;
    private static final String MAGIC_HEADER = "SECURE_DIR_V1";

    /**
     * 加密整个目录
     *
     * @param sourceDir  源目录路径
     * @param outputFile 输出的加密文件路径
     * @param password   加密密码
     */
    public static void encryptDirectory(String sourceDir, String outputFile, String password)
            throws Exception {
        // 验证并规范化输入路径
        Path sourcePath = Paths.get(sourceDir).toAbsolutePath().normalize();
        if (!Files.isDirectory(sourcePath)) {
            throw new IllegalArgumentException("源路径必须是一个目录: " + sourceDir);
        }

        // 验证输出文件路径安全性
        Path outputFilePath = Paths.get(outputFile).toAbsolutePath().normalize();
        // 确保输出路径不在系统敏感目录
        validateOutputPath(outputFilePath);

        // 生成加密参数
        byte[] salt = generateRandomBytes(SALT_LENGTH);
        byte[] iv = generateRandomBytes(GCM_IV_LENGTH);
        SecretKey key = generateStrongKey(password, salt);

        // 初始化加密器
        Cipher cipher = Cipher.getInstance(ALGORITHM);
        GCMParameterSpec gcmSpec = new GCMParameterSpec(GCM_TAG_LENGTH, iv);
        cipher.init(Cipher.ENCRYPT_MODE, key, gcmSpec);

        // 创建临时ZIP文件
        Path tempZip = Files.createTempFile("temp_", ".zip");
        try {
            // 首先将目录打包为ZIP
            try (ZipOutputStream zos = new ZipOutputStream(new FileOutputStream(tempZip.toFile()))) {
                List<Path> files = new ArrayList<>();
                try (Stream<Path> walkStream = Files.walk(sourcePath)) {
                    walkStream
                            .filter(path -> !Files.isDirectory(path))
                            .forEach(files::add);
                }
                for (Path path : files) {
                    try {
                        String relativePath = sourcePath.relativize(path).toString();
                        ZipEntry entry = new ZipEntry(relativePath);
                        zos.putNextEntry(entry);
                        Files.copy(path, zos);
                        zos.closeEntry();
                    } catch (IOException e) {
                        throw new RuntimeException("打包文件时出错: " + path, e);
                    }
                }
            } catch (Exception e){
                log.info("打包文件时出错: " + e.getMessage());
            }

            // 读取ZIP文件内容
            byte[] zipContent = Files.readAllBytes(tempZip);

            // 加密ZIP内容
            byte[] encryptedContent = cipher.doFinal(zipContent);

            // 写入最终的加密文件
            try (FileOutputStream fos = new FileOutputStream(outputFile)) {
                // 写入魔数
                fos.write(MAGIC_HEADER.getBytes(StandardCharsets.UTF_8));

                // 写入加密参数
                fos.write(salt);
                fos.write(iv);

                // 写入加密后的数据长度和数据
                writeInt(fos, encryptedContent.length);
                fos.write(encryptedContent);
            }

            log.info("目录加密完成: " + outputFile);
            try (Stream<Path> walkStream = Files.walk(sourcePath)) {
                log.info("处理的文件数量: " +
                        walkStream.filter(p -> !Files.isDirectory(p)).count());
            }

        } finally {
            // 清理临时文件
            Files.deleteIfExists(tempZip);
        }
    }

    /**
     * 解密并还原目录
     *
     * @param encryptedFile 加密的文件路径
     * @param outputDir     输出目录路径
     * @param password      解密密码
     */
    public static void decryptDirectory(String encryptedFile, String outputDir, String password)
            throws Exception {
        // 验证并规范化输入路径
        Path encryptedFilePath = Paths.get(encryptedFile).toAbsolutePath().normalize();
        if (!Files.exists(encryptedFilePath) || Files.isDirectory(encryptedFilePath)) {
            throw new IllegalArgumentException("加密文件不存在或不是文件: " + encryptedFile);
        }

        // 创建并验证输出目录
        Path outputPath = Paths.get(outputDir).toAbsolutePath().normalize();
        validateOutputPath(outputPath);
        Files.createDirectories(outputPath);

        try (FileInputStream fis = new FileInputStream(encryptedFile)) {
            // 验证文件格式
            byte[] magicBytes = new byte[MAGIC_HEADER.length()];
            if (fis.read(magicBytes) != MAGIC_HEADER.length() ||
                    !Arrays.equals(magicBytes, MAGIC_HEADER.getBytes(StandardCharsets.UTF_8))) {
                throw new IllegalArgumentException("无效的加密文件格式");
            }

            // 读取加密参数
            byte[] salt = new byte[SALT_LENGTH];
            int read = fis.read(salt);
            if (read != SALT_LENGTH) {
                throw new IllegalArgumentException("无效的加密文件格式");
            }
            byte[] iv = new byte[GCM_IV_LENGTH];
            int read1 = fis.read(iv);
            if (read1 != GCM_IV_LENGTH) {
                throw new IllegalArgumentException("无效的加密文件格式");
            }

            // 重新生成密钥
            SecretKey key = generateStrongKey(password, salt);

            // 初始化解密器
            Cipher cipher = Cipher.getInstance(ALGORITHM);
            GCMParameterSpec gcmSpec = new GCMParameterSpec(GCM_TAG_LENGTH, iv);
            cipher.init(Cipher.DECRYPT_MODE, key, gcmSpec);

            // 读取加密数据
            int dataLength = readInt(fis);
            byte[] encryptedData = new byte[dataLength];
            int read2 = fis.read(encryptedData);
            if (read2 != dataLength) {
                throw new IllegalArgumentException("无效的加密文件格式");
            }

            // 解密数据
            byte[] decryptedData = cipher.doFinal(encryptedData);

            // 创建临时ZIP文件
            Path tempZip = Files.createTempFile("dec_", ".zip");
            fis.close();
            try {
                // 写入解密后的ZIP数据
                Files.write(tempZip, decryptedData);

                // 解压ZIP文件
                int fileCount = 0;
                try (ZipInputStream zis = new ZipInputStream(new FileInputStream(tempZip.toFile()))) {
                    ZipEntry entry;
                    while ((entry = zis.getNextEntry()) != null) {
                        // 预处理文件名，移除路径操作符
                        String entryName = sanitizeEntryName(entry.getName());
                        if (entryName.isEmpty()) {
                            log.warn("跳过空文件名或不安全的文件路径");
                            continue;
                        }

                        // 解析并规范化路径
                        Path filePath = outputPath.resolve(entryName).normalize();

                        // 安全检查：确保路径不越界
                        if (!filePath.startsWith(outputPath.normalize())) {
                            log.warn("检测到路径穿越尝试，跳过文件: {}", entryName);
                            continue;
                        }

                        // 创建父目录
                        Files.createDirectories(filePath.getParent());

                        // 解压文件
                        if (!entry.isDirectory()) {
                            Files.copy(zis, filePath, StandardCopyOption.REPLACE_EXISTING);
                            fileCount++;
                        } else {
                            log.info("已创建目录: " + filePath); // 日志输出
                        }
                        zis.closeEntry();
                    }
                }catch (Exception  e){
                    log.error("解压文件时出错: " + e.getMessage());
                }

                log.info("目录解密完成: " + outputDir);
                log.info("解压的文件数量: " + fileCount);

            } finally {
                // 清理临时文件
                Files.deleteIfExists(tempZip);
            }

        }
    }

    /**
     * 清理和验证ZIP条目名称，防止路径穿越攻击
     */
    private static String sanitizeEntryName(String entryName) {
        // 过滤掉绝对路径
        if (entryName.startsWith("/") || entryName.startsWith("\\")) {
            return "";
        }

        // 移除所有特殊路径组件
        String[] parts = entryName.split("[/\\\\]");
        StringBuilder builder = new StringBuilder();
        boolean first = true;

        for (String part : parts) {
            // 跳过空路径段、当前目录标记和父目录引用
            if (part.isEmpty() || ".".equals(part) || "..".equals(part)) {
                continue;
            }

            // 跳过可能导致问题的特殊文件名
            if (part.contains(":") || part.startsWith("~")) {
                continue;
            }

            if (!first) {
                builder.append(File.separator);
            } else {
                first = false;
            }
            builder.append(part);
        }

        return builder.toString();
    }

    private static boolean isSafeExtractPath(Path filePath, Path targetDir) {
        // 额外检查ZipEntry是否为符号链接（防止符号链接攻击）
        if (Files.isSymbolicLink(targetDir)) {
            throw new SecurityException("拒绝处理符号链接: " + targetDir);
        }
        return filePath.normalize().startsWith(targetDir.normalize());
    }

    // 生成强密钥
    private static SecretKey generateStrongKey(String password, byte[] salt) throws Exception {
        SecretKeyFactory factory = SecretKeyFactory.getInstance("PBKDF2WithHmacSHA512");
        PBEKeySpec spec = new PBEKeySpec(password.toCharArray(), salt, 200000, 256);
        byte[] keyBytes = factory.generateSecret(spec).getEncoded();
        return new SecretKeySpec(keyBytes, "AES");
    }

    // 生成随机字节
    private static byte[] generateRandomBytes(int length) {
        byte[] bytes = new byte[length];
        new SecureRandom().nextBytes(bytes);
        return bytes;
    }

    // 写入32位整数
    private static void writeInt(OutputStream out, int value) throws IOException {
        out.write((value >>> 24) & 0xFF);
        out.write((value >>> 16) & 0xFF);
        out.write((value >>> 8) & 0xFF);
        out.write(value & 0xFF);
    }

    // 读取32位整数
    private static int readInt(InputStream in) throws IOException {
        return ((in.read() & 0xFF) << 24) |
                ((in.read() & 0xFF) << 16) |
                ((in.read() & 0xFF) << 8) |
                (in.read() & 0xFF);
    }

    /**
     * 验证输出路径的安全性
     *
     * @param path 要验证的路径
     */
    private static void validateOutputPath(Path path) {
        // 检查输出路径是否在敏感系统目录中
        String pathStr = path.toString().toLowerCase();
        String[] sensitiveDirs = {
            "/etc", "/bin", "/sbin", "/var", "/boot", "/dev", "/lib", "/proc", "/sys",  // Linux
            "c:\\windows", "c:\\program files", "c:\\program files (x86)", "c:\\boot"    // Windows
        };

        for (String dir : sensitiveDirs) {
            if (pathStr.startsWith(dir.toLowerCase())) {
                throw new SecurityException("不允许在系统目录中创建文件: " + path);
            }
        }

        // 检查路径长度
        if (path.toString().length() > 255) {
            throw new IllegalArgumentException("路径长度过长: " + path);
        }
    }

    // 创建临时文件时的安全改进
    private static Path createSecureTempFile(String prefix, String suffix) throws IOException {
        // 获取系统临时目录
        Path tempDir = Paths.get(System.getProperty("java.io.tmpdir")).normalize();
        // 创建应用专属临时目录
        Path secureDir = tempDir.resolve("jasolar_encryption_temp");
        Files.createDirectories(secureDir);

        // 创建随机文件名
        String randomName = prefix + UUID.randomUUID().toString() + suffix;
        Path tempFile = secureDir.resolve(randomName);

        return Files.createFile(tempFile);
    }
}
