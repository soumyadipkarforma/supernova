#!/bin/bash
# SuperNova Shell - BusyBox Build Script
# Host: Linux x86_64
# Target: Android aarch64 (API 21)

set -e

# ========================================
# STEP 1 — CLONE REQUIRED REPOSITORIES
# ========================================
echo "Step 1: Cloning BusyBox..."
git clone https://github.com/mirror/busybox.git
cd busybox

# ========================================
# STEP 2 — INSTALL ANDROID NDK
# ========================================
echo "Step 2: Installing Android NDK..."
wget https://dl.google.com/android/repository/android-ndk-r26c-linux.zip
unzip -q android-ndk-r26c-linux.zip
export ANDROID_NDK=$PWD/android-ndk-r26c

# ========================================
# STEP 3 — SET TOOLCHAIN VARIABLES
# ========================================
echo "Step 3: Setting Toolchain..."
export TOOLCHAIN=$ANDROID_NDK/toolchains/llvm/prebuilt/linux-x86_64
export TARGET=aarch64-linux-android
export API=21
export CC=$TOOLCHAIN/bin/${TARGET}${API}-clang
export AR=$TOOLCHAIN/bin/llvm-ar
export LD=$TOOLCHAIN/bin/ld.lld
export STRIP=$TOOLCHAIN/bin/llvm-strip

# ========================================
# STEP 4 — CONFIGURE BUSYBOX (STATIC)
# ========================================
echo "Step 4: Configuring BusyBox..."
make distclean
make defconfig
# Enable static build and ash shell
sed -i 's/# CONFIG_STATIC is not set/CONFIG_STATIC=y/' .config
sed -i 's/# CONFIG_ASH is not set/CONFIG_ASH=y/' .config

# ========================================
# STEP 5 — COMPILE BUSYBOX
# ========================================
echo "Step 5: Compiling..."
make -j$(nproc) CC="$CC" AR="$AR" LD="$LD"

echo "Verifying output..."
file busybox
$STRIP busybox

echo "Build Complete. Binary is at: $(pwd)/busybox"
echo "Step 6: Move this binary to app/src/main/assets/bin/busybox"
