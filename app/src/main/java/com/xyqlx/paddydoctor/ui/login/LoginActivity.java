package com.xyqlx.paddydoctor.ui.login;

import android.app.Activity;

import androidx.lifecycle.Observer;
import androidx.lifecycle.ViewModelProvider;

import android.content.Intent;
import android.os.AsyncTask;
import android.os.Bundle;

import androidx.annotation.Nullable;
import androidx.annotation.StringRes;
import androidx.appcompat.app.AppCompatActivity;

import android.text.Editable;
import android.text.TextWatcher;
import android.view.KeyEvent;
import android.view.View;
import android.view.inputmethod.EditorInfo;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ProgressBar;
import android.widget.TextView;
import android.widget.Toast;

import com.xyqlx.paddydoctor.BottomNavigationActivity;
import com.xyqlx.paddydoctor.PhotoActivity;
import com.xyqlx.paddydoctor.R;
import com.xyqlx.paddydoctor.data.model.LoggedInUser;
import com.xyqlx.paddydoctor.ui.login.LoginViewModel;
import com.xyqlx.paddydoctor.ui.login.LoginViewModelFactory;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.util.HashMap;
import java.util.Properties;

public class LoginActivity extends AppCompatActivity {

    // 引入了一个ViewModel，主要是作为数据更新和View刷新关联起来
    private LoginViewModel loginViewModel;

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_login);
        // 这个就是简单的ViewModel的构造方法
        loginViewModel = new ViewModelProvider(this, new LoginViewModelFactory())
                .get(LoginViewModel.class);

        final EditText usernameEditText = findViewById(R.id.username);
        final EditText passwordEditText = findViewById(R.id.password);
        final Button loginButton = findViewById(R.id.login);
        final Button guestLoginButton = findViewById(R.id.guestLogin);
        final ProgressBar loadingProgressBar = findViewById(R.id.loading);

        // 手动设置标题，见 https://stackoverflow.com/a/29455956
        getSupportActionBar().setTitle(R.string.title_activity_login);

        // 观察 viewmodel,被观察对象发生变化的时候，触发这里的操作
        loginViewModel.getLoginFormState().observe(this, new Observer<LoginFormState>() {
            @Override
            public void onChanged(@Nullable LoginFormState loginFormState) {
                if (loginFormState == null) {
                    return;
                }
                loginButton.setEnabled(loginFormState.isDataValid());
                if (loginFormState.getUsernameError() != null) {
                    usernameEditText.setError(getString(loginFormState.getUsernameError()));
                }
                if (loginFormState.getPasswordError() != null) {
                    passwordEditText.setError(getString(loginFormState.getPasswordError()));
                }
            }
        });

        loginViewModel.getLoginResult().observe(this, new Observer<LoginResult>() {
            @Override
            public void onChanged(@Nullable LoginResult loginResult) {
                if (loginResult == null) {
                    return;
                }
                loadingProgressBar.setVisibility(View.GONE);
                if (loginResult.getError() != null) {
                    showLoginFailed(loginResult.getError());
                }
                if (loginResult.getSuccess() != null) {
                    updateUiWithUser(loginResult.getSuccess());
                    //Complete and destroy login activity once successful
                    finish();
                    // 保存用户及密码
                    saveUser(usernameEditText.getText().toString(), passwordEditText.getText().toString());
                    // 保存session token
                    LoggedInUser.setLoggedInUser(LoginActivity.this, loginResult.getSuccess().getUser());
                    // 登录成功后，启动主界面，利用Intent启动另外一个Activity
                    // 关于显式和隐式https://blog.csdn.net/qq_35698774/article/details/105966038
                    Intent myIntent = new Intent(LoginActivity.this, BottomNavigationActivity.class);
                    LoginActivity.this.startActivity(myIntent);
                }
                setResult(Activity.RESULT_OK);
            }
        });

        TextWatcher afterTextChangedListener = new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence s, int start, int count, int after) {
                // ignore
            }

            @Override
            public void onTextChanged(CharSequence s, int start, int before, int count) {
                // ignore
            }

            @Override
            public void afterTextChanged(Editable s) {
                // 进行检查并重新set值，也根据情况提示输入是否符合要求
                loginViewModel.loginDataChanged(usernameEditText.getText().toString(),
                        passwordEditText.getText().toString());
            }
        };
        usernameEditText.addTextChangedListener(afterTextChangedListener);
        passwordEditText.addTextChangedListener(afterTextChangedListener);

        passwordEditText.setOnEditorActionListener(new TextView.OnEditorActionListener() {

            @Override
            public boolean onEditorAction(TextView v, int actionId, KeyEvent event) {
                // 捕获Android文本输入框的软键盘完成(Done)按键消息
                if (actionId == EditorInfo.IME_ACTION_DONE) {
//                    HashMap<String, String> map = new HashMap<>();
//                    map.put("username", usernameEditText.getText().toString());
//                    map.put("password", passwordEditText.getText().toString());
//                    LoginTask loginTask = new LoginTask();
//                    synchronized (loginTask) {
//                        loginTask.execute(map).notify();
//                    }
                    // 实现多线程，由implements Runnable变形而来的
                    // Runnable是接口，不可实例化，这里虽然new了，但确实没有实例化，实际上是一种内部类的一种简写
                    // https://blog.csdn.net/fchyang/article/details/81941123
                    Runnable runnable = new Runnable() {
                        @Override
                        public void run() {
                            // 进行登录，然后在LoginResult检查后进行跳转
                            loginViewModel.login(usernameEditText.getText().toString(),
                                    passwordEditText.getText().toString());
                        }
                    };
                    new Thread(runnable).start();
                }
                return false;
            }
        });
        // 和在密码软键盘按done一样的效果
        loginButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                // 打开旋转标识
                loadingProgressBar.setVisibility(View.VISIBLE);
                // 又启动新线程
                Runnable runnable = new Runnable() {
                    @Override
                    public void run() {
                        loginViewModel.login(usernameEditText.getText().toString(),
                                passwordEditText.getText().toString());
                    }
                };
                new Thread(runnable).start();
            }
        });

        // 游客登录
        guestLoginButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                loadingProgressBar.setVisibility(View.VISIBLE);
                Runnable runnable = new Runnable() {
                    @Override
                    public void run() {
                        loginViewModel.login("guest",
                                "guestpass");
                    }
                };
                new Thread(runnable).start();
            }
        });

        // 之前全是响应式代码如listener或者observer，这一句运行到了就会把上次的账号密码填在text
        loadUser(usernameEditText, passwordEditText);
    }

    // 这里是按原来的登录逻辑，就是显示toast信息，也可以带图片，跳转逻辑还是自己写
    private void updateUiWithUser(LoggedInUserView model) {
        // String welcome = getString(R.string.welcome) + model.getUser().getDisplayName();
        // 在这里更新登录后的UI（如果有）
        // Toast.makeText(getApplicationContext(), welcome, Toast.LENGTH_LONG).show();
    }

    // 登录失败的toast消息
    private void showLoginFailed(String errorString) {
        Toast.makeText(getApplicationContext(), errorString, Toast.LENGTH_SHORT).show();
    }

    private void loadUser(EditText usernameEditText, EditText passwordEditText){
        try {
            // 创建File对象
            File file = new File(this.getFilesDir(), "info.properties");
            // 创建FileIutputStream 对象
            FileInputStream fis = new FileInputStream(file);
            // 创建属性对象
            Properties pro = new Properties();
            // 加载文件
            pro.load(fis);
            // 关闭输入流对象
            fis.close();
            // 读取属性
            String username = pro.getProperty("username");
            String password = pro.getProperty("password");
            // 检查是否存在
            if(username != null){
                usernameEditText.setText(username);
                passwordEditText.setText(password);
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    private void saveUser(String username, String password){
        try {
            // 使用Android上下问获取当前项目的路径
            File file = new File(this.getFilesDir(), "info.properties");
            // 创建输出流对象
            FileOutputStream fos = new FileOutputStream(file);
            // 创建属性文件对象
            Properties pro = new Properties();
            // 设置用户名或密码
            pro.setProperty("username", username);
            // TODO 可以的话不要采用明文密码
            pro.setProperty("password", password);
            // 保存文件
            pro.store(fos, "info.properties");
            // 关闭输出流对象
            fos.close();
        } catch (Exception e) {
            e.printStackTrace();
        }
    }
}