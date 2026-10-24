package com.danya.kbzhu;
import android.app.AlarmManager;
import android.app.PendingIntent;
import android.appwidget.AppWidgetManager;
import android.appwidget.AppWidgetProvider;
import android.content.ComponentName;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.net.Uri;
import android.os.Bundle;
import android.widget.RemoteViews;
import android.widget.Toast;
import java.util.Locale;
public class KbzhuWidget extends AppWidgetProvider {
    static final String ACT_WATER = "com.danya.kbzhu.ADD_WATER";
    static final String ACT_TICK = "com.danya.kbzhu.MIDNIGHT_TICK";
    static final int WATER_ML = 500;
    @Override public void onUpdate(Context c, AppWidgetManager m, int[] ids) { scheduleMidnight(c); for (int i : ids) draw(c, m, i); }
    @Override public void onEnabled(Context c) { scheduleMidnight(c); }
    @Override public void onDisabled(Context c) {
        try {
            AlarmManager am = (AlarmManager) c.getSystemService(Context.ALARM_SERVICE);
            if (am != null) am.cancel(tickPi(c));
        } catch (Throwable ignored) { }
    }
    @Override public void onAppWidgetOptionsChanged(Context c, AppWidgetManager m, int id, Bundle o) { draw(c, m, id); }
    @Override public void onReceive(Context c, Intent in) {
        super.onReceive(c, in);
        String a = in == null ? null : in.getAction();
        if (ACT_WATER.equals(a)) {
            try {
                addWater(c, WATER_ML);
                Toast.makeText(c, "💧 +" + WATER_ML + " мл", Toast.LENGTH_SHORT).show();
            } catch (Throwable ignored) { }
            refresh(c);
        } else if (ACT_TICK.equals(a) || Intent.ACTION_DATE_CHANGED.equals(a) || Intent.ACTION_TIME_CHANGED.equals(a) || Intent.ACTION_TIMEZONE_CHANGED.equals(a)) {
            refresh(c);
        }
    }
    // The system does not always deliver DATE_CHANGED or run periodic updates for sleeping apps,
    // so we also wake the widget ourselves right after midnight and re-arm the alarm every time.
    static PendingIntent tickPi(Context c) {
        Intent i = new Intent(c, KbzhuWidget.class);
        i.setAction(ACT_TICK);
        return PendingIntent.getBroadcast(c, 5, i, PendingIntent.FLAG_IMMUTABLE | PendingIntent.FLAG_UPDATE_CURRENT);
    }
    static void scheduleMidnight(Context c) {
        try {
            AlarmManager am = (AlarmManager) c.getSystemService(Context.ALARM_SERVICE);
            if (am == null) return;
            java.util.Calendar t = java.util.Calendar.getInstance();
            t.add(java.util.Calendar.DAY_OF_YEAR, 1);
            t.set(java.util.Calendar.HOUR_OF_DAY, 0);
            t.set(java.util.Calendar.MINUTE, 0);
            t.set(java.util.Calendar.SECOND, 5);
            t.set(java.util.Calendar.MILLISECOND, 0);
            am.setAndAllowWhileIdle(AlarmManager.RTC_WAKEUP, t.getTimeInMillis(), tickPi(c));
        } catch (Throwable ignored) { }
    }
    static String today() { return new java.text.SimpleDateFormat("yyyy-MM-dd", Locale.US).format(new java.util.Date()); }
    // pending water taps are stored as "yyyy-MM-dd=ml,yyyy-MM-dd=ml" until the app is opened and takes them
    static synchronized void addWater(Context c, int ml) {
        SharedPreferences s = c.getSharedPreferences("kbzhu", 0);
        String d = today(), q = s.getString("wq", "");
        StringBuilder out = new StringBuilder();
        boolean found = false;
        if (q != null && !q.isEmpty()) {
            for (String it : q.split(",")) {
                String[] p = it.split("=");
                if (p.length != 2) continue;
                int v = 0;
                try { v = Integer.parseInt(p[1]); } catch (Exception e) { continue; }
                if (p[0].equals(d)) { v += ml; found = true; }
                if (out.length() > 0) out.append(",");
                out.append(p[0]).append("=").append(v);
            }
        }
        if (!found) { if (out.length() > 0) out.append(","); out.append(d).append("=").append(ml); }
        s.edit().putString("wq", out.toString()).apply();
    }
    static synchronized int pendingToday(SharedPreferences s) {
        String q = s.getString("wq", ""), d = today();
        int t = 0;
        if (q == null || q.isEmpty()) return 0;
        for (String it : q.split(",")) {
            String[] p = it.split("=");
            if (p.length == 2 && p[0].equals(d)) { try { t += Integer.parseInt(p[1]); } catch (Exception e) { } }
        }
        return t;
    }
    static synchronized String takePending(SharedPreferences s) {
        String q = s.getString("wq", "");
        s.edit().remove("wq").apply();
        return q == null ? "" : q;
    }
    static void draw(Context c, AppWidgetManager m, int id) {
        try {
            drawSafe(c, m, id);
        } catch (Throwable t) {
            // never leave the launcher with "Can't load widget": show a plain card that opens the app
            try {
                RemoteViews r = new RemoteViews(c.getPackageName(), R.layout.widget);
                r.setTextViewText(R.id.wg, "відкрийте додаток");
                r.setOnClickPendingIntent(R.id.wr, openApp(c));
                m.updateAppWidget(id, r);
            } catch (Throwable ignored) { }
        }
    }
    static void drawSafe(Context c, AppWidgetManager m, int id) {
        SharedPreferences s = c.getSharedPreferences("kbzhu", 0);
        String today = today();
        boolean fresh = today.equals(s.getString("d", ""));
        int gb = s.getInt("gb", 0);
        int k = fresh ? s.getInt("k", 0) : 0, g = fresh ? s.getInt("g", 0) : (gb > 0 ? gb : s.getInt("g", 0));
        int p = fresh ? s.getInt("p", 0) : 0, f = fresh ? s.getInt("f", 0) : 0, v = fresh ? s.getInt("v", 0) : 0;
        int w = (fresh ? s.getInt("w", 0) : 0) + pendingToday(s), wg = s.getInt("wg", 2500);
        RemoteViews r = new RemoteViews(c.getPackageName(), R.layout.widget);
        r.setTextViewText(R.id.wk, String.valueOf(k));
        r.setTextViewText(R.id.wg, g > 0 ? ("з " + g + " ккал · " + (g >= k ? "ще " + (g - k) : "перебір " + (k - g))) : "відкрийте додаток");
        r.setTextViewText(R.id.wm, "Б " + p + " · Ж " + f + " · В " + v);
        r.setTextViewText(R.id.ww, "💧 " + w + " / " + wg + " мл");
        int mx = Math.max(g, 1);
        r.setProgressBar(R.id.wb, mx, Math.min(k, mx), false);
        r.setOnClickPendingIntent(R.id.wr, openApp(c));
        r.setOnClickPendingIntent(R.id.wadd, link(c, "kbzhu://add", 1));
        r.setOnClickPendingIntent(R.id.wai, link(c, "kbzhu://ai", 2));
        r.setOnClickPendingIntent(R.id.wcam, link(c, "kbzhu://cam", 3));
        r.setOnClickPendingIntent(R.id.wwa, waterPi(c));
        m.updateAppWidget(id, r);
    }
    static PendingIntent waterPi(Context c) {
        Intent i = new Intent(c, KbzhuWidget.class);
        i.setAction(ACT_WATER);
        return PendingIntent.getBroadcast(c, 4, i, PendingIntent.FLAG_IMMUTABLE | PendingIntent.FLAG_UPDATE_CURRENT);
    }
    static PendingIntent openApp(Context c) {
        Intent i = c.getPackageManager().getLaunchIntentForPackage(c.getPackageName());
        if (i == null) { i = new Intent(); i.setClass(c, MainActivity.class); }
        i.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
        return PendingIntent.getActivity(c, 0, i, PendingIntent.FLAG_IMMUTABLE | PendingIntent.FLAG_UPDATE_CURRENT);
    }
    static PendingIntent link(Context c, String url, int code) {
        Intent i = new Intent(Intent.ACTION_VIEW, Uri.parse(url));
        i.setClass(c, MainActivity.class);
        i.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TOP);
        return PendingIntent.getActivity(c, code, i, PendingIntent.FLAG_IMMUTABLE | PendingIntent.FLAG_UPDATE_CURRENT);
    }
    static void refresh(Context c) {
        scheduleMidnight(c);
        AppWidgetManager m = AppWidgetManager.getInstance(c);
        int[] ids = m.getAppWidgetIds(new ComponentName(c, KbzhuWidget.class));
        if (ids == null) return;
        for (int id : ids) draw(c, m, id);
    }
}
