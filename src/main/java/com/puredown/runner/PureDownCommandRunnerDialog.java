package com.puredown.runner;

import com.intellij.openapi.ide.CopyPasteManager;
import com.intellij.openapi.application.ApplicationManager;
import com.intellij.openapi.project.Project;
import com.intellij.openapi.ui.DialogWrapper;
import com.intellij.openapi.ui.Messages;
import com.intellij.ui.JBColor;
import com.intellij.ui.components.JBLabel;
import com.intellij.ui.components.JBScrollPane;
import com.intellij.ui.components.JBTextArea;
import com.intellij.util.ui.JBUI;
import com.puredown.i18n.PureDownI18n;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import javax.swing.*;
import java.awt.*;
import java.awt.datatransfer.StringSelection;
import java.io.BufferedReader;
import java.io.File;
import java.io.InputStreamReader;
import java.nio.charset.StandardCharsets;
import java.util.concurrent.TimeUnit;

public class PureDownCommandRunnerDialog extends DialogWrapper {

    private final Project project;
    private final JBTextArea commandArea = new JBTextArea(3, 70);
    private final JBTextArea outputArea = new JBTextArea(18, 70);
    private final JBLabel statusLabel = new JBLabel();
    private final JButton runButton = new JButton();
    private final JButton copyOutputButton = new JButton();

    public PureDownCommandRunnerDialog(@NotNull Project project, @NotNull String initialCommand) {
        super(project, false);
        this.project = project;

        setTitle(PureDownI18n.message("PureDown 快速命令执行 (Quick Runner)", "PureDown Quick Command Runner"));
        commandArea.setText(initialCommand);

        init();
        executeCurrentCommand();
    }

    @Nullable
    @Override
    protected JComponent createCenterPanel() {
        JPanel root = new JPanel(new BorderLayout(0, 10));
        root.setPreferredSize(new Dimension(850, 600));

        // 1. Top command panel
        JPanel topPanel = new JPanel(new BorderLayout(0, 6));
        topPanel.setBorder(JBUI.Borders.compound(
                JBUI.Borders.customLine(JBColor.border(), 0, 0, 1, 0),
                JBUI.Borders.empty(0, 0, 10, 0)
        ));

        JPanel commandHeader = new JPanel(new BorderLayout());
        JBLabel cmdLabel = new JBLabel(PureDownI18n.message("待执行命令 / Command (可编辑):", "Command (Editable):"));
        cmdLabel.setFont(JBUI.Fonts.smallFont());

        runButton.setText(PureDownI18n.message("▶️ 重新执行", "▶️ Run Command"));
        runButton.addActionListener(e -> executeCurrentCommand());

        commandHeader.add(cmdLabel, BorderLayout.WEST);
        commandHeader.add(runButton, BorderLayout.EAST);

        commandArea.setFont(new Font(Font.MONOSPACED, Font.PLAIN, 12));
        commandArea.setLineWrap(true);
        commandArea.setWrapStyleWord(true);
        JBScrollPane commandScroll = new JBScrollPane(commandArea);
        commandScroll.setPreferredSize(new Dimension(800, 75));

        topPanel.add(commandHeader, BorderLayout.NORTH);
        topPanel.add(commandScroll, BorderLayout.CENTER);

        // 2. Center output panel
        JPanel centerPanel = new JPanel(new BorderLayout(0, 6));

        JPanel outputHeader = new JPanel(new BorderLayout());
        statusLabel.setFont(JBUI.Fonts.smallFont());
        statusLabel.setText(PureDownI18n.message("准备就绪", "Ready"));

        copyOutputButton.setText(PureDownI18n.message("📋 复制结果", "📋 Copy Output"));
        copyOutputButton.addActionListener(e -> {
            String outText = outputArea.getText();
            if (!outText.isEmpty()) {
                CopyPasteManager.getInstance().setContents(new StringSelection(outText));
                Messages.showInfoMessage(
                        project,
                        PureDownI18n.message("命令输出已复制到剪贴板。", "Output copied to clipboard."),
                        "PureDown"
                );
            }
        });

        outputHeader.add(statusLabel, BorderLayout.WEST);
        outputHeader.add(copyOutputButton, BorderLayout.EAST);

        outputArea.setFont(new Font(Font.MONOSPACED, Font.PLAIN, 12));
        outputArea.setEditable(false);
        outputArea.setLineWrap(false);
        JBScrollPane outputScroll = new JBScrollPane(outputArea);

        centerPanel.add(outputHeader, BorderLayout.NORTH);
        centerPanel.add(outputScroll, BorderLayout.CENTER);

        root.add(topPanel, BorderLayout.NORTH);
        root.add(centerPanel, BorderLayout.CENTER);

        return root;
    }

