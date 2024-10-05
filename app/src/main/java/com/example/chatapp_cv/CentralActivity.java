package com.example.chatapp_cv;

import android.content.Intent;
import android.os.Bundle;
import android.text.Editable;
import android.text.TextWatcher;
import android.view.View;
import android.widget.EditText;
import android.widget.Toast;

import androidx.activity.EdgeToEdge;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.graphics.Insets;
import androidx.core.view.ViewCompat;
import androidx.core.view.WindowInsetsCompat;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.google.android.material.floatingactionbutton.FloatingActionButton;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;

import java.util.ArrayList;
import java.util.List;

public class CentralActivity extends AppCompatActivity {

    private FirebaseDatabase database;
    private DatabaseReference usersRef, chatsRef;
    private FirebaseAuth auth;
    private String currentUserId;

    private List<Chat> chats;
    private List<User> users;

    private EditText searchEditText;
    private RecyclerView chatRecyclerView, userListRecyclerView;
    private ChatAdapter chatAdapter;
    private UserAdapter userAdapter;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        EdgeToEdge.enable(this);
        setContentView(R.layout.activity_central);
        FloatingActionButton fab = findViewById(R.id.floatingActionButton);
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
        chatRecyclerView = findViewById(R.id.chatRecyclerView);
        userListRecyclerView = findViewById(R.id.userListRecyclerView);

        // Configurar RecyclerView y Adapter para chats
        chatRecyclerView.setLayoutManager(new LinearLayoutManager(this));
        chatAdapter = new ChatAdapter(chats, currentUserId, this);
        chatRecyclerView.setAdapter(chatAdapter);

        // Configurar RecyclerView y Adapter para usuarios
        userListRecyclerView.setLayoutManager(new LinearLayoutManager(this));
        userAdapter = new UserAdapter(users, this);
        userListRecyclerView.setAdapter(userAdapter);

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
                chatAdapter.notifyDataSetChanged();
            }

            @Override
            public void onCancelled(DatabaseError error) {
                // Error al leer los chats
            }
        });
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
                        userAdapter.notifyDataSetChanged();
                    }

                    @Override
                    public void onCancelled(DatabaseError error) {
                        // Error al leer los usuarios
                    }
                });
    }


    public void goToSettingsView(View v) {
        Intent intent = new Intent(CentralActivity.this, Settings.class);
        startActivity(intent);
    }

    public void goToAddUserView(View v) {
        Intent intent = new Intent(CentralActivity.this, AddChat.class);
        startActivity(intent);
    }
}
