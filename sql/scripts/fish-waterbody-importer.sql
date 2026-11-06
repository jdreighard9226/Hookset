use hookset;

-- ============================================================
-- SOURCES
--
-- Rivers and the fish linked to them come from the Fly Guys N Lies
-- Montana river pages, checked 2026-11-05:
--   https://flyguysnlies.com/state/montana-fly-fishing/
-- Each river's section pages are cited above its fish links. Rivers the
-- site splits into sections are one water body here, and their fish are
-- every species listed in any of their sections.
--
-- Only the species in each page's Fish Species list are linked. Fish that
-- are only mentioned in a page's regulations or overview are left out.
--
-- Belt Creek, Flathead Lake, and Georgetown Lake are not on that site.
-- Their sources are other pages, cited above each one's fish links.
-- ============================================================

-- ============================================================
-- WATER BODIES
-- ============================================================

INSERT INTO WaterBody
    (name, type, state, description, slug)
VALUES

(
    'Beaverhead River',
    'River',
    'Montana',
    'A southwest Montana river beginning below Clark Canyon Dam near Dillon and known as one of the state''s premier brown trout fisheries.',
    'beaverhead-river'
),

(
    'Belt Creek',
    'Stream',
    'Montana',
    'A central Montana stream that winds through the Sluice Boxes State Park canyon southeast of Great Falls, supporting rainbow, brown, and brook trout and important westslope cutthroat trout habitat.',
    'belt-creek'
),

(
    'Big Hole River',
    'River',
    'Montana',
    'A scenic southwest Montana river flowing through mountain valleys and agricultural country, recognized for its wild trout fishery and important native fish habitat.',
    'big-hole-river'
),

(
    'Big Spring Creek',
    'Stream',
    'Montana',
    'A spring-fed central Montana creek near Lewistown with clear pools, gentle riffles, and weedy runs that flows through ranchland and town under willows and cottonwoods.',
    'big-spring-creek'
),

(
    'Bighorn River',
    'River',
    'Montana',
    'A large southeast Montana tailwater below the Yellowtail Afterbay near Fort Smith, with riffles, deep runs, gravel bars, and pools running through open ranchland.',
    'bighorn-river'
),

(
    'Bitterroot River',
    'River',
    'Montana',
    'A western Montana freestone river that runs north from below the Bitterroot Mountains near Darby to Missoula, mixing meadow riffles, boulder pocketwater, deep runs, and pools.',
    'bitterroot-river'
),

(
    'Blackfoot River',
    'River',
    'Montana',
    'A western Montana river flowing toward the Clark Fork near Missoula, known for scenic recreation and fisheries that include native and introduced trout.',
    'blackfoot-river'
),

(
    'Boulder River (Big Timber)',
    'River',
    'Montana',
    'A south-central Montana river that drops out of the Absaroka Mountains and runs through a valley to the Yellowstone near Big Timber, alternating riffles, runs, and pools lined with willows and cottonwoods.',
    'boulder-river-big-timber'
),

(
    'Boulder River (Boulder)',
    'River',
    'Montana',
    'A southwest Montana freestone river near the town of Boulder in the Jefferson drainage, with riffles, boulder pockets, and pools, some shaded by trees.',
    'boulder-river-boulder'
),

(
    'Clark Fork River',
    'River',
    'Montana',
    'A major western Montana river managed as a wild trout fishery, supporting both native fish and established populations of introduced trout.',
    'clark-fork-river'
),

(
    'Dearborn River',
    'River',
    'Montana',
    'A rugged central Montana river that cuts a volcanic canyon before joining the Missouri near Craig, with varied pools, runs, and riffles.',
    'dearborn-river'
),

(
    'East Gallatin River',
    'River',
    'Montana',
    'A small southwest Montana river that winds through Gallatin Valley farmland near Belgrade, where trout hold along grassy banks and current edges.',
    'east-gallatin-river'
),

(
    'Flathead Lake',
    'Lake',
    'Montana',
    'A large natural lake in northwest Montana surrounded by mountain landscapes and supporting a diverse cold-water fish community including native and introduced species.',
    'flathead-lake'
),

(
    'Flathead River',
    'River',
    'Montana',
    'A large northwest Montana river formed where the North and Middle Forks meet, flowing past Kalispell into Flathead Lake and leaving the lake through a short gorge below Polson.',
    'flathead-river'
),

(
    'Gallatin River',
    'River',
    'Montana',
    'A major southwest Montana trout river that begins near Yellowstone National Park and flows through mountain valleys before joining the Missouri River headwaters system.',
    'gallatin-river'
),

(
    'Gardner River',
    'River',
    'Montana',
    'A short river that flows out of Yellowstone National Park through a rugged gorge and joins the Yellowstone River at Gardiner, with boulder pockets and pools.',
    'gardner-river'
),

(
    'Georgetown Lake',
    'Lake',
    'Montana',
    'A productive high-elevation lake in western Montana known for rainbow trout, kokanee salmon, brook trout, and year-round recreational fishing.',
    'georgetown-lake'
),

