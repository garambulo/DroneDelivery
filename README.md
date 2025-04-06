# Drone Delivery Management System

This is a Spring Boot application that provides a REST API for managing drones and medications for "The Drone" delivery company.

## Overview

The system allows management of a fleet of drones that can deliver medications to locations with difficult access. The API provides functionality for:

* Registering drones
* Loading drones with medications
* Checking loaded medications for a given drone
* Checking drone availability for loading
* Monitoring drone battery levels

## Technologies Used

* Java 17
* Spring Boot 3.4.4
* Spring Data JPA
* H2 In-memory Database
* Maven

## Getting Started

### Prerequisites

* JDK 17 or higher
* Maven 3.6 or higher

### Building the Application

```bash
mvn clean package
```

### Running the Application

```bash
java -jar target/drone-management-0.0.1-SNAPSHOT.jar
```

Or using Maven:

```bash
mvn spring-boot:run
```

The application will start on `http://localhost:8080`

## Database

The application uses H2 in-memory database which is configured in `application.properties`.
You can access the H2 console at `http://localhost:8080/h2-console` with these credentials:

* JDBC URL: `jdbc:h2:mem:dronedb`
* Username: `sa`
* Password: (empty)

The database is pre-loaded with test data for 10 drones and 10 medications from `data.sql`.

## API Endpoints

### Drone Endpoints

* `POST /api/drones` - Register a new drone
* `GET /api/drones` - Get all drones
* `GET /api/drones/{id}` - Get drone by ID
* `GET /api/drones/serial/{serialNumber}` - Get drone by serial number
* `GET /api/drones/available` - Get available drones for loading
* `POST /api/drones/load` - Load medications onto a drone
* `GET /api/drones/{id}/medications` - Get medications loaded on a drone
* `GET /api/drones/{id}/battery` - Check drone battery level
* `PUT /api/drones/{id}/state` - Update drone state

### Medication Endpoints

* `POST /api/medications` - Create a new medication
* `POST /api/medications/with-image` - Create a medication with image upload
* `GET /api/medications` - Get all medications
* `GET /api/medications/{id}` - Get medication by ID
* `GET /api/medications/code/{code}` - Get medication by code
* `PUT /api/medications/{id}` - Update a medication
* `DELETE /api/medications/{id}` - Delete a medication

## Functional Requirements Implementation

* **Drone Weight Limit**: Drones cannot be loaded beyond their maximum capacity based on model.
* **Battery Level Check**: Drones cannot enter LOADING state if battery is below 25%.
* **Battery Reduction**: Each delivery reduces battery by 10% (configurable).
* **State Transition**: The system has a scheduler that handles drone state transitions automatically.

## Testing the API

### Curl Examples

#### Register a new drone

```bash
curl -X POST \
  http://localhost:8080/api/drones \
  -H 'Content-Type: application/json' \
  -d '{
    "serialNumber": "DRN-TEST-001",
    "model": "HEAVYWEIGHT",
    "batteryCapacity": 100
}'
```

#### Load a drone with medications

```bash
curl -X POST \
  http://localhost:8080/api/drones/load \
  -H 'Content-Type: application/json' \
  -d '{
    "droneId": 1,
    "medicationIds": [1, 2, 3]
}'
```

#### Check drone battery

```bash
curl -X GET http://localhost:8080/api/drones/1/battery
```

#### Get medications loaded on a drone

```bash
curl -X GET http://localhost:8080/api/drones/1/medications
```

#### Check available drones

```bash
curl -X GET http://localhost:8080/api/drones/available
```

## Configuration

The application can be configured through `application.properties`. Key configurations:

* `drone.battery.min-level` - Minimum battery level for loading (default: 25%)
* `drone.battery.reduction-per-delivery` - Battery reduction after delivery (default: 10%)
* `drone.weight.check-enabled` - Enable/disable weight validation (default: true)
