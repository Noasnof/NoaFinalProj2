package com.example.noafinalproj2;

import androidx.annotation.NonNull;

import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.Query;
import com.google.firebase.database.ValueEventListener;

// google explanations
// https://firebase.google.com/docs/database/android/lists-of-data#java_1


public class FB {
    private static FB instance;

    FirebaseDatabase database;

    private FB() {
        //database = FirebaseDatabase.getInstance("https://fbrecordssingletone-default-rtdb.firebaseio.com/");
        database = FirebaseDatabase.getInstance();
        // Test

        //this.records = MainActivity.records;

        // read the records from the Firebase and order them by the record from highest to lowest
        // limit to only 8 items
        Query myQuery = database.getReference("records").orderByChild("score").limitToLast(10);

        myQuery.addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange( DataSnapshot snapshot) {
                //records.clear();  // clear the array list
                MainActivity.records.clear();
                for(DataSnapshot userSnapshot : snapshot.getChildren())
                {
                    //String str =userSnapshot.child()  .getValue(Record.class);
                    Record currentRecord =userSnapshot.getValue(Record.class);
                    MainActivity.records.add(0, currentRecord);
                }
            }

            @Override
            public void onCancelled(@NonNull DatabaseError error) {
            }
        });


    }

    public static FB getInstance() {
        if (null == instance) {
            instance = new FB();
        }
        return instance;
    }

    public void setRecord(String name, int record)
    {
        // Write a message to the database
        DatabaseReference myRef = database.getReference("records").push(); // push adds new node with unique value

        //DatabaseReference myRef = database.getReference("
        // /" + FirebaseAuth.getInstance().getUid());

        Record rec = new Record(name, record);
        myRef.setValue(rec);
    }
}
