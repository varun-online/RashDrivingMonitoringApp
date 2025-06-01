package app.rashdriving.saferide;

import android.annotation.SuppressLint;
import android.content.Intent;
import android.os.Bundle;
import androidx.appcompat.app.AppCompatActivity;

import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;


@SuppressLint("Customsplashscreen")
public class splashscreen extends AppCompatActivity {

    public FirebaseAuth mAuth;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        mAuth = FirebaseAuth.getInstance();

        // Check if the user is already logged in
        FirebaseUser currentUser = mAuth.getCurrentUser();

        if (currentUser != null) {
            // If user is logged in, redirect to Home activity
            Intent intent = new Intent(splashscreen.this, dashboard.class);
            startActivity(intent);
            finish(); // Close the MainActivity so that the user cannot navigate back
        } else {
            // If user is not logged in, redirect to Login activity
            Intent intent = new Intent(splashscreen.this, login.class);
            startActivity(intent);
            finish(); // Close the MainActivity to prevent back navigation
        }
    }
}