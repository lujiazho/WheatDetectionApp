package com.xyqlx.paddydoctor.ui.home;

import android.content.Intent;
import android.graphics.Color;
import android.os.Bundle;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ExpandableListAdapter;
import android.widget.ExpandableListView;
import android.widget.ListAdapter;
import android.widget.ListView;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import androidx.lifecycle.Observer;
import androidx.lifecycle.ViewModelProvider;

import com.github.mikephil.charting.charts.LineChart;
import com.github.mikephil.charting.components.XAxis;
import com.github.mikephil.charting.data.Entry;
import com.github.mikephil.charting.data.LineData;
import com.github.mikephil.charting.data.LineDataSet;
import com.github.mikephil.charting.formatter.ValueFormatter;
import com.google.gson.JsonArray;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import com.google.gson.JsonParser;
import com.xyqlx.paddydoctor.BottomNavigationActivity;
import com.xyqlx.paddydoctor.DetectorActivity;
import com.xyqlx.paddydoctor.FlaskServerService;
import com.xyqlx.paddydoctor.R;
import com.xyqlx.paddydoctor.data.model.HistoryItem;
import com.xyqlx.paddydoctor.data.model.LoggedInUser;
import com.xyqlx.paddydoctor.ui.history.HistoryActivity;
import com.xyqlx.paddydoctor.ui.history.HistoryRecyclerAdapter;
import com.xyqlx.paddydoctor.ui.login.LoginActivity;
import com.youth.banner.Banner;
import com.youth.banner.holder.BannerImageHolder;
import com.youth.banner.indicator.CircleIndicator;
import com.youth.banner.transformer.RotateYTransformer;
import com.youth.banner.util.BannerUtils;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import java.util.Locale;

import okhttp3.ResponseBody;
import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;
import retrofit2.Retrofit;

public class HomeFragment extends Fragment {

    private HomeViewModel homeViewModel;
    // 这三个是自己添加的
    private Banner banner;
    private LineChart chart;
    private ExpandableListView expandable;

    public View onCreateView(@NonNull LayoutInflater inflater,
                                ViewGroup container, Bundle savedInstanceState) {
        
        homeViewModel = new ViewModelProvider(this).get(HomeViewModel.class);
        View root = inflater.inflate(R.layout.fragment_home, container, false);

        banner = root.findViewById(R.id.banner);
        chart = (LineChart) root.findViewById(R.id.chart);
        expandable = (ExpandableListView)root.findViewById(R.id.expandableListView);
//        final TextView textView = root.findViewById(R.id.text_home);
//        homeViewModel.getText().observe(getViewLifecycleOwner(), new Observer<String>() {
//            @Override
//            public void onChanged(@Nullable String s) {
//                textView.setText(s);
//            }
//        });
        useBanner();
        initExpandableListView();
        initChart();
        return root;
    }

    private void initExpandableListView(){
        Retrofit retrofit = new Retrofit.Builder().baseUrl(FlaskServerService.HttpConfig.BASE_URL).build();
        FlaskServerService serverService =retrofit.create(FlaskServerService.class);
        Call<ResponseBody> call = serverService.getKnowledge();
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
                ArrayList<String> groups = new ArrayList<>();
                ArrayList<ArrayList<String>> childs = new ArrayList<>();
                ArrayList<ArrayList<String>> details = new ArrayList<>();
                for (java.util.Map.Entry<String, JsonElement> entry: obj.entrySet()) {
                    groups.add(entry.getKey());
                    ArrayList<String> child = new ArrayList<>();
                    childs.add(child);
                    ArrayList<String> detail = new ArrayList<>();
                    details.add(detail);
                    JsonObject entryObject = entry.getValue().getAsJsonObject();
                    JsonObject diseases = entryObject.get("diseases").getAsJsonObject();
                    for (java.util.Map.Entry<String, JsonElement> disease: diseases.entrySet()) {
                        child.add(disease.getKey());
                        detail.add(disease.getValue().getAsJsonObject().get("text").getAsString());
                    }
                }
                ExpandableListviewAdapter adapter=new ExpandableListviewAdapter(HomeFragment.this.getContext(), expandable,groups,childs,details);
                expandable.setAdapter(adapter);
                //默认展开第一个数组
                // expandable.expandGroup(0);
                //关闭数组某个数组，可以通过该属性来实现全部展开和只展开一个列表功能
                //expand_list_id.collapseGroup(0);
                //子视图的点击事件
                setListViewHeight(expandable);
                expandable.setOnChildClickListener(new ExpandableListView.OnChildClickListener() {
                    @Override
                    public boolean onChildClick(ExpandableListView expandableListView, View view, int groupPosition, int childPosition, long l) {
                        // Toast.makeText(HomeFragment.this.getContext(), childs.get(groupPosition).get(childPosition), Toast.LENGTH_SHORT).show();
                        // setListViewHeight(expandableListView, groupPosition, childPosition);
                        return true;
                    }
                });
                expandable.setOnGroupClickListener(new ExpandableListView.OnGroupClickListener() {
                    @Override
                    public boolean onGroupClick(ExpandableListView parent, View v, int groupPosition, long id) {
                        // 关于可滚动的ExpandableListView见 https://stackoverflow.com/a/26839780
                        setListViewHeight(parent, groupPosition);
                        return false;
                    }
                });
            }

