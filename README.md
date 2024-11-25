# Система Управления Задачами

## Описание проекта

Проект "Система Управления Задачами" представляет собой веб-приложение, которое позволяет пользователям создавать, редактировать, удалять и просматривать задачи. Каждая задача имеет заголовок, описание, статус, приоритет, автора и исполнителя.

## Функциональные возможности

- **Создание задач**: Пользователи могут создавать новые задачи, указывая необходимые данные.
- **Редактирование задач**: Пользователи могут редактировать существующие задачи.
- **Удаление задач**: Пользователи могут удалять ненужные задачи.
- **Просмотр задач**: Пользователи могут просматривать список задач и детально изучить каждую задачу.

## Стек технологий

- **Backend**:
  - Java
  - Maven
  - Spring Boot
  - Spring Web
  - Spring Data
  - Spring JPA
  - Spring Security
  - JWT
  - Spring Stream API
  - SQL
  - PostgreSQL
  - Docker Compose
  
- **Инструменты**:
  - GIT
  - REST
  - Swagger
  - Postman
  - 
## Условия
- Убедитесь, что у вас установлены следующие программные компоненты:
  - [Docker](https://www.docker.com/products/docker-desktop/)

## Начало работы

Эти инструкции помогут вам запустить копию проекта на вашей локальной машине для целей разработки и тестирования.

### Установка

1. Клонируйте репозиторий проекта:
https://github.com/Fatima38278857/Task_Management

**После запуска приложения, оно будет доступно по адресу**
 [http://localhost:8080](http://localhost:8080).

## Быстрый набор запросов в Postman

### Регистрация пользователя
`POST - http://localhost:8080/authenticate/register`
{
  "login": "testUser1",
  "password": "password1",
  "confirmPassword": "password1"
}
### Аутентификация пользователя
`POST - http://localhost:8080/authenticate/create`
{
  "login": "testUser1",
  "password": "password1"
}
### Создание учетных данных
`POST - http://localhost:8080/users/createProfile`
{
  "email":"user@example.com",
  "role":"AUTHOR"
}

### Создание задачи
`POST - http://localhost:8080/task/add?userId=1`
{   "title": "",
    "description": "",
    "status": "",
    "taskPriority": ""
    }
### Назначение исполнителя
`POST - http://localhost:8080/task/assign-task`
{
  "taskId": 1,
  "userId": 2
}
### Создать коментарии
`POST - http://localhost:8080/task/1/comments`
{
  "text": "Это комментарий к задаче.",
  "userEmail": "Fatima@example.com"
}
### Получить задачи конкретного автора а также все комментарии к ним
`GET - http://localhost:8080/task/user/1`

## Конфигурация
Настройки базы данных и другие конфигурации можно изменить в файле `application.properties`.

## Основной разработчик
Аксагова Фатима
