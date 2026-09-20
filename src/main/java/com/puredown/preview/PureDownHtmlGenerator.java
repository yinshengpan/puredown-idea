package com.puredown.preview;

import com.puredown.table.PureDownTableParser;
import java.util.ArrayList;
import java.util.List;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

public final class PureDownHtmlGenerator {
    private static final Pattern WIKILINK_PATTERN = Pattern.compile("\\[\\[([^|\\]]+)(?:\\|([^\\]]+))?\\]\\]");
    private static final Pattern TAG_PATTERN = Pattern.compile("(?<=\\s|^)#([a-zA-Z0-9_-]+)(?=\\s|$)");
    private static final Pattern IMAGE_PATTERN = Pattern.compile("!\\[([^\\]]*)\\]\\(([^\\)]+)\\)");
    private static final Pattern LINK_PATTERN = Pattern.compile("\\[([^\\]]+)\\]\\(([^\\)]+)\\)");
    private static final Pattern BOLD_PATTERN = Pattern.compile("(\\*\\*|__)(.*?)\\1");
    private static final Pattern ITALIC_PATTERN = Pattern.compile("(?<!\\*)(\\*|_)(.*?)\\1(?!\\*)");
    private static final Pattern STRIKE_PATTERN = Pattern.compile("~~(.*?)~~");
    private static final Pattern CODE_PATTERN = Pattern.compile("`([^`]+)`");
    private static final Pattern MATH_INLINE_PATTERN = Pattern.compile("\\$([^\\$]+)\\$");
    private static final Pattern CALLOUT_START_PATTERN = Pattern.compile("^>\\s*\\[!([a-zA-Z_-]+)\\](?:\\s+(.*))?$");
    private static final Pattern TOC_PATTERN = Pattern.compile("^\\[\\[?TOC\\]?\\]$", Pattern.CASE_INSENSITIVE);

    private PureDownHtmlGenerator() {}

    public static class TocEntry {
        public final int level;
        public final String title;
        public final String slug;

        public TocEntry(int level, String title, String slug) {
            this.level = level;
            this.title = title;
            this.slug = slug;
        }
    }

