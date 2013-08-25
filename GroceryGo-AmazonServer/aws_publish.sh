#!/bin/bash

if [ $# -eq 0 ]
    then
    echo "usage: \n\tsh aws_publish <path to war file> <version> <description>\n\teg. sh aws_publish ./out/Server_war.war 1.1.1 "published new version"\n\tcheck current version with elastic_beanstalk_describe_aplication_versions"
    fi

s3cmd put $1 s3://groceryotg-1.1.0/Server_war_$2.war;
elastic-beanstalk-create-application-version -a My'\' First'\' Elastic'\' Beanstalk'\' Application -d '"'$3'"' -l groceryotg-$2 -s groceryotg-1.1.0/Server_war_$2.war;
elastic-beanstalk-update-environment -e groceryotg -l groceryotg-$2;