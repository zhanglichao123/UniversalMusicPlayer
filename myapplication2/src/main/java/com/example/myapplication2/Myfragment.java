package com.example.myapplication2;

import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.v4.app.Fragment;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

public class Myfragment extends Fragment {
    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {

        TextView textView = new TextView(getContext());
        textView.setText("12312312312312313131");
        Log.e("张", "onCreateView");
        return textView;
    }

    @Override
    public void onActivityCreated(@Nullable Bundle savedInstanceState) {
        super.onActivityCreated(savedInstanceState);
        Log.e("张", "onActivityCreated");
    }

    @Override
    public void onResume() {
        super.onResume();
        Log.e("张", "onResume");
    }

    @Override
    public void onPause() {
        super.onPause();
    }
}