(
    'Jefferson River',
    'River',
    'Montana',
    'A southwest Montana river that starts where the Beaverhead and Big Hole meet near Twin Bridges and winds through ranchland and cottonwoods to Three Forks.',
    'jefferson-river'
),

(
    'Kootenai River',
    'River',
    'Montana',
    'A big northwest Montana tailwater below Libby Dam with about 40 floatable miles, known for dry-fly fishing and wild trout.',
    'kootenai-river'
),

(
    'Madison River',
    'River',
    'Montana',
    'An iconic southwest Montana trout river formed by the Firehole and Gibbon rivers near Yellowstone National Park and known for its productive wild trout fishery.',
    'madison-river'
),

(
    'Marias River',
    'River',
    'Montana',
    'A north-central Montana prairie river below Tiber Dam whose trout depend on cold releases from the reservoir, with deep, slow pools.',
    'marias-river'
),

(
    'Middle Fork Flathead River',
    'River',
    'Montana',
    'A freestone river that forms part of the southern boundary of Glacier National Park near West Glacier and is known for native westslope cutthroat trout.',
    'middle-fork-flathead-river'
),

(
    'Mill Creek',
    'Stream',
    'Montana',
    'A mountain freestone creek that drains the Absaroka Range into the Yellowstone River in Paradise Valley, with fast riffles, pocket water, and log-formed pools.',
    'mill-creek'
),

(
    'Missouri River',
    'River',
    'Montana',
    'A large tailwater in west-central Montana that runs through a narrow canyon below Hauser Dam and then through canyons and ranchland below Holter Dam toward Cascade.',
    'missouri-river'
),

(
    'Musselshell River',
    'River',
    'Montana',
    'A narrow, winding central Montana river near Martinsdale that runs through grassland and hay meadows, with undercut banks, riffles, and runs.',
    'musselshell-river'
),

(
    'North Fork Flathead River',
    'River',
    'Montana',
    'A river that winds through timbered country along the west edge of Glacier National Park toward Columbia Falls, with fast shallows, deep channels, sandbars, and pools.',
    'north-fork-flathead-river'
),

(
    'Poindexter Slough',
    'Stream',
    'Montana',
    'A spring-creek-like channel south of Dillon, fed by groundwater and Beaverhead water, that winds through a hayfield valley with willow-lined runs and riffles into deeper pools.',
    'poindexter-slough'
),

(
    'Red Rock River',
    'River',
    'Montana',
    'A southwest Montana meadow river below Lima Reservoir that winds through sagebrush and willow country, with flows shaped by reservoir releases and irrigation.',
    'red-rock-river'
),

(
    'Rock Creek',
    'Stream',
    'Montana',
    'A cold-water western Montana stream in the Clark Fork drainage known for wild trout fishing and populations of native and introduced salmonids.',
    'rock-creek'
),

(
    'Ruby River',
    'River',
    'Montana',
    'A southwest Montana river below Ruby Dam that winds through farmland near Alder toward Twin Bridges, with flows and clarity shaped by reservoir releases and irrigation.',
    'ruby-river'
),

(
    'Shields River',
    'River',
    'Montana',
    'A south-central Montana river that drains farmland and tree-lined country before joining the Yellowstone near Livingston, with riffles, deeper runs, and rock-formed pools.',
    'shields-river'
),

(
    'Smith River',
    'River',
    'Montana',
    'A central Montana river famous for a remote 59-mile permitted float section, dramatic scenery, and a productive trout fishery.',
    'smith-river'
),

(
    'South Fork Flathead River',
    'River',
    'Montana',
    'A northwest Montana freestone river that flows through the Bob Marshall Wilderness to Hungry Horse Reservoir, with a short tailwater gorge below Hungry Horse Dam.',
    'south-fork-flathead-river'
),

(
    'Stillwater River',
    'River',
    'Montana',
    'A south-central Montana river above Absarokee that runs through canyon walls, cottonwoods, and farmland, with fast riffles, boulder pockets, and deep pools.',
    'stillwater-river'
),

(
    'Sun River',
    'River',
    'Montana',
    'A central Montana river near Simms that flows through farmland edged with cottonwoods and willows over a mixed gravel, cobble, and sand bottom.',
    'sun-river'
),

(
    'Swan River',
    'River',
    'Montana',
    'A northwest Montana river that slows into an impoundment above the dam at Bigfork, then drops through a steep, rocky canyon below it.',
    'swan-river'
),

(
    'West Fork Bitterroot River',
    'River',
    'Montana',
    'A western Montana river below Painted Rocks Dam near Conner that runs through willow, cottonwood, and conifer country, with pockets, riffles, and deeper pools.',
    'west-fork-bitterroot-river'
),

(
    'Yaak River',
    'River',
    'Montana',
    'A shaded, heavily wooded freestone river in far northwest Montana that runs between the town of Yaak and Yaak Falls in the Kootenai drainage.',
    'yaak-river'
),

