package com.puredown.obsidian.graph;

public class GraphEdge {
    private final String sourceId;
    private final String targetId;

    public GraphEdge(String sourceId, String targetId) {
        this.sourceId = sourceId;
        this.targetId = targetId;
    }

    public String getSourceId() {
        return sourceId;
    }

    public String getTargetId() {
        return targetId;
    }
}
