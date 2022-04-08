package com.xyqlx.paddydoctor.data;

import android.util.Log;

import com.google.gson.Gson;
import com.google.gson.JsonObject;
import com.google.gson.JsonParser;
import com.xyqlx.paddydoctor.FlaskServerService;
import com.xyqlx.paddydoctor.data.model.LoggedInUser;
import com.xyqlx.paddydoctor.data.model.LoginUser;

import java.io.IOException;

import okhttp3.RequestBody;
import okhttp3.ResponseBody;
import retrofit2.Callback;
import retrofit2.Response;
import retrofit2.Retrofit;

/**
 * Class that handles authentication w/ login credentials and retrieves user information.
 */
public class LoginDataSource {
    // 处理带有登录凭据的身份验证，并检索用户信息。简单说就是检索用户登陆信息，做出登陆动作，并返回结果

    public Result<LoggedInUser> login(String username, String password) {
        // 得到user信息
        String id = "me";
        String token = "123";
        String location = "beijing";
        // 实例化登录用户
        LoggedInUser loggedInUser =
                new LoggedInUser(id, username, token, location);
        return new Result.Success<>(loggedInUser);
//        try {
//            // 根据用户输入的账号密码创建个LoginUser，传给服务器验证
//            LoginUser user=new LoginUser(username,password);
//            // GSON弥补了JSON的许多不足的地方,在实际应用中更加适用于Java开发
//            Gson gson=new Gson();
//            // 把新建的user转化成json格式的字符串
//            String obj=gson.toJson(user);
//            // 构建Retrofit实例
//            Retrofit retrofit=new Retrofit.Builder()
//                    //设置网络请求BaseUrl地址
//                    .baseUrl(FlaskServerService.HttpConfig.BASE_URL)
//                    // 构建
//                    .build();
//            // json转化为RequestBody对象
//            RequestBody body=RequestBody.create(okhttp3.MediaType.parse("application/json; charset=utf-8"),obj);
//            // 创建网络请求接口的实例
//            FlaskServerService serverService = retrofit.create(FlaskServerService.class);
//            // 对发送请求进行封装，即把FlaskServerService里的函数封装成这个Call类型
//            retrofit2.Call<ResponseBody> data = serverService.login(body);
//            // execute是同步方法，enqueue的方法是异步方法
//            ResponseBody responseBody = data.execute().body();
//            // 在Retrofit.Builder处没设置使用addConverterFactory添加Gson解析，因此需要自己去解析
//            JsonObject document = JsonParser.parseString(responseBody.string()).getAsJsonObject();
//
//            // TODO 异常处理
//            // 处理服务器返回的东西
//            if(document.has("user")){
//                // 得到user信息
//                String id = document.get("user").getAsJsonObject().get("id").getAsString();
//                String token = document.get("user").getAsJsonObject().get("token").getAsString();
//                String location = document.get("user").getAsJsonObject().get("location").getAsString();
//                // 实例化登录用户
//                LoggedInUser loggedInUser =
//                        new LoggedInUser(id, username, token, location);
//                return new Result.Success<>(loggedInUser);
//            }
//            // 服务器返回的信息
//            if(document.has("message")){
//                return new Result.Error(new Exception(document.get("message").getAsString()));
//            }
//            return new Result.Error(new Exception("登录失败，可能是服务器错误"));
////            data.enqueue(new Callback<ResponseBody>() {
////                @Override
////                public void onResponse(retrofit2.Call<ResponseBody> call, Response<ResponseBody> response) {
////                    Log.d("", "onResponse: --ok--"+response.body());
////                    try {
////                        Log.d("", "onResponse: --ok--"+response.body().string());
////                    } catch (IOException e) {
////                        e.printStackTrace();
////                    }
////                }
////                @Override
////                public void onFailure(retrofit2.Call<ResponseBody> call, Throwable t) {
////                    Log.d("", "onResponse: --err--"+t.toString());
////                } });
//        } catch (Exception e) {
//            // 返回失败信息
//            return new Result.Error(new IOException("登录失败", e));
//        }
    }

    public void logout() {
        // TODO: revoke authentication
    }


}