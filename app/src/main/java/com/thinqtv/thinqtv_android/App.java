package com.thinqtv.thinqtv_android;

import android.app.Application;
import android.app.NotificationChannel;
import android.app.NotificationManager;
import android.os.Build;

import com.jakewharton.threetenabp.AndroidThreeTen;

import com.stripe.android.PaymentConfiguration;

public class App extends Application {
    @Override
    public void onCreate() {
        super.onCreate();
        AndroidThreeTen.init(this);

        PaymentConfiguration.init(
                getApplicationContext(),
                "pk_test_wWEX8coPe3XAN08BCg8wk7hg00b8AUS35M"
        );

        createNotificationChannel();
    }

    private void createNotificationChannel() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            CharSequence name = "main";
            String description = "The main notification channel.";
            int importance = NotificationManager.IMPORTANCE_DEFAULT;
            NotificationChannel channel = new NotificationChannel("main-id", name, importance);
            channel.setDescription(description);
            NotificationManager notificationManager = getSystemService(NotificationManager.class);
            notificationManager.createNotificationChannel(channel);
        }
    }
}
