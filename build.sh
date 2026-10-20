#!/usr/bin/env bash
# usage: bash build.sh full|safe   (full = Capacitor 8 + Health Connect, safe = Capacitor 6 without Health Connect)
set -euo pipefail
MODE="$1"
if [ "$MODE" = full ]; then export JAVA_HOME="$JAVA_HOME_21_X64"; MS=26; else export JAVA_HOME="$JAVA_HOME_17_X64"; MS=23; fi
export PATH="$JAVA_HOME/bin:$PATH"
java -version
rm -rf android node_modules package-lock.json out
mkdir -p out
cp "package.$MODE.json" package.json
npm install --no-audit --no-fund --legacy-peer-deps
npx cap add android
A=android/app/src/main
sed -i -E "s/minSdkVersion = [0-9]+/minSdkVersion = $MS/" android/variables.gradle
VC=$(date +%s)   # seconds since epoch: always larger than any earlier scheme (run number / minutes), so Android never sees a downgrade
sed -i -E "s/versionCode [0-9]+/versionCode $VC/; s/versionName \"[^\"]*\"/versionName \"2.${GITHUB_RUN_NUMBER:-1}\"/" android/app/build.gradle
grep -E "versionCode|versionName" android/app/build.gradle || true
cp native/*.java $A/java/com/danya/kbzhu/
cp native/widget.xml $A/res/layout/
mkdir -p $A/res/drawable $A/res/xml
cp native/wbg.xml native/wbtn.xml native/wprog.xml $A/res/drawable/
cp native/kbzhu_widget.xml $A/res/xml/
python3 native/patch_manifest.py "$MODE"
npx cap sync android
mkdir -p ~/.android
echo "$KEYB64" | base64 -d > ~/.android/debug.keystore
cp ~/.android/debug.keystore android/app/kbzhu.keystore
keytool -list -keystore android/app/kbzhu.keystore -storepass android | tail -2
EXPECT=$(keytool -list -keystore android/app/kbzhu.keystore -storepass android | grep -i "SHA-256" | head -1 | awk '{print $NF}' | tr -d ':' | tr 'A-F' 'a-f' || true)
python3 native/patch_signing.py
(cd android && chmod +x gradlew && ./gradlew assembleDebug)
APK="out/kbzhu-v2.${GITHUB_RUN_NUMBER:-1}-$MODE.apk"
cp android/app/build/outputs/apk/debug/app-debug.apk "$APK"
# the APK must be signed with OUR fixed key, otherwise Android refuses to update the app over the previous build
AS=$(ls "${ANDROID_HOME:-$ANDROID_SDK_ROOT}"/build-tools/*/apksigner 2>/dev/null | tail -1 || true)
if [ -n "$AS" ]; then
  "$AS" verify --print-certs "$APK" | grep -iE "SHA-256|certificate DN" || true
  GOT=$("$AS" verify --print-certs "$APK" | grep -i "certificate SHA-256 digest" | head -1 | awk '{print $NF}' | tr -d ':' | tr 'A-F' 'a-f' || true)
  echo "expected signing key SHA-256: $EXPECT"
  echo "actual   signing key SHA-256: $GOT"
  if [ -n "$GOT" ] && [ -n "$EXPECT" ] && [ "$GOT" != "$EXPECT" ]; then
    echo "::error::APK is signed with a different key than the fixed keystore, updates over the old app would fail"
    exit 1
  fi
fi
echo "BUILT MODE: $MODE"
