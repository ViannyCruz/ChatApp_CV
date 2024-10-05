package com.example.chatapp_cv;

import android.util.Log;

import androidx.annotation.NonNull;

import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;

import java.io.IOException;

import okhttp3.Call;
import okhttp3.Callback;
import okhttp3.MediaType;
import okhttp3.OkHttpClient;
import okhttp3.Request;
import okhttp3.RequestBody;
import okhttp3.Response;

public class NotificationSender {

    private static final String FCM_API_URL = "https://fcm.googleapis.com/fcm/send";
    private static final String SERVER_KEY = "YOUR_SERVER_KEY";

    public static void sendNotificationToUser(String userId, String title, String body) {
        DatabaseReference databaseReference = FirebaseDatabase.getInstance().getReference("users");
        databaseReference.child(userId).child("fcmToken").addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                String fcmToken = dataSnapshot.getValue(String.class);
                if (fcmToken != null) {
                    sendNotification(fcmToken, title, body);
                }
            }

            @Override
            public void onCancelled(@NonNull DatabaseError databaseError) {
                Log.w("NotificationSender", "Failed to read FCM Token", databaseError.toException());
            }
        });
    }

    private static void sendNotification(String fcmToken, String title, String body) {
        OkHttpClient client = new OkHttpClient();
        MediaType mediaType = MediaType.parse("application/json");
        String json = "{\"to\":\"" + fcmToken + "\",\"notification\":{\"title\":\"" + title + "\",\"body\":\"" + body + "\"}}";
        RequestBody requestBody = RequestBody.create(mediaType, json);

        Request request = new Request.Builder()
                .url(FCM_API_URL)
                .post(requestBody)
                .addHeader("Authorization", "key=" + SERVER_KEY)
                .addHeader("Content-Type", "application/json")
                .build();

        client.newCall(request).enqueue(new Callback() {
            @Override
            public void onFailure(Call call, IOException e) {
                Log.w("NotificationSender", "Failed to send notification", e);
            }

            @Override
            public void onResponse(Call call, Response response) throws IOException {
                if (response.isSuccessful()) {
                    Log.d("NotificationSender", "Notification sent successfully");
                } else {
                    Log.w("NotificationSender", "Failed to send notification: " + response.body().string());
                }
            }
        });
    }
}