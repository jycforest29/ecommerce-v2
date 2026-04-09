# Ecommerce Platform

Java 21 + Spring Boot 3.5 기반의 MSA 이커머스 플랫폼

## 기술 스택

| 분류 | 기술 |
|------|------|
| Language / Framework | Java 21, Spring Boot 3.5.7 |
| Database | MySQL 8.0, Redis 7, Elasticsearch 8.13 |
| Messaging | Apache Kafka (Confluent 7.6.0) |
| Infra | Docker, Docker Compose |
| Auth | JWT (JJWT 0.13.0), Spring Security |
| Docs | Springdoc OpenAPI (Swagger UI) |

## 아키텍처

```
┌─────────────────────────────────────────────────────────┐
│                    Docker Compose                       │
│                                                         │
│  ┌──────────┐  ┌──────────┐  ┌──────────┐              │
│  │  User     │  │  Order   │  │ Payment  │              │
│  │  :9006    │  │  :9002   │  │  :9003   │              │
│  └────┬─────┘  └────┬─────┘  └────┬─────┘              │
│       │              │              │                    │
│       ▼              ▼              ▼                    │
│  ┌─────────────────────────────────────────────────┐    │
│  │                 Apache Kafka                     │    │
│  │              (21 Event Topics)                   │    │
│  └─────────────────────────────────────────────────┘    │
│       ▲              ▲              ▲                    │
│       │              │              │                    │
│  ┌────┴─────┐  ┌────┴─────┐  ┌────┴─────┐              │
│  │ Product   │  │  Coupon  │  │ Notifi-  │              │
│  │  :9004    │  │:9000/:01 │  │ cation   │              │
│  └──────────┘  └──────────┘  │  :9005   │              │
│                               └──────────┘              │
│  ┌──────────┐  ┌──────────┐  ┌──────────┐              │
│  │ Review   │  │ Ranking  │  │ Search   │              │
│  │  :9008   │  │  :9007   │  │  :9009   │              │
│  └──────────┘  └──────────┘  └──────────┘              │
│                                                         │
│  ┌─────────┐  ┌─────────┐  ┌───────────────┐           │
│  │ MySQL   │  │  Redis  │  │ Elasticsearch │           │
│  │  :3307  │  │  :6379  │  │    :9200      │           │
│  └─────────┘  └─────────┘  └───────────────┘           │
└─────────────────────────────────────────────────────────┘
```

## 서비스 구성

| 서비스 | 포트 | 저장소 | 설명 |
|--------|------|--------|------|
| User Service | 9006 | MySQL + Redis | 회원가입/로그인, JWT 인증 |
| Order Service | 9002 | MySQL | 주문 생성/취소, Saga 오케스트레이션 |
| Payment Service | 9003 | MySQL | 결제/환불 처리 (PG Mock) |
| Product Service | 9004 | MySQL | 상품 CRUD, 재고 관리 |
| Coupon API | 9000 | MySQL + Redis | 프로모션/쿠폰 발급 |
| Coupon Worker | 9001 | MySQL + Redis | 쿠폰 적용/롤백 비동기 처리 |
| Notification Service | 9005 | MySQL + Redis | SSE 실시간 알림 |
| Review Service | 9008 | MySQL + Redis | 리뷰 CRUD, 평균 점수 집계 |
| Ranking Service | 9007 | Redis | Sorted Set 기반 실시간 랭킹 |
| Search Service | 9009 | Elasticsearch + Redis | 상품 전문 검색 |

## 핵심 설계

### Transactional Outbox Pattern

비즈니스 로직과 이벤트 저장을 동일 트랜잭션으로 처리하여 메시지 발행의 신뢰성을 보장한다. `EventPoller`가 3초 주기로 미발행 이벤트를 폴링하여 Kafka로 발행하며, 최대 5회 재시도한다.

```
[Service] ──TX──▶ [DB: Business Data + OutboxEvent]
                          │
               EventPoller (3s polling)
                          │
                          ▼
                      [Kafka]
```

### Saga Pattern (주문 플로우)

