Стояла задача - создать приложение «Системы Управления Банковскими Картами» с требованиями, которые описаны много ниже в ТЗ.

Где-то на 95% весь функционал и требования реализованы. Перечислять не буду. Всё можно посмотреть в работе программы.

Немного пояснений:

* для практики чистого кода использовал возможности 21 Джавы с использованием var, также исключил импорты с звездочкой
* пароли в файле .env пришлось запушить в проект для возможности запуска приложения

Пример запуска и проверка работы программы:

1. Прописываем docker-compose up --build тем самым поднимаем свою программу в контейнере:

![Запустили приложение в контейнере](https://github.com/user-attachments/assets/b832ef2c-368e-42fc-9bed-360aee4184bc)

Образы на месте:
![Образы](https://github.com/user-attachments/assets/29b87500-0f6f-4c6d-aeb5-fff5987531d3)

Сохранение БД при перезапусках Docker тже на месте:
![Volume](https://github.com/user-attachments/assets/c525171c-42a6-47ba-b939-84655a59bed8)

2. При запуске приложения у нас с помощью миграции создаётся профиль с логином admin, пароль password123, роль ROLE_ADMIN (в бд).

3. Тестируем весь функционал в Swagger или Postman.

![swagger](https://github.com/user-attachments/assets/50a1b1b3-5c3f-498f-829d-1555bd563b5d)

4. Регистрируем нового пользователя, по дефолту он у нас всегда ROLE_USER, с некоторыми обязательными полями. post http://localhost:8080/auth/register

 Тело:
{
    "username": "user1",
    "password": "password1",
    "email": "user1@mail.com"
}

5. Затем заходим за созданного ранее админа проверяем функционал post http://localhost:8080/auth/login

 Тело:
{
    "username": "admin",
    "password": "password123"
}

Получаем токен который, используем в Authorization (Bearer Token) в Postman при последующих запросах, например при создании новых двух пользователей. 

6. Два новых пользователя за admin. post http://localhost:8080/admin/users

Тело:
{
  "username": "user2",
  "password": "userpass2",
  "email": "user2@example.com",
  "role": {
    "id": 1
  }
}

Тело:
{
  "username": "user3",
  "password": "userpass3",
  "email": "user3@example.com",
  "role": {
    "id": 1
  }
}

7. За admin создаем 3 карты для нашего первого пользователя, которого мы сами регистрировали. post http://localhost:8080/admin/cards

Тело:
{
  "cardNumber": "1234567812345678",
  "owner": {
    "id": 2
  },
  "expiryDate": "2028-10-15",
  "status": "ACTIVE",
  "balance": 1000.00
}

Тело:
{
  "cardNumber": "5555666677778888",
  "owner": {
    "id": 2
  },
  "expiryDate": "2029-03-01",
  "status": "ACTIVE",
  "balance": 2500.50
}

Тело:
{
  "cardNumber": "9999000011112222",
  "owner": {
    "id": 2
  },
  "expiryDate": "2030-07-20",
  "status": "ACTIVE",
  "balance": 0.00
}

8. Далее пробуем все admin операции по очереди в AdminCardController.

get метод по адресу возвращает все карты: http://localhost:8080/admin/cards
get метод по адресу ищет определенную карту: http://localhost:8080/admin/cards/1

Обращаем внимание, что в ответе у нас возвращается maskedNumber с видимыми последними 4 цифрами.

![Ответ по карте с маской](https://github.com/user-attachments/assets/335b4287-6cc1-443e-a74a-632b5863b927)

patch метод по адресу обновляет данные по определенной карте: http://localhost:8080/admin/cards/3

Тело:
{
  "cardNumber": "9999000011112222",
  "owner": {
    "id": 2
  },
  "expiryDate": "9999-07-20",
  "status": "ACTIVE",
  "balance": 0.00
}

patch метод по адресу блокирует определенную карту: http://localhost:8080/admin/cards/3/block
patch метод по адресу активирует определенную карту: http://localhost:8080/admin/cards/3/activate
delete метод по адресу удаляет определенную карту: http://localhost:8080/admin/cards/3

![БД после тестирования по AdminCardController](https://github.com/user-attachments/assets/72724863-59f1-4c7c-8192-9f05afeedfb0)

9. Далее пробуем все admin операции по очереди в AdminUserController.

get метод по адресу возвращает всех юзеров: http://localhost:8080/admin/users
get метод по адресу возвращает определенного юзера: http://localhost:8080/admin/users/4
patch метод по адресу возвращает определенного юзера: http://localhost:8080/admin/users/4

Тело:
{
  "username": "user3",
  "password": "userpass3",
  "email": "user3@example.com",
  "role": {
    "id": 1
  }
}

delete метод по адресу удаляет определенного юзера: http://localhost:8080/admin/users/4

![БД после тестирования по AdminUserController](https://github.com/user-attachments/assets/e9cbf683-6a3c-4a36-953a-e276b152151d)

10. Тестируем последний UserCardController для этого логинимся за user1. post http://localhost:8080/auth/login

Тело:
{
    "username": "user1",
    "password": "password1"
}

get метод по адресу возвращает все карты авторизованного юзера: http://localhost:8080/user/cards
get метод по адресу возвращает баланс определенной карты авторизованного юзера: http://localhost:8080/user/cards/1/balance
post метод по адресу переводит средства с собственной карты на другую свою карту авторизованного юзера: http://localhost:8080/user/cards/transfer?fromCardId=2&toCardId=1&amount=1500.5
post метод по адресу делает запрос на блокировку карты авторизованного юзера: http://localhost:8080/user/cards/2/requestCardBlock

![БД после тестирования по UserCardController](https://github.com/user-attachments/assets/8dbbcd32-a17f-4a30-b77e-ae42ca9e5615)


Весь функционал приложения протестирован!

Ниже ТЗ приложения


<h1>🚀 Разработка Системы Управления Банковскими Картами</h1>

<h2>📁 Стартовая структура</h2>
  <p>
    Проектная структура с директориями и описательными файлами (<code>README Controller.md</code>, <code>README Service.md</code> и т.д.) уже подготовлена.<br />
    Все реализации нужно добавлять <strong>в соответствующие директории</strong>.
  </p>
  <p>
    После завершения разработки <strong>временные README-файлы нужно удалить</strong>, чтобы они не попадали в итоговую сборку.
  </p>
  
<h2>📝 Описание задачи</h2>
  <p>Разработать backend-приложение на Java (Spring Boot) для управления банковскими картами:</p>
  <ul>
    <li>Создание и управление картами</li>
    <li>Просмотр карт</li>
    <li>Переводы между своими картами</li>
  </ul>

<h2>💳 Атрибуты карты</h2>
  <ul>
    <li>Номер карты (зашифрован, отображается маской: <code>**** **** **** 1234</code>)</li>
    <li>Владелец</li>
    <li>Срок действия</li>
    <li>Статус: Активна, Заблокирована, Истек срок</li>
    <li>Баланс</li>
  </ul>

<h2>🧾 Требования</h2>

<h3>✅ Аутентификация и авторизация</h3>
  <ul>
    <li>Spring Security + JWT</li>
    <li>Роли: <code>ADMIN</code> и <code>USER</code></li>
  </ul>

<h3>✅ Возможности</h3>
<strong>Администратор:</strong>
  <ul>
    <li>Создаёт, блокирует, активирует, удаляет карты</li>
    <li>Управляет пользователями</li>
    <li>Видит все карты</li>
  </ul>

<strong>Пользователь:</strong>
  <ul>
    <li>Просматривает свои карты (поиск + пагинация)</li>
    <li>Запрашивает блокировку карты</li>
    <li>Делает переводы между своими картами</li>
    <li>Смотрит баланс</li>
  </ul>

<h3>✅ API</h3>
  <ul>
    <li>CRUD для карт</li>
    <li>Переводы между своими картами</li>
    <li>Фильтрация и постраничная выдача</li>
    <li>Валидация и сообщения об ошибках</li>
  </ul>

<h3>✅ Безопасность</h3>
  <ul>
    <li>Шифрование данных</li>
    <li>Ролевой доступ</li>
    <li>Маскирование номеров карт</li>
  </ul>

<h3>✅ Работа с БД</h3>
  <ul>
    <li>PostgreSQL или MySQL</li>
    <li>Миграции через Liquibase (<code>src/main/resources/db/migration</code>)</li>
  </ul>

<h3>✅ Документация</h3>
  <ul>
    <li>Swagger UI / OpenAPI — <code>docs/openapi.yaml</code></li>
    <li><code>README.md</code> с инструкцией запуска</li>
  </ul>

<h3>✅ Развёртывание и тестирование</h3>
  <ul>
    <li>Docker Compose для dev-среды</li>
    <li>Liquibase миграции</li>
    <li>Юнит-тесты ключевой бизнес-логики</li>
  </ul>

<h2>📊 Оценка</h2>
  <ul>
    <li>Соответствие требованиям</li>
    <li>Чистота архитектуры и кода</li>
    <li>Безопасность</li>
    <li>Обработка ошибок</li>
    <li>Покрытие тестами</li>
    <li>ООП и уровни абстракции</li>
  </ul>

<h2>💡 Технологии</h2>
  <p>
    Java 17+, Spring Boot, Spring Security, Spring Data JPA, PostgreSQL/MySQL, Liquibase, Docker, JWT, Swagger (OpenAPI)
  </p>

<h2> 📤 Формат сдачи</h2>
<p>
Весь код и изменения принимаются только через git-репозиторий с открытым доступом к проекту. Отправка файлов в любом виде не принимается.
  </p>