    public static String generateHtml(String markdown) {
        if (markdown == null) return "";
        String[] rawLines = markdown.split("\n", -1);
        StringBuilder out = new StringBuilder();
        List<TocEntry> tocList = new ArrayList<>();

        int i = 0;

        // 0. YAML Front Matter Parser (if starts at line 0 with ---)
        if (rawLines.length > 0 && rawLines[0].trim().equals("---")) {
            int endYaml = -1;
            for (int k = 1; k < rawLines.length; k++) {
                if (rawLines[k].trim().equals("---") || rawLines[k].trim().equals("...")) {
                    endYaml = k;
                    break;
                }
            }
            if (endYaml != -1) {
                renderFrontMatter(rawLines, 1, endYaml, out);
                i = endYaml + 1;
            }
        }

        while (i < rawLines.length) {
            String line = rawLines[i];
            String trimmed = line.trim();

            // 1. [TOC] marker
            if (TOC_PATTERN.matcher(trimmed).matches()) {
                out.append("<!--IN_DOC_TOC_PLACEHOLDER-->\n");
                i++;
                continue;
            }

            // 2. Code fence
            if (trimmed.startsWith("```")) {
                String lang = trimmed.substring(3).trim().toLowerCase();
                List<String> codeLines = new ArrayList<>();
                int startLine = i;
                i++;
                while (i < rawLines.length && !rawLines[i].trim().startsWith("```")) {
                    codeLines.add(rawLines[i]);
                    i++;
                }
                if (i < rawLines.length) i++; // skip closing fence

                String codeContent = escapeHtml(String.join("\n", codeLines));
                if ("mermaid".equals(lang)) {
                    out.append("<div class=\"mermaid\" data-source-line=\"").append(startLine).append("\">")
                            .append(codeContent)
                            .append("</div>\n");
                } else {
                    String displayLang = lang.isEmpty() ? "text" : lang;
                    String copyTitle = com.puredown.i18n.PureDownI18n.message("复制代码", "Copy code");
                    String copyBtnText = com.puredown.i18n.PureDownI18n.message("📋 复制", "📋 Copy");
                    out.append("<div class=\"code-block-wrapper\" data-source-line=\"").append(startLine).append("\">\n")
                            .append("  <div class=\"code-header\">\n")
                            .append("    <span class=\"code-lang\">").append(escapeHtml(displayLang)).append("</span>\n")
                            .append("    <button class=\"copy-button\" onclick=\"copyCode(this)\" title=\"").append(copyTitle).append("\">").append(copyBtnText).append("</button>\n")
                            .append("  </div>\n")
                            .append("  <pre><code class=\"language-").append(displayLang).append("\">")
                            .append(codeContent)
                            .append("</code></pre>\n")
                            .append("</div>\n");
                }
                continue;
            }

            // 3. Math block $$
            if (trimmed.equals("$$") || (trimmed.startsWith("$$") && trimmed.endsWith("$$") && trimmed.length() > 2)) {
                if (trimmed.startsWith("$$") && trimmed.endsWith("$$") && trimmed.length() > 2) {
                    String math = trimmed.substring(2, trimmed.length() - 2).trim();
                    out.append("<div class=\"katex-display\" data-source-line=\"").append(i).append("\">$$")
                            .append(math).append("$$</div>\n");
                    i++;
                    continue;
                }
                List<String> mathLines = new ArrayList<>();
                int startLine = i;
                i++;
                while (i < rawLines.length && !rawLines[i].trim().equals("$$")) {
                    mathLines.add(rawLines[i]);
                    i++;
                }
                if (i < rawLines.length) i++;
                out.append("<div class=\"katex-display\" data-source-line=\"").append(startLine).append("\">$$\n")
                        .append(escapeHtml(String.join("\n", mathLines)))
                        .append("\n$$</div>\n");
                continue;
            }

            // 4. Obsidian Callouts
            Matcher calloutMatch = CALLOUT_START_PATTERN.matcher(trimmed);
            if (calloutMatch.find()) {
                String calloutType = calloutMatch.group(1).toLowerCase();
                String rawTitle = calloutMatch.group(2);
                String calloutTitle = (rawTitle != null && !rawTitle.trim().isEmpty())
                        ? rawTitle.trim()
                        : Character.toUpperCase(calloutType.charAt(0)) + calloutType.substring(1);
                int startLine = i;
                List<String> calloutBody = new ArrayList<>();
                i++;

                while (i < rawLines.length && (rawLines[i].trim().startsWith(">") || rawLines[i].trim().isEmpty())) {
                    String raw = rawLines[i].trim();
                    if (raw.startsWith(">")) {
                        calloutBody.add(raw.substring(1).trim());
                    } else if (raw.isEmpty()) {
                        calloutBody.add("");
                    }
                    i++;
                }

                String iconSvg = getCalloutIconSvg(calloutType);
                out.append("<div class=\"callout callout-").append(calloutType).append("\" data-source-line=\"").append(startLine).append("\">\n")
                        .append("  <div class=\"callout-title\">\n")
                        .append("    <span class=\"callout-icon\">").append(iconSvg).append("</span>\n")
                        .append("    <span>").append(escapeHtml(calloutTitle)).append("</span>\n")
                        .append("  </div>\n")
                        .append("  <div class=\"callout-content\">\n");

                for (String bodyLine : calloutBody) {
                    if (!bodyLine.trim().isEmpty()) {
                        out.append("    <p>").append(formatInline(bodyLine)).append("</p>\n");
                    }
                }
                out.append("  </div>\n</div>\n");
                continue;
            }

            // 5. Headings (# H1 to ###### H6)
            if (trimmed.startsWith("#")) {
                int level = 0;
                while (level < trimmed.length() && trimmed.charAt(level) == '#') {
                    level++;
                }
                if (level >= 1 && level <= 6 && trimmed.length() > level && trimmed.charAt(level) == ' ') {
                    String headingText = trimmed.substring(level + 1).trim();
                    String slug = headingText.toLowerCase().replaceAll("[^a-z0-9_-]+", "-").replaceAll("^-+|-+$", "");
                    if (slug.isEmpty()) slug = "heading-" + i;
                    tocList.add(new TocEntry(level, headingText, slug));

                    out.append("<h").append(level).append(" id=\"").append(slug).append("\" data-source-line=\"").append(i).append("\">")
                            .append(formatInline(headingText))
                            .append("</h").append(level).append(">\n");
                    i++;
                    continue;
                }
            }

            // 6. Tables
            if (PureDownTableParser.isTableLine(trimmed)) {
                List<LineEntry> tableLines = new ArrayList<>();
                while (i < rawLines.length && PureDownTableParser.isTableLine(rawLines[i].trim())) {
                    tableLines.add(new LineEntry(i, rawLines[i].trim()));
                    i++;
                }

                if (tableLines.size() >= 2) {
                    renderTable(tableLines, out);
                    continue;
                } else {
                    LineEntry single = tableLines.get(0);
                    out.append("<p data-source-line=\"").append(single.lineIndex).append("\">")
                            .append(formatInline(single.text)).append("</p>\n");
                    continue;
                }
            }

            // 7. Horizontal Rule
            if (trimmed.matches("^(-{3,}|\\*{3,}|_{3,})$")) {
                out.append("<hr data-source-line=\"").append(i).append("\" />\n");
                i++;
                continue;
            }

            // 8. Lists
            if (trimmed.startsWith("- [ ] ") || trimmed.startsWith("- [x] ") || trimmed.startsWith("- [X] ")) {
                boolean checked = trimmed.startsWith("- [x] ") || trimmed.startsWith("- [X] ");
                String content = trimmed.substring(6);
                String checkedAttr = checked ? "checked" : "";
                out.append("<ul class=\"task-list\"><li class=\"task-list-item\" data-source-line=\"").append(i).append("\">")
                        .append("<input type=\"checkbox\" ").append(checkedAttr).append(" data-source-line=\"").append(i).append("\" />")
                        .append(" ").append(formatInline(content))
                        .append("</li></ul>\n");
                i++;
                continue;
            }

            if (trimmed.startsWith("- ") || trimmed.startsWith("* ") || trimmed.startsWith("+ ")) {
                String content = trimmed.substring(2);
                out.append("<ul><li data-source-line=\"").append(i).append("\">")
                        .append(formatInline(content))
                        .append("</li></ul>\n");
                i++;
                continue;
            }

            Pattern numPattern = Pattern.compile("^(\\d+)\\.\\s+(.*)$");
            Matcher numMatcher = numPattern.matcher(trimmed);
            if (numMatcher.find()) {
                String content = numMatcher.group(2);
                out.append("<ol><li data-source-line=\"").append(i).append("\">")
                        .append(formatInline(content))
                        .append("</li></ol>\n");
                i++;
                continue;
            }

            // 9. Blockquote
            if (trimmed.startsWith(">")) {
                String content = trimmed.substring(1).trim();
                out.append("<blockquote data-source-line=\"").append(i).append("\"><p>")
                        .append(formatInline(content))
                        .append("</p></blockquote>\n");
                i++;
                continue;
            }

            // 10. Blank line
            if (trimmed.isEmpty()) {
                i++;
                continue;
            }

            // 11. Paragraph
            out.append("<p data-source-line=\"").append(i).append("\">")
                    .append(formatInline(trimmed))
                    .append("</p>\n");
            i++;
        }

        String result = out.toString();

        // If in-doc TOC placeholder exists, generate HTML TOC tree
        if (result.contains("<!--IN_DOC_TOC_PLACEHOLDER-->")) {
            String tocTitle = com.puredown.i18n.PureDownI18n.message("📑 目录大纲", "📑 Table of Contents");
            StringBuilder tocBuilder = new StringBuilder();
            tocBuilder.append("<div class=\"in-doc-toc\">\n")
                    .append("  <div class=\"in-doc-toc-title\">").append(tocTitle).append("</div>\n")
                    .append("  <ul>\n");
            for (TocEntry entry : tocList) {
                int indent = Math.max(0, (entry.level - 1) * 16);
                tocBuilder.append("    <li style=\"margin-left: ").append(indent).append("px;\">")
                        .append("<a href=\"#").append(entry.slug).append("\">")
                        .append(escapeHtml(entry.title))
                        .append("</a></li>\n");
            }
            tocBuilder.append("  </ul>\n</div>\n");
            result = result.replace("<!--IN_DOC_TOC_PLACEHOLDER-->", tocBuilder.toString());
        }

        return result;
    }