(
    'Yellowstone River',
    'River',
    'Montana',
    'Montana''s longest undammed river, flowing out of Yellowstone National Park through Paradise Valley and past Livingston, Big Timber, and Columbus to Billings, changing from a mountain freestone river to a broad cottonwood-lined valley river.',
    'yellowstone-river'
);

-- ============================================================
-- FISH
--
-- Only game fish people fish for. Families:
--   Salmonidae: Rainbow Trout, Brown Trout, Mountain Whitefish, Westslope Cutthroat Trout, Brook Trout, Arctic Grayling, Lake Trout, Lake Whitefish, Kokanee Salmon, Bull Trout, Yellowstone Cutthroat Trout
--   Esocidae: Northern Pike
--   Centrarchidae: Smallmouth Bass
--
-- New in this version, these need images in static/images/fish:
--   yellowstone-cutthroat-trout.png (Yellowstone Cutthroat Trout)
--   northern-pike.png (Northern Pike)
--   smallmouth-bass.png (Smallmouth Bass)
-- ============================================================

INSERT INTO Fish
    (family, species, image)
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
    ('Salmonidae', 'Bull Trout', 'bull-trout.png'),
    ('Salmonidae', 'Yellowstone Cutthroat Trout', 'yellowstone-cutthroat-trout.png'),
    ('Esocidae', 'Northern Pike', 'northern-pike.png'),
    ('Centrarchidae', 'Smallmouth Bass', 'smallmouth-bass.png');

-- ============================================================
-- BEAVERHEAD RIVER
--
-- Source:
--   https://flyguysnlies.com/rivers/fly-fishing-beaverhead-river-twin-bridges/
--
-- Brown Trout
-- Rainbow Trout
-- Mountain Whitefish
-- ============================================================

INSERT INTO FishWaterBody (fishId, waterBodyId)
SELECT f.id, w.id
FROM Fish f
CROSS JOIN WaterBody w
WHERE w.slug = 'beaverhead-river'
AND f.species IN (
    'Brown Trout',
    'Rainbow Trout',
    'Mountain Whitefish'
);

-- ============================================================
-- BELT CREEK
--
-- Source:
--   https://genuinemontana.com/?p=610
--   https://en.wikipedia.org/wiki/Belt_Creek_(Montana)
--   https://theelectricgf.com/2022/01/14/fwp-seeking-public-comment-on-proposals-to-increase-trout-in-belt-creek-watershed-plant-trees-in-giant-springs-state-park/
--
-- Note: Genuine Montana lists rainbow, cutthroat, brook, and brown trout. Wikipedia, citing Montana FWP, lists brook, brown, and rainbow trout as introduced. The Electric reports FWP's westslope cutthroat work in the Belt Creek watershed. No source names mountain whitefish, so it was removed, and brook trout was added.
--
-- Rainbow Trout
-- Brown Trout
-- Brook Trout
-- Westslope Cutthroat Trout
-- ============================================================

INSERT INTO FishWaterBody (fishId, waterBodyId)
SELECT f.id, w.id
FROM Fish f
CROSS JOIN WaterBody w
WHERE w.slug = 'belt-creek'
AND f.species IN (
    'Rainbow Trout',
    'Brown Trout',
    'Brook Trout',
    'Westslope Cutthroat Trout'
);

-- ============================================================
-- BIG HOLE RIVER
--
-- Source:
--   https://flyguysnlies.com/rivers/fly-fishing-big-hole-river-wise-river/
--   https://flyguysnlies.com/rivers/fly-fishing-big-hole-river-divide-to-melrose/
--   https://flyguysnlies.com/rivers/fly-fishing-big-hole-river-melrose-to-twin-bridges/
--
-- Note: The Wise River page lists plain "Cutthroat Trout". Mapped to Westslope Cutthroat Trout, the native cutthroat of the upper Missouri drainage.
--
-- Brown Trout
-- Rainbow Trout
-- Mountain Whitefish
-- Arctic Grayling
-- Westslope Cutthroat Trout
-- ============================================================

INSERT INTO FishWaterBody (fishId, waterBodyId)
SELECT f.id, w.id
FROM Fish f
CROSS JOIN WaterBody w
WHERE w.slug = 'big-hole-river'
AND f.species IN (
    'Brown Trout',
    'Rainbow Trout',
    'Mountain Whitefish',
    'Arctic Grayling',
    'Westslope Cutthroat Trout'
);

-- ============================================================
-- BIG SPRING CREEK
--
-- Source:
--   https://flyguysnlies.com/rivers/fly-fishing-big-spring-creek-lewistown/
--
-- Brown Trout
-- Rainbow Trout
-- Mountain Whitefish
-- ============================================================

INSERT INTO FishWaterBody (fishId, waterBodyId)
SELECT f.id, w.id
FROM Fish f
CROSS JOIN WaterBody w
WHERE w.slug = 'big-spring-creek'
AND f.species IN (
    'Brown Trout',
    'Rainbow Trout',
    'Mountain Whitefish'
);

