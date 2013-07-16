#!/bin/bash

if [ $# -eq 0 ]
    then
    echo "usage: \n\tsh release_build_publish <version>\n\teg. sh release_build_publish 1.1.718 \n\tmake sure you have ran ant release before this"
    fi

s3cmd put ./bin/GroceryGo-release.apk s3://grocerygo/GroceryGo-$1.apk;