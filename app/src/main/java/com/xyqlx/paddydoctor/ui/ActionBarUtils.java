package com.xyqlx.paddydoctor.ui;

import android.app.Activity;
import android.util.DisplayMetrics;
import android.view.Gravity;
import android.widget.LinearLayout;
import android.widget.TextView;

import androidx.appcompat.app.ActionBar;
import androidx.appcompat.app.AppCompatActivity;

public class ActionBarUtils {
    //把actionBar的文字标题居中
    public static void centerActionBarTitle(AppCompatActivity activity) {
        ActionBar bar = activity.getSupportActionBar();
        if(bar == null){
            return;
        }
        String title = bar.getTitle().toString();
        bar.setDisplayShowCustomEnabled(true);

        TextView textView = new TextView(activity);
        textView.setText(title);
        bar.setTitle("");
        textView.setTextSize(20);
        textView.setTextColor(0xff000000);

        LinearLayout actionbarLayout = new LinearLayout(activity);

        bar.setCustomView(actionbarLayout,new ActionBar.LayoutParams(ActionBar.LayoutParams.WRAP_CONTENT,
                ActionBar.LayoutParams.WRAP_CONTENT));

        ActionBar.LayoutParams mP = (ActionBar.LayoutParams) actionbarLayout

                .getLayoutParams();

        mP.gravity = mP.gravity & ~Gravity.HORIZONTAL_GRAVITY_MASK| Gravity.CENTER_HORIZONTAL;

        actionbarLayout.addView(textView);

        bar.setCustomView(actionbarLayout, mP);
    }
}
