package com.puredown.obsidian;

import com.intellij.codeInsight.completion.*;
import com.intellij.codeInsight.lookup.LookupElementBuilder;
import com.intellij.openapi.editor.Document;
import com.intellij.patterns.PlatformPatterns;
import com.intellij.util.ProcessingContext;
import com.puredown.PureDownIcons;
import org.jetbrains.annotations.NotNull;

import java.util.List;

public class PureDownWikiLinkCompletionContributor extends CompletionContributor {

    public PureDownWikiLinkCompletionContributor() {
        extend(CompletionType.BASIC, PlatformPatterns.psiElement(), new CompletionProvider<CompletionParameters>() {
            @Override
            protected void addCompletions(@NotNull CompletionParameters parameters,
                                          @NotNull ProcessingContext context,
                                          @NotNull CompletionResultSet result) {
                Document document = parameters.getEditor().getDocument();
                int offset = parameters.getOffset();
                int line = document.getLineNumber(offset);
                int lineStart = document.getLineStartOffset(line);
                String prefixText = document.getCharsSequence().subSequence(lineStart, offset).toString();

                int openBracketIdx = prefixText.lastIndexOf("[[");
                if (openBracketIdx != -1) {
                    int closeBracketIdx = prefixText.lastIndexOf("]]");
                    if (closeBracketIdx < openBracketIdx) {
                        List<String> notes = PureDownWikiLinkResolver.listAllNotes(parameters.getPosition().getProject());
                        for (String note : notes) {
                            result.addElement(LookupElementBuilder.create(note)
                                    .withIcon(PureDownIcons.PUREDOWN)
                                    .withTypeText("Note")
                                    .withInsertHandler((ctx, item) -> {
                                        int tailOffset = ctx.getTailOffset();
                                        Document doc = ctx.getDocument();
                                        int end = Math.min(tailOffset + 2, doc.getTextLength());
                                        String remaining = doc.getCharsSequence().subSequence(tailOffset, end).toString();
                                        if (!remaining.equals("]]")) {
                                            doc.insertString(tailOffset, "]]");
                                            ctx.getEditor().getCaretModel().moveToOffset(tailOffset + 2);
                                        }
                                    }));
                        }
                    }
                }
            }
        });
    }
}
