package com.campus.algorithm.datastructure;

import java.util.Collection;
import java.util.HashMap;
import java.util.Map;

/**
 * Disjoint Set Union (DSU / Union-Find) with path compression and union by rank.
 */
public class DisjointSetUnion {
    private final Map<String, String> parent = new HashMap<>();
    private final Map<String, Integer> rank = new HashMap<>();
    private int componentCount = 0;

    public void makeSet(Collection<String> nodeIds) {
        parent.clear();
        rank.clear();
        componentCount = 0;
        for (String id : nodeIds) {
            parent.put(id, id);
            rank.put(id, 0);
            componentCount++;
        }
    }

    public String find(String id) {
        String root = parent.get(id);
        if (root == null) {
            return null;
        }
        if (!root.equals(id)) {
            parent.put(id, find(root)); // Path compression
            root = parent.get(id);
        }
        return root;
    }

    public boolean union(String a, String b) {
        String rootA = find(a);
        String rootB = find(b);

        if (rootA == null || rootB == null) {
            return false;
        }
        if (rootA.equals(rootB)) {
            return false; // Already in the same set (cycle detected)
        }

        int rankA = rank.get(rootA);
        int rankB = rank.get(rootB);

        if (rankA < rankB) {
            parent.put(rootA, rootB);
        } else if (rankA > rankB) {
            parent.put(rootB, rootA);
        } else {
            parent.put(rootB, rootA);
            rank.put(rootA, rankA + 1);
        }
        componentCount--;
        return true;
    }

    public int getComponentCount() {
        return componentCount;
    }
}