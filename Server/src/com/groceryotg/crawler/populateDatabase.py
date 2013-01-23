from subprocess import Popen, PIPE
import sys


# Fill in your MySQL user & password
mysql_user = "USERNAME"
mysql_password = "PASSWORD"
mysql_db = "groceryotg"
mysql_filename = "groceryotg_populate.sql"


def populateDatabase():
    
    # Execute the .sql file to create the database
    process = Popen('mysql %s -u%s -p%s' % (mysql_db, mysql_user, mysql_password), \
                    stdout=PIPE, stdin=PIPE, shell=True)
    output = process.communicate('source ' + mysql_filename)[0]


populateDatabase()