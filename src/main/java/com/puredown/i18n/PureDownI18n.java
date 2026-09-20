package com.puredown.i18n;

import com.puredown.settings.PureDownSettings;
import java.util.Locale;

public final class PureDownI18n {
    private PureDownI18n() {}

    public static boolean isChinese() {
        try {
            PureDownSettings settings = PureDownSettings.getInstance();
            if (settings != null) {
                AppLanguage lang = settings.getLanguage();
                if (lang == AppLanguage.ZH_CN) return true;
                if (lang == AppLanguage.EN) return false;
            }
        } catch (Throwable ignored) {}

        Locale locale = Locale.getDefault();
        return "zh".equalsIgnoreCase(locale.getLanguage());
    }

    public static String message(String zh, String en) {
        return isChinese() ? zh : en;
    }

    public static String tr(String zh, String en) {
        return message(zh, en);
    }
}
