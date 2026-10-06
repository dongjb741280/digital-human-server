# digital-human-server

A digital human platform backend. Built on [yudao-cloud](https://github.com/YunaiV/ruoyi-vue-pro) (Spring Cloud microservices), adding a **digital human business module** `yudao-module-digital` on top of the standard system management / infrastructure modules. It provides avatar cloning, voice cloning, copywriting, video generation, and AI agent interaction.

[简体中文](./README.md) | English

## Demo

<p align="center">
  <a href="https://dongjb741280.github.io/digital-human-server/demo/电信政企2026年报.mp4">
    <img src="https://raw.githubusercontent.com/dongjb741280/digital-human-server/main/site/demo/电信政企2026年报-poster.jpg" width="320" alt="Digital human demo">
  </a>
</p>

> Click the preview above to play the demo video. Website: [https://dongjb741280.github.io/digital-human-server/](https://dongjb741280.github.io/digital-human-server/)

## Core Features

| Feature | Description |
|---|---|
| Avatar Clone | Upload a training video → `rembg` matting → digital human avatar (transparent PNG + green-screen video) |
| Voice Clone | Zero-shot voice cloning based on `GPT-SoVITS` |
| Copywriting | LLM-generated outlines / scripts / PPT (`python-pptx` + `Pillow`) |
| PPT Creation (ppt-master) | One-click native editable `.pptx` (Claude tool-use → SVG → masters/layouts) |
| PPT Online Editing (Collabora) | Edit `.pptx` in the browser via the WOPI protocol (Collabora Online); previews re-rendered and speaker notes re-extracted after edits |
| Video Creation | 7-step pipeline: TTS → segment cropping → lip-sync → layer compositing → merge → first frame → subtitles |
| Background / Asset Management | Video layer asset library (background images / videos / foreground decorations) |
| AI Agent / Real-time Interaction | Agent list, script rehearsal, flow orchestration, real-time digital human interaction (SSE streaming) |

> See [`doc/`](./doc/) for full business flows and deployment details (in Chinese).

## Tech Stack

- **Backend**: JDK 11, Spring Boot 2.7.18, Spring Cloud 2021 (Alibaba), MyBatis-Plus, MapStruct, Lombok
- **Middleware**: MySQL, Redis, RabbitMQ, Nacos (service discovery / config), MinIO (object storage), XXL-Job
- **AI Engines** (Python side): GPT-SoVITS (TTS), Wav2Lip / MuseTalk (lip-sync), rembg (matting), LLM (OpenAI-compatible gateway)

## Module Structure

| Module | Port | Description |
|---|---|---|
| `yudao-gateway` | 48080 | API gateway (Spring Cloud Gateway) |
| `yudao-module-system` | 48081 | System features (users, permissions, tenants, menus) |
| `yudao-module-infra` | 48082 | Infrastructure (files, config, logs, jobs) |
| `yudao-module-digital` | 48083 | **Digital human business module** (project core) |
| `yudao-framework` | - | Framework base, starters (web / security / mybatis / mq / monitor) |
| `yudao-dependencies` | - | Dependency version BOM |

## System Architecture

```
┌────────────┐
│  Web Frontend  │  digital-human-web (separate repo)
└─────┬──────┘
      │ HTTP
┌─────▼──────────────────────────────────────────┐
│  Gateway :48080                                 │
└──┬─────────┬─────────┬─────────┬───────────────┘
   │         │         │         │
   ▼         ▼         ▼         ▼
 system    infra    digital
 :48081    :48082   :48083  ← digital-human business, callback-driven async pipeline
                         │
   ┌─────────────────────┼─────────────────────────┐
   │ Python AI services (separately deployed)      │
   │  digital-human-engine orchestration :60013    │
   │    ├─ GPT-SoVITS api_v2 :9880  (TTS)          │
   │    ├─ Wav2Lip / MuseTalk     (lip-sync)       │
   │    └─ rembg                  (matting)        │
   └─────────────────────┼─────────────────────────┘
                         │
   MySQL :3306    Redis :6379    RabbitMQ :5672
   MinIO :9000 (bucket: aidigital)    Nacos :8848
```

- **Java microservices**: business orchestration, persistence, MinIO upload, and invoking Python services with callbacks to advance state.
- **Python AI services**: the actual AI inference (matting / TTS / lip-sync / LLM copywriting), deployed separately.

## Quick Start

### 1. Prerequisites

Start the following middleware:

| Dependency | Default | Description |
|---|---|---|
| MySQL | `localhost:3306` | Create the `ai_digital_human` database and import [`sql/mysql/ruoyi-vue-pro.sql`](./sql/mysql/ruoyi-vue-pro.sql) |
| Redis | `localhost:6379` | Cache / distributed locks |
| RabbitMQ | `localhost:5672` | Message queue |
| Nacos | `localhost:8848` | Service registration & config |
| MinIO | `localhost:9000` | Object storage, create the `aidigital` bucket |

> Local config lives in `application-local.yaml`; adjust DB / Redis / MinIO connection settings as needed.

### 2. Startup Order

```bash
# 1. System service (users/permissions)  — SystemServerApplication
# 2. Infrastructure service              — InfraServerApplication
# 3. Digital human business service      — DigitalServerApplication
# 4. API gateway                          — GatewayServerApplication
```

Run each service with the `local` profile (`--spring.profiles.active=local`).

### 3. Digital Human AI Dependencies

Avatar / voice cloning, video creation, and copywriting require the **Python AI services** (a sibling project outside this repo):

| Service | Port | Purpose |
|---|---|---|
| `digital-human-engine` orchestration | 60013 | Matting, TTS forwarding, video processing, LLM copywriting; callbacks to Java |
| GPT-SoVITS `api_v2` | 9880 | TTS for voice cloning / video voiceover |
| Wav2Lip / MuseTalk | - | Lip-sync for digital humans |

## Related Docs

- [MinIO Storage Plan](./doc/minio-storage.md)
- [Avatar Clone Sequence](./doc/avatar-clone-sequence.md)
- [Voice Clone Sequence](./doc/voice-clone-sequence.md)
- [Copywriting Sequence](./doc/copywriting-sequence.md)
- [PPT Creation Sequence](./doc/ppt-creation-sequence.md)
- [ppt-master Sequence](./doc/ppt-master-sequence.md)
- [PPT Online Editing Sequence](./doc/ppt-online-editing-sequence.md)
- [Video Creation Sequence](./doc/video-creation-sequence.md)
- [Background Asset Management](./doc/background-asset-management.md)

## Related Projects

- [digital-human-web](https://github.com/dongjb741280/digital-human-web) — digital human platform frontend
- [digital-human-engine](https://github.com/dongjb741280/digital-human-engine) — Python AI orchestration service
- [GPT-SoVITS](https://github.com/RVC-Boss/GPT-SoVITS) / [Wav2Lip](https://github.com/Rudrabha/Wav2Lip) / [MuseTalk](https://github.com/TMElyralab/MuseTalk) — third-party AI engines

## License

Based on yudao-cloud. See [LICENSE](./LICENSE).
