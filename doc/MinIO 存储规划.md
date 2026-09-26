# MinIO 存储路径规划

> 范围：数字人平台全链路（声音、形象、文案、视频、素材）的 MinIO 对象存储路径规范
> 对象存储：MinIO（`aidigital` bucket）
> 生效版本：路径重构后（老数据不迁移，仅新文件走新路径）

## 1. 规划原则

早期各模块的文件散落在大量顶层目录（声音按 `{voiceId}/`、形象按 `{humanId}/`、视频中间产物散在 11 个顶层目录等），命名混乱、难以清理和管理。

重构后统一按 **「大类 → 实体 id → 功能用途」** 三层组织：

```
{category}/{entityId}/{purpose}/...
```

- **category**（顶层类别）：`avatar` 形象 / `voice` 声音 / `copywriting` 文案 / `video` 视频 / `asset` 公共素材。
- **entityId**（实体 id）：`{humanId}` / `{voiceId}` / `{pptId}` / `{videoId}`。
- **purpose**（功能用途）：如 `original` / `matting` / `ref` / `split` / `lipsync` / `composite` / `final` 等。

**迁移规则**：**老数据不迁移**，历史 DB 记录里的 url 字段仍指向老路径、老文件留在原位置；**只有新产生的文件走新路径**。读取侧经 `MinioClientService.toObjectKey()`（Java）/ `_to_object_key()`（Python）归一化，兼容老/新两种路径。

## 2. 新路径结构总览

```
aidigital/                            # bucket
├── avatar/{humanId}/                 # 形象（数字人）
│   ├── original/{humanId}.mp4        #   原始训练视频
│   ├── image/{humanId}_rgba.png      #   抠图 PNG（透明通道）
│   ├── image/123.png                 #   处理中占位图
│   └── matting/{humanId}.mp4         #   抠像绿幕视频（bg=1 去除背景）
│
├── voice/{voiceId}/                  # 声音
│   ├── ref/ref.wav                   #   切片后的参考音频
│   └── tts/{voiceId}.{ext}           #   声音复刻合成结果
├── voice/sample/{fileName}           #   上传的声音样本（未关联 voiceId 前）
├── voice/temp/{model,gpt,wav,sample}/#   声音训练临时产物
│
├── copywriting/{pptId}/              # 文案（按 PPT id 聚合）
│   ├── {pptId}.pptx                  #   PPT 文件
│   └── slides/{pptId}_{i}.png        #   每页幻灯片图片
│
├── video/{videoId}/{batch}/          # 视频（每次「立即制作」= 一个 batch）
│   ├── voice/{pptNum}.wav            #   每页 TTS 语音
│   ├── split/{pptNum}.mp4            #   裁剪分段
│   ├── lipsync/{pptNum}.mp4          #   对嘴型
│   ├── composite/{pptNum}.mp4        #   图层合成
│   ├── merged.mp4                    #   合并
│   ├── first_frame.jpg               #   首帧封面
│   └── final.mp4                     #   最终成片（带字幕）
│
└── asset/                            # 公共素材
    ├── background/{fileName}         #   背景图/视频
    ├── material/{fileName}           #   前景装饰
    └── example/{fileName}            #   示例视频
```

## 3. 旧 → 新路径对照表

| 模块 | 用途 | 旧路径 | 新路径 |
|---|---|---|---|
| 形象 | 原始视频 | `{humanId}/viedo/original/{humanId}.mp4` | `avatar/{humanId}/original/{humanId}.mp4` |
| 形象 | 抠图 PNG | `{humanId}/image/{humanId}_rgba.png` | `avatar/{humanId}/image/{humanId}_rgba.png` |
| 形象 | 占位图 | `{humanId}/mattingmage/123.png` | `avatar/{humanId}/image/123.png` |
| 形象 | 抠像绿幕视频 | `{humanId}/viedo/generatePath/{humanId}.mp4` | `avatar/{humanId}/matting/{humanId}.mp4` |
| 声音 | 上传样本 | `voice/{fileName}` | `voice/sample/{fileName}` |
| 声音 | 参考音频 | `{voiceId}/wav/ref.wav` | `voice/{voiceId}/ref/ref.wav` |
| 声音 | 声音复刻合成 | `voiceSample/{voiceId}_voice.{ext}` | `voice/{voiceId}/tts/{voiceId}.{ext}` |
| 文案 | PPT 文件 | `ppt/{pptId}.pptx` | `copywriting/{pptId}/{pptId}.pptx` |
| 文案 | 幻灯片图片 | `ppt/{pptId}_{i}.png` | `copywriting/{pptId}/slides/{pptId}_{i}.png` |
| 视频 | 每页 TTS 语音 | `voiceSample/{batch}_{pptId}_{pptNum}_{videoId}_voice.wav` | `video/{videoId}/{batch}/voice/{pptNum}.wav` |
| 视频 | 裁剪分段 | `voicesplithuman/{batch}_{pptId}_{videoId}_{pptNum}.mp4` | `video/{videoId}/{batch}/split/{pptNum}.mp4` |
| 视频 | 对嘴型 | `MuseTalk/results/{batch}_{pptId}_{pptNum}_{videoId}/{...}.mp4` | `video/{videoId}/{batch}/lipsync/{pptNum}.mp4` |
| 视频 | 图层合成 | `layerVideoData/{batch}_{pptId}_{pptNum}_{videoId}.mp4` | `video/{videoId}/{batch}/composite/{pptNum}.mp4` |
| 视频 | 合并 | `videomerge/{batch}_{videoId}/{batch}_{videoId}.mp4` | `video/{videoId}/{batch}/merged.mp4` |
| 视频 | 首帧 | `videofirstframe/{batch}_{videoId}/{batch}_{videoId}.jpg` | `video/{videoId}/{batch}/first_frame.jpg` |
| 视频 | 最终成片 | `videocaptions/{batch}_{videoId}/{batch}_{videoId}.mp4` | `video/{videoId}/{batch}/final.mp4` |
| 素材 | 背景 | `background/{fileName}` | `asset/background/{fileName}` |
| 素材 | 装饰 | `materia/{fileName}` | `asset/material/{fileName}` |
| 素材 | 示例视频 | `example/{fileName}` | `asset/example/{fileName}` |

