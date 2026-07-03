# Jakarta shim JARs — security & compliance notes

Audience: security reviewers assessing an XNAT deployment that includes the byte-code-migrated ("shimmed")
JARs in `libs/`. This document explains what these artifacts are, how to independently verify them, how they
appear to SCA tooling, and the remediation roadmap.

## 1. What these artifacts are

Each JAR in `libs/` is an **unmodified upstream release** from Maven Central whose Java EE namespace
references (`javax.servlet.*` et al.) were mechanically rewritten to Jakarta EE (`jakarta.*`) using the
**Apache Tomcat jakartaee-migration tool 1.0.8** (`-profile=EE`), maintained by the Apache Tomcat project.

This is not a bespoke patching mechanism: Apache Tomcat 10+ ships the *same conversion built in* — a Java EE
WAR placed in the `webapps-javaee/` directory is converted automatically at deployment with the same tool.
This project performs that officially supported conversion at build time instead of deploy time, so the
shipped artifact is deterministic and verifiable.

No classes are added, removed, or functionally altered; only constant-pool references to renamed EE
packages change. `verify.sh` proves this (see §3).

## 2. Provenance

| Shimmed artifact (SHA-256) | Upstream source (Maven Central) |
|---|---|
| `org.restlet-1.1.10-jakarta.jar` `62ea62d79ca5d7b58f20860d25d52e701e73852e1a1541babf2a753ce293e051` | `org.restlet:org.restlet:1.1.10` |
| `com.noelios.restlet-1.1.10-jakarta.jar` `3a9448423c4d031b6bbb3dde2ff532e20cd8409494c889a7b1e9083d596528ac` | `com.noelios.restlet:com.noelios.restlet:1.1.10` |
| `com.noelios.restlet.ext.servlet-1.1.10-jakarta.jar` `53dec7023a47fac38a6c9a37984bacf0f97483e7f477f8d420c00361b2725f63` | `com.noelios.restlet:com.noelios.restlet.ext.servlet:1.1.10` |
| `org.restlet.ext.fileupload-1.1.10-jakarta.jar` `30fe6231b3a00b65d970e5a9528dd04eceb09697cae9b92b543ffe1e2ac6e5cf` | `org.restlet:org.restlet.ext.fileupload:1.1.10` |
| `turbine-2.3.3-jakarta.jar` `7c1eac7446286408a77cba33e85199c952e9e635b29b234a47c74689d03352b0` | `turbine:turbine:2.3.3` |
| `velocity-1.7-jakarta.jar` `05b894907fb3eef24bd5ef6a8763f06dd886d7b1e09c7dac24e399a46d4cf269` | `org.apache.velocity:velocity:1.7` |
| `velocity-tools-2.0-jakarta.jar` `5c2dbdbbac58a05624d8952620a25342e77117d7d8dc98b1323c42f1a3409cad` | `org.apache.velocity:velocity-tools:2.0` |
| `commons-fileupload-1.5-jakarta.jar` `67d69ec0c826fd2164d96743eae714e8a066b133d1ec301859cc1947f4987a9f` | `commons-fileupload:commons-fileupload:1.5` |

Transformation tool: `org.apache.tomcat:jakartaee-migration:1.0.8` (shaded), Maven Central.

A machine-readable pedigree is provided in `sbom-pedigree.cdx.json` (CycloneDX). Merge or attach it to the
deployment SBOM so vulnerability matching runs against the **upstream** coordinates.

## 3. Independent verification (reproducibility)

Any reviewer can reproduce and verify these artifacts without trusting this repository:

```bash
./rewrite.sh    # downloads upstream JARs + the Apache tool from Maven Central, re-runs the conversion
./verify.sh     # four gates: zero residual javax/servlet bytecode refs; jakarta refs present;
                # zero reflection-by-name string constants; JVM linkage test (21/21 key classes)
```

`rewrite.sh` pins the tool version; conversions are deterministic per input JAR.

## 4. How these artifacts appear to SCA scanners

- The upstream JARs (2007–2010 era, Ant-built) contain **no Maven `pom.properties`**; component
  identification typically comes from `MANIFEST.MF` metadata (e.g. `Bundle-SymbolicName: org.restlet`,
  `Bundle-Version: 1.1`), which the conversion **preserves**. Manifest-based scanners will therefore still
  identify the upstream components and report their advisories — vulnerabilities are *not* masked.
- Hash-based matchers will not match the upstream hashes and may report "unknown component". Use the
  CycloneDX pedigree file to resolve these to their upstream identities.

## 5. Known-advisory review status

The namespace conversion neither adds nor removes vulnerabilities: the risk profile is identical to the
upstream artifacts that XNAT has shipped in this configuration for many years. Known public advisories for
these components, with deployment-context reachability notes (**initial engineering assessment — to be
confirmed and signed off by a security review**):

| Component | Advisory | Reachability notes (to confirm) |
|---|---|---|
| velocity 1.7 | CVE-2020-13936 (sandbox escape via template modification) | Requires ability to modify server-side templates; in XNAT, template editing is an admin-only capability. Permanently remediated by the Turbine 7 / Velocity 2.x upgrade (evaluated, see roadmap). |
| velocity-tools 2.0 | CVE-2020-13959 (XSS in VelocityView error page) | XNAT does not use the velocity-tools `view` module (`VelocityViewServlet` is not registered); believed unreachable. |
| restlet 1.1.10 | CVE-2013-4221 / CVE-2013-4271 (XEE/deserialization in XML representations) | Affects `ObjectRepresentation`/XStream-based XML handling; XNAT's Restlet layer uses its own representation classes. Reachability review required; superseded endpoint-by-endpoint by the Restlet→xAPI migration. |
| turbine 2.3.3 | No direct core advisories known | Risk concentrated in transitive dependencies, which this build excludes or manages explicitly (see `xnat-web/build.gradle` exclusion list). |
| commons-fileupload 1.5 | — | 1.5 is the current fixed line (includes the CVE-2023-24998 DoS fix). |

## 6. Remediation roadmap (sunset plan)

These shims are a **transition mechanism with a documented sunset**, not a permanent state:

1. **Restlet**: endpoint-by-endpoint migration to XNAT's Spring MVC-based XAPI is planned and partially
   complete (~144 XAPI endpoints already exist). Target end state: `restlet/` package removed.
2. **Turbine/Velocity/velocity-tools**: upgrade to jakarta-native Turbine 7.0 + Velocity 2.x has been
   PoC-evaluated (bounded, ~11–19 person-weeks) and permanently removes the shimmed artifacts and the
   Velocity advisory above.
3. **commons-fileupload**: retired together with the Restlet fileupload path in (1).
