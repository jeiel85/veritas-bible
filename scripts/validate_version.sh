#!/bin/bash
# =============================================================================
# Version Sync Validator
# =============================================================================
# This script validates that the GitHub release tag matches the app version
# defined in pubspec.yaml to prevent version mismatch errors.
#
# Usage: ./scripts/validate_version.sh
# =============================================================================

set -e

# Colors for output
RED='\033[0;31m'
GREEN='\033[0;32m'
YELLOW='\033[1;33m'
NC='\033[0m' # No Color

echo "========================================"
echo "  Version Sync Validator"
echo "========================================"
echo ""

# Get GitHub tag version (without 'v' prefix)
GITHUB_TAG="${GITHUB_REF_NAME:-$(git describe --tags --abbrev=0 2>/dev/null || echo '')}"
GITHUB_VERSION="${GITHUB_TAG#v}"  # Remove 'v' prefix if present

# Get version from pubspec.yaml
PUBSPEC_VERSION=$(grep -E "^version:" pubspec.yaml | sed 's/version: //' | tr -d ' ')
APP_VERSION="${PUBSPEC_VERSION%+*}"  # Remove +N suffix if present (e.g., 1.0.0+1 -> 1.0.0)

echo -e "${YELLOW}GitHub Release Tag:${NC} v${GITHUB_VERSION}"
echo -e "${YELLOW}pubspec.yaml Version:${NC} ${APP_VERSION}"
echo ""

# Compare versions
if [ "${GITHUB_VERSION}" != "${APP_VERSION}" ]; then
    echo -e "${RED}❌ ERROR: Version mismatch detected!${NC}"
    echo ""
    echo "  GitHub Tag:  v${GITHUB_VERSION}"
    echo "  App Version: ${APP_VERSION}"
    echo ""
    echo "Please ensure both versions match before creating a release."
    echo ""
    echo "To fix:"
    echo "  1. Update pubspec.yaml version to ${GITHUB_VERSION}"
    echo "  2. Or create a new tag matching the current version"
    echo ""
    exit 1
else
    echo -e "${GREEN}✅ Version check passed!${NC}"
    echo "  GitHub Tag and App Version are in sync."
    echo ""
    exit 0
fi