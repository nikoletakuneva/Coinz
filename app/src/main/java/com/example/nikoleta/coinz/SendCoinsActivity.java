package com.example.nikoleta.coinz;

import android.os.Bundle;
import android.app.Activity;
import android.support.annotation.NonNull;
import android.view.Menu;
import android.widget.ArrayAdapter;import android.widget.ListView;

import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.Query;
import com.google.firebase.firestore.QuerySnapshot;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

public class SendCoinsActivity extends Activity
{
    ListView usernameList;



    @Override   protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_send_coins);
        usernameList = (ListView)findViewById(R.id.listView);
        FirebaseFirestore db = FirebaseFirestore.getInstance();
        FirebaseAuth firebaseAuth = FirebaseAuth.getInstance();
        FirebaseUser user = firebaseAuth.getCurrentUser();

        Query users = db.collection("users");
        Task<QuerySnapshot> snapshotTask = users.get();
        Task<QuerySnapshot> snapshotTask1 = snapshotTask.addOnCompleteListener(new OnCompleteListener<QuerySnapshot>() {
            @Override
            public void onComplete(@NonNull Task<QuerySnapshot> task) {
                List<String> userList = new ArrayList<>();
                List<DocumentSnapshot> documentsList = snapshotTask.getResult().getDocuments();
                for (DocumentSnapshot document : documentsList) {
                    if (!document.getId().equals(user.getUid())) {
                        userList.add((String) document.get("username"));
                    }
                }
                String[] userArray= new String[userList.size()];
                userList.toArray(userArray);
                createListView(userArray);
            }
        });
    }
    
    public void createListView(String[] userArray) {
        ArrayAdapter<String> arrayAdapter = new ArrayAdapter<String>(this, R.layout.activity_list_view, R.id.textView, userArray);
        usernameList.setAdapter(arrayAdapter);
    }
}