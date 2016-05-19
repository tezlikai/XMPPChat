package com.buaa.tezlikai.xmppchat.activity;

import android.content.ComponentName;
import android.content.Context;
import android.content.Intent;
import android.content.ServiceConnection;
import android.database.ContentObserver;
import android.database.Cursor;
import android.net.Uri;
import android.os.Bundle;
import android.os.Handler;
import android.os.IBinder;
import android.support.v7.app.AppCompatActivity;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.CursorAdapter;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.ListView;
import android.widget.TextView;

import com.buaa.tezlikai.xmppchat.R;
import com.buaa.tezlikai.xmppchat.dbhelper.SmsOpenHelper;
import com.buaa.tezlikai.xmppchat.provider.SmsProvider;
import com.buaa.tezlikai.xmppchat.srevice.IMService;
import com.buaa.tezlikai.xmppchat.utils.ThreadUtils;

import org.jivesoftware.smack.packet.Message;

import java.text.SimpleDateFormat;
import java.util.Date;

import butterknife.ButterKnife;
import butterknife.InjectView;
import butterknife.OnClick;

public class ChatActivity extends AppCompatActivity {


    public static final String CLICKACCOUNT = "clickAccount";
    public static final String CLICKNICKNAME = "clickNickName";

    @InjectView(R.id.title)
    TextView mTitle;

    @InjectView(R.id.listView)
    ListView mListView;

    @InjectView(R.id.et_body)
    EditText mEtBody;

    @InjectView(R.id.btn_send)
    Button mBtnSend;

