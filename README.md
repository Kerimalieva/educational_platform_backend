# Educational Platform Backend

A secure REST API for an online learning platform built with **Spring Boot**.  
Supports role-based access (**Student / Instructor / Admin**), course management, OTP-based attendance, assignments, quizzes, and real‑time notifications.

## Live Demo

[https://educational-platform-backend-o7c3.onrender.com](https://educational-platform-backend-o7c3.onrender.com)  

## Test Accounts

| Role       | Email               | Password     |
|------------|---------------------|--------------|
| Student    | student@test.com    | Password123! |
| Instructor | instructor@test.com | Password123! |
| Admin      | admin@example.com   | admin123     |

(Passwords are shown as plain text for testing purposes. In production, use strong credentials.)

## Problem

Existing educational platforms lack transparent attendance verification and flexible backend APIs that allow integration with custom frontends. Many platforms are monolithic, not scalable, or force their own frontend.

## Solution

The developed backend provides:

- Role‑based access (Student, Instructor, Admin)
- OTP‑based attendance tracking (prevents fake presence)
- Full assignment and quiz workflow (creation, submission, auto‑grading, feedback)
- Stateless JWT authentication (ready for horizontal scaling)
- REST API – can be consumed by any frontend (React, mobile, etc.)
- Clean layered architecture (Controller → Service → Repository)

## Goal

To deliver a secure, scalable, and well‑documented backend that can power any online learning frontend, while solving real issues like attendance fraud and assignment management.

## Objectives

- Implement secure authentication with Spring Security + JWT
- Build role‑based access control (RBAC)
- Create OTP‑based attendance system
- Develop CRUD for courses, lessons, assignments, quizzes
- Add auto‑grading for quizzes and notification system
- Write unit & integration tests (JUnit, Mockito)
- Containerize with Docker and deploy on Render

## Technology Stack

- Java 21
- Spring Boot 4.0
- Spring Security + JWT (jjwt 0.11.5)
- Spring Data JPA (Hibernate)
- MySQL (deployed on Railway)
- Jakarta Validation
- ModelMapper + custom ConvertHelper
- Lombok
- SLF4J
- JUnit 5 + Mockito
- Swagger / OpenAPI 3
- Maven
- Docker
- Render (deployment)

## Installation and Local Setup

### Prerequisites

- JDK 21
- Maven 3.9+
- MySQL (or use Railway cloud DB)

### Steps

```bash
git clone https://github.com/Kerimalieva/educational_platform_backend
cd educational_platform_backend
# Configure application.properties (database, JWT secret)
mvn clean package
java -jar target/*.jar
