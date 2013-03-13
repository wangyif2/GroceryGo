# This script crawls the following grocery_table store flyer websites:
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
# - MySQLdb (for connecting to mysql)

import ast
from bs4 import BeautifulSoup
import cookielib
import datetime
import htmllib
import logging
import MySQLdb as mdb
import nltk
import os
import re
#import sqlalchemy
import sys
import time
import traceback
import urllib
import urllib2
from urlparse import urlparse


# Initialize log file (filename based on YYYY_MM_DD_hhmmsscc.log)
timestamp = datetime.datetime.now()
logname = "./log/" + str(timestamp.year).zfill(4) + "_" + str(timestamp.month).zfill(2) + "_" + str(timestamp.day).zfill(2) + "_" + \
          str(timestamp.hour).zfill(2) + str(timestamp.minute).zfill(2) + str(timestamp.second).zfill(2) + "_" + \
          str(timestamp.microsecond) + ".log"
print("writing log to %s..." % logname)

# Define logging level (if you set this to logging.DEBUG, the debug print messages will be displayed) 
logging.basicConfig(filename=logname, format='%(asctime)s:%(levelname)s: %(message)s', level=logging.INFO)


# Keep these BELOW the logging setup. Otherwise, their loggers get registered as the root.
import getNouns
import classifier


# Start timing how long it takes to run the whole script
start_timer = time.time()

# Fill in your MySQL user & password
mysql_user = "root"
mysql_password = ""
mysql_db = "groceryotg"


# TODO:
# Done. 1) Pass in only the item part of the line string to getNouns, so it doesn't get confused with the price 
# Done. 2) Build a language model of bigram probabilities to detect compound nouns (e.g. "potato chips" vs just "chips")
#          If a probability of word B to occur after word A is > 0.5, then it's a compound. 
# Done. 3) Add a "Misc" subcategory in database, in case no subcategories match the line.
# Done. 4) Add a "tags" column in Subcategory table in database (use that list of tags instead of the subcategory name)
#          That way, we can exclude words like "and" and improve efficiency.
# Done. 5) Use all words in the list of tags when determining subcategory_id in classifier.py
#


# When getting a table's primary key from MySQL, this is the index of the primary key column name
SQL_INDEX_PRIMARY_KEY = 4


def unescape(s):
    '''Takes a string with escaped HTML special characters, e.g. "param1=value1&amp;param2=value2". 
       Returns an unescaped version of the string, e.g. "param1=value1&param2=value2". '''
    p = htmllib.HTMLParser(None)
    p.save_bgn()
    p.feed(s)
    return p.save_end()

