package com.puredown.table;

import com.intellij.openapi.actionSystem.AnAction;
import com.intellij.openapi.actionSystem.AnActionEvent;
import com.intellij.openapi.actionSystem.CommonDataKeys;
import com.intellij.openapi.command.WriteCommandAction;
import com.intellij.openapi.editor.Document;
import com.intellij.openapi.editor.Editor;
import com.intellij.openapi.project.Project;
import com.intellij.openapi.ui.Messages;
import com.puredown.PureDownIcons;
import com.puredown.i18n.PureDownI18n;
import org.jetbrains.annotations.NotNull;

public class PureDownTableActions {

    public static class FormatTableAction extends AnAction {
        public FormatTableAction() {
            super("Format Table", "Align and format Markdown table", PureDownIcons.TABLE);
        }

        @Override
        public void actionPerformed(@NotNull AnActionEvent e) {
            Project project = e.getProject();
            Editor editor = e.getData(CommonDataKeys.EDITOR);
            if (project == null || editor == null) return;

            boolean formatted = PureDownTableFormatter.formatTableAtCaret(project, editor.getDocument(), editor.getCaretModel().getOffset());
            if (!formatted) {
                Messages.showInfoMessage(
                        project,
                        PureDownI18n.message("光标当前未位于 Markdown 表格内。", "Caret is not inside a Markdown table."),
                        PureDownI18n.message("PureDown 表格", "PureDown Table")
                );
            }
        }

        @Override
        public void update(@NotNull AnActionEvent e) {
            e.getPresentation().setEnabled(e.getData(CommonDataKeys.EDITOR) != null);
            e.getPresentation().setText(PureDownI18n.message("格式化表格", "Format Table"));
            e.getPresentation().setDescription(PureDownI18n.message("对齐排版 Markdown 表格各列", "Align and format Markdown table"));
        }
    }

    public static class InsertTableAction extends AnAction {
        public InsertTableAction() {
            super("Insert Table", "Insert a new Markdown table", PureDownIcons.TABLE);
        }

        @Override
        public void actionPerformed(@NotNull AnActionEvent e) {
            Project project = e.getProject();
            Editor editor = e.getData(CommonDataKeys.EDITOR);
            if (project == null || editor == null) return;

            Document document = editor.getDocument();
            int offset = editor.getCaretModel().getOffset();
            String template = "\n" + PureDownTableFormatter.generateTableTemplate(3, 3) + "\n";

            WriteCommandAction.runWriteCommandAction(project, "Insert Markdown Table", null, () -> {
                document.insertString(offset, template);
                editor.getCaretModel().moveToOffset(offset + template.length());
            });
        }

        @Override
        public void update(@NotNull AnActionEvent e) {
            e.getPresentation().setEnabled(e.getData(CommonDataKeys.EDITOR) != null);
            e.getPresentation().setText(PureDownI18n.message("插入表格", "Insert Table"));
            e.getPresentation().setDescription(PureDownI18n.message("插入标准 Markdown 表格模板", "Insert a new Markdown table"));
        }
    }
}
