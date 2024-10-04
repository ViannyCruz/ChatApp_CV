package com.example.chatapp_cv;

import android.graphics.drawable.Drawable;
import android.os.Bundle;
import androidx.activity.EdgeToEdge;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.content.ContextCompat;
import androidx.core.graphics.Insets;
import androidx.core.view.ViewCompat;
import androidx.core.view.WindowInsetsCompat;
import androidx.appcompat.widget.Toolbar;
import android.content.Intent;
import android.view.View;

import com.google.firebase.auth.FirebaseAuth;

import java.util.Objects;

public class Settings extends AppCompatActivity {
    private FirebaseAuth Auth;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        EdgeToEdge.enable(this);
        setContentView(R.layout.activity_settings);

        Auth = FirebaseAuth.getInstance();

        // Inicializar la toolbar
        Toolbar toolbar = findViewById(R.id.toolbarsettings);
        setSupportActionBar(toolbar);

        // Habilitar flecha
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

        toolbar.setNavigationOnClickListener(v -> onBackPressed());
        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.main), (v, insets) -> {
            Insets systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars());
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom);
            return insets;
        });
    }

    public void logoutUser(View v) {
        Auth.signOut();

        // Limpiar el estado de sesion y el ID de usuario
        getSharedPreferences("LoginPrefs", MODE_PRIVATE)
                .edit()
                .remove("isLoggedIn")
                .remove("userId")  // Eliminar el ID de usuario
                .apply();

        // Redirigir al usuario a la pantalla de login
        startActivity(new Intent(Settings.this, MainActivity.class));
        finish();
    }
}