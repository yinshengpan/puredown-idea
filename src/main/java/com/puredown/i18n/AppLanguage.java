package com.puredown.i18n;

public enum AppLanguage {
    AUTO("跟随 IDE / 系统", "Auto (Follow IDE/System)"),
    ZH_CN("简体中文", "Simplified Chinese"),
    EN("English", "English");

    private final String zhName;
    private final String enName;

    AppLanguage(String zhName, String enName) {
        this.zhName = zhName;
        this.enName = enName;
    }

    public String getDisplayName(boolean isZh) {
        return isZh ? zhName : enName;
    }

    @Override
    public String toString() {
        return zhName + " (" + enName + ")";
    }
}
