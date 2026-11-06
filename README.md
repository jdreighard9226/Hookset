# Hookset

Hookset is a fly fishing web app for Montana waters. Users can browse fish, water bodies, and flies, keep their own flies, and log the fish they catch.

## What Hookset does

- **Accounts.** Users sign up, log in, and log out. Passwords are hashed with BCrypt. Users can change their password on the profile page. The home, login, and signup pages are public. Every other page requires a logged-in user.
- **Flies.** Hookset comes with a catalog of 198 default flies (dries, nymphs, and streamers). Users can add, edit, and delete their own flies. The fly list can be filtered to show Hookset flies, your flies, or all flies. Each fly has a details page.
- **Fish.** A page listing the fish species in the app.
- **Water bodies.** A list of Montana rivers, streams, and lakes. Each one has a details page showing which fish live there.
- **Catches.** Users log a catch with the water body, fish, fly, length, date, and notes. When you pick a water body, the fish list only shows fish found in that water. Users can view all catches, view just their own, see catch details, and edit their catches.

Bad input on any form is caught and the page shows a message explaining what went wrong.

## Running Hookset

### What you need

| Tool | Version | Notes |
| --- | --- | --- |
| JDK | 25 | Hookset was built with Oracle JDK 25 (25.0.4.1 LTS). Any JDK 25 works, such as Eclipse Temurin 25. Gradle's toolchain in `build.gradle` asks for Java 25, so the build fails if no JDK 25 is installed. |
| MySQL Server | 8.0.46 | Hookset was built and tested with MySQL 8.0.46. Homebrew and apt install the latest 8.0 release, which works the same. |
| MySQL Workbench | 8.0 | Optional. Only needed if you'd rather run the SQL scripts in a GUI. |
| Git | Any recent version | Used to clone the repo. |
| Gradle | None | The Gradle wrapper is included in the repo. Nothing to install. |

### 1. Install JDK 25

**Windows**

1. Download the JDK 25 Windows x64 Installer (`.msi`) from <https://www.oracle.com/java/technologies/downloads/>.
2. Run the installer.
3. Open a new PowerShell window and check it:

   ```powershell
   java -version
   ```

   The first line should start with `java 25`.

**Mac**

1. Download the JDK 25 macOS installer (`.dmg`) from <https://www.oracle.com/java/technologies/downloads/>. Pick ARM64 for Apple Silicon Macs or x64 for Intel Macs.
2. Run the installer.
3. Open a new Terminal window and check it:

   ```bash
   java -version
   ```

**Linux (Ubuntu/Debian)**

1. Download the JDK 25 Linux x64 Debian package (`.deb`) from <https://www.oracle.com/java/technologies/downloads/>.
2. Install it:

   ```bash
   sudo apt install ./jdk-25_linux-x64_bin.deb
   ```

3. Check it:

   ```bash
   java -version
   ```

If `java -version` shows an older version, another JDK is first on your `PATH`. Gradle can still find JDK 25 on its own, but the check above won't show it.

### 2. Install MySQL 8.0.46

**Windows**

1. Go to the MySQL archive at <https://downloads.mysql.com/archives/community/>, set Product Version to 8.0.46 and Operating System to Microsoft Windows, then download the Windows (x86, 64-bit) MSI Installer and run it.
2. Install MySQL Server 8.0.46. If you want the GUI, download MySQL Workbench separately from <https://dev.mysql.com/downloads/workbench/>.
3. Set a root password when the installer asks. You'll need it in the next steps.
4. Add MySQL to your `PATH` so the `mysql` command works in PowerShell. The installer doesn't do this. Run this once, then open a new PowerShell window:

   ```powershell
   [Environment]::SetEnvironmentVariable("Path", $env:Path + ";C:\Program Files\MySQL\MySQL Server 8.0\bin", "User")
   ```

5. Check it:

   ```powershell
   mysql --version
   ```

**Mac**

