package com.example.noafinalproj2;

import android.os.Bundle;
import android.os.CountDownTimer;
import android.os.Handler;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.TextView;
import android.widget.Toast;

import androidx.activity.EdgeToEdge;
import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.example.noafinalproj2.gemini.GeminiCallback;
import com.example.noafinalproj2.gemini.GeminiManager;

public class GameActivity extends AppCompatActivity implements View.OnClickListener {

    private Button btnSend;
    private EditText edAnswer;
    private TextView tvAnswer;
    private RecordAdapter adapter;
    private int currentPlayer = 1;
    private CountDownTimer turnTimer;
    private TextView timerText;
    private TextView playerText;
    private static final long TURN_TIME = 30000; // 30 שניות

    private boolean hasAnswered = false;
    private boolean gameOver = false;

    private String topic;
    FBsingleton fBsingleton;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        EdgeToEdge.enable(this);
        setContentView(R.layout.activity_game);

        // אתחול רכיבי ממשק
        btnSend = findViewById(R.id.btnSend);
        btnSend.setOnClickListener(this);
        edAnswer = findViewById(R.id.edAnswer);
        tvAnswer = findViewById(R.id.tvAnswer);
        timerText = findViewById(R.id.timerText);
        playerText = findViewById(R.id.playerText);

        // קבלת נושא מהאינטנט
        topic = getIntent().getStringExtra("subject");
        tvAnswer.setText(topic);

        // הגדרת ה-RecyclerView פעם אחת בלבד ב-onCreate
        setupRecyclerView();

        startTurn();
    }

    private void setupRecyclerView() {
        RecyclerView recyclerView = findViewById(R.id.recyclerView);
        recyclerView.setLayoutManager(new LinearLayoutManager(this));
        // שימוש ברשימה הסטטית מ-MainActivity
        adapter = new RecordAdapter(this, MainActivity.records);
        recyclerView.setAdapter(adapter);
    }

    private void startTurn() {
        hasAnswered = false;
        playerText.setText("תור של שחקן " + currentPlayer);

        if (turnTimer != null) turnTimer.cancel();

        turnTimer = new CountDownTimer(TURN_TIME, 1000) {
            @Override
            public void onTick(long millisUntilFinished) {
                timerText.setText("זמן: " + millisUntilFinished / 1000);
            }

            @Override
            public void onFinish() {
                if (!hasAnswered && !gameOver) {
                    endGame(currentPlayer == 1 ? 2 : 1);
                }
            }
        }.start();
    }

    private void switchTurn() {
        currentPlayer = (currentPlayer == 1) ? 2 : 1;
        startTurn();
    }

    @Override
    public void onClick(View v) {
        if (v == btnSend && !gameOver) {
            String answer = edAnswer.getText().toString().trim();

            if (answer.isEmpty()) {
                Toast.makeText(this, "יש להזין תשובה", Toast.LENGTH_SHORT).show();
                return;
            }

            // --- בדיקת כפילויות ---
            if (isDuplicate(answer)) {
                Toast.makeText(this, "המילה '" + answer + "' כבר נאמרה! נסה מילה אחרת", Toast.LENGTH_SHORT).show();
                edAnswer.setText(""); // מנקים את השדה
                return; // עוצרים כאן ולא ממשיכים ל-AI
            }
            // -----------------------

            // אם המילה חדשה, ממשיכים לבדיקת ה-AI
            checkAnswerWithAI(answer);
        }
    }

    /**
     * פונקציית עזר שבודקת אם המילה כבר קיימת ברשימה
     */
    private boolean isDuplicate(String newAnswer) {
        // עוברים על כל הרשומות ששמורות ברשימה הסטטית
        for (Record record : MainActivity.records) {
            // בודקים התאמה (מתעלמים מרווחים ואותיות גדולות/קטנות ליתר ביטחון)
            if (record.getAnswer().trim().equalsIgnoreCase(newAnswer)) {
                return true; // נמצאה כפילות
            }
        }
        return false; // המילה חדשה
    }

    private void checkAnswerWithAI(String answerToCheck) {
        btnSend.setEnabled(false); // נועלים את הכפתור כדי שלא ילחצו שוב

        // הנחיה חזקה יותר ל-AI
        String prompt = "You are a game judge. The category is: " + topic +
                ". Is the word '" + answerToCheck + "' part of this category? " +
                "Answer ONLY with the word 'true' or 'false'. No punctuation.";

        GeminiManager.getInstance().sendMessage(prompt, new GeminiCallback() {
            @Override
            public void onSuccess(String response) {
                // ניקוי התשובה מרווחים, סימני פיסוק ואותיות גדולות
                String cleanResponse = response.toLowerCase().trim().replaceAll("[^a-z]", "");

                Log.d("AI_DEBUG", "Original: " + response + " | Cleaned: " + cleanResponse);

                runOnUiThread(() -> {
                    btnSend.setEnabled(true);

                    if (cleanResponse.equals("true")) {
                        // התשובה נכונה!
                        proceedWithCorrectAnswer(answerToCheck);
                    } else {
                        // התשובה לא מתאימה - הודעה חמודה לילדים
                        Toast.makeText(GameActivity.this,
                                "הממ... '" + answerToCheck + "' זה לא " + topic + "! נסה מילה אחרת 😊",
                                Toast.LENGTH_LONG).show();
                        edAnswer.setText(""); // מנקים כדי שיוכל לנסות שוב
                    }
                });
            }

            @Override
            public void onError(Throwable e) {
                handleAIError(answerToCheck);
            }

            @Override
            public void onError(Exception e) {
                handleAIError(answerToCheck);
            }
        });
    }

    private void handleAIError(String answerToCheck) {
        runOnUiThread(() -> {
            btnSend.setEnabled(true);
            // במקרה של שגיאת רשת, נאשר את התשובה כדי לא לתקוע את המשחק
            proceedWithCorrectAnswer(answerToCheck);
        });
    }

    private void proceedWithCorrectAnswer(String validAnswer) {
        hasAnswered = true;

        // שמירה ב-Firebase
        validAnswer = "Player " + currentPlayer + " : " + validAnswer;
        FB.getInstance().setRecord(validAnswer);

        // ניקוי השדה
        edAnswer.setText("");

        // עדכון הרשימה לאחר השהייה קלה לסנכרון מול ה-Database
        new Handler().postDelayed(() -> {
            if (adapter != null) {
                adapter.notifyDataSetChanged();
            }
        }, 600);


        switchTurn();
    }

    private void endGame(int winnerPlayer) {
        gameOver = true;
        if (turnTimer != null) turnTimer.cancel();

        FB.getInstance().clearRecords();
        MainActivity.records.clear();
        if (adapter != null) adapter.notifyDataSetChanged();

        String message = "שחקן " + winnerPlayer + " ניצח! 🎉";
        Toast.makeText(this, message, Toast.LENGTH_LONG).show();
        btnSend.setEnabled(false);


    }
}