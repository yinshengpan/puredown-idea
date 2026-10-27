package com.puredown.editor;

import com.intellij.codeHighlighting.BackgroundEditorHighlighter;
import com.intellij.openapi.actionSystem.*;
import com.intellij.openapi.editor.Editor;
import com.intellij.openapi.editor.VisualPosition;
import com.intellij.openapi.editor.event.DocumentEvent;
import com.intellij.openapi.editor.event.DocumentListener;
import com.intellij.openapi.editor.event.VisibleAreaEvent;
import com.intellij.openapi.editor.event.VisibleAreaListener;
import com.intellij.openapi.fileEditor.FileEditor;
import com.intellij.openapi.fileEditor.FileEditorLocation;
import com.intellij.openapi.fileEditor.FileEditorState;
import com.intellij.openapi.fileEditor.TextEditor;
import com.intellij.openapi.project.Project;
import com.intellij.openapi.util.Disposer;
import com.intellij.openapi.util.UserDataHolderBase;
import com.intellij.openapi.vfs.VirtualFile;
import com.intellij.ui.JBColor;
import com.intellij.ui.JBSplitter;
import com.intellij.util.Alarm;
import com.intellij.util.ui.JBUI;
import com.puredown.PureDownIcons;
import com.puredown.actions.*;
import com.puredown.preview.PureDownJBCefPreview;
import com.puredown.settings.PureDownSettings;
import com.puredown.table.PureDownTableActions;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import javax.swing.*;
import java.awt.*;
import java.beans.PropertyChangeListener;

public class PureDownSplitEditor extends UserDataHolderBase implements FileEditor {

    private final Project project;
    private final VirtualFile file;
    private final TextEditor textEditor;
    private final PureDownJBCefPreview preview;
    private final PureDownFloatingToolbar floatingToolbar;
    private final PureDownStatusBarPanel statusBarPanel;

    private final JPanel rootPanel = new JPanel(new BorderLayout());
    private final JBSplitter splitter = new JBSplitter(false, 0.5f);
    private final Alarm updateAlarm = new Alarm(Alarm.ThreadToUse.SWING_THREAD, this);

    private SplitLayoutMode layoutMode = SplitLayoutMode.SPLIT;
    private boolean isSyncScrollEnabled = true;
    private ActionToolbar actionToolbar;
    private VisibleAreaListener visibleAreaListener;
    private volatile boolean myDisposed = false;

    public PureDownSplitEditor(Project project, VirtualFile file, TextEditor textEditor) {
        this.project = project;
        this.file = file;
        this.textEditor = textEditor;
        this.preview = new PureDownJBCefPreview(project, textEditor.getEditor().getDocument(), this);
        this.floatingToolbar = new PureDownFloatingToolbar(project, textEditor.getEditor());
        this.statusBarPanel = new PureDownStatusBarPanel(textEditor.getEditor(), file);

        PureDownSettings settings = PureDownSettings.getInstance();
        this.layoutMode = settings.getDefaultLayoutMode();
        this.isSyncScrollEnabled = settings.isSyncScrolling();

        Disposer.register(this, textEditor);
        Disposer.register(this, preview);
        Disposer.register(this, floatingToolbar);
        Disposer.register(this, statusBarPanel);

        splitter.setFirstComponent(textEditor.getComponent());
        splitter.setSecondComponent(preview.getComponent());

        JComponent toolbarComponent = createToolbar();
        rootPanel.add(toolbarComponent, BorderLayout.NORTH);
        rootPanel.add(splitter, BorderLayout.CENTER);
        rootPanel.add(statusBarPanel, BorderLayout.SOUTH);

        // Apply initial layout mode from settings
        setLayoutMode(this.layoutMode);

        // Settings change listener for instant dynamic i18n & preference updates
        PureDownSettings.SettingsChangeListener settingsListener = () -> {
            if (actionToolbar != null) {
                try {
                    ActionToolbar.class.getMethod("updateActionsAsync").invoke(actionToolbar);
                } catch (Throwable ignored) {
                    try {
                        ActionToolbar.class.getMethod("updateActionsImmediately").invoke(actionToolbar);
                    } catch (Throwable ignored2) {}
                }
            }
            if (statusBarPanel != null) {
                statusBarPanel.updateCaretInfo();
                statusBarPanel.updateStats();
            }
            schedulePreviewUpdate(0);
        };
        settings.addSettingsChangeListener(settingsListener);
        Disposer.register(this, () -> settings.removeSettingsChangeListener(settingsListener));

        // Real-time live update
        textEditor.getEditor().getDocument().addDocumentListener(new DocumentListener() {
            @Override
            public void documentChanged(@NotNull DocumentEvent event) {
                schedulePreviewUpdate();
            }
        }, this);

        // Scroll sync
        this.visibleAreaListener = new VisibleAreaListener() {
            @Override
            public void visibleAreaChanged(@NotNull VisibleAreaEvent e) {
                if (!isSyncScrollEnabled || layoutMode == SplitLayoutMode.EDITOR_ONLY) return;
                Editor ed = textEditor.getEditor();
                VisualPosition vp = ed.xyToVisualPosition(e.getNewRectangle().getLocation());
                int line = ed.visualToLogicalPosition(vp).line;
                preview.scrollToLine(line);
            }
        };
        textEditor.getEditor().getScrollingModel().addVisibleAreaListener(visibleAreaListener);

        schedulePreviewUpdate(0);
    }

