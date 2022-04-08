package com.xyqlx.paddydoctor.ui.notifications;

import android.app.AlertDialog;
import android.content.DialogInterface;
import android.content.Intent;
import android.os.Bundle;
import android.text.Html;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TableRow;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import androidx.lifecycle.Observer;
import androidx.lifecycle.ViewModelProvider;
import com.xyqlx.paddydoctor.R;
import com.xyqlx.paddydoctor.data.model.LoggedInUser;
import com.xyqlx.paddydoctor.ui.history.HistoryActivity;
import com.xyqlx.paddydoctor.ui.login.LoginActivity;
import com.xyqlx.paddydoctor.ui.profile.ProfileActivity;

public class NotificationsFragment extends Fragment {

    private NotificationsViewModel notificationsViewModel;

    public View onCreateView(@NonNull LayoutInflater inflater,
            ViewGroup container, Bundle savedInstanceState) {
        notificationsViewModel =
                new ViewModelProvider(this).get(NotificationsViewModel.class);
        View root = inflater.inflate(R.layout.fragment_notifications, container, false);
        // 注释这段是自动生成的
//        final TextView textView = root.findViewById(R.id.text_notifications);
//        notificationsViewModel.getText().observe(getViewLifecycleOwner(), new Observer<String>() {
//            @Override
//            public void onChanged(@Nullable String s) { textView.setText(s); }
//        });
        // 下面是自己添加的
        // 检测记录
        TableRow detectRecordRow = (TableRow) root.findViewById(R.id.detect_record_row);
        detectRecordRow.setOnClickListener(v->{
            Intent intent = new Intent(this.getContext(), HistoryActivity.class);
            this.startActivity(intent);
        });
        // 个人信息
        TableRow userProfileRow = (TableRow) root.findViewById(R.id.user_profile_row);
        userProfileRow.setOnClickListener(v->{
            Intent intent = new Intent(this.getContext(), ProfileActivity.class);
            this.startActivity(intent);
        });
        // 用户注册 - 似乎已弃用
        TableRow registerRow = root.findViewById(R.id.user_register_row);
        LoggedInUser user = LoggedInUser.getLoggedInUser(this.getContext());
        if(user.getDisplayName().equals("guest")){
            detectRecordRow.setVisibility(View.GONE);
            userProfileRow.setVisibility(View.GONE);
            registerRow.setVisibility(View.VISIBLE);
            registerRow.setOnClickListener(v -> {
                Intent intent = new Intent(this.getContext(), LoginActivity.class);
                this.startActivity(intent);
            });
        }
        // 开发团队
        TableRow developerRow = root.findViewById(R.id.developerRow);
        developerRow.setOnClickListener(v -> {
            AlertDialog aldg;
            AlertDialog.Builder adBd=new AlertDialog.Builder(getContext());
            adBd.setTitle("开发团队");
            adBd.setMessage("本应用由通宵达旦小队开发");
            adBd.setPositiveButton(Html.fromHtml("<font color='#000000'>确定</font>", Html.FROM_HTML_MODE_COMPACT), new DialogInterface.OnClickListener() {
                @Override
                public void onClick(DialogInterface dialog, int which) {

                }
            });
            aldg=adBd.create();
            aldg.show();
        });
        return root;
    }
}