# This script crawls the following grocery store flyer websites:
# 1. Dominion - same as Metro
# 2. Loblaws
# 3. Food Basics
# 4. No Frills
# 4. Sobeys
#
# Requirements:
# - Python 2.x (the MySQLdb module has not been updated to support Python 3.x yet)
# - BeautifulSoup (for HTML parsing)
# - nltk (for NLP)
# - sqlalchemy (for ORM)


from bs4 import BeautifulSoup
import MySQLdb as mdb
import sqlalchemy
import sys
import urllib
import urllib2
from urlparse import urlparse
import nltk
import re
import datetime

import getNouns
import classifier


# Fill in your MySQL user & password
mysql_user = "USERNAME"
mysql_password = "PASSWORD"
mysql_db = "groceryotg"


def getFlyer():
    '''No input parameters, accesses the database directly. 
       Finds this week's URL of the accessible plain-text flyer web pages for each grocery store
       in the database. Return a dictionary of {store_id : flyer_url} pairs. '''
    flyers = {}
    cur.execute('SELECT store_id, store_url FROM Store ORDER BY store_name')
    data = cur.fetchall()
    for record in data:
        store_id, next_url = record[0], record[1]
        flyer_url = ""
        if next_url and next_url.find("foodbasics") != -1:
            print("Crawling store: %d" % store_id)
            hostname = urlparse(next_url).hostname
            soup = BeautifulSoup(urllib2.urlopen(next_url))
            linkElem = soup('span', text=re.compile(r'View accessible flyer'))[0].parent
            flyer_url = "http://" + hostname + linkElem['href']
        elif next_url and next_url.find("metro") != -1:
            flyer_url = ""
        elif next_url and next_url.find("nofrills") != -1:
            flyer_url = ""
        if flyer_url:
            flyers[int(store_id)] = flyer_url
    return flyers

def parseFlyer(flyers):
    '''Takes a dictionary of {store_id : flyer_url} pairs, assuming that "flyer_url" is a valid URL. 
       Parses the accessible plain-text only flyer webpages to identify items. 
       Returns a dictionary of {store_id : items} where "items" is a list of 
       [item_raw_string, unit_price, unit_type_id, total_price, start_date, end_date, page_number, 
       update_date].'''
    items = {}
    stores = flyers.keys()
    for store_id in stores:
        flyer_url = flyers[store_id]
        store_items = []
        page_number = 0
        print("Parsing store %s, url %s" % (store_id, flyer_url))
        
        today = datetime.date.today()
        update_date = today.strftime("%Y/%m/%d")
        start_date = ""
        end_date = ""
        
        soup = BeautifulSoup(urllib2.urlopen(flyer_url))
        divPages = soup('div')
        
        for page in divPages:
            page_number += 1
            #print("\n\nPAGE: %d" % page_number)
            page_lines = re.sub('<[bB][rR]\s*?>', '', page.text).split('\n')
            page_lines = filter(None, map(lambda x: x.strip('\t').strip('\r').strip('\n').strip(), page_lines))
            for line in page_lines:
                unit_price = 0
                unit_type_id = 0
                total_price = 0
                
                # Split up sentences and identify items.
                #tokenizer = nltk.data.load('tokenizers/punkt/english.pickle')
                #sentences = tokenizer.tokenize(line)
                
                # Splitting on price is more effective at separating items, because individual
                # items often contain several sentences of item information.
                tokens = line.split('PRICE')
                pattern = re.compile('([\\xa2]|[$])([0-9.]+)')
                total_price = pattern.findall(line)[0]
                if total_price[0] == "$":
                    total_price = float(total_price[1])
                else:
                    total_price = float(total_price[1])/100.0
                    
                # TODO: calculate unit price by dividing if necessary
                unit_price = total_price
                
                item_details = [line, unit_price, unit_type_id, total_price, \
                                start_date, end_date, page_number, update_date]
                store_items += [item_details]
        
        items[store_id] = store_items
    #print(items)
    return items


con = None

try:
    con = mdb.connect('localhost', mysql_user, mysql_password, mysql_db)
    cur = con.cursor()
    print("connected to database")
    
    # TODO: replace SQL calls with SQLAlchemy (a Python ORM)
    #print("SQLAlchemy version: ", sqlalchemy.__version__)
    
    # Step 1: Parse the flyers into (item, price) pairs
    flyers = getFlyer()
    items = parseFlyer(flyers)
    
    # Step 2: Pass the items one by one to the "getNouns" module to get a list of nouns for each item
    getNouns.init()
    stores = items.keys()
    for store_id in stores:
        item_list = items[store_id]
        for item in item_list:
            noun_list = getNouns.getNouns(item[0])
            print
            print(item[0])
            print(noun_list)
            
            
            # Step 3: Pass the list of nouns to the "classifier" module to classify the item into one subcategory
            #classifier(noun_list)
            subcategory_id = classifier.classify(noun_list)
    
    # Step 4: Write to database
    
    
    
except mdb.Error, e:
    print("error: %s" % e)
    sys.exit(1)
finally:
    if con:
        con.close()
        print("closed connection to database")
