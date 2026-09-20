package com.puredown.inspection;

import com.intellij.codeInspection.*;
import com.intellij.openapi.editor.Document;
import com.intellij.openapi.project.Project;
import com.intellij.openapi.util.TextRange;
import com.intellij.psi.PsiFile;
import com.puredown.table.ParsedTable;
import com.puredown.table.PureDownTableFormatter;
import com.puredown.table.PureDownTableParser;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.ArrayList;
import java.util.List;

public class PureDownTableInspection extends LocalInspectionTool {

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
        int line = 0;

        while (line < document.getLineCount()) {
            ParsedTable table = PureDownTableParser.findTableAroundLine(document, line);
            if (table != null) {
                String formatted = PureDownTableFormatter.format(table);
                int startOffset = document.getLineStartOffset(table.getStartLine());
                int endOffset = document.getLineEndOffset(table.getEndLine());
                String currentText = document.getCharsSequence().subSequence(startOffset, endOffset).toString();

                if (!currentText.equals(formatted)) {
                    int headerEnd = document.getLineEndOffset(table.getStartLine());
                    final int fStartOffset = startOffset;
                    problems.add(manager.createProblemDescriptor(
                            file,
                            new TextRange(startOffset, headerEnd),
                            "PureDown: Table columns are unaligned",
                            ProblemHighlightType.WEAK_WARNING,
                            isOnTheFly,
                            new LocalQuickFix() {
                                @NotNull
                                @Override
                                public String getFamilyName() {
                                    return "Format Table Alignment";
                                }

                                @Override
                                public void applyFix(@NotNull Project project, @NotNull ProblemDescriptor descriptor) {
                                    PureDownTableFormatter.formatTableAtCaret(project, document, fStartOffset);
                                }
                            }
                    ));
                }
                line = table.getEndLine() + 1;
            } else {
                line++;
            }
        }

        return problems.isEmpty() ? null : problems.toArray(new ProblemDescriptor[0]);
    }
}
