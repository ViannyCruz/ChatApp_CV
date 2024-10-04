package com.example.chatapp_cv;

public class Message {
    private String text;
    private String userId;
    private long timestamp;
    private String imageBase64; // Nuevo campo para la imagen en base64

    // Constructores, getters y setters

    public Message() {
        // Constructor vacío requerido por Firebase
    }

    public Message(String text, String userId, long timestamp, String imageBase64) {
        this.text = text;
        this.userId = userId;
        this.timestamp = timestamp;
        this.imageBase64 = imageBase64;
    }

    public String getText() {
        return text;
    }

    public void setText(String text) {
        this.text = text;
    }

    public String getUserId() {
        return userId;
    }

    public void setUserId(String userId) {
        this.userId = userId;
    }

    public long getTimestamp() {
        return timestamp;
    }

    public void setTimestamp(long timestamp) {
        this.timestamp = timestamp;
    }

    public String getImageBase64() {
        return imageBase64;
    }

    public void setImageBase64(String imageBase64) {
        this.imageBase64 = imageBase64;
    }
}