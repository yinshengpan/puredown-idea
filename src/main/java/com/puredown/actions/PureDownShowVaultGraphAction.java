package com.puredown.actions;

import com.intellij.openapi.actionSystem.AnAction;
import com.intellij.openapi.actionSystem.AnActionEvent;
import com.intellij.openapi.project.Project;
import com.puredown.PureDownIcons;
import com.puredown.obsidian.graph.PureDownVaultGraphDialog;
import org.jetbrains.annotations.NotNull;

public class PureDownShowVaultGraphAction extends AnAction {

    public PureDownShowVaultGraphAction() {
        super("Show Knowledge Vault Graph", "Open visual note graph", PureDownIcons.GRAPH);
    }

    @Override
    public void actionPerformed(@NotNull AnActionEvent e) {
        Project project = e.getProject();
        if (project == null) return;
        PureDownVaultGraphDialog dialog = new PureDownVaultGraphDialog(project);
        dialog.show();
    }

    @Override
    public void update(@NotNull AnActionEvent e) {
        e.getPresentation().setEnabled(e.getProject() != null);
    }
}
