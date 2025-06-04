package com.example.rmb02;

import android.app.ListActivity;
import android.os.Bundle;
import android.os.Handler;
import android.os.Looper;
import android.os.Message;
import android.util.Log;
import android.view.View;
import android.widget.ArrayAdapter;
import android.widget.ListAdapter;
import androidx.activity.EdgeToEdge;
import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.graphics.Insets;
import androidx.core.view.ViewCompat;
import androidx.core.view.WindowInsetsCompat;
import android.app.ListActivity;
import android.content.Intent;
import android.os.Bundle;
import android.os.Handler;
import android.os.Looper;
import android.os.Message;
import android.util.Log;
import android.widget.ArrayAdapter;
import android.widget.ListAdapter;
import android.widget.Toast;
import androidx.activity.EdgeToEdge;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.graphics.Insets;
import androidx.core.view.ViewCompat;
import androidx.core.view.WindowInsetsCompat;
import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;
import org.jsoup.nodes.Element;
import org.jsoup.select.Elements;
import java.io.IOException;
import java.net.MalformedURLException;
import java.net.URL;
import java.util.ArrayList;
import java.util.List;
import java.util.ArrayList;
import java.util.List;

public class RateListActivity extends ListActivity {
    Handler handler;
    private static final String TAG = "RateListActivity";
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        handler = new Handler(Looper.getMainLooper()) {
            @Override
            public void handleMessage(@NonNull Message msg) {
                //处理接收的消息
                List<String> getlist = (List<String>) msg.obj;
                if (msg.what == 5) {
                    Log.i(TAG, "成功获取汇率数据，数量: " + getlist.size());
                    //适配器
                    ListAdapter adapter = new ArrayAdapter<String>(RateListActivity.this, android.R.layout.simple_list_item_1,getlist);
                    //绑定
                    setListAdapter(adapter);
                }
                else if (msg.what == 0) {
                    Log.e(TAG, "网络请求失败");
                    Toast.makeText(RateListActivity.this, "网络请求失败", Toast.LENGTH_SHORT).show();
                }
            }
        };
        new Thread(new Runnable() {
            @Override
            public void run() {
                Log.d(TAG, "请求线程");
                List<String> rateList = new ArrayList<>();
                try {
                    Log.d(TAG, "尝试连接网站");
                    Document doc = Jsoup.connect("https://www.huilvbiao.com/bank/spdb").get();
                    Log.d(TAG, "获取HTML文档");

                    Element table = doc.select("table").first();
                    Elements rows = table.select("tr");
                    Log.d(TAG, "表格行数: " + rows.size());

                    for (Element row : rows) {
                        Element coinSpan = row.select("span").first();
                        String currencyName = coinSpan != null ? coinSpan.text() : "未知币种";

                        Elements tds = row.select("td");
                        if (tds.size() < 4) continue;

                        String data1 = tds.get(0).text();
                        String data2 = tds.get(1).text();
                        String data3 = tds.get(2).text();
                        String data4 = tds.get(3).text();
                        Log.i(TAG, String.format(
                                "%s  现汇买入:%s 现汇卖出:%s 现钞买入:%s 现钞卖出:%s",
                                currencyName, data1, data2, data3, data4
                        ));
                        float f = Float.parseFloat(data1);
                        rateList.add(currencyName + " ==> 汇率" + f / 100f + "|");
                    }

                    Message msg = handler.obtainMessage(5, rateList);
                    handler.sendMessage(msg);

                } catch (IOException e) {
                    Log.e(TAG, "网络请求异常", e);
                    handler.sendEmptyMessage(0);
                }
            }
        }).start();

    }
}