package com.bytedance.day20220113_1;

import androidx.appcompat.app.AppCompatActivity;

import android.os.Bundle;
import android.util.Log;

public class MainActivity extends AppCompatActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        SlideMenuView slideMenuView = findViewById(R.id.slide_menu_view);
        slideMenuView.setOnEditClickListener(new SlideMenuView.OnEditClickListener() {
            @Override
            public void onReadClick() {
                Log.e("marden", "点击已读按钮");
            }

            @Override
            public void onTopClick() {
                Log.e("marden", "点击置顶按钮");
            }

            @Override
            public void onDeleteClick() {
                Log.e("marden", "点击删除按钮");
            }
        });
    }
}