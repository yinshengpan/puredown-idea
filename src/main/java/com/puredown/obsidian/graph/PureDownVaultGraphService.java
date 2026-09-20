package com.puredown.obsidian.graph;

import com.intellij.openapi.project.Project;
import com.intellij.openapi.vfs.VirtualFile;
import com.intellij.psi.search.FileTypeIndex;
import com.intellij.psi.search.GlobalSearchScope;
import com.puredown.PureDownFileType;

import java.nio.charset.StandardCharsets;
import java.util.*;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

public final class PureDownVaultGraphService {
    private static final Pattern WIKILINK_PATTERN = Pattern.compile("\\[\\[([^|\\]]+)(?:\\|([^\\]]+))?\\]\\]");
    private static final Pattern MD_LINK_PATTERN = Pattern.compile("\\[[^\\]]+\\]\\(([^\\)]+\\.md)\\)");

    private PureDownVaultGraphService() {}

    public static VaultGraphData buildGraph(Project project) {
        Collection<VirtualFile> files = FileTypeIndex.getFiles(PureDownFileType.INSTANCE, GlobalSearchScope.projectScope(project));
        Map<String, GraphNode> nodesMap = new LinkedHashMap<>();
        List<GraphEdge> edges = new ArrayList<>();

        for (VirtualFile file : files) {
            String id = file.getNameWithoutExtension().toLowerCase();
            nodesMap.put(id, new GraphNode(id, file.getNameWithoutExtension(), file));
        }

        for (VirtualFile file : files) {
            String sourceId = file.getNameWithoutExtension().toLowerCase();
            try {
                String content = new String(file.contentsToByteArray(), StandardCharsets.UTF_8);

                // Wikilinks
                Matcher wikiMatcher = WIKILINK_PATTERN.matcher(content);
                while (wikiMatcher.find()) {
                    String rawTarget = wikiMatcher.group(1).split("#")[0].trim().toLowerCase();
                    if (!rawTarget.isEmpty() && !rawTarget.equals(sourceId)) {
                        edges.add(new GraphEdge(sourceId, rawTarget));
                    }
                }

                // Markdown links
                Matcher mdMatcher = MD_LINK_PATTERN.matcher(content);
                while (mdMatcher.find()) {
                    String rawPath = mdMatcher.group(1);
                    int slashIdx = rawPath.lastIndexOf('/');
                    String filename = (slashIdx != -1) ? rawPath.substring(slashIdx + 1) : rawPath;
                    String targetName = filename.replace(".md", "").trim().toLowerCase();
                    if (!targetName.isEmpty() && !targetName.equals(sourceId)) {
                        edges.add(new GraphEdge(sourceId, targetName));
                    }
                }
            } catch (Exception ignored) {}
        }

        List<GraphNode> nodes = new ArrayList<>(nodesMap.values());
        int count = nodes.size();
        double radius = Math.max(180.0, count * 35.0);
        for (int i = 0; i < count; i++) {
            double angle = 2.0 * Math.PI * i / Math.max(1, count);
            nodes.get(i).x = Math.cos(angle) * radius;
            nodes.get(i).y = Math.sin(angle) * radius;
        }

        return new VaultGraphData(nodes, edges);
    }
}
