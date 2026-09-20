package com.puredown.settings;

import com.intellij.openapi.options.SearchableConfigurable;
import com.intellij.openapi.ui.ComboBox;
import com.intellij.ui.components.JBCheckBox;
import com.intellij.ui.components.JBLabel;
import com.intellij.util.ui.FormBuilder;
import com.intellij.util.ui.JBUI;
import com.intellij.util.ui.UIUtil;
import com.puredown.editor.SplitLayoutMode;
import com.puredown.i18n.AppLanguage;
import com.puredown.i18n.PureDownI18n;
import org.jetbrains.annotations.Nls;
import org.jetbrains.annotations.NonNls;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import javax.swing.*;

public class PureDownConfigurable implements SearchableConfigurable {

    private JPanel mainPanel;
    private ComboBox<AppLanguage> languageCombo;
    private ComboBox<LayoutModeItem> defaultLayoutCombo;
    private JBCheckBox syncScrollingCheckBox;
    private JBCheckBox showStatusBarStatsCheckBox;
    private JBCheckBox enableYamlCheckBox;
    private JBCheckBox enableTocCheckBox;
    private JBCheckBox enableKatexCheckBox;
    private JBCheckBox enableMermaidCheckBox;
    private JBCheckBox enableLightboxCheckBox;

    private static class LayoutModeItem {
        final SplitLayoutMode mode;
        final String labelZh;
        final String labelEn;

        LayoutModeItem(SplitLayoutMode mode, String labelZh, String labelEn) {
            this.mode = mode;
            this.labelZh = labelZh;
            this.labelEn = labelEn;
        }

        @Override
        public String toString() {
            return PureDownI18n.message(labelZh, labelEn);
        }
    }

    @NotNull
    @Override
    public @NonNls String getId() {
        return "com.puredown.settings";
    }

    @Nls(capitalization = Nls.Capitalization.Title)
    @Override
    public String getDisplayName() {
        return "PureDown";
    }

    @Nullable
    @Override
    public JComponent createComponent() {
        languageCombo = new ComboBox<>(AppLanguage.values());

        defaultLayoutCombo = new ComboBox<>(new LayoutModeItem[]{
                new LayoutModeItem(SplitLayoutMode.SPLIT, "编辑 + 预览 (Split Mode)", "Split Mode (Editor + Preview)"),
                new LayoutModeItem(SplitLayoutMode.EDITOR_ONLY, "仅编辑 (Editor Only)", "Editor Only"),
                new LayoutModeItem(SplitLayoutMode.PREVIEW_ONLY, "仅预览 (Preview Only)", "Preview Only")
        });

        syncScrollingCheckBox = new JBCheckBox(PureDownI18n.message(
                "启用双向同步滚动 (Synchronized Scrolling)",
                "Synchronize editor and preview scrolling"));

        showStatusBarStatsCheckBox = new JBCheckBox(PureDownI18n.message(
                "显示底部文档统计栏 (字符 / 词数 / 行数 / 预估阅读时间)",
                "Show document statistics bar (chars / words / lines / reading time)"));

        enableYamlCheckBox = new JBCheckBox(PureDownI18n.message(
                "启用 YAML Front Matter 元数据卡片展示",
                "Render YAML Front Matter metadata card"));

        enableTocCheckBox = new JBCheckBox(PureDownI18n.message(
                "启用文内 [TOC] 目录自动生成",
                "Generate interactive in-document [TOC] outline"));

        enableKatexCheckBox = new JBCheckBox(PureDownI18n.message(
                "启用 KaTeX 数学公式离线渲染 ($x$ 与 $$...$$)",
                "Render KaTeX math formulas ($x$ and $$...$$)"));

        enableMermaidCheckBox = new JBCheckBox(PureDownI18n.message(
                "启用 Mermaid 架构与流程图表渲染",
                "Render Mermaid architecture & sequence diagrams"));

        enableLightboxCheckBox = new JBCheckBox(PureDownI18n.message(
                "启用预览图片点击沉浸式灯箱放大 (Lightbox)",
                "Enable image click lightbox zoom"));

        JBLabel viewModeHint = new JBLabel(PureDownI18n.message(
                "选择打开 Markdown 文件时默认呈现的视图模式。",
                "Select default view mode when opening a Markdown file."));
        viewModeHint.setForeground(UIUtil.getContextHelpForeground());
        viewModeHint.setFont(JBUI.Fonts.smallFont());

        mainPanel = FormBuilder.createFormBuilder()
                .addLabeledComponent(new JBLabel(PureDownI18n.message("界面语言 / Language:", "Interface Language:")), languageCombo, 1, false)
                .addSeparator(10)
                .addLabeledComponent(new JBLabel(PureDownI18n.message("默认打开视图模式:", "Default View Mode:")), defaultLayoutCombo, 1, false)
                .addComponentToRightColumn(viewModeHint, 1)
                .addSeparator(15)
                .addComponent(new JBLabel(PureDownI18n.message("编辑器与交互设置:", "Editor & Interaction:")), 1)
                .addComponent(syncScrollingCheckBox, 1)
                .addComponent(showStatusBarStatsCheckBox, 1)
                .addSeparator(15)
                .addComponent(new JBLabel(PureDownI18n.message("语法扩展与高级渲染:", "Markdown Extensions & Rendering:")), 1)
                .addComponent(enableYamlCheckBox, 1)
                .addComponent(enableTocCheckBox, 1)
                .addComponent(enableKatexCheckBox, 1)
                .addComponent(enableMermaidCheckBox, 1)
                .addComponent(enableLightboxCheckBox, 1)
                .addComponentFillVertically(new JPanel(), 0)
                .getPanel();

        reset();
        return mainPanel;
    }

