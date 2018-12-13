package com.example.nikoleta.coinz;

import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.app.Activity;
import android.view.inputmethod.EditorInfo;
import android.view.inputmethod.InputMethodManager;
import android.widget.ArrayAdapter;
import android.widget.EditText;
import android.widget.ListView;
import android.widget.TextView;
import android.widget.Toast;
import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.Query;
import com.google.firebase.firestore.QuerySnapshot;
import java.util.ArrayList;
import java.util.List;
import java.util.Objects;

public class SelectUserActivity extends Activity
{
    ListView usernameList;
    static String selectedUser;
    static boolean sendCoins = false;
    static boolean stealCoins = false;

    @Override   protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_select_user);
        usernameList = findViewById(R.id.listView);
        FirebaseFirestore db = FirebaseFirestore.getInstance();
        FirebaseAuth firebaseAuth = FirebaseAuth.getInstance();
        final FirebaseUser[] user = {firebaseAuth.getCurrentUser()};

        // Display in a ListView all the users except the current user.
        Query users = db.collection("users");
        Task<QuerySnapshot> snapshotTask = users.get();
        snapshotTask.addOnCompleteListener(task -> {
            List<String> userList = new ArrayList<>();
            List<DocumentSnapshot> documentsList = Objects.requireNonNull(snapshotTask.getResult()).getDocuments();
            for (DocumentSnapshot document : documentsList) {
                if (!document.getId().equals(user[0].getUid())) {
                    userList.add((String) document.get("username"));
                }
            }
            String[] userArray= new String[userList.size()];
            userList.toArray(userArray);
            createListView(userArray);
        });

        usernameList.setOnItemClickListener((adapterView, view, position, l) -> {
            TextView selectedUserView = adapterView.getChildAt(position).findViewById(R.id.textView);
            selectedUser = selectedUserView.getText().toString();

            if (sendCoins && !stealCoins) {
                startActivity(new Intent(getApplicationContext(), SelectCoinGiftsActivity.class));
            }

            finish();
            if (!sendCoins && stealCoins) {
                StealActivity.stealCoin(getApplicationContext());
            }

        });

        // Search for a user by writing his username.
        EditText search = findViewById(R.id.search_text);
        search.setOnEditorActionListener((textView, actionId, keyEvent) -> {
            // Triggered when done editing (by clicking search on keyboard).
            if (actionId == EditorInfo.IME_ACTION_SEARCH) {
                InputMethodManager imm = (InputMethodManager)getSystemService(Context.INPUT_METHOD_SERVICE);
                assert imm != null;
                imm.hideSoftInputFromWindow(search.getWindowToken(), 0);
                search.clearFocus();
                String searchUsername = search.getText().toString();
                if (searchUsername.equals("")) {
                    Query users1 = db.collection("users");
                    Task<QuerySnapshot> snapshotTask2 = users1.get();
                    snapshotTask2.addOnCompleteListener(task -> {
                        List<String> userList = new ArrayList<>();
                        List<DocumentSnapshot> documentsList = Objects.requireNonNull(snapshotTask2.getResult()).getDocuments();
                        for (DocumentSnapshot document : documentsList) {
                            if (!document.getId().equals(user[0].getUid())) {
                                userList.add((String) document.get("username"));
                            }
                        }
                        String[] userArray= new String[userList.size()];
                        userList.toArray(userArray);
                        createListView(userArray);
                    });
                }
                else {
                    Query searchUsers = db.collection("users").whereEqualTo("username", searchUsername);
                    Task<QuerySnapshot> task = searchUsers.get();
                    task.addOnCompleteListener(task1 -> {
                        List<DocumentSnapshot> documentsList = Objects.requireNonNull(task1.getResult()).getDocuments();
                        if(documentsList.isEmpty()) {
                            Toast.makeText(SelectUserActivity.this, "No such user.",
                                    Toast.LENGTH_SHORT).show();
                        }
                        else {
                            List<String> userList = new ArrayList<>();
                            for (DocumentSnapshot document : documentsList) {
                                userList.add((String) document.get("username"));
                            }
                            String[] userArray= new String[userList.size()];
                            userList.toArray(userArray);
                            createListView(userArray);
                        }
                    });
                }

            }
            return false;
        });
    }
    
    public void createListView(String[] userArray) {
        ArrayAdapter<String> arrayAdapter = new ArrayAdapter<>(this, R.layout.activity_list_view, R.id.textView, userArray);
        usernameList.setAdapter(arrayAdapter);
    }
}