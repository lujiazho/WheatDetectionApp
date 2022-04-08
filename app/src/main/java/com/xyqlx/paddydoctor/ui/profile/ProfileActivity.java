package com.xyqlx.paddydoctor.ui.profile;

import androidx.appcompat.app.ActionBar;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.app.ActivityCompat;

import android.Manifest;
import android.content.pm.PackageManager;
import android.graphics.BitmapFactory;
import android.location.Address;
import android.location.Geocoder;
import android.location.Location;
import android.location.LocationManager;
import android.os.Bundle;
import android.text.Editable;
import android.text.TextWatcher;
import android.util.Log;
import android.view.MenuItem;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Toast;

import com.google.gson.Gson;
import com.google.gson.JsonObject;
import com.google.gson.JsonParser;
import com.xyqlx.paddydoctor.FlaskServerService;
import com.xyqlx.paddydoctor.R;
import com.xyqlx.paddydoctor.data.model.LoggedInUser;
import com.xyqlx.paddydoctor.data.model.LoginUser;
import com.xyqlx.paddydoctor.ui.ActionBarUtils;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.util.List;
import java.util.Locale;

import okhttp3.RequestBody;
import okhttp3.ResponseBody;
import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;
import retrofit2.Retrofit;

public class ProfileActivity extends AppCompatActivity {
    private EditText usernameEditText;
    private EditText passwordEditText;
    private EditText locationEditText;
    private Button modifyButton;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_profile);
        // 添加返回键
        ActionBar actionBar = getSupportActionBar();
        if (actionBar != null) {
            actionBar.setHomeButtonEnabled(true);
            actionBar.setDisplayHomeAsUpEnabled(true);
        }
        // 初始化控件
        usernameEditText = findViewById(R.id.usernameEditText);
        passwordEditText = findViewById(R.id.passwordEditText);
        locationEditText = findViewById(R.id.locationEditText);
        modifyButton = findViewById(R.id.modifyProfileButton);
        // 载入用户信息
        LoggedInUser user = LoggedInUser.getLoggedInUser(this);
        usernameEditText.setText(user.getDisplayName());
        locationEditText.setText(user.getLocation());
        if(user.getLocation().equals("unknown")){
            locationEditText.setText("");
        }
        // 监听修改
        usernameEditText.addTextChangedListener(new ProfileTextWatcher());
        locationEditText.addTextChangedListener(new ProfileTextWatcher());
        // 修改按钮
        modifyButton.setOnClickListener(v -> {
            modifyProfile(user.getUserToken());
        });
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

    public void onPositionButtonClick(View view) {
        LocationManager locationManager = (LocationManager) getApplicationContext().getSystemService(LOCATION_SERVICE);
        if (ActivityCompat.checkSelfPermission(this, Manifest.permission.ACCESS_FINE_LOCATION) != PackageManager.PERMISSION_GRANTED && ActivityCompat.checkSelfPermission(this, Manifest.permission.ACCESS_COARSE_LOCATION) != PackageManager.PERMISSION_GRANTED) {
            ActivityCompat.requestPermissions(this, new String[]{
                    Manifest.permission.ACCESS_FINE_LOCATION,
                    Manifest.permission.ACCESS_COARSE_LOCATION
            },1);
            return;
        }
        // TODO 允许权限后需要再次点击按钮才能定位
        Location location = locationManager.getLastKnownLocation(LocationManager.NETWORK_PROVIDER);
        if(location == null){
            Toast.makeText(this, "请打开定位~", Toast.LENGTH_SHORT).show();
            return;
        }
        Geocoder geocoder = new Geocoder(this, Locale.CHINA);
        try {
            List<Address> result = geocoder.getFromLocation(location.getLatitude(), location.getLongitude(), 1);
            String province = result.get(0).getAdminArea();
            String city = result.get(0).getLocality();
            locationEditText.setText(city);
            // Toast.makeText(this, result.get(0).getCountryName(), Toast.LENGTH_SHORT).show();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    private class ProfileTextWatcher implements TextWatcher{

        @Override
        public void beforeTextChanged(CharSequence s, int start, int count, int after) {

        }

        @Override
        public void onTextChanged(CharSequence s, int start, int before, int count) {

        }

        @Override
        public void afterTextChanged(Editable s) {
            modifyButton.setVisibility(View.VISIBLE);
        }
    }

    private void modifyProfile(String token){
        JsonObject obj = new JsonObject();
        String username = usernameEditText.getText().toString();
        String password = passwordEditText.getText().toString();
        String location = locationEditText.getText().toString();
        obj.addProperty("username", username);
        obj.addProperty("password", password);
        obj.addProperty("location", location);
        Retrofit retrofit=new Retrofit.Builder().baseUrl(FlaskServerService.HttpConfig.BASE_URL).build();
        RequestBody body=RequestBody.create(okhttp3.MediaType.parse("application/json; charset=utf-8"),obj.toString());
        FlaskServerService serverService = retrofit.create(FlaskServerService.class);
        retrofit2.Call<ResponseBody> data = serverService.updateProfile(body, token);
        data.enqueue(new Callback<ResponseBody>() {
            @Override
            public void onResponse(Call<ResponseBody> call, Response<ResponseBody> response) {
                //数据请求成功
                // TODO 数据合法性校验
                Toast.makeText(ProfileActivity.this, "修改成功", Toast.LENGTH_SHORT).show();
                // TODO 修改成功后，根据服务端通过情况，更新本地用户
                LoggedInUser user = LoggedInUser.getLoggedInUser(ProfileActivity.this);
                if(!username.isEmpty()){user.setDisplayName(username);}
                if(!location.isEmpty()){user.setLocation(location);}
                LoggedInUser.setLoggedInUser(ProfileActivity.this, user);
                // TODO 更新本地存储的用户名和密码
            }

            @Override
            public void onFailure(Call<ResponseBody> call, Throwable t) {
                //数据请求失败
                Log.d("", "failure");
            }
        });
    }
}