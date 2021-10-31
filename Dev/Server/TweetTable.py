import json
import mysql.connector

def updateTweetRecord(cur, id, tweetId):
    #The record is not there, insert one
    tweetId = str(tweetId)
    sqlCommand ="INSERT INTO TweetIDs VALUES ("+ "\'" + id + "\'"+ ", " + "\'"+ tweetId + "\'"+");"
    print(sqlCommand)
    cur.execute(sqlCommand)
    cur.execute("COMMIT")

def deleteTweetRecord(cur, tweetId):
    sqlCommand = "DELETE FROM TweetIDs WHERE tweetId = " + tweetId
    print(sqlCommand)
    try:
        cur.execute(sqlCommand)
        cur.execute("COMMIT")
    except Exception as err:
        print("Exception")

def findTweetId(cur, id):
    sqlCommand = "SELECT tweetId FROM TweetIDs WHERE id = " + id
    print(sqlCommand)
    cur.execute(sqlCommand)
    res = cur.fetchall()
    idList=[]
    for record in res:
        idList.append(record[0])
    print(idList)
    return idList


