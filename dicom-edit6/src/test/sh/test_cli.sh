#!/bin/bash
# Script to exercise the CLI

mkdir -p /tmp/de6
echo 'version "6.3"' >/tmp/de6/cli.das
echo '(0010,0010) := "fubar"' >>/tmp/de6/cli.das

dcmdump ../resources/dicom/1.2.840.113717.2.21733635.1-3-2-7b04bw.dcm | grep 0010,0010

# java -jar ~/Downloads/dicom-edit6-6.3.1-jar-with-dependencies.jar \
java -jar ../../../target/dicom-edit6-6.3.1-jar-with-dependencies.jar \
   -s /tmp/de6/cli.das \
   -i ../resources/dicom/1.2.840.113717.2.21733635.1-3-2-7b04bw.dcm \
   -o /tmp/de6/cli_test

dcmdump /tmp/de6/cli_test/1.2.840.113717.2.21733635.1-3-2-7b04bw.dcm | grep 0010,0010
