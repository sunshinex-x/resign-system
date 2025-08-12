package com.example.resign.service;

import java.io.InputStream;

/**
 * 文件服务接口
 */
public interface FileService {

    /**
     * 上传文件到MinIO
     *
     * @param inputStream 文件输入流
     * @param objectName  对象名称
     * @param contentType 内容类型
     * @return 文件访问URL
     */
    String uploadFile(InputStream inputStream, String objectName, String contentType);

    /**
     * 从URL下载文件
     *
     * @param fileUrl 文件URL
     * @return 文件输入流
     */
    InputStream downloadFileFromUrl(String fileUrl);

    /**
     * 从MinIO下载文件
     *
     * @param objectName 对象名称
     * @return 文件输入流
     */
    InputStream downloadFile(String objectName);

    /**
     * 删除MinIO中的文件
     *
     * @param objectName 对象名称
     * @return 是否删除成功
     */
    boolean deleteFile(String objectName);

    /**
     * 获取文件的访问URL
     *
     * @param objectName 对象名称
     * @return 文件访问URL
     */
    String getFileUrl(String objectName);

    /**
     * 从URL获取对象名称
     *
     * @param fileUrl 文件URL
     * @return 对象名称
     */
    String getObjectNameFromUrl(String fileUrl);
}