package com.example.myapplication;

import android.os.Bundle;
import android.support.v4.app.FragmentTransaction;
import android.support.v7.app.AppCompatActivity;

import android.view.View;
import android.widget.Button;

import java.util.ArrayList;
import java.util.List;

public class Main2Activity extends AppCompatActivity implements View.OnClickListener {

    private Button viewById;
    private Button viewById1;
    private FragmentTransaction fragmentTransaction;
    private Myfragment2 myfragment2;
    private Myfragment1 myfragment1;

    ArrayList<String> list = new ArrayList<>();
    private Bean bean;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main2);
        viewById1 = findViewById(R.id.button);
        viewById1.setOnClickListener(this);
        fragmentTransaction = getSupportFragmentManager().beginTransaction();
        myfragment1 = new Myfragment1();
        myfragment2 = new Myfragment2();
        Bundle bundle = new Bundle();
        bean = new Bean();
        bean.setString("aaaaaaa");
        bundle.putSerializable("data", bean);
        myfragment1.setArguments(bundle);
        fragmentTransaction.add(R.id.fregment, myfragment1).commit();
    }

    @Override
    public void onClick(View v) {
        Bundle bundle = new Bundle();
        bundle.putSerializable("data", bean);
        myfragment2.setArguments(bundle);
        FragmentTransaction fragmentTransaction = getSupportFragmentManager().beginTransaction();
        if (!myfragment2.isAdded()) {

            fragmentTransaction.add(R.id.fregment, myfragment2).hide(myfragment1).commit();

        } else {
            fragmentTransaction.show(myfragment2).hide(myfragment1).commit();
        }
    }
}
