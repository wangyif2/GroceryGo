# This script crawls the following grocery store flyer websites:
# 1. Dominion - same as Metro
# 2. Loblaws
# 3. Food Basics
# 4. No Frills
# 5. Price Chopper - PDF (the images have clear black borders)
# 6. Sobeys
#
# Requirements:
# Python 3.x or higher
#
# Possible categories:
# - dairy
# - breads & bakery
# - produce
# - meat
# - seafood
# - non-edible / household items
# 

import sqlite3 as lite
import urllib.request


flyerURL = {"metro": "http://www.metro.ca/en/on/accessible-flyer.html?method=getAccessibleFlyer&idFlyer=1576",
            "loblaws": "http://eflyerontario.loblaws.ca/cached_banner_pages/publication.aspx?BannerName=LOB",
            "foodbasics":"http://www.foodbasics.ca/en/circulaire-accessible.html?method=getAccessibleFlyer&idFlyer=229",
            "nofrills":"http://shopnofrills.ca/LCLOnline/flyers_landing_page.jsp?flyerView=accessible&flyerId=28100120",
            "sobeys":"http://sob.ca.flyerservices.com/cached_banner_pages/publication.aspx?BannerName=SOB"}

def getFlyer():
    for url in flyerURL.values():
        if url:
            print("Crawling URL: ", url)
            f = urllib.request.urlopen(url)
            content = f.read()
            #print(content)

def createSchema():
    sql = "create table if not exists Items (id INTEGER PRIMARY KEY, store TEXT, storeLocation TEXT, " + \
            "rawItem TEXT, category TEXT, price REAL, unit TEXT, dateStart TEXT, dateEnd TEXT)"
    cur.execute(sql)
    con.commit()


con = None
con = lite.connect('test.db')

with con:   
    # Testing connection with database:------
    cur = con.cursor()
    cur.execute('SELECT SQLITE_VERSION()')
    data = cur.fetchone()
    print("SQLite version: ", data)
    createSchema()
    # ---------------------------------------
    
    # Crawl all the flyers and parse into (item, price) pairs
    getFlyer()
    
    # Do categorization of each item line based on trained classifier
    # ...
    
    # Write to database
    # ...
    
    print("done")