    private static void renderFrontMatter(String[] rawLines, int start, int end, StringBuilder out) {
        String title = null;
        String date = null;
        String author = null;
        List<String> tags = new ArrayList<>();
        List<String> otherProps = new ArrayList<>();

        for (int k = start; k < end; k++) {
            String l = rawLines[k].trim();
            if (l.isEmpty()) continue;
            int colonIdx = l.indexOf(':');
            if (colonIdx > 0) {
                String key = l.substring(0, colonIdx).trim();
                String val = l.substring(colonIdx + 1).trim();
                if ((val.startsWith("\"") && val.endsWith("\"")) || (val.startsWith("'") && val.endsWith("'"))) {
                    val = val.substring(1, val.length() - 1);
                }

                if ("title".equalsIgnoreCase(key)) {
                    title = val;
                } else if ("date".equalsIgnoreCase(key)) {
                    date = val;
                } else if ("author".equalsIgnoreCase(key)) {
                    author = val;
                } else if ("tags".equalsIgnoreCase(key) || "tag".equalsIgnoreCase(key)) {
                    if (val.startsWith("[") && val.endsWith("]")) {
                        String[] arr = val.substring(1, val.length() - 1).split(",");
                        for (String t : arr) {
                            String trimmedTag = t.trim().replaceAll("^['\"]|['\"]$", "");
                            if (!trimmedTag.isEmpty()) tags.add(trimmedTag);
                        }
                    } else if (!val.isEmpty()) {
                        tags.add(val);
                    }
                } else {
                    otherProps.add(key + ": " + val);
                }
            }
        }

        out.append("<div class=\"front-matter-card\">\n");
        if (title != null && !title.isEmpty()) {
            out.append("  <div class=\"front-matter-title\">").append(escapeHtml(title)).append("</div>\n");
        }
        out.append("  <div class=\"front-matter-meta\">\n");
        if (date != null && !date.isEmpty()) {
            out.append("    <span>📅 ").append(escapeHtml(date)).append("</span>\n");
        }
        if (author != null && !author.isEmpty()) {
            out.append("    <span>✍️ ").append(escapeHtml(author)).append("</span>\n");
        }
        if (!tags.isEmpty()) {
            out.append("    <span>🏷️ ");
            for (String tag : tags) {
                out.append("<span class=\"front-matter-tag\">").append(escapeHtml(tag)).append("</span> ");
            }
            out.append("</span>\n");
        }
        for (String prop : otherProps) {
            out.append("    <span>🔹 ").append(escapeHtml(prop)).append("</span>\n");
        }
        out.append("  </div>\n</div>\n");
    }

