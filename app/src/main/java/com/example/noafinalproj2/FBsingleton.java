package com.example.noafinalproj2;

import androidx.annotation.NonNull;

import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.Query;
import com.google.firebase.database.ValueEventListener;

// google explanations
// https://firebase.google.com/docs/database/android/lists-of-data#java_1


public class FBsingleton {
    private static FBsingleton instance;

    FirebaseDatabase database;

    protected FBsingleton() {
        database = FirebaseDatabase.getInstance();

        // read the records from the Firebase and order them by the record from highest to lowest
        // limit to only 8 items
        Query myQuery = database.getReference("records").orderByChild("score").limitToLast(10);

        myQuery.addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange( DataSnapshot snapshot) {
                database = FirebaseDatabase.getInstance();
            }

            @Override
            public void onCancelled(@NonNull DatabaseError error) {
            }
        });


    }

    public static FBsingleton getInstance() {
        if (null == instance) {
            instance = new FBsingleton();
        }
        return instance;
    }

    public void setName(String name)
    {
        // Write a message to the database
        //DatabaseReference myRef = database.getReference("records").push(); // push adds new node with unique value

        DatabaseReference myRef = database.getReference("records/" + FirebaseAuth.getInstance().getUid() + "/MyName");

        myRef.setValue(name);
    }



}
