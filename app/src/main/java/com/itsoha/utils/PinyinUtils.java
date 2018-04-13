package com.itsoha.utils;

import opensource.jpinyin.PinyinFormat;
import opensource.jpinyin.PinyinHelper;

public class PinyinUtils {
    /**
     * 字符串转拼音
     */
    public static String getPinyin(String string) {
        return PinyinHelper.convertToPinyinString(string, "", PinyinFormat.WITHOUT_TONE);
    }
}
