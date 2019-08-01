package com.example.myapplication2;

import android.app.Service;
import android.content.Intent;
import android.os.Binder;
import android.os.IBinder;
import android.util.Log;

public class MyService extends Service {
    MyBind myBind = new MyBind();

    @Override
    public void onCreate() {
        super.onCreate();
        Log.e("张","onCreate");
    }

    @Override
    public IBinder onBind(Intent intent) {
        return myBind;
    }


    class MyBind extends Binder {



    }


}
