package com.example.nikoleta.coinz;

import android.annotation.SuppressLint;
import android.content.Context;
import android.content.Intent;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.widget.Button;
import android.widget.Toast;
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
import java.util.Objects;
import static com.example.nikoleta.coinz.SelectUserActivity.selectedUser;

public class StealActivity extends AppCompatActivity {
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_steal);

        Button use_steal = findViewById(R.id.use_steal);
        use_steal.setOnClickListener(view -> {
            SelectUserActivity.stealCoins = true;
            SelectUserActivity.sendCoins = false;
            startActivity(new Intent(getApplicationContext(), SelectUserActivity.class));
            finish();
        });
    }
    static void stealCoin(Context context) {
        FirebaseFirestore db = FirebaseFirestore.getInstance();
        FirebaseAuth firebaseAuth = FirebaseAuth.getInstance();
        FirebaseUser user = firebaseAuth.getCurrentUser();

        Query searchUsers = db.collection("users").whereEqualTo("username", selectedUser);
        Task<QuerySnapshot> task = searchUsers.get();
        task.addOnCompleteListener(task1 -> {
            List<DocumentSnapshot> documentsList = Objects.requireNonNull(task1.getResult()).getDocuments();
            if(documentsList.isEmpty()) {
                Toast.makeText(context, "No such user.", Toast.LENGTH_SHORT).show();
            }
            else {
                DocumentSnapshot userDocument = documentsList.get(0);
                DocumentReference docRef = db.collection("users").document(userDocument.getId());
                docRef.get().addOnCompleteListener(taskSteal -> {
                    if (Objects.requireNonNull(taskSteal.getResult()).contains("piggybankProtected") && (boolean) Objects.requireNonNull(Objects.requireNonNull(taskSteal).getResult()).get("piggybankProtected")) {
                            Toast.makeText(context, SelectUserActivity.selectedUser + " is protected from stealling. Please select another user.", Toast.LENGTH_SHORT).show();
                    }
                    else {
                        assert user != null;
                        DocumentReference docRef1 = db.collection("users").document(user.getUid());
                        docRef1.get().addOnCompleteListener(taskCurrentUser -> {
                            boolean canSteal = true;
                            if (Objects.requireNonNull(taskCurrentUser.getResult()).contains("cantStealFrom")) {
                                String[] users = Objects.requireNonNull(taskCurrentUser.getResult().get("cantStealFrom")).toString().replaceAll("\\[", "").replaceAll("]", "").split(", ");
                                List<String> usersList = new ArrayList<>(Arrays.asList(users));
                                if (usersList.contains(SelectUserActivity.selectedUser)) {
                                    canSteal = false;
                                    String username = Objects.requireNonNull(taskCurrentUser.getResult().get("username")).toString();
                                    Toast.makeText(context, username + " gave you a present today. Don't try to bite the hand that feeds you!", Toast.LENGTH_LONG).show();
                                }
                            }
                            if (canSteal) {
                                if (taskSteal.getResult().contains("piggybank")) {
                                    // Steal the coin with highest value from the selected user
                                    String[] coins = Objects.requireNonNull(taskSteal.getResult().get("piggybank")).toString().replaceAll("\\[", "").replaceAll("]", "").split(", ");
                                    if (!coins[0].equals("")) {
                                        double maxValue = 0.0;
                                        double maxCoinValue = 0.0;
                                        String maxCurrency = "";
                                        String maxId = "";
                                        for (String c : coins) {
                                            if (!c.equals("")) {
                                                String[] coinProperties = c.split(" ");
                                                double rate = 0;
                                                switch (coinProperties[1]) {
                                                    case "QUID":
                                                        rate = MapActivity.rateQUID;
                                                        break;
                                                    case "DOLR":
                                                        rate = MapActivity.rateDOLR;
                                                        break;
                                                    case "PENY":
                                                        rate = MapActivity.ratePENY;
                                                        break;
                                                    case "SHIL":
                                                        rate = MapActivity.rateSHIL;
                                                        break;
                                                }
                                                double value = Double.parseDouble(coinProperties[0]) * rate;
                                                if (value > maxValue) {
                                                    maxValue = value;
                                                    maxCoinValue = Double.parseDouble(coinProperties[0]);
                                                    maxCurrency = coinProperties[1];
                                                    maxId = coinProperties[2];
                                                }
                                            }
                                        }
                                        List<String> piggybankCoins = new ArrayList<>(Arrays.asList(coins));
                                        String maxCoin = String.valueOf(maxCoinValue) + " " + maxCurrency + " " + maxId;
                                        piggybankCoins.remove(maxCoin);
                                        db.collection("users").document(userDocument.getId()).update("piggybank", piggybankCoins);

                                        // Notify the robbed user a coin has been stolen
                                        List<String> notifications = new ArrayList<>();
                                        @SuppressLint("DefaultLocale") String notification = String.format("Someone stole a coin worth  %.2f GOLD from your Piggybank!", maxValue);
                                        notifications.add(notification);

                                        if (taskSteal.getResult().contains("notifications")) {
                                            String[] prevNotifications = Objects.requireNonNull(taskSteal.getResult().get("notifications")).toString().replaceAll("\\[", "").replaceAll("]", "").split(", ");
                                            for (String n : prevNotifications) {
                                                if (!n.equals("")) {
                                                    notifications.add(n);
                                                }
                                            }
                                        }
                                        db.collection("users").document(userDocument.getId()).update("notifications", notifications);
                                        db.collection("users").document(userDocument.getId()).update("newNotifications", true);

                                        // Add the stolen coin in the thief's piggybank.
                                        List<String> piggybankCoinsList = new ArrayList<>();
                                        piggybankCoinsList.add(maxCoin);
                                        if (taskCurrentUser.getResult().contains("piggybank")) {
                                            // Remove the square brackets from the String of previous coins stored in the database and split it into coins
                                            String[] prevCoins = Objects.requireNonNull(taskCurrentUser.getResult().get("piggybank")).toString().replaceAll("\\[", "").replaceAll("]", "").split(", ");
                                            for (String c : prevCoins) {
                                                if (!c.equals("")) {
                                                    piggybankCoinsList.add(c);
                                                }
                                            }
                                        }
                                        db.collection("users").document(user.getUid()).update("piggybank", piggybankCoinsList);
                                        db.collection("users").document(user.getUid()).update("stealUsed", true);
                                        stealComplete(context);
                                    }
                                    else {
                                        stealNotComplete(context);
                                    }
                                }
                                else {
                                    stealNotComplete(context);
                                }
                            }
                        });

                    }
                });
            }
        });
    }

    public static void stealComplete(Context context) {
        Toast.makeText(context, "You have stolen " + SelectUserActivity.selectedUser + "'s coin.", Toast.LENGTH_SHORT).show();
    }

    public static void stealNotComplete(Context context) {
        Toast.makeText(context, SelectUserActivity.selectedUser + " doesn't have any coins in their piggybank. Please select another user.", Toast.LENGTH_LONG).show();
    }
}