def getFlyer():
    '''No input parameters, accesses the database directly. 
       Finds this week's URL of the accessible plain-text flyer web pages for each grocery_table store
       in the database. Parses the accessible plain-text only flyer webpages to identify items. 
       Return a dictionary of {flyer_id : item} pairs, where "items" is a list of 
       [item_raw_string, unit_price, unit_type_id, total_price, start_date, end_date, page_number, 
       update_date].'''
    flyers = {}
    items = {}
    
    today = datetime.datetime.now()
    update_date = today.strftime("%Y-%m-%d %H:%M:%S")
    
    # Get unit IDs from the database
    cur.execute('SELECT unit_id, unit_type_name FROM Unit;')
    units = cur.fetchall()
    
    cur.execute('SELECT StoreParent.store_parent_id, Flyer.flyer_url, Store.flyer_id FROM ((Store INNER JOIN Flyer ON Flyer.flyer_id=Store.flyer_id) INNER JOIN StoreParent ON StoreParent.store_parent_id=Store.store_parent_id) ORDER BY StoreParent.store_parent_name')
    data = cur.fetchall()
    for record in data:
        store_id, next_url, flyer_id = record[0], record[1], record[2]
        flyer_url = ""
        if next_url:
            #logging.info("next url: %s" % next_url)
            
            # Metro
            if store_id == 1:
                try:
                    flyer_url = ""
                    logging.info("Crawling store: %d" % store_id)
                    hostname = urlparse(next_url).hostname
                    soup = BeautifulSoup(urllib2.urlopen(next_url))
                    foundElems = soup('a', text=re.compile(r'Metro Ontario Flyer'))
                    if foundElems:
                        linkElem = foundElems[0]
                        flyer_page = "http://" + hostname + linkElem['href']
                        soup = BeautifulSoup(urllib2.urlopen(flyer_page))
                        linkElem = soup('span', text=re.compile(r'View accessible flyer'))[0].parent
                        flyer_url = "http://"+ hostname + linkElem['href']
                        
                        store_items = []
                        line_number = 0
                        
                        start_date = ""
                        end_date = ""
                        
                        soup = BeautifulSoup(urllib2.urlopen(flyer_url))
                        logging.info("URL: %s" % flyer_url)
                        div_pages = soup.find_all(lambda tag: tag.name=='div' and tag.has_key('style') and not tag.has_key('id'))
                        
                        # Find the start and end dates
                        months = {'Jan':1,'Feb':2,'Mar':3,'Apr':4,'May':5,'Jun':6,'Jul':7,'Aug':8,'Sep':9,'Oct':10,'Nov':11,'Dec':12}
                        
                        tag_dates = soup.find(text=re.compile('Jan|Feb|Mar|Apr|May|Jun|Jul|Aug|Sep|Oct|Nov|Dec[a-zA-Z]*\s[0-9]')).string
                        pattern = re.compile('(Jan|Feb|Mar|Apr|May|Jun|Jul|Aug|Sep|Oct|Nov|Dec)[a-zA-Z.]*\s([0-9]{1,2})(?=[^0-9])')
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
                        logging.info("start date: %s" % start_date)
                        logging.info("end date: %s" % end_date)
                        logging.info("update date: %s" % update_date)
                        
                        for page in div_pages:
                            
                            page_lines = re.sub('<[bB][rR]\s*?>', '', page.text).split('\n')
                            page_lines = filter(None, map(lambda x: x.strip('\t').strip('\r').strip('\n').strip(), page_lines))
                            for line in page_lines:
                                line_number += 1
                                unit_price = None
                                unit_type_id = None
                                total_price = None
                                
                                # The price is preceded either by a dollar sign ($) or a cent sign (\\xa2)
                                pattern = re.compile('([\\xa2]|[$])([0-9.]+)')
                                if pattern.findall(line):
                                    total_price = pattern.findall(line)[0]
                                    if total_price[0] == "$":
                                        total_price = float(total_price[1])
                                    else:
                                        total_price = float(total_price[1])/100.0
                                
                                # TODO: calculate unit price by dividing if necessary
                                unit_price = total_price
                                
                                raw_item = line
                                raw_price = ""
                                tag_price = "PRICE"
                                index_price = line.find(tag_price)
                                if index_price != -1:
                                    raw_price = line[index_price+len(tag_price):].strip().strip(":").strip()
                                    raw_item = line[:index_price].strip()
                                
                                item_details = [raw_item, raw_price, unit_price, unit_type_id, total_price, \
                                                start_date, end_date, line_number, flyer_id, update_date]
                                
                                #logging.info(item_details)
                                store_items += [item_details]
                        
                        items[flyer_id] = store_items
                except urllib2.URLError as e:
                    logging.info("Could not connect to store %d due to Error %d (%s)" % (store_id, e.errno, e.strerror))
            # Loblaws
            elif store_id == 2:
                try:
                    logging.info("Crawling store: %d" % store_id)
                    parsed_url = urlparse(next_url)
                    hostname = parsed_url.hostname
                    
                    cj = cookielib.CookieJar()
                    opener = urllib2.build_opener(urllib2.HTTPCookieProcessor(cj))
                    response = opener.open(next_url).read()
                    soup = BeautifulSoup(response)
                    
                    # Fill out the form asking for Province, City, Store ID
                    the_form = soup.findAll('form')[0]
                    form_url = the_form['action'].lstrip('.').lstrip('/')
                    
                    # Get the query portion of the form URL
                    query_string = urlparse(form_url).query
                    the_url = parsed_url.scheme + "://" + hostname + "/LCL/" + "PublicationDirector.ashx?" + query_string
                    
                    post_data = []
                    
                    # input tags (hidden)
                    input_list = the_form.findChildren('input')
                    for param in input_list:
                        if param.has_key("value"):
                            post_data += [(param['id'], param['value'])]
                        else:
                            post_data += [(param['id'], '')]
                            
                    # select tags (visible)
                    # 9 = Ontario, 4802 = Toronto, storeID = Queen & Portland store
                    post_data += [('ddlProvince','9'), ('ddlCity', '4802'), \
                                  ('ddlStore', '83e70c01-ca1b-4b08-95f3-a84f769f303f'), \
                                  ('btnSelectStore', '')]
                    post_data = urllib.urlencode(post_data)
                    the_url += "&storeid=" + "83e70c01-ca1b-4b08-95f3-a84f769f303f"
                    
                    response = opener.open(the_url, post_data).read()
                    soup = BeautifulSoup(response)
                    
                    # Click on the "Accessible Flyer" link to get to the actual flyer page
                    accessible_link = soup('a', text=re.compile(r'Accessible Flyer'))[0]['href']
                    accessible_link = "http://" + hostname + "/LCL/" + accessible_link
                    #logging.info("Accessing link: %s" % accessible_link)
                    response = opener.open(accessible_link).read()
                    soup = BeautifulSoup(response)
                    
                    # On the last day of a flyer's period, the page may change to give the user
                    # the option of selecting either the current or next week's flyer from a 
                    # list of publications
                    flag_nopub = False
                    if soup('div', {'id': 'PublicationList'}):
                        if soup('span', {'id':'lblNoPublication'}):
                            flag_nopub = True
                        else:
                            pub_link = soup('span', {'class':'publicationDate'})[-1].parent['href']
                            pub_link = "http://" + hostname + "/LCL/" + pub_link.lstrip('.').lstrip('/')
                            response = opener.open(pub_link).read()
                            soup = BeautifulSoup(response)
                    
                    if flag_nopub:
                        logging.info("No publications for store %d this week." % store_id)
                    else:
                        # Before getting to the actual flyer page, submit an intermediate page web form
                        the_form = soup('form', {'name':'form1'})[0]
                        target_url = the_form['action']
                        children = the_form.findChildren()
                        post_data = []
                        for param in children:
                            if param.has_key("value"):
                                post_data += [(param['id'], param['value'])]
                            elif param.has_key("id"):
                                post_data += [(param['id'], '')]
                        
                        #logging.info("target url: %s" % target_url)
                        post_data = urllib.urlencode(post_data)
                        response = opener.open(target_url, post_data).read()
                        
                        # On the actual flyer page, the iframe data is populated via an AJAX call
                        # Simulate the AJAX call by fetching all necessary parameters.
                        
                        # Default values (in case not found on page):
                        BANNER_NAME = "LOB"
                        PUBLICATION_ID = "b556f81a-909c-4aa2-8f67-00f800ab9d67"
                        PUBLICATION_TYPE = "1"
                        CUSTOMER_NAME = "LCL"
                        LANGUAGE_ID = "1"
                        BANNER_ID = "3d5f3800-c099-11d9-9669-0800200c9a66"
                        
                        # Find values from the HTML:
                        # NB: Look-behind regex requires fixed-width pattern, so we can't match for arbitrary
                        # number of spaces between "=" sign and the variables..
                        match_banner = re.search(r"(?<=BANNER[_]NAME [=]['])[a-zA-Z]+(?=['])", response)
                        if match_banner:
                            BANNER_NAME = str(match_banner.group(0))
                        
                        match_pub_id = re.search(r"(?<=PUBLICATION[_]ID [=] ['])[-a-zA-Z0-9]+(?=['])", response)
                        if match_pub_id:
                            PUBLICATION_ID = str(match_pub_id.group(0))
                            
                        match_pub_type = re.search(r"(?<=PUBLICATION[_]TYPE [=] ['])[0-9]+(?=['])", response)
                        if match_pub_type:
                            PUBLICATION_TYPE = str(match_pub_type.group(0))
                        
                        match_cust_name = re.search(r"(?<=CUSTOMER[_]NAME [=] ['])[a-zA-Z]+(?=['])", response)
                        if match_cust_name:
                            CUSTOMER_NAME = str(match_cust_name.group(0))
                        
                        match_language_id = re.search(r"(?<=LANGUAGE[_]ID [=] )[0-9]+(?=[;])", response)
                        if match_language_id:
                            LANGUAGE_ID = str(match_language_id.group(0))
                        
                        match_banner_id = re.search(r"(?<=BANNER[_]ID [=] ['])[-0-9a-zA-Z]+(?=['])", response)
                        if match_banner_id:
                            BANNER_ID = str(match_banner_id.group(0))
                        
                        ajax_url = urlparse(target_url)
                        url_path = ajax_url.path[:ajax_url.path.rfind("/")+1]
                        page_id = 1
                        
                        ajax_query = ajax_url.scheme + "://" + ajax_url.netloc + url_path + \
                            "AJAXProxy.aspx?bname=" + BANNER_NAME + "&AJAXCall=GetPublicationData.aspx?" + \
                            "view=TEXT" + "&version=Flash" + "&publicationid=" + PUBLICATION_ID + \
                            "&publicationtype=" + PUBLICATION_TYPE + "&bannername=" + BANNER_NAME + \
                            "&customername=" + CUSTOMER_NAME + "&pageid1=" + str(page_id) + \
                            "&languageid=" + LANGUAGE_ID + "&bannerid=" + BANNER_ID
                        logging.info("URL: %s" % ajax_query)
                        response = opener.open(ajax_query).read()
                        dict_items = ast.literal_eval(response)
                        
                        # Parse into a list of items
                        data_list = dict_items["textdata"]
                        store_items = []
                        
                        line_number = 0
                        start_date = ""
                        end_date = ""
                        
                        # Find the start and end dates
                        months = {'Jan':1,'Feb':2,'Mar':3,'Apr':4,'May':5,'Jun':6,'Jul':7,'Aug':8,'Sep':9,'Oct':10,'Nov':11,'Dec':12}
                        
                        pattern = re.compile('(Jan|Feb|Mar|Apr|May|Jun|Jul|Aug|Sep|Oct|Nov|Dec)[a-zA-Z.]*\s([0-9]{1,2})(?=[^0-9])')
                        matches = pattern.findall(response)
                        pattern = re.compile('2[0-9]{3}')
                        year_matches = pattern.findall(tag_dates)
                        start_year = int(year_matches[0])
                        if len(year_matches) > 1:
                            end_year = int(year_matches[1])
                        else:
                            end_year = start_year
                        
                        start_date = datetime.datetime(start_year, months[matches[0][0]], int(matches[0][1])).strftime('%Y-%m-%d')
                        end_date = datetime.datetime(end_year, months[matches[1][0]], int(matches[1][1])).strftime('%Y-%m-%d')
                        logging.info("start date: %s" % start_date)
                        logging.info("end date: %s" % end_date)
                        logging.info("update date: %s" % update_date)
                        
                        data_list = filter(lambda x: x if x.has_key('regiontypeid') and x['regiontypeid']=='1' else None, data_list)
                        for item in data_list:
                            line_number += 1
                            raw_item = item['title'] + ", " + item['description'] 
                            
                            # Price format:
                            # 1) $1.50              (dollars)
                            # 2) 99c                (cents)
                            # 3) 4/$5               (ratio)
                            # 4) $3.99 - $4.29      (range)
                            unit_price = None
                            unit_type_id = None
                            total_price = None
                            
                            raw_price = item['price']
                            orig_price = raw_price
                            
                            numeric_pattern = re.compile("[0-9]+")
                            numeric_only = re.compile("^[0-9.]+$")
                            
                            # If the price contains any spaces, e.g. "Starting from $19.99", then 
                            # split on space and keep the elements that contains numeric chars
                            if raw_price.find(" ") != -1:
                                raw_price = " ".join(filter(lambda x: x if numeric_pattern.findall(x) else None, raw_price.split(" ")))
                            
                            if raw_price and numeric_pattern.findall(raw_price):
                                # If a range given, take the lowest value
                                if raw_price.find("-") != -1:
                                    raw_price = raw_price.split("-")[0].strip()
                                
                                index_ratio = raw_price.find("/")
                                index_dollar = raw_price.find("$")
                                index_cents = raw_price.find("\xa2")
                                #logging.info(raw_price)
                                if index_ratio != -1:
                                    if numeric_only.findall(raw_price[:index_ratio]):
                                        # If the first half is numeric only, e.g. "2 / $5", then it is
                                        # indeed a ratio
                                        num_products = float(raw_price[:index_ratio])
                                        total_price = raw_price[index_ratio+1:]
                                        if index_dollar != -1:
                                            the_price = total_price.strip().strip("$").strip()
                                            if numeric_only.findall(the_price):
                                                total_price = float(the_price)
                                                # Default unit_price
                                                unit_price = total_price / num_products
                                    
                                        else:
                                            the_price = total_price.strip("\xa2").strip("\xc2")
                                            if numeric_only.findall(the_price):
                                                total_price = float(the_price) / 100.0
                                                # Default unit_price
                                                unit_price = total_price / num_products
                                    elif numeric_only.findall(raw_price[:index_ratio].strip("$").strip("\xa2").strip("\xc2")):
                                        # Otherwise, it's a range, e.g. "$33 / $36". Take the first 
                                        # price.
                                        total_price = float(raw_price[:index_ratio].strip("$").strip("\xa2").strip("\xc2"))
                                        unit_price = total_price
                                elif index_dollar != -1:
                                    the_price = raw_price.strip("$")
                                    if numeric_only.findall(the_price):
                                        total_price = float(the_price)
                                        # Default unit_price
                                        unit_price = total_price
                                elif index_cents != -1:
                                    the_price = raw_price.strip("\xa2").strip("\xc2")
                                    if numeric_only.findall(the_price):
                                        total_price = float(the_price) / 100.0
                                        # Default unit_price
                                        unit_price = total_price
                                
                                # When price units are specified, the unit price is usually given in
                                # the "priceunits" key-value pair
                                price_units = item['priceunits']
                                if price_units:
                                    index_or = price_units.find("or ")
                                    index_kg = price_units.find("/kg")
                                    if index_or != -1:
                                        dollar_matches = re.search(r'(?<=[$])[0-9.]+', price_units)
                                        cent_matches = re.search(r'(?<=or )[0-9.]+', price_units)
                                        if dollar_matches:
                                            unit_price = float(dollar_matches.group(0))
                                        elif cent_matches:
                                            unit_price = float(cent_matches.group(0))/100.0
                                    elif index_kg != -1:
                                        price_matches = re.search(r'[0-9.]+(?=/kg)', price_units)
                                        if price_matches:
                                            unit_price = float(price_matches.group(0))
                                            unit_type_id = filter(lambda x: x if x[1]=='kg' else None,units)[0][0]
                            
                            item_details = [raw_item, orig_price, unit_price, unit_type_id, total_price, \
                                            start_date, end_date, line_number, flyer_id, update_date]
                            
                            store_items += [item_details]
                            
                        items[flyer_id] = store_items
                except urllib2.URLError as e:
                    logging.info("Could not connect to store %d due to Error %d (%s)" % (store_id, e.errno, e.strerror))
            # Food Basics
            elif store_id == 3:
                try:
                    logging.info("Crawling store: %d" % store_id)
                    hostname = urlparse(next_url).hostname
                    soup = BeautifulSoup(urllib2.urlopen(next_url))
                    linkElem = soup('span', text=re.compile(r'View accessible flyer'))[0].parent
                    flyer_url = "http://" + hostname + linkElem['href']
                    
                    store_items = []
                    line_number = 0
                    #logging.info("Parsing store %s, url %s" % (store_id, flyer_url))
                    
                    start_date = ""
                    end_date = ""
                    
                    soup = BeautifulSoup(urllib2.urlopen(flyer_url))
                    div_pages = soup('div')
                    
                    # Find the start and end dates
                    months = {'Jan':1,'Feb':2,'Mar':3,'Apr':4,'May':5,'Jun':6,'Jul':7,'Aug':8,'Sep':9,'Oct':10,'Nov':11,'Dec':12}
                    
                    tag_dates = soup.find(text=re.compile('Jan|Feb|Mar|Apr|May|Jun|Jul|Aug|Sep|Oct|Nov|Dec[a-zA-Z]*\s[0-9]')).string
                    pattern = re.compile('(Jan|Feb|Mar|Apr|May|Jun|Jul|Aug|Sep|Oct|Nov|Dec)[a-zA-Z.]*\s([0-9]{1,2})(?=[^0-9])')
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
                    logging.info("start date: %s" % start_date)
                    logging.info("end date: %s" % end_date)
                    logging.info("update date: %s" % update_date)
                    
                    for page in div_pages:
                        
                        page_lines = re.sub('<[bB][rR]\s*?>', '', page.text).split('\n')
                        page_lines = filter(None, map(lambda x: x.strip('\t').strip('\r').strip('\n').strip(), page_lines))
                        for line in page_lines:
                            line_number += 1
                            unit_price = 0
                            unit_type_id = None
                            total_price = None
                            
                            # Split up sentences and identify items.
                            #tokenizer = nltk.data.load('tokenizers/punkt/english.pickle')
                            #sentences = tokenizer.tokenize(line)
                            
                            # The price is preceded either by a dollar sign ($) or a cent sign (\\xa2)
                            pattern = re.compile('([\\xa2]|[$])([0-9.]+)')
                            if pattern.findall(line):
                                total_price = pattern.findall(line)[0]
                                if total_price[0] == "$":
                                    total_price = float(total_price[1])
                                else:
                                    total_price = float(total_price[1])/100.0
                            
                            # TODO: calculate unit price by dividing if necessary
                            unit_price = total_price
                            
                            raw_item = line
                            raw_price = ""
                            tag_price = "PRICE"
                            index_price = line.find(tag_price)
                            if index_price != -1:
                                raw_price = line[index_price+len(tag_price):].strip().strip(":").strip()
                                raw_item = line[:index_price].strip()
                            
                            item_details = [raw_item, raw_price, unit_price, unit_type_id, total_price, \
                                            start_date, end_date, line_number, flyer_id, update_date]
                            
                            #logging.info(item_details)
                            store_items += [item_details]
                    
                    items[flyer_id] = store_items
                except urllib2.URLError as e:
                    logging.info("Could not connect to store %d due to Error %d (%s)" % (store_id, e.errno, e.strerror))
            # No Frills
            elif store_id == 4:
                try:
                    flyer_url = ""
                    logging.info("Crawling store: %d" % store_id)
                    parsed_url = urlparse(next_url)
                    hostname = parsed_url.hostname
                    
                    cj = cookielib.CookieJar()
                    opener = urllib2.build_opener(urllib2.HTTPCookieProcessor(cj))
                    response = opener.open(next_url).read()
                    soup = BeautifulSoup(response)
                    
                     # Click on the "Accessible Flyer" link to get to the actual flyer page
                    accessible_link = soup('a', text=re.compile(r'Accessible Flyer'))[0]['href']
                    accessible_link = "http://" + hostname + "/LCL/" + accessible_link
                    response = opener.open(accessible_link).read()
                    soup = BeautifulSoup(response)
                    
                    # Before getting to the actual flyer page, submit an intermediate page web form
                    parsed_accessible = urlparse(accessible_link)
                    index_end = parsed_accessible.path.rfind("/")
                    
                    # Select store: Nicholson's (Bloor St W)
                    link_store = parsed_accessible.scheme + "://" + parsed_accessible.netloc + parsed_accessible.path[:index_end+1] +\
                                "publicationdirector.ashx?PublicationType=32&OrganizationId=797d6dd1-a19f-4f1c-882d-12d6601dc376&" +\
                                "BannerId=1f2ff19d-2888-44b3-93ea-1905aa0d9756&Language=EN&BannerName=NOFR&" +\
                                "Version=Text&pubclass=1&province=9&city=4802&storeid=2f476909-e9c6-41a8-90fd-7bc8a2e14e03"
                    response = opener.open(link_store).read()
                    soup = BeautifulSoup(response)
                    
                    # On the last day of a flyer's period, the page may change to give the user
                    # the option of selecting either the current or next week's flyer from a 
                    # list of publications
                    if soup('div', {'id': 'PublicationList'}):
                        pub_link = soup('span', {'class':'publicationDate'})[-1].parent['href']
                        pub_link = "http://" + hostname + "/LCL/" + pub_link.lstrip('.').lstrip('/')
                        response = opener.open(pub_link).read()
                        soup = BeautifulSoup(response)
                    
                    # Before getting to the actual flyer page, submit an intermediate page web form
                    the_form = soup('form', {'name':'form1'})[0]
                    target_url = the_form['action']
                    children = the_form.findChildren()
                    post_data = []
                    for param in children:
                        if param.has_key("value"):
                            post_data += [(param['id'], param['value'])]
                        elif param.has_key("id"):
                            post_data += [(param['id'], '')]
                            
                    post_data = urllib.urlencode(post_data)
                    response = opener.open(target_url, post_data).read()
                    
                    # On the actual flyer page, the iframe data is populated via an AJAX call
                    # Simulate the AJAX call by fetching all necessary parameters.
                    
                    # Default values (in case not found on page):
                    BANNER_NAME = "NOFR"
                    PUBLICATION_ID = "38706e85-01a0-4b00-94d5-25ea0cbe8eb8"
                    PUBLICATION_TYPE = "32"
                    CUSTOMER_NAME = "LCL"
                    LANGUAGE_ID = "1"
                    BANNER_ID = "1f2ff19d-2888-44b3-93ea-1905aa0d9756"
                    
                    # Find values from the HTML:
                    # NB: Look-behind regex requires fixed-width pattern, so we can't match for arbitrary
                    # number of spaces between "=" sign and the variables..
                    match_banner = re.search(r"(?<=BANNER[_]NAME [=]['])[a-zA-Z]+(?=['])", response)
                    if match_banner:
                        BANNER_NAME = str(match_banner.group(0))
                    
                    match_pub_id = re.search(r"(?<=PUBLICATION[_]ID [=] ['])[-a-zA-Z0-9]+(?=['])", response)
                    if match_pub_id:
                        PUBLICATION_ID = str(match_pub_id.group(0))
                        
                    match_pub_type = re.search(r"(?<=PUBLICATION[_]TYPE [=] ['])[0-9]+(?=['])", response)
                    if match_pub_type:
                        PUBLICATION_TYPE = str(match_pub_type.group(0))
                    
                    match_cust_name = re.search(r"(?<=CUSTOMER[_]NAME [=] ['])[a-zA-Z]+(?=['])", response)
                    if match_cust_name:
                        CUSTOMER_NAME = str(match_cust_name.group(0))
                    
                    match_language_id = re.search(r"(?<=LANGUAGE[_]ID [=] )[0-9]+(?=[;])", response)
                    if match_language_id:
                        LANGUAGE_ID = str(match_language_id.group(0))
                    
                    match_banner_id = re.search(r"(?<=BANNER[_]ID [=] ['])[-0-9a-zA-Z]+(?=['])", response)
                    if match_banner_id:
                        BANNER_ID = str(match_banner_id.group(0))
                    
                    ajax_url = urlparse(target_url)
                    url_path = ajax_url.path[:ajax_url.path.rfind("/")+1]
                    page_id = 1
                    
                    ajax_query = ajax_url.scheme + "://" + ajax_url.netloc + url_path + \
                        "AJAXProxy.aspx?bname=" + BANNER_NAME + "&AJAXCall=GetPublicationData.aspx?" + \
                        "view=TEXT" + "&version=Flash" + "&publicationid=" + PUBLICATION_ID + \
                        "&publicationtype=" + PUBLICATION_TYPE + "&bannername=" + BANNER_NAME + \
                        "&customername=" + CUSTOMER_NAME + "&pageid1=" + str(page_id) + \
                        "&languageid=" + LANGUAGE_ID + "&bannerid=" + BANNER_ID
                    logging.info("URL: %s" % ajax_query)
                    response = opener.open(ajax_query).read()
                    dict_items = ast.literal_eval(response)
                    
                    # Parse into a list of items
                    data_list = dict_items["textdata"]
                    store_items = []
                    
                    line_number = 0
                    start_date = ""
                    end_date = ""
                    
                    # Find the start and end dates
                    months = {'Jan':1,'Feb':2,'Mar':3,'Apr':4,'May':5,'Jun':6,'Jul':7,'Aug':8,'Sep':9,'Oct':10,'Nov':11,'Dec':12}
                    
                    pattern = re.compile('(Jan|Feb|Mar|Apr|May|Jun|Jul|Aug|Sep|Oct|Nov|Dec)[a-zA-Z.]*\s([0-9]{1,2})(?=[^0-9])')
                    matches = pattern.findall(response)
                    pattern = re.compile('2[0-9]{3}')
                    year_matches = pattern.findall(tag_dates)
                    start_year = int(year_matches[0])
                    if len(year_matches) > 1:
                        end_year = int(year_matches[1])
                    else:
                        end_year = start_year
                    
                    start_date = datetime.datetime(start_year, months[matches[0][0]], int(matches[0][1])).strftime('%Y-%m-%d')
                    end_date = datetime.datetime(end_year, months[matches[1][0]], int(matches[1][1])).strftime('%Y-%m-%d')
                    logging.info("start date: %s" % start_date)
                    logging.info("end date: %s" % end_date)
                    logging.info("update date: %s" % update_date)
                    
                    data_list = filter(lambda x: x if x.has_key('regiontypeid') and x['regiontypeid']=='1' else None, data_list)
                    for item in data_list:
                        line_number += 1
                        raw_item = item['title'] + ", " + item['description'] 
                        
                        # Price format:
                        # 1) $1.50              (dollars)
                        # 2) 99c                (cents)
                        # 3) 4/$5               (ratio)
                        # 4) $3.99 - $4.29      (range)
                        unit_price = None
                        unit_type_id = None
                        total_price = None
                        
                        numeric_pattern = re.compile("[0-9]+")
                        numeric_only = re.compile("^[0-9.]+$")
                        
                        raw_price = item['price']
                        orig_price = raw_price
                        if raw_price:
                            # If a range given, take the lowest value
                            if raw_price.find("-") != -1:
                                raw_price = raw_price.split("-")[0].strip()
                            
                            # Replace any occurrences of "for" with "/"
                            # e.g., "3 for $5" becomes "3 / $5"
                            raw_price = re.sub("\sfor\s", " / ", raw_price)
                            
                            index_ratio = raw_price.find("/")
                            index_dollar = raw_price.find("$")
                            index_cents = raw_price.find("\xa2")
                            if index_ratio != -1:
                                num_products = float(raw_price[:index_ratio])
                                total_price = raw_price[index_ratio+1:]
                                if index_dollar != -1:
                                    total_price = float(total_price.strip().strip("$").strip())
                                else:
                                    total_price = float(total_price.strip("\xa2").strip("\xc2")) / 100.0
                                
                                # Default unit_price
                                unit_price = total_price / num_products
                            
                            elif index_dollar != -1:
                                total_price = float(raw_price.strip("$"))
                                
                                # Default unit_price
                                unit_price = total_price
                            elif index_cents != -1:
                                if numeric_only.findall(raw_price.strip("\xa2").strip("\xc2")):
                                    
                                    total_price = float(raw_price.strip("\xa2").strip("\xc2")) / 100.0
                                
                                    # Default unit_price
                                    unit_price = total_price
                            
                            # When price units are specified, the unit price is usually given in
                            # the "priceunits" key-value pair
                            price_units = item['priceunits']
                            if price_units:
                                index_or = price_units.find("or ")
                                index_kg = price_units.find("/kg")
                                if index_or != -1:
                                    dollar_matches = re.search(r'(?<=[$])[0-9.]+', price_units)
                                    cent_matches = re.search(r'(?<=or )[0-9.]+', price_units)
                                    if dollar_matches:
                                        unit_price = float(dollar_matches.group(0))
                                    elif cent_matches:
                                        unit_price = float(cent_matches.group(0))/100.0
                                elif index_kg != -1:
                                    price_matches = re.search(r'[0-9.]+(?=/kg)', price_units)
                                    if price_matches:
                                        unit_price = float(price_matches.group(0))
                                        unit_type_id = filter(lambda x: x if x[1]=='kg' else None,units)[0][0]
                        
                        item_details = [raw_item, orig_price, unit_price, unit_type_id, total_price, \
                                        start_date, end_date, line_number, flyer_id, update_date]
                        
                        store_items += [item_details]
                        
                    items[flyer_id] = store_items
                except urllib2.URLError as e:
                    logging.info("Could not connect to store %d due to Error %d (%s)" % (store_id, e.errno, e.strerror))
                
            # Sobeys
            elif store_id == 5:
                flyer_url = ""
                logging.info("Crawling store: %d" % store_id)
                parsed_url = urlparse(next_url)
                soup = BeautifulSoup(urllib2.urlopen(next_url))
                
                # Fill out form with province, city and store selection
                formElem = soup('form', {'id':'form1'})[0]
                target_url = parsed_url.scheme + "://" + parsed_url.netloc + formElem['action']
                post_data = []
                
                input_list = formElem.findChildren('input')
                for param in input_list:
                    if param.has_key("value"):
                        post_data += [(param['id'], param['value'])]
                    else:
                        post_data += [(param['id'], '')]
                
                # Select Sobeys-Spadina store
                post_data += [('store', '934')]
                post_data = urllib.urlencode(post_data)
                
                response = opener.open(target_url, post_data).read()
                soup = BeautifulSoup(response)
                accessible_link = parsed_url.scheme + "://" + parsed_url.netloc + \
                                  soup('a', text=re.compile(r'Accessible Flyer'))[0]['href']
                
                # Before getting to the actual flyer page, submit an intermediate page web form
                response = opener.open(accessible_link).read()
                soup = BeautifulSoup(response)
                the_form = soup('form', {'name':'form1'})[0]
                target_url = the_form['action']
                children = the_form.findChildren()
                post_data = []
                for param in children:
                    if param.has_key("value"):
                        post_data += [(param['id'], param['value'])]
                    else:
                        post_data += [(param['id'], '')]
                        
                post_data = urllib.urlencode(post_data)
                response = opener.open(target_url, post_data).read()
                
                # On the actual flyer page, the iframe data is populated via an AJAX call
                # Simulate the AJAX call by fetching all necessary parameters.
                
                # Default values (in case not found on page):
                BANNER_NAME = "SOB"
                PUBLICATION_ID = "ac10ac3c-5cd1-46ef-a1e9-f02d77a422d9"
                PUBLICATION_TYPE = "1"
                CUSTOMER_NAME = "SOB"
                LANGUAGE_ID = "1"
                BANNER_ID = "0f69e65d-a96e-4871-8f86-a5fe7dde96c0"
                
                # Find values from the HTML:
                # NB: Look-behind regex requires fixed-width pattern, so we can't match for arbitrary
                # number of spaces between "=" sign and the variables..
                match_banner = re.search(r"(?<=BANNER[_]NAME [=]['])[a-zA-Z]+(?=['])", response)
                if match_banner:
                    BANNER_NAME = str(match_banner.group(0))
                
                match_pub_id = re.search(r"(?<=PUBLICATION[_]ID [=] ['])[-a-zA-Z0-9]+(?=['])", response)
                if match_pub_id:
                    PUBLICATION_ID = str(match_pub_id.group(0))
                    
                match_pub_type = re.search(r"(?<=PUBLICATION[_]TYPE [=] ['])[0-9]+(?=['])", response)
                if match_pub_type:
                    PUBLICATION_TYPE = str(match_pub_type.group(0))
                
                match_cust_name = re.search(r"(?<=CUSTOMER[_]NAME [=] ['])[a-zA-Z]+(?=['])", response)
                if match_cust_name:
                    CUSTOMER_NAME = str(match_cust_name.group(0))
                
                match_language_id = re.search(r"(?<=LANGUAGE[_]ID [=] )[0-9]+(?=[;])", response)
                if match_language_id:
                    LANGUAGE_ID = str(match_language_id.group(0))
                
                match_banner_id = re.search(r"(?<=BANNER[_]ID [=] ['])[-0-9a-zA-Z]+(?=['])", response)
                if match_banner_id:
                    BANNER_ID = str(match_banner_id.group(0))
                
                ajax_url = urlparse(target_url)
                url_path = ajax_url.path[:ajax_url.path.rfind("/")+1]
                page_id = 1
                
                ajax_query = ajax_url.scheme + "://" + ajax_url.netloc + url_path + \
                    "AJAXProxy.aspx?bname=" + BANNER_NAME + "&AJAXCall=GetPublicationData.aspx?" + \
                    "view=TEXT" + "&version=Flash" + "&publicationid=" + PUBLICATION_ID + \
                    "&publicationtype=" + PUBLICATION_TYPE + "&bannername=" + BANNER_NAME + \
                    "&customername=" + CUSTOMER_NAME + "&pageid1=" + str(page_id) + \
                    "&languageid=" + LANGUAGE_ID + "&bannerid=" + BANNER_ID
                logging.info("URL: %s" % ajax_query)
                response = opener.open(ajax_query).read()
                dict_items = ast.literal_eval(response)
                
                # Parse into a list of items
                data_list = dict_items["textdata"]
                store_items = []
                
                line_number = 0
                start_date = ""
                end_date = ""
                
                # Find the start and end dates
                months = {'Jan':1,'Feb':2,'Mar':3,'Apr':4,'May':5,'Jun':6,'Jul':7,'Aug':8,'Sep':9,'Oct':10,'Nov':11,'Dec':12}
                
                pattern = re.compile('(Jan|Feb|Mar|Apr|May|Jun|Jul|Aug|Sep|Oct|Nov|Dec)[a-zA-Z.]*\s([0-9]{1,2})(?=[^0-9])')
                matches = pattern.findall(response)
                pattern = re.compile('2[0-9]{3}')
                year_matches = pattern.findall(tag_dates)
                start_year = int(year_matches[0])
                if len(year_matches) > 1:
                    end_year = int(year_matches[1])
                else:
                    end_year = start_year
                
                start_date = datetime.datetime(start_year, months[matches[0][0]], int(matches[0][1])).strftime('%Y-%m-%d')
                end_date = datetime.datetime(end_year, months[matches[1][0]], int(matches[1][1])).strftime('%Y-%m-%d')
                logging.info("start date: %s" % start_date)
                logging.info("end date: %s" % end_date)
                logging.info("update date: %s" % update_date)
                
                data_list = filter(lambda x: x if x.has_key('regiontypeid') and x['regiontypeid']=='1' else None, data_list)
                for item in data_list:
                    line_number += 1
                    raw_item = item['title'] + ", " + item['description'] 
                    
                    # Price format:
                    # 1) $1.50                (dollars)
                    # 2) 99c                  (cents)
                    # 3) 4/$5                 (ratio)
                    # 4) $3.99 - $4.29        (range)
                    # 5) BUY ONE GET ONE FREE (string)
                    unit_price = None
                    unit_type_id = None
                    total_price = None
                    
                    raw_price = item['price']
                    orig_price = raw_price
                    
                    numeric_pattern = re.compile("[0-9]+")
                    
                    # If the price contains any spaces, e.g. "Starting from $19.99", then 
                    # split on space and keep the elements that contains numeric chars
                    if raw_price.find(" ") != -1:
                        raw_price = " ".join(filter(lambda x: x if numeric_pattern.findall(x) else None, raw_price.split(" ")))
                    
                    if raw_price and numeric_pattern.findall(raw_price):
                        # If a range given, take the lowest value
                        if raw_price.find("-") != -1:
                            raw_price = raw_price.split("-")[0].strip()
                        
                        index_ratio = raw_price.find("/")
                        index_dollar = raw_price.find("$")
                        index_cents = raw_price.find("\xa2")
                        if index_ratio != -1:
                            num_products = float(raw_price[:index_ratio])
                            total_price = raw_price[index_ratio+1:]
                            if index_dollar != -1:
                                total_price = float(total_price.strip().strip("$").strip())
                            else:
                                total_price = float(total_price.strip("\xa2").strip("\xc2")) / 100.0
                            
                            # Default unit_price
                            unit_price = total_price / num_products
                        
                        elif index_dollar != -1:
                            total_price = float(raw_price.strip("$"))
                            
                            # Default unit_price
                            unit_price = total_price
                            
                        elif index_cents != -1:
                            total_price = float(raw_price.strip("\xa2").strip("\xc2")) / 100.0
                            
                            # Default unit_price
                            unit_price = total_price
                        
                        # When price units are specified, the unit price is usually given in
                        # the "priceunits" key-value pair
                        price_units = item['priceunits']
                        if price_units:
                            index_or = price_units.find("or ")
                            index_kg = price_units.find("/kg")
                            if index_or != -1:
                                dollar_matches = re.search(r'(?<=[$])[0-9.]+', price_units)
                                cent_matches = re.search(r'(?<=or )[0-9.]+', price_units)
                                if dollar_matches:
                                    unit_price = float(dollar_matches.group(0))
                                elif cent_matches:
                                    unit_price = float(cent_matches.group(0))/100.0
                            elif index_kg != -1:
                                price_matches = re.search(r'[0-9.]+(?=/kg)', price_units)
                                if price_matches:
                                    unit_price = float(price_matches.group(0))
                                    unit_type_id = filter(lambda x: x if x[1]=='kg' else None,units)[0][0]
                    
                    item_details = [raw_item, orig_price, unit_price, unit_type_id, total_price, \
                                    start_date, end_date, line_number, flyer_id, update_date]
                    
                    store_items += [item_details]
                    
                items[flyer_id] = store_items
                
                
            logging.info('\n')
        
    return items


