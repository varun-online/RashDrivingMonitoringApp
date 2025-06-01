package app.rashdriving.saferide;

import androidx.appcompat.app.AppCompatActivity;

import android.content.Intent;
import android.os.Bundle;
import android.text.TextUtils;
import android.text.method.HideReturnsTransformationMethod;
import android.text.method.PasswordTransformationMethod;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageButton;
import android.widget.TextView;
import android.widget.Toast;

import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseAuthInvalidUserException;

import java.util.Objects;



public class login extends AppCompatActivity {

    private EditText etEmail, etPassword;
     TextView tvSignUp,forgetpsd;
    private FirebaseAuth mAuth;
    boolean isLoginPasswordVisible = false;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_login);

        // Initialize Firebase Auth
        mAuth = FirebaseAuth.getInstance();

        // Initialize UI elements
        etEmail = findViewById(R.id.lgName);
        etPassword = findViewById(R.id.lgPassword);
        Button btnLogin = findViewById(R.id.btnLogin);
        tvSignUp = findViewById(R.id.lgsignup);
        forgetpsd = findViewById(R.id.tvForgotPassword);
        ImageButton passwordtoggle = findViewById(R.id.passwordToggle3);
        // Initially hide the password toggle button
        passwordtoggle.setVisibility(ImageButton.GONE);

        // Show password toggle button when the password field is focused
        etPassword.setOnFocusChangeListener((v, hasFocus) -> {
            if (hasFocus) {
                passwordtoggle.setVisibility(ImageButton.VISIBLE);
            } else if (TextUtils.isEmpty(etPassword.getText().toString())) {
                passwordtoggle.setVisibility(ImageButton.GONE);
            }
        });


// Toggle password visibility for password field
        passwordtoggle.setOnClickListener(view -> {
            if (isLoginPasswordVisible) {
                etPassword.setTransformationMethod(PasswordTransformationMethod.getInstance());
                passwordtoggle.setImageResource(R.drawable.visibility_off_signup_ps);
            } else {
                etPassword.setTransformationMethod(HideReturnsTransformationMethod.getInstance());
                passwordtoggle.setImageResource(R.drawable.visibility_on_sign_ps);
            }
            isLoginPasswordVisible = !isLoginPasswordVisible;
            etPassword.setSelection(etPassword.getText().length()); // Move cursor to end
        });

        // Login button logic
        btnLogin.setOnClickListener(v -> {
            String email = etEmail.getText().toString().trim();
            String password = etPassword.getText().toString().trim();

            // Validate inputs
            if (TextUtils.isEmpty(email)) {
                Toast.makeText(login.this, "Please enter email", Toast.LENGTH_SHORT).show();
                return;
            }
            if (TextUtils.isEmpty(password)) {
                Toast.makeText(login.this, "Please enter password", Toast.LENGTH_SHORT).show();
                return;
            }

            // Log in with Firebase
            mAuth.signInWithEmailAndPassword(email, password)
                    .addOnCompleteListener(loginTask -> {
                        if (loginTask.isSuccessful()) {
                            // Get current user
                            if (mAuth.getCurrentUser() != null) {
                                if (mAuth.getCurrentUser().isEmailVerified()) {
                                    // Email is verified, allow login
                                    Toast.makeText(login.this, "Login Successful!", Toast.LENGTH_SHORT).show();
                                    startActivity(new Intent(login.this, dashboard.class));
                                    finish();
                                } else {
                                    // Email not verified, prevent login
                                    Toast.makeText(login.this, "Please verify your email before login.", Toast.LENGTH_LONG).show();
                                    mAuth.signOut(); // Sign out to prevent access
                                }
                            }
                        } else {
                            // Handle failed login
                            if (loginTask.getException() instanceof FirebaseAuthInvalidUserException) {
                                Toast.makeText(login.this, "You don't have an account. Please sign up.", Toast.LENGTH_SHORT).show();
                            } else {
                                Toast.makeText(login.this, "Authentication Failed: " +
                                        Objects.requireNonNull(loginTask.getException()).getMessage(), Toast.LENGTH_LONG).show();
                            }
                        }
                    });
        });

        // Redirect to Sign Up Page
        tvSignUp.setOnClickListener(v -> {
            Intent intent = new Intent(login.this, signup.class);
            startActivity(intent);
            finish();
        });
        forgetpsd.setOnClickListener(v -> {
            Intent intent = new Intent(login.this, forgetpassword.class);
            startActivity(intent);
            finish();
        });
    }
}
