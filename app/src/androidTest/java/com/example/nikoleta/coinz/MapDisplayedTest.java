package com.example.nikoleta.coinz;

import android.Manifest;
import android.support.test.rule.ActivityTestRule;
import android.support.test.rule.GrantPermissionRule;
import android.support.test.runner.AndroidJUnit4;
import android.support.test.filters.LargeTest;

import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.firestore.FirebaseFirestore;

import org.junit.After;
import org.junit.Before;
import org.junit.Rule;
import org.junit.Test;
import org.junit.runner.RunWith;

import java.util.ArrayList;

import static android.support.test.espresso.Espresso.onView;
import static android.support.test.espresso.action.ViewActions.click;
import static android.support.test.espresso.action.ViewActions.closeSoftKeyboard;
import static android.support.test.espresso.action.ViewActions.pressImeActionButton;
import static android.support.test.espresso.action.ViewActions.replaceText;
import static android.support.test.espresso.assertion.ViewAssertions.matches;
import static android.support.test.espresso.matcher.ViewMatchers.isDisplayed;
import static android.support.test.espresso.matcher.ViewMatchers.withId;

@LargeTest
@RunWith(AndroidJUnit4.class)
public class MapDisplayedTest {

    @Rule
    public ActivityTestRule<LoginActivity> mActivityTestRule = new ActivityTestRule<>(LoginActivity.class);
    @Rule
    public GrantPermissionRule permissionsRule = GrantPermissionRule.grant(android.Manifest.permission.ACCESS_FINE_LOCATION,
            Manifest.permission.INTERNET, Manifest.permission.ACCESS_NETWORK_STATE);

    @Before
    public void createUser() {

        FirebaseAuth firebaseAuth = FirebaseAuth.getInstance();

        firebaseAuth.createUserWithEmailAndPassword("testingtests@gmail.com", "123456")
                .addOnCompleteListener(task -> {
                    if (task.isSuccessful()) {
                        ArrayList<String> wallet = new ArrayList<>();
                        wallet.add("2.6 DOLR 7f28-c3d8-8ac5-ef51-8c05-c071");

                        FirebaseFirestore db = FirebaseFirestore.getInstance();
                        FirebaseUser user = firebaseAuth.getCurrentUser();

                        if (user != null) {
                            db.collection("users").document(user.getUid()).update("username", "Testing Tests");
                            db.collection("users").document(user.getUid()).set(new User("testingtests@gmail.com", "123456", 0.0));
                            db.collection("users").document(user.getUid()).update("provider", "");
                            db.collection("users").document(user.getUid()).update("coinsLeft", "25");
                            db.collection("users").document(user.getUid()).update("wallet", wallet);
                            db.collection("users").document(user.getUid()).update("coinsLeft", 25);
                            db.collection("users").document(user.getUid()).update("magnetUnlocked", false);
                            db.collection("users").document(user.getUid()).update("stealUnlocked", false);
                            db.collection("users").document(user.getUid()).update("shieldUnlocked", false);
                            db.collection("users").document(user.getUid()).update("magnetMode", false);
                            db.collection("users").document(user.getUid()).update("stealUsed", false);
                            db.collection("users").document(user.getUid()).update("piggybankProtected", false);
                            db.collection("users").document(user.getUid()).update("cantStealFrom", new ArrayList<String>());
                            db.collection("users").document(user.getUid()).update("piggybank", new ArrayList<String>());
                            db.collection("users").document(user.getUid()).update("treasureUnlocked", false);
                            db.collection("users").document(user.getUid()).update("treasureFound", false);
                        }

                    }
                });

    }

    @Test
    public void mapDisplayedTest() {
        // Added a sleep statement to match the app's execution delay.
        // The recommended way to handle such scenarios is to use Espresso idling resources:
        // https://google.github.io/android-testing-support-library/docs/espresso/idling-resource/index.html
        try {
            Thread.sleep(3000);
        } catch (InterruptedException e) {
            e.printStackTrace();
        }

        onView(withId(R.id.email)).perform(replaceText("testingtests@gmail.com"), closeSoftKeyboard());

        // Added a sleep statement to match the app's execution delay.
        // The recommended way to handle such scenarios is to use Espresso idling resources:
        // https://google.github.io/android-testing-support-library/docs/espresso/idling-resource/index.html
        try {
            Thread.sleep(3000);
        } catch (InterruptedException e) {
            e.printStackTrace();
        }

        onView(withId(R.id.password)).perform(replaceText("123456"), closeSoftKeyboard());

        onView(withId(R.id.password)).perform(pressImeActionButton());

        onView(withId(R.id.logInButton)).perform(click());

        // Added a sleep statement to match the app's execution delay.
        // The recommended way to handle such scenarios is to use Espresso idling resources:
        // https://google.github.io/android-testing-support-library/docs/espresso/idling-resource/index.html
        try {
            Thread.sleep(4000);
        } catch (InterruptedException e) {
            e.printStackTrace();
        }

        onView(withId(R.id.play)).perform(click());

        // Added a sleep statement to match the app's execution delay.
        // The recommended way to handle such scenarios is to use Espresso idling resources:
        // https://google.github.io/android-testing-support-library/docs/espresso/idling-resource/index.html
        try {
            Thread.sleep(8000);
        } catch (InterruptedException e) {
            e.printStackTrace();
        }

        onView(withId(R.id.include2)).check(matches(isDisplayed()));

    }

    @After
    public void deleteAccount(){
        FirebaseFirestore db = FirebaseFirestore.getInstance();
        FirebaseAuth firebaseAuth = FirebaseAuth.getInstance();
        FirebaseUser user = firebaseAuth.getCurrentUser();
        firebaseAuth.signOut();
        if (user != null) {
            db.collection("users").document(user.getUid()).delete();
            user.delete();
        }
    }

}
