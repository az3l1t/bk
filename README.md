📚 Book Catalog Service
Это RESTful-сервис для управления каталогом книг. Поддерживает работу с PostgreSQL, Redis, кэширование, JWT-аутентификацию, Swagger-документацию и страницу на Thymeleaf.

🚀 Функционал
Получение списка книг с пагинацией и фильтрами

Создание, редактирование и удаление книг (для роли ADMIN)

Кэширование данных в Redis

Swagger UI для тестирования API

Thymeleaf-страница по адресу /books

Модульные тесты

⚙️ Настройка
1. application.yaml
   Перед запуском обязательно настройте файл application.yaml:
```dockerfile
spring:
  datasource:
    url: jdbc:postgresql://localhost:5432/bookdb # ← при необходимости изменить
    username: postgres                            # ← при необходимости изменить
    password: password                            # ← при необходимости изменить
    driver-class-name: org.postgresql.Driver
  jpa:
    hibernate:
      ddl-auto: none
    properties:
      hibernate:
        dialect: org.hibernate.dialect.PostgreSQLDialect
  data:
    redis:
      host: localhost
      port: 6379
  liquibase:
    change-log: classpath:db/changelog/db.changelog-master.yaml

jwt:
  secret: ${key:<ВАШ_256_БИТНЫЙ_СЕКРЕТНЫЙ_КЛЮЧ>}  # ← обязательно замените!
  expiration: 86400000                    # 24 часа

cache:
  redis:
    ttl: 60
  pool:
    core-size: 4
    max-size: 10
    queue-capacity: 100
    thread-name-prefix: CacheThread-

server:
  port: 8080
```

🔒 Пример генерации 256-битного ключа (в hex):

```bash
openssl rand -hex 64
```

2. Swagger UI
   Swagger доступен по адресу:

bash
Копировать
Редактировать
```dockerfile
http://localhost:8080/swagger-ui/index.html
```
Позволяет тестировать все публичные и защищённые (с JWT) эндпоинты.

3. Thymeleaf страница
   Веб-страница отображения книг доступна по адресу:
```dockerfile
http://localhost:8080/books
```

🐳 Docker
Приложение можно запустить через Docker Compose:
```dockerfile
docker-compose up --build
```
Сервисы:

app — Spring Boot приложение

postgres — база данных PostgreSQL

redis — in-memory хранилище для кэша

Убедитесь, что порты 5432, 6379, 8080 свободны перед запуском.


🧪 Тестирование API
Можно использовать Swagger или Postman. Для защищённых эндпоинтов используйте JWT-токен с ролью ADMIN.