-- ============================================================
-- BIGHORN RIVER
--
-- Source:
--   https://flyguysnlies.com/rivers/fly-fishing-bighorn-river-below-yellowtail-afterbay/
--
-- Brown Trout
-- Rainbow Trout
-- Mountain Whitefish
-- ============================================================

INSERT INTO FishWaterBody (fishId, waterBodyId)
SELECT f.id, w.id
FROM Fish f
CROSS JOIN WaterBody w
WHERE w.slug = 'bighorn-river'
AND f.species IN (
    'Brown Trout',
    'Rainbow Trout',
    'Mountain Whitefish'
);

-- ============================================================
-- BITTERROOT RIVER
--
-- Source:
--   https://flyguysnlies.com/rivers/fly-fishing-bitterroot-river-darby/
--   https://flyguysnlies.com/rivers/fly-fishing-bitterroot-river-missoula/
--
-- Brown Trout
-- Rainbow Trout
-- Westslope Cutthroat Trout
-- Mountain Whitefish
-- ============================================================

INSERT INTO FishWaterBody (fishId, waterBodyId)
SELECT f.id, w.id
FROM Fish f
CROSS JOIN WaterBody w
WHERE w.slug = 'bitterroot-river'
AND f.species IN (
    'Brown Trout',
    'Rainbow Trout',
    'Westslope Cutthroat Trout',
    'Mountain Whitefish'
);

-- ============================================================
-- BLACKFOOT RIVER
--
-- Source:
--   https://flyguysnlies.com/rivers/fly-fishing-blackfoot-river-bonner/
--
-- Westslope Cutthroat Trout
-- Rainbow Trout
-- Brown Trout
-- Bull Trout
-- Brook Trout
-- Mountain Whitefish
-- ============================================================

INSERT INTO FishWaterBody (fishId, waterBodyId)
SELECT f.id, w.id
FROM Fish f
CROSS JOIN WaterBody w
WHERE w.slug = 'blackfoot-river'
AND f.species IN (
    'Westslope Cutthroat Trout',
    'Rainbow Trout',
    'Brown Trout',
    'Bull Trout',
    'Brook Trout',
    'Mountain Whitefish'
);

-- ============================================================
-- BOULDER RIVER (BIG TIMBER)
--
-- Source:
--   https://flyguysnlies.com/rivers/fly-fishing-boulder-river-big-timber/
--
-- Brown Trout
-- Rainbow Trout
-- Mountain Whitefish
-- ============================================================

INSERT INTO FishWaterBody (fishId, waterBodyId)
SELECT f.id, w.id
FROM Fish f
CROSS JOIN WaterBody w
WHERE w.slug = 'boulder-river-big-timber'
AND f.species IN (
    'Brown Trout',
    'Rainbow Trout',
    'Mountain Whitefish'
);

-- ============================================================
-- BOULDER RIVER (BOULDER)
--
-- Source:
--   https://flyguysnlies.com/rivers/fly-fishing-boulder-river-boulder/
--
-- Brown Trout
-- Rainbow Trout
-- Mountain Whitefish
-- ============================================================

INSERT INTO FishWaterBody (fishId, waterBodyId)
SELECT f.id, w.id
FROM Fish f
CROSS JOIN WaterBody w
WHERE w.slug = 'boulder-river-boulder'
AND f.species IN (
    'Brown Trout',
    'Rainbow Trout',
    'Mountain Whitefish'
);

-- ============================================================
-- CLARK FORK RIVER
--
-- Source:
--   https://flyguysnlies.com/rivers/fly-fishing-clark-fork-drummond/
--   https://flyguysnlies.com/rivers/fly-fishing-clark-fork-missoula/
--
-- Brown Trout
-- Rainbow Trout
-- Westslope Cutthroat Trout
-- Mountain Whitefish
-- ============================================================

INSERT INTO FishWaterBody (fishId, waterBodyId)
SELECT f.id, w.id
FROM Fish f
CROSS JOIN WaterBody w
WHERE w.slug = 'clark-fork-river'
AND f.species IN (
    'Brown Trout',
    'Rainbow Trout',
    'Westslope Cutthroat Trout',
    'Mountain Whitefish'
);

-- ============================================================
-- DEARBORN RIVER
--
-- Source:
--   https://flyguysnlies.com/rivers/fly-fishing-dearborn-river-craig/
--
-- Brown Trout
-- Rainbow Trout
-- Westslope Cutthroat Trout
-- Brook Trout
-- Mountain Whitefish
-- ============================================================

INSERT INTO FishWaterBody (fishId, waterBodyId)
SELECT f.id, w.id
FROM Fish f
CROSS JOIN WaterBody w
WHERE w.slug = 'dearborn-river'
AND f.species IN (
    'Brown Trout',
    'Rainbow Trout',
    'Westslope Cutthroat Trout',
    'Brook Trout',
    'Mountain Whitefish'
);

-- ============================================================
-- EAST GALLATIN RIVER
--
-- Source:
--   https://flyguysnlies.com/rivers/fly-fishing-east-gallatin-river-belgrade/
--
-- Brown Trout
-- Rainbow Trout
-- Mountain Whitefish
-- ============================================================

