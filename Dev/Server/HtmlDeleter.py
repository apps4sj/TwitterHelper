import os
import shutil
def deleteHTML(theJson, htmlDir):
#defining the function of generateHTML later used in main.py
    id = theJson.get("id")
    homeDir = htmlDir + "/" + id
    shutil.rmtree(homeDir)
