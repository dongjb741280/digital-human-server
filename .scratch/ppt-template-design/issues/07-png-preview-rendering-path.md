# 确定 PNG 预览渲染路径

Type: grilling

## Question

生成可下载 .pptx 后，每页 PNG 预览图怎么出？

**现状**：Pillow 独立渲染每页 PNG（与 .pptx 的版式/母版无关）。

**问题**（ticket 04 结论）：Pillow 不读母版/主题/占位符，改用真母版后 PNG 预览会与 .pptx 脱节。

**候选**：
- A. LibreOffice headless 从生成的 .pptx 直接转 PNG（一致，但重引入 LibreOffice 依赖——历史上因本机没装 LibreOffice 导致 `process.waitFor()` 挂起，已移除）。
- B. Pillow 手动复刻母版视觉（无新依赖，但每加一个版式要双写、易漂移）。
- C. ppt-master 式「语义 SVG+标注」单一中间层，原生 pptx 与出图共用一份源（一致且可扩展，但工程量大、需自研 SVG→DrawingML / DrawingML→图 转换）。

需要决策：选哪条路，权衡一致性 vs 依赖 vs 工程量。
