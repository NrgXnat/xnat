## Description
<!-- What changed and why, in your own words — the user-facing story and the technical one. Link the work: the ticket ("XNAT-1234"), "Closes #123" (auto-closes on merge, so only if this PR fully resolves it), "Related to #123", or the merge order if this is stacked on another PR. Detail meant to outlive this PR belongs in the code, the docs, or the ticket. If you used AI to help draft this, read it and edit it before you post. -->

## Impact
<!-- Every change carries some risk; this is where you name the risks you know about. Answer the sections that apply and delete the rest. How much attention each deserves is your call. -->

### PHI and de-identification
<!-- Does this touch anonymization, DicomEdit scripts, an importer, the prearchive, or anything that writes into the archive? Could an identifier survive a path that used to scrub it? -->

### Security, authentication, and authorization
<!-- Do your changes add or modify roles, or change what data a user can see or what actions they can take? Could a user edit a REST path and reach a project they aren't a member of? Were you mindful of least privilege? See the [OWASP checklist](https://github.com/0xRadi/OWASP-Web-Checklist). -->

### Input data
<!-- Which edge cases did you consider? DICOM type 2 elements are required to be present but may be empty; private tags, multiframe objects, and non-conforming data from real scanners all show up in the wild. On bad data, does this fail loudly or quietly? -->

### Upgrade and compatibility
<!-- Changes to the data model, schema, REST API, or dependencies? Does anything break for an existing site on upgrade, or for a plugin compiled against the old signatures? If there is a schema migration, how long does it run on a large archive, and does the site need to be down for it? -->

### Performance and scale
<!-- At what scale do we expect this to run — study size, session count, concurrent uploads? How have you verified that it holds up? -->

## Testing
<!-- Please don't delete this section. At a minimum, show proof that a computer has executed the lines you changed: a green build does not by itself mean your new code ran. Cover the automated tests you added and the manual verification you performed, and reference the areas of impact above. Show the evidence — commands and their output, screenshots, before/after values, log lines, the data you threw at it. A model writing a test is not the same as you confirming the behavior. Where automated coverage doesn't exist for what you changed, describe the scenarios you exercised by hand. And if there's something a reviewer would expect you to have tested and you didn't, say so: a named gap is more useful than a blank section. -->

### How a reviewer can verify this
<!-- The shortest path for someone else to see it work: (1) setup required — branch, build, data; (2) actions to perform; (3) what they should observe. -->

## Note for reviewers
<!-- Where to look first, what you're unsure about, what you deliberately left out of scope, decisions you'd like a second opinion on. If this also needs to land on `develop-1.9.x`, say so. -->
