# 调研 ppt-master 的母版与套模板机制

Type: research
Status: resolved

## Question

hugohe3/ppt-master 如何产出「真母版/版式（p:sldMaster / p:sldLayout）」、原生形状与图表，以及「套用自有 .pptx 模板」的机制？

具体要摸清：

1. 它如何生成真母版/版式（slide master / layout 继承）？用什么库（python-pptx 还是直接操作 OOXML）？
2. 「套用自有 .pptx 模板填空」的机制：怎么解析现有模板、识别占位符、把新内容填进去并保留设计？
3. 原生形状 / 图表 / 表格 / 动画是怎么做的，哪些依赖其 agentic 工作流（不适合照搬），哪些是纯库能力（可借鉴）？
4. 提炼 3~5 条可借鉴到本项目（python-pptx 同步生成 .pptx + Pillow 出图）的设计思想。

## Answer

**真母版/版式生成**：混合方案——python-pptx 仅作包骨架，母版/版式/占位符拓扑**直接手写 OOXML**（ElementTree + 正则编辑 p:sldMaster / p:sldLayout / p:sldMasterIdLst / p:sldLayoutIdLst / p:sp / p:ph）。SVG 上带 `data-pptx-layer/master/layout/placeholder` 标注，由 `template_structure.py` 编译成 master/layout/slide 三层结构。

**套用自有模板两条路**：① 填空（edit-native-pptx）：pptx↔SVG roundtrip，未改页字节级还原、改对象保留 shape id；② 提炼模板（create-template）：读 OOXML 抽母版/版式/占位符/主题成 manifest.json + inheritance.json。

**形状/图表/表格/动画**：全是自研纯工具（SVG→DrawingML 转换器、c:chart / a:tbl 直写），与 python-pptx 无关。

**可借鉴设计思想**：① 单一「语义 SVG+标注」中间层，双端导出（原生 pptx 与出图共用一份源，正好解 ticket 04 的 PNG 脱节问题）；② 母版/版式/占位符分层建模，占位符带语义角色；③ 源保真 roundtrip；④ python-pptx 只做骨架、母版/图表直写 OOXML；⑤ 导入时拆「身份/结构/方向」独立清单。

**不建议照搬**：整体是 agentic 工作流 + 自研 DrawingML 转换器（195KB elements.py），体量巨大。

关键文件（skills/ppt-master/）：`scripts/svg_to_pptx/pptx_package/{builder,template_structure}.py`、`drawingml/{converter,elements}.py`、`native_objects/{chart_xml,table}.py`。
