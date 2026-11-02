package com.campus.algorithm.mst;

import com.campus.algorithm.GraphFilter;
import com.campus.algorithm.datastructure.DisjointSetUnion;
import com.campus.dto.AlgorithmResult;
import com.campus.model.Edge;
import com.campus.model.Graph;
import com.campus.model.Node;
import org.springframework.stereotype.Component;

import java.util.*;

/**
 * Kruskal's Minimum Spanning Tree (MST) Algorithm using Disjoint Set Union.
 */
@Component
public class KruskalsMst {

    public AlgorithmResult computeMst(
            Graph graph,
            Set<String> blockedEdgeIds,
            boolean wheelchairAccessibleOnly) {

        long startTime = System.nanoTime();
        AlgorithmResult result = new AlgorithmResult();
        result.setAlgorithm("KRUSKALS_MST");

        if (graph == null || graph.getNodes().isEmpty()) {
            result.setExecutionTimeMicros((System.nanoTime() - startTime) / 1_000);
            return result;
        }

        Map<String, Node> nodeMap = GraphFilter.buildNodeMap(graph);
        List<String> validNodeIds = new ArrayList<>();
        for (Node node : graph.getNodes()) {
            if (GraphFilter.isNodeAllowed(node, wheelchairAccessibleOnly)) {
                validNodeIds.add(node.getId());
            }
        }

        DisjointSetUnion dsu = new DisjointSetUnion();
        dsu.makeSet(validNodeIds);

        List<Edge> sortedEdges = new ArrayList<>();
        for (Edge edge : graph.getEdges()) {
            if (!GraphFilter.isEdgeAllowed(edge, blockedEdgeIds)) {
                continue;
            }
            Node uNode = nodeMap.get(edge.getSource());
            Node vNode = nodeMap.get(edge.getTarget());
            if (GraphFilter.isNodeAllowed(uNode, wheelchairAccessibleOnly) &&
                GraphFilter.isNodeAllowed(vNode, wheelchairAccessibleOnly)) {
                sortedEdges.add(edge);
            }
        }

        sortedEdges.sort(Comparator.comparingDouble(Edge::getWeight));

        List<Edge> mstEdges = new ArrayList<>();
        double totalWeight = 0.0;

        for (Edge edge : sortedEdges) {
            String u = edge.getSource();
            String v = edge.getTarget();

            if (dsu.union(u, v)) {
                mstEdges.add(edge);
                totalWeight += edge.getWeight();
            }
        }

        result.setPathEdges(mstEdges);
        result.setTotalWeight(totalWeight);
        result.setConnectedComponentsCount(dsu.getComponentCount());
        result.setReachable(dsu.getComponentCount() == 1);
        result.setExecutionTimeMicros((System.nanoTime() - startTime) / 1_000);
        return result;
    }
}