    @Override
    public boolean isModified() {
        PureDownSettings settings = PureDownSettings.getInstance();
        LayoutModeItem selectedItem = (LayoutModeItem) defaultLayoutCombo.getSelectedItem();
        SplitLayoutMode selectedMode = selectedItem != null ? selectedItem.mode : SplitLayoutMode.SPLIT;
        AppLanguage selectedLang = (AppLanguage) languageCombo.getSelectedItem();

        return selectedLang != settings.getLanguage() ||
                selectedMode != settings.getDefaultLayoutMode() ||
                syncScrollingCheckBox.isSelected() != settings.isSyncScrolling() ||
                showStatusBarStatsCheckBox.isSelected() != settings.isShowStatusBarStats() ||
                enableYamlCheckBox.isSelected() != settings.isEnableYamlFrontMatter() ||
                enableTocCheckBox.isSelected() != settings.isEnableInDocToc() ||
                enableKatexCheckBox.isSelected() != settings.isEnableKatex() ||
                enableMermaidCheckBox.isSelected() != settings.isEnableMermaid() ||
                enableLightboxCheckBox.isSelected() != settings.isEnableLightbox();
    }

    @Override
    public void apply() {
        PureDownSettings settings = PureDownSettings.getInstance();
        AppLanguage selectedLang = (AppLanguage) languageCombo.getSelectedItem();
        if (selectedLang != null) {
            settings.setLanguage(selectedLang);
        }

        LayoutModeItem selectedItem = (LayoutModeItem) defaultLayoutCombo.getSelectedItem();
        if (selectedItem != null) {
            settings.setDefaultLayoutMode(selectedItem.mode);
        }
        settings.setSyncScrolling(syncScrollingCheckBox.isSelected());
        settings.setShowStatusBarStats(showStatusBarStatsCheckBox.isSelected());
        settings.setEnableYamlFrontMatter(enableYamlCheckBox.isSelected());
        settings.setEnableInDocToc(enableTocCheckBox.isSelected());
        settings.setEnableKatex(enableKatexCheckBox.isSelected());
        settings.setEnableMermaid(enableMermaidCheckBox.isSelected());
        settings.setEnableLightbox(enableLightboxCheckBox.isSelected());
    }

    @Override
    public void reset() {
        PureDownSettings settings = PureDownSettings.getInstance();
        languageCombo.setSelectedItem(settings.getLanguage());

        SplitLayoutMode currentMode = settings.getDefaultLayoutMode();
        for (int i = 0; i < defaultLayoutCombo.getItemCount(); i++) {
            LayoutModeItem item = defaultLayoutCombo.getItemAt(i);
            if (item.mode == currentMode) {
                defaultLayoutCombo.setSelectedIndex(i);
                break;
            }
        }
        syncScrollingCheckBox.setSelected(settings.isSyncScrolling());
        showStatusBarStatsCheckBox.setSelected(settings.isShowStatusBarStats());
        enableYamlCheckBox.setSelected(settings.isEnableYamlFrontMatter());
        enableTocCheckBox.setSelected(settings.isEnableInDocToc());
        enableKatexCheckBox.setSelected(settings.isEnableKatex());
        enableMermaidCheckBox.setSelected(settings.isEnableMermaid());
        enableLightboxCheckBox.setSelected(settings.isEnableLightbox());
    }
}
