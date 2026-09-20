package com.puredown.obsidian.graph;

import java.util.List;

public class VaultGraphData {
    private final List<GraphNode> nodes;
    private final List<GraphEdge> edges;

    public VaultGraphData(List<GraphNode> nodes, List<GraphEdge> edges) {
        this.nodes = nodes;
        this.edges = edges;
    }

    public List<GraphNode> getNodes() {
        return nodes;
    }

    public List<GraphEdge> getEdges() {
        return edges;
    }
}
