#!/usr/bin/env bash

set -euo pipefail

echo "Building..."
./batect build
echo

echo "Running unit tests and static analysis..."
./batect check
echo

echo "Generating code coverage report..."
./batect generateCodeCoverageReport
echo

if [ "$TRAVIS_PULL_REQUEST" == "false" ]; then
    echo "Assembling release..."
    ./batect assembleRelease
    echo
else
    echo "Not assembling a release for a pull request build as this requires access to the GPG key."
fi
