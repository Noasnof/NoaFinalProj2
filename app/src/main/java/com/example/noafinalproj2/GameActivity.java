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
import androidx.annotation.NonNull;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.database.ValueEventListener;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;

import java.util.ArrayList;
import java.util.List;

public class GameActivity extends AppCompatActivity implements View.OnClickListener {

    private Button btnSend;
    private EditText edAnswer;
    private TextView tvAnswer;
    private RecordAdapter adapter;
    private CountDownTimer turnTimer;
    private TextView timerText;
    private TextView playerText;
    private static final long TURN_TIME = 30000;

    private boolean hasAnswered = false;
    private boolean gameOver = false;
    private String topic;

    private String gameID;
    private boolean isPlayer1;


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

        //topic = getIntent().getStringExtra("subject");
        //tvAnswer.setText(topic);

        gameID = getIntent().getStringExtra("GAME_ID");
        isPlayer1 = getIntent().getBooleanExtra("IS_PLAYER_1", true);

        if (gameID != null) {
            fetchGameData(gameID);
            listenToGameChanges();
            listenToAnswers();
        }

        setupRecyclerView();
        //startTurn();
    }

    private void fetchGameData(String gameId) {
        // כאן את פונה ל-Firebase לשלוף את הנושא (Topic)
        // אני משתמש בשם גנרי, תתאימי לשם הפונקציה ב-FBsingleton שלך
        FBsingleton.getInstance().getRoomData(gameId, new FBsingleton.RoomCallback() {
            @Override
            public void onDataRetrieved(String cloudTopic, String status) {
                runOnUiThread(() -> {
                    // ניקוי כוכביות אם קיימות והצגה
                    topic = cloudTopic.replace("*", "").trim();
                    tvAnswer.setText(topic);

                    // כאן אפשר גם לבדוק אם הסטטוס הוא "playing"
                    Log.d("GAME_DEBUG", "Topic loaded: " + topic);
                });
            }
        });
    }

    private void listenToGameChanges() {
        FBsingleton.getInstance().listenToRoom(gameID, new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {
                if (!snapshot.exists() || gameOver) return;

                String status = snapshot.child("status").getValue(String.class);
                String currentTurnUid = snapshot.child("currentTurn").getValue(String.class);
                String myUid = FirebaseAuth.getInstance().getUid();

                if ("finished".equals(status)) {
                    handleRemoteEndGame();
                    return;
                }

                boolean isMyTurn = myUid != null && myUid.equals(currentTurnUid);
                updateUIForTurn(isMyTurn);
            }

            @Override
            public void onCancelled(@NonNull DatabaseError error) {}
        });
    }

    // 3. עדכון הממשק לפי התור
    private void updateUIForTurn(boolean isMyTurn) {
        runOnUiThread(() -> {
            btnSend.setEnabled(isMyTurn);
            playerText.setText(isMyTurn ? "התור שלך!" : "ממתין ליריב...");
            if (isMyTurn) {
                startTurn();
            } else {
                if (turnTimer != null) turnTimer.cancel();
            }
        });
    }



    private void setupRecyclerView() {
        RecyclerView recyclerView = findViewById(R.id.recyclerView);
        recyclerView.setLayoutManager(new LinearLayoutManager(this));
        adapter = new RecordAdapter(this, MainActivity.records);
        recyclerView.setAdapter(adapter);
    }

    /* private void startTurn() {
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
    }*/

    private void startTurn() {
        hasAnswered = false;

        if (turnTimer != null) turnTimer.cancel();

        turnTimer = new CountDownTimer(TURN_TIME, 1000) {
            @Override
            public void onTick(long millisUntilFinished) {
                timerText.setText("זמן: " + millisUntilFinished / 1000);
            }

            @Override
            public void onFinish() {
                if (!hasAnswered && !gameOver) {
                    // אם הזמן נגמר, מי שלא ענה מפסיד - לכן winnerPlayer הוא היריב
                    endGame(isPlayer1 ? 2 : 1);
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
                        //usedWordsOnly.add(answerToCheck); // הוספה לרשימת הכפילויות
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

    /*private void proceedWithCorrectAnswer(String validAnswer) {
        hasAnswered = true;

        // שמירה לתצוגה ב-Firebase (עם שם השחקן)
        String displayMessage = "Player " + currentPlayer + " : " + validAnswer;
        FB.getInstance().setRecord(displayMessage);

        edAnswer.setText("");

        new Handler().postDelayed(() -> {
            if (adapter != null) adapter.notifyDataSetChanged();
        }, 600);

        switchTurn();
    }*/

    private void proceedWithCorrectAnswer(String validAnswer) {
        hasAnswered = true;

        // עדכון: משתמשים ב-isPlayer1 כדי לכתוב מי השחקן
        String displayMessage = "Player " + (isPlayer1 ? "1" : "2") + " : " + validAnswer;

        // כאן השורה של ה-setRecord נשארת (ודאי שהיא שולחת לחדר הנכון ב-FB)
        FB.getInstance().setRecord(gameID,displayMessage);

        edAnswer.setText("");

        new Handler().postDelayed(() -> {
            if (adapter != null) adapter.notifyDataSetChanged();
            switchTurn(); // זה יפעיל את ה-switchTurn החדש שמשתמש ב-Singleton
        }, 600);
    }

    /*private void switchTurn() {

        currentPlayer = (currentPlayer == 1) ? 2 : 1;
        startTurn();
    }*/

    private void switchTurn() {
        // מחשבים מי השחקן הבא
        int nextPlayerNum = isPlayer1 ? 2 : 1;

        // משתמשים בפונקציה שהוספנו ל-Singleton
        FBsingleton.getInstance().switchTurn(gameID, nextPlayerNum);
    }



    /*private void endGame(int winnerPlayer) {
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
    }*/


    private void handleRemoteEndGame() {
        if (!gameOver) {
            // אם הצד השני סיים, כנראה הוא ניצח (הזמן שלי נגמר או שהוא צבר נקודות)
            endGame(isPlayer1 ? 1 : 2);
        }
    }

    private void endGame(int winnerPlayer) {
        gameOver = true;
        if (turnTimer != null) turnTimer.cancel();

        // עדכון סטטוס ב-Firebase (כדי ששחקן 2 יראה שהמשחק נגמר)
        FBsingleton.getInstance().updateGameStatus(gameID, "finished");

        boolean iWon = (winnerPlayer == 1 && isPlayer1) || (winnerPlayer == 2 && !isPlayer1);

        if (iWon) {
            Toast.makeText(this, "הידד ניצחת ! התווספו לך 10 גביעים !", Toast.LENGTH_LONG).show();
            FBsingleton.getInstance().addTrophies(10);
        } else {
            Toast.makeText(this, "לא נורא חביבי ! תנסה פעם הבאה.", Toast.LENGTH_LONG).show();
            FBsingleton.getInstance().addTrophies(-5);
        }

        // ניקוי רשימות וסגירה
        if (isPlayer1) {
            FB.getInstance().clearRecords(gameID);
        }
        //FB.getInstance().clearRecords(gameID);
        MainActivity.records.clear();

        new Handler().postDelayed(this::finish, 2000);
    }

    private void listenToAnswers() {
        FBsingleton.getInstance().listenToAnswers(gameID, new FBsingleton.AnswersCallback() {
            @Override
            public void onAnswersChanged(List<Record> newRecords) {
                // עדכון הרשימה הגלובלית שבה ה-Adapter משתמש
                MainActivity.records.clear();
                MainActivity.records.addAll(newRecords);

                usedWordsOnly.clear();
                for (Record r : newRecords) {
                    if (r.getAnswer() != null) {
                        String raw = r.getAnswer().toLowerCase().trim();

                        if (raw.contains(":")) {
                            String[] parts = raw.split(":");
                            if (parts.length > 1) {
                                usedWordsOnly.add(parts[1].trim());
                            }
                        } else {
                            usedWordsOnly.add(raw);
                        }
                    }
                }

                // עדכון ה-UI על המסך
                runOnUiThread(() -> {
                    if (adapter != null) {
                        adapter.notifyDataSetChanged();
                    }
                });
            }
        });
    }
}