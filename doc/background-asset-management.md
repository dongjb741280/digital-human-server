# 背景 / 素材管理

> 模块：`yudao-module-digital`
> 核心类：`VideoBackgroundController`、`VideoBackgroundServiceImpl`
> 数据表：`tb_ai_dh_video_background`（背景）、`tb_ai_dh_video_material`（素材/前景装饰）
> 对象存储：MinIO（`aidigital` bucket，`asset/background/`、`asset/material/`）
> 批量导入脚本：`digital-human-engine/scripts/download_backgrounds.py`

## 1. 概述

「背景」和「素材」是数字人视频的**图层素材库**，在视频制作的图层合成阶段被当作图层使用：

- **背景**（`background`）：视频的底图/底视频（对应图层类型 `layerType=6` 背景图片、`layerType=7` 动态背景视频）。
- **素材**（`material`）：叠加在画面上的前景装饰（对应图层类型 `layerType=2`），如 logo、图标、装饰线条、遮罩等。

两者都是简单的 CRUD 管理（上传 / 查询 / 删除），不涉及回调式异步流程。前端在视频制作页右侧「背景」「素材」侧栏里选择，数据落到视频的图层明细 `tb_ai_dh_video_layer_track`。

## 2. 数据模型

### 2.1 背景表 `tb_ai_dh_video_background`

| 字段 | 类型 | 说明 |
|---|---|---|
| `id` | varchar(50) | 主键（上传时 `UUID 前2位 + 时间戳`） |
| `bg_name` | varchar(200) | 背景名称 |
| `bg_url` | varchar(500) | 完整 MinIO URL（`http://host:9000/aidigital/asset/background/{file}`） |
| `bg_type` | varchar(20) | `0` 图片 / `1` 视频 |
| `bg_format` | varchar(255) | 文件格式：`1` png / `2` jpg / `3` mp4 |
| `bg_share` | varchar(200) | `0` 我的 / `1` 公共库 |
| `creator` / `create_time` | | 创建人 / 创建时间 |
| `deleted` | bit(1) | 逻辑删除标记，**`0` 未删除**（查询必须 `deleted='0'`） |

### 2.2 素材表 `tb_ai_dh_video_material`

| 字段 | 类型 | 说明 |
|---|---|---|
| `id` | varchar(50) | 主键 |
| `material_name` | varchar(200) | 素材名称 |
| `material_url` | varchar(500) | 完整 MinIO URL（`asset/material/{file}`） |
| `material_type` | varchar(1) | `1` 图片 / `2` 视频 |
| `bg_format` | varchar(1) | 文件格式：`1` png / `2` jpg / `3` mp4 |
| `bg_share` | varchar(1) | `0` 我的 / `1` 公共库 |
| `creator` / `create_time` | | 创建人 / 创建时间 |
| `deleted` | bit(1) | 逻辑删除标记，`0` 未删除 |

## 3. 存储路径

遵循 MinIO 统一规划（见 [minio-storage.md](./minio-storage.md)），背景/素材统一放在 `asset/` 下：

```
asset/background/{fileName}   # 背景图/背景视频
asset/material/{fileName}     # 前景装饰素材
```

- `bg_url` / `material_url` 存**完整 URL**（`http://host:9000/aidigital/asset/...`），与 `minioUpload` 返回格式一致。
- 视频图层合成时经 `MinioClientService.toObjectKey()` 归一化成相对键再下载。

## 4. 接口

前缀：`POST /digital-api/system/videoBackground`

| 接口 | 说明 | 关键入参 |
|---|---|---|
| `/backgroundQuery` | 背景查询 | `bgName`(模糊)、`bgShare`、`bgType`、`pageNum`、`pageSize` |
| `/backgroundUpload` | 背景上传 | 文件 + `bgType`、`bgShare` |
| `/backgroundDel` | 背景删除（逻辑删除） | `id` |
| `/materialQuery` | 素材查询 | `materialName`(模糊)、`bgShare`、`materialType`、`pageNum`、`pageSize` |
| `/materialUpload` | 素材上传 | 文件 + `materialType`、`bgShare` |
| `/materialDel` | 素材删除（逻辑删除） | `id` |

