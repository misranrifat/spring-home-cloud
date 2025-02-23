# Spring Home Cloud

A personal cloud storage solution built with Spring Boot that provides secure file storage and management capabilities.

## Features

- Secure file storage and management
- User authentication and authorization using JWT
- Web-based user interface using Thymeleaf
- Database persistence with JPA (supports both H2 and PostgreSQL)

## Technologies

- Java 17
- Spring Boot 3.2.3
- Spring Security
- Spring Data JPA
- JWT
- Thymeleaf
- PostgreSQL / H2 Database

## Prerequisites

- JDK 17 or later
- Maven 3.6+ 
- PostgreSQL or H2

## Getting Started

1. Configure the database:
   - For development: H2 database is configured by default
   - For production: Update `application.yaml` with your PostgreSQL credentials

2. Build the project:
   ```bash
   mvn clean install
   ```

3. Run the application:
   ```bash
   mvn spring-boot:run
   ```

The application will be available at `http://localhost:7777`
