import mysql.connector
import json
from HtmlGenerator import generateHTML
from HtmlDeleter import deleteHTML
from HtmlDeleter import deleteHTMLById
from HtmlPreviewer import previewHTML
from AgingTable import updateAgeRecord
from AgingTable import deleteAgeRecord
from AgingTable import getExpiredIds
from HtmlPublisher import publishHTML
from HtmlTweetHandler import sendTweet
from HtmlTweetHandler import deleteTweet
from TweetTable import updateTweetRecord
from TweetTable import deleteTweetRecord
from TweetTable import findTweetId

import time

def ProcessFile(fileName):
    ###### Open The file ##############
    #### Read Json to a string ########
    cnx = mysql.connector.connect(user='root', password='xx',host='localhost',database='TwitterHelperDatabase')
    cur = cnx.cursor()
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
       updateAgeRecord(cur, theJson.get("id"), expiringTime)
       cnx.close()
       file.close()
       return imagePath
    #### This is a publish type of request #####
    if type == "publish":
       res = publishHTML(theJson, "/var/www/staged", "/var/www/html")
       if res == True:
           id = theJson.get("id")
           currentTime = int(time.time())
           expiringTime = currentTime + 3600
           updateAgeRecord(cur, theJson.get("id"), expiringTime)
           webpagePath = "/var/www/html" + "/" + id
           url = "https://apps4sj.org" + "/" + id
           #Add hashtag
           itemNameFile = open(webpagePath +"/itemName.txt", 'r')
           itemName = itemNameFile.read()
           itemNameFile.close()
           print(itemName)
           wordList = itemName.split()
           hashTag=""
           for word in wordList:
               hashTag = hashTag+"#"+word.lower()
           tweetId = sendTweet(webpagePath, url, hashTag)
           updateTweetRecord(cur, id, tweetId)
           cnx.close()
           file.close()
           return theJson.get("id") + "/index.html"
       else:
           cnx.close()
           file.close()
           return "Error"
    #### This is a delete type of request #####
    if type == "delete":
       id = theJson.get("id")
       deleteHTML(theJson, "/var/www/html")
       deleteHTML(theJson, "/var/www/staged")
       deleteAgeRecord(cur, id)
       tweetIds = findTweetId(cur, id)
       for tweetId in tweetIds:
           deleteTweet(tweetId)
           deleteTweetRecord(cur, tweetId)
       cnx.close()
       file.close()
       return "deleted"
    #### This is a extend type of request #####
    if type == "extend":
       currentTime = int(time.time())
       expiringTime = currentTime + 3600
       updateAgeRecord(cur, theJson.get("id"), expiringTime)
       cnx.close()
       file.close()
       return "extended"
    #### This is a clean type of request #####
    if type == "clean":
       currentTime = int(time.time())
       ids = getExpiredIds(cur, currentTime)
       for id in ids:
           deleteHTMLById(id, "/var/www/html")
           deleteHTMLById(id, "/var/www/staged")
           deleteAgeRecord(cur, id)
           tweetIds = findTweetId(cur,id)
           for tweetId in tweetIds:
               deleteTweet(tweetId)
               deleteTweetRecord(cur, tweetId)
       cnx.close()
       file.close()
       return "cleaned"
    
