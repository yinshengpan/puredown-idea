package com.puredown.obsidian.graph;

import com.intellij.openapi.vfs.VirtualFile;

public class GraphNode {
    private final String id;
    private final String name;
    private final VirtualFile file;
    public double x = 0;
    public double y = 0;
    public double vx = 0;
    public double vy = 0;
    public double radius = 22;

    public GraphNode(String id, String name, VirtualFile file) {
        this.id = id;
        this.name = name;
        this.file = file;
    }

    public String getId() {
        return id;
    }

    public String getName() {
        return name;
    }

    public VirtualFile getFile() {
        return file;
    }
}
