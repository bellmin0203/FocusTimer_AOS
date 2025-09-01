# Git Commit Message Convention
> 개발자들이 협업 시 일관된 메시지 형식으로 커밋 내역을 관리하는 규칙입니다. <br>
> 명확하고 체계적인 커밋 메시지는 프로젝트의 히스토리를 쉽게 파악하고, <br>
> 버그를 추적하며, 자동화된 도구와의 연동을 용이하게 합니다.
___

### 전체 Git Commit Message 포맷
```
<type>(<scope>): <subject>
<body>
<footer>
```
> feat(user-auth): add login feature<br>
> \- 사용자 로그인 시나리오 구현을 위한 로그인 모듈 추가
___

#### \<type\> : 커밋이 어떤 목적으로 이루어졌는지 명시합니다.

Type|설명
:---|---
feat| 새로운 기능 개발
modify| 개발 중 잘못된 코드 수정
fix| 버그 수정
docs|	문서(README, Wiki 등) 수정
style|	코드 포맷팅, 세미콜론 등 코드 변경 없는 스타일 수정
refactor|	기능 변경 없는 코드 리팩토링
test|	테스트 코드 추가 및 수정
chore|	빌드, 패키지 매니저 등 코드와 무관한 기타 변경 사항
wip|	작업 중인(work-in-progress) 커밋으로, 임시 저장용
temp|	임시로 추가하거나 삭제한 파일/코드 (ex. 디버깅용 로그)
hotfix|	긴급 버그 수정 (production 환경에 영향을 주는 치명적 문제
___

#### \<scope\> : 해당 커밋이 영향을 미치는 범위를 명시합니다.
- 예시: ```feat(user-auth): Add login feature```
___

#### \<subject\> : 커밋 내용을 간결하게 요약합니다. 다음 규칙을 따릅니다.
- 명령형으로 작성: 'Added'가 아닌 'Add', 'Fixed'가 아닌 'Fix'
- 50자 이내로 작성
- 첫 글자는 대문자로 시작
- 마침표(.)는 사용하지 않음
___

#### \<body\>: 상세 내용 (Detail)
변경 사항의 **'이유'** 와 **'의도'** 를 상세하게 설명합니다.<br>
**'왜 이 변경이 필요한가(원인)'** 와 **'어떻게 해결했는가(과정)'** 를 명확히 합니다.

- Why: 무엇을 고쳤는지, 어떤 기능을 추가했는지.
- How: 어떻게 해결했는지, 어떤 문제를 해결했는지.
___

#### \<footer\>: 관련 정보 (Information)
- Issue Tracking: 관련 이슈 번호(DVT #0014657, CVT #BUG_0014657)를 명시합니다.