    private JComponent createToolbar() {
        DefaultActionGroup actionGroup = new DefaultActionGroup();

        actionGroup.add(new AnAction(
                com.puredown.i18n.PureDownI18n.message("仅编辑", "Editor Only"),
                com.puredown.i18n.PureDownI18n.message("仅显示编辑器单栏", "Show editor only"),
                PureDownIcons.EDITOR) {
            @Override
            public void actionPerformed(@NotNull AnActionEvent e) {
                setLayoutMode(SplitLayoutMode.EDITOR_ONLY);
            }

            @Override
            public void update(@NotNull AnActionEvent e) {
                e.getPresentation().setText(com.puredown.i18n.PureDownI18n.message("仅编辑", "Editor Only"));
                e.getPresentation().setDescription(com.puredown.i18n.PureDownI18n.message("仅显示编辑器单栏", "Show editor only"));
            }
        });
        actionGroup.add(new AnAction(
                com.puredown.i18n.PureDownI18n.message("双栏分屏", "Split Mode"),
                com.puredown.i18n.PureDownI18n.message("同时显示编辑与即时预览", "Show editor and preview side-by-side"),
                PureDownIcons.SPLIT) {
            @Override
            public void actionPerformed(@NotNull AnActionEvent e) {
                setLayoutMode(SplitLayoutMode.SPLIT);
            }

            @Override
            public void update(@NotNull AnActionEvent e) {
                e.getPresentation().setText(com.puredown.i18n.PureDownI18n.message("双栏分屏", "Split Mode"));
                e.getPresentation().setDescription(com.puredown.i18n.PureDownI18n.message("同时显示编辑与即时预览", "Show editor and preview side-by-side"));
            }
        });
        actionGroup.add(new AnAction(
                com.puredown.i18n.PureDownI18n.message("仅预览", "Preview Only"),
                com.puredown.i18n.PureDownI18n.message("仅显示渲染预览单栏", "Show preview only"),
                PureDownIcons.PREVIEW) {
            @Override
            public void actionPerformed(@NotNull AnActionEvent e) {
                setLayoutMode(SplitLayoutMode.PREVIEW_ONLY);
            }

            @Override
            public void update(@NotNull AnActionEvent e) {
                e.getPresentation().setText(com.puredown.i18n.PureDownI18n.message("仅预览", "Preview Only"));
                e.getPresentation().setDescription(com.puredown.i18n.PureDownI18n.message("仅显示渲染预览单栏", "Show preview only"));
            }
        });

        actionGroup.addSeparator();

        actionGroup.add(new ToggleAction(
                com.puredown.i18n.PureDownI18n.message("同步滚动", "Sync Scroll"),
                com.puredown.i18n.PureDownI18n.message("双向同步编辑与预览滚动条", "Synchronize editor and preview scroll position"),
                PureDownIcons.SYNC) {
            @Override
            public boolean isSelected(@NotNull AnActionEvent e) {
                return isSyncScrollEnabled;
            }

            @Override
            public void setSelected(@NotNull AnActionEvent e, boolean state) {
                isSyncScrollEnabled = state;
            }

            @Override
            public void update(@NotNull AnActionEvent e) {
                super.update(e);
                e.getPresentation().setText(com.puredown.i18n.PureDownI18n.message("同步滚动", "Sync Scroll"));
                e.getPresentation().setDescription(com.puredown.i18n.PureDownI18n.message("双向同步编辑与预览滚动条", "Synchronize editor and preview scroll position"));
            }
        });

        actionGroup.addSeparator();

        actionGroup.add(new PureDownToggleTaskAction());
        actionGroup.add(new PureDownTableActions.InsertTableAction());
        actionGroup.add(new PureDownTableActions.FormatTableAction());
        actionGroup.add(new PureDownInsertCalloutAction());
        actionGroup.add(new PureDownRenumberListAction());

        actionGroup.addSeparator();

        actionGroup.add(new PureDownShowVaultGraphAction());
        actionGroup.add(new PureDownCopyHtmlAction());
        actionGroup.add(new PureDownExportHtmlAction());

        ActionToolbar toolbar = ActionManager.getInstance().createActionToolbar("PureDownEditorToolbar", actionGroup, true);
        toolbar.setTargetComponent(rootPanel);
        this.actionToolbar = toolbar;
        JComponent comp = toolbar.getComponent();
        comp.setBorder(JBUI.Borders.customLine(JBColor.border(), 0, 0, 1, 0));
        return comp;
    }

