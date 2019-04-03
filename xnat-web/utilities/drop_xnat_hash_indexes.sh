#!/bin/bash
#
# web: drop_xnat_hash_indexes.sh
# XNAT http://www.xnat.org
# Copyright (c) 2019, Washington University School of Medicine and Howard Hughes Medical Institute
# All Rights Reserved
#  
# Released under the Simplified BSD.
#

function displayHelp() {
    echo "Available options:"
    echo " -e|--execute:            Indicates that the queries to drop hash indexes should be executed. If not specified,"
    echo "                          this script generates the required queries but just displays them."
    echo " -r|--restore:            Indicates that the dropped hash indexes should be restored after being dropped. Please"
    echo "                          note that this can cause performance issues on your database server while the indexes"
    echo "                          are being rebuilt. This defaults to true when the script is run in display mode (i.e.,"
    echo "                          the --execute option is not specified) and false when run in execute mode."
    echo " -n|--no-restore:         Indicates that the dropped hash indexes should NOT be restored after being dropped."
    echo "                          This can be used to prevent generating the CREATE INDEX statements when running this"
    echo "                          script in display mode."
    echo " -h|--host=HOSTNAME       Database server host or socket directory. It's worth noting that the --no-password"
    echo "                          option is automatically added to the psql command line when the --host option is"
    echo "                          specified. This may cause the connection to your database server to fail, but it's"
    echo "                          not practical to enter a password for each of what may be a LOT of queries. If this"
    echo "                          is the case, you can run this script in display mode and redirect the output SQL into"
    echo "                          a file. You can then run psql with the --file=FILE option using your captured SQL."
    echo " -p|--port=PORT:          Database server port"
    echo " -U|--username=USERNAME:  Database username"
}

# Set default values
EXECUTE_QUERIES="false"
CREATE_INDEXES="false"
FORCE_NO_CREATE="false"
PSQL_OPTIONS=""

while [[ $# -gt 0 ]]; do
    case ${1} in
        -e|--execute)
            EXECUTE_QUERIES="true"
            shift
            ;;
        -n|--no-restore)
            FORCE_NO_CREATE="true"
            shift
            ;;
        -r|--restore)
            CREATE_INDEXES="true"
            shift
            ;;
        -h)
            PSQL_OPTIONS+="--host=${2} --no-password"
            shift 2
            ;;
        --host=*)
            PSQL_OPTIONS+="--host=$(echo ${1} | cut -f 2 -d =) --no-password"
            shift
            ;;
        -p)
            PSQL_OPTIONS+="--port=${2} "
            shift 2
            ;;
        --port=*)
            PSQL_OPTIONS+="--port=$(echo ${1} | cut -f 2 -d =)"
            shift
            ;;
        -u)
            PSQL_OPTIONS+="--username=${2} "
            shift 2
            ;;
        --username=*)
            PSQL_OPTIONS+="--username=$(echo ${1} | cut -f 2 -d =)"
            shift
            ;;
        --help)
            displayHelp
            exit 0
            ;;
        *)
            echo "Unknown option: ${1}"
            displayHelp
            exit -1
            ;;
    esac
done

[[ ${EXECUTE_QUERIES} == "false" && ${FORCE_NO_CREATE} == "false" ]] && { CREATE_INDEXES="true"; }

[[ ${EXECUTE_QUERIES} == "true" ]] && {
    INDEX_COUNT=$(psql ${PSQL_OPTIONS} --no-align --tuples-only --command="SELECT count(indexname) FROM pg_indexes WHERE indexname LIKE '%entries_info%' AND indexdef LIKE '%hash%'")
    echo "You've specified the -e or --execute option. This will drop ${INDEX_COUNT} hash indexes from the database."
    read -n 1 -p "Press 'q' to quit or any other key to continue: " INPUT
    echo
    [[ ${INPUT} == "q" || ${INPUT} == "Q" ]] && { echo "Exiting at user request..."; exit 1; }
}

psql ${PSQL_OPTIONS} --no-align --tuples-only --field-separator="," --command="SELECT indexname, regexp_replace(indexdef, E'[\n\r]+', ' ', 'g') FROM pg_indexes WHERE indexdef LIKE '%hash%'" | \
while IFS= read INDEX; do
    DROP_INDEX="DROP INDEX $(echo ${INDEX} | cut -f 1 -d ,)"
    CREATE_INDEX="$(echo ${INDEX} | cut -f 2 -d ,)"
    if [[ ${EXECUTE_QUERIES} == "false" ]]; then
        echo "${DROP_INDEX};"
        [[ ${CREATE_INDEXES} == "true" ]] && { echo "${CREATE_INDEX};"; }
    else 
        echo psql ${PSQL_OPTIONS} --command="${DROP_INDEX}"
        [[ ${CREATE_INDEXES} == "true" ]] && { echo psql ${PSQL_OPTIONS} --command="${CREATE_INDEX}"; }
    fi
done
