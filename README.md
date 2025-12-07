# 📸 찍어보카 - Android Client

![Kotlin](https://img.shields.io/badge/Kotlin-1.9.22-purple?logo=kotlin)
![Android SDK](https://img.shields.io/badge/Target%20SDK-36-brightgreen?logo=android)
![Protocol](https://img.shields.io/badge/Protocol-TCP%2FTLS-red)
![Architecture](https://img.shields.io/badge/Architecture-MVVM-blue)

> **OCR 및 AI 챗봇(RAG)을 활용한 자기주도적 영어 학습 플랫폼**
> 안드로이드 16 (API 36) 최신 환경 대응 및 자체 TCP/TLS 소켓 프로토콜 기반 통신

## 📱 프로젝트 소개 (Introduction)
**찍어보카**는 일상 속 단어를 카메라로 촬영(OCR)하여 나만의 단어장을 만들고, **AI 튜터**와 실시간으로 회화하며 학습하는 안드로이드 애플리케이션입니다.
HTTP(Retrofit) 대신 **TLS 기반의 네이티브 소켓 통신**을 사용하여 보안성을 강화했으며, 모든 학습 데이터는 로컬 DB 없이 **서버 API를 통해 중앙에서 통합 관리**됩니다.

## 🌟 주요 기능 (Key Features)

### 1. ⚡ TCP/TLS 소켓 통신 (Secure Socket Communication)
* **Custom Protocol**: `Packet Size(4 bytes) + JSON Payload` 구조의 자체 프로토콜을 설계하여 데이터 송수신을 처리합니다.
* **Persistent Connection**: 서버와 연결을 지속적으로 유지하며, 안정적인 데이터 파이프라인을 구축했습니다.
* **TLS Security**: SSL/TLS 핸드셰이킹을 적용하여 통신 간 모든 데이터를 암호화합니다.

### 2. 🤖 AI 챗봇 (Interactive Chat)
* **Request-Response Model**: 단방향 스트리밍이 아닌, 사용자의 입력(Request)에 대해 AI가 적절한 답변(Response)을 보내는 상호작용 구조를 구현했습니다.
* **Rich Text 지원**: `Markwon`을 통해 AI가 보내주는 마크다운(Code Block, Bold 등)을 깔끔하게 렌더링합니다.
* **Lottie Animation**: 대화 처리 상태에 따라 생동감 있는 애니메이션을 제공하여 UX를 향상시켰습니다.

### 3. 🗓️ 스마트 학습 관리 & 서버 동기화
* **Cloud-Based Data**: RoomDB 등 로컬 저장소를 사용하지 않고, 모든 학습 기록과 단어장 데이터를 **서버 API**로 실시간 저장 및 조회합니다.
* **학습 스트릭(Streak)**: `Material CalendarView`를 통해 서버에 저장된 학습 이력을 받아와 캘린더에 시각화하여 동기를 부여합니다.

## 🏗️ 시스템 아키텍처 (Architecture)

클라이언트는 **MVVM 패턴**을 기반으로 설계되었으며, `SocketManager`를 통해 서버와 **TLS 암호화 소켓 연결**을 맺고 비동기적으로 데이터를 교환합니다.

![System Architecture](images/architecture_diagram.png)

## 🛠️ 기술 스택 (Tech Stack)

| Category | Library | Version | Description |
| --- | --- | --- | --- |
| **Language** | Kotlin | 1.9.22 | Android Native |
| **SDK** | Android SDK | **Target 36** | Latest Android Preview Support |
| **Network** | **Java Socket (NIO)** | **TCP/TLS Custom Protocol** |
| **Async** | Coroutines | 비동기 소켓 I/O 처리 |
| **Calendar** | Material CalendarView | 학습 일정 관리 |
| **Auth** | Kakao SDK | 2.13.0 (Social Login) |
| **Image** | Glide | 4.16.0 (Image Loading) |

## 📂 패키지 구조 (Package Structure)

```text
com.example.language
├── adapter      # RecyclerView Adapters
├── api          # Socket Manager & Protocol Handler (TCP/TLS)
├── data         # Data Layer (Model, Repository - Remote Only)
├── ui           # Presentation Layer (Activities, Fragments, Custom Views)
└── viewmodel    # MVVM ViewModels (State Holders)
