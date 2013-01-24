from nltk.corpus import wordnet as wn
import re

#weighting
w_word = 1
w_def = 0.5
w_syn = 0.5
w_hyp = 0.5

def classify(noun_list, subcategory):
    '''Takes a list of nouns representing 1 item. Returns a subcategory for the item.'''
    #score determining which subcategory to assign the item to
    subcategory_score = [0]*len(subcategory)
    for word in noun_list:
        # Generate three lists: (1) definition terms, (2) synonyms, (3) hypernyms
        
        # Select the first synset of the word (this may need to be modified to select the most relevant synset)
        #print("Attempting to look up word %s" % word)
        synset = wn.synsets(word, pos=wn.NOUN)
        if synset:
            synset = synset[0]
            definition = synset.definition
            synonyms = synset.lemma_names
            hypernym_set = synset.hypernyms()
            hypernyms = [x.name for x in hypernym_set]
            #hypernyms = [x.definition for x in hypernym_set]
            
            #lower case
            word = word.lower()
            definition = definition.lower()
            
            for record in subcategory:
                subcategory_id, subcategory_name = record[0], re.split(r"\s+|[,]", record[1])
                cat = subcategory_name[0].lower()
                if cat in word:
                    print("Word: ", word)
                    print(record[1])
                    subcategory_score[subcategory_id-1] += w_word
                
                if cat in definition:
                    print("Word: ", word)
                    print("Definition: ", definition)
                    print(record[1])
                    subcategory_score[subcategory_id-1] += w_def

                if any(cat in s for s in synonyms):
                    print("Word: ", word)
                    print("Synonyms: ", synonyms)
                    print(record[1])
                    subcategory_score[subcategory_id-1] += w_syn
                        
                if any(cat in h for h in hypernyms):
                    print("Word: ", word)
                    print("Hypernyms: ", hypernyms)
                    print(record[1])
                    subcategory_score[subcategory_id-1] += w_hyp

    #print(subcategory_score)
                        
    if max(subcategory_score) == 0:
        print("unknown id!!!")
        return -1
                        
    max_id = subcategory_score.index(max(subcategory_score)) + 1
    for record in subcategory:
        if max_id == record[0]:
            max_subcat = record[1]
    print("subcategory id = ", max_id, max_subcat)
    return max_id


#nouns =[u'PORK', u'PICNIC', u'SHOULDER', u'ROAST', u'VACUUM', u'PRICE']
#subcategory = ((1L, 'Dairy and Egg Products'), (2L, 'Spices and Herbs'), (3L, 'Babyfoods'), (4L, 'Fats and Oils'), (5L, 'Poultry Products'), (6L, 'Soups, Sauces and Gravies'), (7L, 'Sausages and Luncheon meats'), (8L, 'Breakfast cereals'), (9L, 'Fruits and fruit juices'), (10L, 'Pork Products'), (11L, 'Vegetables and Vegetable Products'), (12L, 'Nuts and Seeds'), (13L, 'Beef Products'), (14L, 'Beverages'), (15L, 'Finfish and Shellfish Products'), (16L, 'Legumes and Legume Products'), (17L, 'Lamb, Veal and Game'), (18L, 'Baked Products'), (19L, 'Sweets'), (20L, 'Cereals, Grains and Pasta'), (21L, 'Fast Foods'), (22L, 'Mixed Dishes'), (23L, 'Snacks'))

#classify(nouns, subcategory)