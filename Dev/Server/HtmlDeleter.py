import os
import shutil
def deleteHTML(theJson, htmlDir):
    id = theJson.get("id")
    homeDir = htmlDir + "/" + id
    shutil.rmtree(homeDir)

def deleteHTMLById(id, htmlDir):
    homeDir = htmlDir + "/" + id
    shutil.rmtree(homeDir)
