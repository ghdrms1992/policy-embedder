# 🚀 Policy Embedder: AI 기반 정책 문서 Q&A 시스템

**Policy Embedder**는 RAG(Retrieval-Augmented Generation) 기술을 활용하여, 사용자가 업로드한 정책 문서(PPTX)에 대해 자연어 질의응답을 수행하는 AI 백엔드 시스템입니다.

<br>

## ✨ 주요 기능

- **PPTX 문서 처리**: PowerPoint(`_pptx_`) 파일을 파싱하여 텍스트 정보를 추출합니다.
- **의미 기반 임베딩**: 추출된 텍스트를 OpenAI 임베딩 모델을 통해 의미를 담은 벡터(Vector)로 변환합니다.
- **벡터 저장 및 검색**: 변환된 벡터를 **Neon PostgreSQL** 데이터베이스(**pgvector** 확장)에 저장하고, 사용자 질문과 의미적으로 가장 유사한 내용을 검색합니다.
- **AI 답변 생성**: 검색된 문서 내용을 근거로 OpenAI 챗 모델(GPT)이 신뢰도 높은 답변을 생성합니다.

<br>

## 💻 기술 스택

| 구분 | 기술 |
| :--- | :--- |
| **언어** | Kotlin |
| **프레임워크** | Spring Boot 3, Spring AI |
| **데이터베이스** | **PostgreSQL (on Neon) with pgvector** |
| **AI 모델** | OpenAI (Embedding & Chat) |
| **빌드 도구** | Gradle |
| **컨테이너** | Docker |

<br>

## 🔌 API 엔드포인트

### 1. 정책 문서 업로드

- `POST /api/ingest`
- PPTX 문서를 업로드하여 데이터베이스에 임베딩 및 저장합니다.

| 구분 | 설명 | 예시 |
| :--- | :--- | :--- |
| **URL** | `/api/ingest` | |
| **Method** | `POST` | |
| **Content-Type** | `multipart/form-data` | |
| **Request** | `file` (form-data key) | `policy.pptx` |

**cURL 예시:**
```bash
curl -X POST http://localhost:8080/api/ingest \
  -F "file=@/경로/내/policy.pptx"
```

### 2. 질의응답

- `GET /api/ask`
- 저장된 문서 기반으로 질문에 대한 답변을 요청합니다.

| 구분 | 설명 | 예시 |
| :--- | :--- | :--- |
| **URL** | `/api/ask` | |
| **Method** | `GET` | |
| **Query Param** | `query` (string) | `연차 사용 규정이 어떻게 되나요?` |

**cURL 예시:**
```bash
curl -X GET "http://localhost:8080/api/ask?query=연차+사용+규정이+어떻게+되나요"
```

<br>

## 🚀 시작하기

### 1단계: 사전 요구사항

- **Java 21** (or higher)
- **Neon 계정** 및 PostgreSQL 데이터베이스
- **OpenAI API 키**

### 2단계: 데이터베이스 설정

1.  **Neon 프로젝트 생성**: [Neon](https://neon.tech/)에 로그인하여 새로운 프로젝트와 데이터베이스를 생성합니다.
2.  **pgvector 확장 활성화**: 생성된 데이터베이스의 `Query Editor`에서 다음 쿼리를 실행하여 pgvector를 활성화합니다.
    ```sql
    CREATE EXTENSION IF NOT EXISTS vector;
    ```
3.  **Connection String 확인**: Neon 대시보드에서 `Connection Details` 위젯을 통해 JDBC 연결 정보를 확인합니다.

### 3단계: 환경 변수 설정

애플리케이션은 `application.yml`에 정의된 대로 환경 변수를 통해 주요 설정값을 주입받습니다. 아래 변수들을 실행 환경에 맞게 설정해주세요.

- `OPENAI_API_KEY`: OpenAI에서 발급받은 API 키
- `DATA_SOURCE_PASSWORD`: Neon 데이터베이스 접속 비밀번호

> **참고**: `application.yml`의 `spring.datasource.url`과 `username`은 Neon 프로젝트의 값으로 이미 설정되어 있습니다. 만약 다른 DB를 사용한다면 이 부분도 환경 변수로 관리하는 것을 권장합니다.

### 4단계: 애플리케이션 실행

#### A. 로컬에서 직접 실행

```bash
# 필요한 환경 변수를 먼저 export 합니다.
export OPENAI_API_KEY="sk-..."
export DATA_SOURCE_PASSWORD="your_db_password"

# Gradle Wrapper를 사용하여 애플리케이션을 실행합니다.
./gradlew bootRun
```

#### B. Docker로 실행

```bash
# 1. Gradle로 애플리케이션을 빌드합니다.
./gradlew build

# 2. Docker 이미지를 빌드합니다.
docker build -t policy-embedder .

# 3. 환경 변수와 함께 Docker 컨테이너를 실행합니다.
docker run -p 8080:8080 \
  -e OPENAI_API_KEY="sk-..." \
  -e DATA_SOURCE_PASSWORD="your_db_password" \
  policy-embedder
```