package com.puredown.obsidian.graph;

import com.intellij.openapi.project.Project;
import com.intellij.openapi.ui.DialogWrapper;
import com.intellij.ui.components.JBLabel;
import com.intellij.ui.components.JBTextField;

import javax.swing.*;
import javax.swing.event.DocumentEvent;
import javax.swing.event.DocumentListener;
import java.awt.*;

public class PureDownVaultGraphDialog extends DialogWrapper {

    private final Project project;
    private final VaultGraphData graphData;
    private final PureDownVaultGraphPanel graphPanel;

    public PureDownVaultGraphDialog(Project project) {
        super(project, true);
        this.project = project;
        this.graphData = PureDownVaultGraphService.buildGraph(project);
        this.graphPanel = new PureDownVaultGraphPanel(project, graphData);
        setTitle("PureDown Knowledge Vault Graph");
        init();
    }

    @Override
    protected JComponent createCenterPanel() {
        JPanel root = new JPanel(new BorderLayout());
        root.setPreferredSize(new Dimension(900, 650));

        JPanel topBar = new JPanel(new BorderLayout());
        JPanel searchPanel = new JPanel(new FlowLayout(FlowLayout.LEFT));
        searchPanel.add(new JBLabel("Search Notes: "));
        JBTextField searchField = new JBTextField(20);
        searchField.getDocument().addDocumentListener(new DocumentListener() {
            @Override
            public void insertUpdate(DocumentEvent e) { update(); }
            @Override
            public void removeUpdate(DocumentEvent e) { update(); }
            @Override
            public void changedUpdate(DocumentEvent e) { update(); }
            private void update() {
                graphPanel.setSearchQuery(searchField.getText());
            }
        });
        searchPanel.add(searchField);

        JBLabel infoLabel = new JBLabel("Notes: " + graphData.getNodes().size() +
                " | Links: " + graphData.getEdges().size() +
                "  (Double-click node to open)");
        infoLabel.setBorder(BorderFactory.createEmptyBorder(0, 0, 0, 16));

        topBar.add(searchPanel, BorderLayout.WEST);
        topBar.add(infoLabel, BorderLayout.EAST);
        topBar.setBorder(BorderFactory.createEmptyBorder(6, 8, 6, 8));

        root.add(topBar, BorderLayout.NORTH);
        root.add(graphPanel, BorderLayout.CENTER);

        return root;
    }
}
