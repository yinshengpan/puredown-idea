package com.puredown.preview;

import com.intellij.openapi.Disposable;
import com.intellij.openapi.command.WriteCommandAction;
import com.intellij.openapi.editor.Document;
import com.intellij.openapi.project.Project;
import com.intellij.ui.jcef.JBCefApp;
import com.intellij.ui.jcef.JBCefBrowser;
import com.intellij.ui.jcef.JBCefJSQuery;
import com.intellij.util.ui.UIUtil;
import com.puredown.obsidian.PureDownWikiLinkResolver;
import org.cef.browser.CefBrowser;
import org.cef.browser.CefFrame;
import org.cef.handler.CefLoadHandlerAdapter;

import javax.swing.*;
import java.awt.*;
import java.io.InputStream;
import java.nio.charset.StandardCharsets;

public class PureDownJBCefPreview implements Disposable {

    private final Project project;
    private final Document document;
    private final JPanel rootPanel = new JPanel(new BorderLayout());

    private JBCefBrowser jbCefBrowser;
    private JEditorPane fallbackPane;
    private boolean isBrowserLoaded = false;
    private String pendingMarkdown = null;

    public PureDownJBCefPreview(Project project, Document document, Disposable parentDisposable) {
        this.project = project;
        this.document = document;

        boolean jcefSupported = false;
        try {
            jcefSupported = JBCefApp.isSupported();
        } catch (Throwable ignored) {}

        if (jcefSupported) {
            try {
                JBCefBrowser browser = new JBCefBrowser();
                this.jbCefBrowser = browser;

                JBCefJSQuery jsQuery = JBCefJSQuery.create(browser);
                jsQuery.addHandler(request -> {
                    handleJsRequest(request);
                    return null;
                });

                browser.getJBCefClient().addLoadHandler(new CefLoadHandlerAdapter() {
                    @Override
                    public void onLoadEnd(CefBrowser b, CefFrame frame, int httpStatusCode) {
                        isBrowserLoaded = true;
                        String injectBridgeJs = "window.cefQuery = function(obj) {" +
                                jsQuery.inject("obj.request") + "};\n" +
                                "window.setTheme(" + UIUtil.isUnderDarcula() + ");";
                        browser.getCefBrowser().executeJavaScript(injectBridgeJs, browser.getCefBrowser().getURL(), 0);

                        if (pendingMarkdown != null) {
                            renderMarkdown(pendingMarkdown);
                            pendingMarkdown = null;
                        }
                    }
                }, browser.getCefBrowser());

                loadTemplate(browser);
                rootPanel.add(browser.getComponent(), BorderLayout.CENTER);
            } catch (Throwable t) {
                initFallback();
            }
        } else {
            initFallback();
        }
    }

    private void initFallback() {
        fallbackPane = new JEditorPane();
        fallbackPane.setEditable(false);
        fallbackPane.setContentType("text/html");
        rootPanel.add(new JScrollPane(fallbackPane), BorderLayout.CENTER);
    }

    public JComponent getComponent() {
        return rootPanel;
    }

    private void loadTemplate(JBCefBrowser browser) {
        String html = readResource("/preview/index.html");
        String css = readResource("/preview/puredown.css");
        String katexCss = readResource("/preview/vendor/katex.min.css");
        String ghCss = readResource("/preview/vendor/github.min.css");
        String katexJs = readResource("/preview/vendor/katex.min.js");
        String hlJs = readResource("/preview/vendor/highlight.min.js");
        String js = readResource("/preview/puredown.js");

        StringBuilder fullCss = new StringBuilder();
        if (!katexCss.isEmpty()) fullCss.append(katexCss).append("\n");
        if (!ghCss.isEmpty()) fullCss.append(ghCss).append("\n");
        if (!css.isEmpty()) fullCss.append(css).append("\n");

        StringBuilder fullJs = new StringBuilder();
        if (!katexJs.isEmpty()) fullJs.append(katexJs).append("\n");
        if (!hlJs.isEmpty()) fullJs.append(hlJs).append("\n");
        if (!js.isEmpty()) fullJs.append(js).append("\n");

        String fullHtml = html.replace("<link rel=\"stylesheet\" href=\"puredown.css\">", "<style>" + fullCss + "</style>")
                .replace("<script src=\"puredown.js\"></script>", "<script>" + fullJs + "</script>");

        browser.loadHTML(fullHtml);
    }

