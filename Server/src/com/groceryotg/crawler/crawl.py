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
mysql_user = "root"
mysql_password = "1123581321ff$$"
mysql_db = "groceryotg"

# TODO:
# 1) Pass in only the item part of the line string to getNouns, so it doesn't get confused with the price 
# 2) Build a language model of bigram probabilities to detect compound nouns (e.g. "potato chips" vs just "chips")
#    If a probability of word B to occur after word A is > 0.5, then it's a compound. 
# 3) Add a "Misc" subcategory in database, in case no subcategories match the line.
# 4) Add a "tags" column in Subcategory table in database (use that list of tags instead of the subcategory name)
#    That way, we can exclude words like "and" and improve efficiency.
# 5) Use all words in the list of tags when determining subcategory_id in classifier.py
#


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
            #print(soup)
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
        update_date = today.strftime("%Y-%m-%d")
        start_date = ""
        end_date = ""
        
        soup = BeautifulSoup(urllib2.urlopen(flyer_url))
        div_pages = soup('div')
        
        # Find the start and end dates
        months = {'Jan':1,'Feb':2,'Mar':3,'Apr':4,'May':5,'Jun':6,'Jul':7,'Aug':8,'Sep':9,'Oct':10,'Nov':11,'Dec':12}
        tag_dates = soup.find(text=re.compile('Jan|Feb|Mar|Apr|May|Jun|Jul|Aug|Sep|Oct|Nov|Dec[a-zA-Z]*\s[0-9]')).string
        pattern = re.compile('(Jan|Feb|Mar|Apr|May|Jun|Jul|Aug|Sep|Oct|Nov|Dec)[a-zA-Z]*\s([0-9]+)')
        matches = pattern.findall(tag_dates)
        pattern = re.compile('2[0-9]{3}')
        year_matches = pattern.findall(tag_dates)
        start_year = int(year_matches[0])
        if len(year_matches) > 1:
            end_year = int(year_matches[1])
        else:
            end_year = start_year
        
        start_date = datetime.datetime(start_year, months[matches[0][0]], int(matches[0][1])).strftime('%Y-%m-%d')
        end_date = datetime.datetime(end_year, months[matches[1][0]], int(matches[1][1])).strftime('%Y-%m-%d')
        print("start date: ", start_date)
        print("end date: ", end_date)
        print("update date: ", update_date)
        
        for page in div_pages:
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
                
                # The price is preceded either by a dollar sign ($) or a cent sign (\\xa2)
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
                #print(item_details)
                store_items += [item_details]
        
        items[store_id] = store_items
    return items


con = None

try:
    con = mdb.connect('localhost', mysql_user, mysql_password, mysql_db)
    cur = con.cursor()
    print("connected to database")
    
    #get sub category from database
    cur.execute('SELECT subcategory_id, subcategory_tag FROM subcategory ORDER BY subcategory_id')
    subcategory = cur.fetchall()
    
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
            tokens = item[0].split('PRICE')
            noun_list = getNouns.getNouns(tokens[0])
            #print(tokens[0])
            print(noun_list)
            
            # Step 3: Pass the list of nouns to the "classifier" module to classify the item into one subcategory
            subcategory_id = classifier.classify(noun_list, subcategory)
            
            print
            print
            
    # Step 4: Write to database
    
    
    
except mdb.Error, e:
    print("error: %s" % e)
    sys.exit(1)
finally:
    if con:
        con.close()
        print("closed connection to database")
