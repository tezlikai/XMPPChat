package com.buaa.tezlikai.xmppchat.srevice;

import android.app.Service;
import android.content.ContentValues;
import android.content.Intent;
import android.os.Binder;
import android.os.IBinder;
import android.support.annotation.Nullable;

import com.buaa.tezlikai.xmppchat.activity.LoginActivity;
import com.buaa.tezlikai.xmppchat.dbhelper.ContactOpenHelper;
import com.buaa.tezlikai.xmppchat.dbhelper.SmsOpenHelper;
import com.buaa.tezlikai.xmppchat.provider.ContactsProvicer;
import com.buaa.tezlikai.xmppchat.provider.SmsProvider;
import com.buaa.tezlikai.xmppchat.utils.PinyinUtil;
import com.buaa.tezlikai.xmppchat.utils.ThreadUtils;
import com.buaa.tezlikai.xmppchat.utils.ToastUtils;

import org.jivesoftware.smack.Chat;
import org.jivesoftware.smack.ChatManager;
import org.jivesoftware.smack.ChatManagerListener;
import org.jivesoftware.smack.MessageListener;
import org.jivesoftware.smack.Roster;
import org.jivesoftware.smack.RosterEntry;
import org.jivesoftware.smack.RosterListener;
import org.jivesoftware.smack.XMPPConnection;
import org.jivesoftware.smack.XMPPException;
import org.jivesoftware.smack.packet.Message;
import org.jivesoftware.smack.packet.Presence;

import java.util.Collection;
import java.util.HashMap;
import java.util.Map;

/**
 * Created by Administrator on 2016/5/15.
 * 保存conn链接对象
 */
public class IMService extends Service {


    public static XMPPConnection conn;
    public static String mCurAccount;

    private Roster mRoster;
    private MyRosterListener mRosterListener;

    private ChatManager mChatManager;
    private Chat mCurChat;

    private Map<String, Chat> mChatMap = new HashMap<>();

    @Nullable
    @Override
    public IBinder onBind(Intent intent) {//调取服务中的方法必须执行此方法
        return new Mybinder();
    }

    public class Mybinder extends Binder {
        //返回service的实例
        public IMService getService() {
            return IMService.this;
        }
    }

    @Override
    public void onCreate() {
        System.out.println("-------------------onCreate---------------------");
        ThreadUtils.runInThread(new Runnable() {
            @Override
            public void run() {
                /*=============== 同步花名册 begin ===============*/
                System.out.println("--------------同步花名册 begin--------------");
                // 需要连接对象
                // 得到花名册对象
                mRoster = IMService.conn.getRoster();

                // 得到所有的联系人
                final Collection<RosterEntry> entries = mRoster.getEntries();

                // 监听联系人的改变
                mRosterListener = new MyRosterListener();
                mRoster.addRosterListener(mRosterListener);

                for (RosterEntry entry : entries) {
                    saveOrUpdateEntry(entry);
                }

                System.out.println("--------------同步花名册 end--------------");
				/*=============== 同步花名册 end ===============*/


				/*=============== 创建消息的管理者 注册监听 begin ===============*/

                //1、获取消息的管理者
                if (mChatManager == null) {
                    mChatManager = IMService.conn.getChatManager();
                }
                //创建会话的时候添加一个ChatManagerListener
                mChatManager.addChatListener(mMyChatManagerListener);

				/*=============== 创建消息的管理者 注册监听 end ===============*/

            }
        });
        super.onCreate();
    }

    @Override
    public int onStartCommand(Intent intent, int flags, int startId) {
        System.out.println("-------------------onStartCommand---------------------");
        return super.onStartCommand(intent, flags, startId);
    }

    @Override
    public void onDestroy() {
        System.out.println("-------------------onDestroy---------------------");
        //移除rosterListener
        if (mRoster != null && mRosterListener != null) {
            mRoster.removeRosterListener(mRosterListener);
        }
        //移除messageListener
        if (mCurChat != null && mMyMessageListener != null) {
            mCurChat.removeMessageListener(mMyMessageListener);
        }
        super.onDestroy();
    }

    /**
     * 保存或更新
     */
    private void saveOrUpdateEntry(RosterEntry entry) {
        ContentValues values = new ContentValues();
        String account = entry.getUser();

        //处理昵称
        String nickname = entry.getName();
        if (nickname == null || "".equals(nickname)) {
            nickname = account.substring(0, account.indexOf("@"));
        }
        values.put(ContactOpenHelper.ContactTable.ACCOUNT, account);
        values.put(ContactOpenHelper.ContactTable.NICKNAME, nickname);
        values.put(ContactOpenHelper.ContactTable.AVATAR, "0");
        values.put(ContactOpenHelper.ContactTable.PINYIN, PinyinUtil.getPinyin(account));

        //先update，后插入 -->重点
        int updateCount = getContentResolver().update(ContactsProvicer.URI_CONTACT, values, ContactOpenHelper.ContactTable.ACCOUNT + "=?", new String[]{account});

        if (updateCount <= 0) {//没有更新到任何记录
            getContentResolver().insert(ContactsProvicer.URI_CONTACT, values);
        }
    }

