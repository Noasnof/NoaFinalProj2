package com.example.noafinalproj2;

import androidx.annotation.NonNull;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;

import java.util.HashMap;
import java.util.ArrayList;
import java.util.List;

public class FBsingleton {
    private static FBsingleton instance;
    public int gvihim;
    private FirebaseDatabase database;

    // ממשק שיעזור לנו להחזיר את מספר הגביעים ל-Activity
    public interface TrophiesCallback {
        void onTrophiesReceived(int trophies);
    }

    public interface RoomCallback {
        void onDataRetrieved(String topic, String status);
    }

    public interface GameRoomCallback {
        void onGameReady(String gameId, boolean isPlayer1);
        void onError(String message);
    }

    public interface AnswersCallback {
        void onAnswersChanged(List<Record> records);
    }

    protected FBsingleton() {
        database = FirebaseDatabase.getInstance();
    }

    public static FBsingleton getInstance() {
        if (null == instance) {
            instance = new FBsingleton();
        }
        return instance;
    }

    // פונקציה חדשה שמושכת את מספר הגביעים מה-Firebase
    public void getTrophies(TrophiesCallback callback) {
        String uid = FirebaseAuth.getInstance().getUid();
        if (uid == null) return;

        // הנתיב שבו הניקוד שמור (לפי setDetails שלך)
        DatabaseReference myRef = database.getReference("Players/" + uid + "/MyDetails/score");

        myRef.addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {
                if (snapshot.exists()) {
                    Integer score = snapshot.getValue(Integer.class);
                    if (score != null) {
                        callback.onTrophiesReceived(score);
                    }
                } else {
                    callback.onTrophiesReceived(0); // אם אין נתונים, נציג 0
                }
            }

            @Override
            public void onCancelled(@NonNull DatabaseError error) {
                // שגיאה בקריאה
            }
        });
    }

    public void setName(String name) {
        DatabaseReference myRef = database.getReference("Players/" + FirebaseAuth.getInstance().getUid() + "/MyName");
        myRef.setValue(name);
    }

    public void setDetails(int score) {
        DatabaseReference myRef = database.getReference("Players/" + FirebaseAuth.getInstance().getUid() + "/MyDetails");
        // כאן את יוצרת אובייקט עם הניקוד שקיבלת
        MyDetailsInFb rec = new MyDetailsInFb(score);
        myRef.setValue(rec);
        gvihim=score;
    }

    public void addTrophies(int cupsToAdd) {
        String uid = FirebaseAuth.getInstance().getUid();
        if (uid == null) return;

        DatabaseReference myRef = database.getReference("Players/" + uid + "/MyDetails/score");
        myRef.setValue(com.google.firebase.database.ServerValue.increment(cupsToAdd));
    }

    public int getGvihim() {
        return gvihim;
    }

    public void createGameRoom(String topic, GameRoomCallback callback) {
        String uid = FirebaseAuth.getInstance().getUid();
        if (uid == null) {
            callback.onError("User not logged in");
            return;
        }

        DatabaseReference gamesRef = database.getReference("Games");
        String gameId = gamesRef.push().getKey();

        if (gameId == null) {
            callback.onError("Failed to generate game ID");
            return;
        }

        // הגדרת נתוני החדר ההתחלתיים
        HashMap<String, Object> roomData = new HashMap<>();
        roomData.put("status", "waiting");
        roomData.put("player1_id", uid);
        roomData.put("topic", topic);
        roomData.put("currentTurn", uid);

        gamesRef.child(gameId).setValue(roomData).addOnCompleteListener(task -> {
            if (task.isSuccessful()) {
                // החדר נוצר, עכשיו מאזינים כדי לראות מתי הסטטוס משתנה ל-playing
                gamesRef.child(gameId).child("status").addValueEventListener(new ValueEventListener() {
                    @Override
                    public void onDataChange(@NonNull DataSnapshot snapshot) {
                        String status = snapshot.getValue(String.class);
                        if ("playing".equals(status)) {
                            // שחקן שני הצטרף! מפסיקים להאזין ומעבירים למשחק
                            gamesRef.child(gameId).child("status").removeEventListener(this);
                            callback.onGameReady(gameId, true); // true כי אנחנו Host (שחקן 1)
                        }
                    }

                    @Override
                    public void onCancelled(@NonNull DatabaseError error) {
                        callback.onError(error.getMessage());
                    }
                });
            } else {
                callback.onError("Error creating room");
            }
        });
    }

    public void joinGameRoom(GameRoomCallback callback) {
        String uid = FirebaseAuth.getInstance().getUid();
        if (uid == null) {
            callback.onError("User not logged in");
            return;
        }

        DatabaseReference gamesRef = database.getReference("Games");

        // חיפוש חדר אחד בלבד שהסטטוס שלו הוא "waiting"
        gamesRef.orderByChild("status").equalTo("waiting").limitToFirst(1)
                .addListenerForSingleValueEvent(new ValueEventListener() {
                    @Override
                    public void onDataChange(@NonNull DataSnapshot snapshot) {
                        if (snapshot.exists()) {
                            for (DataSnapshot roomSnapshot : snapshot.getChildren()) {
                                String gameId = roomSnapshot.getKey();

                                if (gameId != null) {
                                    // מצאנו חדר! נרשמים בתור שחקן 2 ומשנים סטטוס כדי להתחיל
                                    gamesRef.child(gameId).child("player2_id").setValue(uid);
                                    gamesRef.child(gameId).child("status").setValue("playing");

                                    callback.onGameReady(gameId, false); // false כי אנחנו Guest (שחקן 2)
                                    return;
                                }
                            }
                        } else {
                            callback.onError("לא נמצאו חדרים פנויים. נסה לפתוח חדר בעצמך!");
                        }
                    }

                    @Override
                    public void onCancelled(@NonNull DatabaseError error) {
                        callback.onError(error.getMessage());
                    }
                });
    }

    // פונקציה למשיכת נתונים על החדר (נושא וסטטוס)
    // פונקציה למשיכת נתונים על החדר (נושא וסטטוס)
    public void getRoomData(String gameId, RoomCallback callback) {
        // שיניתי מ-mDatabase ל-database
        database.getReference("Games").child(gameId).get().addOnSuccessListener(snapshot -> {
            if (snapshot.exists()) {
                String topic = snapshot.child("topic").getValue(String.class);
                String status = snapshot.child("status").getValue(String.class);
                if (callback != null) {
                    callback.onDataRetrieved(topic, status);
                }
            }
        });
    }

    // פונקציה לעדכון סטטוס המשחק (למשל מ-"playing" ל-"finished")
    public void updateGameStatus(String gameId, String newStatus) {
        if (gameId != null) {
            // שיניתי מ-mDatabase ל-database והוספתי getReference("Games")
            database.getReference("Games").child(gameId).child("status").setValue(newStatus)
                    .addOnSuccessListener(aVoid -> android.util.Log.d("FB_DEBUG", "Status updated to: " + newStatus))
                    .addOnFailureListener(e -> android.util.Log.e("FB_DEBUG", "Failed to update status", e));
        }
    }

    public void listenToRoom(String gameId, ValueEventListener listener) {
        if (gameId != null) {
            database.getReference("Games").child(gameId).addValueEventListener(listener);
        }
    }

    // 2. החלפת תור ב-Firebase
    // הפונקציה מושכת את ה-UID של השחקן הבא ומעדכנת את currentTurn
    public void switchTurn(String gameId, int nextPlayerNum) {
        if (gameId == null) return;

        DatabaseReference gameRef = database.getReference("Games").child(gameId);
        gameRef.get().addOnSuccessListener(snapshot -> {
            if (snapshot.exists()) {
                // שולף את ה-player1_id או player2_id לפי המספר ששלחנו
                String nextPlayerId = snapshot.child("player" + nextPlayerNum + "_id").getValue(String.class);
                if (nextPlayerId != null) {
                    gameRef.child("currentTurn").setValue(nextPlayerId);
                }
            }
        });
    }

    public void removeRoomListener(String gameId, ValueEventListener listener) {
        if (gameId != null && listener != null) {
            database.getReference("Games").child(gameId).removeEventListener(listener);
        }
    }

    // 2. הפונקציה שמאזינה למילים בחדר ספציפי
    public void listenToAnswers(String gameId, AnswersCallback callback) {
        if (gameId == null) return;

        DatabaseReference answersRef = database.getReference("Games").child(gameId).child("Answers");

        answersRef.addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {
                List<Record> tempRecords = new ArrayList<>();
                for (DataSnapshot dataSnapshot : snapshot.getChildren()) {
                    Record record = dataSnapshot.getValue(Record.class);
                    if (record != null) {
                        tempRecords.add(0, record); // מוסיף להתחלה
                    }
                }
                // שולח את הרשימה המעודכנת חזרה ל-Activity
                if (callback != null) {
                    callback.onAnswersChanged(tempRecords);
                }
            }

            @Override
            public void onCancelled(@NonNull DatabaseError error) {}
        });
    }


}
