package com.example.noafinalproj2;

import static android.content.ContentValues.TAG;
import android.content.Intent;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Toast;
import androidx.activity.EdgeToEdge;
import androidx.appcompat.app.AppCompatActivity;
import com.example.noafinalproj2.gemini.GeminiCallback;
import com.example.noafinalproj2.gemini.GeminiManager;
import com.google.firebase.auth.FirebaseAuth;
import java.util.ArrayList;

public class MainActivity extends AppCompatActivity implements View.OnClickListener {

    public Button btnAgainstPlayer, btnAgainstAi, btnInstruction, btnLogout, btnNewPlayer;
    EditText edGvihim;
    public static ArrayList<Record> records;
    FB fb;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        EdgeToEdge.enable(this);
        setContentView(R.layout.activity_main);

        btnAgainstPlayer = findViewById(R.id.btnAgainstPlayer);
        btnAgainstPlayer.setOnClickListener(this);

        btnInstruction = findViewById(R.id.btnInstruction);
        btnInstruction.setOnClickListener(this);

        btnAgainstAi = findViewById(R.id.btnAgainstAi);
        btnAgainstAi.setOnClickListener(this);

        btnLogout = findViewById(R.id.btnLogout);
        btnLogout.setOnClickListener(this);

        btnNewPlayer = findViewById(R.id.btnNewPlayer);
        btnNewPlayer.setOnClickListener(this);

        edGvihim = findViewById(R.id.edGvihim);

        initialization();
    }

    @Override
    public void onClick(View v) {
        if (v == btnAgainstPlayer) {
            // מעבר למסך ההמתנה לחיפוש שחקן נוסף
            Intent i = new Intent(MainActivity.this, LobbyActivity.class);
            startActivity(i);
        }

        if (v == btnAgainstAi) {
            String prompt = "תן לי נושא אחד למשחק אסוסיאציות. תכתוב רק את הנושא בלי דברים נוספים. בעברית. תשובה עד שלוש מילים";
            GeminiManager.getInstance().sendMessage(prompt, new GeminiCallback() {
                @Override
                public void onSuccess(String response) {
                    runOnUiThread(() -> {
                        Intent intent = new Intent(MainActivity.this, LobbyActivity.class);
                        intent.putExtra("subject", response);
                        // במשחק נגד AI, השחקן השני הוא מזהה קבוע
                        intent.putExtra("PLAYER1_UID", FirebaseAuth.getInstance().getUid());
                        intent.putExtra("PLAYER2_UID", "AI_PLAYER_ID");
                        startActivity(intent);
                    });
                }
                @Override public void onError(Throwable e) { Log.e(TAG, "Error", e); }
                @Override public void onError(Exception e) { Log.e(TAG, "Exception", e); }
            });
        }

        if (v == btnInstruction) {
            startActivity(new Intent(MainActivity.this, InstructionActivity.class));
        }

        if (v == btnLogout) {
            FirebaseAuth.getInstance().signOut();
            finish();
        }

        if (v == btnNewPlayer) {
            startActivity(new Intent(MainActivity.this, CreatePlayer.class));
        }
    }

    private void initialization() {
        records = new ArrayList<>();
        fb = FB.getInstance();
    }
}