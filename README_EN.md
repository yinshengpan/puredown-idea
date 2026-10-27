# PureDown 🚀

<p align="center">
  <a href="README.md">简体中文</a> | <b>English</b>
</p>

> A modern, high-performance Markdown editing, real-time rich preview, and knowledge workspace plugin designed for **IntelliJ IDEA** and **Android Studio**.

PureDown integrates a blazing-fast multi-mode split editor, in-place formatting automation, extended academic and technical publishing syntax (KaTeX math, Mermaid diagrams, Obsidian callouts, YAML metadata cards, one-click code execution), and built-in Obsidian-grade bidirectional links with an interactive 2D Knowledge Graph. It provides developers with an all-in-one out-of-the-box Markdown writing and knowledge management experience.

---

## 🌟 Core Highlights & Features

### 1. Blazing Fast Multi-Mode Editor & In-Place Live Preview
- **Three Flexible View Modes**:
  - **Split Mode**: Source code on the left, zero-latency rendering on the right.
  - **Editor Only**: Distraction-free, pure code and text editing experience.
  - **Preview Only**: Full-width reading and technical document reviewing.
- **Customizable Default Mode**:
  - Configure your preferred startup layout mode in **`Settings | Tools | PureDown`** (Split / Editor Only / Preview Only).
- **Smooth Bidirectional Sync Scrolling**:
  - Precise line-level anchor tracking keeps the editor cursor and preview viewport smoothly aligned; togglable via the toolbar.
- **Zero-Flicker In-Place DOM Diffing**:
  - Incremental updates preserve your reading position without blank page flashes or scroll jumping.

---

### 2. Intelligent Editing Assistant & Formatting Toolbox
- **Selection Floating Toolbar**:
  - Automatically surfaces upon selecting text in the editor, providing instant formatting for Bold, Italic, Strikethrough, Inline Code, Links, Quotes, and Task Lists.
- **CJK Smart Table Formatter & Auto-Alignment**:
  - Detects full-width Chinese, Japanese, and Korean character widths to align messy Markdown tables neatly in-place.
  - Shortcuts: `Option + Command + T` (macOS) / `Alt + Ctrl + T` (Windows/Linux).
  - One-click insertion of 3-column table templates.
- **Interactive Task Checkbox Toggling**:
  - Toggle between `- [ ]` $\leftrightarrow$ `- [x]` directly by clicking checkboxes in the preview pane or with a keyboard shortcut.
  - Shortcuts: `Shift + Command + X` (macOS) / `Shift + Ctrl + X` (Windows/Linux).
- **Sequential List Auto-Renumbering**:
  - Automatically re-numbers ordered lists (1. 2. 3. ...) sequentially with a single click.

---

### 3. Extended Technical & Academic Publishing Syntax
- **YAML Front Matter Metadata Cards**:
  - Automatically parses YAML headers (e.g. `title`, `date`, `author`, `tags`, `category`) into clean, stylized metadata cards.
- **In-Document Dynamic Outline ([TOC])**:
  - Type `[TOC]` or `[[TOC]]` anywhere to dynamically generate an interactive hierarchical outline tree with smooth scroll navigation.
- **One-Click Quick Run for API / Shell Scripts**:
  - Provides a quick execution button (`▶️ 执行` / `▶️ Run`) on `curl` and `bash` code blocks, opening an interactive popup console for real-time output and instant re-runs.
- **100% Offline KaTeX Math Rendering**:
  - Bundled with complete local KaTeX rendering engines and webfonts. Supports both inline `$E=mc^2$` and display math `$$\int_0^\infty e^{-x^2}dx$$` without external network access.
- **Native Mermaid Architecture Diagrams**:
  - Renders Flowcharts, Sequence Diagrams, Class Diagrams, and State Diagrams natively with automatic dark/light IDE theme adaptation.
- **Rich Text Extensions & Bidirectional Footnotes**:
  - Supports `==highlighted text==`, subscripts (e.g. `H~2~O`), and superscripts (e.g. `n^2^`).
  - Full bidirectional footnote support (`[^1]`, `[^note]`) with backrefs between citations and footnotes.
- **Native Complex HTML Table Parsing**:
  - Seamlessly parses raw `<table>`, `<tr>`, `<th>`, and `<td>` tags, with support for nested Markdown pipe tables, code snippets, and lists inside table cells.
