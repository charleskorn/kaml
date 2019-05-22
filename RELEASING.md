# Release process

1. Commit any remaining changes and push. Wait for Travis build to come back green.
2. Create Git tag with next version number: `git tag -s <version>`
3. Push tag. Travis will automatically create GitHub release with binaries and push them to OSSRH
   (which will push them to Maven Central).
4. Go to GitHub and add release notes / changelog to release.
