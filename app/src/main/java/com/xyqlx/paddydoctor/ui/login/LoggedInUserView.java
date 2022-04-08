package com.xyqlx.paddydoctor.ui.login;

import com.xyqlx.paddydoctor.data.model.LoggedInUser;

/**
 * Class exposing authenticated user details to the UI.
 */
class LoggedInUserView { // 用户界面展示信息
    private LoggedInUser user; // 这里改了
    //... other data fields that may be accessible to the UI

    LoggedInUserView(LoggedInUser user) {
        this.user = user;
    }

    LoggedInUser getUser() {
        return user;
    }
}