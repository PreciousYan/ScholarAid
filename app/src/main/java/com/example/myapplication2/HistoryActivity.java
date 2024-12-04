package com.example.myapplication2;

import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.util.Log;
import android.view.GestureDetector;
import android.view.MotionEvent;
import android.widget.ArrayAdapter;
import android.widget.EditText;
import android.widget.ListView;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Map;

public class HistoryActivity extends AppCompatActivity {

        private ListView historyListView;
        private SharedPreferences sharedPreferences;
        private EditText searchEditText;  // 搜索框
        private static final String TAG = "HistoryActivity";  // 用于调试的 TAG
        private List<String> fileNameList = new ArrayList<>();
        private List<String> fileUriList = new ArrayList<>();

        @Override
        protected void onCreate(Bundle savedInstanceState) {
                super.onCreate(savedInstanceState);
                setContentView(R.layout.activity_history);

                historyListView = findViewById(R.id.historyListView);
                searchEditText = findViewById(R.id.searchEditText);  // 获取搜索框
                sharedPreferences = getSharedPreferences("PdfHistory", MODE_PRIVATE);

                // 获取保存的历史文件名和 URI 列表
                String historyNames = sharedPreferences.getString("historyPdfNames", "");
                String historyUris = sharedPreferences.getString("historyPdfUris", "");

                Log.d(TAG, "onCreate: Retrieved historyNames = " + historyNames);
                Log.d(TAG, "onCreate: Retrieved historyUris = " + historyUris);

                if (!historyNames.isEmpty() && !historyUris.isEmpty()) {
                        // 将历史文件名按行显示
                        String[] fileNames = historyNames.split(",");
                        String[] fileUris = historyUris.split(",");

                        Log.d(TAG, "onCreate: File names = " + fileNames.length + " items, File URIs = " + fileUris.length + " items");

                        fileNameList = new ArrayList<>(Arrays.asList(fileNames));
                        fileUriList = new ArrayList<>(Arrays.asList(fileUris));

                        // 打印文件名和 URI
                        for (int i = 0; i < fileNames.length; i++) {
                                Log.d(TAG, "File " + i + ": " + fileNames[i] + " - " + fileUris[i]);
                        }

                        // 创建带标签的文件名列表
                        List<String> labeledFileNames = new ArrayList<>();
                        for (int i = 0; i < fileNameList.size(); i++) {
                                String label = sharedPreferences.getString(fileUriList.get(i), "");
                                labeledFileNames.add(fileNameList.get(i) + "   " + label);  // 添加标签
                        }

                        // 设置 Adapter
                        ArrayAdapter<String> adapter = new ArrayAdapter<>(this, android.R.layout.simple_list_item_1, labeledFileNames);
                        historyListView.setAdapter(adapter);

                        // 创建 GestureDetector 来检测双击事件
                        GestureDetector gestureDetector = new GestureDetector(this, new GestureDetector.SimpleOnGestureListener() {
                                @Override
                                public boolean onDoubleTap(MotionEvent e) {
                                        // 获取双击的文件名位置
                                        int position = historyListView.pointToPosition((int) e.getX(), (int) e.getY());

                                        Log.d(TAG, "onDoubleTap: position = " + position);

                                        if (position != ListView.INVALID_POSITION) {
                                                // 获取点击的文件名
                                                String clickedFileName = (String) historyListView.getItemAtPosition(position);

                                                Log.d(TAG, "onDoubleTap: clickedFileName = " + clickedFileName);

                                                // 打印 SharedPreferences 中所有的键值对
                                                Map<String, ?> allEntries = sharedPreferences.getAll();
                                                for (Map.Entry<String, ?> entry : allEntries.entrySet()) {
                                                        Log.d(TAG, "SharedPreferences - Key: " + entry.getKey() + ", Value: " + entry.getValue().toString());
                                                }

                                                // 遍历文件名和 URI 列表，找到对应的 URI
                                                for (int i = 0; i < fileNameList.size(); i++) {
                                                        // 移除标签前缀并进行匹配
                                                        if (fileNameList.get(i).equals(clickedFileName.replaceAll("^[^:]+: ", ""))) {
                                                                String selectedUri = fileUriList.get(i);
                                                                Log.d(TAG, "Found URI for clicked file: " + selectedUri);
                                                                openHistoryFile(selectedUri);
                                                                return true;  // 表示双击事件已经处理
                                                        }
                                                }
                                                Log.w(TAG, "No matching URI found for file: " + clickedFileName);
                                        }
                                        return super.onDoubleTap(e);  // 返回给父类，避免重复触发
                                }
                        });

                        // 在 ListView 中处理触摸事件
                        historyListView.setOnTouchListener((v, event) -> gestureDetector.onTouchEvent(event));

                        // 设置搜索框监听
                        searchEditText.addTextChangedListener(new android.text.TextWatcher() {
                                @Override
                                public void beforeTextChanged(CharSequence charSequence, int start, int count, int after) {}

                                @Override
                                public void onTextChanged(CharSequence charSequence, int start, int before, int count) {
                                        String query = charSequence.toString().toLowerCase();
                                        // 筛选符合条件的文件名
                                        List<String> filteredFileNames = new ArrayList<>();
                                        List<String> filteredFileUris = new ArrayList<>();

                                        for (int i = 0; i < fileNameList.size(); i++) {
                                                if (fileNameList.get(i).toLowerCase().contains(query) ||
                                                        sharedPreferences.getString(fileUriList.get(i), "").toLowerCase().contains(query)) {
                                                        filteredFileNames.add(fileNameList.get(i));
                                                        filteredFileUris.add(fileUriList.get(i));
                                                }
                                        }

                                        // 更新 ListView 数据
                                        List<String> filteredLabeledFileNames = new ArrayList<>();
                                        for (int i = 0; i < filteredFileNames.size(); i++) {
                                                String label = sharedPreferences.getString(filteredFileUris.get(i), "默认标签");
                                                filteredLabeledFileNames.add(label + ": " + filteredFileNames.get(i));
                                        }

                                        ArrayAdapter<String> newAdapter = new ArrayAdapter<>(HistoryActivity.this, android.R.layout.simple_list_item_1, filteredLabeledFileNames);
                                        historyListView.setAdapter(newAdapter);

                                        // 更新数据源，确保双击事件使用正确的文件 URI
                                        updateFileUriList(filteredFileNames, filteredFileUris);
                                }

                                @Override
                                public void afterTextChanged(android.text.Editable editable) {}
                        });

                } else {
                        Toast.makeText(this, "No history found.", Toast.LENGTH_SHORT).show();
                }
        }

        // 打开历史文件
        private void openHistoryFile(String pdfUri) {
                if (pdfUri != null && !pdfUri.isEmpty()) {
                        Log.d(TAG, "openHistoryFile: Opening file with URI = " + pdfUri);
                        // 直接启动 PdfViewActivity，传递 pdfUri
                        Intent intent = new Intent(this, PdfViewActivity.class);
                        intent.putExtra("pdfUri", pdfUri);  // 将 URI 传递给 PdfViewActivity
                        startActivity(intent);
                } else {
                        Log.e(TAG, "openHistoryFile: Invalid file URI");
                        Toast.makeText(this, "Invalid file URI", Toast.LENGTH_SHORT).show();
                }
        }

        // 更新文件 URI 列表
        private void updateFileUriList(List<String> filteredFileNames, List<String> filteredFileUris) {
                // 这里你可以更新数据源或其他方式处理文件 URI。
        }
}
