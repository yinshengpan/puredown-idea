package com.puredown;

import com.intellij.lang.Language;

public class PureDownLanguage extends Language {
    public static final PureDownLanguage INSTANCE = new PureDownLanguage();

    private PureDownLanguage() {
        super("PureDown");
    }
}
