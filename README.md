<!--- Copyright (C) from 2022 The Play Framework Contributors <https://github.com/playframework>, 2011-2021 Lightbend Inc. <https://www.lightbend.com> -->

# Play Ebean

[![X (formerly Twitter) Follow](https://img.shields.io/twitter/follow/playframework?label=follow&style=flat&logo=x&color=brightgreen)](https://x.com/playframework)
[![Discord](https://img.shields.io/discord/931647755942776882?logo=discord&logoColor=white)](https://discord.gg/g5s2vtZ4Fa)
[![GitHub Discussions](https://img.shields.io/github/discussions/playframework/playframework?&logo=github&color=brightgreen)](https://github.com/playframework/playframework/discussions)
[![StackOverflow](https://img.shields.io/static/v1?label=stackoverflow&logo=stackoverflow&logoColor=fe7a16&color=brightgreen&message=playframework)](https://stackoverflow.com/tags/playframework)
[![YouTube](https://img.shields.io/youtube/channel/views/UCRp6QDm5SDjbIuisUpxV9cg?label=watch&logo=youtube&style=flat&color=brightgreen&logoColor=ff0000)](https://www.youtube.com/channel/UCRp6QDm5SDjbIuisUpxV9cg)
[![Twitch Status](https://img.shields.io/twitch/status/playframework?logo=twitch&logoColor=white&color=brightgreen&label=live%20stream)](https://www.twitch.tv/playframework)
[![OpenCollective](https://img.shields.io/opencollective/all/playframework?label=financial%20contributors&logo=open-collective)](https://opencollective.com/playframework)

[![Build Status](https://github.com/playframework/play-ebean/actions/workflows/build-test.yml/badge.svg)](https://github.com/playframework/play-ebean/actions/workflows/build-test.yml)
[![Maven](https://img.shields.io/maven-central/v/org.playframework/play-ebean_2.13.svg?logo=apache-maven)](https://mvnrepository.com/artifact/org.playframework/play-ebean_2.13)
[![Repository size](https://img.shields.io/github/repo-size/playframework/play-ebean.svg?logo=git)](https://github.com/playframework/play-ebean)
[![Scala Steward badge](https://img.shields.io/badge/Scala_Steward-helping-blue.svg?style=flat&logo=data:image/png;base64,iVBORw0KGgoAAAANSUhEUgAAAA4AAAAQCAMAAAARSr4IAAAAVFBMVEUAAACHjojlOy5NWlrKzcYRKjGFjIbp293YycuLa3pYY2LSqql4f3pCUFTgSjNodYRmcXUsPD/NTTbjRS+2jomhgnzNc223cGvZS0HaSD0XLjbaSjElhIr+AAAAAXRSTlMAQObYZgAAAHlJREFUCNdNyosOwyAIhWHAQS1Vt7a77/3fcxxdmv0xwmckutAR1nkm4ggbyEcg/wWmlGLDAA3oL50xi6fk5ffZ3E2E3QfZDCcCN2YtbEWZt+Drc6u6rlqv7Uk0LdKqqr5rk2UCRXOk0vmQKGfc94nOJyQjouF9H/wCc9gECEYfONoAAAAASUVORK5CYII=)](https://scala-steward.org)
[![Mergify Status](https://img.shields.io/endpoint.svg?url=https://api.mergify.com/v1/badges/playframework/play-ebean&style=flat)](https://mergify.com)

This module provides Ebean support for Play Framework.

## Releases

The Play Ebean plugin supports several different versions of Play and Ebean.

| Plugin version                                                                                         | Play version | Ebean version  |
|--------------------------------------------------------------------------------------------------------|--------------|----------------|
| 8.5.0                                                                                                  | 3.0.9+       | 17.1.0         |
| 8.4.0                                                                                                  | 3.0.9+       | 15.11.0        |
| 8.3.0                                                                                                  | 3.0.2+       | 15.1.0         |
| 8.2.0                                                                                                  | 3.0.2+       | 15.0.1         |
| 8.1.0                                                                                                  | 3.0.1+       | 13.23.0        |
| 8.0.0                                                                                                  | 3.0.0+       | 13.17.3        |
| 7.5.0                                                                                                  | 2.9.9+       | 17.1.0         |
| 7.4.0                                                                                                  | 2.9.9+       | 15.11.0        |
| 7.3.0                                                                                                  | 2.9.2+       | 15.1.0         |
| 7.2.0                                                                                                  | 2.9.2+       | 15.0.1         |
| 7.1.0                                                                                                  | 2.9.1+       | 13.23.0        |
| 7.0.0                                                                                                  | 2.9.0+       | 13.17.3        |
| 6.2.0<br>**! [Important notes](https://github.com/playframework/play-ebean/releases/tag/6.2.0-RC4) !** | 2.8.18+      | 12.16.1        |
| 6.0.0                                                                                                  | 2.8.1        | 11.45.1        |
| 5.0.2                                                                                                  | 2.7.0        | 11.39.x        |
| 5.0.1                                                                                                  | 2.7.0        | 11.32.x        |
| 5.0.0                                                                                                  | 2.7.0        | 11.22.x        |
| 4.1.4                                                                                                  | 2.6.x        | 11.32.x        |
| 4.1.3                                                                                                  | 2.6.x        | 11.15.x        |
| 4.1.0                                                                                                  | 2.6.x        | 11.7.x         |
| 4.0.6                                                                                                  | 2.6.x        | 10.4.x         |
| 4.0.2                                                                                                  | 2.6.x        | 10.3.x         |
| 3.2.0                                                                                                  | 2.5.x        | 10.4.x         |
| 3.1.0                                                                                                  | 2.5.x        | 8.2.x          |
| 3.0.2                                                                                                  | 2.5.x        | 7.6.x          |
| 3.0.1                                                                                                  | 2.5.x        | 6.18.x         |
| 2.0.0                                                                                                  | 2.4.x        | 6.8.x          |
| 1.0.0                                                                                                  | 2.4.x        | 4.6.x          |

> * Release Candidate: these releases are not stable and should not be used in production.

We also recommend using the payintech fork: https://github.com/payintech/play-ebean

## Testing the evolution scripts on real databases

`EbeanEvolutionScriptDatabasesTest` applies the evolution script that Play Ebean generates with Play's evolutions, uses the models, and reverts the script again. It always runs on H2 and SQLite. On Postgres, MySQL, MariaDB, SQL Server and Oracle, it only runs when these environment variables are set, and is skipped otherwise, like on CI:

- `PLAY_EBEAN_TEST_<DB>_URL`: the JDBC URL
- `PLAY_EBEAN_TEST_<DB>_USER`
- `PLAY_EBEAN_TEST_<DB>_PASSWORD`, or `PLAY_EBEAN_TEST_<DB>_PASSWORD_FILE` to read the password from a file

`<DB>` is one of `POSTGRES`, `MYSQL`, `MARIADB`, `SQLSERVER` and `ORACLE`. Use an empty database that you can throw away, because the test creates and drops its tables, and Ebean's stored procedures and history tables.

For example, start the databases with Docker:

```bash
docker run -d --name play-ebean-test-postgres -e POSTGRES_PASSWORD=test -e POSTGRES_DB=play_ebean_test -p 127.0.0.1:15432:5432 postgres:17
docker run -d --name play-ebean-test-mysql -e MYSQL_ROOT_PASSWORD=test -e MYSQL_DATABASE=play_ebean_test -p 127.0.0.1:13306:3306 mysql:8.4
docker run -d --name play-ebean-test-mariadb -e MARIADB_ROOT_PASSWORD=test -e MARIADB_DATABASE=play_ebean_test -p 127.0.0.1:13307:3306 mariadb:11.4
docker run -d --name play-ebean-test-oracle -e ORACLE_PASSWORD=test -e APP_USER=play_ebean_test -e APP_USER_PASSWORD=test -p 127.0.0.1:11521:1521 gvenzl/oracle-free:23-slim
# The SQL Server images are only available for x86-64
docker run -d --name play-ebean-test-sqlserver -e ACCEPT_EULA=Y -e MSSQL_SA_PASSWORD=Test-1234 -p 127.0.0.1:11433:1433 mcr.microsoft.com/mssql/server:2022-latest
```

When SQL Server has started, create its database:

```bash
docker exec play-ebean-test-sqlserver /opt/mssql-tools18/bin/sqlcmd -C -S localhost -U sa -P Test-1234 -Q 'CREATE DATABASE play_ebean_test'
```

Then set the variables for the databases to test, and run the test once the databases are ready (Oracle takes a while on its first start):

```bash
export PLAY_EBEAN_TEST_POSTGRES_URL='jdbc:postgresql://127.0.0.1:15432/play_ebean_test' PLAY_EBEAN_TEST_POSTGRES_USER=postgres PLAY_EBEAN_TEST_POSTGRES_PASSWORD=test
export PLAY_EBEAN_TEST_MYSQL_URL='jdbc:mysql://127.0.0.1:13306/play_ebean_test' PLAY_EBEAN_TEST_MYSQL_USER=root PLAY_EBEAN_TEST_MYSQL_PASSWORD=test
export PLAY_EBEAN_TEST_MARIADB_URL='jdbc:mariadb://127.0.0.1:13307/play_ebean_test' PLAY_EBEAN_TEST_MARIADB_USER=root PLAY_EBEAN_TEST_MARIADB_PASSWORD=test
export PLAY_EBEAN_TEST_ORACLE_URL='jdbc:oracle:thin:@//127.0.0.1:11521/FREEPDB1' PLAY_EBEAN_TEST_ORACLE_USER=play_ebean_test PLAY_EBEAN_TEST_ORACLE_PASSWORD=test
export PLAY_EBEAN_TEST_SQLSERVER_URL='jdbc:sqlserver://127.0.0.1:11433;databaseName=play_ebean_test;encrypt=true;trustServerCertificate=true' PLAY_EBEAN_TEST_SQLSERVER_USER=sa PLAY_EBEAN_TEST_SQLSERVER_PASSWORD=Test-1234

sbt --server 'core/testOnly play.db.ebean.EbeanEvolutionScriptDatabasesTest'
```

With `--server`, sbt starts on its own and sees the variables. Without it, sbt can connect to an sbt server that's already running, which doesn't see them.

## Releasing a new version

See https://github.com/playframework/.github/blob/main/RELEASING.md
