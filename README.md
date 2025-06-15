# Env file

Contents of `.env` file are set for testing purposes and should be changed, except when running the application locally.

All environment variables have values which are consistent with commands shown in this file,
so when changing values in `.env` file, corresponding values in the commands should also be changed to reflect `.env`.

# Running the application

Run the postgres database with docker.

Example command:

```bash
$ docker run --name postgres-db \
    -e POSTGRES_USER=admin \
    -e POSTGRES_PASSWORD=admin \
    -e POSTGRES_DB=pgdb \
    -p 5432:5432 \
    -v postgres-data:/var/lib/postgresql/data \
    -d postgres
```

Copy `schema.sql` file into the running docker container:

```bash
$ docker cp src/main/resources/schema.sql postgres-db:/docker-entrypoint-initdb.d/schema.sql
```

Execute `schema.sql` inside the running docker container:

```bash
$ docker exec -it postgres-db psql -U admin -d pgdb -f docker-entrypoint-initdb.d/schema.sql
```

Run Spring Boot application:

```bash
$ ./gradlew bootRun
```

# Testing the endpoints

In the project root directory, there is a file called `Prewave.postman_collection.json`.

This file can be imported into Postman and there should be 6 endpoints to try.

Each request has example data filled in.