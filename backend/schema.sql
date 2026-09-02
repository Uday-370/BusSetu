-- KMT BusSetu Database Schema
-- Run this file once in your PostgreSQL client (pgAdmin or psql)

-- Users table (admin & driver accounts)
CREATE TABLE IF NOT EXISTS users (
    id SERIAL PRIMARY KEY,
    name VARCHAR(100) NOT NULL,
    email VARCHAR(150) UNIQUE NOT NULL,
    password TEXT NOT NULL,
    role VARCHAR(20) NOT NULL CHECK (role IN ('admin', 'driver')),
    created_at TIMESTAMP DEFAULT NOW()
);

-- Buses table
CREATE TABLE IF NOT EXISTS buses (
    id SERIAL PRIMARY KEY,
    bus_number VARCHAR(50) UNIQUE NOT NULL,
    status VARCHAR(20) DEFAULT 'inactive' CHECK (status IN ('active', 'inactive', 'maintenance')),
    created_at TIMESTAMP DEFAULT NOW()
);

-- Routes table
CREATE TABLE IF NOT EXISTS routes (
    id SERIAL PRIMARY KEY,
    route_name VARCHAR(150) NOT NULL,
    description TEXT,
    start_point_name VARCHAR(150),
    start_latitude DECIMAL(10, 8),
    start_longitude DECIMAL(11, 8),
    end_point_name VARCHAR(150),
    end_latitude DECIMAL(10, 8),
    end_longitude DECIMAL(11, 8),
    created_by INTEGER REFERENCES users(id) ON DELETE SET NULL,
    created_at TIMESTAMP DEFAULT NOW()
);

-- Stops table
CREATE TABLE IF NOT EXISTS stops (
    id SERIAL PRIMARY KEY,
    stop_name VARCHAR(150) NOT NULL,
    latitude DECIMAL(10, 8) NOT NULL,
    longitude DECIMAL(11, 8) NOT NULL,
    route_id INTEGER REFERENCES routes(id) ON DELETE CASCADE,
    stop_order INTEGER NOT NULL,
    created_at TIMESTAMP DEFAULT NOW()
);

-- Trips table
CREATE TABLE IF NOT EXISTS trips (
    id SERIAL PRIMARY KEY,
    driver_id INTEGER REFERENCES users(id) ON DELETE SET NULL,
    bus_id INTEGER REFERENCES buses(id) ON DELETE SET NULL,
    route_id INTEGER REFERENCES routes(id) ON DELETE SET NULL,
    status VARCHAR(20) DEFAULT 'active' CHECK (status IN ('active', 'completed', 'cancelled')),
    started_at TIMESTAMP DEFAULT NOW(),
    ended_at TIMESTAMP
);

-- Locations table (live GPS data)
CREATE TABLE IF NOT EXISTS locations (
    id SERIAL PRIMARY KEY,
    trip_id INTEGER REFERENCES trips(id) ON DELETE CASCADE,
    latitude DECIMAL(10, 8) NOT NULL,
    longitude DECIMAL(11, 8) NOT NULL,
    speed DECIMAL(5, 2) DEFAULT 0,
    timestamp TIMESTAMP DEFAULT NOW()
);

-- Seed admin user (password: admin123)
-- bcrypt hash of 'admin123' with 10 rounds
INSERT INTO users (name, email, password, role)
VALUES (
    'Admin User',
    'admin@bussetu.com',
    '$2b$10$EzdQgIsIOFd0tJ3nc94dUOI2CBlgoKtilm/jZZuSd82YT5AzFIY56',
    'admin'
) ON CONFLICT (email) DO UPDATE SET password = EXCLUDED.password;

-- Seed driver user (password: driver123)
INSERT INTO users (name, email, password, role)
VALUES (
    'Test Driver',
    'driver@bussetu.com',
    '$2b$10$Cgc883mi0jW1YslTRO754evXwvFwujskJY9t6/N7BZ81aD6nQr2Ou',
    'driver'
) ON CONFLICT (email) DO UPDATE SET password = EXCLUDED.password;
