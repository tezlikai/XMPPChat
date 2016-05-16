package com.buaa.tezlikai.xmppchat.provider;

import android.content.ContentProvider;
import android.content.ContentUris;
import android.content.ContentValues;
import android.content.UriMatcher;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.net.Uri;
import android.support.annotation.Nullable;

import com.buaa.tezlikai.xmppchat.dbhelper.ContactOpenHelper;

/**
 * Created by Administrator on 2016/5/16.
 */
public class ContactsProvicer extends ContentProvider {
    //得到一个类的完整路径
    public static final String AUTHORITIES = ContactsProvicer.class.getCanonicalName();

    //地址匹配对象
    public static UriMatcher sUriMatcher;

    //对应联系人的一个uri常量
    public static Uri URI_CONTACT = Uri.parse("content://" + AUTHORITIES + "/contact");

    private static final int CONTACT = 1;

    static {
        sUriMatcher = new UriMatcher(UriMatcher.NO_MATCH);

        //添加一个匹配规则
        sUriMatcher.addURI(AUTHORITIES, "/contact", CONTACT);
        //contact://com.buaa.tezlikai.xmppchat.provider.ContactsProvicer/contact
    }

    private ContactOpenHelper mHelper;


    @Override
    public boolean onCreate() {
        mHelper = new ContactOpenHelper(getContext());
        if (mHelper != null){
            return true;
        }
        return false;
    }

    @Nullable
    @Override
    public String getType(Uri uri) {
        return null;
    }

    /**
     * ------------------------------- crud begin ------------------------------------
     */
    @Nullable
    @Override
    public Uri insert(Uri uri, ContentValues values) {
        //数据是存到sqlite --> 创建db文件，建立表 --> sqliteOpenHelper
        int code = sUriMatcher.match(uri);
        switch (code){
            case CONTACT:
                SQLiteDatabase db = mHelper.getWritableDatabase();
                //新插入的id
                long id = db.insert(ContactOpenHelper.T_CONTACT, "", values);
                if (id != -1){
                    System.out.println("----------------ContactProvides insertSuccess-----------------");
                    //拼接最新的uri
                    uri = ContentUris.withAppendedId(uri,id);
                    //通知ContentObserver数据改变了
//                    getContext().getContentResolver().notifyChange(ContactsProvicer.URI_CONTACT,"指定只有某一个observer可以收到");
                    getContext().getContentResolver().notifyChange(ContactsProvicer.URI_CONTACT,null);//为null就是所有都可以收到
                }
                break;
            default:
                break;
        }
        return uri;
    }

    @Override
    public int delete(Uri uri, String selection, String[] selectionArgs) {
        int mDeleteCount = 0;//删除多少行
        int code = sUriMatcher.match(uri);
        switch (code){
            case CONTACT:
                SQLiteDatabase db = mHelper.getWritableDatabase();
                mDeleteCount = db.delete(ContactOpenHelper.T_CONTACT, selection, selectionArgs);
                if (mDeleteCount > 0 ){
                    System.out.println("----------------ContactProvides deleteSuccess-----------------");
                    //通知ContentObserver数据改变了
//                    getContext().getContentResolver().notifyChange(ContactsProvicer.URI_CONTACT,"指定只有某一个observer可以收到");
                    getContext().getContentResolver().notifyChange(ContactsProvicer.URI_CONTACT, null);//为null就是所有都可以收到
                }
                break;
            default:
                break;
        }
        return mDeleteCount;
    }
    @Override
    public int update(Uri uri, ContentValues values, String selection, String[] selectionArgs) {
        int updateCounut = 0;//删除多少行
        int code = sUriMatcher.match(uri);
        switch (code){
            case CONTACT:
                SQLiteDatabase db = mHelper.getWritableDatabase();
                updateCounut = db.update(ContactOpenHelper.T_CONTACT, values, selection, selectionArgs);
                if (updateCounut > 0 ){
                    System.out.println("----------------ContactProvides updateSuccess-----------------");
                    //通知ContentObserver数据改变了
//                    getContext().getContentResolver().notifyChange(ContactsProvicer.URI_CONTACT,"指定只有某一个observer可以收到");
                    getContext().getContentResolver().notifyChange(ContactsProvicer.URI_CONTACT, null);//为null就是所有都可以收到
                }
                break;
            default:
                break;
        }
        return updateCounut;
    }

    @Nullable
    @Override
    public Cursor query(Uri uri, String[] projection, String selection, String[] selectionArgs, String sortOrder) {
        Cursor cursor = null;
        int code = sUriMatcher.match(uri);
        switch (code){
            case CONTACT:
                SQLiteDatabase db = mHelper.getReadableDatabase();
                cursor = db.query(ContactOpenHelper.T_CONTACT, projection, selection, selectionArgs, null, null, sortOrder);
                System.out.println("----------------ContactProvides querySuccess-----------------");
                //通知ContentObserver数据改变了
//                    getContext().getContentResolver().notifyChange(ContactsProvicer.URI_CONTACT,"指定只有某一个observer可以收到");
                getContext().getContentResolver().notifyChange(ContactsProvicer.URI_CONTACT, null);//为null就是所有都可以收到
                break;
            default:
                break;
        }
        return cursor;
    }
    /**------------------------------- crud end ------------------------------------*/
}
