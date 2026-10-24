package com.danya.kbzhu;
import android.content.Context;
import android.content.SharedPreferences;
import com.getcapacitor.JSObject;
import com.getcapacitor.Plugin;
import com.getcapacitor.PluginCall;
import com.getcapacitor.PluginMethod;
import com.getcapacitor.annotation.CapacitorPlugin;
import org.json.JSONArray;
import org.json.JSONObject;
@CapacitorPlugin(name = "WidgetBridge")
public class WidgetBridge extends Plugin {
    @PluginMethod public void update(PluginCall c) {
        try {
            Context x = getContext();
            x.getSharedPreferences("kbzhu", 0).edit()
                .putString("d", c.getString("d", ""))
                .putInt("k", c.getInt("k", 0)).putInt("g", c.getInt("g", 0)).putInt("gb", c.getInt("gb", 0))
                .putInt("p", c.getInt("p", 0)).putInt("f", c.getInt("f", 0)).putInt("v", c.getInt("v", 0))
                .putInt("w", c.getInt("w", 0)).putInt("wg", c.getInt("wg", 2500)).apply();
            KbzhuWidget.refresh(x);
            c.resolve();
        } catch (Throwable t) {
            c.reject(String.valueOf(t.getMessage()));
        }
    }
    // water added from the widget while the app was closed: hand it over to the app once and clear it
    @PluginMethod public void takeWater(PluginCall c) {
        try {
            SharedPreferences s = getContext().getSharedPreferences("kbzhu", 0);
            String q = KbzhuWidget.takePending(s);
            JSONArray arr = new JSONArray();
            if (!q.isEmpty()) {
                for (String it : q.split(",")) {
                    String[] p = it.split("=");
                    if (p.length != 2) continue;
                    try {
                        JSONObject o = new JSONObject();
                        o.put("d", p[0]);
                        o.put("ml", Integer.parseInt(p[1]));
                        arr.put(o);
                    } catch (Exception ignored) { }
                }
            }
            JSObject r = new JSObject();
            r.put("items", arr);
            c.resolve(r);
        } catch (Throwable t) {
            c.reject(String.valueOf(t.getMessage()));
        }
    }
}
