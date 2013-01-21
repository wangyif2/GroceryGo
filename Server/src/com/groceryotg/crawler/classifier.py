import subprocess,sys, re;

def tag(word):
    #Get a list of hypernyms for a noun
    cmd = ["E:\\WordNet\\bin\\wn.exe", word, "-hypen"]
    wnOut = subprocess.Popen(cmd, stdout=subprocess.PIPE)
    
    #Extract each sense from WordNet and insert into a list of senses
    Senses = [];
    sense = [];
    senseNumber = 0
    for line in wnOut.stdout.readlines():
        line = line.strip().decode()

        if "Sense" in line:
            if senseNumber > 0:
                Senses.append(sense.copy())
                sense.clear()
            senseNumber += 1
            continue
        
        if senseNumber > 0 and "=>" in line:
            sense.append(re.sub("=>", '', line).strip())
    Senses.append(sense.copy())
    
    for sense in Senses:
        print(sense)
    

tag('sirloin')