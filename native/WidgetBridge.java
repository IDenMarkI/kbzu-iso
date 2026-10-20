package com.danya.kbzhu;
import android.content.Context;
import com.getcapacitor.Plugin;
import com.getcapacitor.PluginCall;
import com.getcapacitor.PluginMethod;
import com.getcapacitor.annotation.CapacitorPlugin;
@CapacitorPlugin(name = "WidgetBridge")
public class WidgetBridge extends Plugin {
    @PluginMethod public void update(PluginCall c) {
        try {
            Context x = getContext();
            x.getSharedPreferences("kbzhu", 0).edit()
                .putString("d", c.getString("d", ""))
                .putInt("k", c.getInt("k", 0)).putInt("g", c.getInt("g", 0))
                .putInt("p", c.getInt("p", 0)).putInt("f", c.getInt("f", 0)).putInt("v", c.getInt("v", 0)).apply();
            KbzhuWidget.refresh(x);
            c.resolve();
        } catch (Throwable t) {
            c.reject(String.valueOf(t.getMessage()));
        }
    }
}
