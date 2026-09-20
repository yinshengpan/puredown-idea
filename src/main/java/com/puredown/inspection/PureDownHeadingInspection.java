package com.puredown.inspection;

import com.intellij.codeInspection.*;
import com.intellij.openapi.command.WriteCommandAction;
import com.intellij.openapi.editor.Document;
import com.intellij.openapi.project.Project;
import com.intellij.openapi.util.TextRange;
import com.intellij.psi.PsiFile;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.ArrayList;
import java.util.List;

public class PureDownHeadingInspection extends LocalInspectionTool {

    @Nullable
    @Override
    public ProblemDescriptor[] checkFile(@NotNull PsiFile file, @NotNull InspectionManager manager, boolean isOnTheFly) {
        if (file.getVirtualFile() == null) return null;
        String ext = file.getVirtualFile().getExtension();
        if (ext == null) return null;
        ext = ext.toLowerCase();
        if (!ext.equals("md") && !ext.equals("markdown") && !ext.equals("mdx")) return null;

        Document document = file.getViewProvider().getDocument();
        if (document == null) return null;

        List<ProblemDescriptor> problems = new ArrayList<>();
        int previousLevel = 0;

        for (int line = 0; line < document.getLineCount(); line++) {
            int start = document.getLineStartOffset(line);
            int end = document.getLineEndOffset(line);
            String text = document.getCharsSequence().subSequence(start, end).toString().trim();

            if (text.startsWith("#")) {
                int level = 0;
                while (level < text.length() && text.charAt(level) == '#') {
                    level++;
                }
                if (level >= 1 && level <= 6 && text.length() > level && text.charAt(level) == ' ') {
                    if (previousLevel > 0 && level > previousLevel + 1) {
                        int expectedLevel = previousLevel + 1;
                        final int fStart = start;
                        final int fLevel = level;
                        problems.add(manager.createProblemDescriptor(
                                file,
                                new TextRange(start, start + level),
                                "PureDown: Heading level skipped (H" + previousLevel + " -> H" + level + "). Expected H" + expectedLevel + ".",
                                ProblemHighlightType.GENERIC_ERROR_OR_WARNING,
                                isOnTheFly,
                                new LocalQuickFix() {
                                    @NotNull
                                    @Override
                                    public String getFamilyName() {
                                        return "Fix heading level to H" + expectedLevel;
                                    }

                                    @Override
                                    public void applyFix(@NotNull Project project, @NotNull ProblemDescriptor descriptor) {
                                        WriteCommandAction.runWriteCommandAction(project, "Fix Heading Level", null, () -> {
                                            document.replaceString(fStart, fStart + fLevel, "#".repeat(expectedLevel));
                                        });
                                    }
                                }
                        ));
                    }
                    previousLevel = level;
                }
            }
        }

        return problems.isEmpty() ? null : problems.toArray(new ProblemDescriptor[0]);
    }
}
