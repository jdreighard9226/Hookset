CREATE DATABASE IF NOT EXISTS hookset;
USE hookset;

DROP TABLE IF EXISTS FishWaterBodies;
DROP TABLE IF EXISTS FlySizes;
DROP TABLE IF EXISTS Flies;
DROP TABLE IF EXISTS FlyMaterials;
DROP TABLE IF EXISTS Fishes;
DROP TABLE IF EXISTS WaterBodies;
DROP TABLE IF EXISTS Users;


CREATE TABLE Users (
    userId INT AUTO_INCREMENT PRIMARY KEY,
    username VARCHAR(20),
    hashPassword TEXT,
    isAdmin BOOLEAN
);


CREATE TABLE Flies (
    flyId INT AUTO_INCREMENT PRIMARY KEY,
    userId INT NULL,
    flyType VARCHAR(50) NULL,
    flyName VARCHAR(250),
    color VARCHAR(100),
    minSize INT,
    maxSize INT,
    dateEntered DATETIME,
    lastDateModified DATETIME NULL,

    FOREIGN KEY (userId)
        REFERENCES Users(userId)
);


CREATE TABLE FlyMaterials (
    materialId INT AUTO_INCREMENT PRIMARY KEY,
    materialName VARCHAR(250),
    materialColor VARCHAR(100) NULL,
    materialType VARCHAR(100)
);


CREATE TABLE FlySizes (
    flyId INT NOT NULL,
    size INT NOT NULL,

    CONSTRAINT PK_FlySizes
        PRIMARY KEY (flyId, size),

    FOREIGN KEY (flyId)
        REFERENCES Flies(flyId)
);


CREATE TABLE WaterBodies (
    waterBodyId INT AUTO_INCREMENT PRIMARY KEY,
    fwpLlid VARCHAR(80),
    waterBodyName VARCHAR(250) NOT NULL
);


CREATE TABLE Fishes (
    fishId INT AUTO_INCREMENT PRIMARY KEY,
    fwpSpeciesId INT,
    fishFamily VARCHAR(200),
    fishSpecies VARCHAR(200)
);


CREATE TABLE FishWaterBodies (
    fishId INT NOT NULL,
    waterBodyId INT NOT NULL,

    CONSTRAINT PK_FishWaterBodies
        PRIMARY KEY (fishId, waterBodyId),

    FOREIGN KEY (fishId)
        REFERENCES Fishes(fishId),

    FOREIGN KEY (waterBodyId)
        REFERENCES WaterBodies(waterBodyId)
);