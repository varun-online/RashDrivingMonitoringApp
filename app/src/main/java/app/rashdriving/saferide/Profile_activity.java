package app.rashdriving.saferide;

import android.content.Intent;
import android.os.Bundle;
import android.widget.ImageButton;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.cardview.widget.CardView;

import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;

public class Profile_activity extends AppCompatActivity {

    private DatabaseReference usersRef;
    private CardView logout_option,speed_option;

    private TextView userNameTextView, emailTextView, phoneNumberTextView, vehicleNumberTextView, userlogout;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_profile);  // Updated to use normal layout

        // Initialize Firebase reference
        usersRef = FirebaseDatabase.getInstance().getReference("users");

        // Initialize TextViews
        ImageButton backarrow = findViewById(R.id.backtohome);
        userNameTextView = findViewById(R.id.user_name);
        emailTextView = findViewById(R.id.email);
        phoneNumberTextView = findViewById(R.id.mobile);
        vehicleNumberTextView = findViewById(R.id.vehicle);
        userlogout = findViewById(R.id.user_log);
        ImageButton sideSpeedHistory = findViewById(R.id.speed_history_nav);
        logout_option = findViewById(R.id.logout_text_card);
        speed_option = findViewById(R.id.speed_history_card);

        // Logout button logic
        logout_option.setOnClickListener(v -> {
            FirebaseAuth.getInstance().signOut();
            Toast.makeText(this, "Signing Out...", Toast.LENGTH_SHORT).show();
            startActivity(new Intent(Profile_activity.this, login.class));
            finish();
        });
        userlogout.setOnClickListener(v -> {
            FirebaseAuth.getInstance().signOut();
            Toast.makeText(this, "Signing Out...", Toast.LENGTH_SHORT).show();
            startActivity(new Intent(Profile_activity.this, login.class));
            finish();
        });

        sideSpeedHistory.setOnClickListener(v -> {
            startActivity(new Intent(Profile_activity.this, Speedhistory.class));
            finish();
        });

        speed_option.setOnClickListener(v -> {
            startActivity(new Intent(Profile_activity.this, Speedhistory.class));
            finish();
        });

        backarrow.setOnClickListener( v -> {
            startActivity(new Intent(Profile_activity.this, dashboard.class));
            finish();
        });

        // Load user details
        loadUserDetails();
    }

    // 🚗 Load User Details from Firebase
    private void loadUserDetails() {
        FirebaseUser currentUser = FirebaseAuth.getInstance().getCurrentUser();

        if (currentUser != null) {
            String userId = currentUser.getUid();

            usersRef.child(userId).addListenerForSingleValueEvent(new ValueEventListener() {
                @Override
                public void onDataChange(@NonNull DataSnapshot snapshot) {
                    if (snapshot.exists()) {
                        String username = snapshot.child("username").getValue(String.class);
                        String email = snapshot.child("email").getValue(String.class);
                        String phoneNumber = snapshot.child("phoneNumber").getValue(String.class);
                        String vehicleNo = snapshot.child("vehicleNo").getValue(String.class);

                        // Display user details
                        userNameTextView.setText(username);
                        emailTextView.setText(email);
                        phoneNumberTextView.setText( phoneNumber);
                        vehicleNumberTextView.setText(vehicleNo);
                    } else {
                        Toast.makeText(Profile_activity.this, "User details not found.", Toast.LENGTH_SHORT).show();
                    }
                }

                @Override
                public void onCancelled(@NonNull DatabaseError error) {
                    Toast.makeText(Profile_activity.this, "Failed to load user details: " + error.getMessage(), Toast.LENGTH_SHORT).show();
                }
            });
        } else {
            Toast.makeText(this, "User not authenticated.", Toast.LENGTH_SHORT).show();
        }
    }
}
