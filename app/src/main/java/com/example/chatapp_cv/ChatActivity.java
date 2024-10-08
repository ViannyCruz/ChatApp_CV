package com.example.chatapp_cv;

import android.app.Activity;
import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.drawable.Drawable;
import android.net.Uri;
import android.os.Bundle;
import android.util.Base64;
import android.util.Log;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Toast;

import androidx.activity.result.ActivityResultLauncher;
import androidx.activity.result.contract.ActivityResultContracts;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;
import androidx.core.content.ContextCompat;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.google.auth.oauth2.AccessToken;
import com.google.firebase.FirebaseApp;
import com.google.firebase.FirebaseOptions;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;

import org.json.JSONException;
import org.json.JSONObject;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.nio.charset.StandardCharsets;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Objects;

import okhttp3.Call;
import okhttp3.Callback;
import okhttp3.MediaType;
import okhttp3.OkHttpClient;
import okhttp3.Request;
import okhttp3.RequestBody;
import okhttp3.Response;
import com.google.auth.oauth2.GoogleCredentials;
import com.google.gson.Gson;


import java.io.FileInputStream;
import java.io.IOException;
public class ChatActivity extends AppCompatActivity {

    private FirebaseDatabase database;
    private DatabaseReference messagesRef;
    private FirebaseAuth auth;
    private String currentUserId;
    private String chatId;

    private RecyclerView recyclerView;
    private MessageAdapter adapter;
    private List<Message> messages;

    private EditText messageInput;
    private Button sendButton, attachImageButton;

    private ActivityResultLauncher<Intent> imagePickerLauncher;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_chat);



        Toolbar toolbar = findViewById(R.id.toolbar);
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



















        database = FirebaseDatabase.getInstance();
        auth = FirebaseAuth.getInstance();
        currentUserId = auth.getCurrentUser().getUid();
        chatId = getIntent().getStringExtra("chatId");



        // Verificar que chatId no sea null
        if (chatId == null) {
            throw new IllegalArgumentException("chatId cannot be null");
        }

        messagesRef = database.getReference("messages").child(chatId);

        recyclerView = findViewById(R.id.recyclerViewView);
        messageInput = findViewById(R.id.messageInputInput);
        sendButton = findViewById(R.id.sendButton);
        attachImageButton = findViewById(R.id.attachImageButton);

        messages = new ArrayList<>();
        adapter = new MessageAdapter(messages, currentUserId);
        recyclerView.setLayoutManager(new LinearLayoutManager(this));
        recyclerView.setAdapter(adapter);

        sendButton.setOnClickListener(v -> sendMessage());
        attachImageButton.setOnClickListener(v -> openImagePicker());

        listenForMessages();

        imagePickerLauncher = registerForActivityResult(
                new ActivityResultContracts.StartActivityForResult(),
                result -> {
                    if (result.getResultCode() == Activity.RESULT_OK) {
                        Intent data = result.getData();
                        if (data != null) {
                            Uri imageUri = data.getData();
                            uploadImage(imageUri);
                        }
                    }
                }
        );
    }