- 查询实现：`VideoBackgroundServiceImpl.backgroundQuery`（`:89`）、`materialQuery`（`:47`）。
- 查询都带 `deleted='0'` 过滤 + `orderByDesc(create_time)`。
- 删除是逻辑删除（`UPDATE deleted=1`），不物理删 MinIO 文件。
- 上传：`minioUpload` 到 `asset/background/` 或 `asset/material/`，返回完整 URL 落库。

## 5. 批量导入

背景/素材除了走页面上传，也可以批量生成或下载后导入。

### 5.1 脚本

`digital-human-engine/scripts/download_backgrounds.py`：从免费图源批量下载背景图 → 上传 MinIO → 输出入库 SQL。

```bash
# 免 key，随机照片（占位/联调）
python scripts/download_backgrounds.py --source picsum --count 12

# 按关键词搜图（需先申请免费 key 并设为环境变量）
export UNSPLASH_ACCESS_KEY=xxxx
python scripts/download_backgrounds.py --source unsplash --keyword "minimal business background" --count 12

export PEXELS_API_KEY=xxxx
python scripts/download_backgrounds.py --source pexels --keyword "minimal background" --count 12
```

| source | key | 说明 |
|---|---|---|
| `picsum` | 不需要 | 随机照片，不能指定风格 |
| `unsplash` | `UNSPLASH_ACCESS_KEY` | 按关键词搜图 |
| `pexels` | `PEXELS_API_KEY` | 按关键词搜图 |

- 脚本输出的 SQL 已带 `deleted=0`、`bg_share`（默认 `1` 公共库）、`bg_type='0'`、`bg_format='2'`。
- 若想让数据在「我的」tab 显示，把 SQL 里的 `bg_share` 改成 `'0'`。

### 5.2 免费图源

| 图源 | 授权 | 适用 |
|---|---|---|
| [Unsplash](https://unsplash.com) | 商用免费、免署名 | 高清背景照片（搜 `minimal background`/`abstract`） |
| [Pexels](https://pexels.com) | 商用免费、免署名 | 照片 + 视频 |
| [Pixabay](https://pixabay.com) | 商用免费 | 照片 + 插画 + 矢量 + 视频 |
| [Picsum](https://picsum.photos) | 免 key，随机 | 占位/联调 |
| [unDraw](https://undraw.co) | 开源免署名 | 插画 SVG（可改色，适合前景装饰） |
| [SVG Repo](https://www.svgrepo.com) | 各类 CC | SVG 图标/矢量 |

> 需要「简约商务风」等具体风格时，用 Unsplash/Pexels 按关键词搜图；纯代码生成（PIL 渐变）零版权、可精确控色，也是常用做法。

## 6. 关键点 / FAQ

- **`deleted` 必须写 0**：查询接口 `WHERE deleted='0'`，若用原生 SQL 插入时漏了 `deleted` 字段（默认 NULL），记录会被过滤掉、列表查不到。上传接口走 mapper 会自动处理，但手动导入 SQL 一定要带 `deleted=0`。
- **`bg_share` 决定在哪个 tab 显示**：`'0'` = 「我的」（前端默认 tab），`'1'` = 「公共库」。手动导入时按需设值，否则默认 tab 看不到。
- **`bg_type` / `material_type` 区分图片/视频**：背景 `0` 图片 / `1` 视频；素材 `1` 图片 / `2` 视频。查询时前端按 tab 传入对应值过滤。
- **`bg_format` 是格式代码**：`1` png / `2` jpg / `3` mp4。
- **删除不物理删文件**：`backgroundDel`/`materialDel` 只做逻辑删除（`deleted=1`），MinIO 里的文件会残留；如需彻底清理，需额外删 MinIO 对象。
- **与视频合成的关联**：背景/素材被选入图层后，`AiDhHumanVideoServiceImpl.videoAddImageLayerTrack` 会把它们的 `*_url` 放进 `layerList` 传给 Python `composite_video` 叠加（背景铺底、素材作前景）。
