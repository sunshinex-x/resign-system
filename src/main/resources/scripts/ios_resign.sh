#!/bin/bash

# iOS IPA重签名脚本
# 参数说明:
# $1: 原始IPA文件路径
# $2: 输出IPA文件路径
# $3: 证书文件路径(.p12)
# $4: 证书密码
# $5: Provisioning Profile文件路径(.mobileprovision)
# $6: Bundle ID (可选，如果需要修改)
# $7: 应用名称 (可选，如果需要修改)
# $8: 应用版本 (可选，如果需要修改)
# $9: 构建版本 (可选，如果需要修改)

set -e

# 检查参数
if [ $# -lt 5 ]; then
    echo "错误: 参数不足"
    echo "用法: $0 <原始IPA> <输出IPA> <证书文件> <证书密码> <Provisioning Profile> [Bundle ID] [应用名称] [应用版本] [构建版本]"
    exit 1
fi

ORIGINAL_IPA="$1"
OUTPUT_IPA="$2"
CERTIFICATE_FILE="$3"
CERTIFICATE_PASSWORD="$4"
PROVISIONING_PROFILE="$5"
NEW_BUNDLE_ID="$6"
NEW_APP_NAME="$7"
NEW_APP_VERSION="$8"
NEW_BUILD_VERSION="$9"

# 检查文件是否存在
if [ ! -f "$ORIGINAL_IPA" ]; then
    echo "错误: 原始IPA文件不存在: $ORIGINAL_IPA"
    exit 1
fi

if [ ! -f "$CERTIFICATE_FILE" ]; then
    echo "错误: 证书文件不存在: $CERTIFICATE_FILE"
    exit 1
fi

if [ ! -f "$PROVISIONING_PROFILE" ]; then
    echo "错误: Provisioning Profile文件不存在: $PROVISIONING_PROFILE"
    exit 1
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

# 解压IPA文件
echo "解压IPA文件..."
unzip -q "$ORIGINAL_IPA" -d "$WORK_DIR"

# 查找Payload目录中的app文件
APP_PATH=$(find "$WORK_DIR/Payload" -name "*.app" -type d | head -1)
if [ -z "$APP_PATH" ]; then
    echo "错误: 在IPA文件中找不到.app文件"
    exit 1
fi

APP_NAME=$(basename "$APP_PATH")
echo "找到应用: $APP_NAME"

# 导入证书到临时钥匙串
TEMP_KEYCHAIN="$WORK_DIR/temp.keychain"
KEYCHAIN_PASSWORD="temppass123"

echo "创建临时钥匙串..."
security create-keychain -p "$KEYCHAIN_PASSWORD" "$TEMP_KEYCHAIN"
security set-keychain-settings -t 3600 -l "$TEMP_KEYCHAIN"
security unlock-keychain -p "$KEYCHAIN_PASSWORD" "$TEMP_KEYCHAIN"

echo "导入证书..."
security import "$CERTIFICATE_FILE" -k "$TEMP_KEYCHAIN" -P "$CERTIFICATE_PASSWORD" -T /usr/bin/codesign

# 获取证书标识
CERT_IDENTITY=$(security find-identity -v -p codesigning "$TEMP_KEYCHAIN" | grep "iPhone" | head -1 | sed 's/.*) \(.*\) ".*/\1/')
if [ -z "$CERT_IDENTITY" ]; then
    echo "错误: 无法找到有效的代码签名证书"
    exit 1
fi
echo "使用证书: $CERT_IDENTITY"

# 复制Provisioning Profile
echo "安装Provisioning Profile..."
cp "$PROVISIONING_PROFILE" "$APP_PATH/embedded.mobileprovision"

# 修改Info.plist文件
INFO_PLIST="$APP_PATH/Info.plist"
if [ -f "$INFO_PLIST" ]; then
    echo "修改Info.plist..."
    
    # 修改Bundle ID
    if [ -n "$NEW_BUNDLE_ID" ]; then
        echo "修改Bundle ID为: $NEW_BUNDLE_ID"
        /usr/libexec/PlistBuddy -c "Set :CFBundleIdentifier $NEW_BUNDLE_ID" "$INFO_PLIST"
    fi
    
    # 修改应用名称
    if [ -n "$NEW_APP_NAME" ]; then
        echo "修改应用名称为: $NEW_APP_NAME"
        /usr/libexec/PlistBuddy -c "Set :CFBundleDisplayName $NEW_APP_NAME" "$INFO_PLIST" 2>/dev/null || \
        /usr/libexec/PlistBuddy -c "Add :CFBundleDisplayName string $NEW_APP_NAME" "$INFO_PLIST"
        /usr/libexec/PlistBuddy -c "Set :CFBundleName $NEW_APP_NAME" "$INFO_PLIST" 2>/dev/null || \
        /usr/libexec/PlistBuddy -c "Add :CFBundleName string $NEW_APP_NAME" "$INFO_PLIST"
    fi
    
    # 修改应用版本
    if [ -n "$NEW_APP_VERSION" ]; then
        echo "修改应用版本为: $NEW_APP_VERSION"
        /usr/libexec/PlistBuddy -c "Set :CFBundleShortVersionString $NEW_APP_VERSION" "$INFO_PLIST"
    fi
    
    # 修改构建版本
    if [ -n "$NEW_BUILD_VERSION" ]; then
        echo "修改构建版本为: $NEW_BUILD_VERSION"
        /usr/libexec/PlistBuddy -c "Set :CFBundleVersion $NEW_BUILD_VERSION" "$INFO_PLIST"
    fi
fi

# 删除原有签名
echo "删除原有签名..."
rm -rf "$APP_PATH/_CodeSignature"
rm -f "$APP_PATH/CodeResources"

# 查找并删除所有Frameworks中的签名
if [ -d "$APP_PATH/Frameworks" ]; then
    echo "删除Frameworks中的签名..."
    find "$APP_PATH/Frameworks" -name "_CodeSignature" -type d -exec rm -rf {} + 2>/dev/null || true
    find "$APP_PATH/Frameworks" -name "CodeResources" -type f -delete 2>/dev/null || true
fi

# 重新签名Frameworks
if [ -d "$APP_PATH/Frameworks" ]; then
    echo "重新签名Frameworks..."
    for framework in "$APP_PATH/Frameworks"/*; do
        if [ -d "$framework" ]; then
            echo "签名: $(basename "$framework")"
            codesign --force --sign "$CERT_IDENTITY" --keychain "$TEMP_KEYCHAIN" "$framework"
        fi
    done
fi

# 重新签名主应用
echo "重新签名主应用..."
codesign --force --sign "$CERT_IDENTITY" --keychain "$TEMP_KEYCHAIN" --entitlements "$APP_PATH/archived-expanded-entitlements.xcent" "$APP_PATH" 2>/dev/null || \
codesign --force --sign "$CERT_IDENTITY" --keychain "$TEMP_KEYCHAIN" "$APP_PATH"

# 验证签名
echo "验证签名..."
codesign --verify --verbose "$APP_PATH"
if [ $? -eq 0 ]; then
    echo "签名验证成功"
else
    echo "警告: 签名验证失败，但继续打包"
fi

# 重新打包IPA
echo "重新打包IPA..."
cd "$WORK_DIR"
zip -qr "$(basename "$OUTPUT_IPA")" Payload/
mv "$(basename "$OUTPUT_IPA")" "$OUTPUT_IPA"

# 删除临时钥匙串
echo "清理钥匙串..."
security delete-keychain "$TEMP_KEYCHAIN" 2>/dev/null || true

echo "重签名完成: $OUTPUT_IPA"
echo "文件大小: $(du -h "$OUTPUT_IPA" | cut -f1)"

exit 0