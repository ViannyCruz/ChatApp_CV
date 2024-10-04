package com.example.chatapp_cv;

import android.annotation.SuppressLint;
import android.content.Intent;
import android.graphics.drawable.Drawable;
import android.os.Bundle;
import android.text.TextUtils;
import android.view.View;
import android.widget.EditText;
import android.widget.Toast;

import androidx.activity.EdgeToEdge;
import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;
import androidx.core.content.ContextCompat;
import androidx.core.graphics.Insets;
import androidx.core.view.ViewCompat;
import androidx.core.view.WindowInsetsCompat;

import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;

import java.util.Objects;

public class Login extends AppCompatActivity {
    private EditText emailEditText, passwordEditText;
    private FirebaseAuth Auth;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        EdgeToEdge.enable(this);
        setContentView(R.layout.activity_login);

        Toolbar toolbar = findViewById(R.id.toolbarLogin);
        setSupportActionBar(toolbar);

        // Habilitar la flecha de ir hacia atrás
        if (getSupportActionBar() != null) {
            getSupportActionBar().setDisplayHomeAsUpEnabled(true);
            getSupportActionBar().setDisplayShowHomeEnabled(true);
        }

        // Quitar el titulo del Toolbar
        Objects.requireNonNull(getSupportActionBar()).setTitle("");

        // Cambiar el color de la flecha a blanco usando
        Drawable upArrow = ContextCompat.getDrawable(this, androidx.appcompat.R.drawable.abc_ic_ab_back_material);
        if (upArrow != null) {
            upArrow.setTint(ContextCompat.getColor(this, android.R.color.white));
            getSupportActionBar().setHomeAsUpIndicator(upArrow);
        }

        Auth = FirebaseAuth.getInstance();

        emailEditText = findViewById(R.id.emailLogin);
        passwordEditText = findViewById(R.id.passwordLogin);

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
                        FirebaseUser user = Auth.getCurrentUser();
                        Toast.makeText(Login.this, "Bienvenido " + user.getEmail(), Toast.LENGTH_SHORT).show();

                        // Guardar el estado de la sesion y el ID de usuario
                        getSharedPreferences("LoginPrefs", MODE_PRIVATE)
                                .edit()
                                .putBoolean("isLoggedIn", true)
                                .putString("userId", user.getUid())  // Guardar el ID de usuario
                                .apply();

                        // Redirigir al usuario a la pantalla principal
                        startActivity(new Intent(Login.this, CentralActivity.class));
                        finish();  // Cierra la actividad de login para que no se pueda regresar a ella
                    } else {
                        Toast.makeText(Login.this, "Error: " + task.getException().getMessage(), Toast.LENGTH_SHORT).show();
                    }
                });
    }
}