package com.moviting.android.util;

import java.util.LinkedHashMap;
import java.util.Map;
import java.util.Set;

/**
 * Created by jongseonglee on 10/21/16.
 */

public class MyHashMap<K,V> extends LinkedHashMap<K,V> {
    public MyHashMap(Map<K,V> map) {
        super(map);
    }

    public V getValue(int i)
    {

        Map.Entry<K, V>entry = this.getEntry(i);
        if(entry == null) return null;

        return entry.getValue();
    }

    public Map.Entry<K, V> getEntry(int i)
    {
        // check if negetive index provided
        Set<Entry<K,V>> entries = entrySet();
        int j = 0;

        for(Map.Entry<K, V>entry : entries)
            if(j++ == i)return entry;

        return null;

    }
}
