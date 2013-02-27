import logging
from nltk.corpus import wordnet as wn
import nltk
import re

#weighting
w_word = 1
w_def = 0.4
w_syn = 0.6
w_hyp = 0.5

# define logging level (if you set this to logging.DEBUG, the debug print messages will be displayed)
logging.basicConfig(format='%(levelname)s:%(message)s', level=logging.INFO)

def classify(noun_list, subcategory):
    '''Takes a list of nouns representing 1 item. Returns a subcategory for the item.'''
    #score determining which subcategory to assign the item to
    subcategory_score = {};
    for s in subcategory:
        subcategory_score[s[0]] = 0;
    lemmatizer = nltk.stem.wordnet.WordNetLemmatizer()
    
    # get the ID of the special "catch-all" subcategory ("miscellaneous") for items that cannot be identified
    subcategory_misc = filter(lambda x: x if x[1]=='miscellaneous' else None, subcategory)[0]
    subcategory = filter(lambda x: x if x[1]!='miscellaneous' else None, subcategory)
    misc_id, misc_name = subcategory_misc[0], subcategory_misc[1]
    
    for word in noun_list:
        # Generate three lists: (1) definition terms, (2) synonyms, (3) hypernyms
        
        # Select the first synset of the word (this may need to be modified to select the most relevant synset)
        synsets = wn.synsets(word, pos=wn.NOUN)
        if synsets:
            
            # Choose the synset which contains the "food" category as a hypernym
            apple_food = wn.synsets('apple', pos=wn.NOUN)[0]
            food_cat = wn.synsets('food', pos=wn.NOUN)[1]
            synset = synsets[0]
            for set in synsets:
                if food_cat in set.common_hypernyms(apple_food):
                    synset = set
                    break
            logging.debug("For noun '%s', use the sense: %s" % (word, synset.definition))
            
            definition = synset.definition
            synonyms = synset.lemma_names
            hypernym_set = synset.hypernyms()
            hypernyms = [x.name for x in hypernym_set]
            #hypernyms = [x.definition for x in hypernym_set]
            
            #lower case
            word = str(word.lower())
            definition = definition.lower()
            
            for record in subcategory:
                # Once the subcategory_tags are implemented, uncomment the below:
                #subcategory_id, subcategory_name = record[0], re.split(r"[,]", record[1])
                subcategory_id, subcategory_name = record[0], re.split(r"\s+|[,]", record[1])
                subcategory_words = map(lambda x: x.lower(), subcategory_name)
                for cat in subcategory_words:
                    cat = lemmatizer.lemmatize(cat)
                    if cat == word:
                        logging.debug("Found subcategory word '%s' directly in word '%s'" % (cat, word))
                        logging.debug("Classify word '%s' as subcategory '%s' with score %.2f" % (word, record[1], w_word))
                        subcategory_score[subcategory_id] += w_word
                    
                    if cat in definition.split(' '):
                        logging.debug("Found subcategory word '%s' in the definition of word '%s' (%s)" % (cat, word, definition))
                        logging.debug("Classify word '%s' as subcategory '%s' with score %.2f" % (word, record[1], w_def))
                        subcategory_score[subcategory_id] += w_def
                    
                    if any(cat in s for s in synonyms):
                        logging.debug("Found subcategory word '%s' in the synonyms of word '%s' (%s)" % (cat, word, synonyms))
                        logging.debug("Classify word '%s' as subcategory '%s' with score %.2f" % (word, record[1], w_syn))
                        subcategory_score[subcategory_id] += w_syn
                    
                    if any(cat in h for h in hypernyms):
                        logging.debug("Found subcategory word '%s' in the hypernyms of word '%s' (%s)" % (cat, word, hypernyms))
                        logging.debug("Classify word '%s' as subcategory '%s' with score %.2f" % (word, record[1], w_hyp))
                        subcategory_score[subcategory_id] += w_hyp

    logging.debug(subcategory_score)
                        
    if max(subcategory_score.values()) == 0:
        logging.debug("unknown id -- miscellaneous!!!")
        return misc_id
    
    #compute maximum subcat
    max_id = max(subcategory_score, key = subcategory_score.get);
    logging.debug("subcategory id = %d" % (max_id));
    return max_id;
    

#nouns =[u'PORK', u'PICNIC', u'SHOULDER', u'ROAST', u'VACUUM', u'PRICE']
#subcategory = ((1L, 'Dairy and Egg Products'), (2L, 'Spices and Herbs'), (3L, 'Babyfoods'), (4L, 'Fats and Oils'), (5L, 'Poultry Products'), (6L, 'Soups, Sauces and Gravies'), (7L, 'Sausages and Luncheon meats'), (8L, 'Breakfast cereals'), (9L, 'Fruits and fruit juices'), (10L, 'Pork Products'), (11L, 'Vegetables and Vegetable Products'), (12L, 'Nuts and Seeds'), (13L, 'Beef Products'), (14L, 'Beverages'), (15L, 'Finfish and Shellfish Products'), (16L, 'Legumes and Legume Products'), (17L, 'Lamb, Veal and Game'), (18L, 'Baked Products'), (19L, 'Sweets'), (20L, 'Cereals, Grains and Pasta'), (21L, 'Fast Foods'), (22L, 'Mixed Dishes'), (23L, 'Snacks'))

#classify(nouns, subcategory)