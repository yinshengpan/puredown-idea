package com.puredown.editor;

import com.intellij.openapi.Disposable;
import com.intellij.openapi.editor.CaretModel;
import com.intellij.openapi.editor.Document;
import com.intellij.openapi.editor.Editor;
import com.intellij.openapi.editor.LogicalPosition;
import com.intellij.openapi.editor.event.CaretEvent;
import com.intellij.openapi.editor.event.CaretListener;
import com.intellij.openapi.editor.event.DocumentEvent;
import com.intellij.openapi.editor.event.DocumentListener;
import com.intellij.openapi.vfs.VirtualFile;
import com.intellij.ui.JBColor;
import com.intellij.util.ui.JBUI;
import com.puredown.settings.PureDownSettings;
import org.jetbrains.annotations.NotNull;

import javax.swing.*;
import java.awt.*;

public class PureDownStatusBarPanel extends JPanel implements Disposable {

    private final JLabel fileLabel = new JLabel();
    private final JLabel cursorLabel = new JLabel();
    private final JLabel statsLabel = new JLabel();
    private final JLabel readingTimeLabel = new JLabel();
    private final JLabel encodingLabel = new JLabel("UTF-8");

    private final Editor editor;
    private final VirtualFile file;

    public PureDownStatusBarPanel(Editor editor, VirtualFile file) {
        super(new BorderLayout());
        this.editor = editor;
        this.file = file;

        setBorder(JBUI.Borders.compound(
                JBUI.Borders.customLine(JBColor.border(), 1, 0, 0, 0),
                JBUI.Borders.empty(4, 12)
        ));
        setBackground(JBColor.PanelBackground);

        // Left side: status dot + file name
        JPanel leftPanel = new JPanel(new FlowLayout(FlowLayout.LEFT, 8, 0));
        leftPanel.setOpaque(false);
        fileLabel.setFont(JBUI.Fonts.smallFont());
        fileLabel.setForeground(JBColor.GRAY);
        leftPanel.add(fileLabel);

        // Right side: cursor, stats, reading time, encoding
        JPanel rightPanel = new JPanel(new FlowLayout(FlowLayout.RIGHT, 12, 0));
        rightPanel.setOpaque(false);

        cursorLabel.setFont(JBUI.Fonts.smallFont());
        cursorLabel.setForeground(JBColor.GRAY);

        statsLabel.setFont(JBUI.Fonts.smallFont());
        statsLabel.setForeground(JBColor.GRAY);

        readingTimeLabel.setFont(JBUI.Fonts.smallFont());
        readingTimeLabel.setForeground(JBColor.GRAY);

        encodingLabel.setFont(JBUI.Fonts.smallFont());
        encodingLabel.setForeground(JBColor.GRAY);

        rightPanel.add(cursorLabel);
        rightPanel.add(createDivider());
        rightPanel.add(statsLabel);
        rightPanel.add(createDivider());
        rightPanel.add(readingTimeLabel);
        rightPanel.add(createDivider());
        rightPanel.add(encodingLabel);

        add(leftPanel, BorderLayout.WEST);
        add(rightPanel, BorderLayout.EAST);

        // Caret listener
        editor.getCaretModel().addCaretListener(new CaretListener() {
            @Override
            public void caretPositionChanged(@NotNull CaretEvent event) {
                updateCaretInfo();
            }
        });

        // Document listener
        editor.getDocument().addDocumentListener(new DocumentListener() {
            @Override
            public void documentChanged(@NotNull DocumentEvent event) {
                updateStats();
            }
        }, this);

        updateCaretInfo();
        updateStats();
    }

    private JLabel createDivider() {
        JLabel div = new JLabel("|");
        div.setFont(JBUI.Fonts.smallFont());
        div.setForeground(JBColor.border());
        return div;
    }

    public void updateCaretInfo() {
        CaretModel caretModel = editor.getCaretModel();
        LogicalPosition pos = caretModel.getLogicalPosition();
        int line = pos.line + 1;
        int col = pos.column + 1;
        cursorLabel.setText(com.puredown.i18n.PureDownI18n.message(
                String.format("行 %d, 列 %d", line, col),
                String.format("Ln %d, Col %d", line, col)
        ));

        String fileName = file != null ? file.getName() : "Untitled";
        fileLabel.setText("📄 " + fileName);
    }

    public void updateStats() {
        if (!PureDownSettings.getInstance().isShowStatusBarStats()) {
            setVisible(false);
            return;
        }
        setVisible(true);

        Document doc = editor.getDocument();
        String text = doc.getText();
        DocumentStats stats = calculateStats(text);

        statsLabel.setText(com.puredown.i18n.PureDownI18n.message(
                String.format("%d 字符  %d 词  %d 行", stats.charCount, stats.wordCount, stats.lineCount),
                String.format("%d chars  %d words  %d lines", stats.charCount, stats.wordCount, stats.lineCount)
        ));
        readingTimeLabel.setText(com.puredown.i18n.PureDownI18n.message(
                String.format("⏱️ 约 %d 分钟阅读", stats.readingTimeMinutes),
                String.format("⏱️ ~%d min read", stats.readingTimeMinutes)
        ));
    }

    private static class DocumentStats {
        final int charCount;
        final int wordCount;
        final int lineCount;
        final int readingTimeMinutes;

        DocumentStats(int charCount, int wordCount, int lineCount, int readingTimeMinutes) {
            this.charCount = charCount;
            this.wordCount = wordCount;
            this.lineCount = lineCount;
            this.readingTimeMinutes = readingTimeMinutes;
        }
    }

    private DocumentStats calculateStats(String text) {
        if (text == null || text.isEmpty()) {
            return new DocumentStats(0, 0, 1, 1);
        }

        int charCount = text.length();
        int lineCount = 1;
        for (int i = 0; i < text.length(); i++) {
            if (text.charAt(i) == '\n') lineCount++;
        }

        int cjkChars = 0;
        int nonCjkWords = 0;
        StringBuilder wordBuffer = new StringBuilder();

        for (int i = 0; i < text.length(); i++) {
            int cp = text.codePointAt(i);
            Character.UnicodeScript script = Character.UnicodeScript.of(cp);
            if (script == Character.UnicodeScript.HAN ||
                    (cp >= 0x4E00 && cp <= 0x9FFF) ||
                    (cp >= 0x3400 && cp <= 0x4DBF)) {
                cjkChars++;
                if (wordBuffer.length() > 0) {
                    nonCjkWords++;
                    wordBuffer.setLength(0);
                }
            } else if (Character.isLetterOrDigit(cp)) {
                wordBuffer.appendCodePoint(cp);
            } else {
                if (wordBuffer.length() > 0) {
                    nonCjkWords++;
                    wordBuffer.setLength(0);
                }
            }
            if (Character.isSupplementaryCodePoint(cp)) {
                i++;
            }
        }
        if (wordBuffer.length() > 0) {
            nonCjkWords++;
        }

        int wordCount = nonCjkWords + cjkChars;
        double speed = ((double) nonCjkWords) / 200.0 + ((double) cjkChars) / 400.0;
        int readingMinutes = Math.max(1, (int) Math.ceil(speed));

        return new DocumentStats(charCount, wordCount, lineCount, readingMinutes);
    }

    @Override
    public void dispose() {}
}
