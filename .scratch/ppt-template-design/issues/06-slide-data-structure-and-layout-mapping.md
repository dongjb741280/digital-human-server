# 确定幻灯片数据结构与内容→版式判定机制

Type: grilling

## Question

幻灯片内容怎么表示、版式由谁判定？

**现状**：LLM 直接输出 `[{title, bullets, notes}]`（每页标题 + 要点 + 备注），`build_pptx`/`render_slide` 只按 `index==0` 分封面/内容两型。

**参照**（ticket 01）：yiyan-ppt 用「LLM 生成 Markdown 大纲 → Markdown 标题层级(树) → 规则映射到版式(first/catalog/main)」，版式参数在 mode.json 里与代码解耦。

**候选方案**：
- A. 保持 JSON 数组，每项加 `layout`（版式类型）+ `image`（配图关键词/URL）+ `chart`（图表数据）字段，由 LLM 每页直接输出。
- B. 改为「Markdown 大纲 + 树」结构，规则（标题层级/内容形状）映射到版式，版式参数化（mode.json 式）与代码解耦。
- C. 混合：LLM 输出结构化 JSON（含 layout + 每页配图关键词），版式样式参数化在模板配置里。

需要决策：数据结构选哪种；版式由 LLM 还是规则判定；配图关键词是否由 LLM 每页生成（联动 ticket 08）。
