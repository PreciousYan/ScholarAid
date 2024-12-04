package com.example.myapplication2;

import android.os.Bundle;
import android.view.View;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Spinner;
import android.widget.TextView;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;

import com.google.gson.JsonObject;
import com.google.gson.JsonParser;

import java.io.IOException;
import java.util.Random;

import okhttp3.Call;
import okhttp3.Callback;
import okhttp3.OkHttpClient;
import okhttp3.Request;
import okhttp3.Response;

public class TranslateActivity extends AppCompatActivity {

    private static final String APP_ID = "20241119002205951";
    private static final String SECRET_KEY = "3_NPf7MguGuw5qutGdnt";
    private static final String BASE_URL = "https://api.fanyi.baidu.com/api/trans/vip/translate";

    private EditText inputEditText;
    private Spinner fromLanguageSpinner;
    private Spinner toLanguageSpinner;
    private Button translateButton;
    private TextView outputTextView;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_translate);

        inputEditText = findViewById(R.id.inputEditText);
        fromLanguageSpinner = findViewById(R.id.fromLanguageSpinner);
        toLanguageSpinner = findViewById(R.id.toLanguageSpinner);
        translateButton = findViewById(R.id.translateButton);
        outputTextView = findViewById(R.id.outputTextView);

        // 初始化语言选项
        String[] languages = {"中文", "英文"};
        String[] languageCodes = {"zh", "en"};
        ArrayAdapter<String> adapter = new ArrayAdapter<>(this, android.R.layout.simple_spinner_item, languages);
        adapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
        fromLanguageSpinner.setAdapter(adapter);
        toLanguageSpinner.setAdapter(adapter);

        translateButton.setOnClickListener(v -> {
            String textToTranslate = inputEditText.getText().toString();
            if (!textToTranslate.isEmpty()) {
                int fromIndex = fromLanguageSpinner.getSelectedItemPosition();
                int toIndex = toLanguageSpinner.getSelectedItemPosition();

                // 检查源语言和目标语言是否相同
                if (fromIndex == toIndex) {
                    Toast.makeText(this, "源语言和目标语言不能相同", Toast.LENGTH_SHORT).show();
                    return;
                }

                translateText(textToTranslate, languageCodes[fromIndex], languageCodes[toIndex]);
            } else {
                Toast.makeText(this, "请输入要翻译的文本", Toast.LENGTH_SHORT).show();
            }
        });
    }

    private void translateText(String text, String fromLang, String toLang) {
        String salt = String.valueOf(new Random().nextInt(10000));
        String sign = generateSign(text, salt);

        String url = BASE_URL +
                "?q=" + text +
                "&from=" + fromLang +
                "&to=" + toLang +
                "&appid=" + APP_ID +
                "&salt=" + salt +
                "&sign=" + sign;

        OkHttpClient client = new OkHttpClient();
        Request request = new Request.Builder()
                .url(url)
                .build();

        client.newCall(request).enqueue(new Callback() {
            @Override
            public void onFailure(Call call, IOException e) {
                runOnUiThread(() -> Toast.makeText(TranslateActivity.this, "翻译失败，请检查网络连接", Toast.LENGTH_SHORT).show());
            }

            @Override
            public void onResponse(Call call, Response response) throws IOException {
                if (response.isSuccessful() && response.body() != null) {
                    String responseBody = response.body().string();
                    runOnUiThread(() -> parseAndDisplayTranslation(responseBody));
                } else {
                    runOnUiThread(() -> Toast.makeText(TranslateActivity.this, "翻译失败，服务器错误", Toast.LENGTH_SHORT).show());
                }
            }
        });
    }

    private String generateSign(String text, String salt) {
        String stringToSign = APP_ID + text + salt + SECRET_KEY;
        return MD5(stringToSign);
    }

    private String MD5(String stringToHash) {
        try {
            java.security.MessageDigest md = java.security.MessageDigest.getInstance("MD5");
            byte[] array = md.digest(stringToHash.getBytes());
            StringBuilder sb = new StringBuilder();
            for (byte b : array) {
                sb.append(String.format("%02x", b));
            }
            return sb.toString();
        } catch (Exception e) {
            e.printStackTrace();
            return "";
        }
    }

    private void parseAndDisplayTranslation(String json) {
        try {
            JsonObject jsonObject = JsonParser.parseString(json).getAsJsonObject();
            String translationResult = jsonObject
                    .getAsJsonArray("trans_result")
                    .get(0)
                    .getAsJsonObject()
                    .get("dst")
                    .getAsString();

            outputTextView.setText(translationResult);
        } catch (Exception e) {
            e.printStackTrace();
            Toast.makeText(this, "翻译结果解析失败", Toast.LENGTH_SHORT).show();
        }
    }
}