With [Homebrew](https://brew.sh):

```bash
brew install mysql@8.0
brew services start mysql@8.0
echo 'export PATH="$(brew --prefix mysql@8.0)/bin:$PATH"' >> ~/.zshrc
source ~/.zshrc
mysql --version
```

Homebrew's MySQL starts with no root password. Set one:

```bash
mysql_secure_installation
```

Homebrew installs the latest 8.0 release. To get exactly 8.0.46, use the macOS installer (`.dmg`) instead: go to <https://downloads.mysql.com/archives/community/>, set Product Version to 8.0.46 and Operating System to macOS, and pick ARM64 for Apple Silicon or x86 for Intel.

**Linux (Ubuntu/Debian)**

```bash
sudo apt update
sudo apt install mysql-server
sudo systemctl start mysql
mysql --version
```

apt installs the 8.0 release your Ubuntu version ships with, so `mysql --version` may not say exactly 8.0.46. To get exactly 8.0.46, download the packages from <https://downloads.mysql.com/archives/community/> with Product Version set to 8.0.46.

On Ubuntu, root logs in with `sudo mysql` instead of a password.

### 3. Clone the repo

Windows (PowerShell), Mac, and Linux use the same commands:

```bash
git clone https://github.com/jdreighard9226/Hookset.git
cd Hookset
```

On Mac and Linux, make the Gradle wrapper runnable:

```bash
chmod +x gradlew
```

### 4. Create the database and app user

Hibernate is set to `ddl-auto=validate`. It checks the tables but does not create them, so the database has to be set up before the app starts.

The app connects as `hooksetapp` to the `hookset` database on `localhost`, port 3306 (`spring.datasource.url=jdbc:mysql://localhost/hookset`). The password must match `spring.datasource.password` in `src/main/resources/application.properties`. If you use a different password, change it in both places.

Log in to MySQL as root:

```bash
mysql -u root -p
```

On Ubuntu, use `sudo mysql` instead.

Then run:

```sql
CREATE DATABASE hookset;
CREATE USER 'hooksetapp'@'localhost' IDENTIFIED BY '<password from application.properties>';
GRANT ALL PRIVILEGES ON hookset.* TO 'hooksetapp'@'localhost';
EXIT;
```

### 5. Run the SQL scripts

Run the scripts in `sql/scripts` in this order. Each one starts with `USE hookset;`, so they go into the right database on their own.

| Order | Script | What it does |
| --- | --- | --- |
| 1 | `hooksetTables.sql` | Drops and recreates every table. Running it again wipes all data. |
| 2 | `fish-waterbody-importer.sql` | Adds the water bodies, fish species, and which fish live in which water. |
| 3 | `hookset_flies_with_slugs.sql` | Adds the 198 default flies. |

Run these from the `Hookset` folder. Each command asks for the `hooksetapp` password.

**Windows (PowerShell)**

PowerShell doesn't support `<` for reading a file into a command, so these use MySQL's `source` command:

```powershell
mysql -u hooksetapp -p -e "source sql/scripts/hooksetTables.sql"
mysql -u hooksetapp -p -e "source sql/scripts/fish-waterbody-importer.sql"
mysql -u hooksetapp -p -e "source sql/scripts/hookset_flies_with_slugs.sql"
```

**Mac and Linux**

```bash
mysql -u hooksetapp -p < sql/scripts/hooksetTables.sql
mysql -u hooksetapp -p < sql/scripts/fish-waterbody-importer.sql
mysql -u hooksetapp -p < sql/scripts/hookset_flies_with_slugs.sql
```

**MySQL Workbench**

Connect as `hooksetapp`. For each script in order, go to File > Open SQL Script, open it, and click the lightning bolt to run the whole thing.

Check that the data loaded:

```sql
USE hookset;
SELECT COUNT(*) FROM Fly;
SELECT COUNT(*) FROM WaterBody;
SELECT COUNT(*) FROM Fish;
```

You should get 198 flies, 39 water bodies, and 13 fish.

The database diagram is in `sql/hookset-erd.png`.

### 6. Start the app

From the `Hookset` folder:

**Windows (PowerShell)**

```powershell
.\gradlew.bat bootRun
```

**Mac and Linux**

```bash
./gradlew bootRun
```

The first run downloads Gradle and the project's dependencies, so it takes a few minutes. When the log shows `Started HooksetApplication`, open <http://localhost:8080/> and sign up for an account.

Stop the app with `Ctrl+C`.

### 7. Run the tests

**Windows (PowerShell)**

```powershell
.\gradlew.bat test
```

**Mac and Linux**

```bash
./gradlew test
```

The tests don't use MySQL. The `test` profile (`src/test/resources/application-test.properties`) runs them against an in-memory H2 database that's built from `hooksetTables.sql` and starts with no data. Each test runs in a transaction that gets rolled back, so tests don't affect each other.

The test report is written to `build/reports/tests/test/index.html`.

### Logs

Logs print to the console and are written to `logs/spring-boot-logger.log`. Logging is configured in `src/main/resources/logback-spring.xml`.

### Troubleshooting

| Problem | Fix |
| --- | --- |
| `mysql` is not recognized (Windows) | MySQL's `bin` folder isn't on your `PATH`. Run the `PATH` command from the Windows part of step 2, then open a new PowerShell window. |
| `Permission denied` running `./gradlew` | Run `chmod +x gradlew`. |
| `No matching toolchains found` | JDK 25 isn't installed. Install it from step 1. |
| `Access denied for user 'hooksetapp'@'localhost'` | The password in `application.properties` doesn't match the one you used in `CREATE USER`. |
| `Schema-validation: missing table` | The SQL scripts haven't been run. Run them in order from step 5. |
| `Communications link failure` | MySQL isn't running. Start it from Services (Windows), `brew services start mysql@8.0` (Mac), or `sudo systemctl start mysql` (Linux). |

## References
### Fish Images

The fish illustrations used in Hookset were AI-generated using fish identification resources, photographs, and user-provided reference images. The sources below were used to guide or check the appearance of each species, including its body shape, coloration, spots, fins, and head structure.

All completed fish images are 1254 × 1254 pixel PNG files with transparent backgrounds. The original photographs remain the property of their respective owners.

| Fish Species | Image Filename | Reference Sources |
|---|---|---|
| Rainbow Trout | `rainbow-trout.png` | [Fly Fisherman](https://www.flyfisherman.com/editorial/all-about-rainbow-trout/454217) (featured illustration credited to Duane Raver/USFWS), [How To Catch Any Fish](https://www.howtocatchanyfish.com/rainbow-trout) |
| Brown Trout | `brown-trout.png` | [How To Catch Any Fish](https://www.howtocatchanyfish.com/brown-trout), [Grizzly Hackle](https://grizzlyhackle.com/blogs/fly-fishing/brown-trout-in-montana-chasing-the-one) (article by Aidan Morton) |
| Brook Trout | `brook-trout.png` | [Michigan DNR](https://www.michigan.gov/dnr/education/michigan-species/fish-species/brook-trout), [Vermont Fish & Wildlife](https://www.vtfishandwildlife.com/learn-more/vermont-critters/fish/brook-trout), user-provided head reference photograph (photographer unknown) |
| Arctic Grayling | `arctic-grayling.png` | [Western Native Trout Initiative](https://westernnativetrout.org/what-are-western-native-trout-and-char/arctic-grayling/), [Alaska Fly Fishing Goods](https://alaskaflyfishinggoods.com/alaskan-fish/meet-the-fish-arctic-grayling/), user-provided head reference photograph (photographer unknown) |
| Westslope Cutthroat Trout | `westslope-cutthroat-trout.png` | [Western Native Trout Initiative](https://westernnativetrout.org/what-are-western-native-trout-and-char/westslope-cutthroat-trout/) |
| Lake Trout | `lake-trout.png` | [Flathead Conservation District](https://flatheadcd.org/fishing-for-a-cause/), [Montana Field Guide](https://fieldguide.mt.gov/speciesDetail.aspx?elcode=afcha05050) |
| Lake Whitefish | `lake-whitefish.png` | [Wild Montana Anglers Facebook](https://www.facebook.com/wildmontanaanglers/posts/the-flathead-valley-might-just-be-one-of-the-most-unique-fisheries-in-montana-th/1808881183662359/), [Minnesota DNR](https://www.dnr.state.mn.us/minnaqua/speciesprofile/lake_whitefish.html), [onWater](https://www.onwaterapp.com/us/species/montana/lake-whitefish) |
| Kokanee Salmon | `kokanee-salmon.png` | [Instagram Reference](https://www.instagram.com/p/DBpKfaOJHnA/), [California Department of Fish and Wildlife](https://wildlife.ca.gov/Fishing/Inland/Kokanee) |
| Bull Trout | `bull-trout.png` | [onWater](https://www.onwaterapp.com/blog/bull-trout-declining-in-montana-anglers-can-help-preserve-and-protect-this-native-species), [Flathead Beacon](https://flatheadbeacon.com/2024/11/01/in-northwest-montana-recreational-fishing-for-bull-trout-is-a-catch-22/) (article by Tristan Scott; one photograph credited to Jim Mogen/USFWS) |
| Yellowstone Cutthroat Trout | `yellowstone-cutthroat-trout.png` | [Western Native Trout Initiative](https://westernnativetrout.org/what-are-western-native-trout-and-char/yellowstone-cutthroat-trout/), [Montana Field Guide](https://fieldguide.mt.gov/speciesDetail.aspx?elcode=AFCHA02170) |
| Northern Pike | `northern-pike.png` | [Living Water Guide Service](https://livingwaterguide.com/fly-fishing-for-northern-pike-in-montana/) (article by Jeff Lattig) |
| Smallmouth Bass | `smallmouth-bass.png` | [The Curious Angler](https://thecuriousangler.com/montana-smallmouth/) |
| Mountain Whitefish | `mountain-whitefish.png` | [Montana Field Guide](https://fieldguide.mt.gov/speciesDetail.aspx?elcode=AFCHA03060), [FishBase](https://www.fishbase.se/summary/SpeciesSummary.php?id=2685) (species description and coloration; photograph credited to E.R. Keeley), [Oregon Sea Grant](https://seagrant.oregonstate.edu/WRF/native-fishes/salmon-and-trout/mountain-whitefish) (identification tips) |

### Image Attribution Notes

All fish illustrations were AI-generated for the Hookset project. The sources listed above were used as visual references or for species identification, rather than having their original photographs directly displayed on the website.

Some user-provided reference photographs and social media posts did not have independently verified photographer information. No photographer attribution has been assigned where that information was unavailable.

**Pending:** Mountain Whitefish (`mountain-whitefish.png`) has reference sources assigned above but has not yet been generated.

### Default Fly Catalog

The default flies in `sql/scripts/hookset_flies_with_slugs.sql` come from two fly shop catalogs. Each fly's pattern name, colors, and hook size range were taken from the shop's product page for that pattern. The script cites the product page above each group of flies. Sources were checked on November 5, 2026.

- [Big Y Fly Co, Best Trout Flies](https://bigyflyco.com/collections/best-trout-flies)
- [Montana Fly Goods, Flies](https://montanaflygoods.com/product-category/flies/)

| Fly Pattern | Type | Product Page |
|---|---|---|
| Elk Hair Caddis | Dry | [Big Y Fly Co](https://bigyflyco.com/collections/best-trout-flies/products/elk-hair-caddis) |
| X-Caddis | Dry | [Big Y Fly Co](https://bigyflyco.com/collections/best-trout-flies/products/x-caddis-black) |
| Parachute Adams | Dry | [Big Y Fly Co](https://bigyflyco.com/collections/best-trout-flies/products/parachute-adams) |
| Adams | Dry | [Big Y Fly Co](https://bigyflyco.com/collections/best-trout-flies/products/adams) |
| Adams Wulff | Dry | [Big Y Fly Co](https://bigyflyco.com/collections/best-trout-flies/products/adams-wulff-white-wings) |
| Comparadun | Dry | [Big Y Fly Co](https://bigyflyco.com/collections/best-trout-flies/products/comparadun) |
| Sparkle Dun | Dry | [Big Y Fly Co](https://bigyflyco.com/collections/best-trout-flies/products/sparkle-duns-dry-fly) |
| Blue Wing Olive | Dry | [Big Y Fly Co](https://bigyflyco.com/collections/best-trout-flies/products/blue-wing-olive) |
| Parachute Blue Wing Olive | Dry | [Big Y Fly Co](https://bigyflyco.com/collections/best-trout-flies/products/parachute-blue-wing-olive) |
| Pale Morning Dun | Dry | [Big Y Fly Co](https://bigyflyco.com/collections/best-trout-flies/products/pale-morning-dun) |
| Stimulator | Dry | [Big Y Fly Co](https://bigyflyco.com/collections/best-trout-flies/products/stimulator) |
| Chubby Chernobyl | Dry | [Big Y Fly Co](https://bigyflyco.com/collections/best-trout-flies/products/chubby-chernobyl) |
| Yellow Sally | Dry | [Big Y Fly Co](https://bigyflyco.com/collections/best-trout-flies/products/yellow-sally) |
| Ant | Dry | [Big Y Fly Co](https://bigyflyco.com/collections/best-trout-flies/products/ant-dry-fly) |
| Foam Beetle | Dry | [Big Y Fly Co](https://bigyflyco.com/collections/best-trout-flies/products/foam-beetle) |
| Dave's Hopper | Dry | [Big Y Fly Co](https://bigyflyco.com/collections/best-trout-flies/products/daves-hopper) |
| Parachute Hopper | Dry | [Big Y Fly Co](https://bigyflyco.com/collections/best-trout-flies/products/parachute-hopper) |
| Royal Wulff | Dry | [Big Y Fly Co](https://bigyflyco.com/collections/best-trout-flies/products/royal-wulff) |
| Humpy | Dry | [Big Y Fly Co](https://bigyflyco.com/collections/best-trout-flies/products/humpy) |
| Griffith Gnat | Dry | [Big Y Fly Co](https://bigyflyco.com/collections/best-trout-flies/products/griffith-gnat) |
| Purple Parachute Wulff | Dry | [Montana Fly Goods](https://montanaflygoods.com/product/purple-para-wulff/) |
| Goddard Caddis | Dry | [Montana Fly Goods](https://montanaflygoods.com/product/goddard-caddis/) |
| Lawson's Green Drake | Dry | [Montana Fly Goods](https://montanaflygoods.com/product/lawsons-green-drake/) |
| Henry's Fork Salmon Fly | Dry | [Montana Fly Goods](https://montanaflygoods.com/product/henryss-fork-salmon-flies/) |
| Parachute Golden Stone | Dry | [Montana Fly Goods](https://montanaflygoods.com/product/parachute-golden-stone/) |
| Pheasant Tail | Nymph | [Big Y Fly Co](https://bigyflyco.com/collections/best-trout-flies/products/pheasant-tail) |
| Beadhead Pheasant Tail | Nymph | [Big Y Fly Co](https://bigyflyco.com/collections/best-trout-flies/products/pheasant-tail-beadhead) |
| Flashback Beadhead Pheasant Tail | Nymph | [Big Y Fly Co](https://bigyflyco.com/collections/best-trout-flies/products/flashback-pheasant-tail-beadhead) |
| Hot Spot Pheasant Tail | Nymph | [Big Y Fly Co](https://bigyflyco.com/collections/best-trout-flies/products/hot-spot-pheasant-tail) |
| Hare's Ear | Nymph | [Big Y Fly Co](https://bigyflyco.com/collections/best-trout-flies/products/hares-ear) |
| Beadhead Hare's Ear | Nymph | [Big Y Fly Co](https://bigyflyco.com/collections/best-trout-flies/products/hares-ear-beadhead) |
| Flashback Hare's Ear | Nymph | [Big Y Fly Co](https://bigyflyco.com/collections/best-trout-flies/products/flashback-hares-ear) |
| Beadhead Zebra Midge | Nymph | [Big Y Fly Co](https://bigyflyco.com/collections/best-trout-flies/products/zebra-midge-beadhead) |
| Copper John | Nymph | [Big Y Fly Co](https://bigyflyco.com/collections/best-trout-flies/products/copper-john) |
| Brassie | Nymph | [Big Y Fly Co](https://bigyflyco.com/collections/best-trout-flies/products/brassie) |
| Tungsten Perdigon | Nymph | [Big Y Fly Co](https://bigyflyco.com/collections/best-trout-flies/products/perdigon-nymph-tungsten-barbless) |
| Tungsten Frenchie | Nymph | [Big Y Fly Co](https://bigyflyco.com/collections/best-trout-flies/products/frenchie-nymph-tungsten) |
| Pat's Rubber Legs | Nymph | [Big Y Fly Co](https://bigyflyco.com/collections/best-trout-flies/products/pats-rubber-legs) |
| Kaufmann Stonefly Nymph | Nymph | [Big Y Fly Co](https://bigyflyco.com/collections/best-trout-flies/products/kaufmann-stonefly-nymph) |
| Caddis Pupa | Nymph | [Big Y Fly Co](https://bigyflyco.com/collections/best-trout-flies/products/caddis-pupa) |
| Tungsten Walt Worm | Nymph | [Big Y Fly Co](https://bigyflyco.com/collections/best-trout-flies/products/walt-worm-tungsten) |
| San Juan Worm | Nymph | [Big Y Fly Co](https://bigyflyco.com/collections/best-trout-flies/products/san-juan-worm) |
| Glo Bug | Nymph | [Big Y Fly Co](https://bigyflyco.com/collections/best-trout-flies/products/glo-bug) |
| WD-40 | Nymph | [Big Y Fly Co](https://bigyflyco.com/collections/best-trout-flies/products/wd-40) |
| Beadhead Soft Hackle | Nymph | [Big Y Fly Co](https://bigyflyco.com/collections/best-trout-flies/products/soft-hackle-beadhead) |
| Beadhead Prince Nymph | Nymph | [Montana Fly Goods](https://montanaflygoods.com/product/bead-head-prince-nymphs/) |
| Woolly Bugger | Streamer | [Big Y Fly Co](https://bigyflyco.com/collections/best-trout-flies/products/woolly-bugger) |
| Beadhead Woolly Bugger | Streamer | [Big Y Fly Co](https://bigyflyco.com/collections/best-trout-flies/products/woolly-bugger-beadhead) |
| Sculpin | Streamer | [Big Y Fly Co](https://bigyflyco.com/collections/best-trout-flies/products/sculpin) |
| Zonker | Streamer | [Big Y Fly Co](https://bigyflyco.com/collections/best-trout-flies/products/zonker) |
| Beadhead Bunny Leech | Streamer | [Big Y Fly Co](https://bigyflyco.com/collections/best-trout-flies/products/bunny-leech-beadhead) |
| Trophy Dungeon | Streamer | [Big Y Fly Co](https://bigyflyco.com/collections/best-trout-flies/products/trophy-dungeon) |
| Clouser Minnow | Streamer | [Big Y Fly Co](https://bigyflyco.com/collections/best-trout-flies/products/clouser-minnow) |
| Muddler Minnow | Streamer | [Big Y Fly Co](https://bigyflyco.com/collections/best-trout-flies/products/muddler-minnow) |

Some product pages don't offer a color choice. For those flies, the color comes from the pattern name or the pattern's standard color, such as Gray for the Adams. These are marked with a note in the script.

### Water Bodies and Fish Species

The rivers and creeks in `sql/scripts/fish-waterbody-importer.sql`, and the fish linked to each one, come from the Fly Guys N Lies Montana river pages. Rivers that the site splits into sections are stored as one water body, with every species listed in any of their sections. Only the species in each page's Fish Species list were used. Water body descriptions were written for Hookset based on each page's overview. Sources were checked on November 5, 2026.

- [Fly Guys N Lies, Montana Fly Fishing](https://flyguysnlies.com/state/montana-fly-fishing/)

| Water Body | Source Pages |
|---|---|
| Beaverhead River | [Beaverhead River Twin Bridges](https://flyguysnlies.com/rivers/fly-fishing-beaverhead-river-twin-bridges/) |
| Big Hole River | [Big Hole River Wise River](https://flyguysnlies.com/rivers/fly-fishing-big-hole-river-wise-river/), [Big Hole River Divide To Melrose](https://flyguysnlies.com/rivers/fly-fishing-big-hole-river-divide-to-melrose/), [Big Hole River Melrose To Twin Bridges](https://flyguysnlies.com/rivers/fly-fishing-big-hole-river-melrose-to-twin-bridges/) |
| Big Spring Creek | [Big Spring Creek Lewistown](https://flyguysnlies.com/rivers/fly-fishing-big-spring-creek-lewistown/) |
| Bighorn River | [Bighorn River Below Yellowtail Afterbay](https://flyguysnlies.com/rivers/fly-fishing-bighorn-river-below-yellowtail-afterbay/) |
| Bitterroot River | [Bitterroot River Darby](https://flyguysnlies.com/rivers/fly-fishing-bitterroot-river-darby/), [Bitterroot River Missoula](https://flyguysnlies.com/rivers/fly-fishing-bitterroot-river-missoula/) |
| Blackfoot River | [Blackfoot River Bonner](https://flyguysnlies.com/rivers/fly-fishing-blackfoot-river-bonner/) |
| Boulder River (Big Timber) | [Boulder River Big Timber](https://flyguysnlies.com/rivers/fly-fishing-boulder-river-big-timber/) |
| Boulder River (Boulder) | [Boulder River Boulder](https://flyguysnlies.com/rivers/fly-fishing-boulder-river-boulder/) |
| Clark Fork River | [Clark Fork Drummond](https://flyguysnlies.com/rivers/fly-fishing-clark-fork-drummond/), [Clark Fork Missoula](https://flyguysnlies.com/rivers/fly-fishing-clark-fork-missoula/) |
| Dearborn River | [Dearborn River Craig](https://flyguysnlies.com/rivers/fly-fishing-dearborn-river-craig/) |
| East Gallatin River | [East Gallatin River Belgrade](https://flyguysnlies.com/rivers/fly-fishing-east-gallatin-river-belgrade/) |
| Flathead River | [Flathead River Kalispell](https://flyguysnlies.com/rivers/fly-fishing-flathead-river-kalispell/), [Flathead River Polson](https://flyguysnlies.com/rivers/fly-fishing-flathead-river-polson/) |
| Gallatin River | [Gallatin River Big Sky](https://flyguysnlies.com/rivers/fly-fishing-gallatin-river-big-sky/), [Gallatin River Gallatin Gateway](https://flyguysnlies.com/rivers/fly-fishing-gallatin-river-gallatin-gateway/) |
| Gardner River | [Gardner River Gardiner](https://flyguysnlies.com/rivers/fly-fishing-gardner-river-gardiner/) |
| Jefferson River | [Jefferson River Twin Bridges](https://flyguysnlies.com/rivers/fly-fishing-jefferson-river-twin-bridges/), [Jefferson River Three Forks](https://flyguysnlies.com/rivers/fly-fishing-jefferson-river-three-forks/) |
| Kootenai River | [Kootenai River Below Libby Dam](https://flyguysnlies.com/rivers/fly-fishing-kootenai-river-below-libby-dam/) |
| Madison River | [Madison River Upper Madison River](https://flyguysnlies.com/rivers/fly-fishing-madison-river-upper-madison-river/), [Madison River Between The Lakes](https://flyguysnlies.com/rivers/fly-fishing-madison-river-between-the-lakes/), [Madison River Ennis](https://flyguysnlies.com/rivers/fly-fishing-madison-river-ennis/), [Madison River Bear Trap Canyon](https://flyguysnlies.com/rivers/fly-fishing-madison-river-bear-trap-canyon/), [Madison River Lower Madison River](https://flyguysnlies.com/rivers/fly-fishing-madison-river-lower-madison-river/) |
| Marias River | [Marias River Below Tiber Dam](https://flyguysnlies.com/rivers/fly-fishing-marias-river-below-tiber-dam/) |
| Middle Fork Flathead River | [Middle Fork Flathead River West Glacier](https://flyguysnlies.com/rivers/fly-fishing-middle-fork-flathead-river-west-glacier/) |
| Mill Creek | [Mill Creek Paradise Valley](https://flyguysnlies.com/rivers/fly-fishing-mill-creek-paradise-valley/) |
| Missouri River | [Missouri River Land Of Giants](https://flyguysnlies.com/rivers/fly-fishing-missouri-river-land-of-giants/), [Missouri River Below Holter Dam](https://flyguysnlies.com/rivers/fly-fishing-missouri-river-below-holter-dam/) |
| Musselshell River | [Musselshell River Martinsdale](https://flyguysnlies.com/rivers/fly-fishing-musselshell-river-martinsdale/) |
| North Fork Flathead River | [North Fork Flathead River Columbia Falls](https://flyguysnlies.com/rivers/fly-fishing-north-fork-flathead-river-columbia-falls/) |
| Poindexter Slough | [Poindexter Slough Dillon](https://flyguysnlies.com/rivers/fly-fishing-poindexter-slough-dillon/) |
| Red Rock River | [Red Rock River Below Lima Reservoir](https://flyguysnlies.com/rivers/fly-fishing-red-rock-river-below-lima-reservoir/) |
| Rock Creek | [Rock Creek Clinton](https://flyguysnlies.com/rivers/fly-fishing-rock-creek-clinton/) |
| Ruby River | [Ruby River Alder](https://flyguysnlies.com/rivers/fly-fishing-ruby-river-alder/) |
| Shields River | [Shields River Livingston](https://flyguysnlies.com/rivers/fly-fishing-shields-river-livingston/) |
| Smith River | [Smith River](https://flyguysnlies.com/rivers/fly-fishing-smith-river/) |
| South Fork Flathead River | [South Fork Flathead River Bob Marshall](https://flyguysnlies.com/rivers/fly-fishing-south-fork-flathead-river-bob-marshall/), [South Fork Flathead River Hungry Horse](https://flyguysnlies.com/rivers/fly-fishing-south-fork-flathead-river-hungry-horse/) |
| Stillwater River | [Stillwater River Absarokee](https://flyguysnlies.com/rivers/fly-fishing-stillwater-river-absarokee/) |
| Sun River | [Sun River Simms](https://flyguysnlies.com/rivers/fly-fishing-sun-river-simms/) |
| Swan River | [Swan River Bigfork](https://flyguysnlies.com/rivers/fly-fishing-swan-river-bigfork/) |
| West Fork Bitterroot River | [West Fork Bitterroot River Conner](https://flyguysnlies.com/rivers/fly-fishing-west-fork-bitterroot-river-conner/) |
| Yaak River | [Yaak River Sylvanite](https://flyguysnlies.com/rivers/fly-fishing-yaak-river-sylvanite/) |
| Yellowstone River | [Yellowstone River Paradise Valley](https://flyguysnlies.com/rivers/fly-fishing-yellowstone-river-paradise-valley/), [Yellowstone River Livingston](https://flyguysnlies.com/rivers/fly-fishing-yellowstone-river-livingston/), [Yellowstone River Big Timber To Columbus](https://flyguysnlies.com/rivers/fly-fishing-yellowstone-river-big-timber-to-columbus/), [Yellowstone River Columbus To Billings](https://flyguysnlies.com/rivers/fly-fishing-yellowstone-river-columbus-to-billings/) |

Two pages list a cutthroat trout without naming the subspecies: Big Hole River (Wise River) and Musselshell River (Martinsdale). Both were linked to Westslope Cutthroat Trout, the native cutthroat of the upper Missouri drainage.

Belt Creek, Flathead Lake, and Georgetown Lake are not covered by the Fly Guys N Lies pages, so their fish come from the sources below.

| Water Body | Reference Sources |
|---|---|
| Belt Creek | [Genuine Montana, Fishing in Central Montana](https://genuinemontana.com/?p=610), [Wikipedia, Belt Creek (Montana)](https://en.wikipedia.org/wiki/Belt_Creek_(Montana)) (fish list cited to Montana Fish, Wildlife & Parks), [The Electric](https://theelectricgf.com/2022/01/14/fwp-seeking-public-comment-on-proposals-to-increase-trout-in-belt-creek-watershed-plant-trees-in-giant-springs-state-park/) (FWP westslope cutthroat trout project in the Belt Creek watershed) |
| Flathead Lake | [Flathead Lake Biological Station, Flathead Lake Facts](https://flbs.umt.edu/newflbs/flathead-lake/lake-facts/) (University of Montana) |
| Georgetown Lake | [Blackfoot River Outfitters, Georgetown Lake Fishing Report](https://blackfootriver.com/blogs/fishing-reports/georgetown-lake-fishing-report) |

## Acknowledgments

I used AI tools as a learning and writing aid on parts of this project. The code, project choices, and final review are my responsibility.