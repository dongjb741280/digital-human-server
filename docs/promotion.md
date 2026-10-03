# 开源一个数字人平台：GPT-SoVITS 克隆声音、Claude 手写 PPT、7 步流水线合成数字人视频

数字人这两年很火，但市面上的方案大多要么是纯 SaaS 闭源，要么是单个模型的 demo，很难拼成一条「从形象、声音到成片」的完整链路。我花了些时间把这条链路完整做出来并开源了，这里分享一下架构设计和几个踩坑比较深的点。

**在线体验**：<https://dongjb741280.github.io/digital-human-server/>

## 它是什么

一个全栈的数字人内容生产平台，覆盖从「素材」到「成片」的完整闭环：

| 能力 | 说明 |
|---|---|
| 形象复刻 | 上传训练视频 → rembg 抠像 → 透明 PNG + 绿幕视频 |
| 声音复刻 | GPT-SoVITS zero-shot 零样本克隆 |
| 文案创作 | LLM 生成提纲 / 正文 / PPT |
| PPT 创作 | Claude tool-use 逐页手写 SVG，直出原生可编辑 .pptx |
| PPT 在线编辑 | WOPI 协议接入 Collabora，浏览器里直接改 .pptx |
| 视频创作 | 7 步流水线自动合成数字人播报视频 |
| 智能体 / 实时互动 | SSE 流式对话、话术对练、流程编排 |

拆成三个仓库，职责清晰：

- **digital-human-server**：Java 后端，Spring Cloud 微服务（网关 / 系统 / 基础设施 / 数字人业务四服务）
- **digital-human-web**：Vue3 前端
- **digital-human-engine**：Python AI 编排服务，承载真正的推理

## 架构：Java 做编排，Python 做推理，回调推进

这是整个系统最核心的设计取舍。数字人视频制作涉及一堆重 AI 操作（TTS、抠像、口型合成、图层合成、ffmpeg），这些活 Java 干不了也不该干，但 Java 擅长的是业务编排、落库、权限、任务状态管理。

所以我把系统拆成两层：

![架构图](https://raw.githubusercontent.com/dongjb741280/digital-human-server/main/docs/assets/architecture.png)

- **Java 侧**负责把「制作视频」拆成 7 个串行步骤写进执行日志，逐步调 Python，再**靠 Python 回调推进下一步**。
- **Python 侧**只做推理，做完上传 MinIO、回调 Java 更新状态。

这套「回调驱动」而不是「轮询」的设计，让每一步都解耦、可独立重试，也能支撑弹性伸缩。

## 亮点一：7 步回调式视频流水线

视频创作是「数字人在 PPT 画面上照着文案逐页说话」。后端把一次制作拆成 7 步：

![7 步流水线](https://raw.githubusercontent.com/dongjb741280/digital-human-server/main/docs/assets/pipeline.png)

1. **文字转语音** —— 每页文案 → GPT-SoVITS 合成语音
2. **裁剪分段** —— 参考数字人视频按每段语音时长循环/裁剪
3. **对嘴型** —— 视频 + 语音 → Wav2Lip / MuseTalk 口型合成
4. **图层合成** —— 背景 + PPT 铺满画布 + 数字人绿幕抠图叠加
5. **合并** —— 所有分段 concat 成完整成片
6. **提取首帧** —— 生成封面图
7. **加字幕** —— 按页时间轴烧录 ASS 字幕

几个工程上的细节，都是我踩过坑才定型的：

- **执行日志驱动状态机**：7 步任务先全部写入 `exec_log`，每步用 `model_type` 区分，`batch_num` 标识一次制作批次。`video_status` 从 1（已保存）→ 2（执行中）→ 4（成功）。
- **并发控制**：图层合成那步最重，做了最多 3 段并发（`TOTAL_CAN_RUN_NUM=3`），用 `exec_status='3'` 标记执行中。
- **一个经典竞态**：早期把「执行中」状态设在调 Python **之后**，结果回调先置成功又被覆盖回执行中，合并步骤永远等不到下一步。改成**先置状态再调用**才修好——这类回调时序 bug 在异步系统里特别隐蔽。
- **回调契约**：Python 回调里「标识符」和「文件路径」分开传，Java 按标识符定位日志行、按路径落字段，改 MinIO 目录结构不影响契约。

## 亮点二：ppt-master，让 LLM 当大脑、脚本当手脚

除了常规的「python-pptx 九版式生成」，我还做了一条更有意思的路线：**让 Claude 通过 tool-use 循环逐页手写 SVG，再导出成原生可编辑的 .pptx**。

流程是：主题/素材/模板交给 Claude 的 tool-use 循环（`bash` / `read_file` / `write_file` / `web_search`，最多 300 轮），逐页手写 SVG，再由 `svg_to_pptx` 导出成带**真实母版和版式**（`p:sldMaster` / `p:sldLayout`）的 .pptx。这意味着产出不是「图片贴进去」的假 PPT，而是能继续在 PowerPoint / Collabora 里逐字编辑的原生文件。

同时引擎 prompt 开了 `--with-notes`，生成每页讲解词，这些备注又直接喂给视频流水线当配音文案——**文案、PPT、视频三者真正串起来了**。

安全性上我也实话实说：引擎的 `bash` 工具目前是「软沙箱」（进程内白名单过滤，非 OS 级隔离），只建议可信内网开启，不对公网暴露。

## 亮点三：PPT 在线编辑（Collabora / WOPI）

接入了 Collabora Online，通过 WOPI 协议在浏览器里直接编辑 .pptx，编辑后自动重渲染预览图、重新提取 speaker notes。这条链路打通后，「生成 PPT → 在线改 → 再出视频」就完全没有导出/导入的断点了。

## 怎么跑起来

- 中间件：MySQL、Redis、RabbitMQ、Nacos、MinIO
- Java 侧四个服务按 system → infra → digital → gateway 顺序启动（`--spring.profiles.active=local`）
- Python 侧启动 `digital-human-engine`，配好 GPT-SoVITS / Wav2Lip

详细时序和部署文档都在仓库 `doc/` 目录下（形象复刻、声音复刻、视频创作、ppt-master 都有完整时序图）。

## 仓库地址

- 后端：<https://github.com/dongjb741280/digital-human-server>
- 前端：<https://github.com/dongjb741280/digital-human-web>
- AI 引擎：<https://github.com/dongjb741280/digital-human-engine>

---

项目还在持续迭代，欢迎 star、提 issue、一起完善。如果你也在做数字人 / AIGC 方向，欢迎交流。
