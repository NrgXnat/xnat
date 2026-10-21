#!/usr/bin/env python3
"""Spike codemod: Turbine RunData -> PipelineData for doBuildTemplate/doPerform overrides.

Transform (keeps method bodies untouched):
  doBuildTemplate([final] RunData <name>[, [final] Context <c>])   { ...
    ->
  doBuildTemplate([final] PipelineData pipelineData[, [final] Context <c>]) {
      final RunData <name> = pipelineData.getRunData();            // inserted
      ...                                                          // body unchanged (still uses <name>)
Adds `import org.apache.turbine.pipeline.PipelineData;` when needed.
Abstract declarations (no body, end in ';') get the signature change only.
"""
import re, sys, pathlib

ROOTS = ["xnat-web/src/main/java", "xdat/src/main/java"]
# match a concrete/abstract declaration: methodName( [final] RunData name [, [final] Context c] )
SIG = re.compile(
    r'\b(?P<m>doBuildTemplate|doPerform|isAuthorized|doOutput|getContentType)\(\s*(?P<f1>final\s+)?RunData\s+(?P<rn>\w+)'
    r'(?P<rest>\s*,\s*(?:final\s+)?Context\s+\w+\s*)?\s*\)'
)

changed_files = 0
methods = 0
edge = []

for root in ROOTS:
    for path in pathlib.Path(root).rglob("*.java"):
        text = path.read_text()
        if "RunData" not in text:
            continue
        out = []
        idx = 0
        file_hits = 0
        for m in SIG.finditer(text):
            # copy up to match
            out.append(text[idx:m.start()])
            f1 = m.group("f1") or ""
            rest = m.group("rest") or ""
            new_sig = f'{m.group("m")}({f1}PipelineData pipelineData{rest})'
            out.append(new_sig)
            after = m.end()
            # find the body '{' or abstract ';' after optional 'throws ...'
            j = after
            while j < len(text) and text[j] not in "{;":
                j += 1
            if j < len(text) and text[j] == "{":
                # insert local right after the opening brace, matching indentation loosely
                inject = "{\n        final RunData %s = pipelineData.getRunData();" % m.group("rn")
                out.append(text[after:j])          # e.g. " throws Exception "
                out.append(inject)
                idx = j + 1
            elif j < len(text) and text[j] == ";":
                out.append(text[after:j + 1])       # abstract: signature only
                idx = j + 1
            else:
                edge.append(f"{path}:{m.group('m')} (no body/abstract terminator found)")
                idx = after
            file_hits += 1
        out.append(text[idx:])
        if file_hits:
            new = "".join(out)
            if "import org.apache.turbine.pipeline.PipelineData;" not in new:
                new = new.replace(
                    "import org.apache.turbine.util.RunData;",
                    "import org.apache.turbine.pipeline.PipelineData;\nimport org.apache.turbine.util.RunData;",
                    1,
                )
            path.write_text(new)
            changed_files += 1
            methods += file_hits

print(f"files changed : {changed_files}")
print(f"methods xform : {methods}")
print(f"edge cases    : {len(edge)}")
for e in edge[:20]:
    print("  EDGE", e)
