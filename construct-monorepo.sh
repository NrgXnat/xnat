#!/usr/bin/env bash

input_file=${1?"Pass the input file name"}
branch=${2-"master"}

while IFS=' ' read -r REPO URL; do
    echo "${REPO}: ${URL}"
    
    # Add the remote repo and fetch its contents
    git remote add $REPO $URL
    git fetch --no-tags $REPO
    
    # Merge in the remote repo
    git subtree add -P ${REPO} ${REPO} ${branch}
done < $input_file
