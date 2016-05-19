package com.buaa.tezlikai.xmppchat.provider;

import android.content.ContentProvider;
import android.content.ContentUris;
import android.content.ContentValues;
import android.content.UriMatcher;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.net.Uri;
import android.support.annotation.Nullable;

import com.buaa.tezlikai.xmppchat.dbhelper.SmsOpenHelper;

/**
 * Created by Administrator on 2016/5/17.
 */
public class SmsProvider extends ContentProvider {

    private static final String AUTHORITIES = SmsProvider.class.getCanonicalName();

    static UriMatcher sUriMatcher;

    public static Uri URI_SESSION = Uri.parse("content://"+AUTHORITIES+"/session");
    public static Uri URI_SMS = Uri.parse("content://"+AUTHORITIES+"/sms");

    private static final int SMS = 1;
    private static final int SESSION = 2;

    static {
        sUriMatcher = new UriMatcher(UriMatcher.NO_MATCH);
        //添加匹配
        sUriMatcher.addURI(AUTHORITIES,"/sms",SMS);
        sUriMatcher.addURI(AUTHORITIES,"/session",SESSION);
    }

    private SmsProvider mMHelper;
    private SmsOpenHelper mHelper;

    @Override
    public boolean onCreate() {
        //创建表，创建数据库
        mHelper = new SmsOpenHelper(getContext());
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

    /**===================== CRUD begin==========================*/
    @Nullable
    @Override
    public Uri insert(Uri uri, ContentValues values) {
        switch (sUriMatcher.match(uri)){
            case SMS:
                //具体插入几条数据
                long id = mHelper.getWritableDatabase().insert(SmsOpenHelper.T_SMS, "", values);

                if (id > 0){
                    System.out.println("---------------- SmsProvider insertSuccess --------------------");
                    uri = ContentUris.withAppendedId(uri,id);//拼接成最新的uri
                    //发送数据改变的信号
                    getContext().getContentResolver().notifyChange(SmsProvider.URI_SMS,null);
                }
                break;
            default:
                break;
        }
        return uri;
    }

    @Override
    public int delete(Uri uri, String selection, String[] selectionArgs) {
        int deleteCount = 0;
        switch (sUriMatcher.match(uri)){
            case SMS:
                //具体删除了几条数据
                deleteCount = mHelper.getWritableDatabase().delete(SmsOpenHelper.T_SMS, selection, selectionArgs);
                if (deleteCount > 0){
                    System.out.println("---------------- SmsProvider deleteSuccess --------------------");
                    //发送数据改变的信号
                    getContext().getContentResolver().notifyChange(SmsProvider.URI_SMS, null);
                }
                break;
            default:
                break;
        }
        return deleteCount;
    }

    @Override
    public int update(Uri uri, ContentValues values, String selection, String[] selectionArgs) {
        int updateCount = 0;
        switch (sUriMatcher.match(uri)){
            case SMS:
                //具体更新多少条数据
                updateCount = mHelper.getWritableDatabase().update(SmsOpenHelper.T_SMS, values, selection, selectionArgs);
                if (updateCount > 0){
                    System.out.println("---------------- SmsProvider updateSuccess --------------------");
                    //发送数据改变的信号
                    getContext().getContentResolver().notifyChange(SmsProvider.URI_SMS, null);
                }
                break;
            default:
                break;
        }
        return updateCount;
    }

    @Nullable
    @Override
    public Cursor query(Uri uri, String[] projection, String selection, String[] selectionArgs, String sortOrder) {
        Cursor cursor = null;
        switch (sUriMatcher.match(uri)){
            case SMS:
                cursor = mHelper.getReadableDatabase().query(SmsOpenHelper.T_SMS, projection, selection, selectionArgs, null, null, sortOrder);
                System.out.println("---------------- SmsProvider querySuccess --------------------");
                break;

            case SESSION:
                SQLiteDatabase db = mHelper.getReadableDatabase();
                cursor = db.rawQuery("SELECT * FROM "//
                        + "(SELECT * FROM t_sms WHERE from_account = ? or to_account = ? ORDER BY time ASC)" //
                        + " GROUP BY session_account", selectionArgs);//
            default:
                break;
        }
        return cursor;
    }
    /**===================== CRUD end==========================*/
}
