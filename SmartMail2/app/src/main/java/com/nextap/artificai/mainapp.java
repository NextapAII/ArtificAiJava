package com.nextap.artificai;

import android.app.Application;
import com.firebase.client.Firebase;


public class mainapp extends Application {
    @Override
    public void onCreate() {

        super.onCreate();
        Firebase.setAndroidContext(this);


    }

    public String getStar(){
        return "Ashwin";
    }
}