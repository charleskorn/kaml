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

echo "Assembling release..."
./batect assembleRelease
echo
