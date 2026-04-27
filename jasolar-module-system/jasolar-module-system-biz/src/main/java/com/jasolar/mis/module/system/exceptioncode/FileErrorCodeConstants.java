package com.jasolar.mis.module.system.exceptioncode;

import com.jasolar.mis.framework.common.exception.ErrorCode;

/**
 * Description: 文件上传下载模块错误码常量
 * Author : jasolar
 * Date : 25/08/2025 16:00
 * Version : 1.0
 */
public interface FileErrorCodeConstants {

    String I18B_PREFIX = "err.file.";

    // 文件上传相关错误码
    FileErrorCode FILE_UPLOAD_EMPTY = new FileErrorCode("8001", "上传文件不能为空");
    
    FileErrorCode FILE_UPLOAD_SIZE_EXCEED = new FileErrorCode("8002", "文件大小超出限制");
    
    FileErrorCode FILE_UPLOAD_TYPE_NOT_ALLOWED = new FileErrorCode("8003", "文件类型不允许上传");

    FileErrorCode FILE_UPLOAD_PATH_NOT_EXIST = new FileErrorCode("8006", "文件上传路径不存在");
    
    FileErrorCode FILE_UPLOAD_PATH_NO_PERMISSION = new FileErrorCode("8007", "文件上传路径无权限");
    
    FileErrorCode FILE_UPLOAD_DISK_FULL = new FileErrorCode("8008", "磁盘空间不足");
    
    FileErrorCode FILE_UPLOAD_NAME_INVALID = new FileErrorCode("8009", "文件名包含非法字符");
    
    FileErrorCode FILE_UPLOAD_NAME_TOO_LONG = new FileErrorCode("8010", "文件名过长");

    // 文件下载相关错误码
    FileErrorCode FILE_DOWNLOAD_NOT_EXIST = new FileErrorCode("8011", "要下载的文件不存在");
    

    // Excel导入导出相关错误码
    FileErrorCode EXCEL_IMPORT_EMPTY = new FileErrorCode("8018", "Excel文件内容为空");
    
    FileErrorCode EXCEL_IMPORT_FORMAT_ERROR = new FileErrorCode("8019", "Excel文件格式错误");
    
    FileErrorCode EXCEL_IMPORT_HEADER_MISMATCH = new FileErrorCode("8020", "Excel表头与模板不匹配");
    
    FileErrorCode EXCEL_IMPORT_DATA_INVALID = new FileErrorCode("8021", "Excel数据格式无效");
    
    FileErrorCode EXCEL_IMPORT_READ_ERROR = new FileErrorCode("8022", "Excel文件读取失败");

    FileErrorCode EXCEL_IMPORT_FAILED = new FileErrorCode("8028", "Excel导出失败");
    
    FileErrorCode EXCEL_EXPORT_FAILED = new FileErrorCode("8023", "Excel导出失败");
    
    FileErrorCode EXCEL_EXPORT_IO_ERROR = new FileErrorCode("8024", "Excel导出IO异常");
    
    FileErrorCode EXCEL_EXPORT_DATA_EMPTY = new FileErrorCode("8025", "导出数据为空");
    
    FileErrorCode EXCEL_EXPORT_TEMPLATE_ERROR = new FileErrorCode("8026", "Excel模板错误");
    
    FileErrorCode EXCEL_EXPORT_FONT_ERROR = new FileErrorCode("8027", "Excel字体依赖错误");


    static class FileErrorCode extends ErrorCode {

        private String i18nCode;

        public FileErrorCode(String code, String message) {
            super(code, message);
            i18nCode = I18B_PREFIX + code;
        }

        public FileErrorCode(String code, String i18nCode, String message) {
            super(code, message);
            this.i18nCode = i18nCode;
        }

        public String getI18nCode() {
            return i18nCode;
        }

    }
}
