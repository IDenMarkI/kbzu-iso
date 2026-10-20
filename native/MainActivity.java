package com.danya.kbzhu;
import android.os.Bundle;
import com.getcapacitor.BridgeActivity;
public class MainActivity extends BridgeActivity {
    @Override public void onCreate(Bundle b) { registerPlugin(WidgetBridge.class); registerPlugin(VoiceBridge.class); super.onCreate(b); }
}
