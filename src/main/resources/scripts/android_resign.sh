#!/bin/bash

# Android APK重签名脚本
# 参数说明:
# $1: 原始APK文件路径
# $2: 输出APK文件路径
# $3: 证书文件路径(.jks或.keystore)
# $4: 证书密码
# $5: 密钥别名
# $6: 密钥密码
# $7: 签名版本 (v1, v2, 或 v1v2，默认为v1v2)
# $8: 应用名称 (可选，如果需要修改)
# $9: 应用版本名称 (可选，如果需要修改)
# $10: 应用版本号 (可选，如果需要修改)
# $11: 包名 (可选，如果需要修改)

set -e

# 检查参数
if [ $# -lt 6 ]; then
    echo "错误: 参数不足"
    echo "用法: $0 <原始APK> <输出APK> <证书文件> <证书密码> <密钥别名> <密钥密码> [签名版本] [应用名称] [版本名称] [版本号] [包名]"
    exit 1
fi

ORIGINAL_APK="$1"
OUTPUT_APK="$2"
KEYSTORE_FILE="$3"
KEYSTORE_PASSWORD="$4"
KEY_ALIAS="$5"
KEY_PASSWORD="$6"
SIGN_VERSION="${7:-v1v2}"
NEW_APP_NAME="$8"
NEW_VERSION_NAME="$9"
NEW_VERSION_CODE="${10}"
NEW_PACKAGE_NAME="${11}"

# 检查文件是否存在
if [ ! -f "$ORIGINAL_APK" ]; then
    echo "错误: 原始APK文件不存在: $ORIGINAL_APK"
    exit 1
fi

if [ ! -f "$KEYSTORE_FILE" ]; then
    echo "错误: 证书文件不存在: $KEYSTORE_FILE"
    exit 1
fi

# 检查必要工具
command -v aapt >/dev/null 2>&1 || { echo "错误: 需要安装Android SDK (aapt命令不可用)"; exit 1; }
command -v jarsigner >/dev/null 2>&1 || { echo "错误: 需要安装Java SDK (jarsigner命令不可用)"; exit 1; }
command -v zipalign >/dev/null 2>&1 || { echo "错误: 需要安装Android SDK (zipalign命令不可用)"; exit 1; }

# 检查apksigner是否可用（用于v2签名）
APKSIGNER_AVAILABLE=false
if command -v apksigner >/dev/null 2>&1; then
    APKSIGNER_AVAILABLE=true
fi

# 如果需要v2签名但apksigner不可用，则回退到v1
if [[ "$SIGN_VERSION" == *"v2"* ]] && [ "$APKSIGNER_AVAILABLE" = false ]; then
    echo "警告: apksigner不可用，回退到v1签名"
    SIGN_VERSION="v1"
fi

# 创建临时工作目录
WORK_DIR=$(mktemp -d)
echo "工作目录: $WORK_DIR"

# 清理函数
cleanup() {
    echo "清理临时文件..."
    rm -rf "$WORK_DIR"
}
trap cleanup EXIT

# 复制原始APK到工作目录
cp "$ORIGINAL_APK" "$WORK_DIR/original.apk"

# 如果需要修改APK内容，先解压
if [ -n "$NEW_APP_NAME" ] || [ -n "$NEW_VERSION_NAME" ] || [ -n "$NEW_VERSION_CODE" ] || [ -n "$NEW_PACKAGE_NAME" ]; then
    echo "需要修改APK内容，开始解压..."
    
    # 解压APK
    cd "$WORK_DIR"
    unzip -q original.apk -d extracted/
    
    # 修改AndroidManifest.xml需要使用aapt工具
    if [ -n "$NEW_PACKAGE_NAME" ] || [ -n "$NEW_VERSION_NAME" ] || [ -n "$NEW_VERSION_CODE" ]; then
        echo "修改AndroidManifest.xml..."
        
        # 备份原始manifest
        cp extracted/AndroidManifest.xml extracted/AndroidManifest.xml.bak
        
        # 使用aapt dump获取当前信息
        CURRENT_PACKAGE=$(aapt dump badging original.apk | grep "package:" | sed "s/.*name='\([^']*\)'.*/\1/")
        CURRENT_VERSION_NAME=$(aapt dump badging original.apk | grep "versionName" | sed "s/.*versionName='\([^']*\)'.*/\1/")
        CURRENT_VERSION_CODE=$(aapt dump badging original.apk | grep "versionCode" | sed "s/.*versionCode='\([^']*\)'.*/\1/")
        
        echo "当前包名: $CURRENT_PACKAGE"
        echo "当前版本名: $CURRENT_VERSION_NAME"
        echo "当前版本号: $CURRENT_VERSION_CODE"
        
        # 如果需要修改包名，这需要更复杂的处理
        if [ -n "$NEW_PACKAGE_NAME" ] && [ "$NEW_PACKAGE_NAME" != "$CURRENT_PACKAGE" ]; then
            echo "警告: 修改包名需要重新编译APK，当前脚本不支持此功能"
            echo "将跳过包名修改，继续其他操作..."
        fi
    fi
    
    # 修改应用名称（修改strings.xml）
    if [ -n "$NEW_APP_NAME" ]; then
        echo "修改应用名称为: $NEW_APP_NAME"
        
        # 查找strings.xml文件
        STRINGS_XML=$(find extracted/res -name "strings.xml" | head -1)
        if [ -f "$STRINGS_XML" ]; then
            # 这里需要使用aapt2或其他工具来正确修改编译后的资源文件
            echo "警告: 修改应用名称需要重新编译资源文件，当前脚本不支持此功能"
            echo "将跳过应用名称修改，继续其他操作..."
        fi
    fi
    
    # 重新打包APK（不修改复杂内容的情况下）
    echo "重新打包APK..."
    cd extracted
    zip -qr ../modified.apk .
    cd ..
    
    # 使用修改后的APK
    cp modified.apk working.apk
else
    # 直接使用原始APK
    cp original.apk working.apk
fi

# 删除原有签名
echo "删除原有签名..."
zip -d working.apk META-INF/* 2>/dev/null || true

# 验证证书
echo "验证证书..."
keytool -list -keystore "$KEYSTORE_FILE" -storepass "$KEYSTORE_PASSWORD" -alias "$KEY_ALIAS" >/dev/null 2>&1
if [ $? -ne 0 ]; then
    echo "错误: 证书验证失败，请检查证书文件、密码和别名"
    exit 1
fi

echo "证书验证成功"

# 根据签名版本进行签名
case "$SIGN_VERSION" in
    "v1")
        echo "使用v1签名..."
        
        # 使用jarsigner进行v1签名
        jarsigner -verbose -sigalg SHA256withRSA -digestalg SHA-256 \
                  -keystore "$KEYSTORE_FILE" \
                  -storepass "$KEYSTORE_PASSWORD" \
                  -keypass "$KEY_PASSWORD" \
                  working.apk "$KEY_ALIAS"
        
        # 验证签名
        jarsigner -verify -verbose -certs working.apk
        if [ $? -eq 0 ]; then
            echo "v1签名验证成功"
        else
            echo "错误: v1签名验证失败"
            exit 1
        fi
        
        # zipalign优化
        echo "进行zipalign优化..."
        zipalign -v 4 working.apk aligned.apk
        mv aligned.apk working.apk
        ;;
        
    "v2")
        if [ "$APKSIGNER_AVAILABLE" = true ]; then
            echo "使用v2签名..."
            
            # 先进行zipalign优化
            echo "进行zipalign优化..."
            zipalign -v 4 working.apk aligned.apk
            
            # 使用apksigner进行v2签名
            apksigner sign --ks "$KEYSTORE_FILE" \
                          --ks-pass pass:"$KEYSTORE_PASSWORD" \
                          --ks-key-alias "$KEY_ALIAS" \
                          --key-pass pass:"$KEY_PASSWORD" \
                          --v2-signing-enabled true \
                          --v1-signing-enabled false \
                          aligned.apk
            
            mv aligned.apk working.apk
            
            # 验证签名
            apksigner verify working.apk
            if [ $? -eq 0 ]; then
                echo "v2签名验证成功"
            else
                echo "错误: v2签名验证失败"
                exit 1
            fi
        else
            echo "错误: apksigner不可用，无法进行v2签名"
            exit 1
        fi
        ;;
        
    "v1v2")
        if [ "$APKSIGNER_AVAILABLE" = true ]; then
            echo "使用v1+v2签名..."
            
            # 先进行zipalign优化
            echo "进行zipalign优化..."
            zipalign -v 4 working.apk aligned.apk
            
            # 使用apksigner进行v1+v2签名
            apksigner sign --ks "$KEYSTORE_FILE" \
                          --ks-pass pass:"$KEYSTORE_PASSWORD" \
                          --ks-key-alias "$KEY_ALIAS" \
                          --key-pass pass:"$KEY_PASSWORD" \
                          --v1-signing-enabled true \
                          --v2-signing-enabled true \
                          aligned.apk
            
            mv aligned.apk working.apk
            
            # 验证签名
            apksigner verify working.apk
            if [ $? -eq 0 ]; then
                echo "v1+v2签名验证成功"
            else
                echo "错误: v1+v2签名验证失败"
                exit 1
            fi
        else
            echo "警告: apksigner不可用，回退到v1签名"
            
            # 使用jarsigner进行v1签名
            jarsigner -verbose -sigalg SHA256withRSA -digestalg SHA-256 \
                      -keystore "$KEYSTORE_FILE" \
                      -storepass "$KEYSTORE_PASSWORD" \
                      -keypass "$KEY_PASSWORD" \
                      working.apk "$KEY_ALIAS"
            
            # 验证签名
            jarsigner -verify -verbose -certs working.apk
            if [ $? -eq 0 ]; then
                echo "v1签名验证成功"
            else
                echo "错误: v1签名验证失败"
                exit 1
            fi
            
            # zipalign优化
            echo "进行zipalign优化..."
            zipalign -v 4 working.apk aligned.apk
            mv aligned.apk working.apk
        fi
        ;;
        
    *)
        echo "错误: 不支持的签名版本: $SIGN_VERSION"
        echo "支持的版本: v1, v2, v1v2"
        exit 1
        ;;
esac

# 最终验证
echo "最终验证APK..."
aapt dump badging working.apk >/dev/null 2>&1
if [ $? -eq 0 ]; then
    echo "APK结构验证成功"
else
    echo "警告: APK结构验证失败，但签名可能仍然有效"
fi

# 复制到输出路径
cp working.apk "$OUTPUT_APK"

echo "重签名完成: $OUTPUT_APK"
echo "文件大小: $(du -h "$OUTPUT_APK" | cut -f1)"
echo "签名版本: $SIGN_VERSION"

# 显示APK信息
echo "\nAPK信息:"
aapt dump badging "$OUTPUT_APK" | grep -E "(package|application-label|versionCode|versionName)"

exit 0