USE Hookset  
 
IF OBJECT_ID('dbo.FlySizes', 'U') IS NOT NULL 
DROP TABLE dbo.FlySizes 
GO 
 
IF OBJECT_ID('dbo.Flies', 'U') IS NOT NULL 
DROP TABLE dbo.Flies  
GO 
 
IF OBJECT_ID('dbo.Users', 'U') IS NOT NULL 
DROP TABLE dbo.Users 
GO 
 
IF OBJECT_ID('dbo.FlyMaterials', 'U') IS NOT NULL 
DROP TABLE dbo.FlyMaterials 
GO

IF OBJECT_ID('dbo.FishWaterBodies', 'U') IS NOT NULL
DROP TABLE dbo.FishWaterBodies
GO

IF OBJECT_ID('dbo.Fishes', 'U') IS NOT NULL
DROP TABLE dbo.Fishes
GO

IF OBJECT_ID('dbo.Water_Bodies', 'U') IS NOT NULL
DROP TABLE dbo.Water_Bodies
GO
CREATE TABLE dbo.Users (
	User_ID INT IDENTITY(1,1) PRIMARY KEY,
	Username NVARCHAR(20),
	Hash_Password NVARCHAR(MAX),
	IsAdmin BIT, 
	);


CREATE TABLE dbo.Flies (
	Fly_ID INT IDENTITY(1,1) PRIMARY KEY,
	User_ID INT NULL,
	FOREIGN KEY (User_ID) REFERENCES Users(User_ID),
	Fly_Type NVARCHAR(50) NULL,
	Fly_Name NVARCHAR(250),
	Color NVARCHAR(100),
	Min_Size INT,
	Max_Size INT,
	Date_Entered DATETIME,
	Last_Date_Modified DATETIME NULL,
);

CREATE TABLE dbo.FlyMaterials (
	Material_ID INT IDENTITY(1,1) PRIMARY KEY,
	Material_Name NVARCHAR(250),
	Material_Color NVARCHAR(100) NULL,
	Material_Type NVARCHAR(100) 
);

CREATE TABLE dbo.FlySizes (
    Fly_ID INT NOT NULL,
    Size INT NOT NULL,

    CONSTRAINT PK_FlySizes
        PRIMARY KEY (Fly_ID, Size)
);

CREATE TABLE dbo.Water_Bodies 
(
	Water_Body_ID INT IDENTITY(1,1) PRIMARY KEY,
	Water_Body_Name NVARCHAR(250) NOT NULL,
);


CREATE TABLE dbo.Fishes
(
	Fish_ID INT IDENTITY(1,1) PRIMARY KEY,
	Fish_Name NVARCHAR(200),
	Fish_Species NVARCHAR(200),
);

CREATE TABLE dbo.FishWaterBodies (
	Fish_ID INT NOT NULL,
	Water_Body_ID INT NOT NULL,
	FOREIGN KEY (Fish_ID) REFERENCES Fishes(Fish_ID),
	FOREIGN KEY(Water_Body_ID) REFERENCES Water_Bodies(Water_Body_ID),

	  CONSTRAINT PK_Fish_ID_Water_Body_ID
        PRIMARY KEY (Fish_ID, Water_Body_ID)
);