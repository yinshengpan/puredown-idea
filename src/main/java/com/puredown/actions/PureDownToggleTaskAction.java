package com.puredown.actions;

import com.intellij.openapi.actionSystem.AnAction;
import com.intellij.openapi.actionSystem.AnActionEvent;
import com.intellij.openapi.actionSystem.CommonDataKeys;
import com.intellij.openapi.command.WriteCommandAction;
import com.intellij.openapi.editor.Document;
import com.intellij.openapi.editor.Editor;
import com.intellij.openapi.editor.SelectionModel;
import com.intellij.openapi.project.Project;
import com.puredown.PureDownIcons;
import org.jetbrains.annotations.NotNull;

public class PureDownToggleTaskAction extends AnAction {

    public PureDownToggleTaskAction() {
        super("Toggle Task Checkbox", "Toggle markdown task item [ ] <-> [x]", PureDownIcons.TASK);
    }

    @Override
    public void actionPerformed(@NotNull AnActionEvent e) {
        Project project = e.getProject();
        Editor editor = e.getData(CommonDataKeys.EDITOR);
        if (project == null || editor == null) return;

        Document document = editor.getDocument();
        SelectionModel selectionModel = editor.getSelectionModel();

        int startOffset = selectionModel.hasSelection() ? selectionModel.getSelectionStart() : editor.getCaretModel().getOffset();
        int endOffset = selectionModel.hasSelection() ? selectionModel.getSelectionEnd() : editor.getCaretModel().getOffset();

        int startLine = document.getLineNumber(startOffset);
        int endLine = document.getLineNumber(endOffset);

        WriteCommandAction.runWriteCommandAction(project, "Toggle Markdown Task", null, () -> {
            for (int line = startLine; line <= endLine; line++) {
                int lineStart = document.getLineStartOffset(line);
                int lineEnd = document.getLineEndOffset(line);
                String text = document.getCharsSequence().subSequence(lineStart, lineEnd).toString();

                String newText;
                if (text.contains("- [ ]")) {
                    newText = text.replaceFirst("- \\[ \\]", "- [x]");
                } else if (text.contains("- [x]")) {
                    newText = text.replaceFirst("- \\[x\\]", "- [ ]");
                } else if (text.contains("- [X]")) {
                    newText = text.replaceFirst("- \\[X\\]", "- [ ]");
                } else if (text.trim().startsWith("- ")) {
                    newText = text.replaceFirst("- ", "- [ ] ");
                } else if (text.trim().startsWith("* ")) {
                    newText = text.replaceFirst("\\* ", "- [ ] ");
                } else {
                    newText = "- [ ] " + text;
                }
                document.replaceString(lineStart, lineEnd, newText);
            }
        });
    }

    @Override
    public void update(@NotNull AnActionEvent e) {
        e.getPresentation().setEnabled(e.getData(CommonDataKeys.EDITOR) != null);
        e.getPresentation().setText(com.puredown.i18n.PureDownI18n.message("切换任务复选框", "Toggle Task Checkbox"));
        e.getPresentation().setDescription(com.puredown.i18n.PureDownI18n.message("切换任务列表复选框 [ ] <-> [x]", "Toggle task list checkbox [ ] <-> [x]"));
    }
}
