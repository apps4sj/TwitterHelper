import json
from HtmlGenerator import generateHTML
from HtmlDeleter import deleteHTML
from HtmlDeleter import deleteHTMLById
from HtmlPreviewer import previewHTML
from AgingTable import updateAgeRecord
from AgingTable import deleteAgeRecord
from AgingTable import getExpiredIds
from HtmlPublisher import publishHTML   
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
    #### This is a stage type of request #####
    if type == "stage":
       generateHTML(file, theJson, "/var/www/staged")
       imagePath = previewHTML(theJson, "/var/www/staged")
       currentTime = int(time.time())
       expiringTime = currentTime + 3600
       updateAgeRecord(theJson.get("id"), expiringTime)
       return imagePath
    #### This is a publish type of request #####
    if type == "publish":
       res = publishHTML(theJson, "/var/www/staged", "/var/www/html")
       if res == True:
           currentTime = int(time.time())
           expiringTime = currentTime + 3600
           updateAgeRecord(theJson.get("id"), expiringTime)
           return theJson.get("id") + "/index.html"
       else:
           return "Error"
    #### This is a delete type of request #####
    if type == "delete":
       deleteHTML(theJson, "/var/www/html")
       deleteHTML(theJson, "/var/www/staged")
       deleteAgeRecord(theJson.get("id"))
       return "deleted"
    #### This is a extend type of request #####
    if type == "extend":
       currentTime = int(time.time())
       expiringTime = currentTime + 3600
       updateAgeRecord(theJson.get("id"), expiringTime)
       return "extended"
    #### This is a clean type of request #####
    if type == "clean":
       currentTime = int(time.time())
       ids = getExpiredIds(currentTime)
       for id in ids:
           deleteHTMLById(id, "/var/www/html")
           deleteHTMLById(id, "/var/www/staged")
           deleteAgeRecord(id)
       return "cleaned"
    #Done with reading
    file.close()
    
#ProcessFile("./deleteJson.bin")
