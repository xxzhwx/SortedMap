package com.xxzhwx;

import java.util.Random;

/**
 * Reference:
 * http://www.cnblogs.com/WJ5888/p/4595306.html
 * https://github.com/aeroditya21/Skip-List/tree/master/Skip%20List%20Implementation/src/skiplist
 * https://github.com/antirez/redis/blob/unstable/src/t_zset.c
 */

@SuppressWarnings("unchecked")
public class SortedMap<K extends Comparable<K>, V> {
    private static final int MAX_LEVEL = 32;
    private static final Random RANDOM = new Random();

    Node<K, V> head;
    int level;
    int length;

    SortedMap() {
        head = new Node<>(null, null, MAX_LEVEL);
        level = 1;
        length = 0;
    }

    boolean insert(K key, V value) {
        Node<K, V> temp = head;
        int[] rank = new int[MAX_LEVEL];
        Node[] prevNode = new Node[MAX_LEVEL];

        // Keep moving down till the lowest level
        int i;
        for (i = level-1; i >= 0; i--) {
            rank[i] = (i == level-1) ? 0 : rank[i+1];
            // Keep moving forward till next node is greater than search key
            while (temp.levels[i].forward != null
                    && temp.levels[i].forward.key.compareTo(key) < 0) {
                rank[i] += temp.levels[i].span;
                temp = temp.levels[i].forward;
            }
            prevNode[i] = temp; // Keep record of previous nodes at each level
        }

        // Node is already present
        if (temp.levels[0].forward != null
                && temp.levels[0].forward.key.compareTo(key) == 0) {
            return false;
        }

        int lv = randomLevel();
        if (lv > level) {
            for (i = level; i < lv; i++) {
                rank[i] = 0;
                prevNode[i] = head;
                prevNode[i].levels[i].span = length;
            }
            level = lv;
        }

        temp = new Node<>(key, value, lv);

        for (i = 0; i < lv; i++) {
            temp.levels[i].forward = prevNode[i].levels[i].forward;
            prevNode[i].levels[i].forward = temp;

            /* update span covered by prevNode[i] as x is inserted here */
            temp.levels[i].span = prevNode[i].levels[i].span - (rank[0] - rank[i]);
            prevNode[i].levels[i].span = (rank[0] - rank[i]) + 1;
        }

        /* increment span for untouched levels */
        for (i = lv; i < level; i++) {
            prevNode[i].levels[i].span++;
        }

        length++;
        return true;
    }

    void remove(K key) {
        Node<K, V> temp = head;
        Node[] prevNode = new Node[MAX_LEVEL];

        int i;
        for (i = level-1; i >= 0; i--) {
            while (temp.levels[i].forward != null
                    && temp.levels[i].forward.key.compareTo(key) < 0) {
                temp = temp.levels[i].forward;
            }
            prevNode[i] = temp;
        }

        if (temp.levels[0].forward == null
                || temp.levels[0].forward.key.compareTo(key) != 0) {
            return;
        }

        temp = temp.levels[0].forward;

        for (i = 0; i < level; i++) {
            if (prevNode[i].levels[i].forward == temp) {
                prevNode[i].levels[i].span += temp.levels[i].span - 1;
                prevNode[i].levels[i].forward = temp.levels[i].forward;
            } else {
                prevNode[i].levels[i].span -= 1;
            }
        }
        while (level > 1 && head.levels[level-1].forward == null) {
            level--;
        }
        length--;
    }

    V find(K key) {
        Node<K, V> temp = head;
        for (int i = level-1; i >= 0; --i) {
            while (temp.levels[i].forward != null
                    && temp.levels[i].forward.key.compareTo(key) < 0) {
                temp = temp.levels[i].forward;
            }
        }

        if (temp.levels[0].forward != null
                && temp.levels[0].forward.key.compareTo(key) == 0) {
            return (V) temp.levels[0].forward.value;
        }
        return null;
    }

    V findByRank(int rank) {
        int traversed = 0;
        Node<K, V> temp = head;
        for (int i = level-1; i >= 0; --i) {
            while (temp.levels[i].forward != null
                    && traversed + temp.levels[i].span < rank) {
                traversed += temp.levels[i].span;
                temp = temp.levels[i].forward;
            }
        }
        if (temp.levels[0].forward != null
                && traversed + temp.levels[0].span == rank) {
            return (V) temp.levels[0].forward.value;
        }
        return null;
    }

    int getRank(K key) {
        int rank = 0;
        Node<K, V> temp = head;
        for (int i = level-1; i >= 0; --i) {
            while (temp.levels[i].forward != null
                    && temp.levels[i].forward.key.compareTo(key) < 0) {
                rank += temp.levels[i].span;
                temp = temp.levels[i].forward;
            }
        }
        if (temp.levels[0].forward != null
                && temp.levels[0].forward.key.compareTo(key) == 0) {
            rank += temp.levels[0].span;
            return rank;
        }
        return 0;
    }

    int getLength() {
        return length;
    }

    private void dump() {
        System.out.println("---- SortedMap ----");
        Node<K, V> temp = head.levels[0].forward;
        while (temp != null) {
            System.out.println(getRank(temp.key) + ">" + temp.key + ":" + temp.value + "[" + temp.levels.length + "]");
            temp = temp.levels[0].forward;
        }
        System.out.println("-------------------");
    }

    private static int randomLevel() {
        return 1 + RANDOM.nextInt(MAX_LEVEL);
    }

    public static void main(String[] args) {
        SortedMap<Integer, String> sm = new SortedMap<>();
        for (int i = 99; i > 0; i--) {
            sm.insert(i, ""+i+"xx");
        }

//        Random random = new Random();
//        for (int i = 0, n = 99; i < n; i++) {
//            int r = random.nextInt(100);
//            sm.remove(r);
//        }
//        for (int i = 0, n = 99; i < n; i++) {
//            int r = random.nextInt(100);
//            sm.insert(r, "new-" + r);
//        }
        sm.dump();

        System.out.println("length: " + sm.getLength());

        System.out.println("rank of key 2: " + sm.getRank(2));
        System.out.println("rank of key 99: " + sm.getRank(99));
        System.out.println("rank of key 100: " + sm.getRank(100));

        System.out.println("find value of key 1: " + sm.find(1));
        System.out.println("find value of key 4: " + sm.find(4));
        System.out.println("find value of key 5: " + sm.find(5));
        System.out.println("find value of key 100: " + sm.find(100));

        System.out.println("find by rank 1: " + sm.findByRank(1));
        System.out.println("find by rank 4: " + sm.findByRank(4));
        System.out.println("find by rank 5: " + sm.findByRank(5));
        System.out.println("find by rank 100: " + sm.findByRank(100));
    }
}
