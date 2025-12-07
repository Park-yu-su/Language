# 📸 찍어보카 - Android Client

![Kotlin](https://img.shields.io/badge/Kotlin-1.9.22-purple?logo=kotlin)
![Android SDK](https://img.shields.io/badge/Target%20SDK-36-brightgreen?logo=android)
![Retrofit](https://img.shields.io/badge/Retrofit-3.0.0-square?logo=squareup)
![Architecture](https://img.shields.io/badge/Architecture-MVVM-blue)

> **OCR 및 AI 챗봇(RAG)을 활용한 자기주도적 영어 학습 플랫폼**
> 안드로이드 16 (API 36) 최신 환경 대응 및 SSE 실시간 스트리밍 채팅 지원

## 📱 프로젝트 소개 (Introduction)
**찍어보카**는 일상 속 단어를 카메라로 촬영(OCR)하여 나만의 단어장을 만들고, **AI 튜터**와 실시간으로 회화하며 학습하는 안드로이드 애플리케이션입니다.
최신 Android 기술 스택을 적용하여 **Server-Sent Events(SSE)** 기반의 실시간 채팅, **Material Calendar**를 활용한 학습 관리, **Room DB** 기반의 로컬 캐싱을 구현했습니다.

## 🌟 주요 기능 (Key Features)

### 1. 🤖 AI 챗봇 & 실시간 소통
* **Real-time Streaming**: `OkHttp SSE`를 적용하여 AI의 긴 답변을 기다리지 않고 타자기처럼 실시간으로 확인합니다.
* **인터랙티브 피드백**: `Lottie` 애니메이션을 적용하여 음성 인식 중이거나 로딩 중일 때 생동감 있는 UX를 제공합니다.
* **Rich Text 지원**: `Markwon`을 통해 AI가 보내주는 마크다운(Code Block, Bold 등)을 깔끔하게 렌더링합니다.

### 2. 🗓️ 스마트 학습 관리
* **학습 스트릭(Streak)**: `Material CalendarView`와 `ThreeTenABP`를 통해 매일의 학습 기록을 캘린더에 표시하여 꾸준한 학습을 유도합니다.
* **로컬 캐싱**: `Room DB`를 활용하여 학습 데이터를 로컬에 저장, 오프라인 환경에서도 내 단어장과 기록을 확인할 수 있습니다.

### 3. 📷 OCR 단어장 & UI UX
* **OCR 텍스트 추출**: 카메라로 촬영한 이미지에서 영단어를 인식하여 자동으로 단어장에 등록합니다.
* **소셜 프로필**: `Glide`와 `CircleImageView`를 사용하여 친구들의 프로필 이미지를 빠르고 둥글게 렌더링합니다.
* **반응형 디자인**: `ConstraintLayout` 및 `GridLayout`을 활용하여 다양한 디바이스 해상도에 대응합니다.

## 🏗️ 시스템 아키텍처 (Architecture)

클라이언트(Android)는 **MVVM 패턴**을 기반으로 설계되었으며, 서버와 **TLS 소켓 통신** 및 **REST API**를 병행하여 통신합니다.

![System Architecture](images/architecture_diagram.png)

## 🛠️ 기술 스택 (Tech Stack)

| Category | Library | Version | Description |
| --- | --- | --- | --- |
| **Language** | Kotlin | 1.9.22 | Android Native |
| **SDK** | Android SDK | **Target 36** | Latest Android Preview Support |
| **Network** | Retrofit2 | **3.0.0** | Type-safe HTTP Client |
| **Streaming** | OkHttp SSE | 4.12.0 | Server-Sent Events |
| **Local DB** | Room | 2.6.1 | Local Caching & DAO |
| **Calendar** | Material CalendarView | 2.0.1 | Learning Streak Calendar |
| **Auth** | Kakao SDK | 2.13.0 | Social Login |
| **Image** | Glide | 4.16.0 | Image Loading |

## 📂 패키지 구조 (Package Structure)

```text
com.example.language
├── adapter      # RecyclerView Adapters
├── api          # Retrofit Interfaces & SSE Clients
├── data         # Data Layer (Model/DTO, Repository, Room Entities)
├── ui           # Presentation Layer (Activities, Fragments, Custom Views)
└── viewmodel    # MVVM ViewModels (State Holders)
