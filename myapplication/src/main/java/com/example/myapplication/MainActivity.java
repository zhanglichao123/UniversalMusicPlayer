package com.example.myapplication;

import android.content.Intent;
import android.support.annotation.NonNull;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.TextView;

import java.util.ArrayList;
import java.util.List;

public class MainActivity extends AppCompatActivity {

    List<String> list = new ArrayList<>();

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        list.add("1231");
        list.add("344");
        list.add("22222");
        list.add("555");
        RecyclerView re = findViewById(R.id.rer);
        LinearLayoutManager linearLayoutManager = new LinearLayoutManager(this);
        linearLayoutManager.setStackFromEnd(true);
        linearLayoutManager.setReverseLayout(true);
        re.setLayoutManager(linearLayoutManager);
        My my = new My();
        re.setAdapter(my);
        Intent intent = new Intent(this, Main2Activity.class);
        startActivity(intent);

    }


    class My extends RecyclerView.Adapter<My.MYh> {


        @NonNull
        @Override
        public MYh onCreateViewHolder(@NonNull ViewGroup viewGroup, int i) {
            TextView textView = new TextView(MainActivity.this);
            MYh mYh = new MYh(textView);
            return mYh;
        }

        @Override
        public void onBindViewHolder(@NonNull MYh mYh, int i) {
            mYh.textView.setText(list.get(i));

        }

        @Override
        public int getItemCount() {
            return list.size();
        }

        class MYh extends RecyclerView.ViewHolder {
            TextView textView;

            public MYh(@NonNull View itemView) {
                super(itemView);
                textView = (TextView) itemView;
            }
        }
    }

}
