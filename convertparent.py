"""
Convert parent pom.xml file to a libs.versions.toml file
"""
try:
    from lxml import etree
except ModuleNotFoundError as e:
    import sys
    print("Must install lxml for this script to work.")
    sys.exit(1)


def standardize(raw: str) -> str:
    return raw.replace(".", "-").replace("_", "-").lower()


def get_version_ref(dependency_version: str, versions: dict[str, str]) -> str:
    """Find"""
    ref = None
    while dependency_version is not None and dependency_version.startswith("${"):
        ref = dependency_version[2:-len(".version")-1]  # Strip "${" and ".version}"
        ref = standardize(ref)
        dependency_version = versions.get(ref)
    return ref

pom = "http://maven.apache.org/POM/4.0.0"
ns = {"pom": pom}
left_tag_strip = "{" + pom + "}"
right_tag_strip = ".version"

infile = "parent/pom.xml"
outfile = "gradle/libs.versions.toml"

tree = etree.parse(infile)

# Collect a list of versions in the form
#   xnat = "1.8.11-SNAPSHOT"
versions = {
    standardize(el.tag[len(pom)+2:-len(".version")]) : el.text
    for el in tree.xpath("/pom:project/pom:properties/*", namespaces=ns)
    if el.tag.endswith('.version') and not 'java.' in el.tag
}

# Collect dependencies
dependencies = {}

for dep in tree.xpath(
        "/pom:project/pom:dependencyManagement/pom:dependencies/pom:dependency", namespaces=ns
):
    artifactId = dep.xpath('pom:artifactId', namespaces=ns)[0].text
    groupId = dep.xpath('pom:groupId', namespaces=ns)[0].text

    raw_version = dep.xpath('pom:version', namespaces=ns)[0].text
    version_ref = get_version_ref(raw_version, versions)
    version = raw_version if version_ref is None else version_ref
    version_attr = "version" if version_ref is None else "version.ref"

    # No dots allowed in library alias
    alias = standardize(artifactId)

    # Special handling for dcm4che5, otherwise we will get duplicate artifactIds
    if version == "dcm4che5":
        alias = alias.replace("dcm4che", "dcm4che5")

    # Special handling for axis, otherwise we will get duplicate artifactIds
    if alias[0:4] == "axis":
        alias = ("axis-" if groupId == "axis" else "apache-") + artifactId

    # Special handling for org.nrg:pipelineX
    if groupId == "org.nrg" and artifactId.startswith("pipeline"):
        # Rename pipelineX to pipeline-X
        alias = "pipeline-" + alias[len("pipeline"):]

    # Special handling for org.nrg.xnat.pipeline:X
    if groupId == "org.nrg.xnat.pipeline" and not artifactId.startswith("pipeline"):
        # Add "pipeline-" to the beginning
        alias = "pipeline-" + alias

    # Special handling for gradle-X-plugin version ref, which is incorrect
    if version.startswith("gradle-") and version.endswith("-plugin"):
        # Remove the "-plugin" from the end
        version = version[:-len("-plugin")]

    dependencies[alias] = \
        f'{{ module = "{groupId}:{artifactId}", {version_attr} = "{version}" }}'

# Special-case this javadoc coverage plugin so I don't have to go digging through the plugins section
dependencies["javadoc-coverage"] = \
    '{ module = "com.manoelcampos:javadoc-coverage", version.ref = "javadoc-coverage" }'

with open(outfile, 'w') as f:
    f.write("[versions]\n")
    for v_key, v_value in versions.items():
        if not v_value.startswith("${"):  # Skip version refs, e.g. ${xnat.version}
            f.write(f"{v_key} = \"{v_value}\"\n")

    f.write("\n[libraries]\n")
    for d_key, d_value in dependencies.items():
        f.write(f"{d_key} = {d_value}\n")
