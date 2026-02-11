# Travel Buddy - Setup Instructions

## Prerequisites

- Java 21 (SapMachine JDK или Oracle JDK)
- PostgreSQL 12+ database
- Google Places API Key
- Maven (включен чрез wrapper)

## 1. Database Setup

### Инсталация на PostgreSQL
1. Изтегли и инсталирай PostgreSQL от https://www.postgresql.org/download/
2. По време на инсталацията запомни паролата за `postgres` потребителя

### Създаване на база данни
```sql
-- Влез в PostgreSQL като postgres потребител
psql -U postgres

-- Създай базата данни
CREATE DATABASE travel_buddy_db;

-- Провери че е създадена
\l

-- Излез
\q
```

## 2. Configuration

### application.properties
Конфигурацията е в `src/main/resources/application.properties`

**ВАЖНО:** Вече има `application.properties.template` файл с примерна конфигурация!

#### Стъпки за конфигурация:

1. **Копирай template файла:**
```powershell
cp src/main/resources/application.properties.template src/main/resources/application.properties
```

2. **Редактирай `application.properties` и попълни:**

**Database Password:**
```properties
spring.datasource.password=ТВОЯТА_POSTGRESQL_ПАРОЛА
```

**JWT Secret** (генерирай нов):
```powershell
# PowerShell команда за генериране:
$bytes = New-Object byte[] 64
[Security.Cryptography.RandomNumberGenerator]::Create().GetBytes($bytes)
[Convert]::ToBase64String($bytes)
```

После добави в application.properties:
```properties
jwt.secret=ГЕНЕРИРАНИЯТ_BASE64_STRING
jwt.expiration=86400000
```

**Google Places API Key:**
```properties
google.places.api-key=ТВОЯТ_GOOGLE_API_KEY
```

### Получаване на Google Places API Key

1. Отиди на Google Cloud Console: https://console.cloud.google.com/
2. Създай нов проект или избери съществуващ
3. Активирай **Places API** и **Geocoding API**:
   - APIs & Services → Library
   - Търси "Places API" → Enable
   - Търси "Geocoding API" → Enable
4. Създай Credentials:
   - APIs & Services → Credentials
   - Create Credentials → API Key
   - Копирай API key и постави в `application.yml`

**Важно:** За development можеш да оставиш API key без restrictions, но за production задай ограничения.

## 3. Стартиране на приложението

### Вариант 1: Maven (Препоръчително)
```powershell
# Стартиране (компилира автоматично)
mvn spring-boot:run
```

### Вариант 2: JAR файл
```powershell
# Build
mvn clean package -DskipTests

# Run
java -jar target/travel-buddy-0.0.1-SNAPSHOT.jar
```

### Вариант 3: Със wrapper (ако нямаш глобален Maven)
```bash
# Windows
.\mvnw.cmd spring-boot:run

# Unix/Linux/Mac
./mvnw spring-boot:run
```

**Приложението ще стартира на:** `http://localhost:8081`

## 4. Тестване на API endpoints

### Регистрация на потребител
```bash
curl -X POST http://localhost:8081/api/auth/register \
  -H "Content-Type: application/json" \
  -d '{
    "username": "testuser",
    "email": "test@example.com",
    "password": "password123",
    "firstName": "Test",
    "lastName": "User"
  }'
```

### Login (получаване на JWT token)
```bash
curl -X POST http://localhost:8081/api/auth/login \
  -H "Content-Type: application/json" \
  -d '{
    "username": "testuser",
    "password": "password123"
  }'
```

Отговор:
```json
{
  "token": "eyJhbGciOiJIUzI1NiJ9...",
  "username": "testuser"
}
```

