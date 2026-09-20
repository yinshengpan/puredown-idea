package com.puredown.table;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

public class TableRow {
    private final List<TableCell> cells;
    private final boolean isSeparator;

    public TableRow(List<TableCell> cells, boolean isSeparator) {
        this.cells = cells != null ? new ArrayList<>(cells) : Collections.emptyList();
        this.isSeparator = isSeparator;
    }

    public TableRow(List<TableCell> cells) {
        this(cells, false);
    }

    public List<TableCell> getCells() {
        return cells;
    }

    public boolean isSeparator() {
        return isSeparator;
    }
}