    private String mClickAccount;
    private String mClickNickName;
    private CursorAdapter mAdapter;
    private IMService mImService;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_chat);
        ButterKnife.inject(this);
        init();
        initView();
        initData();
        initListener();
    }

    private void init() {
        //注册监听
        registerContentObserver();
        //綁定服務
        Intent service = new Intent(ChatActivity.this, IMService.class);
        bindService(service, mMyServiceConnect, BIND_AUTO_CREATE);

        mClickAccount = getIntent().getStringExtra(ChatActivity.CLICKACCOUNT);
        mClickNickName = getIntent().getStringExtra(ChatActivity.CLICKNICKNAME);
    }

    private void initView() {
        //设置title
        mTitle.setText("与" + mClickNickName + "聊天中");
    }

    private void initData() {
        setAdapterOrNotify();
    }

    private void setAdapterOrNotify() {
        //首先判断是否存在adapter
        if (mAdapter != null) {
            //刷新
            Cursor cursor = mAdapter.getCursor();
            cursor.requery();

            mListView.setSelection(cursor.getCount() - 1);//滚动到最后一行
            return;
        }
        ThreadUtils.runInThread(new Runnable() {
            @Override
            public void run() {
                /** 这样写查询语句是为了防止聊天记录混乱的问题*/
                final Cursor c = getContentResolver().query(SmsProvider.URI_SMS,//
                        null,//
                        "(from_account = ? and to_account=?)or(from_account = ? and to_account= ? )",// where条件
                        new String[]{IMService.mCurAccount, mClickAccount, mClickAccount, IMService.mCurAccount},// where条件的参数
                        SmsOpenHelper.SmsTable.TIME + " ASC"// 根据时间升序排序
                );

                if (c.getCount() < 1) {//如果没有数据就返回
                    return;
                }
                ThreadUtils.runInUIThread(new Runnable() {
                    @Override
                    public void run() {
                        // CursorAdapter: getview --> newView --> bindView
                        mAdapter = new CursorAdapter(ChatActivity.this, c) {
                            private static final int RECEIVE = 1;
                            private static final int SEND = 0;
                            //如果contentView == null 的时候会调用 --》 返回根布局
                           /* @Override
                            public View newView(Context context, Cursor cursor, ViewGroup parent) {
                                TextView tv = new TextView(context);
                                return tv;
                            }

                            @Override
                            public void bindView(View view, Context context, Cursor cursor) {
                                TextView tv = (TextView) view;
                                //具体的数据
                                String body = cursor.getString(cursor.getColumnIndex(SmsOpenHelper.SmsTable.BODY));
                                tv.setText(body);
                            }*/
                            //重写getview --> newView --> bindView等方法


                            @Override
                            public int getItemViewType(int position) {
                                c.moveToPosition(position);
                                //取出消息的创建者
                                String fromAccount = c.getString(c.getColumnIndex(SmsOpenHelper.SmsTable.FROM_ACCOUNT));
                                if (!IMService.mCurAccount.equals(fromAccount)) {//接收
                                    return RECEIVE;
                                } else {//发送
                                    return SEND;
                                }
                                //接收 -- 如果当前的账号不等于消息的创建者
                                //发送
                                //  return super.getItemViewType(position);
                            }

                            @Override
                            public int getViewTypeCount() {//一个ListView中添加两种布局
                                return super.getViewTypeCount() + 1;
                            }

                            @Override
                            public View getView(int position, View convertView, ViewGroup parent) {
                                viewHolder holder;
                                if (getItemViewType(position) == RECEIVE) {
                                    if (convertView == null) {
                                        convertView = View.inflate(ChatActivity.this, R.layout.item_chat_receive, null);
                                        holder = new viewHolder();
                                        convertView.setTag(holder);

                                        //holder的赋值
                                        holder.time = (TextView) convertView.findViewById(R.id.time);
                                        holder.body = (TextView) convertView.findViewById(R.id.content);
                                        holder.head = (ImageView) convertView.findViewById(R.id.head);
                                    } else {
                                        holder = (viewHolder) convertView.getTag();
                                    }
                                    //得到数据，展示数据

                                } else {
                                    if (convertView == null) {
                                        convertView = View.inflate(ChatActivity.this, R.layout.item_chat_send, null);
                                        holder = new viewHolder();
                                        convertView.setTag(holder);

                                        //holder的赋值
                                        holder.time = (TextView) convertView.findViewById(R.id.time);
                                        holder.body = (TextView) convertView.findViewById(R.id.content);
                                        holder.head = (ImageView) convertView.findViewById(R.id.head);
                                    } else {
                                        holder = (viewHolder) convertView.getTag();
                                    }
                                    //得到数据，展示数据
                                }
                                //得到数据，展示数据(因为两个布局的ID都是相同的，可以将数据展示写在一起)
                                c.moveToPosition(position);
                                String time = c.getString(c.getColumnIndex(SmsOpenHelper.SmsTable.TIME));
                                String body = c.getString(c.getColumnIndex(SmsOpenHelper.SmsTable.BODY));

                                String formatTime = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss").format(new Date(Long.parseLong(time)));

                                holder.time.setText(formatTime);
                                holder.body.setText(body);

                                return super.getView(position, convertView, parent);
                            }

                            @Override
                            public View newView(Context context, Cursor cursor, ViewGroup parent) {
                                return null;
                            }

                            @Override
                            public void bindView(View view, Context context, Cursor cursor) {

                            }

                            class viewHolder {
                                TextView body;
                                TextView time;
                                ImageView head;
                            }
                        };
                        mListView.setAdapter(mAdapter);
                        //滚动到最后一行
                        mListView.setSelection(mAdapter.getCount() - 1);
                    }
                });
            }
        });
    }

    private void initListener() {

    }

    @OnClick(R.id.btn_send)
    public void send(View view) {

        ThreadUtils.runInThread(new Runnable() {
            @Override
            public void run() {
                final String body = mEtBody.getText().toString();
//                Toast.makeText(getApplicationContext(), body, Toast.LENGTH_SHORT).show();
                //3、初始化了一个消息
                Message msg = new Message();
                msg.setFrom(IMService.mCurAccount);//当前登录的用户
                msg.setTo(mClickAccount);
                msg.setBody(body);//输入框里面的内容
                msg.setType(Message.Type.chat);//类型就是chat
                msg.setProperty("key", "value");//额外属性 额外的信息，这里用不到

                // TODO: 调用服务器里面的sendMessage这个方法
                mImService.sendMessage(msg);

                IMService service = new IMService();
                //4、清空输入框
                ThreadUtils.runInUIThread(new Runnable() {
                    @Override
                    public void run() {
                        mEtBody.setText("");
                    }
                });
            }
        });
    }

    @Override
    protected void onDestroy() {
        //反注册监听
        ungisterContentObserver();
        //解绑服务
        if (mMyServiceConnect != null) {
            unbindService(mMyServiceConnect);
        }
        super.onDestroy();
    }

    /**
     * ------------------使用contentObserver时刻监听记录的改变----------------------------
     */
    MyContentObserver mMyContentObserver = new MyContentObserver(new Handler());

    /**
     * 注册监听
     */
    public void registerContentObserver() {
        getContentResolver().registerContentObserver(SmsProvider.URI_SMS, true, mMyContentObserver);
    }

    /**
     * 反注册监听
     */
    public void ungisterContentObserver() {
        getContentResolver().unregisterContentObserver(mMyContentObserver);
    }

    class MyContentObserver extends ContentObserver {

        public MyContentObserver(Handler handler) {
            super(handler);
        }

        @Override
        public void onChange(boolean selfChange, Uri uri) {
            //设置adapter或者notifyAdapter
            setAdapterOrNotify();
            super.onChange(selfChange, uri);
        }
    }

    MyServiceConnect mMyServiceConnect = new MyServiceConnect();//创建MyServiceConnect对象

    class MyServiceConnect implements ServiceConnection {

        @Override
        public void onServiceConnected(ComponentName name, IBinder service) {
            System.out.println("---------------onServiceConnected----------------");
            IMService.Mybinder binder = (IMService.Mybinder) service;
            mImService = binder.getService();
        }

        @Override
        public void onServiceDisconnected(ComponentName name) {
            System.out.println("---------------onServiceDisconnected----------------");
        }
    }
}
