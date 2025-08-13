feat(complaint-service): implement complete complaint management API

- Add complaint creation, retrieval, and search endpoints
- Implement status transition workflow (OPEN → ASSIGNED → IN_PROGRESS → RESOLVED → CLOSED)
- Add event publishing for complaint lifecycle events
- Configure circuit breaker with Resilience4j for external service calls
- Add comprehensive validation and error handling
- Include Swagger documentation for API endpoints

-------

# Complaint Service

A microservice for managing citizen complaints in the SmartCityFix platform. This service handles the complete lifecycle of complaints from creation to resolution.

## Features

- Create, retrieve, update, and delete complaints
- Search complaints with various filters
- Manage complaint lifecycle with status transitions
- Assign complaints to departments
- Track complaint history
- Event publishing for integration with other services

## Tech Stack

- Java 17
- Spring Boot 3.x
- Spring Cloud (Eureka, Config, Circuit Breaker)
- PostgreSQL
- RabbitMQ
- Resilience4j
- Flyway for database migrations
- OpenAPI/Swagger for API documentation

## Prerequisites

- JDK 17+
- Maven 3.8+
- PostgreSQL 14+
- RabbitMQ 3.9+
- Running instances of:
    - Config Service (port 8888)
    - Discovery Service (port 8761)
    - Gateway Service (port 8080)
    - User Service (port 8081)
    - RabbitMQ (port 15672)

## Setup & Running

1. **Clone the repository**
   ```bash
   git clone https://github.com/singh-vaibhav-07/SmartCityFix.git
   cd complaint-service

Configure the database

Create a PostgreSQL database:

sql


CREATE DATABASE complaint_service;
Configure application properties

The service uses Spring Cloud Config Server for configuration. Ensure your config server has the appropriate properties for this service.

Build the application

bash


mvn clean package
Run the application

bash


mvn spring-boot:run
The service will start on port 8082 by default.