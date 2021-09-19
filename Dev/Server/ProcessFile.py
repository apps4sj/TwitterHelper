import json
from HtmlGenerator import generateHTML
from HtmlDeleter import deleteHTML
from HtmlDeleter import deleteHTMLById
from HtmlPreviewer import previewHTML
from AgingTable import updateAgeRecord
from AgingTable import deleteAgeRecord
from AgingTable import getExpiredIds
import time

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
       currentTime = int(time.time())
       expiringTime = currentTime + 7*24*3600
       updateAgeRecord(theJson.get("id"), expiringTime)
       return theJson.get("id") + "/index.html"
    #### This is a delete type of request #####
    if type == "delete":
       deleteHTML(theJson, "/var/www/html")
       deleteAgeRecord(theJson.get("id"))
       return "deleted"
    #### This is a preview type of request #####
    if type == "preview":
       imagePath = previewHTML(theJson, "/var/www/html")
       return imagePath
    #### This is a extend type of request #####
    if type == "extend":
       currentTime = int(time.time())
       expiringTime = currentTime + 7*24*3600
       updateAgeRecord(theJson.get("id"), expiringTime)
       return "extended"
    #### This is a clean type of request #####
    if type == "clean":
       currentTime = int(time.time())
       ids = getExpiredIds(currentTime)
       for id in ids:
           deleteHTMLById(id, "var/www/html")
           deleteAgeRecord(id)
       return "cleaned"
    #Done with reading
    file.close()
    
#ProcessFile("./deleteJson.bin")
