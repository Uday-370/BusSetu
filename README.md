# 🚍 BusSetu — Real-Time Fleet Telemetry & Transit Navigation System

<p align="center">
  <img src="app/src/main/res/drawable/bussetu_icon.png" alt="BusSetu Logo" width="120" height="120" style="border-radius: 20%;" />
</p>

<p align="center">
  <b>A full-stack, real-time public transit tracking and smart telemetry platform connecting passengers, drivers, and fleet administrators.</b>
</p>

<p align="center">
  <img src="https://img.shields.io/badge/Platform-Android-3DDC84?style=for-the-badge&logo=android&logoColor=white" alt="Android" />
  <img src="https://img.shields.io/badge/Kotlin-2.0+-7F52FF?style=for-the-badge&logo=kotlin&logoColor=white" alt="Kotlin" />
  <img src="https://img.shields.io/badge/Node.js-18+-339933?style=for-the-badge&logo=nodedotjs&logoColor=white" alt="Node.js" />
  <img src="https://img.shields.io/badge/Express.js-5.x-000000?style=for-the-badge&logo=express&logoColor=white" alt="Express.js" />
  <img src="https://img.shields.io/badge/PostgreSQL-15+-4169E1?style=for-the-badge&logo=postgresql&logoColor=white" alt="PostgreSQL" />
  <img src="https://img.shields.io/badge/Socket.IO-4.8-010101?style=for-the-badge&logo=socketdotio&logoColor=white" alt="Socket.IO" />
  <img src="https://img.shields.io/badge/React-18+-61DAFB?style=for-the-badge&logo=react&logoColor=black" alt="React" />
</p>

---

