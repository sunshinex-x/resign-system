package com.example.resign.model.dto;

import lombok.Data;

/**
 * 重签名结果DTO
 */
@Data
public class ResignResultDTO {

    /**
     * 是否成功
     */
    private boolean success;

    /**
     * 重签名后的文件URL
     */
    private String resignedFileUrl;

    /**
     * 重签名后的文件路径（MinIO对象名）
     */
    private String resignedFilePath;

    /**
     * 文件大小
     */
    private Long fileSize;

    /**
     * 新的签名信息
     */
    private String newSignature;

    /**
     * 处理耗时（毫秒）
     */
    private Long processingTime;

    /**
     * 错误信息
     */
    private String errorMessage;

    /**
     * 详细日志
     */
    private String detailLog;

    /**
     * 创建成功结果
     */
    public static ResignResultDTO success(String resignedFileUrl, String resignedFilePath, Long fileSize) {
        ResignResultDTO result = new ResignResultDTO();
        result.setSuccess(true);
        result.setResignedFileUrl(resignedFileUrl);
        result.setResignedFilePath(resignedFilePath);
        result.setFileSize(fileSize);
        return result;
    }

    /**
     * 创建失败结果
     */
    public static ResignResultDTO failure(String errorMessage) {
        ResignResultDTO result = new ResignResultDTO();
        result.setSuccess(false);
        result.setErrorMessage(errorMessage);
        return result;
    }
}