def evaluateAccuracy(store_id, labels,  category_map, item_list = None, noun_list = None):
    '''Takes a list of correct classifications, "targets", and a list of predicted classifications, "labels". 
       Returns None if either list is empty or the lists are not of the same length. Returns the fraction 
       of correctly classified items otherwise (as a floating point value between 0 and 1). '''
    
    # Read in the correct list of targets for this store's flyer
    file_in = open('flyer_' + str(store_id) + '.txt', 'rU')
    file_contents = file_in.read()
    file_in.close()
    
    if not file_contents:
        logging.info("Classification accuracy for store %d could not be determined due to missing labelled targets" % store_id)
        return None
    
    targets = map(lambda x: int(x), filter(None, file_contents.replace(os.linesep, ',').split(',')))
    '''
    if (not targets or not labels) or (len(targets) != len(labels)):
        logging.info("Classification accuracy for store %d could not be determined due to missing labelled targets" % store_id)
        return None
    '''
    if len(targets) == 0:
        logging.info("Classification accuracy for store %d could not be determined due to missing labelled targets" % store_id)
        return None
    
    correctly_classified = 0
    correctly_classified_category = 0
    for i in range(len(targets)):
        if i >= len(labels):
            logging.info("Too many targets compared to labels")
            break;
        #logging.info("(predicted: %d, actual: %d): %s" % (labels[i], targets[i], item_list[i]))
        if targets[i] == labels[i]:
            correctly_classified += 1
        elif item_list:
            logging.debug("Misclassified item (predicted: %d, actual: %d): %s" % (labels[i], targets[i], noun_list[i]))
        if category_map[targets[i]] == category_map[labels[i]]:
            correctly_classified_category += 1
    
    return [float(correctly_classified) / float(len(targets)), float(correctly_classified_category) / float(len(targets))]            