    private static class LineEntry {
        final int lineIndex;
        final String text;
        LineEntry(int lineIndex, String text) {
            this.lineIndex = lineIndex;
            this.text = text;
        }
    }

    private static void renderTable(List<LineEntry> tableLines, StringBuilder out) {
        int startLine = tableLines.get(0).lineIndex;
        List<String> headerCells = PureDownTableParser.splitPipes(tableLines.get(0).text);
        int sepIdx = -1;
        List<String> alignments = new ArrayList<>();

        if (tableLines.size() > 1 && PureDownTableParser.isSeparatorLine(tableLines.get(1).text)) {
            sepIdx = 1;
            List<String> sepCells = PureDownTableParser.splitPipes(tableLines.get(1).text);
            for (String c : sepCells) {
                String t = c.trim();
                if (t.startsWith(":") && t.endsWith(":")) {
                    alignments.add("center");
                } else if (t.endsWith(":")) {
                    alignments.add("right");
                } else if (t.startsWith(":")) {
                    alignments.add("left");
                } else {
                    alignments.add("left");
                }
            }
        }

        while (alignments.size() < headerCells.size()) {
            alignments.add("left");
        }

        out.append("<table data-source-line=\"").append(startLine).append("\">\n");
        out.append("  <thead>\n    <tr>\n");
        for (int idx = 0; idx < headerCells.size(); idx++) {
            String align = idx < alignments.size() ? alignments.get(idx) : "left";
            out.append("      <th style=\"text-align: ").append(align).append(";\">")
                    .append(formatInline(headerCells.get(idx)))
                    .append("</th>\n");
        }
        out.append("    </tr>\n  </thead>\n");

        int dataStart = (sepIdx != -1) ? 2 : 1;
        if (dataStart < tableLines.size()) {
            out.append("  <tbody>\n");
            for (int r = dataStart; r < tableLines.size(); r++) {
                LineEntry entry = tableLines.get(r);
                List<String> cells = PureDownTableParser.splitPipes(entry.text);
                out.append("    <tr data-source-line=\"").append(entry.lineIndex).append("\">\n");
                for (int idx = 0; idx < headerCells.size(); idx++) {
                    String align = idx < alignments.size() ? alignments.get(idx) : "left";
                    String content = idx < cells.size() ? cells.get(idx) : "";
                    out.append("      <td style=\"text-align: ").append(align).append(";\">")
                            .append(formatInline(content))
                            .append("</td>\n");
                }
                out.append("    </tr>\n");
            }
            out.append("  </tbody>\n");
        }
        out.append("</table>\n");
    }

