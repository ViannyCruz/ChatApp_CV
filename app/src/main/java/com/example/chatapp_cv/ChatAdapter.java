package com.example.chatapp_cv;

import static android.content.Context.MODE_PRIVATE;

import android.annotation.SuppressLint;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.cardview.widget.CardView;
import androidx.recyclerview.widget.RecyclerView;

import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;

import java.util.List;
import java.util.Objects;

public class ChatAdapter extends RecyclerView.Adapter<ChatAdapter.ChatViewHolder> {

    private List<Chat> chats;
    private Context context;
    private DatabaseReference usersRef;

    // Variables para el estado de sesión
    private boolean isLoggedIn;
    private String currentUserId;

    public ChatAdapter(List<Chat> chats, String currentUserId, Context context) {
        this.chats = chats;
        this.context = context;
        this.usersRef = FirebaseDatabase.getInstance().getReference("users");

        // Acceder a SharedPreferences
        SharedPreferences sharedPreferences = context.getSharedPreferences("LoginPrefs", MODE_PRIVATE);
        this.isLoggedIn = sharedPreferences.getBoolean("isLoggedIn", false);
        this.currentUserId = sharedPreferences.getString("userId", null);
    }

    @NonNull
    @Override
    public ChatViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext()).inflate(R.layout.item_chat, parent, false);
        return new ChatViewHolder(view);
    }

    @SuppressLint("SetTextI18n")
    @Override
    public void onBindViewHolder(@NonNull ChatViewHolder holder, int position) {
        Chat chat = chats.get(position);

        // Obtener el otro usuario en el chat
        String userId;
        if (Objects.equals(currentUserId, chat.getUserId1()))
            userId = chat.getUserId2();
        else
            userId = chat.getUserId1();

        // Buscar el nombre de usuario en la base de datos
        usersRef.child(userId).addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                if (dataSnapshot.exists()) {
                    String username = dataSnapshot.child("username").getValue(String.class);
                    holder.usernameTextView.setText(username);
                } else {
                    holder.usernameTextView.setText("Usuario no encontrado");
                }
            }

            @Override
            public void onCancelled(@NonNull DatabaseError databaseError) {
                // Manejar errores
            }
        });

        // Verificación y consulta de mensajes en la base de datos
        DatabaseReference messagesRef = FirebaseDatabase.getInstance().getReference("chats")
                .child(chat.getUserId1() + "_" + chat.getUserId2()).child("messages");

        Log.d("Firebase", "Referencia a los mensajes: " + messagesRef.toString());

        messagesRef.orderByChild("timestamp").limitToLast(1)
                .addListenerForSingleValueEvent(new ValueEventListener() {
                    @Override
                    public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                        Log.d("Firebase", "DataSnapshot: " + dataSnapshot.toString());  // Verifica si hay datos
                        if (dataSnapshot.exists()) {
                            for (DataSnapshot snapshot : dataSnapshot.getChildren()) {
                                String lastMessageText = snapshot.child("text").getValue(String.class);
                                Log.d("Firebase", "Último mensaje obtenido: " + lastMessageText);  // Verifica el último mensaje
                                holder.lastMessageTextView.setText(lastMessageText != null ? lastMessageText : "Imagen");
                            }
                        } else {
                            Log.d("Firebase", "No hay mensajes");  // Verificación en caso de no encontrar mensajes
                           // holder.lastMessageTextView.setText("Sin mensajes");
                            holder.lastMessageTextView.setText("");
                        }
                    }

                    @Override
                    public void onCancelled(@NonNull DatabaseError databaseError) {
                        Log.d("Firebase", "Error en la consulta de mensajes: " + databaseError.getMessage());  // Log de error
                    }
                });

        // Abrir el chat cuando se haga clic en el cardView
        holder.cardView.setOnClickListener(v -> {
            Intent intent = new Intent(context, ChatActivity.class);
            intent.putExtra("chatId", chat.getUserId1() + "_" + chat.getUserId2());
            context.startActivity(intent);
        });
    }



    @Override
    public int getItemCount() {
        return chats.size();
    }

    public static class ChatViewHolder extends RecyclerView.ViewHolder {

        TextView usernameTextView;
        TextView lastMessageTextView;
        ImageView chatImageView;
        CardView cardView;

        public ChatViewHolder(@NonNull View itemView) {
            super(itemView);
            chatImageView = itemView.findViewById(R.id.chat_image);
            usernameTextView = itemView.findViewById(R.id.chat_username);
            lastMessageTextView = itemView.findViewById(R.id.chat_last_message);
            cardView = itemView.findViewById(R.id.chatcard);
        }
    }
}