    class MyRosterListener implements RosterListener {

        @Override
        public void entriesAdded(Collection<String> addresses) {//联系人添加了
            System.out.println("------------------entriesAdded-----------------");
            //对应更新数据库
            for (String address : addresses) {
                RosterEntry entry = mRoster.getEntry(address);
                //要么更新，要么插入
                saveOrUpdateEntry(entry);
            }
        }

        @Override
        public void entriesUpdated(Collection<String> addresses) {//联系人修改了
            System.out.println("------------------entriesUpdated-----------------");
            //对应更新数据库
            for (String address : addresses) {
                RosterEntry entry = mRoster.getEntry(address);
                //要么更新，要么插入
                saveOrUpdateEntry(entry);
            }
        }

        @Override
        public void entriesDeleted(Collection<String> addresses) {//联系人删除啦
            System.out.println("------------------entriesDeleted-----------------");
            //对应更新数据库
            for (String account : addresses) {
                //执行删除操作
                getContentResolver().delete(ContactsProvicer.URI_CONTACT,
                        ContactOpenHelper.ContactTable.ACCOUNT + "=?", new String[]{account});
            }
        }

        @Override
        public void presenceChanged(Presence presence) {//联系人状态改变了
            System.out.println("------------------presenceChanged-----------------");
        }
    }

    /**
     * 发送消息
     */
    public void sendMessage(Message msg) {
        try {
            //2、创建聊天对象
//                chatManager.createChat("被发送对象jid",消息的监听者,用来接受信息的);
            String toAccount = msg.getTo();
            if (mChatMap.containsKey(toAccount)) {
                mCurChat = mChatMap.get(toAccount);
            } else {
                mCurChat = mChatManager.createChat(msg.getTo(), mMyMessageListener);
                mChatMap.put(toAccount, mCurChat);
            }
            //发送消息
            mCurChat.sendMessage(msg);
            //保存消息
            //我（from） --> 美女（to）-->美女
            saveMessage(msg.getTo(), msg);
        } catch (XMPPException e) {
            e.printStackTrace();
        }
    }

    /**
     * 保存信息
     */
    private void saveMessage(String sessionAccount, Message msg) {

        ContentValues values = new ContentValues();

        values.put(SmsOpenHelper.SmsTable.FROM_ACCOUNT,msg.getFrom());
        values.put(SmsOpenHelper.SmsTable.TO_ACCOUNT, msg.getTo());
        values.put(SmsOpenHelper.SmsTable.BODY, msg.getBody());
        values.put(SmsOpenHelper.SmsTable.STATUS, "offline");
        values.put(SmsOpenHelper.SmsTable.TYPE, msg.getType().name());
        values.put(SmsOpenHelper.SmsTable.TIME, System.currentTimeMillis());
        values.put(SmsOpenHelper.SmsTable.SESSION_ACCOUNT, sessionAccount);
        getContentResolver().insert(SmsProvider.URI_SMS, values);
    }


    /**
     * 信息的监听器
     */
    MyMessageListener mMyMessageListener = new MyMessageListener();

    class MyMessageListener implements MessageListener {
        @Override
        public void processMessage(Chat chat, Message message) {
            String body = message.getBody();
            ToastUtils.showToastSafe(getApplicationContext(), body);

            System.out.println("getType:" + message.getType());
            System.out.println("getBody:" + message.getBody());
            System.out.println("getFrom:" + message.getFrom());
            System.out.println("getTo:" + message.getTo());

            //收到消息，保存消息
            String participant = chat.getParticipant();//获得聊天的参与者
//            participant = participant.substring(0, participant.indexOf("@")) + "@" + LoginActivity.SERVICENAME;
//            System.out.println("participant:"+participant);
            saveMessage(participant, message);
        }
    }

    MyChatManagerListener mMyChatManagerListener = new MyChatManagerListener();
    class MyChatManagerListener implements ChatManagerListener {
        public void chatCreated(Chat chat, boolean createdLocally) {
            System.out.println("----------------chatCreated-----------------");

            //判断chat是否才在map里面
            String participant = chat.getParticipant();//参与者是和我聊天的那个人

            //因为别人创建和我自己创建。参与者（与我聊天的人）对应的jid不同
            // 所以要统一处理,保证得到 participant 是一致的
            participant = participant.substring(0, participant.indexOf("@")) + "@" + LoginActivity.SERVICENAME;

//          System.out.println("participant:"+participant);
            if (!mChatMap.containsKey(participant)) {
                //保存chat
                mChatMap.put(participant, chat);
                chat.addMessageListener(mMyMessageListener);
            }
            System.out.println("participant:" + participant);
            if (createdLocally) {
                System.out.println("----------------我创建一个chat-----------------");
            } else {
                System.out.println("----------------别人创建一个chat-----------------");
            }
        }
    }



}