INSERT INTO FishWaterBody (fishId, waterBodyId)
SELECT f.id, w.id
FROM Fish f
CROSS JOIN WaterBody w
WHERE w.slug = 'east-gallatin-river'
AND f.species IN (
    'Brown Trout',
    'Rainbow Trout',
    'Mountain Whitefish'
);

-- ============================================================
-- FLATHEAD LAKE
--
-- Source:
--   https://flbs.umt.edu/newflbs/flathead-lake/lake-facts/
--
-- Lake Trout
-- Lake Whitefish
-- Bull Trout
-- Westslope Cutthroat Trout
-- Mountain Whitefish
-- Rainbow Trout
-- Brook Trout
-- ============================================================

INSERT INTO FishWaterBody (fishId, waterBodyId)
SELECT f.id, w.id
FROM Fish f
CROSS JOIN WaterBody w
WHERE w.slug = 'flathead-lake'
AND f.species IN (
    'Lake Trout',
    'Lake Whitefish',
    'Bull Trout',
    'Westslope Cutthroat Trout',
    'Mountain Whitefish',
    'Rainbow Trout',
    'Brook Trout'
);

-- ============================================================
-- FLATHEAD RIVER
--
-- Source:
--   https://flyguysnlies.com/rivers/fly-fishing-flathead-river-kalispell/
--   https://flyguysnlies.com/rivers/fly-fishing-flathead-river-polson/
--
-- Rainbow Trout
-- Westslope Cutthroat Trout
-- Brown Trout
-- Mountain Whitefish
-- Northern Pike
-- ============================================================

INSERT INTO FishWaterBody (fishId, waterBodyId)
SELECT f.id, w.id
FROM Fish f
CROSS JOIN WaterBody w
WHERE w.slug = 'flathead-river'
AND f.species IN (
    'Rainbow Trout',
    'Westslope Cutthroat Trout',
    'Brown Trout',
    'Mountain Whitefish',
    'Northern Pike'
);

-- ============================================================
-- GALLATIN RIVER
--
-- Source:
--   https://flyguysnlies.com/rivers/fly-fishing-gallatin-river-big-sky/
--   https://flyguysnlies.com/rivers/fly-fishing-gallatin-river-gallatin-gateway/
--
-- Note: Both pages also list plain "Cutthroat Trout" next to Yellowstone Cutthroat Trout, so only the specific one is linked.
--
-- Brown Trout
-- Rainbow Trout
-- Yellowstone Cutthroat Trout
-- Mountain Whitefish
-- ============================================================

INSERT INTO FishWaterBody (fishId, waterBodyId)
SELECT f.id, w.id
FROM Fish f
CROSS JOIN WaterBody w
WHERE w.slug = 'gallatin-river'
AND f.species IN (
    'Brown Trout',
    'Rainbow Trout',
    'Yellowstone Cutthroat Trout',
    'Mountain Whitefish'
);

-- ============================================================
-- GARDNER RIVER
--
-- Source:
--   https://flyguysnlies.com/rivers/fly-fishing-gardner-river-gardiner/
--
-- Rainbow Trout
-- Brown Trout
-- Yellowstone Cutthroat Trout
-- Mountain Whitefish
-- ============================================================

INSERT INTO FishWaterBody (fishId, waterBodyId)
SELECT f.id, w.id
FROM Fish f
CROSS JOIN WaterBody w
WHERE w.slug = 'gardner-river'
AND f.species IN (
    'Rainbow Trout',
    'Brown Trout',
    'Yellowstone Cutthroat Trout',
    'Mountain Whitefish'
);

-- ============================================================
-- GEORGETOWN LAKE
--
-- Source:
--   https://blackfootriver.com/blogs/fishing-reports/georgetown-lake-fishing-report
--
-- Rainbow Trout
-- Brook Trout
-- Kokanee Salmon
-- ============================================================

INSERT INTO FishWaterBody (fishId, waterBodyId)
SELECT f.id, w.id
FROM Fish f
CROSS JOIN WaterBody w
WHERE w.slug = 'georgetown-lake'
AND f.species IN (
    'Rainbow Trout',
    'Brook Trout',
    'Kokanee Salmon'
);

-- ============================================================
-- JEFFERSON RIVER
--
-- Source:
--   https://flyguysnlies.com/rivers/fly-fishing-jefferson-river-twin-bridges/
--   https://flyguysnlies.com/rivers/fly-fishing-jefferson-river-three-forks/
--
-- Brown Trout
-- Rainbow Trout
-- Mountain Whitefish
-- ============================================================

INSERT INTO FishWaterBody (fishId, waterBodyId)
SELECT f.id, w.id
FROM Fish f
CROSS JOIN WaterBody w
WHERE w.slug = 'jefferson-river'
AND f.species IN (
    'Brown Trout',
    'Rainbow Trout',
    'Mountain Whitefish'
);

-- ============================================================
-- KOOTENAI RIVER
--
-- Source:
--   https://flyguysnlies.com/rivers/fly-fishing-kootenai-river-below-libby-dam/
--
-- Rainbow Trout
-- Westslope Cutthroat Trout
-- Bull Trout
-- Brown Trout
-- Mountain Whitefish
-- ============================================================

