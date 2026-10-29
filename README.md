# Smart Clinic Queue Management System — Backend

The backend of the Smart Clinic Queue Management System is designed to manage users, clinic information, patient queues, estimated waiting times, and related application data.

## About the Project

The Smart Clinic Queue Management System helps patients check clinic queue information and estimated waiting times before visiting a clinic.

The backend provides APIs to support communication between the frontend application and the database.

## Technologies Used

- Java
- Spring Boot
- Spring Data JPA
- Hibernate
- MySQL
- Maven

## Features

- User Management
- Patient Registration and Login Support
- Clinic Management
- Queue Management
- Estimated Waiting Time
- REST API Development
- MySQL Database Integration
- CRUD Operations

## Project Architecture

The application follows a layered backend architecture.

- **Controller Layer:** Handles incoming HTTP requests.
- **Service Layer:** Contains application business logic.
- **Repository Layer:** Handles database operations.
- **Entity Layer:** Represents database entities.
- **MySQL Database:** Stores application data.

## Project Overview

The backend provides API endpoints for the following modules:

- Users
- Clinics
- Queues
- Waiting Time
- Notifications

## Getting Started

### Prerequisites

Install the following software:

- Java JDK
- MySQL Server
- Maven, if not using the included Maven Wrapper
- Eclipse IDE or another Java IDE
  
##  API Endpoints

The backend includes the following API routes:

| Method | Endpoint | Purpose |
|---|---|---|
| GET | `/api/users` | Retrieve users |
| POST | `/api/users` | Create a user |
| PUT | `/api/users/{id}` | Update a user |
| GET | `/api/clinics` | Retrieve clinic information |
| GET | `/api/queues` | Retrieve queue information |
| GET | `/api/queues/{id}/wait-time` | Retrieve estimated waiting time |
| GET | `/api/notifications/patient/{patientId}` | Retrieve patient notifications |

*Note: The availability and exact behavior of individual endpoints depend on the current backend implementation.*

## 🎯 Project Objective

The objective of this project is to improve the clinic visit experience by providing patients with access to queue information and estimated waiting times through a web-based application.

##  Developer

**Bhavesh Khertala**

GitHub: [bhaveshkhertala](https://github.com/bhaveshkhertala)
