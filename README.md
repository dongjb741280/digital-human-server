# digital-human-server

[English](./README_EN.md) | 简体中文

[![License](https://img.shields.io/github/license/dongjb741280/digital-human-server)](./LICENSE)
![Java](https://img.shields.io/badge/Java-11-007396)
![Spring Boot](https://img.shields.io/badge/Spring%20Boot-2.7.18-6DB33F)

> 前端仓库：[digital-human-web](https://github.com/dongjb741280/digital-human-web) ｜ AI 引擎：[digital-human-engine](https://github.com/dongjb741280/digital-human-engine)

数字人平台服务端。基于 [yudao-cloud](https://github.com/YunaiV/ruoyi-vue-pro)（Spring Cloud 微服务架构）改造，在标准的系统管理 / 基础设施模块之上，新增 **数字人业务模块** `yudao-module-digital`，提供数字人形象复刻、声音复刻、文案创作、视频创作、AI 智能体互动等能力。

## 效果演示

[![数字人视频演示](https://raw.githubusercontent.com/dongjb741280/digital-human-server/main/site/demo/digital-human-demo-poster.jpg)](https://dongjb741280.github.io/digital-human-server/demo/digital-human-demo.mp4)

> 点击上方预览图直接播放演示视频。在线官网：[https://dongjb741280.github.io/digital-human-server/](https://dongjb741280.github.io/digital-human-server/)

## 核心功能

| 功能 | 说明 |
|---|---|
| 形象复刻（Avatar Clone） | 上传训练视频 → `rembg` 抠图 / 抠像 → 数字人形象（透明 PNG + 绿幕视频） |
| 声音复刻（Voice Clone） | 基于 `GPT-SoVITS` 的 zero-shot 零样本声音克隆 |
| 文案创作（Copywriting） | 基于 LLM 生成提纲 / 正文 / PPT（`python-pptx` + `Pillow` 出图） |
| PPT 创作（ppt-master） | 一键直出原生可编辑 `.pptx`（Claude tool-use → SVG → 母版/版式） |
| PPT 在线编辑（Collabora） | 基于 WOPI 协议接入 Collabora Online，浏览器里直接编辑 `.pptx`，编辑后重渲染预览图 + 重新提取 speaker notes |
| 视频创作（Video） | 7 步流水线：TTS → 裁剪分段 → 口型合成 → 图层合成 → 合并 → 首帧 → 字幕 |
| 背景 / 素材管理 | 视频图层素材库（背景图 / 背景视频 / 前景装饰） |
| AI 智能体 / 实时互动 | 智能体列表、话术对练、流程编排、数字人实时互动（SSE 流式） |

> 完整的业务流程与部署细节见 [`doc/`](./doc/) 目录下的时序文档。

## 技术栈

- **后端**：JDK 11、Spring Boot 2.7.18、Spring Cloud 2021（Alibaba）、MyBatis-Plus、MapStruct、Lombok
- **中间件**：MySQL、Redis、RabbitMQ、Nacos（服务发现/配置中心）、MinIO（对象存储）、XXL-Job
- **AI 引擎**（Python 侧）：GPT-SoVITS（TTS）、Wav2Lip / MuseTalk（口型合成）、rembg（抠像）、LLM（OpenAI 兼容网关）

## 模块结构

| 模块 | 端口 | 说明 |
|---|---|---|
| `yudao-gateway` | 48080 | API 网关，基于 Spring Cloud Gateway |
| `yudao-module-system` | 48081 | 系统功能（用户、权限、租户、菜单等） |
| `yudao-module-infra` | 48082 | 基础设施（文件、配置、日志、Job 等） |
| `yudao-module-digital` | 48083 | **数字人业务模块**（本项目核心） |
| `yudao-framework` | - | 框架基础，各 starter（web / security / mybatis / mq / monitor 等） |
| `yudao-dependencies` | - | 依赖版本 BOM |

## 系统架构

```
┌────────────┐
│  Web 前端   │  digital-human-web（独立仓库）
└─────┬──────┘
      │ HTTP
┌─────▼──────────────────────────────────────────┐
│  Gateway 网关 :48080                             │
└──┬─────────┬─────────┬─────────┬───────────────┘
   │         │         │         │
   ▼         ▼         ▼         ▼
 system    infra    digital
 :48081    :48082   :48083  ← 数字人业务，回调驱动异步流水线
                         │
   ┌─────────────────────┼─────────────────────────┐
   │ Python AI 服务（独立部署，本仓库外）            │
   │  digital-human-engine 编排服务 :60013          │
   │    ├─ GPT-SoVITS api_v2 :9880  （TTS 合成）     │
   │    ├─ Wav2Lip / MuseTalk     （口型合成）       │
   │    └─ rembg                  （抠像）          │
   └─────────────────────┼─────────────────────────┘
                         │
   MySQL :3306    Redis :6379    RabbitMQ :5672
   MinIO :9000 (bucket: aidigital)    Nacos :8848
```

- **Java 微服务**：负责业务编排、落库、MinIO 上传，以及调用 Python 服务并接收回调推进状态。
- **Python AI 服务**：承载真正的 AI 推理（抠像 / 语音合成 / 口型合成 / LLM 文案），是独立部署的兄弟项目。

## 快速开始

### 1. 前置依赖

确保以下中间件已启动：

| 依赖 | 默认地址 | 说明 |
|---|---|---|
| MySQL | `localhost:3306` | 初始化 `ai_digital_human` 库，导入 [`sql/mysql/ruoyi-vue-pro.sql`](./sql/mysql/ruoyi-vue-pro.sql) |
| Redis | `localhost:6379` | 缓存 / 分布式锁 |
| RabbitMQ | `localhost:5672` | 消息队列 |
| Nacos | `localhost:8848` | 服务注册与配置中心 |
| MinIO | `localhost:9000` | 对象存储，需创建 `aidigital` bucket |

> 本地配置在 `application-local.yaml`，按需修改数据库 / Redis / MinIO 等连接信息。

### 2. 启动顺序

```bash
# 1. 系统服务（用户/权限）
#    启动类：SystemServerApplication
# 2. 基础设施服务
#    启动类：InfraServerApplication
# 3. 数字人业务服务
#    启动类：DigitalServerApplication
# 4. API 网关
#    启动类：GatewayServerApplication
```

各服务使用 `local` profile 启动（`--spring.profiles.active=local`）。

### 3. 数字人 AI 能力依赖

形象复刻 / 声音复刻 / 视频创作 / 文案创作需要 **Python AI 服务**配合（本仓库外的兄弟项目）：

| 服务 | 端口 | 用途 |
|---|---|---|
| `digital-human-engine` 编排服务 | 60013 | 抠像、TTS 转发、视频处理、LLM 文案，回调 Java |
| GPT-SoVITS `api_v2` | 9880 | 声音复刻 / 视频配音的 TTS 推理 |
| Wav2Lip / MuseTalk | - | 数字人口型合成 |

部署细节见 [`doc/声音复刻完整时序.md`](./doc/声音复刻完整时序.md)、[`doc/形象复刻完整时序.md`](./doc/形象复刻完整时序.md)。

## 相关文档

- [MinIO 存储规划](./doc/MinIO%20存储规划.md)
- [形象复刻完整时序](./doc/形象复刻完整时序.md)
- [声音复刻完整时序](./doc/声音复刻完整时序.md)
- [文案创作完整时序](./doc/文案创作完整时序.md)
- [PPT 制作完整时序](./doc/PPT制作完整时序.md)
- [ppt-master 完整时序](./doc/ppt-master完整时序.md)
- [PPT 在线编辑完整时序](./doc/PPT在线编辑完整时序.md)
- [视频创作完整时序](./doc/视频创作完整时序.md)
- [背景素材管理](./doc/背景素材管理.md)

## 相关项目

- [digital-human-web](https://github.com/dongjb741280/digital-human-web)：数字人平台前端
- [digital-human-engine](https://github.com/dongjb741280/digital-human-engine)：Python AI 编排服务
- [GPT-SoVITS](https://github.com/RVC-Boss/GPT-SoVITS) / [Wav2Lip](https://github.com/Rudrabha/Wav2Lip) / [MuseTalk](https://github.com/TMElyralab/MuseTalk)：第三方 AI 引擎

## 开源协议

本项目基于 yudao-cloud 改造，开源协议见 [LICENSE](./LICENSE)。
