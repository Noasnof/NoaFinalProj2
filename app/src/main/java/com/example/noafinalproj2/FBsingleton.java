package com.example.noafinalproj2;

import androidx.annotation.NonNull;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;

public class FBsingleton {
    private static FBsingleton instance;
    public int gvihim;
    private FirebaseDatabase database;

    // ממשק שיעזור לנו להחזיר את מספר הגביעים ל-Activity
    public interface TrophiesCallback {
        void onTrophiesReceived(int trophies);
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

        // Pointing exactly to the score path in your database
        DatabaseReference myRef = database.getReference("Players/" + uid + "/MyDetails/score");

        // Tell Firebase to add cupsToAdd to the current value safely
        myRef.setValue(com.google.firebase.database.ServerValue.increment(cupsToAdd));
    }

}
