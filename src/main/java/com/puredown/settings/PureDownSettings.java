package com.puredown.settings;

import com.intellij.openapi.application.ApplicationManager;
import com.intellij.openapi.components.PersistentStateComponent;
import com.intellij.openapi.components.State;
import com.intellij.openapi.components.Storage;
import com.intellij.util.xmlb.XmlSerializerUtil;
import com.puredown.editor.SplitLayoutMode;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

@State(
    name = "PureDownSettings",
    storages = @Storage("puredown.xml")
)
public class PureDownSettings implements PersistentStateComponent<PureDownSettings> {

    private SplitLayoutMode defaultLayoutMode = SplitLayoutMode.SPLIT;
    private com.puredown.i18n.AppLanguage language = com.puredown.i18n.AppLanguage.AUTO;
    private boolean syncScrolling = true;
    private boolean showStatusBarStats = true;
    private boolean enableYamlFrontMatter = true;
    private boolean enableInDocToc = true;
    private boolean enableLightbox = true;
    private boolean enableKatex = true;
    private boolean enableMermaid = true;

    public static PureDownSettings getInstance() {
        return ApplicationManager.getApplication().getService(PureDownSettings.class);
    }

    @Nullable
    @Override
    public PureDownSettings getState() {
        return this;
    }

    @Override
    public void loadState(@NotNull PureDownSettings state) {
        XmlSerializerUtil.copyBean(state, this);
    }

    public SplitLayoutMode getDefaultLayoutMode() {
        return defaultLayoutMode != null ? defaultLayoutMode : SplitLayoutMode.SPLIT;
    }

    public void setDefaultLayoutMode(SplitLayoutMode defaultLayoutMode) {
        this.defaultLayoutMode = defaultLayoutMode;
    }

    public com.puredown.i18n.AppLanguage getLanguage() {
        return language != null ? language : com.puredown.i18n.AppLanguage.AUTO;
    }

    public void setLanguage(com.puredown.i18n.AppLanguage language) {
        this.language = language;
    }

    public boolean isSyncScrolling() {
        return syncScrolling;
    }

    public void setSyncScrolling(boolean syncScrolling) {
        this.syncScrolling = syncScrolling;
    }

    public boolean isShowStatusBarStats() {
        return showStatusBarStats;
    }

    public void setShowStatusBarStats(boolean showStatusBarStats) {
        this.showStatusBarStats = showStatusBarStats;
    }

    public boolean isEnableYamlFrontMatter() {
        return enableYamlFrontMatter;
    }

    public void setEnableYamlFrontMatter(boolean enableYamlFrontMatter) {
        this.enableYamlFrontMatter = enableYamlFrontMatter;
    }

    public boolean isEnableInDocToc() {
        return enableInDocToc;
    }

    public void setEnableInDocToc(boolean enableInDocToc) {
        this.enableInDocToc = enableInDocToc;
    }

    public boolean isEnableLightbox() {
        return enableLightbox;
    }

    public void setEnableLightbox(boolean enableLightbox) {
        this.enableLightbox = enableLightbox;
    }

    public boolean isEnableKatex() {
        return enableKatex;
    }

    public void setEnableKatex(boolean enableKatex) {
        this.enableKatex = enableKatex;
    }

    public boolean isEnableMermaid() {
        return enableMermaid;
    }

    public void setEnableMermaid(boolean enableMermaid) {
        this.enableMermaid = enableMermaid;
    }
}
