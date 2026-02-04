package com.example.noafinalproj2;

import android.content.Intent;
import android.os.Bundle;
import android.os.Handler;
import android.view.View;
import android.widget.Button;
import android.widget.TextView;
import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;

public class LobbyActivity extends AppCompatActivity {
    private DatabaseReference roomRef;
    private String myUid;
    private ValueEventListener lobbyListener;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_lobby);

        myUid = FirebaseAuth.getInstance().getUid();
        // צומת ב-Firebase שבו נחכה ליריב
        roomRef = FirebaseDatabase.getInstance().getReference("GameRooms").child("waiting_room");

        checkAndJoinRoom();

        Button btnCancel = findViewById(R.id.btnCancelLobby);
        btnCancel.setOnClickListener(v -> {
            roomRef.removeValue(); // מבטל את החדר אם יצאנו
            finish();
        });
    }

    private void checkAndJoinRoom() {
        roomRef.addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {
                if (!snapshot.exists()) {
                    // החדר ריק - אני שחקן 1. אני נרשם ומחכה.
                    roomRef.child("p1").setValue(myUid);
                    waitForPlayer2();
                } else if (snapshot.hasChild("p1") && !snapshot.hasChild("p2")) {
                    // שחקן 1 כבר מחכה - אני שחקן 2.
                    String p1Uid = snapshot.child("p1").getValue(String.class);
                    roomRef.child("p2").setValue(myUid);
                    // עוברים למשחק מיד כשחקן 2
                    startGame(p1Uid, myUid);
                }
            }
            @Override public void onCancelled(@NonNull DatabaseError error) {}
        });
    }

    private void waitForPlayer2() {
        lobbyListener = roomRef.addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {
                // אם שחקן 2 הצטרף, ה-Listener של שחקן 1 יזהה זאת
                if (snapshot.hasChild("p2")) {
                    String p2Uid = snapshot.child("p2").getValue(String.class);
                    roomRef.removeEventListener(this);

                    // מנקים את החדר ועוברים למשחק
                    new Handler().postDelayed(() -> {
                        roomRef.removeValue();
                        startGame(myUid, p2Uid);
                    }, 1500);
                }
            }
            @Override public void onCancelled(@NonNull DatabaseError error) {}
        });
    }

    private void startGame(String p1, String p2) {
        String subject = getIntent().getStringExtra("subject"); // הנושא מ-MainActivity
        String gameId = p1; // ה-UID של שחקן 1

        // רק שחקן 1 מעדכן את הגדרות המשחק בשרת
        if (FirebaseAuth.getInstance().getUid().equals(p1)) {
            DatabaseReference gameRef = FirebaseDatabase.getInstance().getReference("Games").child(gameId);
            gameRef.child("subject").setValue(subject);
            gameRef.child("currentTurn").setValue(p1); // שחקן 1 מתחיל
            gameRef.child("Answers").removeValue(); // מנקים תשובות ישנות
        }

        Intent intent = new Intent(this, GameActivity.class);
        intent.putExtra("GAME_ID", gameId);
        intent.putExtra("PLAYER1_UID", p1);
        intent.putExtra("PLAYER2_UID", p2);
        // אנחנו כבר לא מסתמכים על האינטנט לנושא, אלא נקרא מה-Firebase
        startActivity(intent);
        finish();
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        if (lobbyListener != null) {
            roomRef.removeEventListener(lobbyListener);
        }
    }
}