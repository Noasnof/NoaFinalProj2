package com.example.noafinalproj2;

import androidx.annotation.NonNull;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.MutableData;
import com.google.firebase.database.Query;
import com.google.firebase.database.Transaction;
import com.google.firebase.database.ValueEventListener;

public class FB {
    private static FB instance;
    private FirebaseDatabase database;

    private FB() {
        database = FirebaseDatabase.getInstance();
        Query myQuery = database.getReference("Answers").orderByChild("Answer");
        myQuery.addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {
                MainActivity.records.clear();
                for(DataSnapshot userSnapshot : snapshot.getChildren()) {
                    Record currentRecord = userSnapshot.getValue(Record.class);
                    MainActivity.records.add(0, currentRecord);
                }
            }
            @Override
            public void onCancelled(@NonNull DatabaseError error) {}
        });
    }

    public static FB getInstance() {
        if (null == instance) instance = new FB();
        return instance;
    }

    public void setRecord(String Answer) {
        DatabaseReference myRef = database.getReference("Answers").push();
        Record rec = new Record(Answer);
        myRef.setValue(rec);
    }

    public void clearRecords() {
        database.getReference("Answers").removeValue();
    }

    // עדכון גביעים לשחקן ספציפי (לפי UID)
    public void updateSpecificPlayerScore(String uid, int amount) {
        if (uid == null || uid.isEmpty()) return;
        DatabaseReference scoreRef = database.getReference("records/" + uid + "/MyDetails/score");

        scoreRef.runTransaction(new Transaction.Handler() {
            @NonNull
            @Override
            public Transaction.Result doTransaction(@NonNull MutableData currentData) {
                Integer currentScore = currentData.getValue(Integer.class);
                if (currentScore == null) currentScore = 100;
                int nextScore = currentScore + amount;
                if (nextScore < 0) nextScore = 0;
                currentData.setValue(nextScore);
                return Transaction.success(currentData);
            }
            @Override
            public void onComplete(DatabaseError error, boolean committed, DataSnapshot snapshot) {}
        });
    }

    public void updatePlayerScore(int amount) {
        updateSpecificPlayerScore(FirebaseAuth.getInstance().getUid(), amount);
    }
    // פונקציה לשליחת תשובה והעברת תור ליריב
    public void sendAnswerAndSwitchTurn(String gameId, String answer, String nextPlayerUid) {
        // 1. שמירת התשובה בתוך החדר הספציפי
        DatabaseReference gameRef = database.getReference("Games").child(gameId);
        gameRef.child("Answers").push().setValue(new Record(answer));

        // 2. העברת התור לשחקן הבא
        gameRef.child("currentTurn").setValue(nextPlayerUid);

        // 3. עדכון זמן תחילת התור (לסנכרון הטיימר)
        gameRef.child("lastTurnTimestamp").setValue(System.currentTimeMillis());
    }

    // נעדכן את המאזין ב-Constructor שיקשיב לחדר ספציפי (נשנה אותו ב-GameActivity)
    public void listenToGameAnswers(String gameId) {
        database.getReference("Games").child(gameId).child("Answers")
                .addValueEventListener(new ValueEventListener() {
                    @Override
                    public void onDataChange(@NonNull DataSnapshot snapshot) {
                        MainActivity.records.clear();
                        for(DataSnapshot ds : snapshot.getChildren()) {
                            MainActivity.records.add(0, ds.getValue(Record.class));
                        }
                        // כאן צריך לקרוא ל-adapter.notifyDataSetChanged() מה-Activity
                    }
                    @Override
                    public void onCancelled(@NonNull DatabaseError error) {}
                });
    }
}