    public void setLayoutMode(SplitLayoutMode mode) {
        this.layoutMode = mode;
        switch (mode) {
            case EDITOR_ONLY -> {
                splitter.setFirstComponent(textEditor.getComponent());
                splitter.setSecondComponent(null);
            }
            case PREVIEW_ONLY -> {
                splitter.setFirstComponent(null);
                splitter.setSecondComponent(preview.getComponent());
            }
            case SPLIT -> {
                splitter.setFirstComponent(textEditor.getComponent());
                splitter.setSecondComponent(preview.getComponent());
                splitter.setProportion(0.5f);
            }
        }
        splitter.revalidate();
        splitter.repaint();
    }

    private void schedulePreviewUpdate() {
        schedulePreviewUpdate(120);
    }

    private void schedulePreviewUpdate(int delayMs) {
        updateAlarm.cancelAllRequests();
        updateAlarm.addRequest(() -> {
            if (!myDisposed) {
                String md = textEditor.getEditor().getDocument().getText();
                preview.renderMarkdown(md);
            }
        }, delayMs);
    }

    @NotNull
    @Override
    public JComponent getComponent() {
        return rootPanel;
    }

    @Nullable
    @Override
    public JComponent getPreferredFocusedComponent() {
        return textEditor.getPreferredFocusedComponent();
    }

    @NotNull
    @Override
    public String getName() {
        return "PureDown Split Editor";
    }

    @Override
    public void setState(@NotNull FileEditorState state) {
        textEditor.setState(state);
    }

    @Override
    public boolean isModified() {
        return textEditor.isModified();
    }

    @Override
    public boolean isValid() {
        return textEditor.isValid();
    }

    @Override
    public void addPropertyChangeListener(@NotNull PropertyChangeListener listener) {
        textEditor.addPropertyChangeListener(listener);
    }

    @Override
    public void removePropertyChangeListener(@NotNull PropertyChangeListener listener) {
        textEditor.removePropertyChangeListener(listener);
    }

    @Nullable
    @Override
    public FileEditorLocation getCurrentLocation() {
        return textEditor.getCurrentLocation();
    }

    @Nullable
    @Override
    public BackgroundEditorHighlighter getBackgroundHighlighter() {
        return textEditor.getBackgroundHighlighter();
    }

    @Nullable
    @Override
    public VirtualFile getFile() {
        return file;
    }

    @Override
    public void dispose() {
        myDisposed = true;
        if (visibleAreaListener != null) {
            textEditor.getEditor().getScrollingModel().removeVisibleAreaListener(visibleAreaListener);
            visibleAreaListener = null;
        }
        floatingToolbar.hideToolbar();
        updateAlarm.cancelAllRequests();
    }
}
