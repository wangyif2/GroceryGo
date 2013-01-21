# This script crawls the following grocery store flyer websites:
# 1. Dominion - same as Metro
# 2. Loblaws
# 3. Food Basics
# 4. No Frills
# 4. Sobeys
#
# Requirements:
# Python 2.x (the MySQLdb module has not been updated to support Python 3.x yet)
# To set the python version in Eclipse: Window -> Preferences -> PyDev -> Interpreter-Python
#


import MySQLdb as mdb
import sqlalchemy
from subprocess import Popen, PIPE
import sys
import urllib
import urllib2


# Fill in your MySQL user & password
mysql_user = "USERNAME"
mysql_password = "PASSWORD"
mysql_db = "groceryotg"
mysql_filename = "groceryotg.sql"


def getFlyer():
    # Get store URLs from the database Store table
    cur.execute('SELECT * FROM Store ORDER BY store_name')
    data = cur.fetchall()
    for record in data:
        next_url = record[3]
        if next_url:
            print("Crawling URL: ", next_url)
            f = urllib2.urlopen(next_url)
            content = f.read()
            #print(content)


def createSchema():
    
    # Execute the .sql file to create the database
    process = Popen('mysql %s -u%s -p%s' % (mysql_db, mysql_user, mysql_password), \
                    stdout=PIPE, stdin=PIPE, shell=True)
    output = process.communicate('source ' + mysql_filename)[0]
    
    
con = None
# Uncomment below to create database:
#createSchema()

try:
    con = mdb.connect('localhost', mysql_user, mysql_password, mysql_db)
    cur = con.cursor()
    print("connected to database")
    
    # SQLAlchemy is a Python ORM
    print("SQLAlchemy version: ", sqlalchemy.__version__)
    
    #getFlyer()
    
except mdb.Error, e:
    print("error: %s" % e)
    sys.exit(1)
finally:
    if con:
        con.close()
        print("closed connection to database")
