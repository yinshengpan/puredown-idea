package com.puredown.actions;

import com.intellij.openapi.actionSystem.AnAction;
import com.intellij.openapi.actionSystem.AnActionEvent;
import com.intellij.openapi.actionSystem.CommonDataKeys;
import com.intellij.openapi.command.WriteCommandAction;
import com.intellij.openapi.editor.Document;
import com.intellij.openapi.editor.Editor;
import com.intellij.openapi.editor.SelectionModel;
import com.intellij.openapi.project.Project;
import com.intellij.openapi.ui.popup.JBPopupFactory;
import com.puredown.PureDownIcons;
import org.jetbrains.annotations.NotNull;

import javax.swing.*;
import java.util.List;

public class PureDownInsertCalloutAction extends AnAction {

    private static class CalloutEntry {
        final String type;
        final String desc;
        CalloutEntry(String type, String desc) {
            this.type = type;
            this.desc = desc;
        }
        @Override
        public String toString() {
            return "[!" + type + "] - " + desc;
        }
    }

    private static final List<CalloutEntry> CALLOUT_TYPES = List.of(
        new CalloutEntry("NOTE", "Informational note"),
        new CalloutEntry("TIP", "Helpful advice or tip"),
        new CalloutEntry("WARNING", "Warning for user caution"),
        new CalloutEntry("CAUTION", "Critical danger / caution"),
        new CalloutEntry("IMPORTANT", "Important notice"),
        new CalloutEntry("SUCCESS", "Success confirmation"),
        new CalloutEntry("QUESTION", "Question or FAQ item"),
        new CalloutEntry("DANGER", "Hazardous action warning"),
        new CalloutEntry("BUG", "Known bug or issue"),
        new CalloutEntry("EXAMPLE", "Code or concept example"),
        new CalloutEntry("QUOTE", "Quotation block")
    );

    public PureDownInsertCalloutAction() {
        super("Insert Obsidian Callout...", "Insert styled callout note", PureDownIcons.CALLOUT);
    }

    @Override
    public void actionPerformed(@NotNull AnActionEvent e) {
        Project project = e.getProject();
        Editor editor = e.getData(CommonDataKeys.EDITOR);
        if (project == null || editor == null) return;

        String popupTitle = com.puredown.i18n.PureDownI18n.message("选择标注框类型", "Select Callout Type");
        JBPopupFactory.getInstance().createPopupChooserBuilder(CALLOUT_TYPES)
            .setTitle(popupTitle)
            .setItemChosenCallback(chosen -> insertCallout(editor, chosen.type))
            .createPopup()
            .showInBestPositionFor(editor);
    }

    private void insertCallout(Editor editor, String type) {
        Project project = editor.getProject();
        if (project == null) return;
        Document document = editor.getDocument();
        SelectionModel selectionModel = editor.getSelectionModel();

        String defaultContent = com.puredown.i18n.PureDownI18n.message("此处填写标注内容。", "Callout content here.");
        String selectedText = selectionModel.hasSelection() ? selectionModel.getSelectedText() : defaultContent;
        String snippet = "\n> [!" + type + "]\n> " + selectedText + "\n\n";

        WriteCommandAction.runWriteCommandAction(project, "Insert Callout", null, () -> {
            if (selectionModel.hasSelection()) {
                document.replaceString(selectionModel.getSelectionStart(), selectionModel.getSelectionEnd(), snippet);
            } else {
                int offset = editor.getCaretModel().getOffset();
                document.insertString(offset, snippet);
                editor.getCaretModel().moveToOffset(offset + snippet.length());
            }
        });
    }

    @Override
    public void update(@NotNull AnActionEvent e) {
        e.getPresentation().setEnabled(e.getData(CommonDataKeys.EDITOR) != null);
        e.getPresentation().setText(com.puredown.i18n.PureDownI18n.message("插入标注框...", "Insert Obsidian Callout..."));
        e.getPresentation().setDescription(com.puredown.i18n.PureDownI18n.message("插入各类高亮提示框 (Note, Tip, Warning...)", "Insert styled callout note"));
    }
}
