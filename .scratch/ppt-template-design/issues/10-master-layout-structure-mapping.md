# 确定母版/版式结构与 9 slug 映射

Type: grilling
Status: resolved

## Question

给定 9 版式（ticket 05）+ 真母版路线（ticket 04：预制 .potx 底座）+ PNG 从 .pptx 渲染（ticket 07），预制模板怎么组织？

**待决**：
1. 主题数：沿用现有 4 套（bg_0~3 背景 + 配色）还是重构？
2. 母版/版式层级：每套主题一个 master + 若干 layout？9 个 slug 各自映射独立版式，还是几个 slug 共用版式、差异靠代码填形状？
3. 占位符 vs 代码画形状：哪些用真占位符（标题/正文/图片占位），哪些保留代码 add_shape（对比页两栏等无原生版式的）？

## Answer

**主题数 / 母版数**：主题仍是 4 套（数据：colors.json 配色 + bg_0~3 背景图）；母版 1 套（结构）。预置**一个** .potx（1 master + 3~4 版式）；4 套主题的配色/bg 继续在代码里注入（python-pptx 能给占位符/形状设色、贴全幅 bg 图）。不按主题乘 4 个 master（python-pptx 建不了母版，且颜色正交，乘了徒增维护）。

**9 slug → 版式映射**：

| slug | 版式 | 类型 |
|---|---|---|
| cover | Title Slide（title + subtitle 占位符） | 真占位符 |
| agenda | Title and Content | 真占位符 |
| section | Section Header | 真占位符 |
| content | Title and Content | 真占位符 |
| closing | Section Header 复用 | 真占位符 |
| image_text | Blank + 代码画（图 + 文字框） | 代码画 |
| full_image | Blank + 代码画（全幅图 + 标题框） | 代码画 |
| quote | Blank + 代码画（大字框） | 代码画 |
| comparison | Blank + 代码画（两栏） | 代码画 |

**占位符 vs 代码画 分界**：文字结构页（cover/agenda/section/content/closing）用真占位符（原生可编辑、吃主题）；视觉/图片页（image_text/full_image/quote/comparison）在 Blank 上代码画（图本就代码加、两栏/大字代码画，仍原生形状可编辑，只是非占位符）。
