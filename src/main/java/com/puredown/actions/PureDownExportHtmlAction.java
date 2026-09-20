package com.puredown.actions;

import com.intellij.openapi.actionSystem.AnAction;
import com.intellij.openapi.actionSystem.AnActionEvent;
import com.intellij.openapi.actionSystem.CommonDataKeys;
import com.intellij.openapi.editor.Editor;
import com.intellij.openapi.fileChooser.FileChooserFactory;
import com.intellij.openapi.fileChooser.FileSaverDescriptor;
import com.intellij.openapi.fileChooser.FileSaverDialog;
import com.intellij.openapi.project.Project;
import com.intellij.openapi.ui.Messages;
import com.intellij.openapi.vfs.VirtualFileWrapper;
import com.intellij.psi.PsiFile;
import com.puredown.PureDownIcons;
import com.puredown.preview.PureDownHtmlGenerator;
import org.jetbrains.annotations.NotNull;

import java.lang.reflect.Constructor;

import java.io.InputStream;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;

public class PureDownExportHtmlAction extends AnAction {

    public PureDownExportHtmlAction() {
        super("Export as HTML", "Export styled standalone HTML file", PureDownIcons.EXPORT);
    }

    @Override
    public void actionPerformed(@NotNull AnActionEvent e) {
        Project project = e.getProject();
        Editor editor = e.getData(CommonDataKeys.EDITOR);
        PsiFile psiFile = e.getData(CommonDataKeys.PSI_FILE);
        if (project == null || editor == null) return;

        String baseName = psiFile != null && psiFile.getVirtualFile() != null
                ? psiFile.getVirtualFile().getNameWithoutExtension()
                : "document";

        FileSaverDescriptor descriptor = createFileSaverDescriptor("Export HTML", "Choose destination for exported HTML file");
        FileSaverDialog dialog = FileChooserFactory.getInstance().createSaveFileDialog(descriptor, project);
        VirtualFileWrapper fileWrapper = dialog.save(psiFile != null && psiFile.getVirtualFile() != null ? psiFile.getVirtualFile().getParent() : null, baseName + ".html");
        if (fileWrapper == null) return;

        String text = editor.getDocument().getText();
        String bodyHtml = PureDownHtmlGenerator.generateHtml(text);
        String css = "";
        try (InputStream stream = getClass().getResourceAsStream("/preview/puredown.css")) {
            if (stream != null) {
                css = new String(stream.readAllBytes(), StandardCharsets.UTF_8);
            }
        } catch (Exception ignored) {}

        String fullHtml = "<!DOCTYPE html>\n" +
                "<html lang=\"en\">\n" +
                "<head>\n" +
                "  <meta charset=\"UTF-8\">\n" +
                "  <meta name=\"viewport\" content=\"width=device-width, initial-scale=1.0\">\n" +
                "  <title>" + baseName + "</title>\n" +
                "  <style>\n" + css + "\n" +
                "  #puredown-content { max-width: 900px; margin: 40px auto; }\n" +
                "  </style>\n" +
                "</head>\n" +
                "<body>\n" +
                "  <div id=\"puredown-content\">\n" +
                bodyHtml + "\n" +
                "  </div>\n" +
                "</body>\n" +
                "</html>";

        try {
            Files.writeString(fileWrapper.getFile().toPath(), fullHtml, StandardCharsets.UTF_8);
            Messages.showInfoMessage(
                    project,
                    com.puredown.i18n.PureDownI18n.message("已成功导出至:\n" + fileWrapper.getFile().getAbsolutePath(), "Exported successfully to:\n" + fileWrapper.getFile().getAbsolutePath()),
                    com.puredown.i18n.PureDownI18n.message("PureDown 导出", "PureDown Export")
            );
        } catch (Exception ex) {
            Messages.showErrorDialog(
                    project,
                    com.puredown.i18n.PureDownI18n.message("导出 HTML 失败: " + ex.getMessage(), "Failed to export HTML: " + ex.getMessage()),
                    com.puredown.i18n.PureDownI18n.message("导出错误", "PureDown Export Error")
            );
        }
    }

    @Override
    public void update(@NotNull AnActionEvent e) {
        e.getPresentation().setEnabled(e.getData(CommonDataKeys.EDITOR) != null);
        e.getPresentation().setText(com.puredown.i18n.PureDownI18n.message("导出独立 HTML...", "Export as HTML..."));
        e.getPresentation().setDescription(com.puredown.i18n.PureDownI18n.message("导出带完整样式的独立 HTML 文件", "Export styled standalone HTML file"));
    }

    private static FileSaverDescriptor createFileSaverDescriptor(String title, String description) {
        try {
            Constructor<FileSaverDescriptor> ctor = FileSaverDescriptor.class.getConstructor(String.class, String.class);
            return ctor.newInstance(title, description);
        } catch (Throwable ignored) {
            try {
                Constructor<FileSaverDescriptor> ctor = FileSaverDescriptor.class.getConstructor(String.class, String.class, String[].class);
                return ctor.newInstance(title, description, new String[]{"html"});
            } catch (Throwable t) {
                throw new RuntimeException("Failed to instantiate FileSaverDescriptor", t);
            }
        }
    }
}
