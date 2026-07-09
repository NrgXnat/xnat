#!/usr/bin/env bash

XNAT_URL="http://localhost:8080"
XNAT_USER="admin"
XNAT_PASS="admin"
GOLDEN_DIR="docs/goldens"

./golden-capture.sh check
