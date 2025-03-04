-- Drop tables if they already exist
DROP TABLE IF EXISTS delivery_booking CASCADE;
DROP TABLE IF EXISTS delivery_slot CASCADE;
DROP TABLE IF EXISTS driver_schedule CASCADE;
DROP TABLE IF EXISTS delivery_driver CASCADE;
DROP TABLE IF EXISTS client CASCADE;
DROP TABLE IF EXISTS users CASCADE;
DROP TABLE IF EXISTS delivery_mode CASCADE;
DROP TABLE IF EXISTS role CASCADE;

-- Create Base Table for Users
CREATE TABLE users (
    id SERIAL PRIMARY KEY,
    username VARCHAR(255) UNIQUE NOT NULL,
    password VARCHAR(255) NOT NULL,
    email VARCHAR(255) UNIQUE NOT NULL,
    role VARCHAR(50) NOT NULL CHECK (role IN ('CLIENT', 'DELIVERY_DRIVER'))
) WITH (OIDS = FALSE);

-- Create Client Table (Inherits from Users) and Enforce Unique ID
CREATE TABLE client (
    address TEXT NOT NULL,
    UNIQUE (id)  --  Enforce uniqueness on id
) INHERITS (users);

-- Create Delivery Driver Table (Inherits from Users) and Enforce Unique ID
CREATE TABLE delivery_driver (
    vehicle_type VARCHAR(100) NOT NULL,
    UNIQUE (id)  --  Enforce uniqueness on id
) INHERITS (users);

-- Create Delivery Slot table
CREATE TABLE delivery_slot (
    id SERIAL PRIMARY KEY,
    delivery_mode_type VARCHAR(50) NOT NULL CHECK (delivery_mode_type IN ('DRIVE', 'DELIVERY', 'DELIVERY_TODAY', 'DELIVERY_ASAP')),
    start_time TIME NOT NULL,
    end_time TIME NOT NULL
);

-- Create Driver Schedule table
CREATE TABLE driver_schedule (
    id SERIAL PRIMARY KEY,
    delivery_driver_id INT REFERENCES delivery_driver(id) ON DELETE CASCADE,
    schedule_date DATE NOT NULL,
    start_time TIME NOT NULL,  -- Added start_time
    end_time TIME NOT NULL,    -- Added end_time
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
