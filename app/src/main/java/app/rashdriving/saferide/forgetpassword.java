package app.rashdriving.saferide;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import android.content.Intent;
import android.os.Bundle;
import android.text.TextUtils;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageButton;
import android.widget.TextView;
import android.widget.Toast;

import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseAuthInvalidUserException;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;

public class forgetpassword extends AppCompatActivity {

    private FirebaseAuth mAuth;
    private DatabaseReference usersRef;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_forgetpassword);

        // Initialize Firebase
        mAuth = FirebaseAuth.getInstance();
        usersRef = FirebaseDatabase.getInstance().getReference("users"); // Adjust if your database structure is different

        // Initialize UI elements
        EditText etEmail = findViewById(R.id.fgname);
        Button forgetBtn = findViewById(R.id.reset_ps);
        TextView tvSignup = findViewById(R.id.rdsignup);
        ImageButton tvLogin = findViewById(R.id.rdLogin);

        // Password Reset Button Logic
        forgetBtn.setOnClickListener(v -> {
            String email = etEmail.getText().toString().trim();

            // Validate input
            if (TextUtils.isEmpty(email)) {
                Toast.makeText(forgetpassword.this, "Please enter your email", Toast.LENGTH_SHORT).show();
                return;
            }

            // Check if the email exists in the database
            checkEmailExists(email);
        });

        // Redirect to Sign Up Page
        tvSignup.setOnClickListener(v -> {
            startActivity(new Intent(forgetpassword.this, signup.class));
            finish();
        });

        // Redirect to Login Page
        tvLogin.setOnClickListener(v -> {
            startActivity(new Intent(forgetpassword.this, login.class));
            finish();
        });
    }

    private void checkEmailExists(String email) {
        usersRef.orderByChild("email").equalTo(email).addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {
                if (snapshot.exists()) {
                    // Email exists, proceed with password reset
                    sendPasswordResetEmail(email);
                } else {
                    // Email does not exist, show error
                    Toast.makeText(forgetpassword.this, "No account found with this email", Toast.LENGTH_SHORT).show();
                }
            }

            @Override
            public void onCancelled(@NonNull DatabaseError error) {
                Toast.makeText(forgetpassword.this, "Database error: " + error.getMessage(), Toast.LENGTH_SHORT).show();
            }
        });
    }

    private void sendPasswordResetEmail(String email) {
        mAuth.sendPasswordResetEmail(email)
                .addOnCompleteListener(task -> {
                    if (task.isSuccessful()) {
                        Toast.makeText(forgetpassword.this, "Password reset link sent to your email", Toast.LENGTH_SHORT).show();
                        startActivity(new Intent(forgetpassword.this, login.class));
                        finish();
                    } else {
                        Exception e = task.getException();
                        if (e instanceof FirebaseAuthInvalidUserException) {
                            Toast.makeText(forgetpassword.this, "No account found with this email", Toast.LENGTH_SHORT).show();
                        } else {
                            Toast.makeText(forgetpassword.this, "Error: " + e.getMessage(), Toast.LENGTH_LONG).show();
                        }
                    }
                });
    }
}
