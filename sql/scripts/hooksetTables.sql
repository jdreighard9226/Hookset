USE hookset;

DROP TABLE IF EXISTS FishWaterBodies;
DROP TABLE IF EXISTS FlySizes;
DROP TABLE IF EXISTS Flies;
DROP TABLE IF EXISTS FlyMaterials;
DROP TABLE IF EXISTS Fishes;
DROP TABLE IF EXISTS WaterBodies;
DROP TABLE IF EXISTS Users;


CREATE TABLE Users (
    userId BIGINT AUTO_INCREMENT PRIMARY KEY,
    username VARCHAR(20),
    hashPassword TEXT,
    isAdmin BOOLEAN
);


CREATE TABLE Flies (
    flyId BIGINT AUTO_INCREMENT PRIMARY KEY,
    userId BIGINT NULL,
    flyType VARCHAR(50) NULL,
    flyName VARCHAR(250),
    color VARCHAR(100),
    minSize INT,
    maxSize INT,
	dateEntered DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP,
    lastDateModified DATETIME NULL,

    FOREIGN KEY (userId)
        REFERENCES Users(userId)
);


CREATE TABLE FlyMaterials (
    materialId BIGINT AUTO_INCREMENT PRIMARY KEY,
    materialName VARCHAR(250),
    materialColor VARCHAR(100) NULL,
    materialType VARCHAR(100)
);


CREATE TABLE WaterBodies (
    waterBodyId BIGINT AUTO_INCREMENT PRIMARY KEY,
    waterBodyName VARCHAR(250) NOT NULL,
    waterBodyType VARCHAR(100) NOT NULL,
    waterBodyState VARCHAR(100) NOT NULL,
    waterBodyDescription varchar(1000) NOT NULL,
    waterBodySlug varchar(100) NOT NULL UNIQUE,
       CHECK (waterBodyType IN ('River', 'Lake', 'Stream'))
);


CREATE TABLE Fishes (
    fishId BIGINT AUTO_INCREMENT PRIMARY KEY,
    fishFamily VARCHAR(200) NOT NULL,
    fishSpecies VARCHAR(200) NOT NULL,
    fishImage VARCHAR(100) NOT NULL
);


CREATE TABLE FishWaterBodies (
    fishWaterBodyId BIGINT AUTO_INCREMENT PRIMARY KEY,
    fishId BIGINT NOT NULL,
    waterBodyId BIGINT NOT NULL,

    UNIQUE (fishId, waterBodyId),

    FOREIGN KEY (fishId)
        REFERENCES Fishes(fishId),

    FOREIGN KEY (waterBodyId)
        REFERENCES WaterBodies(waterBodyId)
);