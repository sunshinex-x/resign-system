package com.example.resign.service;

import java.io.InputStream;

/**
 * 重签名服务接口
 */
public interface ResignService {

    /**
     * 重签名iOS应用
     *
     * @param packageInputStream 安装包输入流
     * @param certificateInputStream 证书输入流
     * @param certificatePassword 证书密码
     * @return 重签名后的安装包输入流
     */
    InputStream resignIosApp(InputStream packageInputStream, InputStream certificateInputStream, String certificatePassword);

    /**
     * 重签名Android应用
     *
     * @param packageInputStream 安装包输入流
     * @param certificateInputStream 证书输入流
     * @param certificatePassword 证书密码
     * @return 重签名后的安装包输入流
     */
    InputStream resignAndroidApp(InputStream packageInputStream, InputStream certificateInputStream, String certificatePassword);

    /**
     * 重签名鸿蒙应用
     *
     * @param packageInputStream 安装包输入流
     * @param certificateInputStream 证书输入流
     * @param certificatePassword 证书密码
     * @return 重签名后的安装包输入流
     */
    InputStream resignHarmonyApp(InputStream packageInputStream, InputStream certificateInputStream, String certificatePassword);
}