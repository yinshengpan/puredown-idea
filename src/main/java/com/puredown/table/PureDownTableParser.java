package com.puredown.table;

import com.intellij.openapi.editor.Document;
import java.util.ArrayList;
import java.util.List;
import java.util.regex.Pattern;

public final class PureDownTableParser {
    private static final Pattern SEP_PATTERN = Pattern.compile("^:?-+:?$");

    private PureDownTableParser() {}

    public static boolean isTableLine(String line) {
        if (line == null) return false;
        String trimmed = line.trim();
        if (!trimmed.contains("|")) return false;
        return trimmed.startsWith("|") || trimmed.endsWith("|") || countChar(trimmed, '|') >= 2;
    }

    private static int countChar(String str, char ch) {
        int count = 0;
        for (int i = 0; i < str.length(); i++) {
            if (str.charAt(i) == ch) count++;
        }
        return count;
    }

    public static boolean isSeparatorLine(String line) {
        if (line == null) return false;
        String trimmed = line.trim();
        if (!isTableLine(trimmed)) return false;
        List<String> cells = splitPipes(trimmed);
        if (cells.isEmpty()) return false;
        for (String cell : cells) {
            if (!SEP_PATTERN.matcher(cell.trim()).matches()) {
                return false;
            }
        }
        return true;
    }

    public static List<String> splitPipes(String line) {
        String str = line != null ? line.trim() : "";
        if (str.startsWith("|")) {
            str = str.substring(1);
        }
        if (str.endsWith("|") && !str.endsWith("\\|")) {
            str = str.substring(0, str.length() - 1);
        }

        List<String> result = new ArrayList<>();
        StringBuilder sb = new StringBuilder();
        boolean escaped = false;

        for (int i = 0; i < str.length(); i++) {
            char ch = str.charAt(i);
            if (escaped) {
                sb.append(ch);
                escaped = false;
            } else if (ch == '\\') {
                escaped = true;
                sb.append(ch);
            } else if (ch == '|') {
                result.add(sb.toString().trim());
                sb.setLength(0);
            } else {
                sb.append(ch);
            }
        }
        result.add(sb.toString().trim());
        return result;
    }

    public static ParsedTable findTableAroundLine(Document document, int lineNum) {
        if (document == null) return null;
        int lineCount = document.getLineCount();
        if (lineNum < 0 || lineNum >= lineCount) return null;

        String currentLineText = getLineText(document, lineNum);
        if (!isTableLine(currentLineText)) return null;

        int startLine = lineNum;
        while (startLine > 0 && isTableLine(getLineText(document, startLine - 1))) {
            startLine--;
        }

        int endLine = lineNum;
        while (endLine < lineCount - 1 && isTableLine(getLineText(document, endLine + 1))) {
            endLine++;
        }

        if (endLine - startLine < 1) return null;

        String headerText = getLineText(document, startLine);
        List<String> rawHeaderCells = splitPipes(headerText);
        List<TableCell> headerCells = new ArrayList<>();
        for (String c : rawHeaderCells) {
            headerCells.add(new TableCell(c));
        }
        int colCount = headerCells.size();

        String nextLineText = getLineText(document, startLine + 1);
        boolean isSep = isSeparatorLine(nextLineText);

        List<ColumnAlignment> alignments = new ArrayList<>();
        List<TableRow> dataRows = new ArrayList<>();

        if (isSep) {
            List<String> sepCells = splitPipes(nextLineText);
            colCount = Math.max(colCount, sepCells.size());
            for (String c : sepCells) {
                String t = c.trim();
                if (t.startsWith(":") && t.endsWith(":")) {
                    alignments.add(ColumnAlignment.CENTER);
                } else if (t.endsWith(":")) {
                    alignments.add(ColumnAlignment.RIGHT);
                } else if (t.startsWith(":")) {
                    alignments.add(ColumnAlignment.LEFT);
                } else {
                    alignments.add(ColumnAlignment.LEFT);
                }
            }
            for (int l = startLine + 2; l <= endLine; l++) {
                List<String> rawRow = splitPipes(getLineText(document, l));
                colCount = Math.max(colCount, rawRow.size());
                List<TableCell> rowCells = new ArrayList<>();
                for (String cell : rawRow) {
                    rowCells.add(new TableCell(cell));
                }
                dataRows.add(new TableRow(rowCells));
            }
        } else {
            for (int l = startLine + 1; l <= endLine; l++) {
                List<String> rawRow = splitPipes(getLineText(document, l));
                colCount = Math.max(colCount, rawRow.size());
                List<TableCell> rowCells = new ArrayList<>();
                for (String cell : rawRow) {
                    rowCells.add(new TableCell(cell));
                }
                dataRows.add(new TableRow(rowCells));
            }
        }

        while (alignments.size() < colCount) {
            alignments.add(ColumnAlignment.LEFT);
        }

        return new ParsedTable(startLine, endLine, new TableRow(headerCells), alignments, dataRows, colCount);
    }

    public static String getLineText(Document document, int line) {
        int start = document.getLineStartOffset(line);
        int end = document.getLineEndOffset(line);
        return document.getCharsSequence().subSequence(start, end).toString();
    }
}
