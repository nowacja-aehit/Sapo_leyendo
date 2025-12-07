# About This Project

This is a simple Java warehouse management system (WMS) backend. It's built with Maven and uses a local SQLite database for data storage. The application's main purpose is to manage products, users, and their locations within a warehouse.

The code and comments are primarily in Polish.

## Architecture

- **Entry Point**: The main application logic starts in `SapoLeyendo.java`. On startup, it initializes the database schema and populates it with test data.
- **Database**: The application uses an embedded SQLite database. The database file (`sapo_wms_main.db`) is created in the `data/` directory at the project root.
- **Database Abstraction**: Database operations are defined in the `DatabaseInterface.java` interface and implemented in `SqliteDatabaseConnection.java`. This decouples the application logic from the specific database implementation.
- **Schema and Seeding**: The database schema and initial data are loaded from `.sql` files located in `src/main/resources/database/`. The `SqlScriptRunner.java` class is responsible for executing these scripts.
- **Models**: Data entities like `Product` and `User` are defined as Java records in the `model` package.

## Developer Workflow

- **Building the Project**: This is a standard Maven project. To build it, run:
  ```sh
  mvn clean install
  ```
- **Running the Application**: You can run the application by executing the `main` method in `src/main/java/com/mycompany/sapo_leyendo/SapoLeyendo.java`.
- **Database**: The database is created and seeded automatically when the application starts. If you want to reset the database, simply delete the `data/` directory.

## Code Conventions

- **Language**: The code, comments, and even some parts of the file names are in Polish.
- **Immutability**: Data models are implemented as Java records (`Product.java`, `User.java`) to promote immutability.
- **Error Handling**: Methods that might not find a result return an `Optional`. For example, `getProductBySku(String sku)` returns `Optional<Product>`.
- **Dependency Management**: All project dependencies are managed in the `pom.xml` file. The key dependencies are `sqlite-jdbc` for database connectivity and `junit-jupiter` for testing.

## Key Files

- `pom.xml`: Defines project dependencies, plugins, and build settings.
- `src/main/java/com/mycompany/sapo_leyendo/SapoLeyendo.java`: The main application class and entry point.
- `src/main/java/com/mycompany/sapo_leyendo/database/DatabaseInterface.java`: The contract for all database operations.
- `src/main/java/com/mycompany/sapo_leyendo/database/SqliteDatabaseConnection.java`: The SQLite-specific implementation of the database interface.
- `src/main/java/com/mycompany/sapo_leyendo/database/SqlScriptRunner.java`: A utility to run SQL scripts from resource files.
- `src/main/resources/database/CreateDB_sqlite.sql`: The SQL script to create the database schema.
- `src/main/resources/database/FillDatabase_sqlite.sql`: The SQL script to populate the database with initial data.