    public static String formatInline(String text) {
        String s = escapeHtml(text);

        // Math inline: $formula$
        Matcher mathMatcher = MATH_INLINE_PATTERN.matcher(s);
        StringBuilder mathSb = new StringBuilder();
        while (mathMatcher.find()) {
            String formula = mathMatcher.group(1);
            mathMatcher.appendReplacement(mathSb, "<span class=\"katex-inline\">\\$" + Matcher.quoteReplacement(formula) + "\\$</span>");
        }
        mathMatcher.appendTail(mathSb);
        s = mathSb.toString();

        // Images: ![alt](url) -> Clickable Lightbox images!
        Matcher imgMatcher = IMAGE_PATTERN.matcher(s);
        StringBuilder imgSb = new StringBuilder();
        while (imgMatcher.find()) {
            String alt = imgMatcher.group(1);
            String url = imgMatcher.group(2);
            String zoomTitle = com.puredown.i18n.PureDownI18n.message("点击放大预览", "Click to zoom preview");
            imgMatcher.appendReplacement(imgSb, "<img class=\"zoomable-img\" src=\"" + Matcher.quoteReplacement(url) + "\" alt=\"" + Matcher.quoteReplacement(alt) + "\" onclick=\"openLightbox(this)\" title=\"" + zoomTitle + "\" />");
        }
        imgMatcher.appendTail(imgSb);
        s = imgSb.toString();

        // WikiLinks: [[Target|Alias]] or [[Target]]
        Matcher wikiMatcher = WIKILINK_PATTERN.matcher(s);
        StringBuilder wikiSb = new StringBuilder();
        while (wikiMatcher.find()) {
            String target = wikiMatcher.group(1);
            String rawAlias = wikiMatcher.group(2);
            String alias = (rawAlias != null && !rawAlias.isEmpty()) ? rawAlias : target;
            wikiMatcher.appendReplacement(wikiSb, "<a class=\"wikilink\" href=\"#\" data-target-note=\"" +
                    Matcher.quoteReplacement(target) + "\">" + Matcher.quoteReplacement(alias) + "</a>");
        }
        wikiMatcher.appendTail(wikiSb);
        s = wikiSb.toString();

        // Standard Links: [text](url)
        Matcher linkMatcher = LINK_PATTERN.matcher(s);
        StringBuilder linkSb = new StringBuilder();
        while (linkMatcher.find()) {
            String linkText = linkMatcher.group(1);
            String url = linkMatcher.group(2);
            linkMatcher.appendReplacement(linkSb, "<a href=\"" + Matcher.quoteReplacement(url) + "\" target=\"_blank\">" + Matcher.quoteReplacement(linkText) + "</a>");
        }
        linkMatcher.appendTail(linkSb);
        s = linkSb.toString();

        // Tags: #tag
        Matcher tagMatcher = TAG_PATTERN.matcher(s);
        StringBuilder tagSb = new StringBuilder();
        while (tagMatcher.find()) {
            String tag = tagMatcher.group(1);
            tagMatcher.appendReplacement(tagSb, "<span class=\"markdown-tag\">#" + Matcher.quoteReplacement(tag) + "</span>");
        }
        tagMatcher.appendTail(tagSb);
        s = tagSb.toString();

        // Code: `code`
        Matcher codeMatcher = CODE_PATTERN.matcher(s);
        StringBuilder codeSb = new StringBuilder();
        while (codeMatcher.find()) {
            String code = codeMatcher.group(1);
            codeMatcher.appendReplacement(codeSb, "<code>" + Matcher.quoteReplacement(code) + "</code>");
        }
        codeMatcher.appendTail(codeSb);
        s = codeSb.toString();

        // Bold: **text**
        Matcher boldMatcher = BOLD_PATTERN.matcher(s);
        StringBuilder boldSb = new StringBuilder();
        while (boldMatcher.find()) {
            String bText = boldMatcher.group(2);
            boldMatcher.appendReplacement(boldSb, "<strong>" + Matcher.quoteReplacement(bText) + "</strong>");
        }
        boldMatcher.appendTail(boldSb);
        s = boldSb.toString();

        // Italic: *text*
        Matcher italicMatcher = ITALIC_PATTERN.matcher(s);
        StringBuilder italicSb = new StringBuilder();
        while (italicMatcher.find()) {
            String iText = italicMatcher.group(2);
            italicMatcher.appendReplacement(italicSb, "<em>" + Matcher.quoteReplacement(iText) + "</em>");
        }
        italicMatcher.appendTail(italicSb);
        s = italicSb.toString();

        // Strike: ~~text~~
        Matcher strikeMatcher = STRIKE_PATTERN.matcher(s);
        StringBuilder strikeSb = new StringBuilder();
        while (strikeMatcher.find()) {
            String stText = strikeMatcher.group(1);
            strikeMatcher.appendReplacement(strikeSb, "<del>" + Matcher.quoteReplacement(stText) + "</del>");
        }
        strikeMatcher.appendTail(strikeSb);
        s = strikeSb.toString();

        return s;
    }

