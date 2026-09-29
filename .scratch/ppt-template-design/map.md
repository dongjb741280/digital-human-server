# PPT 模板美观化设计

## Destination

选型决策 + 可落地规格：确定 `digital-human-engine` 的 PPT 模板架构，产出足够细节的规格（版式清单、母版/版式结构、配色/字体/配图策略、幻灯片数据结构），可直接交给 `digital-human-engine` 实现。核心诉求：让生成的 PPT 更美观 —— 版式多样 + 真实母版/原生可编辑 + 动态搜图配图。

## Notes

- 领域：PPT 生成逻辑在 `digital-human-engine`（Python，本仓库外的兄弟项目 `/Users/dongjb/IdeaProjects/ai-digital/digital-human-engine`）；本仓库 `digital-human-server` 只做 HTTP 转发 + 落库。
- 关键文件：`digital-human-engine/services/ppt.py`（生成/渲染）、`services/templates/`（内置 4 套主题：bg_0~3.jpg + colors.json）、`services/llm.py`。
- 现状：已有 4 套硬编码主题（背景图 + 配色），但只有 2 种版式（封面 + 通用内容页），用 `blank` 空白版式手动画形状，无真母版、无版式多样性、无内容配图 —— 这是"不够美观"的结构性根因。
- 已确认决策（Q1~Q4 结论）：
  - 终点 = 选型 + 规格（非直接实现）；
  - 主路线 = 混合（python-pptx 原生母版为底座 + "内容→版式地图" + ppt-master 作原生深度参照）；
  - **动态搜图：上**（依赖外部图库 API，key/授权/成本/中文命中待选型）；
  - 优先级 = 版式多样性 + 真实母版 > 配色字体 > 配图。
  - 图库选型（用户拍板 2026-09-29）：百度图片在线搜索（image.baidu.com 网页搜索），替代 Pexels 调研推荐；版权 / 稳定性 / 分辨率风险已确认接受。
- 长期偏好：借鉴外部项目用 python-pptx 原生复刻，不整体引入外部工作流/框架（呼应 memory [[frontend-integration]] 的"原生复刻"原则）。
- 建议技能：`research`（调研外部项目/API）、`grilling`（决策对话）、`domain-modeling`（术语/结构）。

## Decisions so far

- [调研 python-pptx 真母版/版式的可行边界](issues/04-research-python-pptx-master.md)：python-pptx 不能新建母版/版式/占位符，真母版须用预制模板作底座；Pillow 独立渲染导致 PNG 与 .pptx 脱节。
- [调研动态搜图图库 API 选型](issues/03-research-image-api.md)：Pexels 首选（免费商用、中文 locale、2万次/月），Bing Image 备选（中文命中最高但版权风险），Unsplash 补充。
- [调研 yiyan-ppt 的地图与动态搜图机制](issues/01-research-yiyan-ppt.md)：地图=Markdown 标题层级→页类型→mode.json 版式参数；动态搜图很糙（百度爬虫、仅标题关键词、随机抽图）；可借鉴 mode.json 版式参数化 + Markdown→树→逐标题。
- [调研 ppt-master 的母版与套模板机制](issues/02-research-ppt-master.md)：真母版=直写 OOXML + SVG 标注中间层，双端导出；可借鉴「语义中间层单源双端」「占位符分层建模」；整体体量巨大不宜照搬。

## Not yet specified

- 与现有 fabric.js 细编辑路径（`build_pptx_from_elements`）如何共存 —— 依赖 ticket 05、06 的结论

## Out of scope

- 整体引入 ppt-master（agentic 工作流形态）—— 只借鉴设计思想，不整体引入
- AI 生图 / 多模态配图（如 gpt-image）—— 超出"动态搜图"范围，暂不做
- SmartArt 等 python-pptx 无法原生支持的对象 —— 待 ticket 04 界定边界
