package com.example.zhanglichao.universalmusicplayer;

import android.Manifest;
import android.annotation.SuppressLint;
import android.app.Activity;
import android.bluetooth.BluetoothAdapter;
import android.content.Intent;
import android.nfc.NfcAdapter;
import android.nfc.Tag;
import android.os.Build;
import android.os.CountDownTimer;
import android.os.Handler;
import android.os.Message;
import android.support.annotation.Nullable;
import android.support.annotation.RequiresApi;
import android.support.annotation.RequiresPermission;
import android.support.v7.app.AlertDialog;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.text.LoginFilter;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.TableLayout;

import com.sunrise.icardreader.model.IdentityCardZ;


public class MainActivity extends AppCompatActivity implements NFCUtils.OnNfcLisener {

    private static final String TAG = "MainActivity";
    private Button button;
    private CountDownTimer countDownTimer;
    private NFCUtils nfcUtils;
    private View button1;


    @RequiresApi(api = Build.VERSION_CODES.KITKAT)
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        button = findViewById(R.id.button);
        button1 = findViewById(R.id.button1);
        if (Build.VERSION.SDK_INT >= 6.0) {
            RequestPermissionUtil.request(this, new String[]{Manifest.permission.READ_PHONE_STATE,
                    Manifest.permission.READ_EXTERNAL_STORAGE, Manifest.permission.WRITE_EXTERNAL_STORAGE, Manifest.permission.WRITE_SETTINGS});
        }
        nfcUtils = NFCUtils.initNFAtils(this);
        nfcUtils.setOnNfcLisener(this);
        button.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                nfcUtils.startNfa();
            }
        });
        button1.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                nfcUtils.stopNfc();
            }
        });

    }

    /**
     * 开启身份读取
     */
    private void startTimer() {
        countDownTimer = new CountDownTimer(10000, 200) {
            @Override
            public void onTick(long millisUntilFinished) {


                countDownTimer.onFinish();
                countDownTimer.cancel();
            }

            @Override
            public void onFinish() {
                Log.e("333", "eeeee");

            }
        };


    }

    @Override
    public void onSueeccd(IdentityCardZ identityCardZ) {
        String name = identityCardZ.name;
        String sex = identityCardZ.sex;
        String expiryDate = identityCardZ.expiryDate;
        String authorityCode = identityCardZ.authorityCode;
        Log.e(TAG, name + "==" + sex + "==" + expiryDate + "==" + authorityCode);
    }

    @Override
    public void onError(String error) {
        Log.e(TAG, error);
    }
}