INSERT INTO FishWaterBody (fishId, waterBodyId)
SELECT f.id, w.id
FROM Fish f
CROSS JOIN WaterBody w
WHERE w.slug = 'kootenai-river'
AND f.species IN (
    'Rainbow Trout',
    'Westslope Cutthroat Trout',
    'Bull Trout',
    'Brown Trout',
    'Mountain Whitefish'
);

-- ============================================================
-- MADISON RIVER
--
-- Source:
--   https://flyguysnlies.com/rivers/fly-fishing-madison-river-upper-madison-river/
--   https://flyguysnlies.com/rivers/fly-fishing-madison-river-between-the-lakes/
--   https://flyguysnlies.com/rivers/fly-fishing-madison-river-ennis/
--   https://flyguysnlies.com/rivers/fly-fishing-madison-river-bear-trap-canyon/
--   https://flyguysnlies.com/rivers/fly-fishing-madison-river-lower-madison-river/
--
-- Brown Trout
-- Rainbow Trout
-- Mountain Whitefish
-- ============================================================

INSERT INTO FishWaterBody (fishId, waterBodyId)
SELECT f.id, w.id
FROM Fish f
CROSS JOIN WaterBody w
WHERE w.slug = 'madison-river'
AND f.species IN (
    'Brown Trout',
    'Rainbow Trout',
    'Mountain Whitefish'
);

-- ============================================================
-- MARIAS RIVER
--
-- Source:
--   https://flyguysnlies.com/rivers/fly-fishing-marias-river-below-tiber-dam/
--
-- Brown Trout
-- Rainbow Trout
-- Mountain Whitefish
-- ============================================================

INSERT INTO FishWaterBody (fishId, waterBodyId)
SELECT f.id, w.id
FROM Fish f
CROSS JOIN WaterBody w
WHERE w.slug = 'marias-river'
AND f.species IN (
    'Brown Trout',
    'Rainbow Trout',
    'Mountain Whitefish'
);

-- ============================================================
-- MIDDLE FORK FLATHEAD RIVER
--
-- Source:
--   https://flyguysnlies.com/rivers/fly-fishing-middle-fork-flathead-river-west-glacier/
--
-- Westslope Cutthroat Trout
-- Bull Trout
-- Mountain Whitefish
-- ============================================================

INSERT INTO FishWaterBody (fishId, waterBodyId)
SELECT f.id, w.id
FROM Fish f
CROSS JOIN WaterBody w
WHERE w.slug = 'middle-fork-flathead-river'
AND f.species IN (
    'Westslope Cutthroat Trout',
    'Bull Trout',
    'Mountain Whitefish'
);

-- ============================================================
-- MILL CREEK
--
-- Source:
--   https://flyguysnlies.com/rivers/fly-fishing-mill-creek-paradise-valley/
--
-- Yellowstone Cutthroat Trout
-- Rainbow Trout
-- Brown Trout
-- Brook Trout
-- Mountain Whitefish
-- ============================================================

INSERT INTO FishWaterBody (fishId, waterBodyId)
SELECT f.id, w.id
FROM Fish f
CROSS JOIN WaterBody w
WHERE w.slug = 'mill-creek'
AND f.species IN (
    'Yellowstone Cutthroat Trout',
    'Rainbow Trout',
    'Brown Trout',
    'Brook Trout',
    'Mountain Whitefish'
);

-- ============================================================
-- MISSOURI RIVER
--
-- Source:
--   https://flyguysnlies.com/rivers/fly-fishing-missouri-river-land-of-giants/
--   https://flyguysnlies.com/rivers/fly-fishing-missouri-river-below-holter-dam/
--
-- Rainbow Trout
-- Brown Trout
-- Mountain Whitefish
-- ============================================================

INSERT INTO FishWaterBody (fishId, waterBodyId)
SELECT f.id, w.id
FROM Fish f
CROSS JOIN WaterBody w
WHERE w.slug = 'missouri-river'
AND f.species IN (
    'Rainbow Trout',
    'Brown Trout',
    'Mountain Whitefish'
);

-- ============================================================
-- MUSSELSHELL RIVER
--
-- Source:
--   https://flyguysnlies.com/rivers/fly-fishing-musselshell-river-martinsdale/
--
-- Note: The page lists plain "Cutthroat Trout". Mapped to Westslope Cutthroat Trout, the native cutthroat of the Missouri drainage. Check this one.
--
-- Brown Trout
-- Rainbow Trout
-- Brook Trout
-- Westslope Cutthroat Trout
-- Mountain Whitefish
-- ============================================================

INSERT INTO FishWaterBody (fishId, waterBodyId)
SELECT f.id, w.id
FROM Fish f
CROSS JOIN WaterBody w
WHERE w.slug = 'musselshell-river'
AND f.species IN (
    'Brown Trout',
    'Rainbow Trout',
    'Brook Trout',
    'Westslope Cutthroat Trout',
    'Mountain Whitefish'
);

