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
# - MySQLdb (for connecting to mysql)


import ast
from bs4 import BeautifulSoup
import cookielib
import datetime
import htmllib
import MySQLdb as mdb
import nltk
import re
#import sqlalchemy
import sys
import urllib
import urllib2
from urlparse import urlparse

import getNouns
import classifier


# Fill in your MySQL user & password
mysql_user = "root"
mysql_password = ""
mysql_db = "groceryotg"

# TODO:
# Done. 1) Pass in only the item part of the line string to getNouns, so it doesn't get confused with the price 
#       2) Build a language model of bigram probabilities to detect compound nouns (e.g. "potato chips" vs just "chips")
#          If a probability of word B to occur after word A is > 0.5, then it's a compound. 
# Done. 3) Add a "Misc" subcategory in database, in case no subcategories match the line.
# Done. 4) Add a "tags" column in Subcategory table in database (use that list of tags instead of the subcategory name)
#          That way, we can exclude words like "and" and improve efficiency.
# Done. 5) Use all words in the list of tags when determining subcategory_id in classifier.py
#


def unescape(s):
    '''Takes a string with escaped HTML special characters, e.g. "param1=value1&amp;param2=value2". 
       Returns an unescaped version of the string, e.g. "param1=value1&param2=value2". '''
    p = htmllib.HTMLParser(None)
    p.save_bgn()
    p.feed(s)
    return p.save_end()

def getFlyer():
    '''No input parameters, accesses the database directly. 
       Finds this week's URL of the accessible plain-text flyer web pages for each grocery store
       in the database. Parses the accessible plain-text only flyer webpages to identify items. 
       Return a dictionary of {store_id : item} pairs, where "items" is a list of 
       [item_raw_string, unit_price, unit_type_id, total_price, start_date, end_date, page_number, 
       update_date].'''
    flyers = {}
    items = {}
    
    today = datetime.date.today()
    update_date = today.strftime("%Y-%m-%d")
    
    # Get unit IDs from the database
    cur.execute('SELECT unit_id, unit_type_name FROM Unit;')
    units = cur.fetchall()
    
    cur.execute('SELECT store_id, store_url FROM Store ORDER BY store_name')
    data = cur.fetchall()
    for record in data:
        store_id, next_url = record[0], record[1]
        flyer_url = ""
        if next_url:
            print("next url: %s" % next_url)
            
            # Metro
            if store_id == 1:
                flyer_url = ""
                print("Crawling store: %d" % store_id)
                hostname = urlparse(next_url).hostname
                soup = BeautifulSoup(urllib2.urlopen(next_url))
                foundElems = soup('a', text=re.compile(r'Metro Ontario Flyer'))
                if foundElems:
                    linkElem = foundElems[0]
                    flyer_page = "http://" + hostname + linkElem['href']
                    soup = BeautifulSoup(urllib2.urlopen(flyer_page))
                    linkElem = soup('span', text=re.compile(r'View accessible flyer'))[0].parent
                    flyer_url = "http://"+ hostname + linkElem['href']
                    
            # Loblaws
            elif store_id == 2:
                flyer_url = ""
                print("Crawling store: %d" % store_id)
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
                
                pattern = re.compile('(Jan|Feb|Mar|Apr|May|Jun|Jul|Aug|Sep|Oct|Nov|Dec)[a-zA-Z]*\s([0-9]+)')
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
                print("start date: ", start_date)
                print("end date: ", end_date)
                print("update date: ", update_date)
                
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
                    unit_type_id = ""
                    total_price = None
                    
                    raw_price = item['price']
                    orig_price = raw_price
                    if raw_price:
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
                                total_price = float(total_price.strip("$"))
                            else:
                                total_price = float(total_price.strip("\xa2").strip("\xc2")) / 100.0
                            
                            # Default unit_price
                            unit_price = total_price / num_products
                        
                        elif index_dollar != -1:
                            total_price = float(raw_price.strip("$"))
                            
                            # Default unit_price
                            unit_price = total_price
                        else:
                            total_price = float(raw_price.strip("\xa2").strip("\xc2"))
                            
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
                                    start_date, end_date, line_number, update_date]
                    
                    store_items += [item_details]
                    
                items[store_id] = store_items
                   
            # Food Basics
            elif store_id == 3:
                print("Crawling store: %d" % store_id)
                hostname = urlparse(next_url).hostname
                soup = BeautifulSoup(urllib2.urlopen(next_url))
                linkElem = soup('span', text=re.compile(r'View accessible flyer'))[0].parent
                flyer_url = "http://" + hostname + linkElem['href']
                
                store_items = []
                line_number = 0
                print("Parsing store %s, url %s" % (store_id, flyer_url))
                
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
                    
                    page_lines = re.sub('<[bB][rR]\s*?>', '', page.text).split('\n')
                    page_lines = filter(None, map(lambda x: x.strip('\t').strip('\r').strip('\n').strip(), page_lines))
                    for line in page_lines:
                        line_number += 1
                        unit_price = 0
                        unit_type_id = 0
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
                                        start_date, end_date, line_number, update_date]
                        
                        #print(item_details)
                        store_items += [item_details]
                
                items[store_id] = store_items
                
            # No Frills
            elif store_id == 4:
                flyer_url = ""
                
            # Sobeys
            elif store_id == 5:
                flyer_url = ""
        
            print
        
    return items


