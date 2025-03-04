-- Drop tables if they already exist
DROP TABLE IF EXISTS delivery_booking CASCADE;
DROP TABLE IF EXISTS delivery_slot CASCADE;
DROP TABLE IF EXISTS driver_schedule CASCADE;
DROP TABLE IF EXISTS delivery_driver CASCADE;
DROP TABLE IF EXISTS client CASCADE;
DROP TABLE IF EXISTS users CASCADE;
DROP TABLE IF EXISTS delivery_mode CASCADE;
DROP TABLE IF EXISTS role CASCADE;

-- Create User table (Base class for Client & DeliveryDriver)
CREATE TABLE users (
    id SERIAL PRIMARY KEY,
    username VARCHAR(255) UNIQUE NOT NULL,
    password VARCHAR(255) NOT NULL,
    email VARCHAR(255) UNIQUE NOT NULL,
    role VARCHAR(50) NOT NULL CHECK (role IN ('CLIENT', 'DELIVERY_DRIVER'))
);

-- Create Client table (Inherits from Users)
CREATE TABLE client (
    id INT PRIMARY KEY REFERENCES users(id) ON DELETE CASCADE,
    address TEXT NOT NULL
);

-- Create Delivery Driver table (Inherits from Users)
CREATE TABLE delivery_driver (
    id INT PRIMARY KEY REFERENCES users(id) ON DELETE CASCADE,
    vehicle_type VARCHAR(100) NOT NULL
);

-- Create Delivery Mode table
CREATE TABLE delivery_mode (
    id SERIAL PRIMARY KEY,
    type VARCHAR(50) UNIQUE NOT NULL CHECK (type IN ('DRIVE', 'DELIVERY', 'DELIVERY_TODAY', 'DELIVERY_ASAP'))
);

-- Create Delivery Slot table
CREATE TABLE delivery_slot (
    id SERIAL PRIMARY KEY,
    mode_id INT REFERENCES delivery_mode(id) ON DELETE CASCADE,
    slot_time TIME NOT NULL,
    max_capacity INT NOT NULL CHECK (max_capacity > 0),
    current_capacity INT DEFAULT 0 CHECK (current_capacity >= 0)
);

-- Create Driver Schedule table
CREATE TABLE driver_schedule (
    id SERIAL PRIMARY KEY,
    delivery_driver_id INT REFERENCES delivery_driver(id) ON DELETE CASCADE,
    schedule_date DATE NOT NULL,
    available BOOLEAN DEFAULT TRUE
);

-- Create Delivery Booking table
CREATE TABLE delivery_booking (
    id SERIAL PRIMARY KEY,
    slot_id INT REFERENCES delivery_slot(id) ON DELETE CASCADE,
    client_id INT REFERENCES client(id) ON DELETE CASCADE,
    delivery_driver_id INT REFERENCES delivery_driver(id) ON DELETE CASCADE,
    status VARCHAR(50) NOT NULL CHECK (status IN ('PENDING', 'IN_TRANSIT', 'DELIVERED'))
);
