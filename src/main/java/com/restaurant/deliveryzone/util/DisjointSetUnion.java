package com.restaurant.deliveryzone.util;

public final class DisjointSetUnion {

    private final int[] parent;
    private final byte[] rank;

    public DisjointSetUnion(int size) {
        this.parent = new int[size];
        this.rank = new byte[size];

        for (int i = 0; i < size; i++) {
            parent[i] = i;
        }
    }

    public int find(int value) {
        int root = value;

        while (parent[root] != root) {
            root = parent[root];
        }

        while (parent[value] != value) {
            int next = parent[value];
            parent[value] = root;
            value = next;
        }

        return root;
    }

    public void union(int first, int second) {
        int rootFirst = find(first);
        int rootSecond = find(second);

        if (rootFirst == rootSecond) {
            return;
        }

        if (rank[rootFirst] < rank[rootSecond]) {
            parent[rootFirst] = rootSecond;
        } else if (rank[rootFirst] > rank[rootSecond]) {
            parent[rootSecond] = rootFirst;
        } else {
            parent[rootSecond] = rootFirst;
            rank[rootFirst]++;
        }
    }
}
