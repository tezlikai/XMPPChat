package com.buaa.tezlikai.xmppchat.activity;

import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentManager;
import android.support.v4.app.FragmentPagerAdapter;
import android.support.v4.view.ViewPager;
import android.support.v7.app.AppCompatActivity;
import android.widget.LinearLayout;
import android.widget.TextView;

import com.buaa.tezlikai.xmppchat.R;
import com.buaa.tezlikai.xmppchat.fragment.ContactsFragment;
import com.buaa.tezlikai.xmppchat.fragment.SessionFragment;
import com.buaa.tezlikai.xmppchat.utils.ToolBarUtil;

import java.util.ArrayList;
import java.util.List;

import butterknife.ButterKnife;
import butterknife.InjectView;

public class MainActivity extends AppCompatActivity {

    @InjectView(R.id.main_viewpager)
    ViewPager mMainViewpager;

    @InjectView(R.id.main_bottom)
    LinearLayout mMainBottom;

    @InjectView(R.id.main_tv_title)
    TextView mMainTvTitle;

    private List<Fragment> mFragments	= new ArrayList<Fragment>();
    private String[] mToolBarTitleArr;
    private ToolBarUtil mToolBarUtil;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        ButterKnife.inject(this);
        initData();
        initListener();
    }

    private void initListener() {
        mMainViewpager.setOnPageChangeListener(new ViewPager.OnPageChangeListener() {
            @Override
            public void onPageScrolled(int position, float positionOffset, int positionOffsetPixels) {

            }

            @Override
            public void onPageSelected(int position) {
                //设置bottom图标颜色改变
                mToolBarUtil.changeColor(position);
                //设置title
                mMainTvTitle.setText(mToolBarTitleArr[position]);
            }

            @Override
            public void onPageScrollStateChanged(int state) {
            }
        });
        mToolBarUtil.setOnToolBarClickListener(new ToolBarUtil.OnToolBarClickListener() {
            @Override
            public void onToolClickClick(int position) {
                mMainViewpager.setCurrentItem(position);
            }
        });
    }

    private void initData() {

        // viewPager-->view-->pagerAdapter
        // viewPager-->fragment-->fragmentPagerAdapter-->fragment数量比较少使用
        // viewPager-->fragment-->fragmentStatePagerAdapter

        // 添加fragment到集合中
        mFragments.add(new SessionFragment());
        mFragments.add(new ContactsFragment());

        mMainViewpager.setAdapter(new MyPagerAdapter(getSupportFragmentManager()));

        //初始化底部导航栏
        mToolBarUtil = new ToolBarUtil();

        mToolBarTitleArr = new String[]{"会话","联系人"};

        int[] iconArr = new int[]{R.drawable.selector_meassage,R.drawable.selector_selfinfo};
        mToolBarUtil.createToolBar(mMainBottom, mToolBarTitleArr, iconArr);
        mToolBarUtil.changeColor(0);
    }

    private class MyPagerAdapter extends FragmentPagerAdapter {

        public MyPagerAdapter(FragmentManager fm) {
            super(fm);
        }

        @Override
        public Fragment getItem(int position) {
            return mFragments.get(position);
        }

        @Override
        public int getCount() {
            return mFragments.size();
        }
    }
}
