# XNAT Contributor SDLC

Contributions to XNAT generally follow one path: plan on a PR, verify the change and pass three review gates before merge, and ship in a quarterly release. Two main exceptions: Open-ended work that no one is actively working on, such as feature requests, suspected bugs or changes with no firm release target, starts in the XNAT discussion group or on the XNAT Community Developer Call, and becomes a GitHub Issue once a bug is confirmed or maintainers agree a feature is planned; and security issues are reported and fixed privately, never in public Issues or PRs, and critical fixes can ship outside the quarterly cycle ([section 4](#4-security-issues)).

**Applies to:** NrgXnat/xnat (core) and NrgXnat plugin repositories. [CONTRIBUTING.md](CONTRIBUTING.md) covers how to build; the [PR template](.github/pull_request_template.md) defines what every PR must contain. This doc covers how work flows.

**Communication:** The **#xnat-community-dev** Slack channel is where contributors talk to each other and ask development questions. Maintainers also use it to coordinate XNAT releases: what goes in, testing, and timing. To be added to the channel, email info@xnat.org. Other ways to connect:

- **XNAT discussion group:** [groups.google.com/group/xnat_discussion](https://groups.google.com/group/xnat_discussion), for questions, suspected bugs and feature requests
- **XNAT Community Developer Call:** features and fixes can also be discussed on the call
- **GitHub:** [github.com/NrgXnat/xnat](https://github.com/NrgXnat/xnat), for Issues, pull requests and CONTRIBUTING.md
- **info@xnat.org:** general questions
- **Security reports:** *Report a vulnerability* on GitHub (preferred), or security@xnat.org ([section 4](#4-security-issues))
- **Documentation:** [xnat.org](https://xnat.org), for docs, news and release announcements

## 1. Versioning

XNAT versions follow SemVer principles: `1.MAJOR.MINOR.PATCH`. The leading `1` doesn't change, so the second number marks a major release, the third a minor release and the fourth a patch, used only for releases such as 1.10.2.1. Plugins follow standard SemVer (MAJOR.MINOR.PATCH). A minor release must be backward compatible, and anything breaking waits for a major release.

| Release type | Example | What it may contain |
| --- | --- | --- |
| **Major** | 1.11.0 | Breaking changes, removal or change of APIs, schema or data-model changes that existing plugins or sites must adapt to, dependency upgrades that break plugin compatibility |
| **Minor** | 1.10.2 | Bug fixes and enhancements that are **backward compatible** with existing user environments, plugins and datatypes, including new APIs and deprecation of existing APIs |
| **Patch** | 1.10.2.1 | An urgent security or functionality fix to a released version, shipped outside the quarterly cycle |

API changes follow semantic versioning. **Adding** or **deprecating** an API is allowed in a minor release. **Removing** an API, or **changing** one in a way that breaks callers, requires a major release. Deprecate first, then remove in a later major release.

A change belongs in a minor release only if an existing site can upgrade without changing configuration, plugins or data, and a plugin compiled against the previous minor release still loads and works. If you're unsure, treat it as major and say so in the PR.

## 2. Branches

Every PR targets `develop`; the milestone, not the branch, records whether a change is minor or major.

| Branch | Purpose |
| --- | --- |
| `main` | Latest release. No direct commits. |
| `releases/<version>-rc` | Release candidate branch, cut from `develop` by the *Release - Cut RC Branch* workflow. Accepts only stabilization fixes. A branch under `releases/` can also carry an out-of-band patch release, such as 1.10.2.1. |
| `develop` | Integration branch for **all** contributions, minor and major |
| `<type>/<short-description>` | Contributor work branches, in a fork or in this repository |

- **Release target:** maintainers set each cycle's target, minor or major, and discuss it in #xnat-community-dev.
- **Minor cycle:** only minor-milestone PRs merge to `develop`. Major-milestone PRs can be reviewed and approved, but they stay open and unmerged.
- **Major cycle:** approved major-milestone PRs merge to `develop`. Rebase them first, and re-verify them if `develop` has moved.
- **Cutting the RC:** the *Release - Cut RC Branch* workflow creates `releases/<version>-rc` from `develop` and bumps `develop` to the next `-SNAPSHOT` version. The version chosen here, for example 1.10.4-SNAPSHOT or 1.11.0-SNAPSHOT, sets the next release target.
- **Releasing:** the *Release - Promote RC to Main* workflow strips the `-RC-SNAPSHOT` suffix, merges the RC branch into `main` with a merge commit, and tags the release (for example `1.10.2`, with no `v` prefix). The RC branch stays as it was.

## 3. Lifecycle of a change

A change moves through six steps, and QA happens before merge, not at release time.

### 3.1 Plan

- **Track work in progress on the PR itself.** Open a draft PR early so others can see what's underway.
- **Start in the XNAT discussion group** with questions, suspected bugs and feature requests. Features and fixes can also be discussed on the XNAT Community Developer Call.
- **Open a GitHub Issue for a confirmed bug,** with the XNAT version, steps to reproduce, the expected and actual results, and logs with no PHI. A feature request becomes an Issue once maintainers agree it's planned work. When you start on an Issue, reference it from your PR (`Related to #123`, or `Closes #123` if the PR fully resolves it).
- **Discuss large or breaking changes first,** in the discussion group, #xnat-community-dev or on the developer call, so the scope and target milestone are agreed early.

### 3.2 Develop

- Most contributors work from a fork: fork NrgXnat/xnat, branch from `develop`, and open your PR against `NrgXnat/xnat:develop`. Leave **Allow edits from maintainers** on, so maintainers can rebase a held PR or make small fixes.
- Follow the code standards and the [Commit Messages](CONTRIBUTING.md#commit-messages) format in CONTRIBUTING.md: Conventional Commits, `type(scope)!: summary`. `feat` and `fix` without `!` are backward compatible and fit a minor release. A `!` right before the colon, plus a `BREAKING CHANGE:` footer, marks a breaking change, which needs a major milestone. Put the Jira key or Issue in the body rather than the summary. PRs are squash-merged, so the PR title uses the same format. New behavior needs tests, and bug fixes should include a regression test where practical.
- If the change is observable through the REST API, add or update coverage in [xnat-rest-tests](https://github.com/NrgXnat/xnat-rest-tests) in a companion PR.
- AI-assisted code and PR text are fine, but you are responsible for having read, run and verified everything you submit.

### 3.3 Set the milestone

Every PR needs a milestone before review; a required CI check blocks PRs without one. Contributors working from a fork can't set a milestone; propose the milestone or target XNAT version in the PR description, and the scope approver sets it.

- **Minor milestone (1.x.y):** backward compatible only ([section 1](#1-versioning)).
- **Major milestone (1.x.0):** anything breaking, plus anything that doesn't clearly meet the minor criteria.

Justify the milestone in the PR template's **Upgrade and compatibility** section. Maintainers may move a PR to a different milestone during review. Ideally, major work is done during its major release cycle, so align with maintainers before starting it. Major work can be underway on a PR or branch through minor cycles, but it doesn't merge to `develop` until the target major cycle. Keep the PR open and current in the meantime.

### 3.4 Verify (contributor-owned QA)

A green CI build is necessary but not sufficient. Scale the evidence to the risk: a docs, CI or build-only change needs a green build, with the other Testing subsections marked not applicable; any change to running code needs the evidence below; higher-risk changes need more (see below). For changes to running code, the PR's **Testing** section must show:

- **Deployment evidence:** the change built, deployed to a running XNAT of the target version, and exercised.
- **Unit tests** added or changed, and how to run them.
- **REST integration tests:** a companion PR or existing coverage, where applicable.
- **Manual verification** that makes clear the change was tested: starting state, steps, observed results, screenshots for UI changes, the happy path and the edge cases you tried to break, and adjacent paths you checked for regressions. Reviewers aren't committed to repeating your testing, so the evidence in the PR has to show it, especially for impactful changes.
- **Gaps:** anything you didn't test, and why.

Complete the **Risks and impact** sections that apply (PHI/de-identification, security, input data, upgrade/compatibility, performance) and delete the rest.

**Higher-risk changes: test what could break.** For these areas, verify the paths around the change, not just whether the fix works:

- **Anonymization and de-identification:** compare output byte for byte with the previous build, including failure paths such as a script error or a cut-off upload.
- **Data access and permissions:** test membership changes (add, remove, promote), not only fixed roles.
- **Ingest and archive data paths:** every import route the change touches, run more than once, with real vendor DICOM files.
- **Schema and data-model migrations:** upgrade an existing site, and say how long the migration runs on a large archive and whether the site must be down.
- **Large new features:** align with maintainers first, and cover every entry point the feature touches.

For any change, also check deployment variants it could depend on: a context path other than root, multi-node, and a non-root container.

**Release note.** The PR template's Release note section is used to write the release notes for the version. Say what an XNAT user needs to know about your change: user-facing changes and any impact on functionality, especially breaking changes. If nothing is user-visible, write "None".

### 3.5 Review and merge

A PR merges only when it passes all three gates, with approvals from two different people.

| Gate | Who | What it confirms |
| --- | --- | --- |
| **Green CI** | CI. A maintainer starts it for pull requests from forks. | The build and required checks pass |
| **Scope approval** | 1 core maintainer (WashU XNAT team) | The change belongs on this line **now**: it fits the roadmap, the milestone is correct, and the timing works with the release schedule. Like the code reviewer, the maintainer confirms the QA evidence is sufficient. For a **higher-risk change** (anonymization and de-identification, data access and permissions, ingest and archive data paths, schema and data-model migrations, large new features), the maintainer also checks that the evidence covers those risks. |
| **Code review approval** | 1 additional reviewer, not the author. Doesn't have to be on the WashU team. | The code is correct and maintainable, the tests are meaningful, and the QA evidence in the Testing section is credible, complete and sufficient for the change. The reviewer can ask for specific further testing before approving. |

Reviewers should send a PR back if its Testing section lacks deployment evidence.

**QA follow-up for higher-risk changes.** A QA follow-up isn't required, but it's considered for every higher-risk change. The contributor can request one in the PR, and either reviewer can ask for one. The scope approver opens a GitHub Issue with the `qa task` label and the target release milestone, or adds the PR to an existing `qa task` issue for the same area, and links it from the PR (see [#108](https://github.com/NrgXnat/xnat/issues/108)). The PR can merge before the follow-up QA is done, but that QA must be finished before the release.

### 3.6 Release

Releases target a **quarterly** cadence. As a guideline, release features should be code complete about two weeks before the release date. The code isn't frozen after that, since release features, plugin compatibility and follow-up QA may still need fixes. Maintainers may adjust dates for a given cycle and will announce changes in #xnat-community-dev. Critical security fixes are the exception and can ship outside the cycle ([section 4](#4-security-issues)).

**Plugins:** minor, backward-compatible plugin releases ship as needed, and their announcements are bundled with the quarterly XNAT announcement. Major plugin releases ship with the corresponding major XNAT release.

1. **Open development:** PRs that pass the gates in [3.5](#35-review-and-merge) merge to `develop`.
2. **Code complete and RC cut** (before release, as announced): a maintainer runs *Cut RC Branch*. Only fixes for release features, plugin compatibility, regressions and follow-up QA merge to `releases/<version>-rc`, each with maintainer approval. `develop` reopens immediately, and unfinished PRs move to the next milestone.
3. **Release candidate testing:** pushes to the RC branch publish `-RC-SNAPSHOT` builds. Contributors smoke-test the features they contributed against them.
4. **Release:** a maintainer runs *Promote RC to Main*, which tags the release and merges it into `main`. The `main` build publishes the artifacts, and release notes are built from each merged PR's Release note section.

## 4. Security issues

Report and fix vulnerabilities privately, never in public Issues, Discussions, PRs or the XNAT discussion group. The [NrgXnat Security Policy](https://github.com/NrgXnat/.github/blob/main/SECURITY.md) is authoritative for reporting and supported versions; this section covers how a report moves through development.

**Never include PHI or real subject data** in reports, logs, screenshots, test data or PRs, including PRs in a private fork. Redact or de-identify first.

| Line | Security fixes |
| --- | --- |
| XNAT 1.10.x | Supported |
| XNAT 1.9.3.x | At the XNAT team's discretion, typically critical or high severity |
| XNAT &lt; 1.9.3 | Not supported |
| Plugins | Latest release only. Fixes ship in a new plugin release and are not backported. |

Security and maintenance fixes for a supported line other than the one `develop` is building are an exception to the "every PR targets `develop`" rule. They ship from a release candidate branch (`releases/<version>-rc`) cut from that line's release tag, for example 1.10.2.1 from the 1.10.2 tag, and PRs for them target that branch. Coordinate with the maintainers before starting one.

1. **Report** via GitHub [Private Vulnerability Reporting](https://docs.github.com/en/code-security/how-tos/report-and-fix-vulnerabilities/report-privately) (preferred): open the affected repo's Security tab and click *Report a vulnerability*. If you're not sure which repo is affected, report it in [NrgXnat/xnat](https://github.com/NrgXnat/xnat/security/advisories/new). Use **security@xnat.org** to attach files, or if the repo is archived or has no button. A private report creates a draft security advisory visible only to the reporter and maintainers, and GitHub credits the reporter.
2. **Triage:** maintainers accept or close the report. Reports sent by email are entered as a draft advisory by a maintainer.
3. **Fix:** maintainers and contributors work on the fix together in a temporary private fork started from the advisory. Advisory admins add collaborators, who can then read the fork, push to it, and open PRs from it.
4. **Review:** maintainers review security fixes, and advisory collaborators may review too when a private fork exists. GitHub doesn't run CI in private forks or enforce branch protection on merge, so the [section 3.5](#35-review-and-merge) gates are met by hand: build and test locally, attach the evidence, and get both approvals.
5. **Merge:** an advisory admin merges the fork's PRs together from the advisory page. Merging makes the fix public, so maintainers time it with the release.
6. **Release:** core fixes ship in a release of each affected supported line; plugin fixes ship in a new plugin release. Critical fixes can ship outside the quarterly release cycle.
7. **Publish:** after the fixed release ships, maintainers may publish the advisory and request a CVE through GitHub.

## 5. Contributor checklist

- [ ] Branch from and target `develop`
- [ ] PR opened (draft is fine) and linked to any related Issue
- [ ] Milestone set and justified in Upgrade and compatibility
- [ ] Risks and impact sections completed
- [ ] Unit tests added or updated; REST tests linked if applicable
- [ ] Deployed to the target XNAT version and manually verified, with evidence in the PR
- [ ] Gaps stated
- [ ] Higher-risk change? QA follow-up considered, and requested in the PR if needed
- [ ] CI green, scope approval from a maintainer, code review approval from a second reviewer
