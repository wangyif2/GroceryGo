import sys, re
import _nlplib_pyc.NLPlib as NLPlib

#initialize POS tagger
tagger = NLPlib.NLPlib()

#Takes a raw string and returns all the nouns in the string
def getNouns(rawStr):
    #tokenize the raw string
    tokens = re.split(r"\s+", rawStr)

    #get tags for tokens
    tags = tagger.tag(tokens)
    
    #store all nouns
    nouns = []
    for i in range(len(tags)):
        if tags[i] == "NN":
            nouns.append(tokens[i])

    return nouns


str1 = "RED GRILL ANGUS TOP SIRLOIN ROAST OR VALUE PACK STEAK. CUT FROM CANADA AA OR USDA SELECT GRADES OR HIGHER. 8.80/KG PRICE : 1/2 PRICE $3.99 /lb"
str2 = "FRESH BONELESS SKINLESS CHICKEN BREAST. FILLET REMOVED, VALUE PACK. 8.80/KG PRICE : 1/2 PRICE $3.99 /lb"
print(getNouns(str1))
print(getNouns(str2))
