package com.xyqlx.paddydoctor.data.model;

import android.content.Context;
import android.widget.EditText;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.util.Properties;

/**
 * Data class that captures user information for logged in users retrieved from LoginRepository
 */
public class LoggedInUser {

    private String userId;
    private String displayName;
    // 下面是添加的两个
    private String token;
    private String location;

    public LoggedInUser(String userId, String displayName, String token, String location) {
        this.userId = userId;
        this.displayName = displayName;
        this.token = token;
        this.location = location;
    }

    public String getUserId() {
        return userId;
    }

    public void setDisplayName(String displayName) {
        this.displayName = displayName;
    }

    public String getToken() {
        return token;
    }

    public void setToken(String token) {
        this.token = token;
    }

    public String getDisplayName() {
        return displayName;
    }

    public String getUserToken() {
        return token;
    }

    // 得到loggedInUser，相当于创建实例
    public static LoggedInUser getLoggedInUser(Context context){
        try {
            // 创建File对象
            File file = new File(context.getFilesDir(), "user.properties");
            // 创建FileIutputStream 对象
            FileInputStream fis = new FileInputStream(file);
            // 创建属性对象
            Properties pro = new Properties();
            // 加载文件
            pro.load(fis);
            // 关闭输入流对象
            fis.close();
            // 读取属性
            String userId = pro.getProperty("userId");
            String displayName = pro.getProperty("displayName");
            String token = pro.getProperty("token");
            String location = pro.getProperty("location");
            // 检查是否存在
            if(userId != null){
                return new LoggedInUser(userId, displayName, token, location);
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
        return null;
    }

    // 本地保存缓存的用户信息
    public static void setLoggedInUser(Context context, LoggedInUser user){
        try {
            // 使用Android上下问获取当前项目的路径
            File file = new File(context.getFilesDir(), "user.properties");
            // 创建输出流对象
            FileOutputStream fos = new FileOutputStream(file);
            // 创建属性文件对象
            Properties pro = new Properties();
            // 设置信息
            pro.setProperty("userId", user.userId);
            pro.setProperty("displayName", user.displayName);
            pro.setProperty("token", user.token);
            pro.setProperty("location", user.location);
            // 保存文件
            pro.store(fos, "user.properties");
            // 关闭输出流对象
            fos.close();
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    public String getLocation() {
        return location;
    }

    public void setLocation(String location) {
        this.location = location;
    }
}