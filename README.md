# 🎲 FindBoardGame (FBG)

<p align="center">
  <img src="https://img.shields.io/badge/Spring_Boot-3.5.7-6DB33F?logo=spring-boot&logoColor=white" />
  <img src="https://img.shields.io/badge/MySQL-8.0-4479A1?logo=mysql&logoColor=white" />
  <img src="https://img.shields.io/badge/Java-17-ED8B00?logo=openjdk&logoColor=white" />
  <img src="https://img.shields.io/badge/Thymeleaf-3.1-005F0F?logo=thymeleaf&logoColor=white" />
</p>

---

### 🔍 프로젝트 소개
**FindBoardGame (FBG)**은 보드게임을 사랑하는 사람들을 위한 **커뮤니티 및 정보 공유 플랫폼**입니다. 
다양한 보드게임 정보를 검색하고, 자신만의 후기와 공략을 공유하며 실시간으로 소통하세요!

---

## 📂 프로젝트 구조
```bash
  📦 FindBoardGame (Root)
   ┣ 📂 src/main/java       # ⚙️ Backend Logic (Spring Boot + JPA)
   ┣ 📂 src/main/resources  # 🎨 Templates (Thymeleaf) & Static Assets
   ┣ 📜 build.gradle        # 🛠 Build Configuration
   ┣ 📜 schema.sql          # 🗄️ Database Schema
   ┗ 📜 README.md           # 📖 가이드 문서
```

---

## 🛠 Tech Stack

| 구분 | 기술 스택 |
| :--- | :--- |
| **Backend** | `Spring Boot 3.5.7`, `Java 17`, `JPA`, `QueryDSL` |
| **Frontend** | `Thymeleaf`, `Vanilla JS`, `CSS3` |
| **Database** | `MySQL 8.0` |
| **Security** | `Spring Security` |
| **Build Tool** | `Gradle` |

---

## ⚙️ 환경 설정 (Configuration)

### 1️⃣ Database 세팅
* **MySQL**을 사용하며, `fbg` 데이터베이스가 필요합니다.
```sql
CREATE DATABASE fbg;
```
* 루트 디렉토리의 `schema.sql`을 실행하여 필요한 테이블 스키마를 생성하세요.

### 2️⃣ Backend 설정
`src/main/resources/application.properties` 수정:
* **DB 계정**: `spring.datasource.username` / `password` 본인 계정으로 수정
* **포트 설정**: 현재 기본 포트는 `8070`으로 설정되어 있습니다.
* **업로드 경로**: `file.upload-dir` 항목을 본인의 환경에 맞게 수정하세요.

---

## 🚀 실행 방법 (How to Run)

### 🟢 Application 실행
```bash
./gradlew bootRun
```
> 서버 접속 주소: `http://localhost:8070`

---

## ⚠️ 주의사항
* **포트 확인**: 브라우저에서 `localhost:8070`으로 접속해야 합니다.
* **파일 업로드**: 이미지를 업로드하려면 `application.properties`에 정의된 경로(`D:/uploads/` 등)가 실제로 존재해야 합니다.

---

## 🎮 주요 기능 (Key Features)

*   **실시간 보드게임 랭킹**: 실시간으로 인기 있는 보드게임을 홈 화면에서 확인
*   **통합 검색 시스템**: 게임 제목, 태그 등을 활용한 정교한 보드게임 검색
*   **커뮤니티 (게시판)**: 자유게시판, 후기 게시판 운영 및 좋아요/싫어요 기능
*   **실시간 알림**: 내가 쓴 글에 댓글이 달리거나 언급될 경우 즉시 알림 발송
*   **회원 관리**: Spring Security 기반의 안전한 회원가입, 로그인 및 마이페이지 제공
*   **이미지 업로드**: 게시글 작성 시 다중 이미지 업로드 및 관리

---