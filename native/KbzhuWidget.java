package com.danya.kbzhu;
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
import java.util.Locale;
public class KbzhuWidget extends AppWidgetProvider {
    @Override public void onUpdate(Context c, AppWidgetManager m, int[] ids) { for (int i : ids) draw(c, m, i); }
    @Override public void onAppWidgetOptionsChanged(Context c, AppWidgetManager m, int id, Bundle o) { draw(c, m, id); }
    @Override public void onReceive(Context c, Intent in) {
        super.onReceive(c, in);
        String a = in == null ? null : in.getAction();
        if (Intent.ACTION_DATE_CHANGED.equals(a) || Intent.ACTION_TIME_CHANGED.equals(a) || Intent.ACTION_TIMEZONE_CHANGED.equals(a)) refresh(c);
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
        String today = new java.text.SimpleDateFormat("yyyy-MM-dd", Locale.US).format(new java.util.Date());
        boolean fresh = today.equals(s.getString("d", ""));
        int k = fresh ? s.getInt("k", 0) : 0, g = s.getInt("g", 0);
        int p = fresh ? s.getInt("p", 0) : 0, f = fresh ? s.getInt("f", 0) : 0, v = fresh ? s.getInt("v", 0) : 0;
        RemoteViews r = new RemoteViews(c.getPackageName(), R.layout.widget);
        r.setTextViewText(R.id.wk, String.valueOf(k));
        r.setTextViewText(R.id.wg, g > 0 ? ("з " + g + " ккал · " + (g >= k ? "ще " + (g - k) : "перебір " + (k - g))) : "відкрийте додаток");
        r.setTextViewText(R.id.wm, "Б " + p + " · Ж " + f + " · В " + v);
        int mx = Math.max(g, 1);
        r.setProgressBar(R.id.wb, mx, Math.min(k, mx), false);
        r.setOnClickPendingIntent(R.id.wr, openApp(c));
        r.setOnClickPendingIntent(R.id.wadd, link(c, "kbzhu://add", 1));
        r.setOnClickPendingIntent(R.id.wai, link(c, "kbzhu://ai", 2));
        r.setOnClickPendingIntent(R.id.wcam, link(c, "kbzhu://cam", 3));
        m.updateAppWidget(id, r);
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
        AppWidgetManager m = AppWidgetManager.getInstance(c);
        int[] ids = m.getAppWidgetIds(new ComponentName(c, KbzhuWidget.class));
        if (ids == null) return;
        for (int id : ids) draw(c, m, id);
    }
}
