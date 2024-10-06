package com.example.chatapp_cv;

import java.util.ArrayList;
import java.util.List;

public class Chat {
    private String userId1;
    private String userId2;
    private List<Message> messages;

    public Chat() {
        // Constructor vacío requerido por Firebase
    }

    public Chat(String userId1, String userId2) {
        this.userId1 = userId1;
        this.userId2 = userId2;
        this.messages = new ArrayList<>();
    }

    public String getUserId1() {
        return userId1;
    }

    public String getUserId2() {
        return userId2;
    }

    public List<Message> getMessages() {
        return messages;
    }

    public void addMessage(Message message) {
        messages.add(message);
    }

    public String getOtherUserId(String currentUserId) {
        if (userId1.equals(currentUserId)) {
            return userId2;
        } else if (userId2.equals(currentUserId)) {
            return userId1;
        } else {
            throw new IllegalArgumentException("El usuario actual no está en esta conversación.");
        }
    }

}
