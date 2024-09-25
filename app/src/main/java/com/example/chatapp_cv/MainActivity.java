package com.example.chatapp_cv;

import android.content.Intent;
import android.os.Bundle;
import android.view.View;

import androidx.activity.EdgeToEdge;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.graphics.Insets;
import androidx.core.view.ViewCompat;
import androidx.core.view.WindowInsetsCompat;

import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;


public class MainActivity extends AppCompatActivity {

    private String email_example = "cruzcruzvianny@gmail.com";
    private String password_example = "Vianny1213";
    private FirebaseAuth Auth;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        EdgeToEdge.enable(this);
        setContentView(R.layout.activity_main);



        Auth = FirebaseAuth.getInstance();
        FirebaseUser currentUser = Auth.getCurrentUser();

        if (currentUser != null) {
            // Si el usuario ya está autenticado, lo rediriges a la actividad principal
            startActivity(new Intent(MainActivity.this, CentralActivity.class));
        } else {
            // Si no hay ningún usuario autenticado, lo rediriges a la actividad de login
            startActivity(new Intent(MainActivity.this, CentralActivity.class));
        }
        finish();

        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.main), (v, insets) -> {
            Insets systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars());
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom);
            return insets;
        });




    }


    public void goToRegisterView(View v) {
        Intent intent = new Intent(MainActivity.this, Register.class);
        startActivity(intent);
    }

    public void goToLoginView(View v) {
        Intent intent = new Intent(MainActivity.this, Login.class);
        startActivity(intent);
    }
}