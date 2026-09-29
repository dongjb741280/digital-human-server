# 调研 yiyan-ppt 的地图与动态搜图机制

Type: research
Status: resolved

## Question

yiyan-ppt 到底如何实现「地图」（内容→版式映射）和「动态搜图」？

具体要摸清：

1. 它的「地图」是什么数据结构：幻灯片类型 → 版式模板的映射表长什么样？版式用 JSON/代码怎么描述？
2. 「动态搜图」接的是哪家图库（Unsplash / Pexels / Bing / 自有图库）？搜索关键词从哪来（LLM 生成 / 标题 / 正文抽取）？哪些页配图？
3. 技术栈（python-pptx？还是别的渲染方式）？整体架构分几层？
4. 哪些做法可直接借鉴到本项目的 python-pptx 场景，哪些是它特有的？

## Answer

已通读 hl123-123/yiyan-ppt（基于 limaoyi1/Auto-PPT 改造）源码。

**「地图」= 两层**：
1. `my_ppt_mode/{1,2}/mode.json`：版式参数 JSON，含 `slide_size` + `first_page`/`catalog_page`/`main_page` 四段，各段含 `title_info`/`content_info`/`img_info`（font/size/pos_x/pos_y/width/height，单位 cm）。`main_page` 无 `img_info` = 纯文字模板。
2. `tree2ppt.py` 的 `traverse_tree()` 硬编码映射：Markdown 标题层级 → 页类型（树根→首页；`#` 无正文有子标题→目录页；叶子标题带正文→main_page）。

**动态搜图（较粗糙）**：百度图片爬虫，请求 `image.baidu.com/search/acjson` 正则抓 `thumbURL`；关键词仅用 PPT 总标题（非 LLM 生成、非正文抽取）；仅 `main_page` 含 `img_info` 的模板配图；正文图片全文只搜一次、随机抽一张、全局复用；首页用本地 `title.png`，背景图随机抽模板 `img/` 目录。

**分层**：`structure_article.py`（LLM 生成 Markdown 大纲）→ `mdtree/parser.py`（Markdown→树）→ `tree2ppt.py`（树→pptx）→ `img_search.py`。

**可借鉴**：① `mode.json` 版式参数化（版式与代码解耦，加模板只加 JSON+图片）；② Markdown→树→逐标题生成；③ 正文按 250 字分页；④ 首页/目录/正文页类型拆分。
**不建议照搬**：百度图片爬虫（无版权、易失效）；图片按段随机抽、不匹配段落语义；版式映射硬编码在代码而非配置。

来源：https://github.com/hl123-123/yiyan-ppt （上游 limaoyi1/Auto-PPT）
