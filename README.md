# 🚀 NoMoreReports - GitHub 커밋 리포트 생성기

GitHub 또는 GitHub Enterprise 저장소에서 특정 작성자의 커밋 메시지를 수집하고, 로컬 LLM을 통해 메시지를 분류한 후, 마크다운 리포트를 생성하는 웹 서비스입니다.

## ✨ 주요 기능

- **GitHub API 연동**: GitHub.com 및 GitHub Enterprise 지원
- **커밋 분류**: 로컬 LLM(Ollama)을 통한 자동 커밋 타입 분류
- **마크다운 리포트**: 날짜별, 타입별로 정리된 리포트 생성
- **Docker 지원**: Docker Compose로 간편한 실행

## 🚀 빠른 시작

### 1. Docker Compose로 실행 (권장)

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

### 2. 로컬 개발 환경

#### 사전 요구사항
- Java 21+
- Gradle 8+
- Ollama (로컬 설치)

#### 실행 방법

```bash
# 1. Ollama 설치 및 모델 다운로드
curl -fsSL https://ollama.ai/install.sh | sh
ollama pull mistral

# 2. Ollama 서버 시작
ollama serve

# 3. Spring Boot 애플리케이션 실행
./gradlew bootRun

# 4. 웹 브라우저에서 접속
open http://localhost:8080
```

## 📋 사용 방법

### 1. 웹 인터페이스 사용

1. 브라우저에서 `http://localhost:8080` 접속
2. 다음 정보 입력:
   - **작성자**: GitHub 사용자명
   - **저장소**: `owner/repo` 형식
   - **시작 날짜**: 필수
   - **종료 날짜**: 선택사항
   - **GitHub 호스트**: GitHub.com 또는 GHE URL
   - **GitHub 토큰**: Private 저장소 접근 시 필요

3. "📊 리포트 생성" 버튼 클릭
4. 생성된 마크다운 리포트 확인

### 2. API 직접 호출

```bash
curl -X POST http://localhost:8080/api/commit-report \
  -H "Content-Type: application/json" \
  -d '{
    "author": "njhyuk",
    "repository": "owner/repo",
    "since": "2024-01-01",
    "until": "2024-01-31",
    "host": "https://api.github.com",
    "token": "your_github_token"
  }'
```

## 🔧 설정

### 환경 변수

| 변수명 | 기본값 | 설명 |
|--------|--------|------|
| `GITHUB_TOKEN` | - | GitHub Personal Access Token |
| `GITHUB_HOST` | `https://api.github.com` | GitHub API 호스트 |
| `OLLAMA_BASE_URL` | `http://localhost:11434` | Ollama 서버 URL |
| `OLLAMA_MODEL` | `mistral` | 사용할 Ollama 모델 |

### application.yml 설정

```yaml
app:
  github:
    default-host: https://api.github.com
    default-token: ${GITHUB_TOKEN:}
  ollama:
    base-url: ${OLLAMA_BASE_URL:http://localhost:11434}
    model: ${OLLAMA_MODEL:mistral}
```

## 📊 커밋 분류

NoMoreReports는 다음 6가지 타입으로 커밋을 자동 분류합니다:

- **✨ FEATURE**: 새로운 기능 추가
- **🐛 FIX**: 버그 수정
- **♻️ REFACTOR**: 코드 리팩토링
- **🧪 TEST**: 테스트 관련
- **📚 DOCS**: 문서 작성/수정
- **🔧 CHORE**: 기타 작업

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

## 🐛 문제 해결

### Ollama 연결 오류

```bash
# Ollama 서버 상태 확인
curl http://localhost:11434/api/tags

# 모델 재다운로드
ollama pull mistral
```

### GitHub API 오류

- GitHub 토큰이 올바른지 확인
- 저장소 접근 권한 확인
- API 호스트 URL 확인 (GHE 사용 시)

### Docker 실행 오류

```bash
# 컨테이너 로그 확인
docker-compose logs app
docker-compose logs ollama

# 컨테이너 재시작
docker-compose down
docker-compose up -d
```

## 🤝 기여하기

1. Fork the repository
2. Create your feature branch (`git checkout -b feature/amazing-feature`)
3. Commit your changes (`git commit -m 'Add some amazing feature'`)
4. Push to the branch (`git push origin feature/amazing-feature`)
5. Open a Pull Request

## 📄 라이선스

이 프로젝트는 MIT 라이선스 하에 배포됩니다. 자세한 내용은 [LICENSE](LICENSE) 파일을 참조하세요.

## 🙏 감사의 말

- [Ollama](https://ollama.ai/) - 로컬 LLM 실행 환경
- [GitHub API](https://docs.github.com/en/rest) - GitHub 데이터 접근
- [Spring Boot](https://spring.io/projects/spring-boot) - 백엔드 프레임워크 