#******************************************************************************
# An interface for accessing a database table, handles writing data to table
#******************************************************************************

class TableInterface:
    
    def __init__(self, con, table_name): 
        '''Default constructor. Takes two arguments: "con" - a valid database 
           connection to the GroceryOTG database, and "table_name" - a valid 
           string name of a database table.''' 
        
        # A list of rows of data, buffered to be inserted into database table
        self.data = []
        
        # A list of field names for the database table
        self.columns = []
        
        self.dbcon = con
        self.dbcur = con.cursor()
        self.table_name = table_name
        
        # Fetch list of columns from database
        self.dbcur.execute("DESCRIBE " + table_name)
        cols = self.dbcur.fetchall()
        
        # Keep the field names only (this contains all auto-increment fields and table primary keys)
        self.columns = [x[0] for x in cols]
        
        # The primary key column names for the table
        self.primary_key = []
        self.dbcur.execute("SHOW KEYS FROM " + self.table_name + " WHERE Key_name = 'PRIMARY'")
        res = self.dbcur.fetchall()
        if len(res) > 0:
            # Grab only the primary key column name from each entry in the results
            for key in res:
                if key:
                    # Remove the primary key from the columns list and get its index
                    key_index = self.columns.index(key[SQL_INDEX_PRIMARY_KEY])
                    self.columns.pop(key_index)
                    self.primary_key += [key_index]
        logging.debug("Created a TableInterface with primary key: %s, columns: %s" % (str(self.primary_key), self.columns))
        
    
    def add_data(self, data_list):
        '''Takes one argument, "data_list" - a list of values corresponding to one row 
           to be inserted into the table. Assumes the values are in the same order as 
           the columns in the database. Returns False if the provided list is invalid. 
           Returns True otherwise.'''
        
        if len(data_list) != len(self.columns):
            return False
        
        # NB: For blank values, you should pass in None, not NULL
        self.data += [data_list]
        return True

    def add_batch(self, data_matrix):
        '''Takes one argument, "data_matrix" - a list of lists of values, each corresponding 
           to one row to be inserted into the table. Assumes the values are in the same order as 
           the columns in the database. Returns False if the provided list is invalid. 
           Returns True otherwise.'''
        
        if not len(data_matrix) or len(data_matrix[0]) != len(self.columns):
            return False
        
        for row in data_matrix:
            self.data += [row]
            
        return True

    def get_data(self):
        '''Takes no arguments. Returns the table's buffered data, as a list of lists, where each corresponds 
           to one row to be inserted into the table.'''
        return self.data
    
    def write_data(self):
        '''Takes no arguments. Writes the buffered rows of values stored in "data" to the 
           database table, if they don't already exist in the table. Returns a list of the 
           newly created row ID's (or the existing row ID's), in the order in which 
           they were created.'''
        
        logging.debug("Writing data to database table...")
        id_list = []
        
        column_str = ", ".join(self.columns)
        type_str = ", ".join(["%s"] * len(self.columns))
        where_clause = ""
        for counter in range(len(self.columns)):
            if where_clause:
                where_clause += " AND "
            where_clause += self.columns[counter] + "=%s"
        
        sql_exists = "SELECT * FROM " + self.table_name + " WHERE " + where_clause
        sql = "INSERT INTO " + self.table_name + " (" + column_str + ") VALUES (" + type_str + ")"
        formatted_data = [tuple(x) for x in self.data]
        
        for item in self.data:
            # Check if the row with these data already exists in the table
            # If it does, return the existing row ID
            cur.execute(sql_exists, item)
            res = cur.fetchall()
            if res:
                # Return the first primary key for the row
                id_list += [res[0][self.primary_key[0]]]
            # Otherwise, insert the row and return the new row ID
            else:
                cur.execute(sql, item)
                new_id = cur.lastrowid
                id_list += [new_id]
        
        #lines_inserted = self.dbcur.executemany(sql, formatted_data)
        logging.info("Wrote %d lines into table %s" %(len(id_list), self.table_name))
        
        # Commit changes to database
        self.dbcon.commit()
        
        # Clear the buffer
        self.data = []
        
        return id_list
        

