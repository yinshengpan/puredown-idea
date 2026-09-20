package com.puredown.table;

import com.intellij.openapi.command.WriteCommandAction;
import com.intellij.openapi.editor.Document;
import com.intellij.openapi.editor.impl.DocumentImpl;
import com.intellij.openapi.project.Project;
import java.util.List;

public final class PureDownTableFormatter {
    private PureDownTableFormatter() {}

    public static int displayWidth(String str) {
        if (str == null) return 0;
        int width = 0;
        for (int i = 0; i < str.length(); i++) {
            char ch = str.charAt(i);
            int code = (int) ch;
            if ((code >= 0x4E00 && code <= 0x9FFF) ||
                (code >= 0x3400 && code <= 0x4DBF) ||
                (code >= 0xF900 && code <= 0xFAFF) ||
                (code >= 0xAC00 && code <= 0xD7AF) ||
                (code >= 0xFF01 && code <= 0xFF60) ||
                (code >= 0xFFE0 && code <= 0xFFE6)) {
                width += 2;
            } else {
                width += 1;
            }
        }
        return width;
    }

    private static String padCell(String content, int targetWidth, ColumnAlignment alignment) {
        int currentWidth = displayWidth(content);
        int diff = targetWidth - currentWidth;
        if (diff <= 0) return content;

        if (alignment == ColumnAlignment.RIGHT) {
            return " ".repeat(diff) + content;
        } else if (alignment == ColumnAlignment.CENTER) {
            int leftPad = diff / 2;
            int rightPad = diff - leftPad;
            return " ".repeat(leftPad) + content + " ".repeat(rightPad);
        } else {
            return content + " ".repeat(diff);
        }
    }

    public static String format(ParsedTable table) {
        int colCount = table.getColumnCount();
        int[] colWidths = new int[colCount];
        for (int i = 0; i < colCount; i++) {
            colWidths[i] = 3;
        }

        List<TableCell> headerCells = table.getHeaderRow().getCells();
        for (int i = 0; i < colCount; i++) {
            String cellText = i < headerCells.size() ? headerCells.get(i).getContent() : "";
            colWidths[i] = Math.max(colWidths[i], displayWidth(cellText));
        }

        for (TableRow row : table.getDataRows()) {
            List<TableCell> cells = row.getCells();
            for (int i = 0; i < colCount; i++) {
                String cellText = i < cells.size() ? cells.get(i).getContent() : "";
                colWidths[i] = Math.max(colWidths[i], displayWidth(cellText));
            }
        }

        StringBuilder sb = new StringBuilder();

        // 1. Header
        sb.append("|");
        for (int i = 0; i < colCount; i++) {
            String cellText = i < headerCells.size() ? headerCells.get(i).getContent() : "";
            ColumnAlignment align = i < table.getAlignments().size() ? table.getAlignments().get(i) : ColumnAlignment.LEFT;
            sb.append(" ").append(padCell(cellText, colWidths[i], align)).append(" |");
        }
        sb.append("\n");

        // 2. Separator
        sb.append("|");
        for (int i = 0; i < colCount; i++) {
            ColumnAlignment align = i < table.getAlignments().size() ? table.getAlignments().get(i) : ColumnAlignment.LEFT;
            int width = Math.max(colWidths[i], 3);
            String sep;
            if (align == ColumnAlignment.CENTER) {
                sep = ":" + "-".repeat(Math.max(1, width - 2)) + ":";
            } else if (align == ColumnAlignment.RIGHT) {
                sep = "-".repeat(Math.max(2, width - 1)) + ":";
            } else if (align == ColumnAlignment.LEFT) {
                sep = ":" + "-".repeat(Math.max(2, width - 1));
            } else {
                sep = "-".repeat(width);
            }
            sb.append(" ").append(sep).append(" |");
        }
        sb.append("\n");

        // 3. Data Rows
        for (TableRow row : table.getDataRows()) {
            sb.append("|");
            List<TableCell> cells = row.getCells();
            for (int i = 0; i < colCount; i++) {
                String cellText = i < cells.size() ? cells.get(i).getContent() : "";
                ColumnAlignment align = i < table.getAlignments().size() ? table.getAlignments().get(i) : ColumnAlignment.LEFT;
                sb.append(" ").append(padCell(cellText, colWidths[i], align)).append(" |");
            }
            sb.append("\n");
        }

        String result = sb.toString();
        while (result.endsWith("\n")) {
            result = result.substring(0, result.length() - 1);
        }
        return result;
    }

    public static boolean formatTableAtCaret(Project project, Document document, int offset) {
        int lineNum = document.getLineNumber(offset);
        ParsedTable table = PureDownTableParser.findTableAroundLine(document, lineNum);
        if (table == null) return false;

        String formatted = format(table);
        int startOffset = document.getLineStartOffset(table.getStartLine());
        int endOffset = document.getLineEndOffset(table.getEndLine());

        WriteCommandAction.runWriteCommandAction(project, "Format PureDown Table", null, () -> {
            document.replaceString(startOffset, endOffset, formatted);
        });
        return true;
    }

    public static String generateTableTemplate(int rows, int cols) {
        StringBuilder sb = new StringBuilder();
        sb.append("|");
        for (int c = 1; c <= cols; c++) {
            sb.append(" Header ").append(c).append(" |");
        }
        sb.append("\n|");
        for (int c = 1; c <= cols; c++) {
            sb.append(" :--- |");
        }
        sb.append("\n");
        for (int r = 1; r <= rows; r++) {
            sb.append("|");
            for (int c = 1; c <= cols; c++) {
                sb.append(" Item ").append(r).append("-").append(c).append(" |");
            }
            sb.append("\n");
        }
        ParsedTable table = PureDownTableParser.findTableAroundLine(new DocumentImpl(sb.toString()), 0);
        return table != null ? format(table) : sb.toString().trim();
    }
}
