package com.example.nikoleta.coinz;

import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.app.Activity;
import android.support.annotation.NonNull;
import android.view.KeyEvent;
import android.view.View;
import android.view.inputmethod.EditorInfo;
import android.view.inputmethod.InputMethodManager;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.EditText;
import android.widget.ListView;
import android.widget.TextView;
import android.widget.Toast;

import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.Query;
import com.google.firebase.firestore.QuerySnapshot;

import java.util.ArrayList;
import java.util.List;

public class SelectUserActivity extends Activity
{
    ListView usernameList;
    static String selectedUser;

    @Override   protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_select_user);
        usernameList = (ListView)findViewById(R.id.listView);
        FirebaseFirestore db = FirebaseFirestore.getInstance();
        FirebaseAuth firebaseAuth = FirebaseAuth.getInstance();
        final FirebaseUser[] user = {firebaseAuth.getCurrentUser()};

        Query users = db.collection("users");
        Task<QuerySnapshot> snapshotTask = users.get();
        Task<QuerySnapshot> snapshotTask1 = snapshotTask.addOnCompleteListener(new OnCompleteListener<QuerySnapshot>() {
            @Override
            public void onComplete(@NonNull Task<QuerySnapshot> task) {
                List<String> userList = new ArrayList<>();
                List<DocumentSnapshot> documentsList = snapshotTask.getResult().getDocuments();
                for (DocumentSnapshot document : documentsList) {
                    if (!document.getId().equals(user[0].getUid())) {
                        userList.add((String) document.get("username"));
                    }
                }
                String[] userArray= new String[userList.size()];
                userList.toArray(userArray);
                createListView(userArray);
            }
        });

        usernameList.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> adapterView, View view, int position, long l) {
                TextView selectedUserView = adapterView.getChildAt(position).findViewById(R.id.textView);
                selectedUser = selectedUserView.getText().toString();

                startActivity(new Intent(getApplicationContext(), SelectCoinGiftsActivity.class));
                finish();
            }
        });

        EditText search = (EditText) findViewById(R.id.search_text);
        search.setOnEditorActionListener(new TextView.OnEditorActionListener() {
            @Override
            public boolean onEditorAction(TextView textView, int actionId,
                                          KeyEvent keyEvent) { //triggered when done editing (as clicked done on keyboard)
                if (actionId == EditorInfo.IME_ACTION_SEARCH) {
                    InputMethodManager imm = (InputMethodManager)getSystemService(Context.INPUT_METHOD_SERVICE);
                    imm.hideSoftInputFromWindow(search.getWindowToken(), 0);
                    search.clearFocus();
                    String searchUsername = search.getText().toString();
                    if (searchUsername.equals("")) {
                        Query users = db.collection("users");
                        Task<QuerySnapshot> snapshotTask = users.get();
                        Task<QuerySnapshot> snapshotTask1 = snapshotTask.addOnCompleteListener(new OnCompleteListener<QuerySnapshot>() {
                            @Override
                            public void onComplete(@NonNull Task<QuerySnapshot> task) {
                                List<String> userList = new ArrayList<>();
                                List<DocumentSnapshot> documentsList = snapshotTask.getResult().getDocuments();
                                for (DocumentSnapshot document : documentsList) {
                                    if (!document.getId().equals(user[0].getUid())) {
                                        userList.add((String) document.get("username"));
                                    }
                                }
                                String[] userArray= new String[userList.size()];
                                userList.toArray(userArray);
                                createListView(userArray);
                            }
                        });
                    }
                    else {
                        Query searchUsers = db.collection("users").whereEqualTo("username", searchUsername);
                        Task<QuerySnapshot> task = searchUsers.get();
                        task.addOnCompleteListener(new OnCompleteListener<QuerySnapshot>() {
                            @Override
                            public void onComplete(@NonNull Task<QuerySnapshot> task) {
                                List<DocumentSnapshot> documentsList = task.getResult().getDocuments();
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
                            }
                        });
                    }

                }
                return false;
            }
        });
    }
    
    public void createListView(String[] userArray) {
        ArrayAdapter<String> arrayAdapter = new ArrayAdapter<String>(this, R.layout.activity_list_view, R.id.textView, userArray);
        usernameList.setAdapter(arrayAdapter);
    }
}