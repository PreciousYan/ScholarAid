package com.example.myapplication2;

import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;
import android.widget.Button;

import androidx.appcompat.app.AppCompatActivity;

import com.example.myapplication2.PdfActivity;
import com.example.myapplication2.TranslateActivity;

public class MainActivity extends AppCompatActivity {

    private Button libraryButton;
    private Button translateButton;
    private Button citationButton;
    private Button materialButton; // 新增按钮

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        // 初始化按钮
        libraryButton = findViewById(R.id.libraryButton);
        translateButton = findViewById(R.id.translateButton);
        citationButton = findViewById(R.id.citationButton);
        materialButton = findViewById(R.id.materialButton); // 初始化 materialButton

        // 点击文献库按钮，跳转到 PdfActivity
        libraryButton.setOnClickListener(v -> {
            Intent intent = new Intent(MainActivity.this, PdfActivity.class);
            startActivity(intent);
        });

        // 点击翻译工具按钮，跳转到 TranslateActivity
        translateButton.setOnClickListener(v -> {
            Intent intent = new Intent(MainActivity.this, TranslateActivity.class);
            startActivity(intent);
        });

        // 点击论文引用工具按钮，跳转到 Google 学术网页
        citationButton.setOnClickListener(v -> {
            Intent intent = new Intent(Intent.ACTION_VIEW);
            intent.setData(Uri.parse("https://scholar.google.com"));
            startActivity(intent);
        });

        // 点击论文素材网站按钮，跳转到 MaterialActivity
        materialButton.setOnClickListener(v -> {
            Intent intent = new Intent(MainActivity.this, MaterialActivity.class);
            startActivity(intent);
        });
    }
}
