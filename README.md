# Vert.x Daemon Process Monitor

간단한 Vert.x 기반 데몬 프로세스 모니터링 애플리케이션입니다.

## 프로젝트 구조

```
.
├── pom.xml                          # Maven 설정 파일
├── src/
│   ├── main/java/com/vertx/monitor/ # 메인 소스코드
│   └── test/java/com/vertx/monitor/ # 테스트 코드
└── README.md                        # 프로젝트 문서
```

## 의존성

- **Vert.x Core 4.5.3** - 핵심 프레임워크
- **Vert.x Web 4.5.3** - HTTP 서버 및 라우팅
- **Vert.x Micrometer Metrics 4.5.3** - 메트릭 수집
- **Micrometer Prometheus 1.12.3** - Prometheus 메트릭 내보내기
- **Logback 1.4.12** - 로깅

## 빌드

```bash
mvn clean compile
```

## 패키징

```bash
mvn clean package
```

## 실행

```bash
java -jar target/daemon-monitor-1.0.0.jar
```

## 개발 환경

- Java 11 이상
- Maven 3.6 이상

## 실행 및 테스트 (빠른 확인)

- 빌드 (테스트 건너뛰기):

```bash
mvn -DskipTests package
```

- 포그라운드 실행:

```bash
java -jar target/daemon-monitor-1.0.0.jar
```

- 백그라운드 실행(로그 파일 생성):

```bash
nohup java -jar target/daemon-monitor-1.0.0.jar > daemon.log 2>&1 & echo $!
```

- 로그 확인:

```bash
tail -n 200 daemon.log
```

- Prometheus 메트릭 확인:

```bash
curl -sS http://localhost:8080/metrics | sed -n '1,120p'
```

- 브라우저에서 로드 테스트 HTML 열기:

```bash
# 기본 경로로 접속
open http://localhost:8080/
# 또는 테스트 페이지로 직접 접속
# http://localhost:8080/loadtest
# http://localhost:8080/loadtest.html
# http://localhost:8080/index.html
```

- 프로세스 종료(예시):

```bash
# PID로 종료
kill <PID>
# 또는 이름으로 종료
pkill -f daemon-monitor-1.0.0.jar
```

### 참고

- 애플리케이션은 기본적으로 `8080` 포트를 사용합니다.
- `/metrics` 엔드포인트는 Prometheus 텍스트 포맷으로 메트릭을 제공합니다.