- **Syntax Highlighting, Language Badges & Code Copy**:
  - Displays language tags and an instant copy button on code snippets with feedback animations.
- **Immersive Image Lightbox**:
  - Click any image in preview to open a full-screen dimmed zoom view; dismiss with a click or `Esc`.
- **Obsidian-Style Callouts**:
  - Natively supports 10+ callout types (`> [!NOTE]`, `> [!TIP]`, `> [!WARNING]`, `> [!DANGER]`, `> [!IMPORTANT]`, `> [!CAUTION]`, `> [!SUCCESS]`, etc.).

---

### 4. Live Document Statistics Status Bar
- Native status bar at the bottom of the editor responding in real-time to caret movement and text changes:
  - **Caret Position**: Displays `Ln X, Col Y` (or `行 X, 列 Y`).
  - **Metrics**: Counts characters, words (CJK characters + Western words), and total lines.
  - **Estimated Reading Time**: Calculated using a dual CJK/English reading rate model (`⏱️ ~N min read`).
  - **Encoding**: Indicates `UTF-8`.

---

### 5. Obsidian-Grade WikiLinks & 2D Knowledge Vault Graph
- **`[[WikiLinks]]` Auto-Completion**:
  - Type `[[` to trigger auto-completion for all Markdown notes across your project.
  - Supports alias syntax: `[[TargetNote|Custom Alias]]`.
  - Click links in preview or `Cmd/Ctrl + Click` in editor to open the target note directly.
- **Interactive 2D Force-Directed Knowledge Graph**:
  - Real-time link relationship analysis across project notes.
  - Visualizes node networks with a physics spring layout.
  - Supports keyword search filtering, canvas panning, mouse wheel zooming, and double-clicking nodes to open notes.

---

### 6. Document Inspections & Code Quality
- **Skipped Heading Level Inspection**: Flags gaps in heading hierarchy (e.g. H1 directly to H3) with quick-fix downgrade.
- **Unaligned Table Inspection**: Automatically detects uneven column widths with quick-fix formatting.
- **Empty Link Inspection**: Warns against dead links with missing URLs or text.

---

### 7. Seamless Bilingual Localization (i18n)
- Language mode supports: **Auto-detect from IDE**, **Forced Simplified Chinese**, **Forced English**.
- Settings, status bars, toolbars, context menus, and inspection tooltips are fully localized.

---

### 8. Exporting & Publishing
- **Copy Rendered HTML**:
  - Copies formatted rich-text HTML directly to clipboard, ready to paste into WeChat, Zhihu, Notion, Lark, or email.
- **Export as Standalone HTML**:
  - Exports a self-contained HTML file bundled with all styles for offline distribution.

---

## ⌨️ Common Keyboard Shortcuts

| Action | macOS Shortcut | Windows / Linux Shortcut |
| :--- | :--- | :--- |
| **Format & Align Table** | `Option + Command + T` | `Alt + Ctrl + T` |
| **Toggle Task Checkbox** | `Shift + Command + X` | `Shift + Ctrl + X` |
| **Open Knowledge Graph** | Right-click Menu / Toolbar Graph Icon | Right-click Menu / Toolbar Graph Icon |
| **Bold / Italic** | `Command + B` / `Command + I` | `Ctrl + B` / `Ctrl + I` |
| **Selection Toolbar** | Automatically triggers on text selection | Automatically triggers on text selection |

---

## 📦 Installation Guide

Download the plugin archive directly from the desktop:
- **Archive Path**: `/Users/jasonyin/Desktop/puredown-1.0.3.zip`

### Installation Steps:
1. Launch **IntelliJ IDEA** or **Android Studio**.
2. Open Settings dialog:
   - **macOS**: Click menu `IntelliJ IDEA (or Android Studio) -> Settings...` (Shortcut: `⌘ ,`)
   - **Windows / Linux**: Click `File -> Settings...` (Shortcut: `Ctrl + Alt + S`)
3. In the left panel, select **`Plugins`**.
4. Click the ⚙️ **Gear icon** in the top-right toolbar and select **`Install Plugin from Disk...`**.
5. Select **`puredown-1.0.3.zip`** from your desktop and click **OK**.
6. Click **Apply** to complete installation, and open any `.md` / `.markdown` / `.mdx` file to start writing!
7. Navigate to **`Settings | Tools | PureDown`** to customize your layout mode, sync scrolling, and preview preferences.
