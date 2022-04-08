package com.xyqlx.paddydoctor.ui.login;

import androidx.annotation.Nullable;

/**
 * Authentication result : success (user details) or error message.
 */
class LoginResult { // 登陆返回结果体
    @Nullable
    private LoggedInUserView success;
    @Nullable
    private String errorMessage; // 把integer改成了string，显示错误信息

    LoginResult(@Nullable String errorMessage) {
        this.errorMessage = errorMessage;
    }

    LoginResult(@Nullable LoggedInUserView success) {
        this.success = success;
    }

    @Nullable
    LoggedInUserView getSuccess() {
        return success;
    }

    @Nullable
    String getError() {
        return errorMessage;
    }
}