package com.example.nikoleta.coinz;

import android.annotation.SuppressLint;
import android.content.Intent;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.widget.ArrayAdapter;
import android.widget.ListView;
import android.widget.TextView;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.firestore.DocumentReference;
import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.Query;
import java.util.ArrayList;
import java.util.List;
import java.util.Objects;

public class Ranking extends AppCompatActivity {
    ListView RankingList;

    @SuppressLint("SetTextI18n")
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_ranking);
        RankingList = findViewById(R.id.listViewRanking);

        FirebaseFirestore db = FirebaseFirestore.getInstance();
        FirebaseAuth firebaseAuth = FirebaseAuth.getInstance();
        FirebaseUser user = firebaseAuth.getCurrentUser();

        // Sort the users in descending order based on their Bank money.
        Query orderedUsers = db.collection("users").orderBy("money", Query.Direction.DESCENDING);
        orderedUsers.get().addOnCompleteListener(task -> {
            TextView userPosition = findViewById(R.id.current_position);
            List<String> userListOrdered = new ArrayList<>();
            List<DocumentSnapshot> documentsList = Objects.requireNonNull(task.getResult()).getDocuments();
            for (int i = 0; i < documentsList.size(); i++) {
                DocumentSnapshot document = documentsList.get(i);
                userListOrdered.add(i + 1 + "    " + document.get("username"));
                assert user != null;
                if (document.getId().equals(user.getUid())) {
                    int currPos = i + 1;
                    if (currPos == 1) {
                        DocumentReference docRefUser = db.collection("users").document(user.getUid());
                        docRefUser.get().addOnCompleteListener(taskCurrentUser -> {
                            userPosition.setText("Congratulations, you are first!");
                            if(Objects.requireNonNull(taskCurrentUser.getResult()).contains("treasureUnlocked") && taskCurrentUser.getResult().contains("treasureFound") && documentsList.size() > 1) {
                                if (!(boolean) Objects.requireNonNull(Objects.requireNonNull(taskCurrentUser).getResult()).get("treasureUnlocked") ) {
                                    db.collection("users").document(user.getUid()).update("treasureUnlocked", true);
                                }
                                if(!(boolean) Objects.requireNonNull(Objects.requireNonNull(taskCurrentUser).getResult()).get("treasureFound")) {
                                    startActivity(new Intent(getApplicationContext(), TreasureActivity.class));
                                    finish();
                                }
                            }

                        });

                    }
                    else {
                        if (currPos % 10 == 1) {
                            userPosition.setText("Your current postion is: " + currPos + "st.");
                        } else if (currPos % 10 == 2) {
                            userPosition.setText("Your current postion is: " + currPos + "nd.");
                        } else if (currPos % 10 == 3) {
                            userPosition.setText("Your current postion is: " + currPos + "rd.");
                        } else {
                            userPosition.setText("Your current postion is: " + currPos + "th.");
                        }

                    }

                }
            }
            String[] userArray = new String[userListOrdered.size()];
            userListOrdered.toArray(userArray);
            createListView(userArray);
        });
    }

    public void createListView(String[] userArray) {
        ArrayAdapter<String> arrayAdapter = new ArrayAdapter<>(this, R.layout.ranking_list_view, R.id.ranking_view, userArray);
        RankingList.setAdapter(arrayAdapter);
    }

}
