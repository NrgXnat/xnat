## Description
<!-- What changed and why, in your own words — the user-facing story and the technical one. Link the work: the ticket ("XNAT-1234"), "Closes #123" (auto-closes on merge, so only if this PR fully resolves it), "Related to #123", or the merge order if this is stacked on another PR. Detail meant to outlive this PR belongs in the code, the docs, or the ticket. If you used AI to help draft this, read it and edit it before you post. -->

## Risks and impact
<!-- Every change carries some risk; this is where you name the risks you know about. Answer the sections that apply and delete the rest. How much attention each deserves is your call. -->

### PHI and de-identification
<!-- Does this touch anonymization, DicomEdit scripts, an importer, the prearchive, or anything that writes into the archive? Could an identifier survive a path that used to scrub it? -->

### Security, authentication, and authorization
<!-- Do your changes add or modify roles, or change what data a user can see or what actions they can take? Could a user edit a REST path and reach a project they aren't a member of? Were you mindful of least privilege? See the [OWASP checklist](https://github.com/0xRadi/OWASP-Web-Checklist). -->

### Input data
<!-- Which edge cases did you consider? DICOM type 2 elements are required to be present but may be empty; private tags, multiframe objects, and non-conforming data from real scanners all show up in the wild. On bad data, does this fail loudly or quietly? -->

### Upgrade and compatibility
<!-- How do you know an existing site can take this upgrade? Say what you checked in the data model, schema, REST API, and dependencies, and whether a plugin compiled against the old signatures still loads. If something does break, name it and say who has to change. If there is a schema migration, how long does it run on a large archive, and does the site need to be down for it? -->

### Performance and scale
<!-- At what scale do we expect this to run — study size, session count, concurrent uploads? How have you verified that it holds up? -->

## Testing
<!-- Please don't delete this section. At a minimum, show proof that this change was deployed and executed successfully: a green build is not sufficient. Show the evidence — commands and their output, screenshots, before/after values, log lines, the data you threw at it — and reference the areas of impact above. A model writing a test is not the same as you confirming the behavior. Not every subsection below applies to every change; where one doesn't, say so in a line rather than leaving it blank. -->

### Unit tests
<!-- The Gradle tests in this repository. Which did you add or change, what behavior does each one pin, and how does a reviewer run them (`./gradlew :<module>:test --tests "..."`)? For a bugfix, does the new test fail without your change? -->

### REST integration tests
<!-- End-to-end coverage lives in [NrgXnat/xnat-rest-tests](https://github.com/NrgXnat/xnat-rest-tests), which exercises a running server over the REST API. If this change is observable through the API, link the companion PR there. If the suite already covers it, say which test. If it needs coverage you haven't written, say so and link the ticket. -->

### Manual verification
<!-- Your test plan, in enough detail that a reviewer could repeat it: the deployment and data you started from, the steps you took, and what you observed at each one. Screenshots for anything with a UI. What did you deliberately try to break — the edge cases you named above, empty or malformed input, the large case, the concurrent case? And what did you exercise that you didn't change, to show you haven't regressed the paths around this one? Where automated coverage doesn't exist for what you changed, this section carries the proof. -->

### Gaps
<!-- If there's something a reviewer would expect you to have tested and you didn't, say so: a named gap is more useful than a blank section. Coverage you decided against is worth a line too, with the reason. -->

## Note for reviewers
<!-- Where to look first, what you're unsure about, what you deliberately left out of scope, decisions you'd like a second opinion on. -->