### Използване на token за защитени endpoints
```bash
# Копирай токена от login отговора
TOKEN="eyJhbGciOiJIUzI1NiJ9..."

# Взимане на профил
curl http://localhost:8081/api/users/me \
  -H "Authorization: Bearer $TOKEN"

# Търсене на места
curl "http://localhost:8081/api/places/search?query=restaurant&latitude=42.6977&longitude=23.3219" \
  -H "Authorization: Bearer $TOKEN"

# Създаване на трип
curl -X POST http://localhost:8081/api/trips \
  -H "Authorization: Bearer $TOKEN" \
  -H "Content-Type: application/json" \
  -d '{
    "name": "Sofia Weekend",
    "placeSearchQueries": ["National Palace of Culture", "Vitosha Mountain"],
    "plannedStartTime": "2026-02-15T10:00:00",
    "plannedEndTime": "2026-02-15T18:00:00",
    "startLatitude": 42.6977,
    "startLongitude": 23.3219
  }'

# Reality Check validation
curl -X POST http://localhost:8081/api/validation/trips/1 \
  -H "Authorization: Bearer $TOKEN"
```

## 5. API Endpoints Overview

### Authentication (Public)
- `POST /api/auth/register` - Регистрация
- `POST /api/auth/login` - Login

### User Profile (Protected)
- `GET /api/users/me` - Профил
- `PATCH /api/users/me` - Актуализация на профил
- `GET /api/users/me/trips` - Всички трипове
- `GET /api/users/me/trips/status/{status}` - Филтрирани трипове
- `GET /api/users/me/stats` - Статистика

### Places (Protected)
- `GET /api/places/search` - Търсене на места
- `POST /api/places` - Създаване на място
- `GET /api/places/{id}` - Детайли за място
- `GET /api/places` - Всички места
- `GET /api/places/nearby` - Места наблизо
- `GET /api/places/type/{type}` - По тип

### Trips (Protected)
- `POST /api/trips` - Създаване на трип
- `GET /api/trips/{id}` - Детайли
- `GET /api/trips` - Всички трипове
- `GET /api/trips/upcoming` - Предстоящи
- `POST /api/trips/{id}/places/{placeId}` - Добавяне на място
- `DELETE /api/trips/{id}/places/{placeId}` - Премахване на място
- `PUT /api/trips/{id}/status` - Промяна на статус
- `POST /api/trips/{id}/refresh-weather` - Обновяване на прогноза

### Validation (Protected)
- `POST /api/validation/trips/{id}` - Reality Check validation

### Favorite Places (Protected)
- `POST /api/favorites/{placeId}` - Добавяне в любими
- `DELETE /api/favorites/{placeId}` - Премахване
- `GET /api/favorites` - Всички любими
- `GET /api/favorites/{placeId}/check` - Проверка
- `GET /api/favorites/count` - Брой

## 6. Troubleshooting

### "Connection refused" при стартиране
- Провери че PostgreSQL е стартиран и работи
- Провери че порт 8081 е свободен (приложението работи на 8081)

### "Invalid API key" за Google Places
- Провери че Places API е enabled в Google Cloud Console
- Провери че API key е копиран правилно

### "JWT signature does not match"
- Генерирай нов JWT secret и рестартирай приложението
- Изтрий старите JWT tokens и направи нов login

### Database connection failed
- Провери username/password в `application.properties`
- Провери че базата `travel_buddy_db` съществува
- Провери PostgreSQL порт (default: 5432)
- Уверете се че PostgreSQL service е running

## 7. Production Recommendations

1. **Security**:
   - Промени JWT secret със силен криптографски ключ
   - Използвай environment variables вместо hardcoded стойности
   - Добави rate limiting
   - Рестриктирай Google API key по IP/domain

2. **Database**:
   - Промени `ddl-auto: update` на `validate` в production
   - Използвай migration tools (Flyway/Liquibase)
   - Настрой connection pool

3. **Logging**:
   - Преминава на INFO/WARN level за production
   - Използвай centralized logging

4. **Monitoring**:
   - Добави Spring Boot Actuator
   - Настрой health checks
   - Мониторинг на API rate limits

## Tech Stack Summary
- **Backend**: Spring Boot 4.0.2
- **Java**: 21 (SapMachine JDK)
- **Database**: PostgreSQL
- **Security**: Spring Security + JWT
- **External APIs**: Google Places API, Open-Meteo Weather API
- **Build Tool**: Maven
