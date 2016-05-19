package com.buaa.tezlikai.xmppchat.fragment;


import android.content.Context;
import android.database.ContentObserver;
import android.database.Cursor;
import android.net.Uri;
import android.os.Bundle;
import android.os.Handler;
import android.support.v4.app.Fragment;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.CursorAdapter;
import android.widget.ImageView;
import android.widget.ListView;
import android.widget.TextView;

import com.buaa.tezlikai.xmppchat.R;
import com.buaa.tezlikai.xmppchat.dbhelper.SmsOpenHelper;
import com.buaa.tezlikai.xmppchat.provider.SmsProvider;
import com.buaa.tezlikai.xmppchat.srevice.IMService;
import com.buaa.tezlikai.xmppchat.utils.ThreadUtils;

public class SessionFragment extends Fragment {

    private ListView mListView;
    private CursorAdapter mAdapter;

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_session, container, false);
        initView(view);
        return view;
    }

    @Override
    public void onActivityCreated(Bundle savedInstanceState) {
        init();
        initData();
        initListener();
        super.onActivityCreated(savedInstanceState);
    }

    private void init() {
        registerContentObserver();
    }

    private void initView(View view) {
        mListView = (ListView) view.findViewById(R.id.listView);
        setOrNotifyAdapter();//设置或更新Adapter
    }

    private void initData() {

    }

    private void initListener() {

    }

    /**
     * 设置或更新Adapter
     */
    private void setOrNotifyAdapter() {

        //判断adapter是否存在
        if (mAdapter != null) {
            //刷新adapter就行了
            mAdapter.getCursor().requery();
            return;
        }
        ThreadUtils.runInThread(new Runnable() {
            @Override
            public void run() {

                //对应查询记录
                final Cursor cursor = getActivity().getContentResolver().query(SmsProvider.URI_SESSION, null, null, new String[]{IMService.mCurAccount, IMService.mCurAccount}, null);

                //加入没有数据
                if (cursor.getCount() <= 0) {
                    return;
                }
                //设置Adapter,然后显示数据
                ThreadUtils.runInUIThread(new Runnable() {
                    @Override
                    public void run() {
                        mAdapter = new CursorAdapter(getContext(), cursor) {

                            //如果converview == null，返回一个具体的根视图
                            @Override
                            public View newView(Context context, Cursor cursor, ViewGroup parent) {
                                View view = View.inflate(context, R.layout.item_session, null);
                                return view;
                            }

                            //设置数据显示数据
                            @Override
                            public void bindView(View view, Context context, Cursor cursor) {
                                ImageView ivHead = (ImageView) view.findViewById(R.id.head);
                                TextView tvBody = (TextView) view.findViewById(R.id.body);
                                TextView tvNickName = (TextView) view.findViewById(R.id.nickname);

                                String body = cursor.getString(cursor.getColumnIndex(SmsOpenHelper.SmsTable.BODY));
                                String account = cursor.getString(cursor.getColumnIndex(SmsOpenHelper.SmsTable.SESSION_ACCOUNT));



                                tvBody.setText(body);
                                tvNickName.setText(account);
                            }
                        };
                        mListView.setAdapter(mAdapter);
                    }
                });
            }
        });
    }

    @Override
    public void onDestroy() {
        unregisterContentObserver();
        super.onDestroy();
    }

    /**
     * -------------------------监听数据库记录的改变---------------------------------
     */

    MyContentObserver mMyContentObserver = new MyContentObserver(new Handler());

    /**
     * 注册监听
     */
    public void registerContentObserver() {
        //arg1:监听的uri
        //arg2:监听的对象一旦发生改变就会收到通知
        getActivity().getContentResolver().registerContentObserver(SmsProvider.URI_SMS, true, mMyContentObserver);
    }

    /**
     * 反注册监听
     */
    public void unregisterContentObserver() {
        getActivity().getContentResolver().unregisterContentObserver(mMyContentObserver);
    }

    class MyContentObserver extends ContentObserver {

        public MyContentObserver(Handler handler) {
            super(handler);
        }

        /**
         * 如果数据库数据改变会在这个方法收到通知
         */
        @Override
        public void onChange(boolean selfChange, Uri uri) {
            super.onChange(selfChange, uri);
            // 更新Adapter或者刷新Adapter
            setOrNotifyAdapter();
        }
    }
}
