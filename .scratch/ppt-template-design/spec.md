# PPT 模板美观化设计 — 落地规格

> 面向 `digital-human-engine`（Python）的实现规格，本规格是 `ppt-template-design` wayfinder 地图 11 张决策票的汇总。需要决策细节时回查 `.scratch/ppt-template-design/issues/`。

## 1. 目标与路线

- 目标：让生成的 PPT 更美观 —— 版式多样 + 真实母版/原生可编辑 + 动态搜图配图。
- 主路线：python-pptx 原生母版（预制 .potx 底座）+ 内容→版式判定（LLM）+ 动态搜图（百度图片）。
- 借鉴：yiyan-ppt 的 mode.json 版式参数化 + Markdown→树→逐标题；ppt-master 的「语义中间层单源双端」「占位符分层建模」。不整体引入二者。

## 2. 版式清单（9 种）

| slug | 名称 | 承载内容 | 配图 |
|---|---|---|---|
| cover | 封面页 | 主标题 + 副标题/主题名 + 强调条 | 主题内置背景图 |
| agenda | 目录页 | 章节条目列表 | 否 |
| section | 章节过渡页 | 章节序号 + 章节标题 | 主题内置背景图 |
| content | 标题+要点页 | 页标题 + 要点列表 | 否 |
| image_text | 左图右文页 | 配图 + 标题 + 要点 | ✅ 动态搜图 |
| full_image | 全图页 | 全屏图 + 叠加标题 | ✅ 动态搜图 |
| quote | 金句/引用页 | 大字引文 + 出处 | ✅ 动态搜图（可作背景） |
| comparison | 对比页 | 两栏对比 | 否 |
| closing | 结束页 | 致谢/总结语 | 否 |

> v2（本规格不做）：timeline 时间线、chart 图表（chart 需 LLM 输出结构化图表数据，会改数据结构）。

## 3. 幻灯片数据结构（LLM 输出 schema）

LLM 输出结构化 JSON 数组，每页一个对象，带 `layout`（9 slug 枚举）+ 版式专属字段：

```json
[
  {"layout":"cover","title":"主标题","subtitle":"副标题?","notes":"备注?"},
  {"layout":"agenda","items":["章节1","章节2"]},
  {"layout":"section","title":"章节标题"},
  {"layout":"content","title":"页标题","bullets":["要点1"],"notes":"备注?"},
  {"layout":"image_text","title":"...","bullets":["..."],"image":"配图关键词","image_side":"left"},
  {"layout":"full_image","title":"...","image":"配图关键词"},
  {"layout":"quote","text":"引文","source":"出处?","image":"配图关键词?"},
  {"layout":"comparison","title":"...","left":{"title":"A","points":["..."]},"right":{"title":"B","points":["..."]}},
  {"layout":"closing","title":"谢谢/总结"}
]
```

- **版式判定：LLM 判 + 规则兜底**。LLM 在 system prompt 拿 9 slug 枚举 + 说明，每页输出 `layout`；后端安全网：首项强制 `cover`、末项强制 `closing`；未知/缺省回退 `content`。
- **配图字段**：`image_text`/`full_image`/`quote` 带 `image`（LLM 出 2~6 字中文实义词），其余缺省。
- **样式解耦**：版式样式（配色/字体/坐标）进模板配置，与代码解耦（mode.json 式）。

## 4. 母版/版式结构

- **主题 4 套（数据）、母版 1 套（结构）**：预置一个 .potx（1 master + 3~4 版式）；4 套主题的配色/bg 在代码注入。
- **9 slug → 版式映射**：

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

- **分界规则**：文字结构页（cover/agenda/section/content/closing）用真占位符（原生可编辑、吃主题）；视觉页（image_text/full_image/quote/comparison）在 Blank 上代码画（仍原生形状可编辑，非占位符）。
- **约束**：python-pptx 不能建母版/版式/占位符，真母版须用预制 .potx 底座；仅能向已有占位符填内容 + 加原生形状/图片。

## 5. 配色/字体策略

- **配色**：以单一配置为唯一事实源（迁出代码，合并现硬编码 `THEMES` 与死代码 `colors.json`）。五色：accent / title_color / body_color / footer_color / cover_bg，9 版式复用，不加 per-版式颜色。
- **字体**：全局一套（不 per-theme）。.pptx 侧显式 `font.name`（微软雅黑/Microsoft YaHei，或跨平台 Noto Sans CJK SC）；PNG 侧 `_FONT_CANDIDATES` 跨平台候选（macOS PingFang/Hiragino + Linux Noto Sans CJK SC/WenQuanYi + Windows msyh/simhei）。

## 6. PNG 预览渲染路径

- **LibreOffice headless 从 .pptx 渲染**（一致，Pillow 不再承担逐页预览）。
- 管线：`.pptx → PDF(soffice --headless --convert-to pdf) → 每页 PNG(pdftoppm -png -r 150)`（soffice `--convert-to png` 只出第一页，故走 PDF 中转）。
- **server（Java）侧执行**，subprocess 加 timeout（历史 `waitFor()` 无超时是挂起根因）。
- 主题缩略图（`list_templates`）维持 Pillow（合成迷你封面，非逐页渲染）。

## 7. 动态搜图方案

- 图库：百度图片在线搜索（image.baidu.com 网页搜索，用户拍板；版权/稳定性/分辨率风险已接受）。
- 关键词：LLM 出 2~6 字中文实义词；不翻译。
- 分档：image_text 取缩略图/中尺寸；full_image/quote 加「壁纸/高清」修饰 + 抓原图（thumbURL 分辨率低）。
- 缓存/降级：按关键词缓存（进程内 + 短期落库）；失败回退 bg_0~3（full_image/quote）或降级 content（image_text）；不主动署名。

## 8. 生成路径与 fabric 细编辑共存

- **单一转换链 + 编辑覆盖**：语义态是初始源，渲染细编辑画布时转元素态，编辑后元素态覆盖，regenerate 编辑态优先。
- **双存**：`ppt_slide_content`（语义态）+ 新增 `ppt_slide_elements`（可空，编辑态），结束单字段双义。
- **预览/下载/细编辑**：预览走 ppt_image_url；细编辑读 elements 优先否则 content 转 elements；下载 regenerate。
- 语义→元素转换落点（前端已有 vs 引擎 `/layout_to_elements` 端点）实现期定。

## 9. 不做（out of scope）

- 整体引入 ppt-master（agentic 工作流形态）。
- AI 生图 / 多模态配图（gpt-image 等）。
- SmartArt 等 python-pptx 无法原生支持的对象。
