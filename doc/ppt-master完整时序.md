# ppt-master 完整时序

> 模块：`yudao-module-digital`
> 核心类：`AiDhPptController`、`AiDhPptServiceImpl`、`CallPythonService`
> 数据表：`tb_ai_dh_copywrite`（文案主表）、`tb_ai_dh_copywrite_ppt_record`（PPT 记录）、`tb_ai_dh_copywrite_ppt_record_detail`（每页明细）
> 菜单：`system_menu`（id=2837，parent=2763 数字人平台）
> 对象存储：MinIO（`aidigital` bucket）

## 1. 概述

ppt-master 是区别于「9 版式 python-pptx 一键生成」（`generate_ppt`，见 [`PPT制作完整时序.md`](./PPT制作完整时序.md)）的另一条**高质量 PPT 路线**：**LLM 当大脑 + 引擎脚本当手脚**，把主题/素材/模板交给 Claude 的 tool-use 循环，逐页手写 SVG，再由 `svg_to_pptx` 导出成**原生可编辑 .pptx**（含真实母版/版式 `p:sldMaster`/`p:sldLayout`）。

整条链路一次同步调用完成（无提纲/正文分步）：**前端 `ppt-master.vue` 一键生成 → Java 转发 Python → Claude tool-use 生成 .pptx 上传 MinIO → Java 落库 + LibreOffice 出预览图**。

> 设计细节（引擎侧软沙箱、接口契约、成本、安全）见 `digital-human-engine` 仓库（本仓库外兄弟项目）的 `docs/api-service-design.md`。

## 2. 参与者

| 角色 | 说明 |
|---|---|
| 前端 | `ppt-master.vue`（菜单「ppt-master」，`src/views/digital/ppt-master.vue`） |
| 后端 | `AiDhPptController` + `AiDhPptServiceImpl` + `CallPythonService` |
| MySQL | 文案主表 / PPT 记录 / 每页明细（复用文案创作三张表） |
| MinIO | 生成的 `.pptx` + 每页幻灯片 PNG 预览图 |
| Python 编排服务（`digital-human-engine`，60013） | `app.py:/generate_ppt_master` → `services/ppt_master.py`（Claude tool-use 循环）+ `skills/ppt-master/`（引擎脚本） |
| LLM 服务（`ai-route.huihaohealth.com`） | Anthropic `/v1/messages`（`claude-opus-4-7-cc`），`thinking: adaptive` + `output effort: high` |
| LibreOffice | Java 侧 `soffice` → PDF → PDFBox 逐页 PNG |

## 3. Mermaid 时序图

```mermaid
sequenceDiagram
    autonumber
    participant FE as 前端<br/>(ppt-master.vue)
    participant BE as 后端<br/>(Digital :48083)
    participant DB as MySQL
    participant ORCH as Python 编排服务<br/>(digital-human-engine :60013)
    participant LLM as Claude<br/>(ai-route)
    participant MIO as MinIO
    participant LO as LibreOffice

    FE->>BE: POST /aiDhPpt/generate_ppt_master (title/pages/lang/canvas/images/sources/template)
    BE->>ORCH: POST /generate_ppt_master（长超时 900s，同步阻塞）
    loop tool-use 循环（最多 300 轮）
        ORCH->>LLM: Anthropic /v1/messages（stream + thinking adaptive）
        LLM-->>ORCH: 工具调用（bash / read_file / write_file）
        ORCH->>ORCH: 执行引擎脚本：project_manager.py / svg_quality_checker.py / svg_to_pptx.py
    end
    ORCH->>MIO: 上传 copywriting/pptmaster_<ts>/pptmaster_<ts>.pptx
    ORCH-->>BE: {pptUrl, recordDesc, summary, usage}
    BE->>DB: INSERT 文案主表 + PPT 记录
    BE->>MIO: 下载 .pptx
    BE->>LO: soffice → PDF → PDFBox 逐页 PNG
    BE->>MIO: 上传每页 PNG 预览
    BE->>DB: INSERT 每页明细（ppt_image_url=预览图，语义/编辑态置空）
    BE-->>FE: {pptId, copywriteId, pptUrl, recordDesc, summary}
    FE->>BE: POST /copyWritManage/downloadPPT (id=pptId) 下载 .pptx
```

## 4. 各步骤详细说明

### ① 一键生成 ppt-master = generate_ppt_master

