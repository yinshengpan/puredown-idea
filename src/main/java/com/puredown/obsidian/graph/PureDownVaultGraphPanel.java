package com.puredown.obsidian.graph;

import com.intellij.openapi.fileEditor.FileEditorManager;
import com.intellij.openapi.project.Project;
import com.intellij.ui.JBColor;
import com.intellij.util.ui.UIUtil;

import javax.swing.*;
import java.awt.*;
import java.awt.event.*;
import java.awt.geom.Ellipse2D;
import java.awt.geom.Line2D;
import java.util.HashMap;
import java.util.Map;

public class PureDownVaultGraphPanel extends JPanel {

    private final Project project;
    private final VaultGraphData graphData;

    private double zoom = 1.0;
    private double panX = 0;
    private double panY = 0;
    private int lastMouseX = 0;
    private int lastMouseY = 0;

    private GraphNode hoveredNode = null;
    private GraphNode selectedNode = null;
    private String searchQuery = "";

    private Timer simulationTimer;
    private int simulationSteps = 0;

    public PureDownVaultGraphPanel(Project project, VaultGraphData graphData) {
        this.project = project;
        this.graphData = graphData;

        setBackground(!JBColor.isBright() ? new Color(30, 31, 34) : new Color(248, 249, 250));
        setFocusable(true);

        MouseAdapter mouseAdapter = new MouseAdapter() {
            @Override
            public void mousePressed(MouseEvent e) {
                lastMouseX = e.getX();
                lastMouseY = e.getY();
                selectedNode = findNodeAt(e.getX(), e.getY());
                repaint();
            }

            @Override
            public void mouseClicked(MouseEvent e) {
                if (e.getClickCount() == 2) {
                    GraphNode clicked = findNodeAt(e.getX(), e.getY());
                    if (clicked != null) {
                        FileEditorManager.getInstance(project).openFile(clicked.getFile(), true);
                    }
                }
            }

            @Override
            public void mouseMoved(MouseEvent e) {
                GraphNode node = findNodeAt(e.getX(), e.getY());
                if (node != hoveredNode) {
                    hoveredNode = node;
                    setCursor(node != null ? Cursor.getPredefinedCursor(Cursor.HAND_CURSOR) : Cursor.getDefaultCursor());
                    repaint();
                }
            }

            @Override
            public void mouseDragged(MouseEvent e) {
                int dx = e.getX() - lastMouseX;
                int dy = e.getY() - lastMouseY;
                panX += dx;
                panY += dy;
                lastMouseX = e.getX();
                lastMouseY = e.getY();
                repaint();
            }

            @Override
            public void mouseWheelMoved(MouseWheelEvent e) {
                double factor = e.getWheelRotation() < 0 ? 1.1 : 0.9;
                zoom = Math.max(0.2, Math.min(4.0, zoom * factor));
                repaint();
            }
        };

        addMouseListener(mouseAdapter);
        addMouseMotionListener(mouseAdapter);
        addMouseWheelListener(mouseAdapter);

        startSimulation();
    }

    private void startSimulation() {
        simulationSteps = 0;
        simulationTimer = new Timer(16, e -> {
            stepPhysics();
            repaint();
            simulationSteps++;
            if (simulationSteps > 120) {
                simulationTimer.stop();
            }
        });
        simulationTimer.start();
    }

    private void stepPhysics() {
        java.util.List<GraphNode> nodes = graphData.getNodes();
        java.util.List<GraphEdge> edges = graphData.getEdges();
        double k = 100.0;

        for (int i = 0; i < nodes.size(); i++) {
            for (int j = i + 1; j < nodes.size(); j++) {
                GraphNode n1 = nodes.get(i);
                GraphNode n2 = nodes.get(j);
                double dx = n2.x - n1.x;
                double dy = n2.y - n1.y;
                double dist = Math.max(1.0, Math.hypot(dx, dy));
                double force = (k * k) / dist;
                double fx = (dx / dist) * force * 0.05;
                double fy = (dy / dist) * force * 0.05;
                n1.vx -= fx;
                n1.vy -= fy;
                n2.vx += fx;
                n2.vy += fy;
            }
        }

        Map<String, GraphNode> map = new HashMap<>();
        for (GraphNode n : nodes) {
            map.put(n.getId(), n);
        }

        for (GraphEdge edge : edges) {
            GraphNode n1 = map.get(edge.getSourceId());
            GraphNode n2 = map.get(edge.getTargetId());
            if (n1 == null || n2 == null) continue;

            double dx = n2.x - n1.x;
            double dy = n2.y - n1.y;
            double dist = Math.max(1.0, Math.hypot(dx, dy));
            double force = (dist * dist) / k;
            double fx = (dx / dist) * force * 0.015;
            double fy = (dy / dist) * force * 0.015;
            n1.vx += fx;
            n1.vy += fy;
            n2.vx -= fx;
            n2.vy -= fy;
        }

        for (GraphNode n : nodes) {
            n.x += Math.max(-10.0, Math.min(10.0, n.vx));
            n.y += Math.max(-10.0, Math.min(10.0, n.vy));
            n.vx *= 0.85;
            n.vy *= 0.85;
        }
    }

