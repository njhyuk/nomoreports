# 🚀 NoMoreReports - GitHub 커밋 리포트 생성기

GitHub 또는 GitHub Enterprise 저장소에서 특정 작성자의 커밋 메시지를 수집하고, 로컬 LLM을 통해 메시지를 분류한 후, 마크다운 리포트를 생성하는 웹 서비스입니다.

## ✨ 주요 기능

- **GitHub API 연동**: GitHub.com 및 GitHub Enterprise 지원
- **커밋 분류**: 로컬 LLM(Ollama)을 통한 자동 커밋 타입 분류
- **마크다운 리포트**: 날짜별, 타입별로 정리된 리포트 생성
- **Docker 지원**: Docker Compose로 간편한 실행

## 🚀 빠른 시작

### Docker Compose로 실행 (권장)

```bash
# 저장소 클론
git clone https://github.com/njhyuk/nomoreports
cd nomoreports

# 환경 변수 설정 (선택사항)
export GITHUB_TOKEN=your_github_token
export GITHUB_HOST=https://github.company.com  # GitHub Enterprise 사용 시

# Docker Compose로 실행
docker-compose up -d

# 웹 브라우저에서 접속
open http://localhost:8080
```

## 📄 리포트 예시

```markdown
# NoMoreReports - 커밋 리포트

## 📊 요약 정보

- **저장소**: `owner/repo`
- **작성자**: `njhyuk`
- **기간**: `2024-01-01` ~ `2024-01-31`
- **총 커밋 수**: `15`

## 📈 타입별 통계

- ✨ **기능 추가**: 5개 (33.3%)
- 🐛 **버그 수정**: 3개 (20.0%)
- ♻️ **리팩토링**: 4개 (26.7%)
- 🧪 **테스트**: 2개 (13.3%)
- 📚 **문서**: 1개 (6.7%)
- 🔧 **기타**: 0개 (0.0%)

## 📅 날짜별 커밋

### 2024-01-15 (3개)

#### ✨ 기능 추가

- **14:30** `a1b2c3d` 새로운 사용자 인증 기능 추가
- **16:45** `e4f5g6h` API 엔드포인트 구현

#### 🐛 버그 수정

- **18:20** `i7j8k9l` 로그인 오류 수정
```

## 🏗️ 기술 스택

- **Backend**: Kotlin + Spring Boot 3.5.3
- **Frontend**: HTML5 + CSS3 + JavaScript (Vanilla)
- **LLM**: Ollama (mistral, zephyr, phi 등)
- **API**: GitHub REST API v3
- **Container**: Docker + Docker Compose

## 📄 라이선스

MIT