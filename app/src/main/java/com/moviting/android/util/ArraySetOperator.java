package com.moviting.android.util;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.Set;

/**
 * Created by jongseonglee on 10/26/16.
 */

public class ArraySetOperator {

    public static <T> ArrayList<T> union(ArrayList<T> list1, ArrayList<T> list2) {
        Set<T> set = new HashSet<T>();

        set.addAll(list1);
        set.addAll(list2);

        return new ArrayList<T>(set);
    }

    public static <T> ArrayList<T> intersection(ArrayList<T> list1, ArrayList<T> list2) {
        ArrayList<T> list = new ArrayList<T>();

        for (T t : list1) {
            if(list2.contains(t)) {
                list.add(t);
            }
        }

        return list;
    }
}
