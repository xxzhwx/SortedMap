package com.xxzhwx;

public class Node<K extends Comparable<K>, V> {
    K key;
    V value;
    Level[] levels;

    public Node(K key, V value, int level) {
        this.key = key;
        this.value = value;
        this.levels = new Level[level];

        for (int i = 0; i < levels.length; ++i) {
            levels[i] = new Level(null, 0);
        }
    }
}
