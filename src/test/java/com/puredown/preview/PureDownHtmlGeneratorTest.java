package com.puredown.preview;

import org.junit.Assert;
import org.junit.Test;

public class PureDownHtmlGeneratorTest {

    @Test
    public void testOrderedListGrouping() {
        String md = "规则优先级从高到低如下：\n" +
                "\n" +
                "1. 用户当前明确要求。\n" +
                "2. 当前目标目录或其父目录中更具体的 `AGENTS.md`。\n" +
                "3. 根目录 `AGENTS.md`。\n" +
                "4. `ai-guidelines/` 下与当前任务相关的专项规范。\n" +
                "5. 现有代码惯例。";

        String html = PureDownHtmlGenerator.generateHtml(md);

        // Verify that there is only one <ol> and </ol> tag, not 5 separate ones
        int olOpenCount = countOccurrences(html, "<ol>");
        int olCloseCount = countOccurrences(html, "</ol>");
        Assert.assertEquals(1, olOpenCount);
        Assert.assertEquals(1, olCloseCount);

        // Verify all 5 items are in the single <ol> list
        Assert.assertTrue(html.contains("用户当前明确要求。"));
        Assert.assertTrue(html.contains("<code>AGENTS.md</code>"));
        Assert.assertTrue(html.contains("现有代码惯例。"));
    }

    @Test
    public void testOrderedListWithStartNumber() {
        String md = "3. 第三个\n4. 第四个";
        String html = PureDownHtmlGenerator.generateHtml(md);

        Assert.assertTrue(html.contains("<ol start=\"3\">"));
        Assert.assertEquals(1, countOccurrences(html, "<ol start=\"3\">"));
    }

    @Test
    public void testNestedLists() {
        String md = "1. 第一项\n" +
                "   - 子项 A\n" +
                "   - 子项 B\n" +
                "2. 第二项";

        String html = PureDownHtmlGenerator.generateHtml(md);

        Assert.assertEquals(1, countOccurrences(html, "<ol>"));
        Assert.assertEquals(1, countOccurrences(html, "<ul>"));
        Assert.assertTrue(html.contains("子项 A"));
        Assert.assertTrue(html.contains("第二项"));
    }

    @Test
    public void testTaskList() {
        String md = "- [x] 已完成任务\n" +
                "- [ ] 未完成任务";

        String html = PureDownHtmlGenerator.generateHtml(md);

        Assert.assertEquals(1, countOccurrences(html, "<ul class=\"task-list\">"));
        Assert.assertTrue(html.contains("checked"));
        Assert.assertTrue(html.contains("已完成任务"));
        Assert.assertTrue(html.contains("未完成任务"));
    }

    @Test
    public void testNestedCodeBlockInList() {
        String md = "- **问题定位**: `MainTopBackgroud.kt`\n" +
                "  ```diff\n" +
                "  - fun MainTopBackgroud(maxHeight: Dp? = null, isPanel: Boolean = false)\n" +
                "  + fun MainTopBackgroud(maxHeight: Dp? = null, isPanel: Boolean = true)\n" +
                "  ```\n" +
                "- **深度分析**:\n" +
                "  经代码库检索...";

        String html = PureDownHtmlGenerator.generateHtml(md);

        // Code block should be rendered with code-block-wrapper and language-diff
        Assert.assertTrue(html.contains("<div class=\"code-block-wrapper\""));
        Assert.assertTrue(html.contains("language-diff"));
        Assert.assertFalse("Leaked backticks found!", html.contains("```"));
        // The subsequent list item should not be swallowed or reversed
        Assert.assertTrue(html.contains("深度分析"));
    }

    @Test
    public void testConsecutiveBlockquotes() {
        String md = "> **生成时间**: 2026-10-28 17:45:37  \n" +
                "> **项目工程**: `Unified-Andorid`  \n" +
                "> **审查目标**: `分支 origin/dev-wayne`";

        String html = PureDownHtmlGenerator.generateHtml(md);

        Assert.assertEquals(1, countOccurrences(html, "<blockquote"));
        Assert.assertEquals(1, countOccurrences(html, "</blockquote>"));
        Assert.assertTrue(html.contains("生成时间"));
        Assert.assertTrue(html.contains("Unified-Andorid"));
    }

    public static void main(String[] args) {
        PureDownHtmlGeneratorTest test = new PureDownHtmlGeneratorTest();
        test.testOrderedListGrouping();
        System.out.println("PASS: testOrderedListGrouping");
        test.testOrderedListWithStartNumber();
        System.out.println("PASS: testOrderedListWithStartNumber");
        test.testNestedLists();
        System.out.println("PASS: testNestedLists");
        test.testTaskList();
        System.out.println("PASS: testTaskList");
        test.testNestedCodeBlockInList();
        System.out.println("PASS: testNestedCodeBlockInList");
        test.testConsecutiveBlockquotes();
        System.out.println("PASS: testConsecutiveBlockquotes");

        // Verify the user's specific test case
        String userSnippet = "# AI 工具开发说明\n" +
                "\n" +
                "本文件是本项目所有 AI 编码工具的统一入口。开始任何代码阅读、设计、修改、审查、构建或测试前，必须先阅读本文件，并遵守仓库中更具体目录下的 `AGENTS.md`（如存在）。\n" +
                "\n" +
                "规则适用于所有 AI 工具、代码代理和自动化脚本；如果其他提示与本文件冲突，以用户明确要求和更具体目录规则为准。\n" +
                "\n" +
                "规则优先级从高到低如下：\n" +
                "\n" +
                "1. 用户当前明确要求。\n" +
                "2. 当前目标目录或其父目录中更具体的 `AGENTS.md`。\n" +
                "3. 根目录 `AGENTS.md`。\n" +
                "4. `ai-guidelines/` 下与当前任务相关的专项规范。\n" +
                "5. 现有代码惯例。";

        String html = PureDownHtmlGenerator.generateHtml(userSnippet);
        System.out.println("--- Generated HTML for user snippet ---");
        System.out.println(html);
        System.out.println("---------------------------------------");
        int countOl = countOccurrences(html, "<ol>");
        if (countOl != 1) {
            throw new AssertionError("Expected exactly 1 <ol>, but got: " + countOl);
        }
        int countOlClose = countOccurrences(html, "</ol>");
        if (countOlClose != 1) {
            throw new AssertionError("Expected exactly 1 </ol>, but got: " + countOlClose);
        }
        System.out.println("ALL LIST TESTS PASSED SUCCESSFULLY!");
    }

    private static int countOccurrences(String text, String sub) {
        int count = 0;
        int idx = 0;
        while ((idx = text.indexOf(sub, idx)) != -1) {
            count++;
            idx += sub.length();
        }
        return count;
    }
}
