package com.puredown.actions;

import com.intellij.openapi.actionSystem.AnAction;
import com.intellij.openapi.actionSystem.AnActionEvent;
import com.intellij.openapi.actionSystem.CommonDataKeys;
import com.intellij.openapi.editor.Editor;
import com.intellij.openapi.ide.CopyPasteManager;
import com.intellij.openapi.ui.Messages;
import com.puredown.PureDownIcons;
import com.puredown.preview.PureDownHtmlGenerator;
import org.jetbrains.annotations.NotNull;

import java.awt.datatransfer.StringSelection;

public class PureDownCopyHtmlAction extends AnAction {

    public PureDownCopyHtmlAction() {
        super("Copy Rendered HTML", "Copy rendered HTML to clipboard", PureDownIcons.COPY);
    }

    @Override
    public void actionPerformed(@NotNull AnActionEvent e) {
        Editor editor = e.getData(CommonDataKeys.EDITOR);
        if (editor == null) return;

        String text = editor.getSelectionModel().hasSelection()
                ? editor.getSelectionModel().getSelectedText()
                : editor.getDocument().getText();

        String html = PureDownHtmlGenerator.generateHtml(text);
        CopyPasteManager.getInstance().setContents(new StringSelection(html));
        Messages.showInfoMessage("Rendered HTML copied to clipboard.", "PureDown");
    }

    @Override
    public void update(@NotNull AnActionEvent e) {
        e.getPresentation().setEnabled(e.getData(CommonDataKeys.EDITOR) != null);
    }
}
