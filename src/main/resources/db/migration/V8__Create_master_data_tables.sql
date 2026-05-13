CREATE TABLE locations (
                           id SERIAL PRIMARY KEY,
                           city_name VARCHAR(100) NOT NULL,
                           slug VARCHAR(100) UNIQUE NOT NULL,
                           is_popular BOOLEAN DEFAULT FALSE
);

CREATE TABLE categories (
                            id SERIAL PRIMARY KEY,
                            name VARCHAR(100) NOT NULL,
                            description TEXT
);

CREATE TABLE amenities (
                           id SERIAL PRIMARY KEY,
                           name VARCHAR(100) NOT NULL UNIQUE,
                           icon_name VARCHAR(50),
                           group_name VARCHAR(50)
);