# ***************************************************************************
# ***************************************************************************
con = None

try:
    con = mdb.connect('localhost', mysql_user, mysql_password, mysql_db)
    cur = con.cursor()
    logging.info("Connected to database")
    
    # Get subcategories from database
    cur.execute('SELECT subcategory_id, subcategory_tag FROM Subcategory ORDER BY subcategory_id')
    subcategory = cur.fetchall()    
    
    #Get subcategory and category IDs from the database
    cur.execute('SELECT subcategory_id, category_id FROM Subcategory;')
    id_pairs = cur.fetchall()
    category_map = {} #used in evaluateAccuracy
    for pair in id_pairs:
        category_map[pair[0]] = pair[1];
        
    # TODO: replace SQL calls with SQLAlchemy (a Python ORM)
    #logging.info("SQLAlchemy version: ", sqlalchemy.__version__)
    
    # Create an interface for writing output to the database table
    grocery_table = TableInterface(con, "Grocery")
    item_table = TableInterface(con, "Item")
    
    # Step 1: Parse the flyers into (item, price) pairs
    items = getFlyer()
    
    # Step 2: Pass the items one by one to the "getNouns" module to get a list of nouns for each item
    getNouns.init()
    logging.info('\n')
    stores = items.keys()
    for store_id in stores:
        item_list = items[store_id]
        predictions = []
        noun_table = []
        
        for item in item_list:
            
            # Only pass in the raw_item string, without the price
            noun_list = getNouns.getNouns(item[0])
            noun_table += [noun_list]
            
            # Step 3: Pass the list of nouns to the "classifier" module to classify the item into one subcategory
            subcategory_id = classifier.classify(noun_list, subcategory)
            predictions += [subcategory_id]
            
            # Add to output buffer
            try:
                item_data = [item[0].encode('utf-8')] + [subcategory_id]
            except:
                item_data = [item[0]] + [subcategory_id]
                
            res_flag = item_table.add_data(item_data)
            if not res_flag:
                raise RuntimeError("item data could not be added to the item table handler")
            
        # Evaluate classification accuraucy for each store flyer based on hand-labelled subcategories
        classification_rates = evaluateAccuracy(store_id, predictions, category_map, item_list, noun_table)
        if classification_rates:
            logging.info("CATEGORY CLASSIFICATION RATE = %.2f for store %d" % (classification_rates[1],store_id))
        # Step 4: Write to Item`
        item_ids = item_table.write_data()
        grocery_data = [tuple(item_ids)] + zip(*item_list)
        grocery_data = map(lambda x: list(x), zip(*grocery_data))
        
        # Encode the raw strings as UTF-8 before adding to database, so all special 
        # characters are preserved.
        try:
            grocery_data = map(lambda x: [x[0]] + [x[1].encode('utf-8')] + [x[2].encode('utf-8')] + x[3:], grocery_data)
        except:
            grocery_data = map(lambda x: [x[0]] + [x[1]] + [x[2]] + x[3:], grocery_data)
            
        res_flag = grocery_table.add_batch(grocery_data)
        if not res_flag:
            raise RuntimeError("grocery data could not be added to the Grocery table handler")
        logging.info('\n')
    
    # Step 5: Write to Grocery
    grocery_ids = grocery_table.write_data()
    
except Exception, e:
    exc_type, exc_obj, exc_tb = sys.exc_info()
    fname = os.path.split(exc_tb.tb_frame.f_code.co_filename)[1]
    logging.info("Error (%s) occurred in %s, line %s: %s" % (exc_type, fname, exc_tb.tb_lineno, e))
    logging.info("Traceback:")
    logging.info(traceback.format_exc())
    sys.exit(1)
finally:
    if con:
        con.close()
        logging.info("Closed connection to database")

elapsed_time = time.time() - start_timer
logging.info("\nELAPSED: %.2f seconds" % elapsed_time)

print("done")