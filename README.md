# Task Manager

[![Actions Status](https://github.com/SerKonstantin/java-project-99/actions/workflows/hexlet-check.yml/badge.svg)](https://github.com/SerKonstantin/java-project-99/actions)
[![Actions Status](https://github.com/SerKonstantin/java-project-99/actions/workflows/build.yml/badge.svg)](https://github.com/SerKonstantin/java-project-99/actions)
[![Maintainability](https://api.codeclimate.com/v1/badges/0a8a2e9161f3a0a09887/maintainability)](https://codeclimate.com/github/SerKonstantin/java-project-99/maintainability)
[![Test Coverage](https://api.codeclimate.com/v1/badges/0a8a2e9161f3a0a09887/test_coverage)](https://codeclimate.com/github/SerKonstantin/java-project-99/test_coverage)

Task manager is a small web application that allows you to create tasks, assign executors and change task statuses. Users have to be authenticated to work with the app . Based on Spring Boot framework, created for educational purposes.

Deployed on render.com: [https://task-manager-1thk.onrender.com](https://task-manager-1thk.onrender.com)  
Username: hexlet@example.com  
Password: qwerty  

Swagger documentation available on render.com: [https://task-manager-1thk.onrender.com/swagger-ui/index.html](https://task-manager-1thk.onrender.com/swagger-ui/index.html)

Caution: free version of web service on render.com works really slowly, so response time might take up to 120 seconds!
Potential expire date: June 24, 2024

## Technologies used
- Spring Boot (including Web, Data JPA, Security)
- Lombok, MapStruct
- PostgreSQL, H2
- JUnit5, JaCoCo
- Docker
- Postman
- Sentry

## To run server locally
```shell
make install
```

then

```shell
make start
# Open http://localhost:8080
# Username: hexlet@example.com
# Password: qwerty
```



