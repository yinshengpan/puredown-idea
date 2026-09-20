# PureDown Plugin for IntelliJ IDEA & Android Studio

**PureDown** 是专为 IntelliJ IDEA 与 Android Studio 打造的高能 Markdown 与知识库工作区插件。在深度对标官方 Markdown 插件基础体验的同时，吸纳了 **Markdown Pro** 的核心精髓功能。

---

## 🌟 核心特色与功能特性

### 1. 极速编写与编辑 (Write & Edit Faster)
- **浮动格式化工具栏 (Floating Toolbar)**：在编辑器选中文字时自动浮出，一键进行加粗、斜体、删除线、行内代码、引用、链接、任务清单等格式化。
- **内联表格编辑器 (Table Editor & Formatter)**：
  - 自动对齐：一键将杂乱的 Markdown 表格排版整齐，完美支持中文/全角字符宽度计算（CJK Monospace）。
  - 快捷插入：一键生成标准 Markdown 表格模板。
  - 快捷键支持：`Option+Command+T` (Mac) / `Alt+Ctrl+T` (Win/Linux)。
- **智能任务清单切换 (Smart Task Toggle)**：
  - 无论是编辑器源码还是预览界面，一键/快捷键切换 `- [ ]` $\leftrightarrow$ `- [x]`。
  - 快捷键支持：`Shift+Command+X` (Mac) / `Shift+Ctrl+X` (Win/Linux)。
- **有序列表智能重排 (Ordered List Renumbering)**：一键重新规整编号列表为连续数字（1. 2. 3. ...）。

### 2. 极致原地实时预览 (Accurate In-Place Preview)
- **平滑双向滚动同步 (Smooth Scroll Sync)**：基于行级锚点映射（`data-source-line`），编辑器滚动时右侧预览精确同步。
- **原地无闪烁更新 (In-Place DOM Diffing)**：输入打字时原地更新 DOM 内容，保持当前阅读与滚动位置，告别页面重载与白屏闪烁。
- **全方位排版支持**：
  - 代码高亮（支持 180+ 种语言语法高亮）
  - LaTeX / 数学公式支持（`$E=mc^2$` 与 `$$\int...$$`）
  - Mermaid 图表原生渲染（流程图、时序图、状态图、类图等）
  - 深色/浅色自适应（Darcula、New UI Dark 与 Light 自动同步）
- **一键富文本导出与复制**：
  - **Copy Rendered HTML**：一键将渲染好的排版 HTML 复制到剪贴板，方便直接粘贴至邮件、Slack、语雀、Word、Teams 等。
  - **Export as HTML**：一键导出带独立精美样式的离线 HTML 单文件。

### 3. Obsidian 级知识库与双链支持 (Obsidian Vault Integration)
- **`[[WikiLinks]]` 双向链接**：
  - 输入 `[[` 自动触发全项目笔记自动补全提示。
  - 支持 `[[Note Name]]`、`[[Note Name|自定义别名]]`。
  - 点击双链直接在 IDE 中定位并打开对应笔记。
- **Obsidian 警告框与提示框 (Callouts)**：
  - 原生渲染 `> [!NOTE]`、`> [!TIP]`、`> [!WARNING]`、`> [!DANGER]`、`> [!CAUTION]`、`> [!IMPORTANT]`、`> [!SUCCESS]`、`> [!QUESTION]`、`> [!BUG]`、`> [!QUOTE]`。
  - 带有醒目的颜色、状态图标与卡片式气泡样式。
- **知识库关系图谱 (Knowledge Vault Graph)**：
  - 可视化笔记拓扑图谱，实时扫描全项目 Markdown 文档及引用关系。
  - 力导向物理引擎（Spring-Physics Simulation）自动展开排布。
  - 支持节点搜索过滤、鼠标缩放平移、双击节点直接打开对应笔记文件！

### 4. 文档健康与 PSI 质量检查 (Inspections & Quick Fixes)
- **标题层级越级检查 (Heading Hierarchy)**：发现跳级标题（如从 H1 直接跳到 H3）时报警，并提供一键修复为连续层级的 QuickFix。
- **表格列未对齐检查 (Unaligned Tables)**：自动高亮排版不齐的表格，提供一键对齐修整 QuickFix。
- **空链接检查 (Empty Links)**：扫描发现 `[text]()` 或 `[](url)` 等无效链接并提示修复。

---

## 📦 安装与导入方式

打包好的插件文件位于：
- **桌面根目录**：`/Users/jasonyin/Desktop/puredown-1.0.0.zip`
- **项目构建目录**：`/Users/jasonyin/Desktop/puredown/build/distributions/puredown-1.0.0.zip`

### 在 IntelliJ IDEA 或 Android Studio 中安装：
1. 打开 **IntelliJ IDEA** 或 **Android Studio**。
2. 打开菜单栏：
   - Mac: **IntelliJ IDEA (或 Android Studio) -> Settings (Preferences)**（快捷键 `Command + ,`）
   - Windows/Linux: **File -> Settings**（快捷键 `Ctrl + Alt + S`）
3. 选择左侧 **Plugins**。
4. 点击顶部齿轮 ⚙️ 图标，选择 **Install Plugin from Disk...**（从磁盘安装插件...）。
5. 选中桌面上的 **`puredown-1.0.0.zip`**，点击 **OK**。
6. 点击 **Apply** 并重启 IDE 即可立即使用！
