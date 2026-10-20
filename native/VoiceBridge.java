package com.danya.kbzhu;
import android.app.Activity;
import android.content.Intent;
import android.speech.RecognizerIntent;
import androidx.activity.result.ActivityResult;
import com.getcapacitor.JSObject;
import com.getcapacitor.Plugin;
import com.getcapacitor.PluginCall;
import com.getcapacitor.PluginMethod;
import com.getcapacitor.annotation.ActivityCallback;
import com.getcapacitor.annotation.CapacitorPlugin;
import java.util.ArrayList;

// Voice dictation through the system speech dialog (Google app). No extra npm plugin and no microphone permission needed.
@CapacitorPlugin(name = "VoiceBridge")
public class VoiceBridge extends Plugin {
    @PluginMethod
    public void listen(PluginCall call) {
        try {
            Intent i = new Intent(RecognizerIntent.ACTION_RECOGNIZE_SPEECH);
            i.putExtra(RecognizerIntent.EXTRA_LANGUAGE_MODEL, RecognizerIntent.LANGUAGE_MODEL_FREE_FORM);
            i.putExtra(RecognizerIntent.EXTRA_LANGUAGE, call.getString("lang", "uk-UA"));
            i.putExtra(RecognizerIntent.EXTRA_PROMPT, call.getString("prompt", "Що ви з’їли?"));
            i.putExtra(RecognizerIntent.EXTRA_MAX_RESULTS, 1);
            startActivityForResult(call, i, "listenResult");
        } catch (Throwable t) {
            call.reject(String.valueOf(t.getMessage()));
        }
    }

    @ActivityCallback
    private void listenResult(PluginCall call, ActivityResult result) {
        if (call == null) return;
        JSObject o = new JSObject();
        String text = "";
        if (result.getResultCode() == Activity.RESULT_OK && result.getData() != null) {
            ArrayList<String> a = result.getData().getStringArrayListExtra(RecognizerIntent.EXTRA_RESULTS);
            if (a != null && !a.isEmpty()) text = a.get(0);
        }
        o.put("text", text);
        call.resolve(o);
    }
}
