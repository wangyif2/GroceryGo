import sys, re
import _nlplib_pyc.NLPlib as NLPlib
import nltk
#from nltk.stem.wordnet import WordNetLemmatizer

tagger = None

def init():
    '''Initialize POS tagger.'''
    global tagger
    tagger = NLPlib.NLPlib()

def getNounsNLTK(rawStr):
    text = nltk.word_tokenize(rawStr.lower())
    tags = nltk.pos_tag(text)
    res = filter(lambda x: x if x[1]=="NN" else [], tags)
    return res

def getNouns(rawStr):
    '''Takes a string representing 1 item, and returns a list of the nouns in the string.'''
    # Tokenize the raw string
    tokens = re.split(r"\s+|[.]", rawStr)
    
    # Lemmatize the strings to get the base form (e.g. 'cars' -> 'car')
    # NB: the lemmatizer is case-sensitive and only works with lower-case nouns
    # Filter out 'nouns' that are one or two characters long (e.g., 'g', 'ml')
    lemmatizer = nltk.stem.wordnet.WordNetLemmatizer()
    tokens = filter(lambda x: x if len(x)>2 else None, map(lambda x: lemmatizer.lemmatize(x.lower()), tokens))
    #print(tokens)
    
    # Get tags for tokens
    global tagger
    tags = tagger.tag(tokens)
    
    # Store all nouns
    nouns = []
    for i in range(len(tags)):
        if tags[i] == "NN":
            nouns.append(tokens[i])

    #filter out numbers, empty tokens
    nouns = filter(None, map(lambda x: x if re.findall('^[a-zA-Z]+$',x) else [],nouns))


    return nouns

#init()
#str1 = "RED GRILL ANGUS TOP SIRLOIN ROAST OR VALUE PACK STEAK. CUT FROM CANADA AA OR USDA SELECT GRADES OR HIGHER. 8.80/KG PRICE : 1/2 PRICE $3.99 /lb"
#str2 = "FRESH BONELESS SKINLESS CHICKEN BREAST. FILLET REMOVED, VALUE PACK. 8.80/KG PRICE : 1/2 PRICE $3.99 /lb"
#print(getNouns(str1))
#print(getNouns(str2))