-- ============================================================
-- NORTH FORK FLATHEAD RIVER
--
-- Source:
--   https://flyguysnlies.com/rivers/fly-fishing-north-fork-flathead-river-columbia-falls/
--
-- Westslope Cutthroat Trout
-- Rainbow Trout
-- Bull Trout
-- Mountain Whitefish
-- ============================================================

INSERT INTO FishWaterBody (fishId, waterBodyId)
SELECT f.id, w.id
FROM Fish f
CROSS JOIN WaterBody w
WHERE w.slug = 'north-fork-flathead-river'
AND f.species IN (
    'Westslope Cutthroat Trout',
    'Rainbow Trout',
    'Bull Trout',
    'Mountain Whitefish'
);

-- ============================================================
-- POINDEXTER SLOUGH
--
-- Source:
--   https://flyguysnlies.com/rivers/fly-fishing-poindexter-slough-dillon/
--
-- Brown Trout
-- Rainbow Trout
-- Mountain Whitefish
-- ============================================================

INSERT INTO FishWaterBody (fishId, waterBodyId)
SELECT f.id, w.id
FROM Fish f
CROSS JOIN WaterBody w
WHERE w.slug = 'poindexter-slough'
AND f.species IN (
    'Brown Trout',
    'Rainbow Trout',
    'Mountain Whitefish'
);

-- ============================================================
-- RED ROCK RIVER
--
-- Source:
--   https://flyguysnlies.com/rivers/fly-fishing-red-rock-river-below-lima-reservoir/
--
-- Brown Trout
-- Rainbow Trout
-- Mountain Whitefish
-- ============================================================

INSERT INTO FishWaterBody (fishId, waterBodyId)
SELECT f.id, w.id
FROM Fish f
CROSS JOIN WaterBody w
WHERE w.slug = 'red-rock-river'
AND f.species IN (
    'Brown Trout',
    'Rainbow Trout',
    'Mountain Whitefish'
);

-- ============================================================
-- ROCK CREEK
--
-- Source:
--   https://flyguysnlies.com/rivers/fly-fishing-rock-creek-clinton/
--
-- Brook Trout
-- Brown Trout
-- Bull Trout
-- Mountain Whitefish
-- Rainbow Trout
-- Westslope Cutthroat Trout
-- ============================================================

INSERT INTO FishWaterBody (fishId, waterBodyId)
SELECT f.id, w.id
FROM Fish f
CROSS JOIN WaterBody w
WHERE w.slug = 'rock-creek'
AND f.species IN (
    'Brook Trout',
    'Brown Trout',
    'Bull Trout',
    'Mountain Whitefish',
    'Rainbow Trout',
    'Westslope Cutthroat Trout'
);

-- ============================================================
-- RUBY RIVER
--
-- Source:
--   https://flyguysnlies.com/rivers/fly-fishing-ruby-river-alder/
--
-- Brown Trout
-- Rainbow Trout
-- Mountain Whitefish
-- ============================================================

INSERT INTO FishWaterBody (fishId, waterBodyId)
SELECT f.id, w.id
FROM Fish f
CROSS JOIN WaterBody w
WHERE w.slug = 'ruby-river'
AND f.species IN (
    'Brown Trout',
    'Rainbow Trout',
    'Mountain Whitefish'
);

-- ============================================================
-- SHIELDS RIVER
--
-- Source:
--   https://flyguysnlies.com/rivers/fly-fishing-shields-river-livingston/
--
-- Brown Trout
-- Rainbow Trout
-- Mountain Whitefish
-- Smallmouth Bass
-- ============================================================

INSERT INTO FishWaterBody (fishId, waterBodyId)
SELECT f.id, w.id
FROM Fish f
CROSS JOIN WaterBody w
WHERE w.slug = 'shields-river'
AND f.species IN (
    'Brown Trout',
    'Rainbow Trout',
    'Mountain Whitefish',
    'Smallmouth Bass'
);

-- ============================================================
-- SMITH RIVER
--
-- Source:
--   https://flyguysnlies.com/rivers/fly-fishing-smith-river/
--
-- Rainbow Trout
-- Brown Trout
-- Westslope Cutthroat Trout
-- Mountain Whitefish
-- ============================================================

INSERT INTO FishWaterBody (fishId, waterBodyId)
SELECT f.id, w.id
FROM Fish f
CROSS JOIN WaterBody w
WHERE w.slug = 'smith-river'
AND f.species IN (
    'Rainbow Trout',
    'Brown Trout',
    'Westslope Cutthroat Trout',
    'Mountain Whitefish'
);

-- ============================================================
-- SOUTH FORK FLATHEAD RIVER
--
-- Source:
--   https://flyguysnlies.com/rivers/fly-fishing-south-fork-flathead-river-bob-marshall/
--   https://flyguysnlies.com/rivers/fly-fishing-south-fork-flathead-river-hungry-horse/
--
-- Westslope Cutthroat Trout
-- Bull Trout
-- Mountain Whitefish
-- ============================================================