    private String readResource(String path) {
        try (InputStream stream = getClass().getResourceAsStream(path)) {
            if (stream != null) {
                return new String(stream.readAllBytes(), StandardCharsets.UTF_8);
            }
        } catch (Exception ignored) {}
        return "";
    }

    public void renderMarkdown(String markdown) {
        String htmlContent = PureDownHtmlGenerator.generateHtml(markdown);

        if (jbCefBrowser != null) {
            if (!isBrowserLoaded) {
                pendingMarkdown = markdown;
                return;
            }

            boolean isDark = UIUtil.isUnderDarcula();
            String escapedJson = jsonEncode(htmlContent);
            String js = "if (window.setTheme) window.setTheme(" + isDark + ");\n" +
                    "if (window.updateContent) window.updateContent(" + escapedJson + ");";

            jbCefBrowser.getCefBrowser().executeJavaScript(js, jbCefBrowser.getCefBrowser().getURL(), 0);
        } else if (fallbackPane != null) {
            boolean isDark = UIUtil.isUnderDarcula();
            String bg = isDark ? "#1e1f22" : "#ffffff";
            String fg = isDark ? "#bcbec4" : "#24292f";
            String fallbackHtml = "<html><body style='background-color:" + bg + "; color:" + fg + "; font-family:sans-serif; padding:16px;'>" + htmlContent + "</body></html>";
            fallbackPane.setText(fallbackHtml);
        }
    }

    public void scrollToLine(int line) {
        if (jbCefBrowser != null && isBrowserLoaded) {
            jbCefBrowser.getCefBrowser().executeJavaScript("window.scrollToLine(" + line + ");", "", 0);
        }
    }

    private void handleJsRequest(String request) {
        if (request == null) return;
        if (request.startsWith("toggleCheckbox:")) {
            String[] parts = request.split(":");
            if (parts.length >= 3) {
                try {
                    int lineNum = Integer.parseInt(parts[1]);
                    boolean checked = Boolean.parseBoolean(parts[2]);
                    toggleCheckboxInDocument(lineNum, checked);
                } catch (NumberFormatException ignored) {}
            }
        } else if (request.startsWith("openWikiLink:")) {
            String targetNote = request.substring("openWikiLink:".length()).trim();
            if (!targetNote.isEmpty()) {
                PureDownWikiLinkResolver.openNote(project, targetNote);
            }
        } else if (request.startsWith("executeCommand:")) {
            String encoded = request.substring("executeCommand:".length()).trim();
            try {
                String cmd = java.net.URLDecoder.decode(encoded, StandardCharsets.UTF_8);
                com.puredown.runner.PureDownCommandRunner.runCommand(project, cmd);
            } catch (Exception ignored) {}
        }
    }

    private void toggleCheckboxInDocument(int lineNum, boolean checked) {
        if (lineNum < 0 || lineNum >= document.getLineCount()) return;
        int start = document.getLineStartOffset(lineNum);
        int end = document.getLineEndOffset(lineNum);
        String lineText = document.getCharsSequence().subSequence(start, end).toString();

        String newText = null;
        if (checked && lineText.contains("- [ ]")) {
            newText = lineText.replaceFirst("- \\[ \\]", "- [x]");
        } else if (!checked && lineText.contains("- [x]")) {
            newText = lineText.replaceFirst("- \\[x\\]", "- [ ]");
        } else if (!checked && lineText.contains("- [X]")) {
            newText = lineText.replaceFirst("- \\[X\\]", "- [ ]");
        }

        if (newText != null) {
            final String replacement = newText;
            WriteCommandAction.runWriteCommandAction(project, "Toggle Task Checkbox", null, () -> {
                document.replaceString(start, end, replacement);
            });
        }
    }

    private String jsonEncode(String str) {
        StringBuilder sb = new StringBuilder("\"");
        for (int i = 0; i < str.length(); i++) {
            char c = str.charAt(i);
            switch (c) {
                case '\\': sb.append("\\\\"); break;
                case '"': sb.append("\\\""); break;
                case '\n': sb.append("\\n"); break;
                case '\r': sb.append("\\r"); break;
                case '\t': sb.append("\\t"); break;
                case '\b': sb.append("\\b"); break;
                case '\f': sb.append("\\f"); break;
                default:
                    if (c < 0x20) {
                        sb.append(String.format("\\u%04x", (int) c));
                    } else {
                        sb.append(c);
                    }
                    break;
            }
        }
        sb.append("\"");
        return sb.toString();
    }

    @Override
    public void dispose() {
        if (jbCefBrowser != null) {
            jbCefBrowser.dispose();
            jbCefBrowser = null;
        }
    }
}
