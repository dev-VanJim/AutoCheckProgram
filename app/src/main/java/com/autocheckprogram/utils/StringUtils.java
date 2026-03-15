package com.autocheckprogram.utils;

public class StringUtils {
    /**
     * 截取出关键信息。
     *
     * @param entireString 待截取字符串。
     * @param separator    分割符。
     * @return 截取出的在最后一个分隔符后的字串。
     */
    public static String getLastSubstring(String entireString, int separator) {

        int lastIndexOf = entireString.lastIndexOf(separator);
        return entireString.substring(++lastIndexOf);
    }
}
