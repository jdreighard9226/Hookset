# Hookset

Hookset is a fly fishing web app for Montana waters. Users can browse fish, water bodies, and flies, keep their own flies, and log the fish they catch.

## What Hookset does

- **Accounts.** Users sign up, log in, and log out. Passwords are hashed with BCrypt. Users can change their password on the profile page. The home, login, and signup pages are public. Every other page requires a logged-in user.
- **Flies.** Hookset comes with a catalog of 200 default flies (dries, nymphs, and streamers). Users can add, edit, and delete their own flies. The fly list can be filtered to show Hookset flies, your flies, or all flies. Each fly has a details page.
- **Fish.** A page listing the fish species in the app.
- **Water bodies.** A list of Montana rivers, streams, and lakes. Each one has a details page showing which fish live there.
- **Catches.** Users log a catch with the water body, fish, fly, length, date, and notes. When you pick a water body, the fish list only shows fish found in that water. Users can view all catches, view just their own, see catch details, and edit their catches.

Bad input on any form is caught and the page shows a message explaining what went wrong.

## Running Hookset

### What you need

- Java 25
- MySQL
- The Gradle wrapper (included in the repo, nothing to install)

### Set up the database

Hibernate is set to `ddl-auto=validate`. It checks the tables but does not create them, so the database has to be set up before the app starts.

1. Create the database and the app user. The username and password must match `src/main/resources/application.properties`.

   ```sql
   CREATE DATABASE hookset;
   CREATE USER 'hooksetapp'@'localhost' IDENTIFIED BY '<password from application.properties>';
   GRANT ALL PRIVILEGES ON hookset.* TO 'hooksetapp'@'localhost';
   ```

2. Run the scripts in `sql/scripts` in this order:

   | Order | Script | What it does |
   | --- | --- | --- |
   | 1 | `hooksetTables.sql` | Drops and recreates every table. Running it again wipes all data. |
   | 2 | `fish-waterbody-importer.sql` | Adds the water bodies, fish species, and which fish live in which water. |
   | 3 | `hookset_flies_with_slugs.sql` | Adds the 200 default flies. |

The database diagram is in `sql/hookset-erd.png`.

### Start the app

From the project root:

```bash
./gradlew bootRun
```

On Windows use `.\gradlew.bat bootRun`.

Open <http://localhost:8080/> and sign up for an account.

### Run the tests

```bash
./gradlew test
```

On Windows use `.\gradlew.bat test`.

The tests start Spring and use the real `hookset` database, so MySQL has to be running and set up first. Each test runs in a transaction that gets rolled back, so tests do not change your data.

### Logs

Logs print to the console and are written to `logs/spring-boot-logger.log`. Logging is configured in `src/main/resources/logback-spring.xml`.

## Acknowledgments

I used AI tools as a learning and writing aid on parts of this project. The code, project choices, and final review are my responsibility.
