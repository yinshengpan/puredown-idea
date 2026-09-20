package com.puredown.inspection;

import com.intellij.codeInspection.InspectionManager;
import com.intellij.codeInspection.LocalInspectionTool;
import com.intellij.codeInspection.ProblemDescriptor;
import com.intellij.codeInspection.ProblemHighlightType;
import com.intellij.openapi.editor.Document;
import com.intellij.openapi.util.TextRange;
import com.intellij.psi.PsiFile;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.ArrayList;
import java.util.List;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class PureDownLinkInspection extends LocalInspectionTool {
    private static final Pattern EMPTY_URL_PATTERN = Pattern.compile("\\[([^\\]]+)\\]\\(\\s*\\)");
    private static final Pattern EMPTY_TEXT_PATTERN = Pattern.compile("\\[\\s*\\]\\(([^\\)]+)\\)");

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

        String text = document.getText();
        List<ProblemDescriptor> problems = new ArrayList<>();

        Matcher m1 = EMPTY_URL_PATTERN.matcher(text);
        while (m1.find()) {
            problems.add(manager.createProblemDescriptor(
                    file,
                    new TextRange(m1.start(), m1.end()),
                    "PureDown: Link has empty target URL",
                    ProblemHighlightType.GENERIC_ERROR_OR_WARNING,
                    isOnTheFly
            ));
        }

        Matcher m2 = EMPTY_TEXT_PATTERN.matcher(text);
        while (m2.find()) {
            problems.add(manager.createProblemDescriptor(
                    file,
                    new TextRange(m2.start(), m2.end()),
                    "PureDown: Link text is empty",
                    ProblemHighlightType.WEAK_WARNING,
                    isOnTheFly
            ));
        }

        return problems.isEmpty() ? null : problems.toArray(new ProblemDescriptor[0]);
    }
}
