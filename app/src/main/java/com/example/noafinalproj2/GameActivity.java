package com.example.noafinalproj2;

import android.os.Bundle;
import android.widget.EditText;

import androidx.appcompat.app.AppCompatActivity;

import org.json.JSONArray;
import org.json.JSONObject;

import java.io.IOException;

import okhttp3.Call;
import okhttp3.Callback;
import okhttp3.MediaType;
import okhttp3.OkHttpClient;
import okhttp3.Request;
import okhttp3.RequestBody;
import okhttp3.Response;

public class GameActivity extends AppCompatActivity {

    private EditText edSubject;
    private static final String API_KEY = "AIzaSyCxQCtqMjCtfgMnSPhY1mQ9dRMQ-ihDvRI"; // הכניסי כאן את מפתח ה-API שלך

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_game);

        edSubject = findViewById(R.id.edSubject);

        getTopicFromAI();
    }

    private void getTopicFromAI() {
        OkHttpClient client = new OkHttpClient();

        // JSON מתוקן עבור v1beta2
        String json = "{"
                + "\"prompt\": {\"text\": \"תן נושא אקראי וקצר למשחק אסוציאציות\"},"
                + "\"temperature\": 0.7,"
                + "\"candidate_count\": 1"
                + "}";

        RequestBody body = RequestBody.create(
                json,
                MediaType.get("application/json; charset=utf-8")
        );

        Request request = new Request.Builder()
                .url("https://generativelanguage.googleapis.com/v1beta2/models/chat-bison-001:generateMessage?key=" + API_KEY)
                .post(body)
                .build();

        client.newCall(request).enqueue(new Callback() {
            @Override
            public void onFailure(Call call, IOException e) {
                runOnUiThread(() ->
                        edSubject.setText("שגיאה בחיבור ל‑AI: " + e.getMessage())
                );
            }

            @Override
            public void onResponse(Call call, Response response) {
                try {
                    if (!response.isSuccessful()) {
                        final String errorMsg = "שגיאת שרת: " + response.code() + "\n" + (response.body() != null ? response.body().string() : "אין גוף תגובה");
                        runOnUiThread(() -> edSubject.setText(errorMsg));
                        return;
                    }

                    String responseBody = response.body() != null ? response.body().string() : "{}";
                    JSONObject jsonObject = new JSONObject(responseBody);

                    if (!jsonObject.has("candidates")) {
                        runOnUiThread(() -> edSubject.setText("התשובה מה-API אינה תקינה: אין 'candidates'"));
                        return;
                    }

                    JSONArray candidates = jsonObject.optJSONArray("candidates");
                    if (candidates == null || candidates.length() == 0) {
                        runOnUiThread(() -> edSubject.setText("לא התקבלה תשובה"));
                        return;
                    }

                    JSONObject firstCandidate = candidates.optJSONObject(0);
                    if (firstCandidate == null || !firstCandidate.has("output")) {
                        runOnUiThread(() -> edSubject.setText("התשובה הראשונה אינה תקינה"));
                        return;
                    }

                    // לפי הפורמט החדש, הטקסט נמצא בתוך output[0].content[0].text
                    JSONArray outputArray = firstCandidate.optJSONArray("output");
                    if (outputArray == null || outputArray.length() == 0) {
                        runOnUiThread(() -> edSubject.setText("לא נמצא תוכן בתשובה"));
                        return;
                    }

                    JSONObject outputObj = outputArray.optJSONObject(0);
                    JSONArray contentArray = outputObj != null ? outputObj.optJSONArray("content") : null;
                    if (contentArray == null || contentArray.length() == 0) {
                        runOnUiThread(() -> edSubject.setText("לא נמצא תוכן טקסטואלי בתשובה"));
                        return;
                    }

                    StringBuilder textResult = new StringBuilder();
                    for (int i = 0; i < contentArray.length(); i++) {
                        JSONObject obj = contentArray.optJSONObject(i);
                        if (obj != null && "text".equals(obj.optString("type")) && obj.has("text")) {
                            textResult.append(obj.optString("text"));
                        }
                    }

                    final String result = textResult.length() > 0 ? textResult.toString().trim() : "לא התקבלה תשובה טקסטואלית";
                    runOnUiThread(() -> edSubject.setText(result));

                } catch (Exception e) {
                    runOnUiThread(() -> edSubject.setText("שגיאה בעיבוד התגובה: " + e.getMessage()));
                }
            }
        });
    }


}





