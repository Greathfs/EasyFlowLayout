package com.example.easyflowlayout;

import androidx.appcompat.app.AppCompatActivity;

import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.TextView;

import com.example.flowlayout.view.FixFlowLayout;
import com.example.flowlayout.view.FlowLayout;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

public class FlowLayoutActivity extends AppCompatActivity {

    private int mIndex = 0;

    private static final List<String> mDataList = Arrays.asList(
            "Android", "IOS6666666666666666666666666666666666666666666666666666666666666", "PC", "666666666", "3123123", "312313121", "321dasdadaadasdad"
    );

    private FixFlowLayout mFlowLayout;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_flow_layout);
        mFlowLayout = findViewById(R.id.flow_layout);
    }

    public void addTag(View view) {
        TextView textView = (TextView) LayoutInflater.from(this).inflate(R.layout.item_tag, mFlowLayout, false);
        textView.setText(mDataList.get(mIndex));
        mFlowLayout.addView(textView);
        mIndex++;
        if (mIndex > mDataList.size() - 1) {
            mIndex = 0;
        }
    }
}
