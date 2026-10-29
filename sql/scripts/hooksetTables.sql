USE hookset;

-- New names, children first
DROP TABLE IF EXISTS FishWaterBody;
DROP TABLE IF EXISTS `Catch`;
DROP TABLE IF EXISTS Fly;
DROP TABLE IF EXISTS Fish;
DROP TABLE IF EXISTS WaterBody;
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
	UNIQUE (userId, slug)
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
    id BIGINT AUTO_INCREMENT PRIMARY KEY,
    fishId BIGINT NOT NULL,
    waterBodyId BIGINT NOT NULL,

    UNIQUE (fishId, waterBodyId),

    FOREIGN KEY (fishId)
        REFERENCES Fish(id),

    FOREIGN KEY (waterBodyId)
        REFERENCES WaterBody(id)
);

CREATE TABLE Catch (
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
        REFERENCES Fly(id),

    FOREIGN KEY (fishId)
        REFERENCES Fish(id),

    FOREIGN KEY (waterBodyId)
        REFERENCES WaterBody(id),

    CHECK (fishLength > 0)
);