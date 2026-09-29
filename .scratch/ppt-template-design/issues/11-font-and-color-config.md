# 确定字体与配色配置结构

Type: grilling
Status: resolved

## Question

字体选型与 colors.json 的配置结构要不要动？（优先级低于版式多样性，可后置）

**已定**：配色 = 数据（colors.json），代码注入（ticket 10）；4 套主题不变；母版/版式管结构不管颜色。

**待决**：
1. 字体：全局一套还是 per-theme？当前 `_FONT_CANDIDATES` 硬编码 3 套 macOS 中文字体（Hiragino Sans GB / STHeiti / PingFang），部署到 Linux（引擎目标环境）会缺字体——需要定一套跨平台中文字体策略。
2. colors.json 结构：现有 accent/text_dark/text_muted 三色是否够，还是要加 per-版式字号/字色？

## Answer

**字体：全局一套（不 per-theme）**。4 套主题只差颜色，字体无需跟着换。
- .pptx 侧：显式设 `font.name`（推荐「微软雅黑 / Microsoft YaHei」，或跨平台「Noto Sans CJK SC」）。
- PNG 侧：`_FONT_CANDIDATES` 扩成跨平台候选（macOS PingFang/Hiragino + Linux Noto Sans CJK SC / WenQuanYi + Windows msyh/simhei），按序尝试、回退默认。
- 目的：.pptx 与 PNG 字体视觉对齐，且不绑死 macOS。

**配色：以 `THEMES` 为唯一事实源，迁出代码进模板配置，删死代码 `colors.json`**。
- 现状双份失同步：硬编码 `THEMES`（真在用）+ `colors.json`（死代码，全引擎无引用）。
- 五色够用（accent / title_color / body_color / footer_color / cover_bg），9 版式复用，不加 per-版式颜色。
- 呼应 ticket 06「样式参数化进模板配置、与代码解耦」。