/*
    private void sendMessage() {
        String text = messageInput.getText().toString().trim();
        if (!text.isEmpty()) {
            // Token del dispositivo al que se enviará la notificación
            String deviceToken = "dohGQQn2STaN6L0mhZe4iC:APA91bHO_-Znkjv6H0PxskzE3XT-weFFeHEyzyatxr6wCoUJqIMlKfn-CS_Lo_Yngie7hLFxXJLtyHw846zai53V0BFtNEpp6-76cqC4CpNAJLCClIgEqMxaCSFtAiTl0B_4mwPfVXxh";






            String key = messagesRef.push().getKey();
            Map<String, Object> messageData = new HashMap<>();
            messageData.put("text", text);
            messageData.put("userId", currentUserId);
            messageData.put("timestamp", System.currentTimeMillis());




            messagesRef.child(key).setValue(messageData).addOnSuccessListener(aVoid -> {
                // Enviar notificación push al destinatario
                sendPushNotification(text);
            }).addOnFailureListener(e -> {
                Toast.makeText(ChatActivity.this, "Error al enviar el mensaje", Toast.LENGTH_SHORT).show();
            });

            messageInput.setText("");
        }
    }
*/

    private void sendMessage() {
        String text = messageInput.getText().toString().trim();
        if (!text.isEmpty()) {
            String chatId = getIntent().getStringExtra("chatId");

            if (chatId == null) {
                throw new IllegalArgumentException("chatId cannot be null");
            }

            DatabaseReference chatRef = FirebaseDatabase.getInstance().getReference("chats").child(chatId);

            chatRef.addListenerForSingleValueEvent(new ValueEventListener() {
                @Override
                public void onDataChange(DataSnapshot dataSnapshot) {
                    if (dataSnapshot.exists()) {
                        Chat chat = dataSnapshot.getValue(Chat.class);
                        if (chat != null) {
                            String otherUserId = chat.getOtherUserId(currentUserId);
                            DatabaseReference otherUserRef = FirebaseDatabase.getInstance().getReference("users").child(otherUserId);

                            otherUserRef.addListenerForSingleValueEvent(new ValueEventListener() {
                                @Override
                                public void onDataChange(DataSnapshot dataSnapshot) {
                                    if (dataSnapshot.exists()) {
                                        String deviceToken = dataSnapshot.child("fcmToken").getValue(String.class);

                                        String key = messagesRef.push().getKey();
                                        Map<String, Object> messageData = new HashMap<>();
                                        messageData.put("text", text);
                                        messageData.put("userId", currentUserId);
                                        messageData.put("timestamp", System.currentTimeMillis());

                                        messagesRef.child(key).setValue(messageData).addOnSuccessListener(aVoid -> {
                                            if (deviceToken != null) {
                                                new Thread(() -> sendPushNotification(text, deviceToken)).start();
                                            } else {
                                                Log.d("ChatActivity", "Mensaje enviado sin notificación porque el token es nulo");
                                            }
                                        }).addOnFailureListener(e -> {
                                            Toast.makeText(ChatActivity.this, "Error al enviar el mensaje", Toast.LENGTH_SHORT).show();
                                        });

                                        messageInput.setText("");
                                    } else {
                                        Log.e("ChatActivity", "Usuario no encontrado para chatId: " + chatId);
                                    }
                                }

                                @Override
                                public void onCancelled(DatabaseError databaseError) {
                                    Log.e("ChatActivity", "Database error: " + databaseError.getMessage());
                                }
                            });
                        } else {
                            Log.e("ChatActivity", "Error: Chat object is null");
                        }
                    } else {
                        Log.e("ChatActivity", "Chat not found for chatId: " + chatId);
                    }
                }

                @Override
                public void onCancelled(DatabaseError databaseError) {
                    Log.e("ChatActivity", "Database error: " + databaseError.getMessage());
                }
            });
        }
    }
    private void sendPushNotification(String message, String deviceToken) {
        try {
            // Credenciales de la cuenta de servicio
            String serviceAccountJson = "{\n" +
                    "  \"type\": \"service_account\",\n" +
                    "  \"project_id\": \"cvchat-fe45c\",\n" +
                    "  \"private_key_id\": \"b9328826cae61c7ef543983ae704513d524f77f1\",\n" +
                    "  \"private_key\": \"-----BEGIN PRIVATE KEY-----\\nMIIEvQIBADANBgkqhkiG9w0BAQEFAASCBKcwggSjAgEAAoIBAQC92UJ2Yc+0ZQfp\\nUI3XlmSuam5vSTOyLQ+xflv3QdMxEFxbiAiFInLSl3JY5R5wU4bSjRX8UFEbplYw\\nfYa3A3Hgy9VrcqTf2SGrFDgF4MAT1pSEXW4K+V6adnnk7gtYz6MqPR82zf9vKyjM\\nH9D0tJuU0qqTEx0uDYR4rSvzAC42oc9ezxQIYuzFt1xFOsVuistePhhOB4NJnHyA\\n2F6bRXZXvgUqtS2jh+7PwOzzJdT6tsk9RECGCJdJjAxjynmLF1ESdN+IFKD/cXvt\\nT5jdzv/hGIsV+qOYQPEx19tJSuAJ5ddLEUeHgWau486tiQOj43UFG5EgJhiK8Zkh\\nJKRpnl+tAgMBAAECggEAQOZ4tgeR/dVb8wK6uqyLFqUDw79+kLae2sqbndc+31L8\\nf73tS7YPErb7PDB3S2cCfBAHKA486/rdFA6VcxMWZwbHmsfCXVPZEqcCz9+ZSi7c\\n7rMR32aIVL2TxhMqhd3VpabneNDLNbHHbKCjykPDvAiYj1bFbuzoEGkC0TuKkNoA\\nU6uehGB6wCDzuYe6lavCbRN9bscAKJHERNba21Kw96mf+9EGX/gSxV3lIeUHKXUZ\\nz6mRK6hm5y7TjWqhFaA/8NyHk0y36qSaNL7BexhEXOhEY05Yf2S2c/bOxZTn894V\\nGMl7Bhj2QWULkg0vcEM7mVdWnxMAIzapTp2h4cW+8QKBgQD3VcRO3tyFFbz1A+7T\\nYRrICoIbuBLzMcNrTrm2fjmK3Yr4kikae4QUhS/jUsdX/QVwmb76Zl0WVPkm+aO6\\nOZc1OsrOcXMFwNCKRnBcKh3ehOMxDOVBv8ZXQLxj5GX3rz52PJAFK6KtbTB9CLOy\\n1hAdeWa9heqIGX6ou5PBXLpbewKBgQDEf+yf9RyPQxBYDWF4xzTG2Syfo6mGi6Uu\\nX0mUBgnny0Cgwjg5icCiJc+sEYNFBK7LiQSluhfMSKc/9Klkf1QTa3j5L6ERIMbT\\nGtFFdNN2ORlRrRdw9FiHeARx56m/EpkEMdodNGFNUUtTkuhnXcTyNWzitCEntIN6\\njUBBcLeU9wKBgFAJ2TUMuZ/3oOZJF4gf7GK/w8rkjn1UpUCcgUylHEnr14UJB3Qi\\ndGOPlieiKhA3OQNvPXYamhUX+mi6cFlbOatoEykrXWWHN/UHRmUM3A2eQ0ckPSxB\\nuxWRRWWuC7FTbIsmnhSOQl6M7xwHLN+6lgxztOOv6m2QkoRX70CrmULxAoGBALo7\\nA2gWRhsA1FyfUeHF6p8K9N/XnLOHZeWtKW7AymKEn5u8ds0RYSBL9aZ+corXBVKh\\naWYoGKXjtZ9HocM9adF96glAusYg6k7o+614K3HAa96czbqf2ceKV6wcgyQz3BG9\\nrZpAKne8tEdg1CIjHKTxPWEels2qp3FeradXzKXBAoGAWjuHU5RVXp2tlGDvvHZO\\nH0faTLqoghjt3eCaNJtiFtXBQqxu5FmoLEI6HVmY2DdV0TQKLhqZRciFHaOJF0vu\\nnjaE+EwhULZUCbiIxywiaPe9p3AEvk9LgIw7b4QokQX4lmRKUDE9fNscThEblNB5\\nFNd/zYzGNrKkBaJQfsl/q34=\\n-----END PRIVATE KEY-----\\n\",\n" +
                    "  \"client_email\": \"tuma-108@cvchat-fe45c.iam.gserviceaccount.com\",\n" +
                    "  \"client_id\": \"100302066165373793024\",\n" +
                    "  \"auth_uri\": \"https://accounts.google.com/o/oauth2/auth\",\n" +
                    "  \"token_uri\": \"https://oauth2.googleapis.com/token\",\n" +
                    "  \"auth_provider_x509_cert_url\": \"https://www.googleapis.com/oauth2/v1/certs\",\n" +
                    "  \"client_x509_cert_url\": \"https://www.googleapis.com/robot/v1/metadata/x509/tuma-108%40cvchat-fe45c.iam.gserviceaccount.com\",\n" +
                    "  \"universe_domain\": \"googleapis.com\"\n" +
                    "}";

            // Cargar las credenciales de la cuenta de servicio
            InputStream serviceAccount = new ByteArrayInputStream(serviceAccountJson.getBytes(StandardCharsets.UTF_8));
            GoogleCredentials credentials = GoogleCredentials.fromStream(serviceAccount)
                    .createScoped(Arrays.asList("https://www.googleapis.com/auth/firebase.messaging"));

            // Refrescar el token de acceso
            credentials.refreshIfExpired();
            AccessToken accessToken = credentials.getAccessToken();

            // Crear el cuerpo de la notificación
            JSONObject notification = new JSONObject();
            JSONObject notificationBody = new JSONObject();
            JSONObject messageObject = new JSONObject();

            notificationBody.put("title", "Nuevo mensaje");
            notificationBody.put("body", message);
            messageObject.put("token", deviceToken);
            messageObject.put("notification", notificationBody);
            notification.put("message", messageObject);

            MediaType JSON = MediaType.parse("application/json; charset=utf-8");
            RequestBody body = RequestBody.create(JSON, notification.toString());

            // Crear la solicitud HTTP
            Request request = new Request.Builder()
                    .url("https://fcm.googleapis.com/v1/projects/cvchat-fe45c/messages:send")
                    .post(body)
                    .addHeader("Authorization", "Bearer " + accessToken.getTokenValue())
                    .addHeader("Content-Type", "application/json")
                    .build();

            OkHttpClient client = new OkHttpClient();
            client.newCall(request).enqueue(new Callback() {
                @Override
                public void onFailure(Call call, IOException e) {
                    Log.e("ChatActivity", "Error al enviar notificación: " + e.getMessage());
                }

                @Override
                public void onResponse(Call call, Response response) throws IOException {
                    if (response.isSuccessful()) {
                        Log.d("ChatActivity", "Notificación enviada con éxito");
                    } else {
                        String responseBody = response.body().string();
                        Log.e("ChatActivity", "Error al enviar notificación: " + response.code() + " " + response.message() + "\n" + responseBody);
                    }
                }
            });
        } catch (JSONException | IOException e) {
            Log.e("ChatActivity", "Error: " + e.getMessage());
        }
    }


















    private void listenForMessages() {
        messagesRef.addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(DataSnapshot dataSnapshot) {
                messages.clear();
                for (DataSnapshot snapshot : dataSnapshot.getChildren()) {
                    Message message = snapshot.getValue(Message.class);
                    messages.add(message);
                }
                adapter.notifyDataSetChanged();
            }

            @Override
            public void onCancelled(DatabaseError error) {
                // Error al leer los mensajes
            }
        });
    }

    private void openImagePicker() {
        Intent intent = new Intent(Intent.ACTION_GET_CONTENT);
        intent.setType("image/*");
        imagePickerLauncher.launch(intent);
    }

    private void uploadImage(Uri imageUri) {
        if (imageUri == null) {
            Toast.makeText(ChatActivity.this, "URI de imagen no válida", Toast.LENGTH_SHORT).show();
            return;
        }

        try {
            InputStream inputStream = getContentResolver().openInputStream(imageUri);
            Bitmap bitmap = BitmapFactory.decodeStream(inputStream);
            ByteArrayOutputStream byteArrayOutputStream = new ByteArrayOutputStream();
            bitmap.compress(Bitmap.CompressFormat.JPEG, 100, byteArrayOutputStream);
            byte[] byteArray = byteArrayOutputStream.toByteArray();
            String encodedImage = Base64.encodeToString(byteArray, Base64.DEFAULT);

            String key = messagesRef.push().getKey();
            Map<String, Object> messageData = new HashMap<>();
            messageData.put("imageBase64", encodedImage);
            messageData.put("userId", currentUserId);
            messageData.put("timestamp", System.currentTimeMillis());

            messagesRef.child(key).setValue(messageData).addOnSuccessListener(aVoid -> {
                // Enviar notificación push al destinatario
                sendPushNotification("Imagen enviada");
            }).addOnFailureListener(e -> {
                Toast.makeText(ChatActivity.this, "Error al enviar la imagen", Toast.LENGTH_SHORT).show();
            });
        } catch (IOException e) {
            Log.e("ChatActivity", "Error al convertir la imagen a base64", e);
            Toast.makeText(ChatActivity.this, "Error al convertir la imagen", Toast.LENGTH_SHORT).show();
        }
    }

    private void sendPushNotification(String message) {
        // Obtener el token del destinatario desde Firebase Realtime Database
        String recipientUserId = "ID_DEL_DESTINATARIO"; // Reemplaza con el ID del destinatario
        DatabaseReference recipientRef = FirebaseDatabase.getInstance().getReference("users").child(recipientUserId);
        recipientRef.addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(DataSnapshot dataSnapshot) {
                if (dataSnapshot.exists()) {
                    String recipientToken = dataSnapshot.child("fcmToken").getValue(String.class);
                    if (recipientToken != null) {
                        sendNotificationToFCM(recipientToken, message);
                    }
                }
            }

            @Override
            public void onCancelled(DatabaseError databaseError) {
                Log.e("ChatActivity", "Error al obtener el token del destinatario", databaseError.toException());
            }
        });
    }

    private void sendNotificationToFCM(String recipientToken, String message) {
        String serverKey = "AIzaSyBgLw0nNGYFGQ6WC50LhBLZAc2gN2HNgsE";
        String url = "https://fcm.googleapis.com/fcm/send";

        OkHttpClient client = new OkHttpClient();

        Map<String, String> notification = new HashMap<>();
        notification.put("title", "Nuevo Mensaje");
        notification.put("body", message);

        Map<String, Object> body = new HashMap<>();
        body.put("to", recipientToken);
        body.put("notification", notification);

        Gson gson = new Gson();
        String jsonBody = gson.toJson(body);

        RequestBody requestBody = RequestBody.create(MediaType.parse("application/json; charset=utf-8"), jsonBody);

        Request request = new Request.Builder()
                .url(url)
                .post(requestBody)
                .addHeader("Authorization", "key=" + serverKey)
                .addHeader("Content-Type", "application/json")
                .build();

        client.newCall(request).enqueue(new Callback() {
            @Override
            public void onFailure(Call call, IOException e) {
                Log.e("ChatActivity", "Error al enviar la notificación push", e);
            }

            @Override
            public void onResponse(Call call, Response response) throws IOException {
                if (response.isSuccessful()) {
                    Log.d("ChatActivity", "Notificación push enviada con éxito");
                } else {
                    Log.e("ChatActivity", "Error al enviar la notificación push: " + response.body().string());
                }
            }
        });
    }
}