- 接口：`POST /digital-api/system/aiDhPpt/generate_ppt_master`（controller `AiDhPptController`，service `AiDhPptServiceImpl.generatePptMaster`）
- 入参：`title`（主题，必填）、`pages`（4~30，默认 8）、`lang`（`zh-CN`/`en`）、`canvas`（`ppt169`/`ppt43`）、`images`（`none`/`web`）、`sources`（本地路径或 URL 列表，可空）、`template`（模板工作区根，可空）、`doc_name`、`user`
- 转发：`CallPythonService.callToPPtPythonPostLong()` → **长超时 900s**（`AbilityShareClient.doPostPPtLong`）。ppt-master 是分钟级串行生成，普通 `doPostPPt`（300s）会超时。
- Python：`app.py:/generate_ppt_master` → `services/ppt_master.py:generate_deck()`
  - 门禁：`PPT_MASTER_ENABLED=1` 才启用，否则返回 `code=9999`。
  - Claude tool-use 循环：工具 `bash`/`read_file`/`write_file`，最多 300 轮，`bash` 命令白名单 + read/write 路径沙箱（软沙箱，非 OS 级）。
  - 产出：`projects/pptmaster_<ts>_*/exports/*.pptx` → 上传 `copywriting/pptmaster_<ts>/pptmaster_<ts>.pptx`。
  - 返回 `{pptUrl, recordDesc, summary, usage}`。
- 后端 `AiDhPptServiceImpl.generatePptMaster`：
  1. `copywritingCreate()` 落文案主表（内容用主题占位，ppt-master 无正文）→ `copywriteId`；
  2. `copywritingCreatePPT()` 落 PPT 记录 → `pptId`；
  3. `renderPptToImages(pptUrl, pptId)`：soffice→PDF→PDFBox 逐页 PNG（**失败不阻断主产物，仅记日志**）；
  4. 逐页 `insertPptRecordDetail()` 落明细（`ppt_image_url` 存预览图，`ppt_slide_content`/`ppt_slide_elements` 置空）。
- 返回 `{pptId, copywriteId, pptUrl, recordDesc, summary}`。

### ② 下载 .pptx

- 前端 `ppt-master.vue` 拿到 `pptId` 后调 `POST /copyWritManage/downloadPPT`（`{id: pptId}`），复用文案管理的下载链路，从 MinIO 取 `.pptx` 返回前端 `download.pptx` 保存。

## 5. 数据表

| 表 | 作用 |
|---|---|
| `tb_ai_dh_copywrite` | 文案主表，内容为主题占位，`main_ppt_id` 指向 PPT 记录 |
| `tb_ai_dh_copywrite_ppt_record` | PPT 记录，`ppt_url` 指向 `copywriting/pptmaster_<ts>/...pptx` |
| `tb_ai_dh_copywrite_ppt_record_detail` | 每页明细，`ppt_image_url` 存 LibreOffice 预览图（语义/编辑态置空） |

## 6. 菜单

菜单由 `system_menu` 驱动，迁移脚本 [`sql/mysql/2026-10-01_add_ppt_master_menu.sql`](../sql/mysql/2026-10-01_add_ppt_master_menu.sql)：

- `system_menu`：id=2837，name=`ppt-master`，parent_id=2763（数字人平台），path=`ppt-master`，component=`digital/ppt-master`。
- `system_role_menu`：授权给与「PPT制作」相同的角色（超级管理员 + 演示/业务等 8 个角色）。

> 菜单树由后端 `get-permission-info` 每次从 DB 现查，插库后无需清 Redis；但前端把菜单缓存在浏览器 localStorage（`roleRouters`/`user`），**需退出登录重新登录**（或清 localStorage）才能看到新菜单。

## 7. 关键点

- **同步阻塞**：整条链路一次 HTTP 调用完成，分钟级；前端 `timeout:0` 无限等，Java→Python 900s 超时。
- **两条 PPT 路线并存**：`generate_ppt`（9 版式 python-pptx，可细编辑）与 `generate_ppt_master`（SVG→svg_to_pptx 原生可编辑 + 母版/版式），互不替换。
- **软沙箱非生产隔离**：引擎 `bash` 工具仍 `shell=True`，白名单是进程内字符串过滤；只建议可信内网开启（`PPT_MASTER_ENABLED=1`），不对公网开放。
- **预览图复用**：与 `generate_ppt` 同一套 `renderPptToImages`（LibreOffice），保证预览与 .pptx 版式一致。

## 8. 故障排查 FAQ

### 8.1 返回「ppt-master engine disabled」

**现象**：`code=9999`，msg 提示 `ppt-master engine disabled`。

**原因**：Python 侧未开启开关。

**解决**：在 `digital-human-engine/.env` 设 `PPT_MASTER_ENABLED=1` 并重启编排服务。

### 8.2 前端看不到「ppt-master」菜单

**现象**：侧边栏没有 ppt-master 菜单。

**原因**：菜单缓存在浏览器 localStorage。

**解决**：退出登录重新登录（或控制台执行 `localStorage.removeItem('roleRouters'); localStorage.removeItem('user');` 后刷新）。

### 8.3 生成超过 5 分钟被中断

**现象**：生成中途返回「无响应」。

**原因**：走了旧 300s 超时的转发。

**解决**：确认走 `callToPPtPythonPostLong`（900s）；仍超时则考虑后续异步任务化（当前为同步阻塞）。
