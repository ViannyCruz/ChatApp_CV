package com.example.chatapp_cv;

import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.LinearLayout;

import androidx.appcompat.app.AppCompatActivity;

import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;

import java.util.ArrayList;
import java.util.List;

public class ChatListActivity extends AppCompatActivity {

    private FirebaseDatabase database;
    private DatabaseReference chatsRef;
    private FirebaseAuth auth;
    private String currentUserId;

    private List<Chat> chats;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_chat_list);

        database = FirebaseDatabase.getInstance();
        chatsRef = database.getReference("chats");
        auth = FirebaseAuth.getInstance();
        currentUserId = auth.getCurrentUser().getUid();

        chats = new ArrayList<>();

        loadChats();
    }

    private void loadChats() {
        chatsRef.addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(DataSnapshot dataSnapshot) {
                chats.clear();
                for (DataSnapshot snapshot : dataSnapshot.getChildren()) {
                    Chat chat = snapshot.getValue(Chat.class);
                    if (chat.getUserId1().equals(currentUserId) || chat.getUserId2().equals(currentUserId)) {
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
        for (Chat chat : chats) {
            String chatPartnerId = chat.getUserId1().equals(currentUserId) ? chat.getUserId2() : chat.getUserId1();
            Button button = new Button(this);
            button.setText(chatPartnerId); // Aquí deberías obtener el nombre del usuario en lugar del ID
            button.setOnClickListener(v -> {
                Intent intent = new Intent(ChatListActivity.this, ChatActivity.class);
                intent.putExtra("chatId", chat.getUserId1() + "_" + chat.getUserId2());
                startActivity(intent);
            });
            ((LinearLayout) findViewById(R.id.chatListLayout)).addView(button);
        }
    }
}