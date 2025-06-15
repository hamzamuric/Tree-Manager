# Env file

Contents of `.env` file are set for testing purposes and should be changed, except when running the application locally.

For trying the project, it is easiest to let this file unchanged.

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

# Endpoint description

## get edges

This is `GET` request with path `/edge/{root}`, where `root` is id (`Int`) for the root node.

This endpoint returns list of edges from the specified root node.

It is not optimized for performance and memory (for that, see **get edges stream**).

## get edges stream

This is `GET` request with path `/edge/{root}/stream`, where `root` is id (`Int`) for the root node.

This endpoint returns list of edges from the specified root node.

The list is fetched lazily from the database and streamed in batches,
so memory usage does not grow with the number of edges
and a client starts getting edges as soon as the first batch is sent.

## get edges pretty

This is `GET` request with path `/edge/{root}/pretty`, where `root` is id (`Int`) for the root node.

This endpoint returns JSON object in the form of a tree structure.
The Relationship of the nodes can be clearly seen in this format.

As the whole object must be constructed before serializing it,
it may not perform well in case of huge trees.

It is still a good option to easily visualize the tree structure.

## create edge

This is `POST` request with path `/edge`.

The request body should be JSON with the following form:

```json
{
  "from": 1,
  "to": 2
}
```

Field `from` represents id (`Int`) of a node from which the edge starts.

Field `to` represents id (`Int`) of a node to which the edge ends.

## delete edge

This is `DELETE` request with path `/edge`.

The request body should be JSON with the following form:

```json
{
  "from": 1,
  "to": 2
}
```

Field `from` represents id (`Int`) of a node from which the edge starts.

Field `to` represents id (`Int`) of a node to which the edge ends.

This endpoint deletes just one edge which is specified in the request body.

## delete edge recursive

This is `DELETE` request with path `/edge/recursive`.

The request body should be JSON with the following form:

```json
{
  "from": 1,
  "to": 2
}
```

Field `from` represents id (`Int`) of a node from which the edge starts.

Field `to` represents id (`Int`) of a node to which the edge ends.

This endpoint deletes all the edges which form a subtree from the node given by `from` field.

No edge from the subtree remains unconnected to the rest of the tree.
