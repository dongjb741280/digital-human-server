# 调研 python-pptx 真母版/版式的可行边界

Type: research
Status: resolved

## Question

python-pptx 对「真实母版/版式」的原生支持到底到什么程度？

具体要摸清：

1. slide master（p:sldMaster）/ slide layout（p:sldLayout）/ 占位符（placeholder）的 API 支持程度；能否用代码创建自定义 layout，或只能读预置 layout？
2. 原生形状、图表（chart）、表格、图片、超链接、动画 / 转场的支持边界；哪些是 python-pptx 做不到或需要手改 OOXML 的（渐变、阴影、SmartArt 等）。
3. 「真母版 + 原生可编辑」在本项目（同步生成可下载 .pptx + Pillow 出 PNG 预览）能落地到什么程度？有哪些现实约束？

## Answer

python-pptx 只能**读**母版/版式/占位符，不能**新建**自定义 master/layout/占位符（需解包 pptx 手改 OOXML）；只能向已有占位符填内容。

- 原生支持：autoshape / 文本框 / 图表（多数类型）/ 表格 / 图片 / 超链接。
- 不支持：转场、动画。
- 需手改 OOXML：渐变、阴影 / 发光 / 3D、SmartArt、动画时间轴、新建母版 / 版式 / 占位符。

**结论**：真母版要用**预制 .potx/.pptx 模板**（真母版 + 版式 + 占位符）作底座，python-pptx 填占位符 + 加原生形状/图表/表格/图片 → 原生可编辑；纯代码造母版做不到。

**关键约束**：Pillow 是独立渲染器，不读母版/主题/占位符，PNG 预览会与 .pptx 脱节。两条路：① LibreOffice headless 从生成的 .pptx 直接转 PNG（一致，但重引入 LibreOffice 依赖）；② Pillow 手动复刻母版视觉（易漂移）。

来源：python-pptx 官方文档（placeholders-using / layout-placeholders / charts / slides / txt-hyperlink）。
