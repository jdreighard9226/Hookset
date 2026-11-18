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

| Tool | Version |
| --- | --- |
| JDK | 25 (built with Oracle JDK 25.0.4.1) |
| MySQL Server | 8.0.46 |
| MySQL Workbench | 8.0 (optional) |
| Git | Any |

Gradle is included in the repo through the Gradle wrapper.

### 1. Install JDK 25

Download and run the JDK 25 installer for your OS from <https://www.oracle.com/java/technologies/downloads/>.

Check it:

```bash
java -version
```

### 2. Install MySQL 8.0.46

Download and run the MySQL 8.0.46 installer for your OS from <https://dev.mysql.com/downloads/installer/>. Set a root password when it asks.

MySQL Workbench is optional: <https://dev.mysql.com/downloads/workbench/>.

### 3. Clone the repo

```bash
git clone https://github.com/jdreighard9226/Hookset.git
cd Hookset
```


### 4. Create the database and app user

Log in to MySQL as root:

```bash
mysql -u root -p
```

Then run:

```sql
CREATE DATABASE hookset;
CREATE USER 'hooksetapp'@'localhost' IDENTIFIED BY '<password from application.properties>';
GRANT ALL PRIVILEGES ON hookset.* TO 'hooksetapp'@'localhost';
EXIT;
```

The password has to match `spring.datasource.password` in `src/main/resources/application.properties`.

### 5. Run the SQL scripts

Run these three scripts in this order:

| Order | Script | What it does |
| --- | --- | --- |
| 1 | `sql/scripts/hooksetTables.sql` | Drops and recreates every table. Running it again wipes all data. |
| 2 | `sql/scripts/fish-waterbody-importer.sql` | Adds the water bodies, fish species, and which fish live in which water. |
| 3 | `sql/scripts/hookset_flies_with_slugs.sql` | Adds the 198 default flies. |

**Windows (PowerShell)**

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

Open each script with File > Open SQL Script and run it with the lightning bolt.

The database diagram is in `sql/hookset-erd.png`.

### 6. Start the app

**Windows (PowerShell)**

```powershell
.\gradlew.bat bootRun
```

**Mac and Linux**

```bash
./gradlew bootRun
```

Open <http://localhost:8080/> and sign up for an account.

### 7. Run the tests

**Windows (PowerShell)**

```powershell
.\gradlew.bat test
```

**Mac and Linux**

```bash
./gradlew test
```

The tests use an in-memory H2 database, so MySQL doesn't need to be running.

### Logs

Logs print to the console and are written to `logs/spring-boot-logger.log`.

### Troubleshooting

| Problem | Fix |
| --- | --- |
| `mysql` is not recognized | MySQL's `bin` folder isn't on your `PATH`. Add it, or use MySQL Workbench for steps 4 and 5. |
| `Permission denied` running `./gradlew` | Run `chmod +x gradlew`. |
| `No matching toolchains found` | JDK 25 isn't installed. |
| `Access denied for user 'hooksetapp'@'localhost'` | The password in `application.properties` doesn't match the one in `CREATE USER`. |
| `Schema-validation: missing table` | Run the SQL scripts from step 5. |
| `Communications link failure` | MySQL isn't running. |

## Sources Used

The libraries Hookset uses, the AI tools I used, and the sources for the images, default flies, water bodies, and fish species are listed in [SOURCES.md](SOURCES.md).