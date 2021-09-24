import os
import os.path
import shutil
def deleteHTML(theJson, htmlDir):
    id = theJson.get("id")
    homeDir = htmlDir + "/" + id
    if os.path.exists(homeDir):
        shutil.rmtree(homeDir)

def deleteHTMLById(id, htmlDir):
    homeDir = htmlDir + "/" + id
    if os.path.exists(homeDir):
        shutil.rmtree(homeDir)
