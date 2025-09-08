# Language Android README

## 커밋 타입 태그
- `[UI]` : XML 및 Drawable 관련 작업  
- `[Logic]` : 기능 구현  
- `[Add]` : 기능 추가  
- `[Delete]` : 파일/코드 삭제  
- `[Fix]` : 버그 수정  
- `[Rename]` : 이름 변경(변수, 파일, 클래스 등)  
- `[Merge]` : Merge 작업  
- `[Backend]` : 백엔드 API/데이터 연결

- ## Branch 전략
모든 작업은 `main` 브랜치에서 파생된 기능별 하위 브랜치에서 진행합니다.  
Branch 이름은 Issue 생성 후, `이름/Issue 번호` 를 이용해 생성합니다.

```text
main
 ├─ [팀원1/#1/]
 ├─ [팀원2/#2/]
 └─ [팀원3/#3/]
```

- **브랜치명**: `팀원명/#이슈번호/`

---

## Issue 컨벤션
- **제목 형식**: `[팀원명/타입] 구현 내용`  
- **예시**: `[어헛차//UI] 메인 화면 UI 구현`

---

## PR 컨벤션
- **제목 형식**: `[팀원명/#이슈번호] - 작업 내용`  
- **예시**: `[어헛차/#1] - 아이템 RecyclerView 연결`

---

## Commit 컨벤션
- **메시지 형식**: `[팀원명/#이슈번호] - 작업 내용`  
- **예시**: `[어헛차/#1] - RecyclerView 어댑터 구현`

---

## Code 컨벤션

### 1. 위젯 ID 네이밍
```
[Activity/Fragment]_[기능]_[위젯이름]
예) MainActivity 뒤로가기 버튼 → main_back_btn
```

### 2. UI 주석
- XML 내 주요 레이아웃/위젯에 한 줄 설명
```xml
<!-- 상단 앱바 -->

 xml 작성...

<!-- 중앙 fragment -->

 xml 작성...

<!-- 하단 bottomNavigationView-->

 xml 작성...

```

### 3. 패키지 구조
```
com.hwarok
├─ data        # Data classes, 모델
├─ api         # 네트워크/API
├─ ui          # Activity, Fragment, Adapter
└─ adapter     # 어댑터
```

### 4. Activity & Fragment 네이밍
- **CamelCase** 사용  
  - 예) `WriteVocActivity`, `DiaryHistoryFragment`

---

## 개발 환경

- **Android Studio 버전**  
  `Android Studio Narwhal | 2025.1.1`

- **SDK 설정**  
  - `targetSdkVersion`: 36  
  - `minSdkVersion`: 24

- **테스트 환경**  
  다양한 기기에서의 원활한 테스트를 위해 **Emulator** 및 **실제 디바이스**를 함께 사용하여 검증을 진행합니다.

  ---
