# Hotel Management Service

## Описание

**Hotel Management Service** — микросервис, отвечающий за управление отелями и номерами.
Он является частью распределённой системы бронирований, включающей:

* **Booking Service** — сервис обработки бронирований
* **API Gateway** — единая точка входа для всех запросов
* **Eureka Server** — сервис регистрации и обнаружения микросервисов
* **Hotel Management Service** — управление отелями и номерами (данный проект)

Основные функции:

* Создание и получение списка отелей
* Управление номерами (создание, поиск, статистика)
* Алгоритм планирования размещения (автоматический выбор оптимального номера)
* Механизмы блокировки и подтверждения бронирования

---

## Архитектурная схема

```text
                        ┌────────────────────────┐
                        │      API Gateway        │
                        │ (Spring Cloud Gateway)  │
                        └────────────┬────────────┘
                                     │
                                     ▼
                 ┌───────────────────────────────┐
                 │       Booking Service          │
                 │  (управление бронированиями)  │
                 └────────────┬──────────────────┘
                              │ REST-запросы
                              ▼
              ┌──────────────────────────────────────┐
              │     Hotel Management Service          │
              │  (управление отелями и номерами)     │
              │                                      │
              │  ┌───────────────────────────────┐   │
              │  │ RoomService / HotelService    │   │
              │  │ RoomRepository (JPA, PESSIMISTIC_WRITE)│
              │  └───────────────────────────────┘   │
              └──────────────────────────────────────┘
                              │
                              ▼
                      ┌────────────────┐
                      │  PostgreSQL DB │
                      └────────────────┘

Eureka Server обеспечивает регистрацию и обнаружение всех сервисов.
```

---

## Инструкция по запуску

### 1. Требования

* **Java 17+**
* **Maven 3.8+**
* **PostgreSQL 14+**
* **Docker** *(опционально, для быстрой сборки)*

---

### 2. Конфигурация окружения

Создай файл `application.yml` или используй существующий с параметрами:

```yaml
server:
  port: 8082

spring:
  application:
    name: hotel-service
  datasource:
    url: jdbc:postgresql://localhost:5432/hotel_db
    username: postgres
    password: postgres
  jpa:
    hibernate:
      ddl-auto: update
    show-sql: true
    properties:
      hibernate.format_sql: true

eureka:
  client:
    service-url:
      defaultZone: http://localhost:8761/eureka
```

---

### 3. Запуск из консоли

```bash
mvn clean install
mvn spring-boot:run
```

---

### 4. Проверка работы API

Примеры HTTP-запросов:

#### Получить список отелей

```bash
GET http://localhost:8082/api/hotels
Authorization: Bearer <JWT>
```

#### Создать отель

```bash
POST http://localhost:8082/api/hotels
Content-Type: application/json

{
  "name": "Ritz Carlton",
  "address": "Amsterdam"
}
```

#### Получить доступные комнаты

```bash
GET http://localhost:8082/api/rooms
Authorization: Bearer <JWT>
```

#### Аллокация комнаты (планирование)

```bash
GET http://localhost:8082/api/rooms/allocate
Authorization: Bearer <JWT>
```

#### Подтвердить бронирование

```bash
POST http://localhost:8082/api/rooms/{id}/confirm
Authorization: Bearer <JWT>
```

#### Освободить комнату

```bash
POST http://localhost:8082/api/rooms/{id}/release
Authorization: Bearer <JWT>
```

#### Статистика по загрузке номеров

```bash
GET http://localhost:8082/api/rooms/stats/all
Authorization: Bearer <JWT>
```

---

## ADR (Architectural Decision Records)

### ADR-001 — Использование Spring Boot + Spring Cloud

**Решение:**
Для унификации микросервисов используется Spring Boot 3 + Spring Cloud Netflix stack.
Это обеспечивает лёгкую интеграцию с Eureka и API Gateway.

**Альтернатива:**
Micronaut или Quarkus.
**Причина отказа:** выше порог входа, слабее экосистема Spring Security и Data JPA.

---

### ADR-002 — Использование Pessimistic Lock в RoomRepository

**Решение:**
Методы `findAndLockAvailableRooms()` и `findById()` используют `@Lock(LockModeType.PESSIMISTIC_WRITE)` для избежания гонок при выборе номера.

**Причина:**
При конкурентных бронированиях важно гарантировать, что один и тот же номер не будет назначен нескольким пользователям одновременно.

**Альтернатива:**
Optimistic Lock (через версионность), но в данном сценарии это могло бы приводить к ошибкам при высокой нагрузке.

---

### ADR-003 — Алгоритм планирования номеров

**Решение:**
Номера сортируются по количеству бронирований (`timesBooked ASC`), что обеспечивает равномерное распределение нагрузки.

**Преимущества:**

* Простая реализация
* Отсутствие лишних зависимостей
* Предсказуемое поведение

**Альтернатива:**
Использование эвристик (например, предпочтение этажей, ценовых категорий), но это не требуется в базовой версии.

---

### ADR-004 — Разделение сервисов Hotel и Room

**Решение:**
Введены отдельные сущности и сервисы `HotelService` и `RoomService`.
Это повышает модульность и масштабируемость: возможно вынести логику отелей в отдельный микросервис в будущем.

---

### ADR-005 — Безопасность

**Решение:**
Доступ к эндпоинтам защищён через аннотации `@PreAuthorize`.
Роли:

* `USER` — просмотр и бронирование
* `ADMIN` — администрирование и статистика

JWT-токен выдается сервисом Booking Service и проверяется через Gateway.

---

## Структура проекта

```
hotel-management-service/
├── src/main/java/com/daniil/hotelmanagementservice/
│   ├── controller/
│   │   ├── HotelController.java
│   │   └── RoomController.java
│   ├── entity/
│   ├── repository/
│   ├── service/
│   └── HotelManagementServiceApplication.java
├── src/main/resources/
│   └── application.yml
├── pom.xml
└── README.md
```

---
