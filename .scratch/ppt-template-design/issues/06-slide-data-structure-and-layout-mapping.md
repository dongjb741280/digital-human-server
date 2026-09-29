# 确定幻灯片数据结构与内容→版式判定机制

Type: grilling
Status: resolved

## Question

幻灯片内容怎么表示、版式由谁判定？

**现状**：LLM 直接输出 `[{title, bullets, notes}]`（每页标题 + 要点 + 备注），`build_pptx`/`render_slide` 只按 `index==0` 分封面/内容两型。

**参照**（ticket 01）：yiyan-ppt 用「LLM 生成 Markdown 大纲 → Markdown 标题层级(树) → 规则映射到版式(first/catalog/main)」，版式参数在 mode.json 里与代码解耦。

**候选方案**：
- A. 保持 JSON 数组，每项加 `layout`（版式类型）+ `image`（配图关键词/URL）+ `chart`（图表数据）字段，由 LLM 每页直接输出。
- B. 改为「Markdown 大纲 + 树」结构，规则（标题层级/内容形状）映射到版式，版式参数化（mode.json 式）与代码解耦。
- C. 混合：LLM 输出结构化 JSON（含 layout + 每页配图关键词），版式样式参数化在模板配置里。

需要决策：数据结构选哪种；版式由 LLM 还是规则判定；配图关键词是否由 LLM 每页生成（联动 ticket 08）。

## Answer

**数据结构：C（混合）**。LLM 输出结构化 JSON 数组，每页一个对象，带 `layout`（9 slug 封闭枚举）+ 版式专属字段；版式样式（配色/字体/坐标）进模板配置（mode.json 式），与代码解耦。不引入 Markdown 树。

**版式判定：LLM 判 + 规则兜底**。LLM 在 system prompt 拿到 9 slug 枚举与说明，每页输出一个 `layout`；后端安全网：首项强制 `cover`、末项强制 `closing`；未知/缺省 `layout` 回退 `content`。

**配图字段：结构上带**。`image_text`/`full_image`/`quote` 三型带 `image`（配图关键词）字段，LLM 仅对这三型输出，其余页缺省。关键词来源 / 哪些页配图 / 缓存降级由 ticket 08 定（06 不越界）。

**目标 schema**：

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
