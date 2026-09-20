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
        Messages.showInfoMessage(
                com.puredown.i18n.PureDownI18n.message("渲染后的富文本 HTML 已成功复制到剪贴板。", "Rendered HTML copied to clipboard."),
                "PureDown"
        );
    }

    @Override
    public void update(@NotNull AnActionEvent e) {
        e.getPresentation().setEnabled(e.getData(CommonDataKeys.EDITOR) != null);
        e.getPresentation().setText(com.puredown.i18n.PureDownI18n.message("复制富文本 HTML", "Copy Rendered HTML"));
        e.getPresentation().setDescription(com.puredown.i18n.PureDownI18n.message("复制文档渲染后的富文本 HTML 到剪贴板", "Copy rendered HTML to clipboard"));
    }
}
