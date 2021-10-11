import os
import imgkit
def previewHTML(theJson, htmlDir):
    id = theJson.get("id")
    homeDir = htmlDir + "/" + id
    url = "file://"+homeDir + "/index.html"
    imagePath = htmlDir + "/" + id + ".jpg"
    options = {
        'quality': 80
    }
    imgkit.from_url(url, imagePath, options)
    return imagePath

