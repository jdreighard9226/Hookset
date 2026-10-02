use hookset;

-- ============================================================
-- WATER BODIES
-- ============================================================

INSERT INTO WaterBodies
    (waterBodyName, waterBodyType, waterBodyState, waterBodyDescription, waterBodySlug)
VALUES

(
    'Gallatin River',
    'River',
    'Montana',
    'A major southwest Montana trout river that begins near Yellowstone National Park and flows through mountain valleys before joining the Missouri River headwaters system.',
    'gallatin-river'
),

(
    'Madison River',
    'River',
    'Montana',
    'An iconic southwest Montana trout river formed by the Firehole and Gibbon rivers near Yellowstone National Park and known for its productive wild trout fishery.',
    'madison-river'
),

(
    'Big Hole River',
    'River',
    'Montana',
    'A scenic southwest Montana river flowing through mountain valleys and agricultural country, recognized for its wild trout fishery and important native fish habitat.',
    'big-hole-river'
),

(
    'Blackfoot River',
    'River',
    'Montana',
    'A western Montana river flowing toward the Clark Fork near Missoula, known for scenic recreation and fisheries that include native and introduced trout.',
    'blackfoot-river'
),

(
    'Smith River',
    'River',
    'Montana',
    'A central Montana river famous for a remote 59-mile permitted float section, dramatic scenery, and a productive trout fishery.',
    'smith-river'
),

(
    'Beaverhead River',
    'River',
    'Montana',
    'A southwest Montana river beginning below Clark Canyon Dam near Dillon and known as one of the state''s premier brown trout fisheries.',
    'beaverhead-river'
),

(
    'Clark Fork River',
    'River',
    'Montana',
    'A major western Montana river managed as a wild trout fishery, supporting both native fish and established populations of introduced trout.',
    'clark-fork-river'
),

(
    'Rock Creek',
    'Stream',
    'Montana',
    'A cold-water western Montana stream in the Clark Fork drainage known for wild trout fishing and populations of native and introduced salmonids.',
    'rock-creek'
),

(
    'Belt Creek',
    'Stream',
    'Montana',
    'A central Montana stream supporting cold-water fisheries, including rainbow and brown trout, mountain whitefish, and important westslope cutthroat trout habitat.',
    'belt-creek'
),

(
    'Flathead Lake',
    'Lake',
    'Montana',
    'A large natural lake in northwest Montana surrounded by mountain landscapes and supporting a diverse cold-water fish community including native and introduced species.',
    'flathead-lake'
),

(
    'Georgetown Lake',
    'Lake',
    'Montana',
    'A productive high-elevation lake in western Montana known for rainbow trout, kokanee salmon, brook trout, and year-round recreational fishing.',
    'georgetown-lake'
);


-- ============================================================
-- FISH
--
-- All species below are members of Salmonidae.
-- ============================================================

INSERT INTO Fishes
    (fishFamily, fishSpecies, fishImage)
VALUES
    ('Salmonidae', 'Rainbow Trout', 'rainbow-trout.png'),
    ('Salmonidae', 'Brown Trout', 'brown-trout.png'),
    ('Salmonidae', 'Mountain Whitefish', 'mountain-whitefish.png'),
    ('Salmonidae', 'Westslope Cutthroat Trout', 'westslope-cutthroat-trout.png'),
    ('Salmonidae', 'Brook Trout', 'brook-trout.png'),
    ('Salmonidae', 'Arctic Grayling', 'arctic-grayling.png'),
    ('Salmonidae', 'Lake Trout', 'lake-trout.png'),
    ('Salmonidae', 'Lake Whitefish', 'whitefish.png'),
    ('Salmonidae', 'Kokanee Salmon', 'kokanee-salmon.png'),
    ('Salmonidae', 'Bull Trout', 'bull-trout.png');


-- ============================================================
-- GALLATIN RIVER
--
-- Rainbow Trout
-- Brown Trout
-- Mountain Whitefish
-- Westslope Cutthroat Trout
-- ============================================================

INSERT INTO FishWaterBodies (fishId, waterBodyId)
SELECT f.fishId, w.waterBodyId
FROM Fishes f
CROSS JOIN WaterBodies w
WHERE w.waterBodySlug = 'gallatin-river'
AND f.fishSpecies IN (
    'Rainbow Trout',
    'Brown Trout',
    'Mountain Whitefish',
    'Westslope Cutthroat Trout'
);


-- ============================================================
-- MADISON RIVER
--
-- Rainbow Trout
-- Brown Trout
-- Mountain Whitefish
-- Westslope Cutthroat Trout
-- Arctic Grayling
-- Brook Trout
-- ============================================================

INSERT INTO FishWaterBodies (fishId, waterBodyId)
SELECT f.fishId, w.waterBodyId
FROM Fishes f
CROSS JOIN WaterBodies w
WHERE w.waterBodySlug = 'madison-river'
AND f.fishSpecies IN (
    'Rainbow Trout',
    'Brown Trout',
    'Mountain Whitefish',
    'Westslope Cutthroat Trout',
    'Arctic Grayling',
    'Brook Trout'
);


-- ============================================================
-- BIG HOLE RIVER
--
-- Rainbow Trout
-- Brown Trout
-- Mountain Whitefish
-- Westslope Cutthroat Trout
-- Arctic Grayling
-- Brook Trout
-- ============================================================