## 📖 Table of Contents
- [Project Overview](#-project-overview)
- [System Architecture](#-system-architecture)
- [Key Features](#-key-features)
  - [📱 Android Mobile App](#-android-mobile-app)
  - [⚙️ Backend & Real-Time Engine](#️-backend--real-time-engine)
  - [🌐 Web Admin & Citizen Portal](#-web-admin--citizen-portal)
- [Database Schema](#-database-schema)
- [Repository Structure](#-repository-structure)
- [Getting Started](#-getting-started)
  - [Prerequisites](#prerequisites)
  - [1. Database Configuration](#1-database-configuration)
  - [2. Backend Setup](#2-backend-setup)
  - [3. Web Dashboard Setup](#3-web-dashboard-setup)
  - [4. Android Application Setup](#4-android-application-setup)
- [API & WebSocket Reference](#-api--websocket-reference)
- [Engineering Highlights](#-engineering-highlights)
- [License](#-license)

---

## 🌟 Project Overview

**BusSetu** (*Setu = Bridge*) bridges the communication gap in municipal and private transit ecosystems. It provides sub-second GPS tracking, predictive arrival times (ETAs), dynamic road-geometry route visualization, conversational natural language search, and centralized fleet operations management.

### The System at a Glance
1. **Driver Interface (Android):** Live telemetry broadcaster with persistent background tracking and trip management.
2. **Passenger Interface (Android & Web):** Interactive OpenStreetMap / OSMDroid live map, stop-by-stop ETA timeline, route discovery, and conversational search.
3. **Fleet Admin Portal (Web):** Central management for drivers, routes, bus inventory, and bulk Excel route imports.
4. **Backend Telemetry Hub (Node.js/PostgreSQL):** High-throughput GPS ingest, WebSocket pub/sub rooms, and dynamic in-memory cached Haversine ETA algorithms.

---

## 🏗️ System Architecture

BusSetu follows a decoupled, event-driven client-server architecture:

```
┌────────────────────────────────────────────────────────┐
│                   Android Client App                   │
│  ┌───────────────────────┐   ┌──────────────────────┐  │
│  │     Driver Duty       │   │   Passenger Transit  │  │
│  │ (Foreground Service / │   │  (OSMDroid Map Flow /│  │
│  │   FusedLocationGPS)   │   │     Chatbot NLP)     │  │
│  └───────────┬───────────┘   └───────────▲──────────┘  │
└──────────────┼───────────────────────────┼─────────────┘
               │ HTTP / WS                 │ WebSocket Stream
               ▼                           │ (location_update)
┌──────────────────────────────────────────┴─────────────┐
│               Node.js + Express Backend                │
│  ┌────────────────────────┐  ┌──────────────────────┐  │
│  │   REST API Endpoints   │  │   Socket.IO Rooms    │  │
│  │ (Auth, Routes, Trips)  │  │(trip_{id}, route_{id}│  │
│  └───────────┬────────────┘  └───────────▲──────────┘  │
│              │                           │             │
│              ▼                           │             │
│  ┌───────────────────────────────────────┴──────────┐  │
│  │   ETA Engine (Haversine + OSRM + TTL Cache)      │  │
│  └───────────────────────┬──────────────────────────┘  │
└──────────────────────────┼─────────────────────────────┘
                           ▼
┌────────────────────────────────────────────────────────┐
│                  PostgreSQL Database                   │
│      [users] [buses] [routes] [stops] [trips]          │
│                    [locations]                         │
└────────────────────────────────────────────────────────┘
```

---

## ✨ Key Features

### 📱 Android Mobile App
* **Modern Clean Architecture + MVVM:** Strict separation of concerns into Domain, Data, and Presentation layers using **Dagger Hilt** for dependency injection.
* **Resilient Foreground GPS Service (`LocationService`):**
  * Android Foreground Service with sticky lifecycle and low-power persistent notification.
  * Google Play Services `FusedLocationProviderClient` with adaptive displacement filtering (10m minimum displacement) to prevent battery drain when idling at stops.
* **Reactive Real-Time Streaming:**
  * Uses `callbackFlow` to adapt bidirectional Socket.IO events into native Kotlin `Flow` streams for UI observation.
* **Geospatial & Road Geometry Engine:**
  * Integrated **OSMDroid (OpenStreetMap)** and **OSRM Routing Engine** to fetch full GeoJSON polyline road paths between stops with fallback straight-line interpolation.
* **Intelligent Query Assistant:**
  * Conversational query engine supporting route discovery, nearest stop queries, and active bus checks.
* **Zero-Latency Session Management:**
  * **Jetpack Preferences DataStore** coupled with an in-memory volatile cache (`SessionManager`) enabling non-blocking dynamic JWT header injection in OkHttp interceptors.

---

### ⚙️ Backend & Real-Time Engine
* **Event-Driven WebSocket Hub:**
  * Socket.IO pub/sub architecture supporting isolated rooms (`trip_{id}`, `route_{id}`) for low-latency coordinate broadcasts.
* **Dynamic ETA Engine (`etaService.js`):**
  * Precise Haversine distance calculations coupled with vehicle velocity heuristics.
  * 5-minute in-memory TTL stop cache with write-through invalidation on route changes.
* **High-Throughput SQL Optimization:**
  * PostgreSQL connection pooling (`pg.Pool`) and SQL `LATERAL` joins to fetch the latest telemetry ping per active trip in a single query.
  * Atomic transactions (`BEGIN ... COMMIT / ROLLBACK`) for bulk Excel spreadsheet route parsing and multi-station uploads.
* **Security & Access Control:**
  * Stateless **JWT** authentication with bcrypt (10 rounds) password hashing and Role-Based Access Control (RBAC: `admin`, `driver`).

---

### 🌐 Web Admin & Citizen Portal
* **Live Fleet Dashboard:** Real-time visual map tracking all active buses across city routes.
* **Bulk Route Importer:** Excel/XLSX file parser for importing full transit routes with sequence-ordered stops and GPS coordinates.
* **Fleet & Driver Management:** CRUD operations for buses, driver assignments, and active trip monitoring.

---

## 🗄️ Database Schema

The PostgreSQL relational schema is structured with referential integrity:

```sql
users (id, name, email, password, role, created_at)
  │
  ├── routes (id, route_name, description, start_point_name, start_latitude, start_longitude, ...)
  │     │
  │     └── stops (id, stop_name, latitude, longitude, route_id, stop_order, created_at)
  │
  └── trips (id, driver_id, bus_id, route_id, status, started_at, ended_at)
        │
        └── locations (id, trip_id, latitude, longitude, speed, timestamp)
```

---

## 📁 Repository Structure

```
BusSetu/
├── app/                          # Android Application (Kotlin, Jetpack Compose, Hilt)
│   └── src/main/java/com/example/bussetu/
│       ├── core/                 # Navigation, theme, session utils, top bar
│       ├── di/                   # Hilt Dependency Injection modules
│       ├── feature_auth/         # Driver authentication & session persistence
│       ├── feature_chatbot/      # AI/NLP transit assistant
│       ├── feature_dashboard/   # Passenger home & search interface
│       ├── feature_driver/       # Driver duty screen, start trip, Foreground Service
│       └── feature_map/          # OSMDroid map, OSRM road geometry, live tracking
├── backend/                      # Node.js + Express REST API & WebSocket Engine
│   ├── config/                   # PostgreSQL connection pool configuration
│   ├── controllers/              # Auth, Bus, Route, Stop, Trip, Location, Chatbot
│   ├── middleware/               # JWT verification & RBAC authorization
│   ├── routes/                   # Express endpoint route definitions
│   ├── services/                 # Haversine & OSRM ETA calculation engine
│   ├── init_db.js                # Database initialization script
│   ├── schema.sql                # Relational PostgreSQL schema & seed data
│   └── server.js                 # Express server & Socket.IO pub/sub setup
└── frontend/                     # Web Portal (React 18 + Vite + Tailwind CSS)
    ├── src/                      # Admin & Citizen pages, live tracking maps
    └── package.json
```

---

## 🚀 Getting Started

### Prerequisites
* **Node.js**: `v18.0.0+`
* **PostgreSQL**: `v14.0+`
* **Android Studio**: Ladybug / Hedgehog or newer
* **Java Development Kit (JDK)**: `JDK 11` or `JDK 17`

---

### 1. Database Configuration
1. Start your local PostgreSQL server and create a database named `kmt_bus` (or your preferred name):
   ```sql
   CREATE DATABASE kmt_bus;
   ```
2. Navigate to the `backend/` directory and configure your `.env` file:
   ```env
   PORT=5000
   DB_USER=postgres
   DB_PASSWORD=your_password
   DB_HOST=localhost
   DB_PORT=5432
   DB_NAME=kmt_bus
   JWT_SECRET=your_super_secret_jwt_key
   ```
3. Initialize tables and seed initial users (`admin@bussetu.com` / `driver@bussetu.com`):
   ```bash
   cd backend
   npm run init-db
   ```

---

### 2. Backend Setup
```bash
cd backend
npm install
npm run dev
```
The backend server and WebSocket listener will start on `http://localhost:5000`.

---

### 3. Web Dashboard Setup
```bash
cd frontend
npm install
npm run dev
```
The web dashboard will be available at `http://localhost:5173`.

---

### 4. Android Application Setup
1. Open the root project in **Android Studio**.
2. Update the backend URL in `app/src/main/java/com/example/bussetu/core/presentation/components/AppConstants.kt`:
   * For **Android Emulator**: `http://10.0.2.2:5000/`
   * For **Physical Device**: Use your local LAN IP (e.g., `http://192.168.1.10:5000/`) or an ngrok tunnel.
3. Sync Gradle and build the project:
   ```bash
   ./gradlew assembleDebug
   ```
4. Run the app on your emulator or physical Android device.

---

## 📡 API & WebSocket Reference

### Key REST Endpoints
| Method | Endpoint | Description | Auth Required |
| :--- | :--- | :--- | :--- |
| `POST` | `/api/auth/login` | Driver / Admin Authentication | Public |
| `GET` | `/api/routes` | Fetch all configured transit routes | Public |
| `POST` | `/api/routes` | Create route with stops (atomic transaction) | `admin` |
| `POST` | `/api/routes/upload-excel`| Bulk route import via spreadsheet | `admin` |
| `GET` | `/api/trips/active` | Get all active trips with latest GPS coordinate | Public |
| `POST` | `/api/trips/start` | Start driver duty & activate bus | `driver` |
| `PUT` | `/api/trips/:id/end` | End driver duty & release bus | `driver` |
| `POST` | `/api/locations/update` | Post GPS ping (lat, lng, speed) & broadcast | `driver` |
| `POST` | `/api/chatbot/query` | Conversational transit NLP query | Public |

### WebSocket Events
| Event Name | Direction | Payload | Description |
| :--- | :--- | :--- | :--- |
| `join_trip` | Client ➔ Server | `tripId` | Joins room `trip_{id}` |
| `join_route` | Client ➔ Server | `routeId` | Joins room `route_{id}` |
| `driver_location` | Client ➔ Server | `{ trip_id, latitude, longitude, speed }` | Ingests live driver GPS ping |
| `location_update` | Server ➔ Client | `{ trip_id, latitude, longitude, speed, etas }` | Broadcasts telemetry & calculated ETAs |

---

## 💡 Engineering Highlights

* **Battery Telemetry Optimization:** 10m minimum displacement filter reduces unnecessary CPU wake-locks when buses idle at signals.
* **Non-Blocking Dynamic Auth:** Synchronous OkHttp interceptor reads cached in-memory JWT tokens backed by asynchronous Jetpack DataStore.
* **Fault-Tolerant Routing:** Live road polylines from OSRM with seamless straight-line coordinate fallback during connectivity dropouts.
* **Scalable Pub/Sub Architecture:** Node.js room-based socket isolation ensures only relevant subscribers receive high-frequency telemetry.

---

## 📄 License
This project is open-source and available under the [ISC License](LICENSE).
