package com.example.nikoleta.coinz;

import android.support.annotation.NonNull;
import android.support.design.widget.NavigationView;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.view.View;
import android.widget.ArrayAdapter;
import android.widget.ListView;
import android.widget.TextView;

import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.firestore.DocumentReference;
import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.FirebaseFirestore;

import java.util.List;

public class NotificationsActivity extends AppCompatActivity {
    ListView notificationsView;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_notifications);
        FirebaseAuth firebaseAuth = FirebaseAuth.getInstance();
        FirebaseUser user = firebaseAuth.getCurrentUser();
        FirebaseFirestore db = FirebaseFirestore.getInstance();
        DocumentReference docRef = db.collection("users").document(user.getUid());
        Task<DocumentSnapshot> documentSnapshotTask = docRef.get().addOnCompleteListener(new OnCompleteListener<DocumentSnapshot>() {
            @Override
            public void onComplete(@NonNull Task<DocumentSnapshot> task) {
                notificationsView = (ListView) findViewById(R.id.notifications_listview);
                if (task.getResult().contains("notifications")) {
                    String[] notifications = task.getResult().get("notifications").toString().replaceAll("\\[", "").replaceAll("\\]", "").split(", ");
                    if (!notifications[0].equals("")) {
                        updateView(notifications);
                        db.collection("users").document(user.getUid()).update("newNotifications", false);
                    }
                    else {
                        notifications[0] = "You have no notifications.";
                        updateView(notifications);
                    }
                }
                else {
                    String[] notifications = new String[1];
                    notifications[0] = "You have no notifications.";
                    updateView(notifications);
                }

            }
        });
    }
    private void updateView(String[] notifications){
        ArrayAdapter<String> arrayAdapter = new ArrayAdapter<String>(this, R.layout.notification_list_view, R.id.notification_view, notifications);
        notificationsView.setAdapter(arrayAdapter);
    }
}
