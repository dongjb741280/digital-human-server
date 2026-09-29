# 确定 PNG 预览渲染路径

Type: grilling
Status: resolved

## Question

生成可下载 .pptx 后，每页 PNG 预览图怎么出？

**现状**：Pillow 独立渲染每页 PNG（与 .pptx 的版式/母版无关）。

**问题**（ticket 04 结论）：Pillow 不读母版/主题/占位符，改用真母版后 PNG 预览会与 .pptx 脱节。

**候选**：
- A. LibreOffice headless 从生成的 .pptx 直接转 PNG（一致，但重引入 LibreOffice 依赖——历史上因本机没装 LibreOffice 导致 `process.waitFor()` 挂起，已移除）。
- B. Pillow 手动复刻母版视觉（无新依赖，但每加一个版式要双写、易漂移）。
- C. ppt-master 式「语义 SVG+标注」单一中间层，原生 pptx 与出图共用一份源（一致且可扩展，但工程量大、需自研 SVG→DrawingML / DrawingML→图 转换）。

需要决策：选哪条路，权衡一致性 vs 依赖 vs 工程量。

## Answer

**渲染机制：A（LibreOffice headless）**。PNG 从生成的 .pptx 渲染，保证与真母版/版式一致；Pillow 不再承担逐页预览（漂移风险随 9 版式扩大）。

**管线**：`.pptx → PDF(soffice --headless --convert-to pdf) → 每页 PNG(pdftoppm -png -r 150)`。注意 LibreOffice `--convert-to png` 只出第一页，故走 PDF 中转；poppler 已装（`/opt/homebrew/bin/pdftoppm`）。

**执行侧：server（Java）**。.pptx 落到 server，就地转 PNG；LibreOffice（`/Applications/LibreOffice.app`）与 poppler 均本机可用，`PptToPdfUtil` 已有 LibreOffice 先例。引擎（Python）不再塞 subprocess（正是历史挂起源）。subprocess 调用必须加 timeout（历史 `waitFor()` 无超时是挂起根因，这次要带超时兜底）。

**范围外（不受影响）**：引擎 `list_templates` 的主题缩略图（Pillow `_render_thumbnail`）是合成迷你封面（主题选色预览），非逐页真实渲染，维持 Pillow，不与本决策冲突。
