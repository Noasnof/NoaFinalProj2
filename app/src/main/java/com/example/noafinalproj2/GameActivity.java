package com.example.noafinalproj2;

import android.os.Bundle;
import android.os.CountDownTimer;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.TextView;
import android.widget.Toast;

import androidx.activity.EdgeToEdge;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.graphics.Insets;
import androidx.core.view.ViewCompat;
import androidx.core.view.WindowInsetsCompat;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

public class GameActivity extends AppCompatActivity implements View.OnClickListener {

    private Button btnSend;
    private EditText edAnswer;
    private TextView tvAnswer;
    private RecordAdapter adapter;
    private int currentPlayer = 1;
    private CountDownTimer turnTimer;
    private TextView timerText;
    private TextView playerText;
    private static final long TURN_TIME = 30_000; // 30 שניות
    private boolean hasAnswered = false;
    private boolean gameOver = false;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        EdgeToEdge.enable(this);
        setContentView(R.layout.activity_game);

        btnSend=findViewById(R.id.btnSend);
        btnSend.setOnClickListener(this);
        edAnswer=findViewById(R.id.edAnswer);
        tvAnswer=findViewById(R.id.tvAnswer);
        String topic=getIntent().getStringExtra("subject");
         tvAnswer.setText(topic);
        timerText = findViewById(R.id.timerText);
        playerText = findViewById(R.id.playerText);

        startTurn();


    }

    private void startTurn() {
        hasAnswered = false;

        playerText.setText("תור של שחקן " + currentPlayer);

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
        if (turnTimer != null) {
            turnTimer.cancel();
        }

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

            hasAnswered = true;

            FB.getInstance().setRecord(answer);

            switchTurn();
            edAnswer.setText("");
            initialization();
        }
    }


    private void initialization() {
        // initialize

        RecyclerView recyclerView = findViewById(R.id.recyclerView);
        recyclerView.setLayoutManager(new LinearLayoutManager(this)); // to be vertical

        adapter = new RecordAdapter(this, MainActivity.records);
        recyclerView.setAdapter(adapter);
    }
    private void endGame(int winnerPlayer) {
        gameOver = true;

        if (turnTimer != null) {
            turnTimer.cancel();
        }

        String message = "שחקן " + winnerPlayer + " ניצח! 🎉";
        Toast.makeText(this, message, Toast.LENGTH_LONG).show();

        btnSend.setEnabled(false);
    }

}