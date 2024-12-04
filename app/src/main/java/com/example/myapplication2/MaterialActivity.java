package com.example.myapplication2;

import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;
import android.widget.Button;
import androidx.appcompat.app.AppCompatActivity;

public class MaterialActivity extends AppCompatActivity {

    private Button mediumButton;
    private Button arxivButton;
    private Button connectedPapersButton;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_material);

        // 初始化按钮
        mediumButton = findViewById(R.id.mediumButton);
        arxivButton = findViewById(R.id.arxivButton);
        connectedPapersButton = findViewById(R.id.connectedPapersButton);

        // Medium 按钮点击事件
        mediumButton.setOnClickListener(v -> {
            Intent intent = new Intent(Intent.ACTION_VIEW, Uri.parse("https://medium.com"));
            startActivity(intent);
        });

        // arXiv 按钮点击事件
        arxivButton.setOnClickListener(v -> {
            Intent intent = new Intent(Intent.ACTION_VIEW, Uri.parse("https://arxiv.org"));
            startActivity(intent);
        });

        // Connected Papers 按钮点击事件
        connectedPapersButton.setOnClickListener(v -> {
            Intent intent = new Intent(Intent.ACTION_VIEW, Uri.parse("https://www.connectedpapers.com"));
            startActivity(intent);
        });
    }
}
