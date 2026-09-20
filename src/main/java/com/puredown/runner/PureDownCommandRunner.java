package com.puredown.runner;

import com.intellij.openapi.project.Project;
import org.jetbrains.annotations.NotNull;

import javax.swing.*;

public final class PureDownCommandRunner {
    private PureDownCommandRunner() {}

    public static void runCommand(@NotNull Project project, @NotNull String command) {
        SwingUtilities.invokeLater(() -> {
            PureDownCommandRunnerDialog dialog = new PureDownCommandRunnerDialog(project, command);
            dialog.show();
        });
    }
}