INSERT INTO FishWaterBody (fishId, waterBodyId)
SELECT f.id, w.id
FROM Fish f
CROSS JOIN WaterBody w
WHERE w.slug = 'south-fork-flathead-river'
AND f.species IN (
    'Westslope Cutthroat Trout',
    'Bull Trout',
    'Mountain Whitefish'
);

-- ============================================================
-- STILLWATER RIVER
--
-- Source:
--   https://flyguysnlies.com/rivers/fly-fishing-stillwater-river-absarokee/
--
-- Rainbow Trout
-- Brown Trout
-- Yellowstone Cutthroat Trout
-- Mountain Whitefish
-- ============================================================

INSERT INTO FishWaterBody (fishId, waterBodyId)
SELECT f.id, w.id
FROM Fish f
CROSS JOIN WaterBody w
WHERE w.slug = 'stillwater-river'
AND f.species IN (
    'Rainbow Trout',
    'Brown Trout',
    'Yellowstone Cutthroat Trout',
    'Mountain Whitefish'
);

-- ============================================================
-- SUN RIVER
--
-- Source:
--   https://flyguysnlies.com/rivers/fly-fishing-sun-river-simms/
--
-- Brown Trout
-- Rainbow Trout
-- Westslope Cutthroat Trout
-- Mountain Whitefish
-- ============================================================

INSERT INTO FishWaterBody (fishId, waterBodyId)
SELECT f.id, w.id
FROM Fish f
CROSS JOIN WaterBody w
WHERE w.slug = 'sun-river'
AND f.species IN (
    'Brown Trout',
    'Rainbow Trout',
    'Westslope Cutthroat Trout',
    'Mountain Whitefish'
);

-- ============================================================
-- SWAN RIVER
--
-- Source:
--   https://flyguysnlies.com/rivers/fly-fishing-swan-river-bigfork/
--
-- Northern Pike
-- Rainbow Trout
-- Westslope Cutthroat Trout
-- Bull Trout
-- Lake Trout
-- Mountain Whitefish
-- ============================================================

INSERT INTO FishWaterBody (fishId, waterBodyId)
SELECT f.id, w.id
FROM Fish f
CROSS JOIN WaterBody w
WHERE w.slug = 'swan-river'
AND f.species IN (
    'Northern Pike',
    'Rainbow Trout',
    'Westslope Cutthroat Trout',
    'Bull Trout',
    'Lake Trout',
    'Mountain Whitefish'
);

-- ============================================================
-- WEST FORK BITTERROOT RIVER
--
-- Source:
--   https://flyguysnlies.com/rivers/fly-fishing-west-fork-bitterroot-river-conner/
--
-- Westslope Cutthroat Trout
-- Rainbow Trout
-- Brown Trout
-- Brook Trout
-- Bull Trout
-- Mountain Whitefish
-- ============================================================

INSERT INTO FishWaterBody (fishId, waterBodyId)
SELECT f.id, w.id
FROM Fish f
CROSS JOIN WaterBody w
WHERE w.slug = 'west-fork-bitterroot-river'
AND f.species IN (
    'Westslope Cutthroat Trout',
    'Rainbow Trout',
    'Brown Trout',
    'Brook Trout',
    'Bull Trout',
    'Mountain Whitefish'
);

-- ============================================================
-- YAAK RIVER
--
-- Source:
--   https://flyguysnlies.com/rivers/fly-fishing-yaak-river-sylvanite/
--
-- Westslope Cutthroat Trout
-- Rainbow Trout
-- Brook Trout
-- Mountain Whitefish
-- ============================================================

INSERT INTO FishWaterBody (fishId, waterBodyId)
SELECT f.id, w.id
FROM Fish f
CROSS JOIN WaterBody w
WHERE w.slug = 'yaak-river'
AND f.species IN (
    'Westslope Cutthroat Trout',
    'Rainbow Trout',
    'Brook Trout',
    'Mountain Whitefish'
);

-- ============================================================
-- YELLOWSTONE RIVER
--
-- Source:
--   https://flyguysnlies.com/rivers/fly-fishing-yellowstone-river-paradise-valley/
--   https://flyguysnlies.com/rivers/fly-fishing-yellowstone-river-livingston/
--   https://flyguysnlies.com/rivers/fly-fishing-yellowstone-river-big-timber-to-columbus/
--   https://flyguysnlies.com/rivers/fly-fishing-yellowstone-river-columbus-to-billings/
--
-- Brown Trout
-- Rainbow Trout
-- Yellowstone Cutthroat Trout
-- Mountain Whitefish
-- Smallmouth Bass
-- ============================================================

INSERT INTO FishWaterBody (fishId, waterBodyId)
SELECT f.id, w.id
FROM Fish f
CROSS JOIN WaterBody w
WHERE w.slug = 'yellowstone-river'
AND f.species IN (
    'Brown Trout',
    'Rainbow Trout',
    'Yellowstone Cutthroat Trout',
    'Mountain Whitefish',
    'Smallmouth Bass'
);