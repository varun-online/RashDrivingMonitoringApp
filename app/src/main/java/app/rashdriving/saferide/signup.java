package app.rashdriving.saferide;

import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;

import android.content.Intent;
import android.os.Bundle;
import android.text.TextUtils;
import android.text.method.HideReturnsTransformationMethod;
import android.text.method.PasswordTransformationMethod;
import android.util.Patterns;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageButton;
import android.widget.TextView;
import android.widget.Toast;
import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;

import java.util.HashMap;
import java.util.Map;

public class signup extends AppCompatActivity {

    private FirebaseAuth mAuth;
    private DatabaseReference usersRef, vehiclesRef;
    private boolean isPasswordVisible = false;
    private boolean isConfirmPasswordVisible = false;
    private EditText usernameEditText, emailEditText, passwordEditText, confirmPasswordEditText, vehicleNoEditText, phoneno;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_signup);

        mAuth = FirebaseAuth.getInstance();
        usersRef = FirebaseDatabase.getInstance().getReference("users");
        vehiclesRef = FirebaseDatabase.getInstance().getReference("vehicles");

        usernameEditText = findViewById(R.id.signName);
        emailEditText = findViewById(R.id.signEmail);
        passwordEditText = findViewById(R.id.signPassword);
        confirmPasswordEditText = findViewById(R.id.signConfirmPassword);
        vehicleNoEditText = findViewById(R.id.signVehicleNumber);
        phoneno = findViewById(R.id.editTextPhone);
        ImageButton redirect = findViewById(R.id.rdBack);
        Button signupButton = findViewById(R.id.btnSignUp);
        TextView redirectLogin = findViewById(R.id.rdLogin);
        ImageButton passwordToggle = findViewById(R.id.passwordToggle);
        ImageButton passwordToggle1 = findViewById(R.id.passwordToggle1);

        passwordToggle.setVisibility(ImageButton.GONE);
        passwordEditText.setOnFocusChangeListener((v, hasFocus) -> {
            if (hasFocus) passwordToggle.setVisibility(ImageButton.VISIBLE);
            else if (TextUtils.isEmpty(passwordEditText.getText().toString())) passwordToggle.setVisibility(ImageButton.GONE);
        });

        passwordToggle1.setVisibility(ImageButton.GONE);
        confirmPasswordEditText.setOnFocusChangeListener((v, hasFocus) -> {
            if (hasFocus) passwordToggle1.setVisibility(ImageButton.VISIBLE);
            else if (TextUtils.isEmpty(confirmPasswordEditText.getText().toString())) passwordToggle1.setVisibility(ImageButton.GONE);
        });

        passwordToggle.setOnClickListener(view -> togglePasswordVisibility(passwordEditText, passwordToggle, true));
        passwordToggle1.setOnClickListener(view -> togglePasswordVisibility(confirmPasswordEditText, passwordToggle1, false));

        signupButton.setOnClickListener(v -> {
            String username = usernameEditText.getText().toString().trim();
            String email = emailEditText.getText().toString().trim();
            String password = passwordEditText.getText().toString().trim();
            String confirmPassword = confirmPasswordEditText.getText().toString().trim();
            String vehicleNo = vehicleNoEditText.getText().toString().trim().toUpperCase();
            String phoneNo = phoneno.getText().toString().trim();

            if (validateInputs(username, email, password, confirmPassword, vehicleNo, phoneNo)) {
                checkVehicleNumber(username, email, password, vehicleNo, phoneNo);
            }
        });

        redirect.setOnClickListener(v -> navigateToLogin());
        redirectLogin.setOnClickListener(v -> navigateToLogin());
    }

    private void togglePasswordVisibility(EditText editText, ImageButton toggleButton, boolean isPassword) {
        boolean isVisible = isPassword ? isPasswordVisible : isConfirmPasswordVisible;
        if (isVisible) {
            editText.setTransformationMethod(PasswordTransformationMethod.getInstance());
            toggleButton.setImageResource(R.drawable.visibility_off_signup_ps);
        } else {
            editText.setTransformationMethod(HideReturnsTransformationMethod.getInstance());
            toggleButton.setImageResource(R.drawable.visibility_on_sign_ps);
        }
        if (isPassword) isPasswordVisible = !isPasswordVisible;
        else isConfirmPasswordVisible = !isConfirmPasswordVisible;
        editText.setSelection(editText.getText().length());
    }

    private void navigateToLogin() {
        startActivity(new Intent(signup.this, login.class));
        finish();
    }

    private boolean validateInputs(String username, String email, String password, String confirmPassword, String vehicleNo, String phoneNo) {
        if (TextUtils.isEmpty(username) || !username.matches("^[A-Za-z][A-Za-z0-9]*$")) {
            usernameEditText.setError("Invalid username format.");
            return false;
        }
        if (TextUtils.isEmpty(email) || !Patterns.EMAIL_ADDRESS.matcher(email).matches()) {
            emailEditText.setError("Enter a valid email.");
            return false;
        }
        if (TextUtils.isEmpty(phoneNo) || !phoneNo.matches("^[6-9]\\d{9}$")) {
            phoneno.setError("Enter a valid 10-digit mobile number.");
            return false;
        }
        if (TextUtils.isEmpty(vehicleNo)) {
            vehicleNoEditText.setError("Enter your vehicle number.");
            return false;
        }
        if (TextUtils.isEmpty(password) || password.length() < 6) {
            passwordEditText.setError("Password must be at least 6 characters.");
            return false;
        }
        if (!password.equals(confirmPassword)) {
            confirmPasswordEditText.setError("Passwords do not match.");
            return false;
        }
        return true;
    }

    private void checkVehicleNumber(String username, String email, String password, String vehicleNo, String phoneNo) {
        vehiclesRef.child(vehicleNo).addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot vehicleSnapshot) {
                if (!vehicleSnapshot.exists()) {
                    vehicleNoEditText.setError("Vehicle number is not registered.");
                    Toast.makeText(signup.this, "Vehicle number not found.", Toast.LENGTH_LONG).show();
                    return;
                }

                usersRef.orderByChild("vehicleNo").equalTo(vehicleNo).addListenerForSingleValueEvent(new ValueEventListener() {
                    @Override
                    public void onDataChange(@NonNull DataSnapshot userSnapshot) {
                        if (userSnapshot.exists()) {
                            vehicleNoEditText.setError("This vehicle is already linked to another account.");
                            Toast.makeText(signup.this, "Vehicle already registered.", Toast.LENGTH_LONG).show();
                        } else {
                            registerUser(username, email, password, vehicleNo, phoneNo);
                        }
                    }

                    @Override
                    public void onCancelled(@NonNull DatabaseError error) {
                        Toast.makeText(signup.this, "Database error: " + error.getMessage(), Toast.LENGTH_LONG).show();
                    }
                });
            }

            @Override
            public void onCancelled(@NonNull DatabaseError error) {
                Toast.makeText(signup.this, "Database error: " + error.getMessage(), Toast.LENGTH_LONG).show();
            }
        });
    }

    private void registerUser(String username, String email, String password, String vehicleNo, String phoneNo) {
        mAuth.createUserWithEmailAndPassword(email, password).addOnCompleteListener(this, task -> {
            if (task.isSuccessful()) {
                FirebaseUser user = mAuth.getCurrentUser();
                if (user != null) {
                    saveUserToDatabase(user.getUid(), username, email, vehicleNo, phoneNo);
                    sendVerificationEmail(user);
                }
            } else {
                Toast.makeText(signup.this, "Sign Up failed: " + task.getException().getMessage(), Toast.LENGTH_LONG).show();
            }
        });
    }

    private void saveUserToDatabase(String userId, String username, String email, String vehicleNo, String phoneNo) {
        Map<String, Object> userData = new HashMap<>();
        userData.put("username", username);
        userData.put("email", email);
        userData.put("vehicleNo", vehicleNo);
        userData.put("phoneNumber", phoneNo);

        usersRef.child(userId).setValue(userData)
                .addOnSuccessListener(aVoid -> {
                    Toast.makeText(signup.this, "Successfully registered! Please verify your email.", Toast.LENGTH_SHORT).show();
                })
                .addOnFailureListener(e -> Toast.makeText(signup.this, "Registration failed.", Toast.LENGTH_LONG).show());

    }
    private void sendVerificationEmail(FirebaseUser user) {
        user.sendEmailVerification()
                .addOnCompleteListener(task -> {
                    if (task.isSuccessful()) {
                        Toast.makeText(signup.this, "Verification email sent. Check your inbox.", Toast.LENGTH_SHORT).show();
                        startActivity(new Intent(signup.this, login.class));
                        finish();
                    } else {
                        Toast.makeText(signup.this, "Failed to send verification email.", Toast.LENGTH_SHORT).show();
                    }
                });
    }
}
