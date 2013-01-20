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
    # Get store URLs from the database Store table
    
    for url in flyerURL.values():
        if url:
            print("Crawling URL: ", url)
            f = urllib.request.urlopen(url)
            content = f.read()
            #print(content)

def createSchema():
    
    sql = "CREATE TABLE if not exists Grocery (_id INTEGER PRIMARY KEY, item_id INTEGER, raw_string TEXT, " + \
            "unit_price REAL, unit_type_id INTEGER, total_price REAL, start_date TEXT, end_date TEXT, " + \
            "line_number INTEGER, store_id INTEGER, update_date TEXT)"
    cur.execute(sql)
    sql = "CREATE TABLE if not exists Item (item_id INTEGER PRIMARY KEY, item_name TEXT, subcategory_id INTEGER)"
    cur.execute(sql)
    sql = "CREATE TABLE if not exists Store (store_id INTEGER PRIMARY KEY, store_name TEXT, store_address TEXT, store_url TEXT)"
    cur.execute(sql)
    sql = "CREATE TABLE if not exists Category (category_id INTEGER PRIMARY KEY, category_name TEXT)"
    cur.execute(sql)
    sql = "CREATE TABLE if not exists Subcategory (subcategory_id INTEGER PRIMARY KEY, subcategory_name TEXT, category_id INTEGER)"
    cur.execute(sql)
    
    # Re-create the Store entries
    sql = "DELETE FROM Store"
    cur.execute(sql)
    sql = "INSERT INTO Store (store_name, store_url) VALUES('metro', 'http://www.metro.ca/en/on/accessible-flyer.html?method=getAccessibleFlyer&idFlyer=1576')"
    cur.execute(sql)
    sql = "INSERT INTO Store (store_name, store_url) VALUES('loblaws', 'http://eflyerontario.loblaws.ca/cached_banner_pages/publication.aspx?BannerName=LOB')"
    cur.execute(sql)
    sql = "INSERT INTO Store (store_name, store_url) VALUES('foodbasics', 'http://www.foodbasics.ca/en/circulaire-accessible.html?method=getAccessibleFlyer&idFlyer=229')"
    cur.execute(sql)
    sql = "INSERT INTO Store (store_name, store_url) VALUES('nofrills', 'http://shopnofrills.ca/LCLOnline/flyers_landing_page.jsp?flyerView=accessible&flyerId=28100120')"
    cur.execute(sql)
    sql = "INSERT INTO Store (store_name, store_url) VALUES('sobeys', 'http://sob.ca.flyerservices.com/cached_banner_pages/publication.aspx?BannerName=SOB')"
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
