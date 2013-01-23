from nltk.corpus import wordnet as wn


def classify(noun_list):
    '''Takes a list of nouns representing 1 item. Returns a subcategory for the item.'''
    
    for word in noun_list:
        # Generate three lists: (1) definition terms, (2) synonyms, (3) hypernyms
        
        # Select the first synset of the word (this may need to be modified to select the most relevant synset)
        print("Attempting to look up word %s" % word)
        synset = wn.synsets(word, pos=wn.NOUN)
        if synset:
            synset = synset[0]
            definition = synset.definition
            synonyms = synset.lemma_names
            hypernym_set = synset.hypernyms()
            hypernyms = [x.definition for x in hypernym_set]
            print("Definition: ", definition)
            print("Synonyms: ", synonyms)
            print("Hypernyms: ", hypernyms)
            return 1
        else:
            print("No entry in WordNet")
            return 22

#classify('sirloin')