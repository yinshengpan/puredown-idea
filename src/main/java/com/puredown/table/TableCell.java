package com.puredown.table;

public class TableCell {
    private final String content;

    public TableCell(String content) {
        this.content = content != null ? content : "";
    }

    public String getContent() {
        return content;
    }
}
