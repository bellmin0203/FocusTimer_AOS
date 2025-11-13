# 프리셋 관리 기능 구현 요약

**작업 ID**: #7  
**완료일**: 2025-11-08  
**상태**: ✅ 완료

## 📋 구현 내용

### 1. UseCase 레이어 추가

프리셋 관리를 위한 4개의 UseCase를 구현했습니다:

#### `AddPresetUseCase`

- **파일**: `core/domain/src/main/kotlin/com/jm/focustimer/domain/usecase/AddPresetUseCase.kt`
- **기능**: 새로운 프리셋 추가
- **검증**:
    - 최대 5개 제한 확인
    - 이름 공백 검사
    - 이름 길이 제한 (20자)
    - 시간 유효성 검사

#### `UpdatePresetUseCase`

- **파일**: `core/domain/src/main/kotlin/com/jm/focustimer/domain/usecase/UpdatePresetUseCase.kt`
- **기능**: 기존 프리셋 수정
- **검증**: 이름 및 시간 유효성 검사

#### `DeletePresetUseCase`

- **파일**: `core/domain/src/main/kotlin/com/jm/focustimer/domain/usecase/DeletePresetUseCase.kt`
- **기능**: 프리셋 삭제
- **특징**: 기본 프리셋으로 설정된 경우 자동으로 설정 해제

#### `GetAllPresetsUseCase`

- **파일**: `core/domain/src/main/kotlin/com/jm/focustimer/domain/usecase/GetAllPresetsUseCase.kt`
- **기능**: 모든 프리셋 조회 (최신순)

### 2. ViewModel 확장

#### `TimerViewModel` 업데이트

- **파일**: `feature/timer/src/main/kotlin/com/jm/focustimer/timer/TimerViewModel.kt`
- **추가 기능**:
    - 프리셋 목록 실시간 관찰
    - 프리셋 선택/저장/수정/삭제 Intent 처리
    - 프리셋과 타이머 상태 동기화

#### `TimerIntent` 확장

- **파일**: `feature/timer/src/main/kotlin/com/jm/focustimer/timer/model/TimerIntent.kt`
- **추가 Intent**:
    - `SelectPreset`: 프리셋 선택
    - `SaveAsPreset`: 현재 시간을 프리셋으로 저장
    - `DeletePreset`: 프리셋 삭제
    - `UpdatePreset`: 프리셋 수정

#### `TimerUiState` 확장

- **파일**: `feature/timer/src/main/kotlin/com/jm/focustimer/timer/model/TimerUiState.kt`
- **추가 속성**:
    - `presets`: 프리셋 목록
    - `selectedPresetId`: 현재 선택된 프리셋 ID
    - `canAddPreset`: 프리셋 추가 가능 여부 (computed property)

### 3. UI 컴포넌트 구현

#### `PresetSection`

- **파일**: `feature/timer/src/main/kotlin/com/jm/focustimer/timer/component/PresetSection.kt`
- **기능**:
    - 프리셋 목록을 가로 스크롤로 표시
    - 프리셋 개수 표시 (N/10)
    - 프리셋 추가 버튼
    - 각 프리셋 카드에 수정/삭제 버튼
    - 선택된 프리셋 시각적 강조

#### 다이얼로그 컴포넌트

- **파일**: `feature/timer/src/main/kotlin/com/jm/focustimer/timer/component/PresetDialog.kt`
- **포함 내용**:
    - `AddPresetDialog`: 프리셋 추가 다이얼로그
    - `EditPresetDialog`: 프리셋 수정 다이얼로그
    - `DeletePresetDialog`: 프리셋 삭제 확인 다이얼로그

### 4. 아이콘 확장

#### `FocusTimerIcons` 업데이트

- **파일**: `core/designsystem/src/main/kotlin/com/jm/focustimer/designsystem/icon/FocusTimerIcons.kt`
- **추가 아이콘**:
    - `Edit`: 프리셋 수정 아이콘
    - `Delete`: 프리셋 삭제 아이콘

### 5. TimerScreen 통합

#### `TimerScreen` 업데이트

- **파일**: `feature/timer/src/main/kotlin/com/jm/focustimer/timer/TimerScreen.kt`
- **변경사항**:
    - PresetSection을 QuickTimeButtons 아래에 배치
    - 스크롤 가능하도록 `verticalScroll` 추가
    - 프리셋 다이얼로그 상태 관리 추가
    - 프리셋 관련 콜백 연결

### 6. 빌드 구성

