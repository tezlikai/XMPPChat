package com.buaa.tezlikai.xmppchat.fragment;


import android.content.Context;
import android.content.Intent;
import android.database.ContentObserver;
import android.database.Cursor;
import android.net.Uri;
import android.os.Bundle;
import android.os.Handler;
import android.support.v4.app.Fragment;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.CursorAdapter;
import android.widget.ImageView;
import android.widget.ListView;
import android.widget.TextView;

import com.buaa.tezlikai.xmppchat.R;
import com.buaa.tezlikai.xmppchat.activity.ChatActivity;
import com.buaa.tezlikai.xmppchat.dbhelper.ContactOpenHelper;
import com.buaa.tezlikai.xmppchat.provider.ContactsProvicer;
import com.buaa.tezlikai.xmppchat.utils.ThreadUtils;

/**
 * 联系人ContactsFragment
 */
public class ContactsFragment extends Fragment {

    private ListView mListView;
    private CursorAdapter mAdapter;

    @Override
    public void onCreate(Bundle savedInstanceState) {
        init();
        super.onCreate(savedInstanceState);
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_contacts, container, false);
        initView(view);
        return view;
    }

    @Override
    public void onActivityCreated(Bundle savedInstanceState) {
        initData();
        initListener();
        super.onActivityCreated(savedInstanceState);
    }

    private void init() {
        registerContentObserver();
    }

    private void initView(View view) {
        mListView = (ListView) view.findViewById(R.id.listView);
    }

    private void initData() {
                setOrNotifyAdapter();//设置或更新Adapter
    }

    private void initListener() {
        mListView.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> parent, View view, int position, long id) {

                //需要把jid和昵称传过去
                Cursor c = mAdapter.getCursor();
                c.moveToPosition(position);

                //拿到jid（账号）--> 发送消息的时候需要
                String account = c.getString(c.getColumnIndex(ContactOpenHelper.ContactTable.ACCOUNT));
                //拿到nickName--> 显示效果
                String nickName = c.getString(c.getColumnIndex(ContactOpenHelper.ContactTable.NICKNAME));

                Intent intent = new Intent(getActivity(),ChatActivity.class);
                intent.putExtra(ChatActivity.CLICKACCOUNT,account);
                intent.putExtra(ChatActivity.CLICKNICKNAME,nickName);

                startActivity(intent);
            }
        });

    }

    @Override
    public void onDestroy() {
        //按照常理，我们Fragment销毁了，那么我们就不应该去继续去监听
        //但是实际，我们是需要一直监听对应roster的改变
        //所以，我们把联系人的监听和同步操作放到Service去
        unregisterContentObserver();

        super.onDestroy();
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
                final Cursor cursor = getActivity().getContentResolver().query(ContactsProvicer.URI_CONTACT, null, null, null, null);

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
                                View view = View.inflate(context, R.layout.item_contact, null);
                                return view;
                            }
                            //设置数据显示数据
                            @Override
                            public void bindView(View view, Context context, Cursor cursor) {
                                ImageView ivHead = (ImageView) view.findViewById(R.id.head);
                                TextView tvAccount = (TextView) view.findViewById(R.id.account);
                                TextView tvNickName = (TextView) view.findViewById(R.id.nickname);

                                String account = cursor.getString(cursor.getColumnIndex(ContactOpenHelper.ContactTable.ACCOUNT));
                                String nickName = cursor.getString(cursor.getColumnIndex(ContactOpenHelper.ContactTable.NICKNAME));

                                tvAccount.setText(account);
                                tvNickName.setText(nickName);
                            }
                        };
                        mListView.setAdapter(mAdapter);
                    }
                });
            }
        });
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
        getActivity().getContentResolver().registerContentObserver(ContactsProvicer.URI_CONTACT, true, mMyContentObserver);
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
