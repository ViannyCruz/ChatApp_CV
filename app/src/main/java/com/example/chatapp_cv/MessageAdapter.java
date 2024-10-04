package com.example.chatapp_cv;

import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.util.Base64;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.List;
import java.util.Locale;

public class MessageAdapter extends RecyclerView.Adapter<MessageAdapter.MessageViewHolder> {

    private List<Message> messages;
    private String currentUserId;

    public MessageAdapter(List<Message> messages, String currentUserId) {
        this.messages = messages;
        this.currentUserId = currentUserId;
    }

    @NonNull
    @Override
    public MessageViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext()).inflate(R.layout.item_message, parent, false);
        return new MessageViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull MessageViewHolder holder, int position) {
        Message message = messages.get(position);
        holder.bind(message);
    }

    @Override
    public int getItemCount() {
        return messages.size();
    }

    public void addMessage(Message message) {
        messages.add(message);
        notifyItemInserted(messages.size() - 1);
    }

    public static class MessageViewHolder extends RecyclerView.ViewHolder {

        private TextView messageText;
        private TextView senderName;
        private TextView timestamp;
        private ImageView messageImage;

        public MessageViewHolder(@NonNull View itemView) {
            super(itemView);
            messageText = itemView.findViewById(R.id.messageText);
            senderName = itemView.findViewById(R.id.senderName);
            timestamp = itemView.findViewById(R.id.timestamp);
            messageImage = itemView.findViewById(R.id.messageImage);
        }

        public void bind(Message message) {
            if (message.getText() != null && !message.getText().isEmpty()) {
                messageText.setVisibility(View.VISIBLE);
                messageText.setText(message.getText());
            } else {
                messageText.setVisibility(View.GONE);
            }

            if (message.getImageBase64() != null && !message.getImageBase64().isEmpty()) {
                messageImage.setVisibility(View.VISIBLE);
                byte[] decodedString = Base64.decode(message.getImageBase64(), Base64.DEFAULT);
                Bitmap decodedByte = BitmapFactory.decodeByteArray(decodedString, 0, decodedString.length);
                messageImage.setImageBitmap(decodedByte);
            } else {
                messageImage.setVisibility(View.GONE);
            }

            senderName.setText(message.getUserId());

            // Formatear el timestamp
            SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss", Locale.getDefault());
            String formattedDate = sdf.format(new Date(message.getTimestamp()));
            timestamp.setText(formattedDate);
        }
    }
}