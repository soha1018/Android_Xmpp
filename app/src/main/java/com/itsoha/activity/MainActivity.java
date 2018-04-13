package com.itsoha.activity;

import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentManager;
import android.support.v4.app.FragmentPagerAdapter;
import android.support.v4.view.PagerAdapter;
import android.support.v4.view.ViewPager;
import android.support.v7.app.AppCompatActivity;
import android.view.View;
import android.widget.LinearLayout;
import android.widget.TextView;
import android.widget.Toolbar;

import com.itsoha.R;
import com.itsoha.fragment.ContactsFragment;
import com.itsoha.fragment.SessionFragment;
import com.itsoha.utils.ToolbarUtils;

import java.util.ArrayList;

import butterknife.ButterKnife;
import butterknife.InjectView;

public class MainActivity extends AppCompatActivity {

    @InjectView(R.id.tv_title)
    TextView mTitle;
    @InjectView(R.id.main_viewpager)
    ViewPager mViewPager;
    @InjectView(R.id.main_button)
    LinearLayout mButton;
    private ArrayList<Fragment> fragmentArrayList = new ArrayList<>();
    private ToolbarUtils toolbarUtils;
    private String[] titleName;

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        ButterKnife.inject(this);
        initData();
        initListener();
    }

    private void initListener() {
        mViewPager.addOnPageChangeListener(new ViewPager.OnPageChangeListener() {
            @Override
            public void onPageScrolled(int position, float positionOffset, int positionOffsetPixels) {

            }

            @Override
            public void onPageSelected(int position) {
                toolbarUtils.changeColor(position);
                mTitle.setText(titleName[position]);
            }

            @Override
            public void onPageScrollStateChanged(int state) {

            }
        });

        toolbarUtils.setToolbarClickListener(new ToolbarUtils.onToolbarClickListener() {
            @Override
            public void onToolbarClick(int position) {
                mViewPager.setCurrentItem(position);
            }
        });

/*        设置单击事件切换窗口
        for (int i = 0; i < mButton.getChildCount(); i++) {
            final int finalI = i;
            mButton.getChildAt(i).setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    toolbarUtils.changeColor(finalI);
                    mViewPager.setCurrentItem(finalI);
                }
            });
        }*/
    }

    private void initData() {
        fragmentArrayList.add(new SessionFragment());
        fragmentArrayList.add(new ContactsFragment());
        mViewPager.setAdapter(new MyAdapter(getSupportFragmentManager()));

        //添加底部工具栏
        titleName = new String[]{"会话", "联系人"};
        int[] icon = new int[]{R.drawable.selected_session, R.drawable.selected_contacts};
        toolbarUtils = new ToolbarUtils();
        toolbarUtils.createBottomToolbar(mButton, titleName,icon);
        toolbarUtils.changeColor(0);

    }

    private class MyAdapter extends FragmentPagerAdapter {
        MyAdapter(FragmentManager fm) {
            super(fm);
        }

        @Override
        public Fragment getItem(int position) {
            return fragmentArrayList.get(position);
        }

        @Override
        public int getCount() {
            return fragmentArrayList.size();
        }
    }
}
