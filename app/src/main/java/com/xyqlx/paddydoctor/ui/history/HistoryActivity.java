package com.xyqlx.paddydoctor.ui.history;

import androidx.appcompat.app.ActionBar;
import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import android.os.Bundle;
import android.util.Log;
import android.view.MenuItem;
import android.view.View;

import com.google.gson.JsonArray;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import com.google.gson.JsonParser;
import com.xyqlx.paddydoctor.FlaskServerService;
import com.xyqlx.paddydoctor.R;
import com.xyqlx.paddydoctor.data.model.HistoryItem;
import com.xyqlx.paddydoctor.data.model.LoggedInUser;
import com.xyqlx.paddydoctor.ui.ActionBarUtils;

import java.io.IOException;
import java.util.ArrayList;

import okhttp3.MediaType;
import okhttp3.MultipartBody;
import okhttp3.RequestBody;
import okhttp3.ResponseBody;
import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;
import retrofit2.Retrofit;

public class HistoryActivity extends AppCompatActivity {

    // 初始化容器
    private RecyclerView recyclerView;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_history);
        // 添加返回键
        ActionBar actionBar = getSupportActionBar();
        if(actionBar != null){
            actionBar.setHomeButtonEnabled(true);
            actionBar.setDisplayHomeAsUpEnabled(true);
        }
        // 初始化容器
        recyclerView = findViewById(R.id.historyRecyclerView);
        //线性布局
        LinearLayoutManager linearLayoutManager = new LinearLayoutManager(this);
        linearLayoutManager.setOrientation(LinearLayoutManager.VERTICAL);
        recyclerView.setLayoutManager(linearLayoutManager);
        // 加载数据
        loadHistory();
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        switch (item.getItemId()) {
            case android.R.id.home:
                this.finish(); // back button
                return true;
        }
        return super.onOptionsItemSelected(item);
    }

    private void loadHistory(){
        // 请求
        Retrofit retrofit = new Retrofit.Builder().baseUrl(FlaskServerService.HttpConfig.BASE_URL).build();
        FlaskServerService serverService =retrofit.create(FlaskServerService.class);
        String token = LoggedInUser.getLoggedInUser(this).getUserToken();
        Call<ResponseBody> call = serverService.history(token);
        call.enqueue(new Callback<ResponseBody>() {
            @Override
            public void onResponse(Call<ResponseBody> call, Response<ResponseBody> response) {
                //数据请求成功
                String body = "{}";
                try {
                    body = response.body().string();
                } catch (IOException e) {
                    Log.d("", "parse failed");
                    e.printStackTrace();
                }
                JsonObject obj = JsonParser.parseString(body).getAsJsonObject();
                // TODO 异常处理
                JsonArray results = obj.getAsJsonArray("result");
                ArrayList<HistoryItem> historyItems = new ArrayList<>();
                for (JsonElement result :
                        results) {
                    JsonObject jsonObject = result.getAsJsonObject();
                    HistoryItem item = new HistoryItem(jsonObject.get("fileid").getAsString(), jsonObject.get("result").getAsString(), jsonObject.get("confidence").getAsByte());
                    historyItems.add(item);
                }
                // String type = obj.get("type").getAsString();
                // String text = obj.get("text").getAsString();
                // Toast.makeText(getContext(), info, Toast.LENGTH_SHORT).show();
                HistoryRecyclerAdapter adapter = new HistoryRecyclerAdapter(HistoryActivity.this, historyItems);
                recyclerView.setAdapter(adapter);
            }

            @Override
            public void onFailure(Call<ResponseBody> call, Throwable t) {
                //数据请求失败
                Log.d("", "failure");
            }
        });


    }
}