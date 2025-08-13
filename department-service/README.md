# Department Service

## Overview
The Department Service is a core component of the SmartCityFix platform that manages city departments and routes complaints to the appropriate department based on various criteria.

## Features
- Create, read, update, and delete departments
- Intelligent complaint routing based on:
    - Complaint category (ROADS, WATER, ELECTRICITY, etc.)
    - Geographic zone
    - Proximity to complaint location
- Department workload management
- Event-driven communication with other services
- Circuit breaker and retry patterns for resilience

## Tech Stack
- Java 17
- Spring Boot 3.1
- Spring Cloud (Config, Discovery, Gateway)
- PostgreSQL
- RabbitMQ
- Resilience4j
- Flyway for database migrations
- OpenAPI/Swagger for API documentation

## Getting Started

### Prerequisites
- JDK 17+
- Maven 3.8+
- PostgreSQL 14+
- RabbitMQ 3.9+

### Environment Setup
1. Create a PostgreSQL database:
```sql
CREATE DATABASE department_service;