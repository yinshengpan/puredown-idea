package com.puredown.obsidian;

import com.intellij.openapi.fileEditor.FileEditorManager;
import com.intellij.openapi.project.Project;
import com.intellij.openapi.vfs.VirtualFile;
import com.intellij.psi.search.FileTypeIndex;
import com.intellij.psi.search.GlobalSearchScope;
import com.puredown.PureDownFileType;

import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.List;

public final class PureDownWikiLinkResolver {
    private PureDownWikiLinkResolver() {}

    public static VirtualFile resolve(Project project, String rawTarget) {
        if (project == null || rawTarget == null) return null;
        String targetName = rawTarget.split("#")[0].split("\\|")[0].trim();
        if (targetName.isEmpty()) return null;

        Collection<VirtualFile> files = FileTypeIndex.getFiles(PureDownFileType.INSTANCE, GlobalSearchScope.projectScope(project));

        for (VirtualFile file : files) {
            if (file.getName().equalsIgnoreCase(targetName)) {
                return file;
            }
        }

        for (VirtualFile file : files) {
            if (file.getNameWithoutExtension().equalsIgnoreCase(targetName)) {
                return file;
            }
        }

        for (VirtualFile file : files) {
            String path = file.getPath();
            if (path.endsWith("/" + targetName) || path.endsWith("/" + targetName + ".md")) {
                return file;
            }
        }

        return null;
    }

    public static void openNote(Project project, String rawTarget) {
        VirtualFile file = resolve(project, rawTarget);
        if (file != null) {
            new com.intellij.openapi.fileEditor.OpenFileDescriptor(project, file).navigate(true);
        }
    }

    public static List<String> listAllNotes(Project project) {
        if (project == null) return Collections.emptyList();
        Collection<VirtualFile> files = FileTypeIndex.getFiles(PureDownFileType.INSTANCE, GlobalSearchScope.projectScope(project));
        List<String> list = new ArrayList<>();
        for (VirtualFile file : files) {
            list.add(file.getNameWithoutExtension());
        }
        Collections.sort(list);
        return list;
    }
}
