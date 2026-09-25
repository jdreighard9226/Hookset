# Hookset

Hookset is a fly fishing web app I am building for my Java class. The goal is to help anglers make better fly choices on Montana waters using the conditions at the time they fish. Eventually, I want to combine catch history, fish and water body data, weather, and moon information to recommend flies that have a better chance of working.

This is still a work in progress. The web app, database, login flow, and data import structure are the foundation. Catch logging and fly recommendations are longer-term goals, so I am not treating those as finished features yet.

## What the project uses

| Part | Technology | Why I am using it |
| --- | --- | --- |
| Backend | Java 25 and Spring Boot | Runs the web app and keeps the business logic in Java. |
| Web pages | Spring MVC and Thymeleaf | Controllers return server-rendered pages and pass data to the templates. |
| Styling | Bootstrap | Gives the pages a responsive layout and ready-made styling for forms and other UI elements. |
| Authentication | Spring Security | Handles login, protected pages, password checking, and logout. |
| Sessions | Spring Security with HTTP sessions | Keeps a user signed in between requests after a successful login. |
| Database | MySQL, Spring Data JPA, and Hibernate | Stores users and the fishing data used by the app. |
| Build | Gradle | Builds the project and manages dependencies across modules. |
| External data | Montana FWP ArcGIS service | Provides fish distribution information for Montana streams and lakes. |

## What is working and what is planned

The project currently has a Spring Boot web app with a home page, login and signup pages, Spring Security configuration, a MySQL-backed user model, and a separate `importers` module for bringing outside data into the database. Login uses Spring Security's form login flow and a user loaded from the database. The importer work focuses on fish and water bodies from Montana Fish, Wildlife & Parks (FWP).

Here is where I want to take it next:

- Let users record catches with the water, fish, fly, fly size, time, and conditions.
- Show each user their own catch history and let them filter it.
- Bring in hourly and historical weather from Open-Meteo.
- Bring in moon phase and sun/moon timing from the U.S. Naval Observatory.
- Rank fly suggestions using catch history and the conditions for a specific water body.
- Add location-aware suggestions, photos, and eventually offline support.

The recommendation system is a goal for the project; I have not trained or shipped a recommendation model at this stage.

## How the app is organized

The main application handles the website: controllers, Thymeleaf templates, login, security, and database access. The `importers` module handles outside data. It calls the FWP service, reads the JSON into DTOs, maps that data to entities, and saves it through Spring Data repositories. Shared JPA code is used across the modules where needed.

The MySQL schema includes `Users`, `Fly`, `FlySize`, `FlyMaterial`, `Fish`, `WaterBody`, and `FishWaterBody`. `FishWaterBody` connects fish species to the waters where they are found. The catch table and the data needed to score fly recommendations are future additions.

## Login, security, and sessions

Spring Security controls which pages can be visited without logging in. The home page, login page, and signup page are public; other pages require an authenticated user. The app uses a custom login page, while Spring Security processes the login form. User records are loaded from MySQL, passwords are checked using the configured password encoder, and the user's admin flag determines whether they receive the `ADMIN` or `USER` role.

After login, Spring Security stores the authenticated user in the HTTP session. The browser sends its session cookie on later requests, so the user does not have to enter their password on every page. This is normal server-side session management. It does not mean the project has a separate Spring Session store such as Redis or JDBC. Logout ends the authenticated session through Spring Security.

Passwords should be stored as hashes, never as plain text. The project uses BCrypt for password encoding. The MySQL password used by the app should also stay out of Git and be supplied through a local environment variable.

## Outside APIs

| Source | How it fits Hookset | Status |
| --- | --- | --- |
| [Montana FWP Fish Distribution — streams](https://services1.arcgis.com/754BERmVIq3RqSf8/ArcGIS/rest/services/MT_FWP_Fish_Distribution/FeatureServer/9) | Stream names, identifiers, and fish distribution data for the importer. | Importer work in progress. |
| [Montana FWP Fish Distribution — lakes](https://services1.arcgis.com/754BERmVIq3RqSf8/ArcGIS/rest/services/MT_FWP_Fish_Distribution/FeatureServer/8) | Lake and fish distribution data for the importer. | Planned/being developed alongside streams. |
| [Open-Meteo forecast and historical weather APIs](https://open-meteo.com/en/docs) | Weather at a location and time, including conditions for past catches. | Planned. |
| [U.S. Naval Observatory API](https://aa.usno.navy.mil/data/api) | Moon phases, illumination, and sun/moon timing. | Planned. |

For the FWP importer, ArcGIS returns a `features` array. Each feature has an `attributes` object containing the fields requested by the query. The importer reads those attributes into DTOs, maps them to the project's entities, and saves them to MySQL. The goal is to keep unique fish and water body records instead of inserting the same name every time it appears in the API response.

External API calls belong in the Spring service layer, so the frontend does not have to call each provider directly. The data sources may have different coverage and update schedules; a record in a distribution layer is not a guarantee that a fish will be present at a specific spot or that fishing is legal there.

## Running it locally

You will need Java 25, MySQL, and the Gradle wrapper included with the project. IntelliJ IDEA is what I use for development.

1. Create a local MySQL database named `hookset` and run the project's schema SQL before starting the app. The Hibernate setting is `ddl-auto=validate`, so Hibernate checks the schema but does not create or update the tables for you.
2. Set the database username and password expected by your local `application.properties`. Keep the real password in an environment variable and do not commit it.
3. From the project root, start the web app:

   ```powershell
   .\gradlew.bat bootRun
   ```

   On macOS or Linux, use `./gradlew bootRun` instead.

4. Open <http://localhost:8080/> in your browser.

To run the tests, use `./gradlew test` on macOS/Linux or `.\gradlew.bat test` on Windows. A test that starts the full Spring context may need access to the configured MySQL database. If the database is not available, that context test can fail before any page or service code is exercised.

If startup reports a missing table, check that the schema was run against `hookset` and that the table names match the JPA mappings. If it reports an access or JDBC error, check the local MySQL server, database user, and environment variable first.

## Project direction

I want Hookset to be useful when someone is actually deciding what to tie on, not just a place to store a list of flies. The first step is getting the web app, authentication, database, and Montana water data working reliably. Once catch logging is in place, those catches can give the recommendation feature real information to work from.

## References and acknowledgments

- [Spring Boot documentation](https://docs.spring.io/spring-boot/reference/)
- [Spring Security form login documentation](https://docs.spring.io/spring-security/reference/servlet/authentication/passwords/form.html)
- [Spring Security session management documentation](https://docs.spring.io/spring-security/reference/servlet/authentication/session-management.html)
- [Bootstrap documentation](https://getbootstrap.com/docs/)
- [Montana FWP Fish Distribution service](https://services1.arcgis.com/754BERmVIq3RqSf8/ArcGIS/rest/services/MT_FWP_Fish_Distribution/FeatureServer)
- [Open-Meteo documentation](https://open-meteo.com/en/docs)
- [U.S. Naval Observatory API documentation](https://aa.usno.navy.mil/data/api)

I also used ChatGPT as a learning and writing aid while working through parts of this project. The code, project choices, and final review are my responsibility.
