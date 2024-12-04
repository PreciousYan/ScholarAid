package com.example.myapplication2;

import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.database.Cursor;
import android.net.Uri;
import android.os.Bundle;
import android.provider.MediaStore;
import android.widget.Button;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;

public class PdfActivity extends AppCompatActivity {

    private static final int PICK_PDF_FILE = 2;  // 文件选择器请求码
    private Button choosePdfButton, viewHistoryButton;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_pdf);

        choosePdfButton = findViewById(R.id.choosePdfButton);
        viewHistoryButton = findViewById(R.id.viewHistoryButton);

        // 选择 PDF 文件按钮
        choosePdfButton.setOnClickListener(v -> openFilePicker());

        // 查看历史文件按钮
        viewHistoryButton.setOnClickListener(v -> openHistoryActivity());
    }

    // 打开 PDF 文件选择器
    private void openFilePicker() {
        Intent intent = new Intent(Intent.ACTION_OPEN_DOCUMENT);
        intent.addCategory(Intent.CATEGORY_OPENABLE);
        intent.setType("application/pdf");
        startActivityForResult(intent, PICK_PDF_FILE);
    }

    // 打开历史文件列表界面
    private void openHistoryActivity() {
        Intent intent = new Intent(this, HistoryActivity.class);
        startActivity(intent);
    }

    // 处理文件选择返回的结果
    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        if (requestCode == PICK_PDF_FILE && resultCode == RESULT_OK && data != null) {
            Uri uri = data.getData();
            if (uri != null) {
                // 获取文件名
                String fileName = getFileName(uri);

                // 显示文件名
                Toast.makeText(this, "Selected PDF: " + fileName, Toast.LENGTH_SHORT).show();

                // 将选择的 PDF URI 和文件名一起保存到 SharedPreferences
                if (!isPdfAlreadySaved(fileName)) {
                    savePdfHistory(uri.toString(), fileName);
                } else {
                    Toast.makeText(this, "This file has already been saved to history", Toast.LENGTH_SHORT).show();
                }

                // 打开 PdfActivity 来显示 PDF 文件
                Intent intent = new Intent(PdfActivity.this, PdfViewActivity.class);
                intent.putExtra("pdfUri", uri.toString()); // 将 Uri 传递给 PdfViewActivity
                startActivity(intent);
            } else {
                Toast.makeText(this, "No file selected", Toast.LENGTH_SHORT).show();
            }
        }
    }

    // 获取文件名
    private String getFileName(Uri uri) {
        String fileName = null;
        String[] projection = { MediaStore.Files.FileColumns.DISPLAY_NAME };
        try (Cursor cursor = getContentResolver().query(uri, projection, null, null, null)) {
            if (cursor != null && cursor.moveToFirst()) {
                int columnIndex = cursor.getColumnIndexOrThrow(MediaStore.Files.FileColumns.DISPLAY_NAME);
                fileName = cursor.getString(columnIndex);
            }
        }
        return fileName;
    }

    // 保存 PDF 文件的 URI 和文件名到 SharedPreferences
    private void savePdfHistory(String pdfUri, String pdfName) {
        SharedPreferences sharedPreferences = getSharedPreferences("PdfHistory", MODE_PRIVATE);
        SharedPreferences.Editor editor = sharedPreferences.edit();

        // 获取已保存的历史文件名和 URI 列表
        String historyNames = sharedPreferences.getString("historyPdfNames", "");
        String historyUris = sharedPreferences.getString("historyPdfUris", "");

        // 添加新的文件名和 URI 到历史记录
        historyNames = historyNames + pdfName + ",";
        historyUris = historyUris + pdfUri + ",";

        // 保存更新后的历史记录
        editor.putString("historyPdfNames", historyNames);
        editor.putString("historyPdfUris", historyUris);
        editor.apply();
    }

    // 检查文件是否已经存在于历史记录中
    private boolean isPdfAlreadySaved(String pdfName) {
        SharedPreferences sharedPreferences = getSharedPreferences("PdfHistory", MODE_PRIVATE);
        String historyNames = sharedPreferences.getString("historyPdfNames", "");

        // 检查历史文件名中是否已经包含该文件名
        return historyNames.contains(pdfName + ",");
    }
}
