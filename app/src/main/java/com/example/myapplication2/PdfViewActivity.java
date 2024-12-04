package com.example.myapplication2;

import android.content.SharedPreferences;
import android.graphics.Bitmap;
import android.graphics.pdf.PdfRenderer;
import android.net.Uri;
import android.os.Bundle;
import android.os.ParcelFileDescriptor;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;

import java.io.IOException;

public class PdfViewActivity extends AppCompatActivity {

    private ImageView imageViewPdf;
    private PdfRenderer pdfRenderer;
    private PdfRenderer.Page currentPage;
    private int currentPageIndex = 0;

    private EditText tagInput; // 标签输入框
    private Button btnAddTag;  // 添加标签按钮
    private SharedPreferences sharedPreferences; // 用于存储标签

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_pdf_view);

        imageViewPdf = findViewById(R.id.imageViewPdf);
        Button btnPrevious = findViewById(R.id.btnPrevious);  // 上一页按钮
        Button btnNext = findViewById(R.id.btnNext);  // 下一页按钮

        // 标签功能
        tagInput = findViewById(R.id.tagInput);  // 标签输入框
        btnAddTag = findViewById(R.id.btnAddTag);  // 添加标签按钮

        sharedPreferences = getSharedPreferences("PdfTags", MODE_PRIVATE);

        // 获取 PDF Uri
        String pdfUriString = getIntent().getStringExtra("pdfUri");
        if (pdfUriString != null) {
            Uri pdfUri = Uri.parse(pdfUriString);  // 转换为 Uri 对象
            loadPdf(pdfUri);  // 加载 PDF 文件
        }

        // 上一页按钮点击事件
        btnPrevious.setOnClickListener(v -> {
            if (currentPageIndex > 0) {
                currentPageIndex--;
                showPage(currentPageIndex);  // 显示上一页
            } else {
                Toast.makeText(this, "已经是第一页", Toast.LENGTH_SHORT).show();
            }
        });

        // 下一页按钮点击事件
        btnNext.setOnClickListener(v -> {
            if (currentPageIndex < pdfRenderer.getPageCount() - 1) {
                currentPageIndex++;
                showPage(currentPageIndex);  // 显示下一页
            } else {
                Toast.makeText(this, "已经是最后一页", Toast.LENGTH_SHORT).show();
            }
        });

        // 添加标签按钮点击事件
        btnAddTag.setOnClickListener(v -> {
            String newTag = tagInput.getText().toString().trim();
            if (!newTag.isEmpty()) {
                saveTag(newTag);  // 保存标签
                tagInput.setText("");  // 清空输入框
                Toast.makeText(this, "标签已保存", Toast.LENGTH_SHORT).show();
            } else {
                Toast.makeText(this, "标签不能为空", Toast.LENGTH_SHORT).show();
            }
        });
    }

    // 加载 PDF 文件
    public void loadPdf(Uri pdfUri) {
        try {
            // 打开 PDF 文件
            ParcelFileDescriptor fileDescriptor = getContentResolver().openFileDescriptor(pdfUri, "r");
            if (fileDescriptor == null) {
                Toast.makeText(this, "无法打开文件", Toast.LENGTH_SHORT).show();
                return;
            }

            pdfRenderer = new PdfRenderer(fileDescriptor);

            // 渲染第一页
            showPage(currentPageIndex);

        } catch (IOException e) {
            e.printStackTrace();
            Toast.makeText(this, "无法打开 PDF 文件", Toast.LENGTH_SHORT).show();
        }
    }

    // 渲染 PDF 页面
    private void showPage(int pageIndex) {
        if (pdfRenderer.getPageCount() <= 0) {
            Toast.makeText(this, "没有可显示的页面", Toast.LENGTH_SHORT).show();
            return;
        }

        if (pageIndex < 0 || pageIndex >= pdfRenderer.getPageCount()) {
            return;
        }

        if (currentPage != null) {
            currentPage.close();
        }

        currentPage = pdfRenderer.openPage(pageIndex);
        Bitmap bitmap = Bitmap.createBitmap(currentPage.getWidth(), currentPage.getHeight(), Bitmap.Config.ARGB_8888);

        // 渲染 PDF 页面
        currentPage.render(bitmap, null, null, PdfRenderer.Page.RENDER_MODE_FOR_DISPLAY);

        imageViewPdf.setImageBitmap(bitmap);  // 设置为 ImageView 的内容
    }

    // 保存标签（每次添加会替换该文档标签）
    private void saveTag(String tag) {
        // 获取当前 PDF 文件的 URI
        String pdfUriString = getIntent().getStringExtra("pdfUri");
        if (pdfUriString == null) {
            return;
        }
        String pdfFileName = Uri.parse(pdfUriString).getLastPathSegment();  // 获取文件名作为 key

        // 将标签保存到 SharedPreferences，使用文件名作为 key
        sharedPreferences.edit().putString(pdfFileName, tag).apply();
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        if (currentPage != null) {
            currentPage.close();
        }
        if (pdfRenderer != null) {
            pdfRenderer.close();
        }
    }
}
