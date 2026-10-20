import sys, os
mode = sys.argv[1]
p = 'android/app/src/main/AndroidManifest.xml'
s = open(p, encoding='utf-8').read()
perms = ['android.permission.CAMERA', 'android.permission.POST_NOTIFICATIONS', 'android.permission.RECEIVE_BOOT_COMPLETED']
queries = ''
act_filters = '<intent-filter><action android:name="android.intent.action.VIEW"/><category android:name="android.intent.category.DEFAULT"/><category android:name="android.intent.category.BROWSABLE"/><data android:scheme="kbzhu"/></intent-filter>'
app_extra = '<receiver android:name=".KbzhuWidget" android:exported="true"><intent-filter><action android:name="android.appwidget.action.APPWIDGET_UPDATE"/><action android:name="android.intent.action.DATE_CHANGED"/><action android:name="android.intent.action.TIME_SET"/><action android:name="android.intent.action.TIMEZONE_CHANGED"/></intent-filter><meta-data android:name="android.appwidget.provider" android:resource="@xml/kbzhu_widget"/></receiver>'
if mode == 'full':
    perms += ['android.permission.health.READ_' + x for x in ['STEPS', 'HEART_RATE', 'RESTING_HEART_RATE', 'SLEEP', 'DISTANCE', 'ACTIVE_CALORIES_BURNED', 'TOTAL_CALORIES_BURNED', 'EXERCISE', 'WEIGHT', 'HEALTH_DATA_HISTORY']]
    queries = '<queries><package android:name="com.google.android.apps.healthdata"/></queries>'
    plug = ''
    for r, d, f in os.walk('node_modules/@capgo/capacitor-health/android'):
        for x in f:
            if x == 'AndroidManifest.xml':
                plug += open(os.path.join(r, x), encoding='utf-8').read()
    if 'SHOW_PERMISSIONS_RATIONALE' not in plug:
        act_filters += '<intent-filter><action android:name="androidx.health.ACTION_SHOW_PERMISSIONS_RATIONALE"/></intent-filter>'
        app_extra += '<activity-alias android:name="ViewPermissionUsageActivity" android:exported="true" android:targetActivity=".MainActivity" android:permission="android.permission.START_VIEW_PERMISSION_USAGE"><intent-filter><action android:name="android.intent.action.VIEW_PERMISSION_USAGE"/><category android:name="android.intent.category.HEALTH_PERMISSIONS"/></intent-filter></activity-alias>'
        print('rationale handlers added by app')
    else:
        print('plugin already provides rationale handler')
head = ''.join('<uses-permission android:name="%s"/>\n    ' % x for x in perms) + queries + '\n    '
s = s.replace('<application', head + '<application', 1)
s = s.replace('</activity>', act_filters + '</activity>', 1)
s = s.replace('</application>', app_extra + '</application>', 1)
open(p, 'w', encoding='utf-8').write(s)
