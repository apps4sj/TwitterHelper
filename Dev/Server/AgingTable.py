import json
import os.path
from filelock import FileLock
import mysql.connector

def updateAgeRecord(cur, id, expiringEpochTimeInSec):
    sqlCommand= "SELECT * FROM AdAges WHERE id = " + "\'" + id + "\'"
    print(sqlCommand)
    cur.execute(sqlCommand )
    if len(cur.fetchall()) > 0:
        #The record is already there. Update it
        sqlCommand = "UPDATE AdAges SET age = " + str(expiringEpochTimeInSec)+ " WHERE id = " + "\'" + id + "\'"
        print(sqlCommand)
        cur.execute(sqlCommand)
        cur.execute("COMMIT;")
    else:
        #The record is not there, insert one
        sqlCommand ="INSERT INTO AdAges VALUES("+ "\'" + id + "\'"+ ", " + str(expiringEpochTimeInSec)+")"
        print(sqlCommand)
        cur.execute(sqlCommand)
        cur.execute("COMMIT;")

def deleteAgeRecord(cur, id):
    sqlCommand = "DELETE FROM AdAges WHERE id = " + "\'"+ id + "\'"
    print(sqlCommand)
    cur.execute(sqlCommand)
    cur.execute("COMMIT;")


def getExpiredIds(cur, threshold):
    sqlCommand = "SELECT id FROM AdAges WHERE age < " + str(threshold)
    print(sqlCommand)
    cur.execute(sqlCommand)
    res = cur.fetchall()
    idList=[]
    for record in res:
        idList.append(record[0])
    print(idList)
    return idList


