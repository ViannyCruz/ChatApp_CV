package com.example.chatapp_cv;

import android.content.Intent;
import android.os.Bundle;
import android.text.Editable;
import android.text.TextWatcher;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.LinearLayout;
import android.widget.Toast;

import androidx.activity.EdgeToEdge;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.graphics.Insets;
import androidx.core.view.ViewCompat;
import androidx.core.view.WindowInsetsCompat;

import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class CentralActivity extends AppCompatActivity {

    private FirebaseDatabase database;
    private DatabaseReference usersRef, chatsRef;
    private FirebaseAuth auth;
    private String currentUserId;

    private List<Chat> chats;
    private List<User> users;

    private EditText searchEditText;
    private LinearLayout chatListLayout, userListLayout;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        EdgeToEdge.enable(this);
        setContentView(R.layout.activity_central);
        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.main), (v, insets) -> {
            Insets systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars());
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom);
            return insets;
        });

        database = FirebaseDatabase.getInstance();
        usersRef = database.getReference("users");
        chatsRef = database.getReference("chats");
        auth = FirebaseAuth.getInstance();
        currentUserId = auth.getCurrentUser().getUid();

        chats = new ArrayList<>();
        users = new ArrayList<>();

        searchEditText = findViewById(R.id.searchEditText);
        chatListLayout = findViewById(R.id.chatListLayout);
        userListLayout = findViewById(R.id.userListLayout);

        loadChats();
        setupSearch();
    }

    private void loadChats() {
        chatsRef.addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(DataSnapshot dataSnapshot) {
                chats.clear();
                for (DataSnapshot snapshot : dataSnapshot.getChildren()) {
                    Chat chat = snapshot.getValue(Chat.class);
                    if (chat != null && (chat.getUserId1().equals(currentUserId) || chat.getUserId2().equals(currentUserId))) {
                        chats.add(chat);
                    }
                }
                displayChats();
            }

            @Override
            public void onCancelled(DatabaseError error) {
                // Error al leer los chats
            }
        });
    }

    private void displayChats() {
        chatListLayout.removeAllViews(); // Limpiar cualquier vista previa

        for (Chat chat : chats) {
            String chatPartnerId = chat.getUserId1().equals(currentUserId) ? chat.getUserId2() : chat.getUserId1();
            Button button = new Button(this);
            button.setText(chatPartnerId); // Aquí deberías obtener el nombre del usuario en lugar del ID
            button.setOnClickListener(v -> {
                Intent intent = new Intent(CentralActivity.this, ChatActivity.class);
                intent.putExtra("chatId", chat.getUserId1() + "_" + chat.getUserId2());
                startActivity(intent);
            });
            chatListLayout.addView(button);
        }
    }

    private void setupSearch() {
        searchEditText.addTextChangedListener(new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence s, int start, int count, int after) {}

            @Override
            public void onTextChanged(CharSequence s, int start, int before, int count) {
                searchUsers(s.toString());
            }

            @Override
            public void afterTextChanged(Editable s) {}
        });
    }

    private void searchUsers(String query) {
        usersRef.orderByChild("username").startAt(query).endAt(query + "\uf8ff")
                .addValueEventListener(new ValueEventListener() {
                    @Override
                    public void onDataChange(DataSnapshot dataSnapshot) {
                        users.clear();
                        for (DataSnapshot snapshot : dataSnapshot.getChildren()) {
                            User user = snapshot.getValue(User.class);
                            if (user != null) {
                                users.add(user);
                            }
                        }
                        displayUsers();
                    }

                    @Override
                    public void onCancelled(DatabaseError error) {
                        // Error al leer los usuarios
                    }
                });
    }

    private void displayUsers() {
        userListLayout.removeAllViews(); // Limpiar cualquier vista previa

        for (User user : users) {
            Button button = new Button(this);
            button.setText(user.getUsername());
            button.setOnClickListener(v -> {
                createChat(user.getUsername());
            });
            userListLayout.addView(button);
        }
    }

    private void createChat(String otherUsername) {
        usersRef.orderByChild("username").equalTo(otherUsername).addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(DataSnapshot dataSnapshot) {
                if (dataSnapshot.exists()) {
                    for (DataSnapshot snapshot : dataSnapshot.getChildren()) {
                        String otherUserId = snapshot.getKey();
                        String chatId = currentUserId.compareTo(otherUserId) < 0 ? currentUserId + "_" + otherUserId : otherUserId + "_" + currentUserId;
                        Map<String, Object> chatData = new HashMap<>();
                        chatData.put("userId1", currentUserId);
                        chatData.put("userId2", otherUserId);
                        chatsRef.child(chatId).setValue(chatData)
                                .addOnCompleteListener(task -> {
                                    if (task.isSuccessful()) {
                                        Toast.makeText(CentralActivity.this, "Chat creado con éxito", Toast.LENGTH_SHORT).show();
                                    } else {
                                        Toast.makeText(CentralActivity.this, "Error al crear el chat", Toast.LENGTH_SHORT).show();
                                    }
                                });
                    }
                }
            }

            @Override
            public void onCancelled(DatabaseError error) {
                // Error al leer los usuarios
            }
        });
    }
}