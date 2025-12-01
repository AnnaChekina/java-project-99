# Task Manager

### Hexlet tests and linter status:
[![Actions Status](https://github.com/AnnaChekina/java-project-99/actions/workflows/hexlet-check.yml/badge.svg)](https://github.com/AnnaChekina/java-project-99/actions)
[![Build](https://github.com/AnnaChekina/java-project-99/actions/workflows/build.yml/badge.svg)](https://github.com/AnnaChekina/java-project-99/actions/workflows/build.yml)
[![Quality Gate Status](https://sonarcloud.io/api/project_badges/measure?project=AnnaChekina_java-project-99&metric=alert_status)](https://sonarcloud.io/summary/new_code?id=AnnaChekina_java-project-99)
[![Bugs](https://sonarcloud.io/api/project_badges/measure?project=AnnaChekina_java-project-99&metric=bugs)](https://sonarcloud.io/summary/new_code?id=AnnaChekina_java-project-99)
[![Code Smells](https://sonarcloud.io/api/project_badges/measure?project=AnnaChekina_java-project-99&metric=code_smells)](https://sonarcloud.io/summary/new_code?id=AnnaChekina_java-project-99)
[![Coverage](https://sonarcloud.io/api/project_badges/measure?project=AnnaChekina_java-project-99&metric=coverage)](https://sonarcloud.io/summary/new_code?id=AnnaChekina_java-project-99)
[![Duplicated Lines (%)](https://sonarcloud.io/api/project_badges/measure?project=AnnaChekina_java-project-99&metric=duplicated_lines_density)](https://sonarcloud.io/summary/new_code?id=AnnaChekina_java-project-99)

## 🚀 Live Application

The application is deployed on Render.com and available at:  
**👉 https://java-project-99-1prm.onrender.com**

## 📋 About

Task Manager is a full-featured web application for task management built with Spring Boot. It provides REST API for managing users, tasks, task statuses, and labels.

## 🎯 Features

- **User Management**: Registration, authentication, profile management
- **Task Management**: Create, edit, delete, and filter tasks
- **Task Statuses**: Workflow system (Draft, ToReview, ToBeFixed, ToPublish, Published)
- **Labels**: Categorize tasks with labels
- **Security**: JWT authentication with endpoint protection
- **Deployment**: Fully deployed on Render.com

## 🛠️ Tech Stack

- **Backend**: Java 21, Spring Boot 3
- **Database**: PostgreSQL (production), H2 (development/testing)
- **Authentication**: JWT (JSON Web Tokens)
- **Build**: Gradle
- **Deployment**: Render.com
- **CI/CD**: GitHub Actions
- **Code Quality**: SonarCloud, Checkstyle

## 🚦 Quick Start

### Local Setup

1. **Clone the repository**
```bash
git clone https://github.com/AnnaChekina/java-project-99.git
cd java-project-99
```
   
2. **Run the application**
```bash
  make run
```
   
App will be available at: `http://localhost:8080`

## 🔐 Authentication & Preloaded Data

### Default User for Testing
- **Email**: `hexlet@example.com`
- **Password**: `qwerty`

### Get JWT Token
```bash
curl -X POST https://java-project-99-1prm.onrender.com/api/login \
-H "Content-Type: application/json" \
-d '{"username":"hexlet@example.com","password":"qwerty"}'
```  
    
### Use Token in Requests
```bash
curl -X GET https://java-project-99-1prm.onrender.com/api/users \
-H "Authorization: Bearer YOUR_JWT_TOKEN"
```
### Available Endpoints:
  - `GET, POST, PUT, DELETE /api/users`
  - `GET, POST, PUT, DELETE /api/tasks`
  - `GET, POST, PUT, DELETE /api/task_statuses`
  - `GET, POST, PUT, DELETE /api/labels`
