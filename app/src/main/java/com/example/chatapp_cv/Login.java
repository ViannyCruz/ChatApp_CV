package com.example.chatapp_cv;

import android.content.Intent;
import android.os.Bundle;
import android.text.TextUtils;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Toast;

import androidx.activity.EdgeToEdge;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.graphics.Insets;
import androidx.core.view.ViewCompat;
import androidx.core.view.WindowInsetsCompat;

import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;

public class Login extends AppCompatActivity {
    private EditText emailEditText, passwordEditText;
    private Button loginButton;
    private FirebaseAuth Auth;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        EdgeToEdge.enable(this);
        setContentView(R.layout.activity_login);

        Auth = FirebaseAuth.getInstance();

        emailEditText = findViewById(R.id.emailLogin);
        passwordEditText = findViewById(R.id.passwordLogin);
        loginButton = findViewById(R.id.buttonContinue);


        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.main), (v, insets) -> {
            Insets systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars());
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom);
            return insets;
        });
    }


    public void loginUser(View v) {
        String email = emailEditText.getText().toString().trim();
        String password = passwordEditText.getText().toString().trim();

        if (TextUtils.isEmpty(email)) {
            emailEditText.setError("El correo es requerido.");
            return;
        }

        if (TextUtils.isEmpty(password)) {
            passwordEditText.setError("La contraseña es requerida.");
            return;
        }

        // Iniciar sesion con Firebase Authentication
        Auth.signInWithEmailAndPassword(email, password)
                .addOnCompleteListener(this, task -> {
                    if (task.isSuccessful()) {
                        // El inicio de sesion es exitoso
                        FirebaseUser user = Auth.getCurrentUser();
                        Toast.makeText(Login.this, "Bienvenido " + user.getEmail(), Toast.LENGTH_SHORT).show();

                        // Redirigir al usuario a la pantalla principal
                        startActivity(new Intent(Login.this, MainActivity.class));
                        finish();  // Cierra la actividad de login para que no se pueda regresar a ella
                    } else {
                        // Si el inicio de sesion falla, mostrar un mensaje
                        Toast.makeText(Login.this, "Error: " + task.getException().getMessage(), Toast.LENGTH_SHORT).show();
                    }
                });
    }
}
