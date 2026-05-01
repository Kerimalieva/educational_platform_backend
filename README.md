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


## Problem

Existing educational platforms in Kyrgyzstan (GeekTech, Codify, Bilim) lack flexible backend APIs and transparent attendance verification. Most platforms are monolithic, not scalable, or force their own frontend.

## Solution

The developed backend provides:

- Role‑based access (Student, Instructor, Admin)
- OTP‑based attendance tracking (prevents fake presence)
- Full assignment and quiz workflow (creation, submission, auto‑grading, feedback)
- Stateless JWT authentication (ready for horizontal scaling)
- Pure REST API – can be consumed by any frontend (React, mobile, etc.)
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

| Category       | Technology / Tool                      |
|----------------|-----------------------------------------|
| Language       | Java 21                                 |
| Framework      | Spring Boot 4.0                         |
| Security       | Spring Security + JWT (jjwt 0.11.5)     |
| Database       | MySQL (deployed on Railway)             |
| ORM            | Hibernate / Spring Data JPA             |
| Validation     | Jakarta Validation                      |
| Mapping        | ModelMapper + custom ConvertHelper      |
| Logging        | SLF4J with Lombok                       |
| Testing        | JUnit 5 + Mockito                       |
| API Docs       | Swagger / OpenAPI 3                     |
| Build Tool     | Maven                                   |
| Container      | Docker                                  |
| Deployment     | Render                                  |

## Installation and Local Setup

### Prerequisites

- JDK 21
- Maven 3.9+
- MySQL (or use Railway cloud DB)

### Steps

```bash
git clone https://github.com/Kerimalieva/educational_platform_backend
cd educational_platform_backend
# Configure application.properties if needed (or use environment variables)
mvn clean package
java -jar target/*.jar


Or run directly with Maven:

bash
mvn spring-boot:run
Features
Student
Register and log in

Browse all courses (with pagination)

Enroll / unenroll in courses

View lessons, submit assignments (text or file)

Take quizzes (multiple attempts prevented)

See grades and feedback instantly

Receive notifications (enrollment, new assignment, graded quiz)

Instructor
Create, update, delete own courses

Create, update, delete lessons (with automatic OTP generation)

Regenerate OTP for any lesson

Create, delete assignments and quizzes

Add questions to course question bank (reusable)

Grade assignments, add feedback

View attendance list per lesson

View all student submissions for assignments

View all quiz grades

Administrator
View all users (students, instructors, admins)

Delete any user (except self – prevents accidental lockout)

View all courses (full list without pagination)

Preview full course content (lessons, assignments, quizzes, question bank)

Delete any course, lesson, assignment, or quiz

View platform statistics (total users, courses, lessons, assignments, quizzes)

General
Stateless JWT authentication – ready for clustering

Global exception handling with consistent error format (errorCode, message, timestamp)

DTOs for all requests/responses (no direct entity exposure)

Automatic notifications for important events

Logging with SLF4J (info, debug, error)

API Endpoints (Main Groups)
Method	Endpoint	Description	Access
POST	/auth/signup	Register new user	All
POST	/auth/login	Login → get JWT token	All
GET	/course/all_courses	List all courses (paginated)	All
POST	/course/add_course	Create course	Instructor
GET	/lesson/get_all_lessons/{courseId}	Lessons of a course	Student/Instructor
POST	/lesson/student_enter_lesson/{courseId}/{lessonId}/otp/{otp}	Mark attendance	Student
POST	/assignment/add_assignment	Create assignment	Instructor
POST	/assignment/uploadAssignment	Submit assignment	Student
POST	/quiz/add_quiz	Create quiz	Instructor
POST	/quiz/grade_quiz	Auto‑grade a quiz	Student
GET	/admin/users	List all users	Admin
DELETE	/admin/users/{id}	Delete any user	Admin
GET	/admin/courses	List all courses	Admin
DELETE	/admin/courses/{id}	Delete any course	Admin
GET	/admin/statistics	Platform statistics	Admin
Full API documentation available at /swagger-ui.html.

Deployment Notes
The backend is deployed on Render (free tier) and uses a MySQL database on Railway (free tier).
Because of the free tier:

The service may “sleep” after ~15 minutes of inactivity.

First request after sleep may take 30‑60 seconds (cold start).

This behaviour is normal for free hosting and does not affect local development.

Screenshots (Postman)
Add your Postman screenshots here. Recommended requests:

Student registration

Instructor login → JWT token

Create course (as instructor)

Create lesson with OTP

Student enrollment

OTP attendance marking

Assignment submission

Assignment grading

Quiz creation and question bank

Auto‑grading quiz

Admin: view all users

Admin: delete a user

Admin: platform statistics

Project Structure

src/main/java/onlinelearning/
├── config/                # Spring config (Security, OpenAPI, WebConfig)
├── controller/            # REST controllers (Auth, Course, Lesson, ...)
├── dto/
│   ├── request/           # DTOs for incoming requests
│   └── response/          # DTOs for responses
├── entity/                # JPA entities (UserAccount, Course, Lesson, ...)
├── exception/             # Custom exceptions & global handler
├── repository/            # Spring Data JPA repositories
├── security/              # JWT filter, UserDetailsService, SecurityConfig
├── service/               # Business logic (AuthService, CourseService, ...)
├── util/                  # ConvertHelper (DTO ↔ Entity mapping)
└── OnlineLearningPlatformApplication.java

Acknowledgements
Special thanks to my supervisor Mr. Erustan Erkebulanov for guidance and support.

Contact
Kerimalieva Zarina – backend developer
Email: zarina.kerimalieva@alatoo.edu.kg
GitHub: https://github.com/Kerimalieva

