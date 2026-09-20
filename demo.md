# PureDown 功能演示文档

欢迎使用 **PureDown**！这是一个结合了官方 Markdown 核心基础与 **Markdown Pro** 精髓的高能 Markdown / 知识库插件。

---

## 1. 浮动工具栏与划词排版

在编辑器中用鼠标**划选任意文字**，上方将弹出快速格式化悬浮工具栏：
- **粗体** (`**Bold**`)
- *斜体* (`*Italic*`)
- ~~删除线~~ (`~~Strikethrough~~`)
- `行内代码` (`` `code` ``)
- [超链接](https://plugins.jetbrains.com)
- > 引用块

---

## 2. 智能表格与自动格式化对齐

测试一键格式化：将光标放在表格内，按快捷键 `Option+Command+T` (Mac) 或点击顶部工具栏的表格对齐按钮：

| 功能模块 | 官方 Markdown | Markdown Pro | PureDown |
| :--- | :---: | ---: | :---: |
| 分栏分屏预览 | 支持 | 支持 | 完整支持 |
| 浮动格式化工具栏 | 基础 | 丰富 | 强大内嵌 |
| CJK表格智能排版 | 弱 | 强 | 完美支持 |
| Obsidian Callouts | 不支持 | 支持 | 完整支持 |
| WikiLinks 双向链接 | 不支持 | 支持 | 自动补全+跳转 |
| 知识库关系拓扑图 | 不支持 | 支持 | 力导向交互图谱 |

---

## 3. Obsidian 风格 Callouts 警告框

> [!NOTE] 提示说明
> 这是一个常规的信息说明 Callout 容器。

> [!TIP] 实用技巧
> 使用 `Shift + Command + X` 可以一键在光标所在行切换任务清单选中状态！

> [!WARNING] 注意事项
> 请确保导出的 HTML 文件所依赖的内联资源完整。

> [!DANGER] 危险警告
> 严禁在生产环境中跳过权限校验。

> [!SUCCESS] 构建成功
> PureDown 插件已在桌面完成构建并打包生成 ZIP！

---

## 4. 任务清单 (Task Lists)

直接在右侧预览界面点击复选框，或者在源码中使用 `Shift + Command + X` 快速切换：

- [x] 完成项目结构搭建与 Gradle 构建配置
- [x] 实现分栏编辑器与三态视图切换 (Editor / Split / Preview)
- [x] 实现平滑双向滚动同步与原地 DOM 无闪更新
- [x] 实现 CJK 宽字符支持的 Markdown 表格格式化器
- [x] 实现 Obsidian `[[WikiLinks]]` 双链提示与跳转
- [x] 实现 Obsidian Callouts 彩色告警卡片
- [x] 实现力导向知识库拓扑图谱 (Vault Knowledge Graph)
- [x] 打包输出为可以直接导入的插件 ZIP 包

---

## 5. 关系图谱与双链 (WikiLinks)

支持 Obsidian 知识库链接与自动补全：
- 查看欢迎笔记：[[README]]
- 或者指定自定义别名：[[README|PureDown 插件使用文档]]

点击顶部工具栏的紫色节点图标，或者菜单中的 **Show Knowledge Vault Graph**，即可开启全项目笔记的 2D 关系图谱！
