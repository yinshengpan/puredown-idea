package com.puredown.editor;

import com.intellij.openapi.fileEditor.FileEditor;
import com.intellij.openapi.fileEditor.FileEditorPolicy;
import com.intellij.openapi.fileEditor.FileEditorProvider;
import com.intellij.openapi.fileEditor.TextEditor;
import com.intellij.openapi.fileEditor.impl.text.TextEditorProvider;
import com.intellij.openapi.project.DumbAware;
import com.intellij.openapi.project.Project;
import com.intellij.openapi.vfs.VirtualFile;
import org.jetbrains.annotations.NotNull;

public class PureDownSplitEditorProvider implements FileEditorProvider, DumbAware {

    @Override
    public boolean accept(@NotNull Project project, @NotNull VirtualFile file) {
        String ext = file.getExtension();
        if (ext == null) return false;
        ext = ext.toLowerCase();
        return ext.equals("md") || ext.equals("markdown") || ext.equals("mdx");
    }

    @NotNull
    @Override
    public FileEditor createEditor(@NotNull Project project, @NotNull VirtualFile file) {
        TextEditor textEditor = (TextEditor) TextEditorProvider.getInstance().createEditor(project, file);
        return new PureDownSplitEditor(project, file, textEditor);
    }

    @NotNull
    @Override
    public String getEditorTypeId() {
        return "puredown-split-editor";
    }

    @NotNull
    @Override
    public FileEditorPolicy getPolicy() {
        return FileEditorPolicy.HIDE_DEFAULT_EDITOR;
    }
}