INSERT INTO FishWaterBodies (fishId, waterBodyId)
SELECT f.fishId, w.waterBodyId
FROM Fishes f
CROSS JOIN WaterBodies w
WHERE w.waterBodySlug = 'big-hole-river'
AND f.fishSpecies IN (
    'Rainbow Trout',
    'Brown Trout',
    'Mountain Whitefish',
    'Westslope Cutthroat Trout',
    'Arctic Grayling',
    'Brook Trout'
);


-- ============================================================
-- BLACKFOOT RIVER
--
-- Westslope Cutthroat Trout
-- Rainbow Trout
-- Brown Trout
-- Bull Trout
-- ============================================================

INSERT INTO FishWaterBodies (fishId, waterBodyId)
SELECT f.fishId, w.waterBodyId
FROM Fishes f
CROSS JOIN WaterBodies w
WHERE w.waterBodySlug = 'blackfoot-river'
AND f.fishSpecies IN (
    'Westslope Cutthroat Trout',
    'Rainbow Trout',
    'Brown Trout',
    'Bull Trout'
);


-- ============================================================
-- SMITH RIVER
--
-- Rainbow Trout
-- Brown Trout
-- Mountain Whitefish
-- ============================================================

INSERT INTO FishWaterBodies (fishId, waterBodyId)
SELECT f.fishId, w.waterBodyId
FROM Fishes f
CROSS JOIN WaterBodies w
WHERE w.waterBodySlug = 'smith-river'
AND f.fishSpecies IN (
    'Rainbow Trout',
    'Brown Trout',
    'Mountain Whitefish'
);


-- ============================================================
-- BEAVERHEAD RIVER
--
-- Brown Trout
--
-- Kept deliberately conservative here because the source
-- directly characterizes it as a premier brown trout fishery.
-- ============================================================

INSERT INTO FishWaterBodies (fishId, waterBodyId)
SELECT f.fishId, w.waterBodyId
FROM Fishes f
CROSS JOIN WaterBodies w
WHERE w.waterBodySlug = 'beaverhead-river'
AND f.fishSpecies IN (
    'Brown Trout'
);


-- ============================================================
-- CLARK FORK RIVER
--
-- Brown Trout
-- Rainbow Trout
-- Brook Trout
-- Westslope Cutthroat Trout
-- Mountain Whitefish
-- Bull Trout
-- ============================================================

INSERT INTO FishWaterBodies (fishId, waterBodyId)
SELECT f.fishId, w.waterBodyId
FROM Fishes f
CROSS JOIN WaterBodies w
WHERE w.waterBodySlug = 'clark-fork-river'
AND f.fishSpecies IN (
    'Brown Trout',
    'Rainbow Trout',
    'Brook Trout',
    'Westslope Cutthroat Trout',
    'Mountain Whitefish',
    'Bull Trout'
);


-- ============================================================
-- ROCK CREEK
--
-- Brook Trout
-- Brown Trout
-- Mountain Whitefish
-- Rainbow Trout
-- Westslope Cutthroat Trout
-- ============================================================

INSERT INTO FishWaterBodies (fishId, waterBodyId)
SELECT f.fishId, w.waterBodyId
FROM Fishes f
CROSS JOIN WaterBodies w
WHERE w.waterBodySlug = 'rock-creek'
AND f.fishSpecies IN (
    'Brook Trout',
    'Brown Trout',
    'Mountain Whitefish',
    'Rainbow Trout',
    'Westslope Cutthroat Trout'
);


-- ============================================================
-- BELT CREEK
--
-- Rainbow Trout
-- Brown Trout
-- Mountain Whitefish
-- Westslope Cutthroat Trout
-- ============================================================

INSERT INTO FishWaterBodies (fishId, waterBodyId)
SELECT f.fishId, w.waterBodyId
FROM Fishes f
CROSS JOIN WaterBodies w
WHERE w.waterBodySlug = 'belt-creek'
AND f.fishSpecies IN (
    'Rainbow Trout',
    'Brown Trout',
    'Mountain Whitefish',
    'Westslope Cutthroat Trout'
);


-- ============================================================
-- FLATHEAD LAKE
--
-- Lake Trout
-- Lake Whitefish
-- Bull Trout
-- Westslope Cutthroat Trout
-- Mountain Whitefish
-- Rainbow Trout
-- Brook Trout
-- ============================================================

INSERT INTO FishWaterBodies (fishId, waterBodyId)
SELECT f.fishId, w.waterBodyId
FROM Fishes f
CROSS JOIN WaterBodies w
WHERE w.waterBodySlug = 'flathead-lake'
AND f.fishSpecies IN (
    'Lake Trout',
    'Lake Whitefish',
    'Bull Trout',
    'Westslope Cutthroat Trout',
    'Mountain Whitefish',
    'Rainbow Trout',
    'Brook Trout'
);


-- ============================================================
-- GEORGETOWN LAKE
--
-- Rainbow Trout
-- Brook Trout
-- Kokanee Salmon
-- ============================================================

INSERT INTO FishWaterBodies (fishId, waterBodyId)
SELECT f.fishId, w.waterBodyId
FROM Fishes f
CROSS JOIN WaterBodies w
WHERE w.waterBodySlug = 'georgetown-lake'
AND f.fishSpecies IN (
    'Rainbow Trout',
    'Brook Trout',
    'Kokanee Salmon'
);