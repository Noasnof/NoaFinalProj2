package com.example.noafinalproj2;

import static android.content.ContentValues.TAG;

import android.content.Intent;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.TextView;
import android.widget.Toast;

import androidx.activity.EdgeToEdge;
import androidx.appcompat.app.AppCompatActivity;

import com.example.noafinalproj2.gemini.GeminiCallback;
import com.example.noafinalproj2.gemini.GeminiManager;
import com.google.firebase.auth.FirebaseAuth;

import java.util.ArrayList;

public class MainActivity extends AppCompatActivity implements View.OnClickListener {

    public Button  btnAgainstAi, btnInstruction, btnLogout;
    TextView edGvihim;

    public static ArrayList<Record> records;

    FB fb;
    FBsingleton fBsingleton;
    int gvihim=0;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        EdgeToEdge.enable(this);
        setContentView(R.layout.activity_main);


        btnInstruction = findViewById(R.id.btnInstruction);
        btnInstruction.setOnClickListener(this);

        btnAgainstAi = findViewById(R.id.btnAgainstAi);
        btnAgainstAi.setOnClickListener(this);

        btnLogout = findViewById(R.id.btnLogout);
        btnLogout.setOnClickListener(this);




        edGvihim = findViewById(R.id.edGvihim);

        initialization();
    }

    @Override
    public void onClick(View v) {
        /*if (v == btnAgainstAi) {
            String prompt = "תן לי נושא אחד למשחק אסוסיאציות. תכתוב רק את הנושא בלי דברים נוספים. בעברית. תשובה עד שלוש מילים";

            GeminiManager.getInstance().sendMessage(prompt, new GeminiCallback() {

                @Override
                public void onSuccess(String response) {
                    runOnUiThread(() -> {
                        Log.d("TOPIC_DEBUG", response);

                        Intent intent = new Intent(MainActivity.this, GameActivity.class);
                        intent.putExtra("subject", response);
                        startActivity(intent);
                    });
                }

                @Override
                public void onError(Throwable e) {
                    Log.e(TAG, "Gemini error", e);
                    runOnUiThread(() ->
                            Toast.makeText(MainActivity.this,
                                    "שגיאה בקבלת נושא מה-AI",
                                    Toast.LENGTH_SHORT).show()
                    );
                }

                @Override
                public void onError(Exception e) {
                    Log.e(TAG, "Gemini exception", e);
                    runOnUiThread(() ->
                            Toast.makeText(MainActivity.this,
                                    "שגיאה: " + e.getMessage(),
                                    Toast.LENGTH_SHORT).show()
                    );
                }
            });
        }*/

        if (v == btnAgainstAi) { // Assuming you renamed the button to btnStartGame
            android.app.ProgressDialog dialog = new android.app.ProgressDialog(this);
            dialog.setMessage("מחפש יריב...");
            dialog.setCancelable(false);
            dialog.show();

            // 1. Try to join an existing game first
            fBsingleton.joinGameRoom(new FBsingleton.GameRoomCallback() {
                @Override
                public void onGameReady(String gameId, boolean isPlayer1) {
                    // Found a game! Let's play.
                    dialog.dismiss();
                    startGameActivity(gameId, isPlayer1);
                }

                @Override
                public void onError(String message) {
                    // No game found (or other error). Let's create one instead.
                    dialog.setMessage("מייצר משחק חדש, ממתין ליריב שיצטרף...");

                    // First, get a topic from Gemini
                    String prompt = "תן לי נושא אחד בלבד למשחק אסוציאציות. תכתוב רק את הנושא בלי דברים נוספים. בעברית. נושאים פשוטים.";

                    GeminiManager.getInstance().sendMessage(prompt, new GeminiCallback() {
                        @Override
                        public void onSuccess(String response) {
                            runOnUiThread(() -> {
                                // Topic received, now create the room
                                fBsingleton.createGameRoom(response, new FBsingleton.GameRoomCallback() {
                                    @Override
                                    public void onGameReady(String gameId, boolean isPlayer1) {
                                        dialog.dismiss();
                                        startGameActivity(gameId, isPlayer1);
                                    }

                                    @Override
                                    public void onError(String message) {
                                        dialog.dismiss();
                                        Toast.makeText(MainActivity.this, "שגיאה ביצירת חדר: " + message, Toast.LENGTH_SHORT).show();
                                    }
                                });
                            });
                        }

                        @Override
                        public void onError(Throwable e) {
                            runOnUiThread(() -> {
                                dialog.dismiss();
                                Toast.makeText(MainActivity.this, "שגיאה בקבלת נושא מ-AI", Toast.LENGTH_SHORT).show();
                            });
                        }

                        @Override
                        public void onError(Exception e) {
                            runOnUiThread(() -> {
                                dialog.dismiss();
                                Toast.makeText(MainActivity.this, "שגיאה: " + e.getMessage(), Toast.LENGTH_SHORT).show();
                            });
                        }
                    });
                }
            });
        }

        if (v == btnInstruction) {
            Intent i2 = new Intent(MainActivity.this, InstructionActivity.class);
            startActivity(i2);
        }

        if (v == btnLogout) {
            FirebaseAuth.getInstance().signOut();
            finish();
        }


    }

    private void initialization() {
        records = new ArrayList<>();
        fb = FB.getInstance();
        fBsingleton = FBsingleton.getInstance(); // אתחול הסינגלטון

        // קריאה לפונקציה שמושכת את הגביעים מה-Firebase ומציגה אותם
        fBsingleton.getTrophies(new FBsingleton.TrophiesCallback() {
            @Override
            public void onTrophiesReceived(int trophies) {
                // שמירה במשתנה המקומי
                gvihim = trophies;

                // הצגת המספר ב-EditText (חייב להמיר ל-String)
                if (edGvihim != null) {
                    edGvihim.setText(String.valueOf(gvihim));
                }
            }
        });
    }






    private void startGameActivity(String gameId, boolean isPlayer1) {
        Intent intent = new Intent(MainActivity.this, GameActivity.class);
        intent.putExtra("GAME_ID", gameId);
        intent.putExtra("IS_PLAYER_1", isPlayer1);
        startActivity(intent);
    }

}
