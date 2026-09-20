package com.puredown.table;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

public class ParsedTable {
    private final int startLine;
    private final int endLine;
    private final TableRow headerRow;
    private final List<ColumnAlignment> alignments;
    private final List<TableRow> dataRows;
    private final int columnCount;

    public ParsedTable(int startLine, int endLine, TableRow headerRow,
                       List<ColumnAlignment> alignments, List<TableRow> dataRows, int columnCount) {
        this.startLine = startLine;
        this.endLine = endLine;
        this.headerRow = headerRow;
        this.alignments = alignments != null ? new ArrayList<>(alignments) : Collections.emptyList();
        this.dataRows = dataRows != null ? new ArrayList<>(dataRows) : Collections.emptyList();
        this.columnCount = columnCount;
    }

    public int getStartLine() {
        return startLine;
    }

    public int getEndLine() {
        return endLine;
    }

    public TableRow getHeaderRow() {
        return headerRow;
    }

    public List<ColumnAlignment> getAlignments() {
        return alignments;
    }

    public List<TableRow> getDataRows() {
        return dataRows;
    }

    public int getColumnCount() {
        return columnCount;
    }
}
