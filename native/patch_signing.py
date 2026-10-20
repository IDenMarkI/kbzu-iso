import re, sys
# Force Gradle to sign the debug APK with OUR fixed keystore (android/app/kbzhu.keystore),
# instead of whatever debug keystore AGP finds on the runner.
p = 'android/app/build.gradle'
s = open(p, encoding='utf-8').read()
block = ("android {\n"
         "    signingConfigs {\n"
         "        debug {\n"
         "            storeFile file('kbzhu.keystore')\n"
         "            storePassword 'android'\n"
         "            keyAlias 'androiddebugkey'\n"
         "            keyPassword 'android'\n"
         "        }\n"
         "    }")
if 'kbzhu.keystore' in s:
    print('signing already patched'); sys.exit(0)
n, c = re.subn(r'(?m)^android\s*\{', block, s, count=1)
if c != 1:
    print('ERROR: could not find android { block in build.gradle'); sys.exit(1)
open(p, 'w', encoding='utf-8').write(n)
print('signing config patched')
