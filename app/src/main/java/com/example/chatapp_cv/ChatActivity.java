package com.example.chatapp_cv;

import android.app.Activity;
import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
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
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

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

    private void sendMessage() {
        String text = messageInput.getText().toString().trim();
        if (!text.isEmpty()) {
            String key = messagesRef.push().getKey();
            Map<String, Object> messageData = new HashMap<>();
            messageData.put("text", text);
            messageData.put("userId", currentUserId);
            messageData.put("timestamp", System.currentTimeMillis());

            messagesRef.child(key).setValue(messageData);
            messageInput.setText("");
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

            messagesRef.child(key).setValue(messageData);
        } catch (IOException e) {
            Log.e("ChatActivity", "Error al convertir la imagen a base64", e);
            Toast.makeText(ChatActivity.this, "Error al convertir la imagen", Toast.LENGTH_SHORT).show();
        }
    }
}