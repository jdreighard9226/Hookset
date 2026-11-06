USE hookset;

-- 198 default Hookset flies: 73 Dry, 79 Nymph, 46 Streamer.
-- userId = NULL means this is a catalog fly, not a user-created fly.
-- Each fly type uses one shared emblem image (dry.png, nymph.png, streamer.png).
-- Inserts only: does not drop tables or delete existing rows.
--
-- Source of truth: every pattern, color, and size range below comes from a
-- retailer product page, cited in the comment above each group. Checked 2026-11-05.
--   Big Y Fly Co, Best Trout Flies collection:
--     https://bigyflyco.com/collections/best-trout-flies
--   Montana Fly Goods, Flies category:
--     https://montanaflygoods.com/product-category/flies/
--
-- minSize and maxSize are the smallest and largest hook size numbers the shop
-- sells for that color. A bigger number is a smaller hook.
-- Slugs follow FlyServiceImpl.generateFlySlug (lowercase, spaces to hyphens).
-- Slash colors like Black/Orange are written with "and" in the name so the
-- slug never has a slash in it.
INSERT INTO Fly (userId, type, name, color, minSize, maxSize, image, slug)
VALUES
    -- ---------- Dry ----------

    -- Source: https://bigyflyco.com/collections/best-trout-flies/products/elk-hair-caddis
    (NULL, 'Dry', 'Black Elk Hair Caddis', 'Black', 8, 20, 'dry.png', 'black-elk-hair-caddis'),
    (NULL, 'Dry', 'Brown Elk Hair Caddis', 'Brown', 8, 20, 'dry.png', 'brown-elk-hair-caddis'),
    (NULL, 'Dry', 'Chartreuse Elk Hair Caddis', 'Chartreuse', 12, 16, 'dry.png', 'chartreuse-elk-hair-caddis'),
    (NULL, 'Dry', 'Cinnamon Elk Hair Caddis', 'Cinnamon', 12, 20, 'dry.png', 'cinnamon-elk-hair-caddis'),
    (NULL, 'Dry', 'Gray Elk Hair Caddis', 'Gray', 12, 20, 'dry.png', 'gray-elk-hair-caddis'),
    (NULL, 'Dry', 'Olive Elk Hair Caddis', 'Olive', 10, 20, 'dry.png', 'olive-elk-hair-caddis'),
    (NULL, 'Dry', 'Orange Elk Hair Caddis', 'Orange', 12, 18, 'dry.png', 'orange-elk-hair-caddis'),
    (NULL, 'Dry', 'Peacock Elk Hair Caddis', 'Peacock', 8, 20, 'dry.png', 'peacock-elk-hair-caddis'),
    (NULL, 'Dry', 'Tan Elk Hair Caddis', 'Tan', 8, 20, 'dry.png', 'tan-elk-hair-caddis'),
    (NULL, 'Dry', 'Yellow Elk Hair Caddis', 'Yellow', 8, 20, 'dry.png', 'yellow-elk-hair-caddis'),

    -- Source: https://bigyflyco.com/collections/best-trout-flies/products/x-caddis-black
    (NULL, 'Dry', 'Black X-Caddis', 'Black', 12, 18, 'dry.png', 'black-x-caddis'),
    (NULL, 'Dry', 'Olive X-Caddis', 'Olive', 12, 18, 'dry.png', 'olive-x-caddis'),
    (NULL, 'Dry', 'Tan X-Caddis', 'Tan', 12, 18, 'dry.png', 'tan-x-caddis'),

    -- Source: https://bigyflyco.com/collections/best-trout-flies/products/parachute-adams
    -- Note: no color option on the page, Gray is the standard Adams body
    (NULL, 'Dry', 'Parachute Adams', 'Gray', 8, 22, 'dry.png', 'parachute-adams'),

    -- Source: https://bigyflyco.com/collections/best-trout-flies/products/adams
    -- Note: no color option on the page, Gray is the standard Adams body
    (NULL, 'Dry', 'Adams', 'Gray', 8, 24, 'dry.png', 'adams'),

    -- Source: https://bigyflyco.com/collections/best-trout-flies/products/adams-wulff-white-wings
    -- Note: no color option on the page, Gray is the standard Adams body
    (NULL, 'Dry', 'Adams Wulff', 'Gray', 10, 16, 'dry.png', 'adams-wulff'),

    -- Source: https://bigyflyco.com/collections/best-trout-flies/products/comparadun
    (NULL, 'Dry', 'Gray Comparadun', 'Gray', 14, 18, 'dry.png', 'gray-comparadun'),
    (NULL, 'Dry', 'Olive Comparadun', 'Olive', 16, 20, 'dry.png', 'olive-comparadun'),
    (NULL, 'Dry', 'Pale Yellow Comparadun', 'Pale Yellow', 14, 20, 'dry.png', 'pale-yellow-comparadun'),
    (NULL, 'Dry', 'Tan Comparadun', 'Tan', 14, 16, 'dry.png', 'tan-comparadun'),

    -- Source: https://bigyflyco.com/collections/best-trout-flies/products/sparkle-duns-dry-fly
    (NULL, 'Dry', 'Brown Sparkle Dun', 'Brown', 12, 16, 'dry.png', 'brown-sparkle-dun'),
    (NULL, 'Dry', 'Gray Sparkle Dun', 'Gray', 16, 18, 'dry.png', 'gray-sparkle-dun'),
    (NULL, 'Dry', 'Olive Sparkle Dun', 'Olive', 12, 18, 'dry.png', 'olive-sparkle-dun'),
    (NULL, 'Dry', 'Pale Yellow Sparkle Dun', 'Pale Yellow', 12, 20, 'dry.png', 'pale-yellow-sparkle-dun'),
    (NULL, 'Dry', 'Sulphur Orange Sparkle Dun', 'Sulphur Orange', 14, 16, 'dry.png', 'sulphur-orange-sparkle-dun'),
    (NULL, 'Dry', 'Tan Sparkle Dun', 'Tan', 12, 20, 'dry.png', 'tan-sparkle-dun'),

    -- Source: https://bigyflyco.com/collections/best-trout-flies/products/blue-wing-olive
    -- Note: no color option on the page, color taken from the pattern name
    (NULL, 'Dry', 'Blue Wing Olive', 'Olive', 12, 24, 'dry.png', 'blue-wing-olive'),

    -- Source: https://bigyflyco.com/collections/best-trout-flies/products/parachute-blue-wing-olive
    -- Note: no color option on the page, color taken from the pattern name
    (NULL, 'Dry', 'Parachute Blue Wing Olive', 'Olive', 12, 24, 'dry.png', 'parachute-blue-wing-olive'),

    -- Source: https://bigyflyco.com/collections/best-trout-flies/products/pale-morning-dun
    (NULL, 'Dry', 'Pale Green Pale Morning Dun', 'Pale Green', 12, 18, 'dry.png', 'pale-green-pale-morning-dun'),
    (NULL, 'Dry', 'Pale Yellow Pale Morning Dun', 'Pale Yellow', 12, 22, 'dry.png', 'pale-yellow-pale-morning-dun'),
    (NULL, 'Dry', 'Yellow and Olive Pale Morning Dun', 'Yellow/Olive', 12, 18, 'dry.png', 'yellow-and-olive-pale-morning-dun'),

    -- Source: https://bigyflyco.com/collections/best-trout-flies/products/stimulator
    (NULL, 'Dry', 'Black Stimulator', 'Black', 6, 16, 'dry.png', 'black-stimulator'),
    (NULL, 'Dry', 'Chartreuse Stimulator', 'Chartreuse', 8, 16, 'dry.png', 'chartreuse-stimulator'),
    (NULL, 'Dry', 'Olive Stimulator', 'Olive', 6, 16, 'dry.png', 'olive-stimulator'),
    (NULL, 'Dry', 'Orange Stimulator', 'Orange', 6, 16, 'dry.png', 'orange-stimulator'),
    (NULL, 'Dry', 'Purple Stimulator', 'Purple', 6, 16, 'dry.png', 'purple-stimulator'),
    (NULL, 'Dry', 'Red Stimulator', 'Red', 8, 16, 'dry.png', 'red-stimulator'),
    (NULL, 'Dry', 'Royal Stimulator', 'Royal', 6, 16, 'dry.png', 'royal-stimulator'),
    (NULL, 'Dry', 'Tan Stimulator', 'Tan', 10, 16, 'dry.png', 'tan-stimulator'),
    (NULL, 'Dry', 'Yellow Stimulator', 'Yellow', 6, 16, 'dry.png', 'yellow-stimulator'),

    -- Source: https://bigyflyco.com/collections/best-trout-flies/products/chubby-chernobyl
    (NULL, 'Dry', 'Brown Chubby Chernobyl', 'Brown', 6, 12, 'dry.png', 'brown-chubby-chernobyl'),
    (NULL, 'Dry', 'Black and Peacock Chubby Chernobyl', 'Black/Peacock', 6, 12, 'dry.png', 'black-and-peacock-chubby-chernobyl'),
    (NULL, 'Dry', 'Golden Stone Chubby Chernobyl', 'Golden Stone', 6, 12, 'dry.png', 'golden-stone-chubby-chernobyl'),
    (NULL, 'Dry', 'Natural Chubby Chernobyl', 'Natural', 6, 12, 'dry.png', 'natural-chubby-chernobyl'),
    (NULL, 'Dry', 'Olive Chubby Chernobyl', 'Olive', 6, 12, 'dry.png', 'olive-chubby-chernobyl'),
    (NULL, 'Dry', 'Pumpkin Chubby Chernobyl', 'Pumpkin', 8, 12, 'dry.png', 'pumpkin-chubby-chernobyl'),
    (NULL, 'Dry', 'Purple Chubby Chernobyl', 'Purple', 6, 12, 'dry.png', 'purple-chubby-chernobyl'),
    (NULL, 'Dry', 'Salmon Fly Chubby Chernobyl', 'Salmon Fly', 6, 8, 'dry.png', 'salmon-fly-chubby-chernobyl'),
    (NULL, 'Dry', 'Pink Chubby Chernobyl', 'Pink', 8, 8, 'dry.png', 'pink-chubby-chernobyl'),
    (NULL, 'Dry', 'Royal Chubby Chernobyl', 'Royal', 6, 12, 'dry.png', 'royal-chubby-chernobyl'),

    -- Source: https://bigyflyco.com/collections/best-trout-flies/products/yellow-sally
    -- Note: no color option on the page, color taken from the pattern name
    (NULL, 'Dry', 'Yellow Sally', 'Yellow', 10, 18, 'dry.png', 'yellow-sally'),

    -- Source: https://bigyflyco.com/collections/best-trout-flies/products/ant-dry-fly
    (NULL, 'Dry', 'Black Ant', 'Black', 12, 18, 'dry.png', 'black-ant'),
    (NULL, 'Dry', 'Cinnamon Ant', 'Cinnamon', 12, 18, 'dry.png', 'cinnamon-ant'),

    -- Source: https://bigyflyco.com/collections/best-trout-flies/products/foam-beetle
    (NULL, 'Dry', 'Black Foam Beetle', 'Black', 10, 18, 'dry.png', 'black-foam-beetle'),
    (NULL, 'Dry', 'Red Foam Beetle', 'Red', 12, 14, 'dry.png', 'red-foam-beetle'),

    -- Source: https://bigyflyco.com/collections/best-trout-flies/products/daves-hopper
    (NULL, 'Dry', 'Olive Dave''s Hopper', 'Olive', 6, 16, 'dry.png', 'olive-dave''s-hopper'),
    (NULL, 'Dry', 'Yellow Dave''s Hopper', 'Yellow', 6, 16, 'dry.png', 'yellow-dave''s-hopper'),

    -- Source: https://bigyflyco.com/collections/best-trout-flies/products/parachute-hopper
    (NULL, 'Dry', 'Olive Parachute Hopper', 'Olive', 6, 14, 'dry.png', 'olive-parachute-hopper'),
    (NULL, 'Dry', 'Tan Parachute Hopper', 'Tan', 6, 14, 'dry.png', 'tan-parachute-hopper'),
    (NULL, 'Dry', 'Yellow Parachute Hopper', 'Yellow', 6, 14, 'dry.png', 'yellow-parachute-hopper'),

    -- Source: https://bigyflyco.com/collections/best-trout-flies/products/royal-wulff
    -- Note: no color option on the page, color taken from the pattern name
    (NULL, 'Dry', 'Royal Wulff', 'Royal', 8, 20, 'dry.png', 'royal-wulff'),

    -- Source: https://bigyflyco.com/collections/best-trout-flies/products/humpy
    (NULL, 'Dry', 'Adams Humpy', 'Adams', 10, 16, 'dry.png', 'adams-humpy'),
    (NULL, 'Dry', 'Blow Fly Blue Humpy', 'Blow Fly Blue', 12, 12, 'dry.png', 'blow-fly-blue-humpy'),
    (NULL, 'Dry', 'Chartreuse Humpy', 'Chartreuse', 12, 14, 'dry.png', 'chartreuse-humpy'),
    (NULL, 'Dry', 'Orange Humpy', 'Orange', 12, 14, 'dry.png', 'orange-humpy'),
    (NULL, 'Dry', 'Red Humpy', 'Red', 10, 16, 'dry.png', 'red-humpy'),
    (NULL, 'Dry', 'Yellow Humpy', 'Yellow', 10, 16, 'dry.png', 'yellow-humpy'),

    -- Source: https://bigyflyco.com/collections/best-trout-flies/products/griffith-gnat
    -- Note: no color option on the page, Peacock is the standard Griffith Gnat body
    (NULL, 'Dry', 'Griffith Gnat', 'Peacock', 14, 22, 'dry.png', 'griffith-gnat'),

    -- Source: https://montanaflygoods.com/product/purple-para-wulff/
    -- Note: no color option on the page, color taken from the pattern name
    (NULL, 'Dry', 'Purple Parachute Wulff', 'Purple', 12, 18, 'dry.png', 'purple-parachute-wulff'),

    -- Source: https://montanaflygoods.com/product/goddard-caddis/
    (NULL, 'Dry', 'Goddard Caddis', 'Tan', 14, 16, 'dry.png', 'goddard-caddis'),

    -- Source: https://montanaflygoods.com/product/lawsons-green-drake/
    (NULL, 'Dry', 'Lawson''s Green Drake', 'Olive', 12, 12, 'dry.png', 'lawson''s-green-drake'),

    -- Source: https://montanaflygoods.com/product/henryss-fork-salmon-flies/
    -- Note: color comes from the product description, not an option
    (NULL, 'Dry', 'Henry''s Fork Salmon Fly', 'Orange', 4, 6, 'dry.png', 'henry''s-fork-salmon-fly'),

    -- Source: https://montanaflygoods.com/product/parachute-golden-stone/
    -- Note: no color option on the page, color taken from the pattern name
    (NULL, 'Dry', 'Parachute Golden Stone', 'Golden', 10, 12, 'dry.png', 'parachute-golden-stone'),

    -- ---------- Nymph ----------

    -- Source: https://bigyflyco.com/collections/best-trout-flies/products/pheasant-tail
    (NULL, 'Nymph', 'Black Pheasant Tail', 'Black', 12, 22, 'nymph.png', 'black-pheasant-tail'),
    (NULL, 'Nymph', 'Natural Pheasant Tail', 'Natural', 10, 22, 'nymph.png', 'natural-pheasant-tail'),
    (NULL, 'Nymph', 'Olive Pheasant Tail', 'Olive', 12, 22, 'nymph.png', 'olive-pheasant-tail'),

    -- Source: https://bigyflyco.com/collections/best-trout-flies/products/pheasant-tail-beadhead
    (NULL, 'Nymph', 'Black Beadhead Pheasant Tail', 'Black', 10, 22, 'nymph.png', 'black-beadhead-pheasant-tail'),
    (NULL, 'Nymph', 'Natural Beadhead Pheasant Tail', 'Natural', 8, 22, 'nymph.png', 'natural-beadhead-pheasant-tail'),
    (NULL, 'Nymph', 'Olive Beadhead Pheasant Tail', 'Olive', 8, 20, 'nymph.png', 'olive-beadhead-pheasant-tail'),

    -- Source: https://bigyflyco.com/collections/best-trout-flies/products/flashback-pheasant-tail-beadhead
    (NULL, 'Nymph', 'Natural Flashback Beadhead Pheasant Tail', 'Natural', 8, 22, 'nymph.png', 'natural-flashback-beadhead-pheasant-tail'),
    (NULL, 'Nymph', 'Olive Flashback Beadhead Pheasant Tail', 'Olive', 12, 20, 'nymph.png', 'olive-flashback-beadhead-pheasant-tail'),

    -- Source: https://bigyflyco.com/collections/best-trout-flies/products/hot-spot-pheasant-tail
    -- Note: no color option on the page, Natural is how the shop labels its standard pheasant tails
    (NULL, 'Nymph', 'Hot Spot Pheasant Tail', 'Natural', 14, 16, 'nymph.png', 'hot-spot-pheasant-tail'),

    -- Source: https://bigyflyco.com/collections/best-trout-flies/products/hares-ear
    (NULL, 'Nymph', 'Black Hare''s Ear', 'Black', 10, 18, 'nymph.png', 'black-hare''s-ear'),
    (NULL, 'Nymph', 'Natural Hare''s Ear', 'Natural', 8, 22, 'nymph.png', 'natural-hare''s-ear'),
    (NULL, 'Nymph', 'Olive Hare''s Ear', 'Olive', 10, 20, 'nymph.png', 'olive-hare''s-ear'),

    -- Source: https://bigyflyco.com/collections/best-trout-flies/products/hares-ear-beadhead
    (NULL, 'Nymph', 'Black Beadhead Hare''s Ear', 'Black', 10, 20, 'nymph.png', 'black-beadhead-hare''s-ear'),
    (NULL, 'Nymph', 'Natural Beadhead Hare''s Ear', 'Natural', 10, 20, 'nymph.png', 'natural-beadhead-hare''s-ear'),
    (NULL, 'Nymph', 'Olive Beadhead Hare''s Ear', 'Olive', 12, 18, 'nymph.png', 'olive-beadhead-hare''s-ear'),
    (NULL, 'Nymph', 'Purple Beadhead Hare''s Ear', 'Purple', 10, 16, 'nymph.png', 'purple-beadhead-hare''s-ear'),

    -- Source: https://bigyflyco.com/collections/best-trout-flies/products/flashback-hares-ear
    (NULL, 'Nymph', 'Black Flashback Hare''s Ear', 'Black', 10, 16, 'nymph.png', 'black-flashback-hare''s-ear'),
    (NULL, 'Nymph', 'Natural Flashback Hare''s Ear', 'Natural', 10, 20, 'nymph.png', 'natural-flashback-hare''s-ear'),
    (NULL, 'Nymph', 'Olive Flashback Hare''s Ear', 'Olive', 10, 14, 'nymph.png', 'olive-flashback-hare''s-ear'),

    -- Source: https://bigyflyco.com/collections/best-trout-flies/products/zebra-midge-beadhead
    (NULL, 'Nymph', 'Black Beadhead Zebra Midge', 'Black', 16, 20, 'nymph.png', 'black-beadhead-zebra-midge'),
    (NULL, 'Nymph', 'Olive Beadhead Zebra Midge', 'Olive', 16, 20, 'nymph.png', 'olive-beadhead-zebra-midge'),
    (NULL, 'Nymph', 'Purple Beadhead Zebra Midge', 'Purple', 16, 22, 'nymph.png', 'purple-beadhead-zebra-midge'),
    (NULL, 'Nymph', 'Red Beadhead Zebra Midge', 'Red', 16, 20, 'nymph.png', 'red-beadhead-zebra-midge'),
    (NULL, 'Nymph', 'Rust Beadhead Zebra Midge', 'Rust', 16, 22, 'nymph.png', 'rust-beadhead-zebra-midge'),

    -- Source: https://bigyflyco.com/collections/best-trout-flies/products/copper-john
    (NULL, 'Nymph', 'Copper Copper John', 'Copper', 10, 22, 'nymph.png', 'copper-copper-john'),
    (NULL, 'Nymph', 'Green Copper John', 'Green', 12, 20, 'nymph.png', 'green-copper-john'),
    (NULL, 'Nymph', 'Red Copper John', 'Red', 12, 20, 'nymph.png', 'red-copper-john'),

    -- Source: https://bigyflyco.com/collections/best-trout-flies/products/brassie
    (NULL, 'Nymph', 'Copper Brassie', 'Copper', 14, 22, 'nymph.png', 'copper-brassie'),
    (NULL, 'Nymph', 'Green Brassie', 'Green', 18, 18, 'nymph.png', 'green-brassie'),
    (NULL, 'Nymph', 'Red Brassie', 'Red', 16, 20, 'nymph.png', 'red-brassie'),

    -- Source: https://bigyflyco.com/collections/best-trout-flies/products/perdigon-nymph-tungsten-barbless
    (NULL, 'Nymph', 'Black Tungsten Perdigon', 'Black', 12, 16, 'nymph.png', 'black-tungsten-perdigon'),
    (NULL, 'Nymph', 'Brown Tungsten Perdigon', 'Brown', 12, 16, 'nymph.png', 'brown-tungsten-perdigon'),
    (NULL, 'Nymph', 'Olive Tungsten Perdigon', 'Olive', 14, 16, 'nymph.png', 'olive-tungsten-perdigon'),
    (NULL, 'Nymph', 'Pink Tungsten Perdigon', 'Pink', 14, 16, 'nymph.png', 'pink-tungsten-perdigon'),
    (NULL, 'Nymph', 'Lavender Pearl Tungsten Perdigon', 'Lavender Pearl', 12, 16, 'nymph.png', 'lavender-pearl-tungsten-perdigon'),
    (NULL, 'Nymph', 'Red Quill Tungsten Perdigon', 'Red Quill', 12, 16, 'nymph.png', 'red-quill-tungsten-perdigon'),

    -- Source: https://bigyflyco.com/collections/best-trout-flies/products/frenchie-nymph-tungsten
    (NULL, 'Nymph', 'Chartreuse Tungsten Frenchie', 'Chartreuse', 10, 16, 'nymph.png', 'chartreuse-tungsten-frenchie'),
    (NULL, 'Nymph', 'Pink Tungsten Frenchie', 'Pink', 10, 18, 'nymph.png', 'pink-tungsten-frenchie'),
    (NULL, 'Nymph', 'Shrimp Orange Tungsten Frenchie', 'Shrimp Orange', 10, 16, 'nymph.png', 'shrimp-orange-tungsten-frenchie'),

    -- Source: https://bigyflyco.com/collections/best-trout-flies/products/pats-rubber-legs
    (NULL, 'Nymph', 'Black Pat''s Rubber Legs', 'Black', 6, 10, 'nymph.png', 'black-pat''s-rubber-legs'),
    (NULL, 'Nymph', 'Black and Brown Pat''s Rubber Legs', 'Black/Brown', 6, 8, 'nymph.png', 'black-and-brown-pat''s-rubber-legs'),
    (NULL, 'Nymph', 'Black and Yellow Pat''s Rubber Legs', 'Black/Yellow', 8, 10, 'nymph.png', 'black-and-yellow-pat''s-rubber-legs'),
    (NULL, 'Nymph', 'Brown Pat''s Rubber Legs', 'Brown', 6, 10, 'nymph.png', 'brown-pat''s-rubber-legs'),
    (NULL, 'Nymph', 'Brown and Orange Pat''s Rubber Legs', 'Brown/Orange', 6, 8, 'nymph.png', 'brown-and-orange-pat''s-rubber-legs'),
    (NULL, 'Nymph', 'Coffee Pat''s Rubber Legs', 'Coffee', 10, 10, 'nymph.png', 'coffee-pat''s-rubber-legs'),
    (NULL, 'Nymph', 'Olive and Black Pat''s Rubber Legs', 'Olive/Black', 6, 10, 'nymph.png', 'olive-and-black-pat''s-rubber-legs'),
    (NULL, 'Nymph', 'Olive and Brown Pat''s Rubber Legs', 'Olive/Brown', 10, 10, 'nymph.png', 'olive-and-brown-pat''s-rubber-legs'),
    (NULL, 'Nymph', 'Peacock Pat''s Rubber Legs', 'Peacock', 6, 10, 'nymph.png', 'peacock-pat''s-rubber-legs'),

    -- Source: https://bigyflyco.com/collections/best-trout-flies/products/kaufmann-stonefly-nymph
    (NULL, 'Nymph', 'Black Kaufmann Stonefly Nymph', 'Black', 4, 14, 'nymph.png', 'black-kaufmann-stonefly-nymph'),
    (NULL, 'Nymph', 'Brown Kaufmann Stonefly Nymph', 'Brown', 4, 14, 'nymph.png', 'brown-kaufmann-stonefly-nymph'),
    (NULL, 'Nymph', 'Golden Kaufmann Stonefly Nymph', 'Golden', 12, 12, 'nymph.png', 'golden-kaufmann-stonefly-nymph'),

    -- Source: https://bigyflyco.com/collections/best-trout-flies/products/caddis-pupa
    (NULL, 'Nymph', 'Brown Caddis Pupa', 'Brown', 12, 18, 'nymph.png', 'brown-caddis-pupa'),
    (NULL, 'Nymph', 'Green Caddis Pupa', 'Green', 12, 16, 'nymph.png', 'green-caddis-pupa'),
    (NULL, 'Nymph', 'Tan Caddis Pupa', 'Tan', 12, 18, 'nymph.png', 'tan-caddis-pupa'),

    -- Source: https://bigyflyco.com/collections/best-trout-flies/products/walt-worm-tungsten
    -- Note: no color option on the page, Natural is the standard hare's ear dubbing
    (NULL, 'Nymph', 'Tungsten Walt Worm', 'Natural', 6, 16, 'nymph.png', 'tungsten-walt-worm'),

    -- Source: https://bigyflyco.com/collections/best-trout-flies/products/san-juan-worm
    (NULL, 'Nymph', 'Brown San Juan Worm', 'Brown', 8, 16, 'nymph.png', 'brown-san-juan-worm'),
    (NULL, 'Nymph', 'Orange San Juan Worm', 'Orange', 12, 12, 'nymph.png', 'orange-san-juan-worm'),
    (NULL, 'Nymph', 'Pink San Juan Worm', 'Pink', 10, 16, 'nymph.png', 'pink-san-juan-worm'),
    (NULL, 'Nymph', 'Purple San Juan Worm', 'Purple', 14, 14, 'nymph.png', 'purple-san-juan-worm'),
    (NULL, 'Nymph', 'Red San Juan Worm', 'Red', 8, 16, 'nymph.png', 'red-san-juan-worm'),

    -- Source: https://bigyflyco.com/collections/best-trout-flies/products/glo-bug
    (NULL, 'Nymph', 'Blood Red Glo Bug', 'Blood Red', 6, 14, 'nymph.png', 'blood-red-glo-bug'),
    (NULL, 'Nymph', 'Chartreuse Glo Bug', 'Chartreuse', 6, 14, 'nymph.png', 'chartreuse-glo-bug'),
    (NULL, 'Nymph', 'Dark Roe Glo Bug', 'Dark Roe', 6, 16, 'nymph.png', 'dark-roe-glo-bug'),
    (NULL, 'Nymph', 'Light Roe Glo Bug', 'Light Roe', 6, 16, 'nymph.png', 'light-roe-glo-bug'),
    (NULL, 'Nymph', 'Oregon Cheese Glo Bug', 'Oregon Cheese', 6, 16, 'nymph.png', 'oregon-cheese-glo-bug'),
    (NULL, 'Nymph', 'Peachy King Glo Bug', 'Peachy King', 6, 16, 'nymph.png', 'peachy-king-glo-bug'),
    (NULL, 'Nymph', 'Salmon Egg Glo Bug', 'Salmon Egg', 6, 16, 'nymph.png', 'salmon-egg-glo-bug'),
    (NULL, 'Nymph', 'Shrimp Pink Glo Bug', 'Shrimp Pink', 6, 16, 'nymph.png', 'shrimp-pink-glo-bug'),
    (NULL, 'Nymph', 'White Glo Bug', 'White', 12, 16, 'nymph.png', 'white-glo-bug'),

    -- Source: https://bigyflyco.com/collections/best-trout-flies/products/wd-40
    (NULL, 'Nymph', 'Black WD-40', 'Black', 16, 22, 'nymph.png', 'black-wd-40'),
    (NULL, 'Nymph', 'Chocolate WD-40', 'Chocolate', 18, 22, 'nymph.png', 'chocolate-wd-40'),
    (NULL, 'Nymph', 'Gray WD-40', 'Gray', 18, 22, 'nymph.png', 'gray-wd-40'),
    (NULL, 'Nymph', 'Olive WD-40', 'Olive', 16, 22, 'nymph.png', 'olive-wd-40'),
    (NULL, 'Nymph', 'Tan WD-40', 'Tan', 18, 22, 'nymph.png', 'tan-wd-40'),

    -- Source: https://bigyflyco.com/collections/best-trout-flies/products/soft-hackle-beadhead
    (NULL, 'Nymph', 'Black Beadhead Soft Hackle', 'Black', 14, 14, 'nymph.png', 'black-beadhead-soft-hackle'),
    (NULL, 'Nymph', 'Green Beadhead Soft Hackle', 'Green', 12, 20, 'nymph.png', 'green-beadhead-soft-hackle'),
    (NULL, 'Nymph', 'Orange Beadhead Soft Hackle', 'Orange', 12, 16, 'nymph.png', 'orange-beadhead-soft-hackle'),
    (NULL, 'Nymph', 'Yellow Beadhead Soft Hackle', 'Yellow', 14, 16, 'nymph.png', 'yellow-beadhead-soft-hackle'),

    -- Source: https://montanaflygoods.com/product/bead-head-prince-nymphs/
    -- Note: no color option on the page, Peacock is the standard Prince Nymph body
    (NULL, 'Nymph', 'Beadhead Prince Nymph', 'Peacock', 8, 16, 'nymph.png', 'beadhead-prince-nymph'),

    -- ---------- Streamer ----------

    -- Source: https://bigyflyco.com/collections/best-trout-flies/products/woolly-bugger
    (NULL, 'Streamer', 'Black Woolly Bugger', 'Black', 6, 14, 'streamer.png', 'black-woolly-bugger'),
    (NULL, 'Streamer', 'Brown Woolly Bugger', 'Brown', 4, 12, 'streamer.png', 'brown-woolly-bugger'),
    (NULL, 'Streamer', 'Chartreuse Woolly Bugger', 'Chartreuse', 6, 12, 'streamer.png', 'chartreuse-woolly-bugger'),
    (NULL, 'Streamer', 'Olive Woolly Bugger', 'Olive', 4, 12, 'streamer.png', 'olive-woolly-bugger'),
    (NULL, 'Streamer', 'Purple Woolly Bugger', 'Purple', 6, 10, 'streamer.png', 'purple-woolly-bugger'),
    (NULL, 'Streamer', 'White Woolly Bugger', 'White', 4, 12, 'streamer.png', 'white-woolly-bugger'),
    (NULL, 'Streamer', 'Yellow Woolly Bugger', 'Yellow', 8, 10, 'streamer.png', 'yellow-woolly-bugger'),

    -- Source: https://bigyflyco.com/collections/best-trout-flies/products/woolly-bugger-beadhead
    (NULL, 'Streamer', 'Black Beadhead Woolly Bugger', 'Black', 2, 14, 'streamer.png', 'black-beadhead-woolly-bugger'),
    (NULL, 'Streamer', 'Black and Orange Beadhead Woolly Bugger', 'Black/Orange', 6, 12, 'streamer.png', 'black-and-orange-beadhead-woolly-bugger'),
    (NULL, 'Streamer', 'Brown Beadhead Woolly Bugger', 'Brown', 6, 12, 'streamer.png', 'brown-beadhead-woolly-bugger'),
    (NULL, 'Streamer', 'Chartreuse Beadhead Woolly Bugger', 'Chartreuse', 6, 12, 'streamer.png', 'chartreuse-beadhead-woolly-bugger'),
    (NULL, 'Streamer', 'Olive Beadhead Woolly Bugger', 'Olive', 2, 14, 'streamer.png', 'olive-beadhead-woolly-bugger'),
    (NULL, 'Streamer', 'Olive and Black Beadhead Woolly Bugger', 'Olive/Black', 2, 14, 'streamer.png', 'olive-and-black-beadhead-woolly-bugger'),
    (NULL, 'Streamer', 'Pink Beadhead Woolly Bugger', 'Pink', 6, 12, 'streamer.png', 'pink-beadhead-woolly-bugger'),
    (NULL, 'Streamer', 'Purple Beadhead Woolly Bugger', 'Purple', 6, 12, 'streamer.png', 'purple-beadhead-woolly-bugger'),
    (NULL, 'Streamer', 'White Beadhead Woolly Bugger', 'White', 4, 12, 'streamer.png', 'white-beadhead-woolly-bugger'),
    (NULL, 'Streamer', 'Yellow Beadhead Woolly Bugger', 'Yellow', 6, 12, 'streamer.png', 'yellow-beadhead-woolly-bugger'),

    -- Source: https://bigyflyco.com/collections/best-trout-flies/products/sculpin
    (NULL, 'Streamer', 'Black Sculpin', 'Black', 2, 8, 'streamer.png', 'black-sculpin'),
    (NULL, 'Streamer', 'Brown Sculpin', 'Brown', 6, 8, 'streamer.png', 'brown-sculpin'),
    (NULL, 'Streamer', 'Olive Sculpin', 'Olive', 2, 8, 'streamer.png', 'olive-sculpin'),

    -- Source: https://bigyflyco.com/collections/best-trout-flies/products/zonker
    (NULL, 'Streamer', 'Black Zonker', 'Black', 2, 10, 'streamer.png', 'black-zonker'),
    (NULL, 'Streamer', 'Grizzly Zonker', 'Grizzly', 2, 10, 'streamer.png', 'grizzly-zonker'),
    (NULL, 'Streamer', 'Olive Zonker', 'Olive', 6, 10, 'streamer.png', 'olive-zonker'),
    (NULL, 'Streamer', 'White Zonker', 'White', 2, 10, 'streamer.png', 'white-zonker'),

    -- Source: https://bigyflyco.com/collections/best-trout-flies/products/bunny-leech-beadhead
    (NULL, 'Streamer', 'Black Beadhead Bunny Leech', 'Black', 6, 8, 'streamer.png', 'black-beadhead-bunny-leech'),
    (NULL, 'Streamer', 'Brown Beadhead Bunny Leech', 'Brown', 6, 8, 'streamer.png', 'brown-beadhead-bunny-leech'),
    (NULL, 'Streamer', 'Ginger Beadhead Bunny Leech', 'Ginger', 6, 8, 'streamer.png', 'ginger-beadhead-bunny-leech'),
    (NULL, 'Streamer', 'Olive Beadhead Bunny Leech', 'Olive', 6, 8, 'streamer.png', 'olive-beadhead-bunny-leech'),
    (NULL, 'Streamer', 'Pink Beadhead Bunny Leech', 'Pink', 6, 8, 'streamer.png', 'pink-beadhead-bunny-leech'),
    (NULL, 'Streamer', 'Purple Beadhead Bunny Leech', 'Purple', 6, 8, 'streamer.png', 'purple-beadhead-bunny-leech'),

    -- Source: https://bigyflyco.com/collections/best-trout-flies/products/trophy-dungeon
    (NULL, 'Streamer', 'Bighorn Trophy Dungeon', 'Bighorn', 4, 4, 'streamer.png', 'bighorn-trophy-dungeon'),
    (NULL, 'Streamer', 'Black Trophy Dungeon', 'Black', 4, 8, 'streamer.png', 'black-trophy-dungeon'),
    (NULL, 'Streamer', 'Black and Olive Trophy Dungeon', 'Black/Olive', 4, 4, 'streamer.png', 'black-and-olive-trophy-dungeon'),
    (NULL, 'Streamer', 'Olive Trophy Dungeon', 'Olive', 4, 8, 'streamer.png', 'olive-trophy-dungeon'),
    (NULL, 'Streamer', 'Olive and Yellow Trophy Dungeon', 'Olive/Yellow', 4, 4, 'streamer.png', 'olive-and-yellow-trophy-dungeon'),
    (NULL, 'Streamer', 'Rusty and Red Trophy Dungeon', 'Rusty/Red', 4, 8, 'streamer.png', 'rusty-and-red-trophy-dungeon'),
    (NULL, 'Streamer', 'Tan and Natural Trophy Dungeon', 'Tan/Natural', 4, 4, 'streamer.png', 'tan-and-natural-trophy-dungeon'),
    (NULL, 'Streamer', 'White and Natural Trophy Dungeon', 'White/Natural', 4, 4, 'streamer.png', 'white-and-natural-trophy-dungeon'),
    (NULL, 'Streamer', 'White and Red Trophy Dungeon', 'White/Red', 4, 8, 'streamer.png', 'white-and-red-trophy-dungeon'),

    -- Source: https://bigyflyco.com/collections/best-trout-flies/products/clouser-minnow
    (NULL, 'Streamer', 'Black Yellow and Chart Clouser Minnow', 'Black/Yellow/Chart', 2, 6, 'streamer.png', 'black-yellow-and-chart-clouser-minnow'),
    (NULL, 'Streamer', 'Chart and White Clouser Minnow', 'Chart/White', 2, 6, 'streamer.png', 'chart-and-white-clouser-minnow'),
    (NULL, 'Streamer', 'Olive and White Clouser Minnow', 'Olive/White', 2, 6, 'streamer.png', 'olive-and-white-clouser-minnow'),
    (NULL, 'Streamer', 'Pink and White Clouser Minnow', 'Pink/White', 2, 6, 'streamer.png', 'pink-and-white-clouser-minnow'),
    (NULL, 'Streamer', 'Red and White Clouser Minnow', 'Red/White', 2, 6, 'streamer.png', 'red-and-white-clouser-minnow'),

    -- Source: https://bigyflyco.com/collections/best-trout-flies/products/muddler-minnow
    (NULL, 'Streamer', 'Natural Muddler Minnow', 'Natural', 2, 12, 'streamer.png', 'natural-muddler-minnow'),
    (NULL, 'Streamer', 'Olive Muddler Minnow', 'Olive', 4, 10, 'streamer.png', 'olive-muddler-minnow');