            @Override
            public void onFailure(Call<ResponseBody> call, Throwable t) {
                //数据请求失败
                Log.d("", "failure");
            }
        });
    }

    private void setListViewHeight(ListView listView) {
        ListAdapter listAdapter = listView.getAdapter();
        int totalHeight = 0;
        for (int i = 0; i < listAdapter.getCount(); i++) {
            View listItem = listAdapter.getView(i, null, listView);
            listItem.measure(0, 0);
            totalHeight += listItem.getMeasuredHeight();
        }

        ViewGroup.LayoutParams params = listView.getLayoutParams();
        params.height = totalHeight
                + (listView.getDividerHeight() * (listAdapter.getCount() - 1));
        listView.setLayoutParams(params);
        listView.requestLayout();
    }

    private void setListViewHeight(ExpandableListView listView,
                                   int group) {
        ExpandableListAdapter listAdapter = (ExpandableListAdapter) listView.getExpandableListAdapter();
        int totalHeight = 0;
        int desiredWidth = View.MeasureSpec.makeMeasureSpec(listView.getWidth(),
                View.MeasureSpec.EXACTLY);
        for (int i = 0; i < listAdapter.getGroupCount(); i++) {
            View groupItem = listAdapter.getGroupView(i, false, null, listView);
            groupItem.measure(desiredWidth, View.MeasureSpec.UNSPECIFIED);

            totalHeight += groupItem.getMeasuredHeight();

            if (((listView.isGroupExpanded(i)) && (i != group))
                    || ((!listView.isGroupExpanded(i)) && (i == group))) {
            //if(listView.isGroupExpanded(i) || i==group){
                for (int j = 0; j < listAdapter.getChildrenCount(i); j++) {
                    View listItem = listAdapter.getChildView(i, j, false, null,
                            listView);
                    listItem.measure(desiredWidth, View.MeasureSpec.UNSPECIFIED);

                    totalHeight += listItem.getMeasuredHeight();

                }
            }
        }
        ViewGroup.LayoutParams params = listView.getLayoutParams();
        int height = totalHeight
                + (listView.getDividerHeight() * (listAdapter.getGroupCount() - 1));
        if (height < 10)
            height = 200;
        params.height = height;
        listView.setLayoutParams(params);
        listView.requestLayout();
    }

    // 设置使用banner（横幅广告）
    private void useBanner(){
        // 文档见 https://github.com/youth5201314/banner
        // 参考例见 https://blog.csdn.net/xhnmbest/article/details/106636503
        ImageAdapter adapter = new ImageAdapter(DataBean.getTestData());
        banner.setAdapter(adapter)//设置适配器
                .setCurrentItem(3,false)
                .addBannerLifecycleObserver(this)//添加生命周期观察者
                .setBannerRound(BannerUtils.dp2px(35))//圆角
                // .addPageTransformer(new RotateYTransformer())//添加切换效果
                .setIndicator(new CircleIndicator(this.getContext()));//设置指示器
        // .addOnPageChangeListener(this);//添加切换监听
    }

    private void initChart(){
        chart.setNoDataTextColor(Color.rgb(160, 160, 160));
        chart.setNoDataText("数据加载中……");
        // 文档见 https://weeklycoding.com/mpandroidchart-documentation
        Retrofit retrofit = new Retrofit.Builder().baseUrl(FlaskServerService.HttpConfig.BASE_URL).build();
        FlaskServerService serverService =retrofit.create(FlaskServerService.class);
        LoggedInUser user = LoggedInUser.getLoggedInUser(this.getContext());
        String token = user.getUserToken();
        if(user.getDisplayName().equals("guest")){
            chart.setNoDataText("需要登录并设置地区才能查看");
        }else if(user.getLocation().equals("unknown")){
            chart.setNoDataText("需要设置地区才能查看");
        }
        Call<ResponseBody> call = serverService.getTrending(token);
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
                if(!obj.has("result")){
                    // chart.invalidate();
                    return;
                }
                // TODO 处理匿名用户等情况
                JsonArray results = obj.getAsJsonArray("result");
                // 解析
                List<Entry> entries = new ArrayList<Entry>();
                int dayCount = 7;
                for (JsonElement result :
                        results) {
                    JsonObject resultAsJsonObject = result.getAsJsonObject();
                    int offset = resultAsJsonObject.get("offset").getAsInt();
                    int number = resultAsJsonObject.get("number").getAsInt();
                    entries.add(new Entry(8 - offset, number));
                }
//                entries.add(new Entry(1, 2));
//                entries.add(new Entry(2, 3));
                LineDataSet dataSet = new LineDataSet(entries, "病虫害数量"); // add entries to dataset
                dataSet.setColor(Color.rgb(255, 0, 0));
                dataSet.setValueTextColor(Color.rgb(255, 0, 0));
                dataSet.setMode(LineDataSet.Mode.HORIZONTAL_BEZIER);
                dataSet.setValueFormatter(new ValueFormatter() {
                    @Override
                    public String getFormattedValue(float value) {
                        return String.format(Locale.CHINA, "%.0f", value);
                    }
                });
                XAxis xAxis = chart.getXAxis();
                xAxis.setPosition(XAxis.XAxisPosition.BOTTOM);
                xAxis.setDrawGridLines(false);
                xAxis.setGranularity(1f);
                xAxis.setLabelCount(7);
                xAxis.setAxisMaximum(7f);
                xAxis.setAxisMinimum(1f);
                xAxis.setValueFormatter(new ValueFormatter() {
                    @Override
                    public String getFormattedValue(float value) {
                        return String.format(Locale.CHINA, "%.0f天前",8 - value);
                    }
                });
                LineData lineData = new LineData(dataSet);
                chart.getDescription().setEnabled(false);
                chart.setData(lineData);
                chart.invalidate();
            }

            @Override
            public void onFailure(Call<ResponseBody> call, Throwable t) {
                //数据请求失败
                Log.d("", "failure");
            }
        });
    }
}