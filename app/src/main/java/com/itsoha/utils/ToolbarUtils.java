package com.itsoha.utils;

import android.view.View;
import android.widget.LinearLayout;
import android.widget.TextView;

import com.itsoha.R;

import java.util.ArrayList;

public class ToolbarUtils {
    private ArrayList<TextView> mTextViews = new ArrayList<>();

    public void createBottomToolbar(LinearLayout layout, String[] name, int[] icon) {
        for (int i = 0; i < name.length; i++) {
            TextView textView = (TextView) View.inflate(layout.getContext(), R.layout.inflate_bottom_toolbar, null);
            textView.setText(name[i]);
            textView.setCompoundDrawablesWithIntrinsicBounds(0, icon[i], 0, 0);
            LinearLayout.LayoutParams params = new LinearLayout.LayoutParams(0, LinearLayout.LayoutParams.MATCH_PARENT);
            //设置宽度的权重
            params.weight = 1;
            layout.addView(textView, params);

            mTextViews.add(textView);

            final int finalI = i;
            textView.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    toolbarClickListener.onToolbarClick(finalI);
                }
            });
        }
    }

    public void changeColor(int position) {
        for (TextView tv : mTextViews) {
            tv.setSelected(false);
        }
        mTextViews.get(position).setSelected(true);
    }

    //1.创一个一个接口
    public interface onToolbarClickListener {
        void onToolbarClick(int position);
    }

    //2.定义接口成员变量
    private onToolbarClickListener toolbarClickListener;

    //3.需要传值的时候使用它

    //4.暴露一个公共的方法

    public void setToolbarClickListener(onToolbarClickListener toolbarClickListener) {
        this.toolbarClickListener = toolbarClickListener;
    }
}
