package com.example.chatapp_cv;

import android.content.Intent;
import android.content.pm.PackageManager;
import android.os.Build;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.Toast;
import android.Manifest;

import androidx.activity.EdgeToEdge;
import androidx.activity.result.ActivityResultLauncher;
import androidx.activity.result.contract.ActivityResultContracts;
import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.content.ContextCompat;
import androidx.core.graphics.Insets;
import androidx.core.view.ViewCompat;
import androidx.core.view.WindowInsetsCompat;

import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.messaging.FirebaseMessaging;

import java.util.UUID;

public class MainActivity extends AppCompatActivity {

    private String email_example = "cruzcruzvianny@gmail.com";
    private String password_example = "Vianny1213";

    private FirebaseAuth auth;
    private DatabaseReference databaseReference;
    private DatabaseReference fcmTokensRef;
    private String deviceId;

    private ActivityResultLauncher<String> resultLauncher = registerForActivityResult(
            new ActivityResultContracts.RequestPermission(), isGranted -> {
                if (isGranted) {
                    // Permission is granted
                    // Get Device Token from Firebase
                } else {
                    // Permission is denied
                }
            }
    );

    @Override
    protected void onStart() {
        super.onStart();

        // Verificar si el usuario ya ha iniciado sesión
        boolean isLoggedIn = getSharedPreferences("LoginPrefs", MODE_PRIVATE)
                .getBoolean("isLoggedIn", false);

        if (isLoggedIn) {
            // Si el usuario está logueado, redirigir a la actividad principal
            startActivity(new Intent(MainActivity.this, CentralActivity.class));
            finish();  // Evita que el usuario vuelva a la pantalla de login
        }

        // Actualizar el token de FCM si el usuario está logueado
        FirebaseUser currentUser = auth.getCurrentUser();
        if (currentUser != null) {
            //updateFCMToken();
        }
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        EdgeToEdge.enable(this);
        setContentView(R.layout.activity_main);

        auth = FirebaseAuth.getInstance();
        databaseReference = FirebaseDatabase.getInstance().getReference("users");
        fcmTokensRef = FirebaseDatabase.getInstance().getReference("fcmTokens");

        // Generar un identificador único para el dispositivo
        deviceId = UUID.randomUUID().toString();

        requestPermission();

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

    public void requestPermission(){
        if(Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU){
            if(ContextCompat.checkSelfPermission(this, Manifest.permission.POST_NOTIFICATIONS) == PackageManager.PERMISSION_GRANTED){
                // Permission is granted
            } else if (shouldShowRequestPermissionRationale(Manifest.permission.POST_NOTIFICATIONS)) {
                // Permission is denied
            } else {
                // Request permission
                resultLauncher.launch(Manifest.permission.POST_NOTIFICATIONS);
            }
        } else {
            // Get Device Token from Firebase
        }
    }

    // Metodo para actualizar el token de FCM
//    private void updateFCMToken() {
//        FirebaseMessaging.getInstance().getToken()
//                .addOnCompleteListener(new OnCompleteListener<String>() {
//                    @Override
//                    public void onComplete(@NonNull Task<String> task) {
//                        if (!task.isSuccessful()) {
//                            Log.w("CentralActivity", "Fetching FCM registration token failed", task.getException());
//                            return;
//                        }
//
//                        String token = task.getResult();
//                        fcmTokensRef.child(deviceId).setValue(token);
//                    }
//                });
//    }
}