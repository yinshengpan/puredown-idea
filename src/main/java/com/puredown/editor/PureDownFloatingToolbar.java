package com.puredown.editor;

import com.intellij.openapi.Disposable;
import com.intellij.openapi.command.WriteCommandAction;
import com.intellij.openapi.editor.Document;
import com.intellij.openapi.editor.Editor;
import com.intellij.openapi.editor.SelectionModel;
import com.intellij.openapi.editor.VisualPosition;
import com.intellij.openapi.editor.event.SelectionEvent;
import com.intellij.openapi.editor.event.SelectionListener;
import com.intellij.openapi.project.Project;
import com.intellij.ui.JBColor;
import com.puredown.PureDownIcons;
import org.jetbrains.annotations.NotNull;

import javax.swing.*;
import java.awt.*;

public class PureDownFloatingToolbar implements Disposable {
    private final Project project;
    private final Editor editor;
    private final JPanel panel = new JPanel(new FlowLayout(FlowLayout.CENTER, 4, 2));
    private final SelectionListener selectionListener;
    private Popup popup = null;
    private boolean isVisible = false;

    public PureDownFloatingToolbar(Project project, Editor editor) {
        this.project = project;
        this.editor = editor;

        panel.setBackground(new JBColor(0xF0F2F5, 0x2B2D30));
        panel.setBorder(BorderFactory.createCompoundBorder(
                BorderFactory.createLineBorder(new JBColor(0xD0D7DE, 0x393B40), 1, true),
                BorderFactory.createEmptyBorder(2, 4, 2, 4)
        ));

        addFormatButton("B", com.puredown.i18n.PureDownI18n.message("粗体 (⌘B)", "Bold (⌘B)"), PureDownIcons.BOLD, () -> wrapSelection("**", "**"));
        addFormatButton("I", com.puredown.i18n.PureDownI18n.message("斜体 (⌘I)", "Italic (⌘I)"), PureDownIcons.ITALIC, () -> wrapSelection("*", "*"));
        addFormatButton("S", com.puredown.i18n.PureDownI18n.message("删除线", "Strikethrough"), PureDownIcons.STRIKETHROUGH, () -> wrapSelection("~~", "~~"));
        addFormatButton("`", com.puredown.i18n.PureDownI18n.message("行内代码", "Inline Code"), PureDownIcons.CODE, () -> wrapSelection("`", "`"));
        addFormatButton("Link", com.puredown.i18n.PureDownI18n.message("插入超链接", "Insert Link"), PureDownIcons.LINK, () -> {
            String selected = editor.getSelectionModel().getSelectedText();
            replaceSelection("[" + (selected != null ? selected : "text") + "](url)");
        });
        addFormatButton("Quote", com.puredown.i18n.PureDownI18n.message("引用区块", "Blockquote"), PureDownIcons.QUOTE, () -> {
            String selected = editor.getSelectionModel().getSelectedText();
            if (selected != null) {
                String[] lines = selected.split("\n", -1);
                StringBuilder sb = new StringBuilder();
                for (String l : lines) {
                    if (sb.length() > 0) sb.append("\n");
                    sb.append("> ").append(l);
                }
                replaceSelection(sb.toString());
            }
        });
        addFormatButton("Task", com.puredown.i18n.PureDownI18n.message("任务列表项", "Task List Item"), PureDownIcons.TASK, () -> {
            String selected = editor.getSelectionModel().getSelectedText();
            if (selected != null) {
                String[] lines = selected.split("\n", -1);
                StringBuilder sb = new StringBuilder();
                for (String l : lines) {
                    if (sb.length() > 0) sb.append("\n");
                    sb.append("- [ ] ").append(l);
                }
                replaceSelection(sb.toString());
            }
        });

        this.selectionListener = new SelectionListener() {
            @Override
            public void selectionChanged(@NotNull SelectionEvent e) {
                if (editor.getSelectionModel().hasSelection()) {
                    showToolbar();
                } else {
                    hideToolbar();
                }
            }
        };
        editor.getSelectionModel().addSelectionListener(selectionListener);
    }

    private void addFormatButton(String text, String tooltip, Icon icon, Runnable action) {
        JButton btn = new JButton();
        if (icon != null) {
            btn.setIcon(icon);
        } else {
            btn.setText(text);
        }
        btn.setToolTipText(tooltip);
        btn.setFocusable(false);
        btn.setMargin(new Insets(2, 6, 2, 6));
        btn.addActionListener(e -> {
            action.run();
            hideToolbar();
        });
        panel.add(btn);
    }

    private void wrapSelection(String prefix, String suffix) {
        SelectionModel sm = editor.getSelectionModel();
        Document doc = editor.getDocument();
        int start = sm.getSelectionStart();
        int end = sm.getSelectionEnd();
        String text = sm.getSelectedText();
        if (text == null) return;

        WriteCommandAction.runWriteCommandAction(project, "Format Selection", null, () -> {
            doc.replaceString(start, end, prefix + text + suffix);
            sm.setSelection(start + prefix.length(), end + prefix.length());
        });
    }

    private void replaceSelection(String replacement) {
        SelectionModel sm = editor.getSelectionModel();
        Document doc = editor.getDocument();
        int start = sm.getSelectionStart();
        int end = sm.getSelectionEnd();

        WriteCommandAction.runWriteCommandAction(project, "Replace Selection", null, () -> {
            doc.replaceString(start, end, replacement);
        });
    }

    public void showToolbar() {
        if (!editor.getComponent().isShowing()) return;
        hideToolbar();

        VisualPosition caretVisual = editor.getCaretModel().getVisualPosition();
        Point p = editor.visualPositionToXY(caretVisual);
        Point editorLoc;
        try {
            editorLoc = editor.getContentComponent().getLocationOnScreen();
        } catch (Exception ignored) {
            return;
        }

        int x = editorLoc.x + p.x;
        int y = editorLoc.y + p.y - 45;

        PopupFactory factory = PopupFactory.getSharedInstance();
        popup = factory.getPopup(editor.getContentComponent(), panel, Math.max(10, x), Math.max(10, y));
        popup.show();
        isVisible = true;
    }

    public void hideToolbar() {
        if (isVisible && popup != null) {
            popup.hide();
            popup = null;
            isVisible = false;
        }
    }

    @Override
    public void dispose() {
        hideToolbar();
        if (selectionListener != null) {
            editor.getSelectionModel().removeSelectionListener(selectionListener);
        }
    }
}
