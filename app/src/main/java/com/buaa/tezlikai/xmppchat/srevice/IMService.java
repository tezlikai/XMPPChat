package com.buaa.tezlikai.xmppchat.srevice;

import android.app.Service;
import android.content.ContentValues;
import android.content.Intent;
import android.os.IBinder;
import android.support.annotation.Nullable;

import com.buaa.tezlikai.xmppchat.dbhelper.ContactOpenHelper;
import com.buaa.tezlikai.xmppchat.provider.ContactsProvicer;
import com.buaa.tezlikai.xmppchat.utils.PinyinUtil;
import com.buaa.tezlikai.xmppchat.utils.ThreadUtils;

import org.jivesoftware.smack.Roster;
import org.jivesoftware.smack.RosterEntry;
import org.jivesoftware.smack.RosterListener;
import org.jivesoftware.smack.XMPPConnection;
import org.jivesoftware.smack.packet.Presence;

import java.util.Collection;

/**
 * Created by Administrator on 2016/5/15.
 * 保存conn链接对象
 */
public class IMService extends Service{


    public static XMPPConnection	conn;
    private Roster mRoster;
    private MyRosterListener		mRosterListener;

    @Nullable
    @Override
    public IBinder onBind(Intent intent) {
        return null;
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
}
