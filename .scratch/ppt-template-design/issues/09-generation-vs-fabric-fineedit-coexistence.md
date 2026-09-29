# 确定生成路径与 fabric.js 细编辑路径的共存关系

Type: grilling
Status: resolved

## Question

新的「生成路径」（LLM → 9 版式结构化 JSON → build_pptx，见 ticket 06）与现有「fabric.js 细编辑路径」（前端画布 → 每页元素 JSON → build_pptx_from_elements）如何共存？

**现状**：细编辑后端（PptRecordDetail / AiCopyWritePptRecordDO）已按页存储前端画布的元素 JSON，`build_pptx_from_elements` 从元素 JSON 重新生成 .pptx；这是「所见即所得」的编辑态。生成路径（ticket 06）产出的则是「版式语义态」JSON（layout + 版式字段）。

**待决**：
1. 两套表示的关系：细编辑是「在生成结果上做增量替换」，还是「生成态 → 元素 JSON 态 → 编辑」的单一转换链，还是两套并存互不转换？
2. 落库怎么存：生成 JSON 与元素 JSON 双存，还是只存一份、按需转换？ticket 06 的 schema 是否需要同时容纳「生成态」与「编辑态」两副面孔？
3. 前端预览/下载（pptId/copywriteId）现在取哪条路径的产物；切换版式架构后，细编辑入口如何衔接？

## Answer

**关系：单一转换链 + 编辑覆盖**。语义态（ticket 06 的 layout JSON）是初始源；渲染细编辑画布时把语义态 → 元素态（每版式有确定性元素布局，转一次）；用户编辑后元素态覆盖该页；重新生成时编辑态优先。不做增量替换（要 diff 语义态，复杂），不并存互不转换（会漂移）。

**落库：双存**。`ppt_slide_content` 保留语义态；新增 `ppt_slide_elements`（可空）存编辑态，结束 `ppt_slide_content` 双义。regenerate 逐页判断：有 elements → `build_pptx_from_elements`，否则 → `build_pptx`（按 layout）。语义态永不丢（换模板/重生成正文仍可用），编辑态明确覆盖。

**预览 / 下载 / 细编辑入口**：
- 预览：走 `ppt_image_url`（ticket 07 后由 .pptx→PDF→PNG 渲染，与 .pptx 一致）。
- 细编辑画布初始化：该页有 `ppt_slide_elements` → 直接读；否则读 `ppt_slide_content` 语义态 → 转元素态渲染。
- 下载 .pptx：regenerate（编辑态优先）。

**留待实现确认（不阻塞本决策）**：语义态→元素态转换的位置 —— 前端是否已有，还是需新增引擎端点（如 `/layout_to_elements`）。本 ticket 定的是存储与关系模型，转换落点实现期定。
