# Releasing

This is released from the `master` branch unless it is a version older than `6.1.0`. If there is no branch for the
release that needs patching, create it from the tag.

## Cutting the release

### Requires contributor access

- Check the [draft release notes](https://github.com/playframework/play-ebean/releases) to see if everything is there
- Wait until [master build finished](https://travis-ci.com/github/playframework/play-ebean/builds) after merging the
  last PR
- Update the [draft release](https://github.com/playframework/play-ebean/releases) with the next tag version
  (eg. `7.0.0`), title and release description
- Check that Travis CI release build has executed successfully (Travis will start
  a [CI build](https://travis-ci.com/github/playframework/play-ebean/builds)
  for the new tag and publish artifacts to Sonatype)

### Requires Sonatype access

- Release the staging repos at https://oss.sonatype.org/#stagingRepositories

### Check Maven Central

- [Play Ebean @ Maven Central](https://repo1.maven.org/maven2/com/typesafe/play/play-ebean_2.12/)
