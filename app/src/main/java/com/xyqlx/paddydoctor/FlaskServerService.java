package com.xyqlx.paddydoctor;

import okhttp3.MultipartBody;
import okhttp3.RequestBody;
import okhttp3.ResponseBody;
import retrofit2.Call;
import retrofit2.http.Body;
import retrofit2.http.GET;
import retrofit2.http.Multipart;
import retrofit2.http.POST;
import retrofit2.http.Part;
import retrofit2.http.Path;
import retrofit2.http.Query;

// https://blog.csdn.net/m0_37796683/article/details/90702095
public interface FlaskServerService {
    @GET("classifier/test")
    Call<ResponseBody> getTest();

    @Multipart // 表示请求发送编码表单数据，使用于有文件上传的场景，每个键值对需要用@Part来注解键名
    @POST("classifier/classify/{modelId}")
    Call<ResponseBody> classify(@Path(value = "modelId", encoded = true) String modelId, @Query("token") String token, @Part MultipartBody.Part filePart);

    @POST("user/login")
    Call<ResponseBody> login(@Body RequestBody body);
    // @Body多用于POST发送非表单数据，不能用于表单或者支持文件上传的表单
    // 即不能与@FormUrlEncoded和@Multipart注解同时使用

    @POST("classifier/feedback") // 或许token的query可以换成Header
    Call<ResponseBody> feedback(@Query("token") String token, @Body RequestBody body);

    @POST("user/history")
    Call<ResponseBody> history(@Query("token") String token);

    @POST("user/image/{fileId}") // @Path请求参数注解，用于Url中的占位符{}，所有在网址中的参数
    Call<ResponseBody> getImage(@Path(value = "fileId", encoded = true) String fileId, @Query("token") String token);

    @POST("user/profile")
    Call<ResponseBody> updateProfile(@Body RequestBody body, @Query("token") String token);

    @POST("user/trending")
    Call<ResponseBody> getTrending(@Query("token") String token);

    @GET("classifier/knowledge")
    Call<ResponseBody> getKnowledge();

    class HttpConfig {

//        public static final String BASE_URL = "http://139.217.228.30:13579/";
//        public static final String BASE_URL = "http://152.136.128.216:8000/";
        public static final String BASE_URL = "http://127.0.0.1:8000/";

    }
}
