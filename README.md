# SmartCityFix

A fault-tolerant, scalable microservices platform that lets citizens register and submit city complaints (potholes, water leakage, power outages). Complaints are automatically routed to the correct municipal department, processed asynchronously, and both citizens and admins receive notifications and status updates.

## Architecture

SmartCityFix is built using a microservices architecture with the following components:

- **Config Service**: Centralized configuration server
- **Discovery Service**: Service registry using Netflix Eureka
- **Gateway Service**: API Gateway using Spring Cloud Gateway
- **User Service**: User management and authentication
- **Complaint Service**: Complaint creation and lifecycle management
- **Department Service**: Department registry and routing rules
- **Notification Service**: Notification delivery (email/SMS)
- **Feedback Service**: Feedback collection after complaint resolution

## Prerequisites

- Java 17
- Maven
- Docker and Docker Compose

## Getting Started

### Building the Project

```bash
mvn clean package -DskipTests