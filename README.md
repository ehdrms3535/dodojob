# 프로젝트 소개

두두잡(DodoJob) 은 사회로 두 번째 진출을 하는 시니어를 위한 일자리 · 교육 · 복지 통합 플랫폼이다.
시니어가 복잡한 절차 없이, 본인 조건에 맞는 일자리와 교육, 복지 서비스를 한 곳에서 탐색하고 신청할 수 있도록 돕는다.

이 프로젝트는 영남대학교 교내 융합경진대회 출품작으로, 시니어들과 기업에 더 나은 서비스를 제공하기 위해 다음과 같은 특징을 가지도록 만들었다.

--------------------

## 시니어 도메인 중심 설계

`senior`, `career_senior`, `license_senior`, `welfare_senior` 등을 통해 시니어의 경력·자격·복지 이력까지 한 번에 관리

## 기업(Employ) 도메인 분리

`employ`, `announcement`, `working_conditions`, `salary_condition`, `preferential_treatment`, `skills_or_experience`, `company_images` 등으로 공고/조건/우대사항/회사 정보를 구조적으로 분리

## 상호작용 데이터의 체계적 관리

`application`, `announcement_senior`, `recent_watch`, `scrappedgreatuser`, `interview_offer`, `suggest_interview`를 통해 “지원–열람–스크랩–면접 제안” 전체 흐름을 추적

## 교육 & 복지 연계

`lecture`, `lecture_weekly`, `lecture_assign_user`, `lecture_favorite`로 교육 콘텐츠 수강 현황을 관리하고, `welfare`, `welfare_senior`로 복지 서비스 참여 상태를 관리

--------------------

앱 레벨에서는 이 데이터 구조를 바탕으로, 시니어/기업/교육/복지 네 가지 축을 하나의 모바일 앱 안에서 자연스럽게 엮는 것을 목표로 하여 개발을 진행하였다.

# 주요 기능
## 시니어 맞춤 일자리 탐색 · 지원

---------------------------

### 공고 정보 구조

`announcement` : 기본 공고(회사명, 근무지 등)

`working_conditions` : 직무 카테고리, 근무형태, 요일·시간, 업무 강도 등

`salary_condition` : 급여 유형/금액, 복리후생, 경력 요구, 성별 제약 등

`preferential_treatment, skills_or_experience, license_announcement` : 우대사항, 필요한 스킬/경험, 자격 관련 조건

### 시니어 행동 데이터

`application` : 실제 지원 이력

`announcement_senior` : 열람/미열람/제안 등 상태(user_status) 관리

`recent_watch` : 최근 본 공고 기록

`scrappedgreatuser` : 기업이 “좋아요”한 시니어 북마크

--------------------------

→ 이를 기반으로, 시니어는 맞춤형 공고 목록, 상세 페이지, 지원 현황을 한 앱에서 확인할 수 있고, 기업은 지원자 상태 관리·면접 제안·관심 시니어 관리를 할 수 있다.

## 기업용 채용 관리 & 면접 제안 플로우

---------------------------

### 기업 계정 및 공고 관리

`employ_tmp` + `announcement` + `그 하위 조건 테이블(working, salary, skills…)`로
공고를 모듈식으로 관리

`company_images`로 사업장/회사 사진을 공고에 매핑

### 지원자/시니어 관리

`announcement_senior` : 공고–시니어 매핑 + 열람/통과 여부 + 좋아요(isliked)

`scrappedgreatuser` : 기업이 관심 있는 시니어를 스크랩/관리

### 면접 제안 & 일정 관리

`suggest_interview` : 공고/유저별 면접 일자, 시간, 방식(대면/비대면), 주소 등 기록

---------------------------------

→ 기업 입장에서는 “공고 등록 → 지원자 유입 → 열람/제안/합격 처리 → 면접 일정 관리“까지 하나의 데이터 흐름으로 관리할 수 있도록 설계하였다.

## 시니어 교육 · 복지 연계 기능

---------------------------

### 교육(에듀) 도메인

`lecture` : 강의 기본 정보(제목, 설명, 카테고리, 영상 URL, 썸네일 URL)

`lecture_weekly` : 주차별 강의/커리큘럼 구조

`lecture_assign_user` : 유저별 수강/구매/즐겨찾기·재생 위치 기록

`lecture_favorite` : 시니어 계정 기준 즐겨찾기 상태

---------------------------

→ 이를 통해, 시니어는 앱에서 교육 강의 목록 → 상세 → 재생 흐름을 경험하고, 재방문 시에는 이어보기(재생 위치), 즐겨찾기, 수강 이력을 기반으로 추천을 받을 수 있다.

---------------------------

### 복지 도메인

`welfare` : 복지 서비스 마스터

`welfare_senior` : 시니어별 복지 참여 상태(welfare_status)

----------------------------

→ 장기적으로는 “일자리–교육–복지”를 하나의 타임라인/대시보드에서 보여주는 시니어의 일상을 관리해주는 것로 확장 가능하다.

## 시니어 프로필 · 경력 · 자격 관리

---------------------------

`users_tmp` : 기본 계정/공통 유저 정보(전화, 이메일, username 등 / senior, employ 통합)

`senior` : 시니어 활동 지표(지원 횟수, 이력서 열람 수, 최근 활동, 교육 완료 여부 등)

`career_senior` : 경력 타임라인(회사명, 직무, 기간, 설명)

`license_senior` : 자격증 이름, 발급 기관, 번호 등

`user_image` : 프로필 이미지 URL

---------------------------

→ 앱에서는 이 데이터를 기반으로 프로필, 경력, 자격증, 사진을 한 번에 보여주는 마이페이지 기업에게 전달되는 이력서/프로필 카드를 생성할 수 있다.

---------------------------

# 기술 스택

## 모바일 앱 (Android)

언어: Kotlin

UI: Jetpack Compose

아키텍처: MVVM + Clean Architecture

data : Repository 구현, Supabase API 연동, DTO/Mapper

비동기/상태 관리: Coroutines, StateFlow

네트워킹: Ktor Client

Supabase REST Endpoint(Schema 기반 /rest/v1/<table> 및 /rest/v1/rpc/<function>) 호출

기타

DI, 로그, 유효성 검증 등은 모듈별로 분리하여 유지보수성 고려

## 백엔드 / 데이터 레이어

### Supabase (PostgreSQL)

RLS + Foreign Key로 역할/도메인별 접근 제어 및 참조 무결성 유지

### 서버리스 구조

별도 커스텀 서버 없이 Supabase Data API + RPC로 모든 비즈니스 로직 처리

필요 시 PostgreSQL 함수로 추천 알고리즘/집계 로직을 DB 서버 측에서 수행
