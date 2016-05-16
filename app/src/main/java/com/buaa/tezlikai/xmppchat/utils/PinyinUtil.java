package com.buaa.tezlikai.xmppchat.utils;

import opensource.jpinyin.PinyinFormat;
import opensource.jpinyin.PinyinHelper;

/**
 * Created by Administrator on 2016/5/16.
 */
public class PinyinUtil {
    public static String getPinyin(String str){
        //将汉字装换成拼音
        return PinyinHelper.convertToPinyinString(str,"", PinyinFormat.WITHOUT_TONE);
    }
}
