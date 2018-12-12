package com.example.nikoleta.coinz;

import android.content.Context;
import android.support.annotation.NonNull;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.BaseAdapter;
import android.widget.ListView;
import android.widget.TextView;

import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.firestore.DocumentReference;
import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.Query;
import com.google.firebase.firestore.QuerySnapshot;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

public class Ranking extends AppCompatActivity {

    ListView RankingList;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_ranking);
        RankingList = (ListView)findViewById(R.id.listViewRanking);

        FirebaseFirestore db = FirebaseFirestore.getInstance();
        FirebaseAuth firebaseAuth = FirebaseAuth.getInstance();
        FirebaseUser user = firebaseAuth.getCurrentUser();

        Query orderedUsers = db.collection("users").orderBy("money", Query.Direction.DESCENDING);
        orderedUsers.get().addOnCompleteListener(new OnCompleteListener<QuerySnapshot>() {
            @Override
            public void onComplete(@NonNull Task<QuerySnapshot> task) {
                TextView userPosition = (TextView) findViewById(R.id.current_position);

                List<String> userListOrdered = new ArrayList<>();
                List<DocumentSnapshot> documentsList = task.getResult().getDocuments();
                for (int i=0; i<documentsList.size(); i++) {
                    DocumentSnapshot document = documentsList.get(i);
                    userListOrdered.add(i+1 + "    " + (String) document.get("username"));
                    if(document.getId().equals(user.getUid())) {
                        int currPos = i+1;
                        if (currPos == 1) {
                            userPosition.setText("Congratulations, you are first!");
                        }
                        else {
                            if (currPos % 10 == 1) {
                                userPosition.setText("Your current postion is: " + currPos + "st.");
                            }
                            else if (currPos % 10 == 2) {
                                userPosition.setText("Your current postion is: " + currPos + "nd.");
                            }
                            else if (currPos % 10 == 3) {
                                userPosition.setText("Your current postion is: " + currPos + "rd.");
                            }
                            else {
                                userPosition.setText("Your current postion is: " + currPos + "th.");
                            }

                        }

                    }
                }

                String[] userArray= new String[userListOrdered.size()];
                userListOrdered.toArray(userArray);
                createListView(userArray);
            }
        });
    }
    public void createListView(String[] userArray) {
        ArrayAdapter<String> arrayAdapter = new ArrayAdapter<String>(this, R.layout.ranking_list_view, R.id.ranking_view, userArray);
        RankingList.setAdapter(arrayAdapter);
    }

}