주문 생성 시 Choreography 기반 분산 트랜잭션을 수행한다. 각 단계 실패 시 보상 트랜잭션이 자동 실행된다.

```
Order ──▶ Coupon ──▶ Product ──▶ Payment
CREATED   COUPON_    STOCK_      PAID
          APPLIED    DEDUCTED

실패 시 보상:
Payment 실패 ──▶ 재고 복원 + 쿠폰 롤백
Stock 실패   ──▶ 쿠폰 롤백
```

### 멱등성 보장

`ProcessedEvent` 테이블로 이벤트 ID 기반 중복 수신을 방지한다. Manual ACK 모드와 결합하여 at-least-once 전달에서도 정확히 한 번 처리를 보장한다.

## Kafka 토픽

| 도메인 | 토픽 |
|--------|------|
| 주문 | `order.events.created`, `order.events.cancelled` |
| 결제 | `payment.events.request`, `payment.events.completed`, `payment.events.failed`, `payment.events.cancelled` |
| 쿠폰 | `coupon.events.apply_request`, `coupon.events.applied`, `coupon.events.rollback_apply` |
| 재고 | `stock.events.deduct_request`, `stock.events.deducted`, `stock.events.restore_request` |
| 회원 | `user.events.joined`, `user.events.withdrew` |
| 리뷰 | `review.events.created`, `review.events.deleted` |
| 알림 | `notification.events.delivery_started`, `notification.events.delivery_completed`, `notification.events.refund_completed`, `notification.events.coupon_expired_soon`, `notification.events.product_out_of_stock`, `notification.events.product_restocked` |

## 프로젝트 구조

```
ecommerce-platform/
├── common/                          # 공통 모듈 (이벤트, 설정, 유틸)
│   └── src/main/java/.../common/
│       ├── config/                  # Security, WebMvc 설정
│       ├── constants/               # Brand, Category, Color 등 enum
│       ├── event/                   # Event, OutboxEvent, ProcessedEvent
│       │   ├── coupon/
│       │   ├── order/
│       │   ├── payment/
│       │   ├── product/
│       │   ├── notification/
│       │   ├── review/
│       │   └── user/
│       └── util/                    # OutboxEventGenerator, EntityFinder
├── service/
│   ├── user-service/
│   ├── order-service/
│   ├── payment-service/
│   ├── product-service/
│   ├── coupon-service/
│   │   ├── coupon-api/
│   │   ├── coupon-worker/
│   │   └── coupon-core/
│   ├── notification-service/
│   ├── review-service/
│   ├── ranking-service/
│   └── search-service/
├── docker/
│   ├── mysql/
│   │   ├── init.sql                 # DB 스키마 초기화
│   │   └── bulk-data.sql            # 시드 데이터
│   └── redis/
│       └── coupon-init.sh           # 쿠폰 재고 초기화
└── docker-compose.yml
```

## 실행 방법

### 전체 서비스 실행

```bash
docker-compose up -d
```

### 개별 서비스 로컬 실행

```bash
# 인프라만 실행
docker-compose up -d mysql redis kafka zookeeper elasticsearch

# 개별 서비스 실행 (local 프로필)
cd service/order-service
mvn spring-boot:run -Dspring-boot.run.profiles=local
```

### 시드 데이터 적재

```bash
docker exec -i ecommerce-mysql mysql -uroot -proot < docker/mysql/bulk-data.sql
```

## API 문서

각 서비스 실행 후 Swagger UI에서 확인 가능:

```
http://localhost:{port}/swagger-ui/index.html
```

| 서비스 | Swagger UI |
|--------|------------|
| User | http://localhost:9006/swagger-ui/index.html |
| Order | http://localhost:9002/swagger-ui/index.html |
| Payment | http://localhost:9003/swagger-ui/index.html |
| Product | http://localhost:9004/swagger-ui/index.html |
| Coupon | http://localhost:9000/swagger-ui/index.html |
| Notification | http://localhost:9005/swagger-ui/index.html |
| Review | http://localhost:9008/swagger-ui/index.html |
| Ranking | http://localhost:9007/swagger-ui/index.html |
| Search | http://localhost:9009/swagger-ui/index.html |