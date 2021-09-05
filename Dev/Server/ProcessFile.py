import json
from HtmlGenerator import generateHTML
from HtmlDeleter import deleteHTML

def ProcessFile(fileName):
    ###### Open The file ##############
    #### Read Json to a string ########
    file = open(fileName, "br")
    jsonString = ""
    theChar = str(file.read(1), 'utf-8')
    jsonString = jsonString + theChar
    bracketNum = 1
    while bracketNum != 0:
        theChar = str(file.read(1), 'utf-8')
        if not theChar:
            break
        jsonString += theChar
        if theChar=="{":
            bracketNum += 1
        if theChar=="}":
            bracketNum -= 1
    #Text file is always ended with a "LF". Need to
    #read it, so the next reading will be at the beginning of the image
    theChar = str(file.read(1), 'utf-8')
    ######Load json string to a Python Dic #####
    theJson = json.loads(jsonString)
    type = theJson.get("type")
    #### This is a create type of request #####
    if type == "create":
       generateHTML(file, theJson, "/var/www/html")
       return theJson.get("id") + "/index.html"
    if type == "delete":
       deleteHTML(theJson, "/var/www/html")
       return "deleted"
    #Done with reading
    file.close()
    
#ProcessFile("./deleteJson.bin")