## 4. 各模块写入点

### 4.1 形象（avatar）

| 文件 | 写入方 | 位置 |
|---|---|---|
| 原始视频 | Java `AiDhHumanServiceImpl` | `uploadVideo` → `avatar/{id}/original/{fileName}` |
| 占位图 | Java `AiDhHumanServiceImpl` | `uploadDefaultImage` → `avatar/{id}/image/123.png` |
| 抠图 PNG | Python `services/video.py:matting_image` | `avatar/{id}/image/{id}_rgba.png`（回调 `changeImega` / `changeImegaAndViedo`） |
| 抠像绿幕视频 | Python `services/video.py:matting_video` | `avatar/{id}/matting/{fileName}` |

- Java `create` 传给 Python 的 `remote_path` / `remote_image_path` / `remote_viedo_path` 已改为 `avatar/{id}/original` / `avatar/{id}/image` / `avatar/{id}/matting`。
- 回调 `callBackAiDhHuman` 写回 `first_frame=avatar/{id}/image`。

### 4.2 声音（voice）

| 文件 | 写入方 | 位置 |
|---|---|---|
| 上传样本 | Java `TbAiDhVoiceController` | `voice/sample/{fileName}` |
| 参考音频 | Python `services/voice.py:train_voice` | `voice/{voiceId}/ref/ref.wav` |
| 复刻合成 | Python `app.py:tts`（voiceType=1） | `voice/{voiceId}/tts/{voiceId}.{ext}` |

### 4.3 文案（copywriting）

| 文件 | 写入方 | 位置 |
|---|---|---|
| PPT 文件 | Python `app.py:generate_ppt` | `copywriting/{pptId}/{pptId}.pptx` |
| 幻灯片图片 | Python `app.py:generate_ppt` | `copywriting/{pptId}/slides/{pptId}_{i}.png` |

### 4.4 视频（video）

| 文件 | 写入方 | 位置 |
|---|---|---|
| 每页 TTS 语音 | Python `app.py:tts`（voiceType=2） | `video/{videoId}/{batch}/voice/{pptNum}.wav` |
| 裁剪分段 | Python `app.py:make_ppt_voice2video` | `video/{videoId}/{batch}/split/{pptNum}.mp4` |
| 对嘴型 | Python `app.py:change_video` | `video/{videoId}/{batch}/lipsync/{pptNum}.mp4` |
| 图层合成 | Python `app.py:deal_video` | `video/{videoId}/{batch}/composite/{pptNum}.mp4` |
| 合并 | Python `app.py:merge_video` | `video/{videoId}/{batch}/merged.mp4` |
| 首帧 | Python `app.py:get_first_frame` | `video/{videoId}/{batch}/first_frame.jpg` |
| 最终成片 | Python `app.py:add_captions` | `video/{videoId}/{batch}/final.mp4` |

- 各步骤输出路径由 Java 侧直接拼 `video/{videoId}/{batch}/...` 传入 Python（不再用 `digital-ability` 配置里的占位符替换）。
- 回调标识符（`batch_pptid_videoid_list` / `batchPptidPagenum` / `batch_video_id`）与文件路径分离，标识符格式 `batch_pptId_videoId_pptNum` 不变，目录结构变化不影响回调解析。

### 4.5 素材（asset）

| 文件 | 写入方 | 位置 |
|---|---|---|
| 背景 | Java `VideoBackgroundController` | `asset/background/{fileName}` |
| 装饰 | Java `VideoBackgroundController` | `asset/material/{fileName}` |
| 示例视频 | 常量 `AiDhHumanController.EXAMPLE_VIDEO_KEY` | `asset/example/123.mp4` |

## 5. 兼容性说明

- **读取**：`MinioClientService.toObjectKey()`（Java）/ `_to_object_key()`（Python）会把老路径里的 `http://host:port/bucket/`、`sftpFile/`、`aifs01/`、`aidigital/` 前缀剥掉，因此老/新两种相对键都能正确下载。
- **删除**：`AiDhHumanVideoServiceImpl.deleteVideoMinioFiles()` 动态收集主表 + 执行日志里所有 `*_url` 字段去重后删除，不依赖硬编码路径，老/新视频都能连带清理。
- **老数据**：不迁移。老记录仍指向老键，老文件留在原位置；老视频的「下载/删除/播放」不受影响（走 `toObjectKey` 归一化）。

## 6. 遗留清理项

- Java 侧 `AiDhHumanVideoServiceImpl` 里 `voiceAddHUmanresultDir`、`mergeOutputFilePath`、`splitHumanOutPutUrl` 等 `@Value` 字段已不再使用（改为直接拼 `video/{videoId}/{batch}/...`），`application-local.yaml` / `application-test.yaml` 里对应占位符同理，属可清理的死配置。
- `museTalkdownLocalPath`、`mergeDirectory`、`mergeVoiceDirectory` 等本地目录（非 MinIO）仍沿用旧配置，未纳入本次重构。
