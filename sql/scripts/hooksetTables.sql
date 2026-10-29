USE hookset;

DROP TABLE IF EXISTS FishWaterBodies;
DROP TABLE IF EXISTS FlySizes;
DROP TABLE IF EXISTS Catches;
DROP TABLE IF EXISTS Flies;
DROP TABLE IF EXISTS FlyMaterials;
DROP TABLE IF EXISTS Fishes;
DROP TABLE IF EXISTS WaterBodies;
DROP TABLE IF EXISTS Users;


CREATE TABLE Users (
    id BIGINT AUTO_INCREMENT PRIMARY KEY,
    username VARCHAR(20),
    hashPassword TEXT,
    isAdmin BOOLEAN
);


CREATE TABLE Fly (
    id BIGINT AUTO_INCREMENT PRIMARY KEY,
    userId BIGINT NULL,
    type VARCHAR(50) NOT NULL,
    name VARCHAR(100) NOT NULL,
    color VARCHAR(40) NOT NULL,
    minSize INT NOT NULL,
    maxSize INT NOT NULL,
    image VARCHAR(100) NOT NULL,
    slug VARCHAR(125) NOT NULL,
    
    FOREIGN KEY (userId)
        REFERENCES Users(id),
	UNIQUE (userId, flySlug)
);

CREATE TABLE WaterBody (
    id BIGINT AUTO_INCREMENT PRIMARY KEY,
    name VARCHAR(250) NOT NULL,
    type VARCHAR(100) NOT NULL,
    state VARCHAR(100) NOT NULL,
    description varchar(1000) NOT NULL,
    slug varchar(100) NOT NULL UNIQUE,
       CHECK (type IN ('River', 'Lake', 'Stream'))
);


CREATE TABLE Fish (
    id BIGINT AUTO_INCREMENT PRIMARY KEY,
    family VARCHAR(200) NOT NULL,
    species VARCHAR(200) NOT NULL,
    image VARCHAR(100) NOT NULL
);


CREATE TABLE FishWaterBody (
    fishWaterBodyId BIGINT AUTO_INCREMENT PRIMARY KEY,
    fishId BIGINT NOT NULL,
    waterBodyId BIGINT NOT NULL,

    UNIQUE (fishId, waterBodyId),

    FOREIGN KEY (fishId)
        REFERENCES Fishes(id),

    FOREIGN KEY (waterBodyId)
        REFERENCES WaterBodies(id)
);

CREATE TABLE Catches (
    id BIGINT AUTO_INCREMENT PRIMARY KEY,
    userId BIGINT NOT NULL,
    flyId BIGINT NOT NULL,
    fishId BIGINT NOT NULL,
    waterBodyId BIGINT NOT NULL,
    fishLength DECIMAL(5,2),
    dateCaught DATETIME NOT NULL,
    notes VARCHAR(1000),

    FOREIGN KEY (userId)
        REFERENCES Users(id),

    FOREIGN KEY (flyId)
        REFERENCES Flies(id),

    FOREIGN KEY (fishId)
        REFERENCES Fishes(id),

    FOREIGN KEY (waterBodyId)
        REFERENCES WaterBodies(id),

    CHECK (fishLength > 0)
);