package com.xyqlx.paddydoctor.ui.home;

import android.content.Context;
import android.graphics.Point;
import android.os.Build;
import android.view.Display;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.WindowManager;
import android.widget.BaseExpandableListAdapter;
import android.widget.ExpandableListView;
import android.widget.TextView;

import com.xyqlx.paddydoctor.R;

import java.util.ArrayList;

public class ExpandableListviewAdapter extends BaseExpandableListAdapter {
    private ArrayList<String> groups;
    private ArrayList<ArrayList<String>> childs;
    private ArrayList<ArrayList<String>> details;
    private Context context;
    private ExpandableListView root;

    public ExpandableListviewAdapter(Context context, ExpandableListView root,ArrayList<String> groups,ArrayList<ArrayList<String>> childs,ArrayList<ArrayList<String>> details){
        this.context=context;
        this.root = root;
        this.groups=groups;
        this.childs=childs;
        this.details=details;
    }

    @Override
    public int getGroupCount() {
        return groups.size();
    }

    @Override
    public int getChildrenCount(int i) {
        return childs.get(i).size();
    }

    @Override
    public Object getGroup(int i) {
        return groups.get(i);
    }

    @Override
    public Object getChild(int i, int i1) {
        return childs.get(i).get(i1);
    }

    @Override
    public long getGroupId(int i) {
        return i;
    }

    @Override
    public long getChildId(int i, int i1) {
        return i1;
    }

    @Override
    //分组和子选项是否持有稳定的ID, 就是说底层数据的改变会不会影响到它们
    public boolean hasStableIds() {
        return true;
    }

    @Override
    public View getGroupView(int groupPosition, boolean isExpanded, View convertView, ViewGroup parent) {
        GroupViewHolder groupViewHolder;
        if (convertView == null){
            convertView = LayoutInflater.from(parent.getContext()).inflate(R.layout.container_expandable,parent,false);
            groupViewHolder = new GroupViewHolder();
            groupViewHolder.parentTextView = convertView.findViewById(R.id.parentTextView);
            convertView.setTag(groupViewHolder);
        }else {
            groupViewHolder = (GroupViewHolder)convertView.getTag();
        }
        groupViewHolder.parentTextView.setText(groups.get(groupPosition));
        return convertView;
    }

    @Override
    public View getChildView(int groupPosition, int childPosition, boolean isLastChild, View convertView, ViewGroup parent) {
        ChildViewHolder childViewHolder;
        if (convertView==null){
            convertView = LayoutInflater.from(parent.getContext()).inflate(R.layout.item_expandable,parent,false);
            childViewHolder = new ChildViewHolder();
            childViewHolder.childrenTextView = convertView.findViewById(R.id.childrenTextView);
            childViewHolder.childrenContentTextView = convertView.findViewById(R.id.childrenTextViewContent);
            convertView.setTag(childViewHolder);

        }else {
            childViewHolder = (ChildViewHolder) convertView.getTag();
        }
        childViewHolder.childrenTextView.setText(childs.get(groupPosition).get(childPosition));
        childViewHolder.childrenContentTextView.setText(details.get(groupPosition).get(childPosition));
        View finalConvertView = root;
        convertView.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if(childViewHolder.childrenContentTextView.getVisibility() == View.GONE){
                    childViewHolder.childrenContentTextView.setVisibility(View.VISIBLE);
                    finalConvertView.measure(View.MeasureSpec.UNSPECIFIED, View.MeasureSpec.UNSPECIFIED);
                    int rawHeight = finalConvertView.getMeasuredHeight();
                    int addHeight = getTextViewHeight(childViewHolder.childrenContentTextView);
                    ViewGroup.LayoutParams params = finalConvertView.getLayoutParams();
                    if(params.height > 0){
                        params.height = addHeight + params.height;
                    }else{
                        params.height = addHeight + rawHeight;
                    }
                    finalConvertView.setLayoutParams(params);
                }else{
                    childViewHolder.childrenContentTextView.setVisibility(View.GONE);
                    childViewHolder.childrenTextView.measure(View.MeasureSpec.UNSPECIFIED, View.MeasureSpec.UNSPECIFIED);
                    int rawHeight = finalConvertView.getMeasuredHeight();
                    int addHeight = getTextViewHeight(childViewHolder.childrenContentTextView);
                    ViewGroup.LayoutParams params = finalConvertView.getLayoutParams();
                    if(params.height > 0){
                        params.height = params.height - addHeight;
                    }else{
                        params.height = rawHeight;
                    }
                    finalConvertView.setLayoutParams(params);
                    // TODO 在展开详情的情况下关闭组高度可能计算错误
                }
                finalConvertView.requestLayout();
            }
        });
        return convertView;
    }

    //指定位置上的子元素是否可选中
    @Override
    public boolean isChildSelectable(int i, int i1) {
        return true;
    }
    static class GroupViewHolder {
        TextView parentTextView;
    }

    static class ChildViewHolder {
        TextView childrenTextView;
        TextView childrenContentTextView;
    }

    /**
     * Get the TextView height before the TextView will render
     * @param textView the TextView to measure
     * @return the height of the textView
     */
    public static int getTextViewHeight(TextView textView) {
        WindowManager wm =
                (WindowManager) textView.getContext().getSystemService(Context.WINDOW_SERVICE);
        Display display = wm.getDefaultDisplay();

        int deviceWidth;

        if(android.os.Build.VERSION.SDK_INT >= Build.VERSION_CODES.HONEYCOMB_MR2){
            Point size = new Point();
            display.getSize(size);
            deviceWidth = size.x;
        } else {
            deviceWidth = display.getWidth();
        }

        int widthMeasureSpec = View.MeasureSpec.makeMeasureSpec(deviceWidth, View.MeasureSpec.AT_MOST);
        int heightMeasureSpec = View.MeasureSpec.makeMeasureSpec(0, View.MeasureSpec.UNSPECIFIED);
        textView.measure(widthMeasureSpec, heightMeasureSpec);
        return textView.getMeasuredHeight();
    }
}
