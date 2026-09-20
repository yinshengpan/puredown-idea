package com.puredown.actions;

import com.intellij.openapi.actionSystem.AnAction;
import com.intellij.openapi.actionSystem.AnActionEvent;
import com.intellij.openapi.actionSystem.CommonDataKeys;
import com.intellij.openapi.command.WriteCommandAction;
import com.intellij.openapi.editor.Document;
import com.intellij.openapi.editor.Editor;
import com.intellij.openapi.project.Project;
import com.puredown.PureDownIcons;
import org.jetbrains.annotations.NotNull;

import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class PureDownRenumberListAction extends AnAction {
    private static final Pattern NUM_LIST_PATTERN = Pattern.compile("^(\\s*)(\\d+)\\.\\s+(.*)$");

    public PureDownRenumberListAction() {
        super("Renumber Ordered List", "Automatically fix sequential numbers in ordered list", PureDownIcons.NUM_LIST);
    }

    @Override
    public void actionPerformed(@NotNull AnActionEvent e) {
        Project project = e.getProject();
        Editor editor = e.getData(CommonDataKeys.EDITOR);
        if (project == null || editor == null) return;

        Document document = editor.getDocument();
        int currentLine = document.getLineNumber(editor.getCaretModel().getOffset());

        String currentText = getLineText(document, currentLine);
        if (!NUM_LIST_PATTERN.matcher(currentText).matches()) return;

        int startLine = currentLine;
        while (startLine > 0 && NUM_LIST_PATTERN.matcher(getLineText(document, startLine - 1)).matches()) {
            startLine--;
        }

        int endLine = currentLine;
        while (endLine < document.getLineCount() - 1 && NUM_LIST_PATTERN.matcher(getLineText(document, endLine + 1)).matches()) {
            endLine++;
        }

        final int finalStartLine = startLine;
        final int finalEndLine = endLine;

        WriteCommandAction.runWriteCommandAction(project, "Renumber Ordered List", null, () -> {
            int counter = 1;
            for (int line = finalStartLine; line <= finalEndLine; line++) {
                int lineStart = document.getLineStartOffset(line);
                int lineEnd = document.getLineEndOffset(line);
                String text = document.getCharsSequence().subSequence(lineStart, lineEnd).toString();

                Matcher m = NUM_LIST_PATTERN.matcher(text);
                if (m.matches()) {
                    String indent = m.group(1);
                    String content = m.group(3);
                    String replacement = indent + counter + ". " + content;
                    document.replaceString(lineStart, lineEnd, replacement);
                    counter++;
                }
            }
        });
    }

    private static String getLineText(Document document, int line) {
        int start = document.getLineStartOffset(line);
        int end = document.getLineEndOffset(line);
        return document.getCharsSequence().subSequence(start, end).toString();
    }

    @Override
    public void update(@NotNull AnActionEvent e) {
        e.getPresentation().setEnabled(e.getData(CommonDataKeys.EDITOR) != null);
        e.getPresentation().setText(com.puredown.i18n.PureDownI18n.message("重新连续编号", "Renumber Ordered List"));
        e.getPresentation().setDescription(com.puredown.i18n.PureDownI18n.message("自动修复有序列表序号为连续数字", "Automatically fix sequential numbers in ordered list"));
    }
}
