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

import java.util.ArrayList;
import java.util.List;

public class GameActivity extends AppCompatActivity implements View.OnClickListener {

    private Button btnSend;
    private EditText edAnswer;
    private TextView tvAnswer;
    private RecordAdapter adapter;
    private int currentPlayer = 1;
    private CountDownTimer turnTimer;
    private TextView timerText;
    private TextView playerText;
    private static final long TURN_TIME = 30000;

    private boolean hasAnswered = false;
    private boolean gameOver = false;
    private String topic;


    // רשימה עזר פנימית לבדיקת כפילויות בצורה נקייה
    private List<String> usedWordsOnly = new ArrayList<>();

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        EdgeToEdge.enable(this);
        setContentView(R.layout.activity_game);

        btnSend = findViewById(R.id.btnSend);
        btnSend.setOnClickListener(this);
        edAnswer = findViewById(R.id.edAnswer);
        tvAnswer = findViewById(R.id.tvAnswer);
        timerText = findViewById(R.id.timerText);
        playerText = findViewById(R.id.playerText);

        topic = getIntent().getStringExtra("subject");
        tvAnswer.setText(topic);

        setupRecyclerView();
        startTurn();
    }

    private void setupRecyclerView() {
        RecyclerView recyclerView = findViewById(R.id.recyclerView);
        recyclerView.setLayoutManager(new LinearLayoutManager(this));
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

    @Override
    public void onClick(View v) {
        if (v == btnSend && !gameOver) {
            String answer = edAnswer.getText().toString().trim().toLowerCase();

            if (answer.isEmpty()) {
                Toast.makeText(this, "יש להזין תשובה", Toast.LENGTH_SHORT).show();
                return;
            }

            // בדיקת כפילויות מול הרשימה הנקייה
            if (usedWordsOnly.contains(answer)) {
                Toast.makeText(this, "המילה '" + answer + "' כבר נאמרה!", Toast.LENGTH_SHORT).show();
                edAnswer.setText("");
                return;
            }

            checkAnswerWithAI(answer);
        }
    }

    private void checkAnswerWithAI(String answerToCheck) {
        btnSend.setEnabled(false);

        // פרומפט משופר ונוקשה
        String prompt = "You are a strict game judge. Category: " + topic + ". " +
                "The player said: '" + answerToCheck + "'. " +
                "Is this word clearly a member of the category? " +
                "Answer ONLY 'true' or 'false'. Be very strict. " +
                "If the word is nonsense or unrelated, answer 'false'.";

        GeminiManager.getInstance().sendMessage(prompt, new GeminiCallback() {
            @Override
            public void onSuccess(String response) {
                // ניקוי עדין - בלי Regex מוגזם
                String cleanResponse = response.toLowerCase().trim();

                runOnUiThread(() -> {
                    btnSend.setEnabled(true);

                    // בדיקה אם התשובה מכילה true ולא false
                    if (cleanResponse.contains("true") && !cleanResponse.contains("false")) {
                        usedWordsOnly.add(answerToCheck); // הוספה לרשימת הכפילויות
                        proceedWithCorrectAnswer(answerToCheck);
                    } else {
                        Toast.makeText(GameActivity.this,
                                "'" + answerToCheck + "' לא קשור ל" + topic + "!", Toast.LENGTH_SHORT).show();
                        edAnswer.setText("");
                    }
                });
            }

            @Override
            public void onError(Throwable e) { handleAIError(); }
            @Override
            public void onError(Exception e) { handleAIError(); }
        });
    }

    private void handleAIError() {
        runOnUiThread(() -> {
            btnSend.setEnabled(true);
            Toast.makeText(GameActivity.this, "שגיאת תקשורת, נסה שוב", Toast.LENGTH_SHORT).show();
        });
    }

    private void proceedWithCorrectAnswer(String validAnswer) {
        hasAnswered = true;

        // שמירה לתצוגה ב-Firebase (עם שם השחקן)
        String displayMessage = "Player " + currentPlayer + " : " + validAnswer;
        FB.getInstance().setRecord(displayMessage);

        edAnswer.setText("");

        new Handler().postDelayed(() -> {
            if (adapter != null) adapter.notifyDataSetChanged();
        }, 600);

        switchTurn();
    }

    private void switchTurn() {

        currentPlayer = (currentPlayer == 1) ? 2 : 1;
        startTurn();
    }



    private void endGame(int winnerPlayer) {
        gameOver = true;
        if (turnTimer != null) turnTimer.cancel();

        FB.getInstance().clearRecords();
        MainActivity.records.clear();
        usedWordsOnly.clear(); // איפוס כפילויות בסוף משחק

        if (adapter != null) adapter.notifyDataSetChanged();
        Toast.makeText(this, "שחקן " + winnerPlayer + " ניצח! 🎉 התווספו 10 גביעים!", Toast.LENGTH_LONG).show();
        btnSend.setEnabled(false);

        FBsingleton.getInstance().addTrophies(10);

        new Handler().postDelayed(() -> {
            finish();
        }, 2000);
    }
}