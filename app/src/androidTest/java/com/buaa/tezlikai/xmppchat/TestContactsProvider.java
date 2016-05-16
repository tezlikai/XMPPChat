package com.buaa.tezlikai.xmppchat;

import android.content.ContentValues;
import android.database.Cursor;
import android.test.AndroidTestCase;

import com.buaa.tezlikai.xmppchat.dbhelper.ContactOpenHelper;
import com.buaa.tezlikai.xmppchat.provider.ContactsProvicer;

/**
 * Created by Administrator on 2016/5/16.
 */
public class TestContactsProvider extends AndroidTestCase {

    public void testInsert(){
        ContentValues values = new ContentValues();
        values.put(ContactOpenHelper.ContactTable.ACCOUNT,"lk@buaa.com");
        values.put(ContactOpenHelper.ContactTable.NICKNAME,"lk");
        values.put(ContactOpenHelper.ContactTable.AVATAR,"0");//头像
        values.put(ContactOpenHelper.ContactTable.PINYIN,"laogao");//拼音
        getContext().getContentResolver().insert(ContactsProvicer.URI_CONTACT,values);

    }
    public void testDelete(){
        getContext().getContentResolver().delete(ContactsProvicer.URI_CONTACT,ContactOpenHelper.ContactTable.ACCOUNT + "=?", new String[]{"lk@buaa.com"});

    }
    public void testUpdate(){
        ContentValues values = new ContentValues();
        values.put(ContactOpenHelper.ContactTable.ACCOUNT,"lk@buaa.com");
        values.put(ContactOpenHelper.ContactTable.NICKNAME,"李凯");
        values.put(ContactOpenHelper.ContactTable.AVATAR,"0");//头像
        values.put(ContactOpenHelper.ContactTable.PINYIN,"laogao");//拼音
        getContext().getContentResolver().update(ContactsProvicer.URI_CONTACT,values,ContactOpenHelper.ContactTable.ACCOUNT+"=?",new String[]{"lk@buaa.com"});
    }
    public void testQuery(){
        Cursor cursor = getContext().getContentResolver().query(ContactsProvicer.URI_CONTACT, null, null, null, null);
        int columnCount = cursor.getColumnCount();//统计多少行数
        while (cursor.moveToNext()){
            //循环打印列
            for (int i = 0; i <columnCount; i++) {
                System.out.println(cursor.getString(i) + " ");
            }
            System.out.println("");
        }
    }

}
