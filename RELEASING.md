# Release process

1. Commit any remaining changes and push. Wait for Travis build to come back green.
2. Create Git tag with next version number:
    * for major changes (v1.0.0 to v2.0.0): `./gradlew reckonTagCreate -Preckon.scope=major -Preckon.stage=final`
    * for minor changes (v1.0.0 to v1.1.0): `./gradlew reckonTagCreate -Preckon.scope=minor -Preckon.stage=final`
    * for patches (v1.0.0 to v1.0.1): `./gradlew reckonTagCreate -Preckon.scope=patch -Preckon.stage=final`

3. Push tag. Travis will automatically create GitHub release with binaries and push them to OSSRH
   (which will push them to Maven Central).
4. Go to GitHub and add release notes / changelog to release.