def evaluateAccuracy(store_id, labels, item_list = None):
    '''Takes a list of correct classifications, "targets", and a list of predicted classifications, "labels". 
       Returns None if either list is empty or the lists are not of the same length. Returns the fraction 
       of correctly classified items otherwise (as a floating point value between 0 and 1). '''
    
    # Read in the correct list of targets for this store's flyer
    file_in = open('flyer_' + str(store_id) + '.txt', 'r')
    file_contents = file_in.read()
    file_in.close()
    
    if not file_contents:
        print("Classification accuracy for store %d could not be determined due to missing labelled targets" % store_id)
        return None
    
    targets = map(lambda x: int(x), file_contents.split('\n'))
    if (not targets or not labels) or (len(targets) != len(labels)):
        print("Classification accuracy for store %d could not be determined due to missing labelled targets" % store_id)
        return None
    
    correctly_classified = 0
    for i in range(len(targets)):
        if targets[i] == labels[i]:
            correctly_classified += 1
        elif item_list:
            print("Misclassified item (predicted: %d, actual: %d): %s" % (labels[i], targets[i], item_list[i]))
                  
    return float(correctly_classified) / float(len(targets))



# ***************************************************************************
# ***************************************************************************
con = None

try:
    con = mdb.connect('localhost', mysql_user, mysql_password, mysql_db)
    cur = con.cursor()
    print("connected to database\n")
    
    # get subcategories from database
    cur.execute('SELECT subcategory_id, subcategory_tag FROM subcategory ORDER BY subcategory_id')
    subcategory = cur.fetchall()    
    
    # TODO: replace SQL calls with SQLAlchemy (a Python ORM)
    #print("SQLAlchemy version: ", sqlalchemy.__version__)
    
    # Step 1: Parse the flyers into (item, price) pairs
    items = getFlyer()
    
    # Step 2: Pass the items one by one to the "getNouns" module to get a list of nouns for each item
    getNouns.init()
    stores = items.keys()
    for store_id in stores:
        item_list = items[store_id]
        predictions = []
        
        for item in item_list:
            tokens = item[0]
            noun_list = getNouns.getNouns(tokens[0])
            
            # Step 3: Pass the list of nouns to the "classifier" module to classify the item into one subcategory
            subcategory_id = classifier.classify(noun_list, subcategory)
            predictions += [subcategory_id]
        
        # Evaluate classification accuracy for each store flyer based on hand-labelled subcategories
        classification_rate = evaluateAccuracy(store_id, predictions, item_list)
        if classification_rate:
            print("TOTAL CLASSIFICATION RATE for store %d: %.2f" % (store_id, classification_rate))
            
    
    # Step 4: Write to database
    
    
    
except mdb.Error, e:
    print("error: %s" % e)
    sys.exit(1)
finally:
    if con:
        con.close()
        print("\nclosed connection to database")
