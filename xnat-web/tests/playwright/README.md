# XNAT Playwright E2E Tests

End-to-end tests for XNAT web application features using [Playwright](https://playwright.dev/).

## Prerequisites

- Node.js 18+
- A running XNAT instance (local or remote)

## Setup

```bash
cd tests/playwright
npm install
npx playwright install chromium
```

## Configuration

Copy `.env.example` to `.env` and set values for your XNAT instance:

```bash
cp .env.example .env
```

Or pass them inline:

```bash
XNAT_URL=http://localhost:8080 XNAT_ADMIN_USER=admin XNAT_ADMIN_PASS=admin npx playwright test
```

## Running Tests

```bash
# All tests
npx playwright test

# Specific test file
npx playwright test tests/direct-archive-overwrite-api.spec.ts
npx playwright test tests/direct-archive-overwrite-ui.spec.ts

# Headed mode (visible browser)
npx playwright test --headed

# Debug mode
npx playwright test --debug
```

## Test Structure

- `helpers/auth.ts` - UI login helper
- `helpers/xnat-api.ts` - REST API client for test setup/teardown
- `tests/direct-archive-overwrite-api.spec.ts` - API-level tests for the directArchiveOverwrite setting
- `tests/direct-archive-overwrite-ui.spec.ts` - UI tests for the DICOM SCP editor overwrite mode dropdown
