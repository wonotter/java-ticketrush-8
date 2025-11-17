# Ticket Rush

> 선착순 이벤트/티켓팅 API 서버를 구축하기 위해 단계적으로 학습하며 솔루션을 적용해보는 오픈 미션 프로젝트입니다.

## 프로젝트 설정 방법

> Docker를 사용하여 MySQL 데이터베이스를 연결하기 위한 설정 방법을 기술하였습니다.

1. Docker 공식 홈페이지나 구글 검색을 통해 Docker Desktop을 설치한다.
    - [Docker 공식 홈페이지](https://www.docker.com/)
    - [Junesker 티스토리: Docker 설치 방법](https://junesker.tistory.com/94)

2. 아래 형식과 같이 `application-local.yml`, `env` 파일을 작성한다.
    - 데이터베이스 이름, 비밀번호, 포트 번호는 2개 파일 모두 동일하게 작성해야 한다.

`application-local.yml` 파일

```yaml
local-db:
  mysql:
    host: localhost
    port: ${사용할 포트 번호} # 3306, 3307 등
    name: ${사용할 데이터베이스 이름} # ticketrush, mydb 등
    password: ${사용할 데이터베이스 비밀번호} # 12345678, ticketpassword 등
```

`.env` 파일

```text
MYSQL_DATABASE=${사용할 데이터베이스 이름} # ticketrush, mydb 등
MYSQL_ROOT_PASSWORD=${사용할 데이터베이스 비밀번호} # 12345678, ticketpassword 등 
MYSQL_PORT=${사용할 포트 번호} # 3306, 3307 등
```

3. `docker-compose.yml` 파일이 있는 경로(프로젝트 최상단 경로)에서
   아래 명령을 실행하여 컨테이너를 생성한다.
    - 파일에 적힌 명령어의 자세한 내용이 궁금하다면 Docker Hub에 있는
      [MySQL 공식 이미지 홈페이지](https://hub.docker.com/_/mysql)를 참고한다.

```text
docker-compose up -d
```

## 기능 요구 사항

100명에게만 선착순으로 이벤트(쿠폰, 티켓 등)를 제공하는 API 서버를 구축한다.

- 문제 상황을 재현하기 위해 쿠폰 또는 티켓과 같이 **동시성 문제가 발생하는** 도메인을 간단하게 설계한다.
- 이벤트를 신청, 재고 확인, 재고 차감, 신청 내역 저장 등 이벤트를 반영할 수 있는 간단한 API를 설계한다.
- 동시성을 제어할 수 있는 여러 기법을 학습하고 적용해보기 위해, **단계별 적용 가이드**를 참고하여 순서대로 구현한다.
- 동시성 문제를 해결하였음을 검증할 수 있는 기법(테스트 코드, 모니터링, 또는 기타 도구)을 학습하여 적용한다.
- Git의 커밋 단위는 **단계별 적용 가이드**에 있는 각 단계를 참고한 다음, 기능 목록을 별도로 작성하여 이에 맞춰 추가한다.

## 단계별 적용 가이드

### Level 0

- 문제 상황을 가정하기 위해 티켓을 관리하는 `Ticket`과, 티켓을 구매한 사용자를 나타내는`TicketOrder` 도메인을 설계한다.
    - 예제를 단순화 하기 위해 사용자를 나타내는 `User`나 `Member` 엔티티를 별도로 구성하지 않고,
      `TicketOrder`에 `userId` 필드를 가지고 있는 것으로 구성한다.
    - `UNIQUE Constraint`를 설정하여 중복 구매를 방지한다.
- 도메인에 설계 후 실제 데이터베이스(MySQL, PostgreSQL 등)를 연결한다.
- 도메인에 맞는 적절한 연산(데이터 삽입, 조회 등)을 구성하기 위해 MVC 패턴을 적용한 REST API를 설계한다.
- 설계한 도메인과 API가 정상적으로 잘 동작하는 지 POSTMAN으로 테스트한다.
- `AtomicInteger`, `ExecutorService`, `CountDonwLatch` 클래스를 사용하여 여러 스레드를 생성하고
  동시에 티켓을 구매하는 테스트 코드를 작성한다.

### Level 1

- `synchronized`를 학습하고 동시성 문제를 해결한다.
- `ReentrantLock`를 학습하고 동시성 문제를 해결한다.

### Level 2

- `비관적 락(Pessimistic Lock)`을 학습하고 동시성 문제를 해결한다.
- `낙관적 락(Optimistic Lock)`을 학습하고 동시성 문제를 해결한다.

### Level 3

- `Redisson 분산 락`을 학습하고 동시성 문제를 해결한다.

## 프로그래밍 요구사항

- `Java Style Guide`를 원칙으로 하여 자바 코드 컨벤션을 지키면서 프로그래밍한다.
- **동시성 제어**라는 고난도 기술을 처음 공부해보고 접하는 단계이므로, LLM 이나 AI를 사용하여 참고할 수 있다.
    - 프로젝트 구현을 위해 처음부터 끝까지 물어보는 방식이 아닌, 이론적으로 학습해야할 방향성에 대한 질문 허용
    - 이론적으로 학습한 제어 기법을 어떻게 적용해야 하는지 예시 코드 요청 허용

## 기능 목록

- [ ] 문제 상황에 적합한 도메인을 설계한다. (`Ticket`, `TicketOrder` 등)
- [ ] 설계한 도메인의 데이터를 저장할 수 있는 MySQL 데이터베이스를 연결한다.
- [ ] 데이터 삽입, 조회와 같은 연산이 가능한 API를 설계한다.
- [ ] `POSTMAN`을 통해 설계한 API가 정상적으로 동작하는지, 데이터가 데이터베이스에 정상적으로 저장되는 지 확인한다.
- [ ] 동시성 문제를 보여줄 수 있는 테스트 코드를 작성한다.
- [ ] `synchronized`를 사용하여 동시성 문제를 해결한다.
- [ ] `ReentrantLock`을 사용하여 동시성 문제를 해결한다.
- [ ] `비관적 락(Pessimistic Lock)`을 사용하여 동시성 문제를 해결한다.
- [ ] `낙관적 락(Optimistic Lock)`을 사용하여 동시성 문제를 해결한다.
