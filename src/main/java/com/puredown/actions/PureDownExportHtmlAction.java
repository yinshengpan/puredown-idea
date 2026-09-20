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

        FileSaverDescriptor descriptor = new FileSaverDescriptor("Export HTML", "Choose destination for exported HTML file", "html");
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
            Messages.showInfoMessage(project, "Exported successfully to:\n" + fileWrapper.getFile().getAbsolutePath(), "PureDown Export");
        } catch (Exception ex) {
            Messages.showErrorDialog(project, "Failed to export HTML: " + ex.getMessage(), "PureDown Export Error");
        }
    }

    @Override
    public void update(@NotNull AnActionEvent e) {
        e.getPresentation().setEnabled(e.getData(CommonDataKeys.EDITOR) != null);
    }
}
