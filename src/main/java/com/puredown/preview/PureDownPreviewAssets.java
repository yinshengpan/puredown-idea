package com.puredown.preview;

import com.intellij.openapi.application.PathManager;
import java.io.File;
import java.io.FileOutputStream;
import java.io.InputStream;
import java.io.OutputStream;

public final class PureDownPreviewAssets {
    private static final String[] ASSET_FILES = {
            "index.html",
            "puredown.css",
            "puredown.js",
            "vendor/github.min.css",
            "vendor/github-dark.min.css",
            "vendor/highlight.min.js",
            "vendor/katex.min.css",
            "vendor/katex.min.js",
            "vendor/mermaid.min.js",
            "vendor/marked.min.js",
            "vendor/fonts/KaTeX_AMS-Regular.woff2",
            "vendor/fonts/KaTeX_Caligraphic-Bold.woff2",
            "vendor/fonts/KaTeX_Caligraphic-Regular.woff2",
            "vendor/fonts/KaTeX_Fraktur-Bold.woff2",
            "vendor/fonts/KaTeX_Fraktur-Regular.woff2",
            "vendor/fonts/KaTeX_Main-Bold.woff2",
            "vendor/fonts/KaTeX_Main-BoldItalic.woff2",
            "vendor/fonts/KaTeX_Main-Italic.woff2",
            "vendor/fonts/KaTeX_Main-Regular.woff2",
            "vendor/fonts/KaTeX_Math-BoldItalic.woff2",
            "vendor/fonts/KaTeX_Math-Italic.woff2",
            "vendor/fonts/KaTeX_SansSerif-Bold.woff2",
            "vendor/fonts/KaTeX_SansSerif-Italic.woff2",
            "vendor/fonts/KaTeX_SansSerif-Regular.woff2",
            "vendor/fonts/KaTeX_Script-Regular.woff2",
            "vendor/fonts/KaTeX_Size1-Regular.woff2",
            "vendor/fonts/KaTeX_Size2-Regular.woff2",
            "vendor/fonts/KaTeX_Size3-Regular.woff2",
            "vendor/fonts/KaTeX_Size4-Regular.woff2",
            "vendor/fonts/KaTeX_Typewriter-Regular.woff2"
    };

    private static volatile File extractedIndexHtml = null;

    private PureDownPreviewAssets() {}

    public static synchronized File getIndexHtmlFile() {
        if (extractedIndexHtml != null && extractedIndexHtml.exists()) {
            return extractedIndexHtml;
        }

        File targetDir;
        try {
            String sysPath = PathManager.getPluginTempPath();
            targetDir = new File(sysPath, "puredown_preview_v2");
        } catch (Throwable t) {
            targetDir = new File(System.getProperty("java.io.tmpdir"), "puredown_preview_v2");
        }

        if (!targetDir.exists()) {
            targetDir.mkdirs();
        }

        for (String relativePath : ASSET_FILES) {
            File outFile = new File(targetDir, relativePath);
            File parent = outFile.getParentFile();
            if (parent != null && !parent.exists()) {
                parent.mkdirs();
            }

            try (InputStream in = PureDownPreviewAssets.class.getResourceAsStream("/preview/" + relativePath)) {
                if (in != null) {
                    try (OutputStream out = new FileOutputStream(outFile)) {
                        in.transferTo(out);
                    }
                }
            } catch (Exception ignored) {}
        }

        File index = new File(targetDir, "index.html");
        if (index.exists()) {
            extractedIndexHtml = index;
            return index;
        }
        return null;
    }
}