    public void setSearchQuery(String query) {
        this.searchQuery = query != null ? query.trim().toLowerCase() : "";
        repaint();
    }

    private GraphNode findNodeAt(int screenX, int screenY) {
        double centerX = getWidth() / 2.0 + panX;
        double centerY = getHeight() / 2.0 + panY;

        java.util.List<GraphNode> nodes = graphData.getNodes();
        for (int i = nodes.size() - 1; i >= 0; i--) {
            GraphNode node = nodes.get(i);
            double nx = centerX + node.x * zoom;
            double ny = centerY + node.y * zoom;
            double r = (node.radius + 4) * zoom;
            double dist = Math.hypot(screenX - nx, screenY - ny);
            if (dist <= r) {
                return node;
            }
        }
        return null;
    }

    @Override
    protected void paintComponent(Graphics g) {
        super.paintComponent(g);
        Graphics2D g2 = (Graphics2D) g;
        g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
        g2.setRenderingHint(RenderingHints.KEY_TEXT_ANTIALIASING, RenderingHints.VALUE_TEXT_ANTIALIAS_ON);

        double centerX = getWidth() / 2.0 + panX;
        double centerY = getHeight() / 2.0 + panY;

        Map<String, GraphNode> nodesById = new HashMap<>();
        for (GraphNode n : graphData.getNodes()) {
            nodesById.put(n.getId(), n);
        }
        boolean isDark = !JBColor.isBright();

        // 1. Draw Edges
        Color edgeColor = isDark ? new Color(70, 75, 85, 180) : new Color(200, 205, 215, 200);
        Color activeEdgeColor = new Color(139, 92, 246, 220);
        g2.setStroke(new BasicStroke((float) (1.5 * zoom)));

        for (GraphEdge edge : graphData.getEdges()) {
            GraphNode source = nodesById.get(edge.getSourceId());
            GraphNode target = nodesById.get(edge.getTargetId());
            if (source == null || target == null) continue;

            double x1 = centerX + source.x * zoom;
            double y1 = centerY + source.y * zoom;
            double x2 = centerX + target.x * zoom;
            double y2 = centerY + target.y * zoom;

            boolean isHighlighted = (selectedNode != null && (source == selectedNode || target == selectedNode)) ||
                    (hoveredNode != null && (source == hoveredNode || target == hoveredNode));

            g2.setColor(isHighlighted ? activeEdgeColor : edgeColor);
            g2.draw(new Line2D.Double(x1, y1, x2, y2));
        }

        // 2. Draw Nodes
        Color normalNodeColor = isDark ? new Color(99, 102, 241) : new Color(79, 70, 229);
        Color hoveredNodeColor = new Color(168, 85, 247);
        Color selectedNodeColor = new Color(236, 72, 153);
        Color matchedNodeColor = new Color(245, 158, 11);

        int fontSize = (int) Math.max(9, Math.min(20, 12 * zoom));
        g2.setFont(new Font("SansSerif", Font.PLAIN, fontSize));

        for (GraphNode node : graphData.getNodes()) {
            double nx = centerX + node.x * zoom;
            double ny = centerY + node.y * zoom;
            double r = node.radius * zoom;

            boolean isSelected = node == selectedNode;
            boolean isHovered = node == hoveredNode;
            boolean isMatched = !searchQuery.isEmpty() && node.getName().toLowerCase().contains(searchQuery);

            Color fillColor = normalNodeColor;
            if (isSelected) fillColor = selectedNodeColor;
            else if (isHovered) fillColor = hoveredNodeColor;
            else if (isMatched) fillColor = matchedNodeColor;

            g2.setColor(fillColor);
            g2.fill(new Ellipse2D.Double(nx - r, ny - r, r * 2, r * 2));

            g2.setColor(Color.WHITE);
            g2.setStroke(new BasicStroke(2f));
            g2.draw(new Ellipse2D.Double(nx - r, ny - r, r * 2, r * 2));

            g2.setColor(isDark ? new Color(220, 225, 230) : new Color(33, 37, 41));
            FontMetrics fm = g2.getFontMetrics();
            int textWidth = fm.stringWidth(node.getName());
            g2.drawString(node.getName(), (float) (nx - textWidth / 2.0), (float) (ny + r + fm.getAscent() + 2));
        }
    }
}
