package com.puredown;

import com.intellij.openapi.fileTypes.LanguageFileType;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;
import javax.swing.Icon;

public class PureDownFileType extends LanguageFileType {
    public static final PureDownFileType INSTANCE = new PureDownFileType();
    public static final String DEFAULT_EXTENSION = "md";

    private PureDownFileType() {
        super(PureDownLanguage.INSTANCE);
    }

    @NotNull
    @Override
    public String getName() {
        return "PureDown";
    }

    @NotNull
    @Override
    public String getDescription() {
        return "PureDown Markdown document";
    }

    @NotNull
    @Override
    public String getDefaultExtension() {
        return DEFAULT_EXTENSION;
    }

    @Nullable
    @Override
    public Icon getIcon() {
        return PureDownIcons.PUREDOWN;
    }
}
