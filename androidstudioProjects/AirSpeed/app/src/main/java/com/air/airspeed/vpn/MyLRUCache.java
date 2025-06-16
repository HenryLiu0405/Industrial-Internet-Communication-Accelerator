package com.air.airspeed.vpn;

import java.util.LinkedHashMap;

//created by liu 2020-01-10

public class MyLRUCache<K, V> extends LinkedHashMap<K, V> {
    private int maxSize;
    private transient CleanupCallback< V> callback;

    public MyLRUCache(int maxSize, CleanupCallback<V> callback) {
        super(maxSize + 1, 1, true);

        this.maxSize = maxSize;
        this.callback = callback;
    }


    @Override
    protected boolean removeEldestEntry(Entry<K, V> eldest) {
        if (size() > maxSize) {
            callback.cleanUp(eldest.getValue());
            return true;
        }
        return false;
    }

    public interface CleanupCallback<V> {
        //清除对象
        void cleanUp(V v);
    }
}