    private void executeCurrentCommand() {
        String cmd = commandArea.getText().trim();
        if (cmd.isEmpty()) return;

        runButton.setEnabled(false);
        statusLabel.setForeground(JBColor.BLUE);
        statusLabel.setText(PureDownI18n.message("⏳ 正在执行命令，请稍候...", "⏳ Executing command, please wait..."));
        outputArea.setText("");

        ApplicationManager.getApplication().executeOnPooledThread(() -> {
            long startTime = System.currentTimeMillis();
            StringBuilder stdoutSb = new StringBuilder();
            int exitCode = -1;

            try {
                boolean isWindows = System.getProperty("os.name", "").toLowerCase().contains("win");
                ProcessBuilder pb;
                if (isWindows) {
                    pb = new ProcessBuilder("cmd.exe", "/c", cmd);
                } else {
                    pb = new ProcessBuilder("/bin/bash", "-c", cmd);
                }

                if (project.getBasePath() != null) {
                    pb.directory(new File(project.getBasePath()));
                }
                pb.redirectErrorStream(true);

                Process process = pb.start();

                try (BufferedReader reader = new BufferedReader(new InputStreamReader(process.getInputStream(), StandardCharsets.UTF_8))) {
                    String line;
                    while ((line = reader.readLine()) != null) {
                        stdoutSb.append(line).append("\n");
                    }
                }

                boolean finished = process.waitFor(30, TimeUnit.SECONDS);
                if (finished) {
                    exitCode = process.exitValue();
                } else {
                    process.destroyForcibly();
                    stdoutSb.append("\n[PureDown] 命令执行超时（超过 30 秒已自动终止） / Command timed out (exceeded 30s)\n");
                }
            } catch (Exception ex) {
                stdoutSb.append("\n[PureDown Error] ").append(ex.getMessage()).append("\n");
            }

            long elapsed = System.currentTimeMillis() - startTime;
            final int finalExitCode = exitCode;
            final String rawOutput = stdoutSb.toString();
            final String formattedOutput = formatJsonIfPossible(rawOutput);

            SwingUtilities.invokeLater(() -> {
                runButton.setEnabled(true);
                outputArea.setText(formattedOutput);
                outputArea.setCaretPosition(0);

                if (finalExitCode == 0) {
                    statusLabel.setForeground(new Color(46, 125, 50));
                    statusLabel.setText(PureDownI18n.message(
                            String.format("✅ 执行成功 (退出码: 0, 耗时: %d ms)", elapsed),
                            String.format("✅ Success (Exit Code: 0, %d ms)", elapsed)
                    ));
                } else {
                    statusLabel.setForeground(new Color(211, 47, 47));
                    statusLabel.setText(PureDownI18n.message(
                            String.format("❌ 执行结束 (退出码: %d, 耗时: %d ms)", finalExitCode, elapsed),
                            String.format("❌ Finished with error (Exit Code: %d, %d ms)", finalExitCode, elapsed)
                    ));
                }
            });
        });
    }

    private static String formatJsonIfPossible(String raw) {
        if (raw == null) return "";
        String trimmed = raw.trim();
        if ((!trimmed.startsWith("{") || !trimmed.endsWith("}")) &&
                (!trimmed.startsWith("[") || !trimmed.endsWith("]"))) {
            return raw;
        }

        try {
            StringBuilder sb = new StringBuilder();
            int indent = 0;
            boolean inQuotes = false;
            for (int i = 0; i < trimmed.length(); i++) {
                char c = trimmed.charAt(i);
                if (c == '"') {
                    int backslashes = 0;
                    int j = i - 1;
                    while (j >= 0 && trimmed.charAt(j) == '\\') {
                        backslashes++;
                        j--;
                    }
                    if (backslashes % 2 == 0) {
                        inQuotes = !inQuotes;
                    }
                    sb.append(c);
                } else if (inQuotes) {
                    sb.append(c);
                } else {
                    switch (c) {
                        case '{':
                        case '[':
                            sb.append(c).append('\n');
                            indent += 2;
                            sb.append(" ".repeat(indent));
                            break;
                        case '}':
                        case ']':
                            sb.append('\n');
                            indent = Math.max(0, indent - 2);
                            sb.append(" ".repeat(indent));
                            sb.append(c);
                            break;
                        case ',':
                            sb.append(c).append('\n');
                            sb.append(" ".repeat(indent));
                            break;
                        case ':':
                            sb.append(": ");
                            break;
                        default:
                            if (!Character.isWhitespace(c)) {
                                sb.append(c);
                            }
                            break;
                    }
                }
            }
            return sb.toString();
        } catch (Exception ignored) {
            return raw;
        }
    }

    @Override
    protected Action @NotNull [] createActions() {
        Action cancelAction = getCancelAction();
        cancelAction.putValue(Action.NAME, PureDownI18n.message("关闭", "Close"));
        return new Action[]{cancelAction};
    }
}