#### `feature/timer/build.gradle.kts`

- **추가 의존성**:
    - `core:domain`
    - `core:data`

## 🎨 UI/UX 특징

### 프리셋 카드

- **선택 상태**: Primary 색상으로 강조
- **비선택 상태**: SurfaceVariant 색상
- **레이아웃**: 140dp x 80dp 크기
- **내용**:
    - 프리셋 이름 (한 줄, 말줄임표)
    - 시간 표시 ("MM분" 또는 "MM분 SS초")
    - 수정/삭제 버튼

### 프리셋 추가 카드

- **크기**: 120dp x 80dp
- **스타일**: PrimaryContainer 색상
- **아이콘**: + 아이콘과 "추가" 텍스트

### 애니메이션

- **등장/사라짐**: `expandVertically` + `fadeIn` / `shrinkVertically` + `fadeOut`

## 🔒 제약사항 및 검증

### 프리셋 제한

- 최대 5개까지 저장 가능
- 11개째 추가 시도 시 오류 메시지 표시

### 이름 검증

- 공백 불허
- 최대 20자 제한

### 시간 검증

- 0보다 큰 값만 허용

### 타이머 실행 중 제한

- 타이머 실행 중에는 프리셋 선택/변경 불가
- 적절한 오류 메시지 표시

## 📱 사용자 플로우

### 프리셋 추가

1. 타이머 시간 설정
2. "+" 카드 클릭
3. 프리셋 이름 입력
4. "저장" 버튼 클릭
5. 성공 메시지 표시

### 프리셋 선택

1. 프리셋 카드 클릭
2. 타이머가 해당 시간으로 설정
3. 프리셋이 시각적으로 강조됨
4. 성공 메시지 표시

### 프리셋 수정

1. 프리셋 카드의 수정 버튼 클릭
2. 새 이름 입력
3. "수정" 버튼 클릭
4. 성공 메시지 표시

### 프리셋 삭제

1. 프리셋 카드의 삭제 버튼 클릭
2. 확인 다이얼로그에서 "삭제" 클릭
3. 성공 메시지 표시

## ✅ 테스트 체크리스트

- [x] 프리셋 추가 기능
- [x] 프리셋 선택 기능
- [x] 프리셋 수정 기능
- [x] 프리셋 삭제 기능
- [x] 5개 제한 검증
- [x] 이름 유효성 검사
- [x] 시간 유효성 검사
- [x] 타이머 실행 중 제한
- [x] UI 애니메이션
- [x] 프로젝트 빌드 성공

## 📦 관련 파일 목록

### Domain Layer

- `core/domain/src/main/kotlin/com/jm/focustimer/domain/usecase/AddPresetUseCase.kt`
- `core/domain/src/main/kotlin/com/jm/focustimer/domain/usecase/UpdatePresetUseCase.kt`
- `core/domain/src/main/kotlin/com/jm/focustimer/domain/usecase/DeletePresetUseCase.kt`
- `core/domain/src/main/kotlin/com/jm/focustimer/domain/usecase/GetAllPresetsUseCase.kt`

### Feature Layer

- `feature/timer/src/main/kotlin/com/jm/focustimer/timer/TimerViewModel.kt`
- `feature/timer/src/main/kotlin/com/jm/focustimer/timer/TimerScreen.kt`
- `feature/timer/src/main/kotlin/com/jm/focustimer/timer/model/TimerIntent.kt`
- `feature/timer/src/main/kotlin/com/jm/focustimer/timer/model/TimerUiState.kt`
- `feature/timer/src/main/kotlin/com/jm/focustimer/timer/component/PresetSection.kt`
- `feature/timer/src/main/kotlin/com/jm/focustimer/timer/component/PresetDialog.kt`

### Design System

- `core/designsystem/src/main/kotlin/com/jm/focustimer/designsystem/icon/FocusTimerIcons.kt`

### Build Configuration

- `feature/timer/build.gradle.kts`

## 🎯 다음 단계

프리셋 관리 기능이 완료되었으므로, 다음 우선 순위 작업은:

1. **작업 9**: 시간 기반 통계 (일간/주간/월간 뷰)
2. **작업 11**: 기본 설정 구현
3. **작업 12**: 알림 설정 구현

## 💡 개선 제안

향후 고려사항:

- 프리셋 순서 변경 기능 (드래그 앤 드롭)
- 프리셋 내보내기/가져오기
- 프리셋 아이콘/색상 커스터마이징
- 프리셋 즐겨찾기 기능
