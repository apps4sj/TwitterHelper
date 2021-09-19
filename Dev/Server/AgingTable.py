import json
import os.path
from filelock import FileLock

def updateAgeRecord(id, expiringEpochTimeInSec):
    lock = FileLock("agingtable.json.lock")
    with lock:
        if os.path.exists("agingtable.json"):
            jsonFile=open("agingtable.json", "r")
            theJson = json.load(jsonFile)
            jsonFile.close()
            theJson.update({id:expiringEpochTimeInSec})
            jsonFile = open("agingtable.json", "w")
            json.dump(theJson, jsonFile)
            jsonFile.close
            
        else:
            theJson = {id:expiringEpochTimeInSec}
            jsonFile = open("agingtable.json", "w")
            json.dump(theJson, jsonFile)
            jsonFile.close

def deleteAgeRecord(id):
    lock = FileLock("agingtable.json.lock")
    with lock:
        if os.path.exists("agingtable.json"):
            jsonFile=open("agingtable.json", "r")
            theJson = json.load(jsonFile)
            jsonFile.close()
            if id in theJson:
                del theJson[id]
                jsonFile = open("agingtable.json", "w")
                json.dump(theJson, jsonFile)
                jsonFile.close

def getExpiredIds(threshold):
    lock = FileLock("agingtable.json.lock")
    res = []
    with lock:
        if os.path.exists("agingtable.json"):
            jsonFile=open("agingtable.json", "r")
            theJson = json.load(jsonFile)
            jsonFile.close()
            for key in theJson:
                value = theJson.get(key)
                if value < threshold:
                    res.append(key)
    return res