    public static String escapeHtml(String text) {
        if (text == null) return "";
        return text.replace("&", "&amp;")
                .replace("<", "&lt;")
                .replace(">", "&gt;")
                .replace("\"", "&quot;");
    }

    private static String getCalloutIconSvg(String type) {
        switch (type) {
            case "tip":
            case "success":
                return "<svg viewBox=\"0 0 16 16\" fill=\"#2e7d32\" width=\"16\" height=\"16\"><path d=\"M8 1a7 7 0 1 0 7 7A7 7 0 0 0 8 1zm3.3 5.3l-4 4a1 1 0 0 1-1.4 0l-2-2a1 1 0 1 1 1.4-1.4L6.6 8.2l3.3-3.3a1 1 0 0 1 1.4 1.4z\"/></svg>";
            case "warning":
            case "caution":
                return "<svg viewBox=\"0 0 16 16\" fill=\"#ed6c02\" width=\"16\" height=\"16\"><path d=\"M8.9 2.4a1 1 0 0 0-1.8 0l-6 10.4A1 1 0 0 0 2 14.3h12a1 1 0 0 0 .9-1.5zM7.2 6h1.6v4H7.2zm0 5h1.6v1.6H7.2z\"/></svg>";
            case "danger":
            case "failure":
            case "bug":
                return "<svg viewBox=\"0 0 16 16\" fill=\"#d32f2f\" width=\"16\" height=\"16\"><path d=\"M8 1a7 7 0 1 0 7 7A7 7 0 0 0 8 1zm3.5 9.1a1 1 0 1 1-1.4 1.4L8 9.4l-2.1 2.1a1 1 0 0 1-1.4-1.4L6.6 8 4.5 5.9a1 1 0 1 1 1.4-1.4L8 6.6l2.1-2.1a1 1 0 0 1 1.4 1.4L9.4 8z\"/></svg>";
            case "important":
                return "<svg viewBox=\"0 0 16 16\" fill=\"#9c27b0\" width=\"16\" height=\"16\"><path d=\"M8 1a7 7 0 1 0 7 7A7 7 0 0 0 8 1zm.8 10H7.2V9.5h1.6zm0-3H7.2V4h1.6z\"/></svg>";
            case "question":
                return "<svg viewBox=\"0 0 16 16\" fill=\"#00897b\" width=\"16\" height=\"16\"><path d=\"M8 1a7 7 0 1 0 7 7A7 7 0 0 0 8 1zm.8 11.5H7.2V10h1.6zm1.7-5.2c-.3.4-.8.8-1.2 1.1-.3.3-.5.7-.5 1.1H7.2c0-.8.3-1.4.9-1.8l.9-.8c.4-.3.6-.7.6-1.1a1.4 1.4 0 1 0-2.8 0H5.2a3 3 0 1 1 6 0c0 .7-.3 1.2-.7 1.5z\"/></svg>";
            default:
                return "<svg viewBox=\"0 0 16 16\" fill=\"#0288d1\" width=\"16\" height=\"16\"><path d=\"M8 1a7 7 0 1 0 7 7A7 7 0 0 0 8 1zm.8 10.5H7.2V6.5h1.6zm0-5.7H7.2V3.8h1.6z\"/></svg>";
        }
    }
}
