package com.example.chatapp_cv;

import android.annotation.SuppressLint;
import android.graphics.drawable.Drawable;
import android.os.Bundle;
import android.text.TextUtils;
import android.view.MenuItem;

import androidx.activity.EdgeToEdge;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;

import android.widget.Button;
import android.widget.EditText;
import android.widget.Toast;

import androidx.core.content.ContextCompat;
import androidx.core.graphics.Insets;
import androidx.core.view.ViewCompat;
import androidx.core.view.WindowInsetsCompat;

import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;

import android.view.View;

import java.util.HashMap;
import java.util.Map;
import java.util.Objects;

public class Register extends AppCompatActivity {

    private EditText emailEditText, passwordEditText, usernameEditText;
    private Button registerButton;
    private FirebaseAuth Auth;
    private FirebaseDatabase database;
    private DatabaseReference usersRef;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        EdgeToEdge.enable(this);
        setContentView(R.layout.activity_register);

        Toolbar toolbar = findViewById(R.id.toolbarsettings);
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
        database = FirebaseDatabase.getInstance();
        usersRef = database.getReference("users");

        emailEditText = findViewById(R.id.editTextText);
        passwordEditText = findViewById(R.id.editTextText2);
        usernameEditText = findViewById(R.id.usernameEditTextText);
        registerButton = findViewById(R.id.button3);

        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.main), (v, insets) -> {
            Insets systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars());
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom);
            return insets;
        });






    }


    public void registerUser(View v) {
        String email = emailEditText.getText().toString().trim();
        String password = passwordEditText.getText().toString().trim();
        String username = usernameEditText.getText().toString().trim();

        if (TextUtils.isEmpty(email)) {
            emailEditText.setError("El correo es requerido.");
            return;
        }

        if (TextUtils.isEmpty(password)) {
            passwordEditText.setError("La contraseña es requerida.");
            return;
        }

        if (TextUtils.isEmpty(username)) {
            usernameEditText.setError("El nombre de usuario es requerido.");
            return;
        }

        if (password.length() < 8) {
            passwordEditText.setError("La contraseña debe tener al menos 8 caracteres.");
            return;
        }

        // Crear usuario con Firebase Authentication
        Auth.createUserWithEmailAndPassword(email, password)
                .addOnCompleteListener(this, task -> {
                    if (task.isSuccessful()) {
                        // Registro exitoso, se obtiene el usuario registrado
                        FirebaseUser user = Auth.getCurrentUser();
                        assert user != null;
                        String userId = user.getUid();

                        Map<String, Object> userData = new HashMap<>();
                        userData.put("email", email);
                        userData.put("username", username);
                        usersRef.child(userId).setValue(userData);

                        Toast.makeText(Register.this, "Usuario registrado: " + user.getEmail(), Toast.LENGTH_SHORT).show();
                        // Redirigir o realizar alguna acción, como cerrar la actividad de registro
                    } else {
                        // Si el registro falla, mostrar un mensaje al usuario.
                        Toast.makeText(Register.this, "Error: " + task.getException().getMessage(), Toast.LENGTH_SHORT).show();
                    }
                });
    }

    // UI
    public boolean onOptionsItemSelected(MenuItem item) {
        if (item.getItemId() == android.R.id.home) {
            // Acción de ir hacia atras
            finish();  // Cierra la actividad actual
            return true;
        }
        return super.onOptionsItemSelected(item);
    }




}