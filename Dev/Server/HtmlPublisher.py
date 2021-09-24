import os.path
import shutil
def publishHTML(theJson, srcDir, tgtDir):
    id = theJson.get("id")
    tgtHomeDir = tgtDir + "/" + id
    srcHomeDir = srcDir + "/" + id
    if os.path.exists(tgtHomeDir):
        shutil.rmtree(tgtHomeDir)
    if os.path.exists(srcHomeDir):
        shutil.move(srcHomeDir, tgtDir + "/")
        return True
    else:
        return False
