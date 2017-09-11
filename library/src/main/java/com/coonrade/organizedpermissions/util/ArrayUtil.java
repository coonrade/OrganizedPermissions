package com.coonrade.organizedpermissions.util;

import java.util.List;

public class ArrayUtil {
    // Use generic type in the future
    public static String[] listToStringArray(List<String> list) {
        return list.toArray(new String[list.size()]);
    }

    public static Integer[] listToIntegerArray(List<Integer> list) {
        return list.toArray(new Integer[list.size()]);
    }


}
