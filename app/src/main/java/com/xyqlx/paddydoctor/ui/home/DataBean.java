package com.xyqlx.paddydoctor.ui.home;

import java.util.ArrayList;
import java.util.List;

public class DataBean {
    public String imageUrl;
    public String text;

    public DataBean(String imageUrl, String text) {
        this.imageUrl = imageUrl;
        this.text = text;
    }

    public static List<DataBean> getTestData() {
        List<DataBean> list = new ArrayList<>();
        list.add(new DataBean("https://img-blog.csdnimg.cn/20200524220705330.png", "1"));
        list.add(new DataBean("https://img-blog.csdnimg.cn/20200524220705336.png", "2"));
        list.add(new DataBean("https://img.zcool.cn/community/01f8735e27a174a8012165188aa959.jpg", "3"));
        list.add(new DataBean("https://img-blog.csdnimg.cn/20200524220705336.png", "4"));
        return list;
    }
}
