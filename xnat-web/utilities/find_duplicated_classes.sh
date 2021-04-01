#!/bin/bash
#
# web: find_duplicated_classes.sh
# XNAT http://www.xnat.org
# Copyright (c) 2019, Washington University School of Medicine and Howard Hughes Medical Institute
# All Rights Reserved
#  
# Released under the Simplified BSD.
#

[[ -z ${1} ]] && {
    for TC_ROOT in /var/lib/tomcat8 /var/lib/tomcat9 /var/lib/tomcat7 /var/lib/tomcat; do
        [[ -d ${TC_ROOT} ]] && { break; }
    done
    LIB_FOLDER=${TC_ROOT}/webapps/ROOT/WEB-INF/lib
} || {
    LIB_FOLDER="${1}"
}

[[ ! -d ${LIB_FOLDER} ]] && { echo "I couldn't find a valid folder to search: ${LIB_FOLDER} doesn't exist"; exit 1; }

echo "Checking all jar files in the folder ${LIB_FOLDER}..."
for JAR in $(ls ${LIB_FOLDER}/*.jar); do
    jar tf ${JAR} | grep '\.class$' | sed 's#^.*$#&\t'"$(basename ${JAR})"'#'
done > whole.tsv

psql --command="DROP TABLE IF EXISTS entries"
psql --command="CREATE TABLE entries (id SERIAL NOT NULL CONSTRAINT entries_pk PRIMARY KEY, entry VARCHAR(1024), jarfile VARCHAR(255) NOT NULL)"
psql --command="\COPY entries (entry, jarfile) FROM 'whole.tsv'"

psql --tuples-only --command="SELECT DISTINCT jarfile FROM (SELECT jarfile FROM entries WHERE entry IN (SELECT entry FROM (SELECT DISTINCT entry, count(*) AS combinations FROM entries WHERE entry LIKE '%.class' GROUP BY entry) totals WHERE combinations > 1 ORDER BY combinations DESC) ORDER BY entry, jarfile) duplicates ORDER BY jarfile" > jars_with_duplicate_classes.txt
DUP_COUNT=$(wc -l jars_with_duplicate_classes.txt)

[[ ${DUP_COUNT} == 0 ]] && {
	echo "There are no duplicated classes in your jar files."
	exit 0
}

echo "There are ${DUP_COUNT} jars that have duplicate entries:"
cat jars_with_duplicate_classes.txt

psql --tuples-only --no-align --field-separator=$'\t' --command="SELECT entry, string_agg(jarfile, ', ' ORDER BY jarfile) AS jarfiles FROM entries WHERE entry IN (SELECT entry FROM (SELECT DISTINCT entry, count(*) AS combinations FROM entries WHERE entry LIKE '%.class' GROUP BY entry) totals WHERE combinations > 1 ORDER BY combinations DESC) GROUP BY entry ORDER BY entry, jarfiles" > duplicates.tsv

echo "You can find the duplicate classes and the jars containing each class in the file duplicates.tsv. You can find the list of jars in the file jars_with_duplicate_classes.tsv."

psql --command="DROP TABLE IF EXISTS entries"
