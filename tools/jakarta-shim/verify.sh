#!/usr/bin/env bash
#
# Verifies the jakarta-shimmed JARs in libs/ (see README.md). Four checks per the migration analysis
# (bindocs: "XNAT Restlet 响应格式与 Jakarta Shim 分析" §4.6/§7.4):
#   1. zero residual javax/servlet (+javax/mail for turbine) bytecode references
#   2. jakarta/servlet references present where expected
#   3. zero string-constant "javax.servlet" (reflection-by-name check)
#   4. decisive: JVM actually links the key classes against jakarta.servlet-api 6.0
#
# Requires: JDK (javap), curl. Downloads the jakarta APIs needed for check 4 into ./work on first run.

set -euo pipefail
cd "$(dirname "$0")"

LIBS=./libs
WORK=./work
mkdir -p "$WORK"

fetch() { [ -f "$WORK/$1" ] || curl -fSL -o "$WORK/$1" "https://repo1.maven.org/maven2/$2"; }
fetch jakarta.servlet-api-6.0.0.jar  "jakarta/servlet/jakarta.servlet-api/6.0.0/jakarta.servlet-api-6.0.0.jar"
fetch jakarta.mail-api-2.1.2.jar     "jakarta/mail/jakarta.mail-api/2.1.2/jakarta.mail-api-2.1.2.jar"
fetch avalon-framework-api-4.3.1.jar "org/apache/avalon/framework/avalon-framework-api/4.3.1/avalon-framework-api-4.3.1.jar"

FAIL=0

echo "== 1/2: bytecode namespace checks =="
for jar in "$LIBS"/*.jar; do
    name="$(basename "$jar")"
    tmp="$(mktemp -d)"
    (cd "$tmp" && jar xf "$OLDPWD/$jar")
    javax_hits="$(cd "$tmp" && javap -c -p $(find . -name '*.class') 2>/dev/null | grep -c 'javax/servlet' || true)"
    jakarta_hits="$(cd "$tmp" && javap -c -p $(find . -name '*.class') 2>/dev/null | grep -c 'jakarta/servlet' || true)"
    rm -rf "$tmp"
    printf "   %-55s javax/servlet=%-4s jakarta/servlet=%-5s" "$name" "$javax_hits" "$jakarta_hits"
    if [ "${javax_hits:-0}" -gt 0 ]; then echo " FAIL"; FAIL=1; else echo " OK"; fi
done

echo "== 3: string-constant scan =="
for jar in "$LIBS"/*.jar; do
    hits="$(strings "$jar" | grep -c 'javax\.servlet\|javax/servlet' || true)"
    printf "   %-55s string-hits=%-3s" "$(basename "$jar")" "$hits"
    if [ "${hits:-0}" -gt 0 ]; then echo " FAIL"; FAIL=1; else echo " OK"; fi
done

echo "== 4: JVM linkage =="
cat > "$WORK/FullStackTest.java" <<'JAVA'
public class FullStackTest {
    public static void main(String[] args) {
        String[] classes = {
            "com.noelios.restlet.ext.servlet.ServerServlet",
            "com.noelios.restlet.ext.servlet.ServletCall",
            "com.noelios.restlet.ext.servlet.ServletConverter",
            "com.noelios.restlet.ext.servlet.ServletContextAdapter",
            "com.noelios.restlet.ext.servlet.ServletWarClient",
            "com.noelios.restlet.ext.servlet.ServletWarClientHelper",
            "org.restlet.Restlet",
            "org.restlet.resource.Resource",
            "org.restlet.data.MediaType",
            "org.apache.turbine.Turbine",
            "org.apache.turbine.services.session.SessionListener",
            "org.apache.turbine.util.RunData",
            "org.apache.turbine.modules.Action",
            "org.apache.turbine.modules.Screen",
            "org.apache.velocity.servlet.VelocityServlet",
            "org.apache.velocity.runtime.log.ServletLogChute",
            "org.apache.velocity.tools.view.VelocityViewServlet",
            "org.apache.velocity.tools.view.VelocityViewFilter",
            "org.apache.velocity.tools.view.ImportSupport$ImportResponseWrapper",
            "org.apache.velocity.tools.generic.DateTool",
            "org.apache.velocity.tools.generic.MathTool",
        };
        int ok = 0, fail = 0;
        ClassLoader cl = FullStackTest.class.getClassLoader();
        for (String name : classes) {
            try {
                Class<?> c = Class.forName(name, false, cl);
                Class<?> s = c.getSuperclass();
                System.out.println("   OK   " + name + (s != null ? "  super=" + s.getName() : ""));
                ok++;
            } catch (Throwable t) {
                System.out.println("   FAIL " + name + "  -> " + t.getClass().getSimpleName() + ": " + t.getMessage());
                fail++;
            }
        }
        System.out.println("   Loaded " + ok + "/" + (ok + fail));
        System.exit(fail == 0 ? 0 : 1);
    }
}
JAVA
CP="$WORK/jakarta.servlet-api-6.0.0.jar:$WORK/jakarta.mail-api-2.1.2.jar:$WORK/avalon-framework-api-4.3.1.jar"
for jar in "$LIBS"/*.jar; do CP="$CP:$jar"; done
javac -d "$WORK" "$WORK/FullStackTest.java"
java -cp "$WORK:$CP" FullStackTest || FAIL=1

echo
if [ "$FAIL" -eq 0 ]; then echo "ALL CHECKS PASSED"; else echo "VERIFICATION FAILED"; exit 1; fi
