package com.buaa.tezlikai.xmppchat.utils;

import android.view.View;
import android.widget.LinearLayout;
import android.widget.TextView;

import com.buaa.tezlikai.xmppchat.R;

import java.util.ArrayList;
import java.util.List;

/**
 * 底部导航栏动态生成
 */
public class ToolBarUtil {

    List<TextView> mTextViews = new ArrayList<TextView>();

    public void createToolBar(LinearLayout container, String[] toolBarTitleArr, int[] iconArr) {
        for (int i = 0; i <toolBarTitleArr.length ; i++) {
            TextView tv = (TextView) View.inflate(container.getContext(), R.layout.inflate_toolbar_btn, null);

            //动态修改textView里面的drawableTop属性
            tv.setText(toolBarTitleArr[i]);
            tv.setCompoundDrawablesWithIntrinsicBounds(0, iconArr[i], 0, 0);

            int width = 0;
            int height = LinearLayout.LayoutParams.MATCH_PARENT;
            LinearLayout.LayoutParams params = new LinearLayout.LayoutParams(width,height);

            //设置weight属性
            params.weight = 1;
            container.addView(tv,params);

            //保存textView到集合中
            mTextViews.add(tv);

            //设置点击事件
            final int finalI = i;
            tv.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    //不同模块之间传值需要调用接口回调
                    //3、需要传值的地方，用接口对象回调方法
                    mOnToolBarClickListener.onToolClickClick(finalI);
                }
            });
        }
    }

    public void changeColor(int position){
        //还原所有的颜色
        for (TextView tv : mTextViews){
            tv.setSelected(false);
        }
        mTextViews.get(position).setSelected(true);//通过设置selected属性控制选中效果
    }
    //1、创建接口或接口回调
    public interface OnToolBarClickListener{
        void onToolClickClick(int position);
    }
    //2、定义接口变量
    OnToolBarClickListener mOnToolBarClickListener;

    //4、暴露一个公共方法
    public void setOnToolBarClickListener(OnToolBarClickListener onToolBarClickListener) {
        mOnToolBarClickListener = onToolBarClickListener;
    }
}
