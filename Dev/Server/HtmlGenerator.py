import os
import os.path
import shutil
import random
def generateHTML(theFile, theJson, htmlDir):
#defining the function of generateHTML later used in main.py
    id = theJson.get("id")
    homeDir = htmlDir + "/" + id
    if os.path.exists(homeDir):
        shutil.rmtree(homeDir)
    os.mkdir(homeDir)
    #Create itemName.txt file
    itemNameFile= open(homeDir + "/itemName.txt", 'w')
    itemNameFile.write(theJson.get("itemName"))
    itemNameFile.close()
    #Create index.html
    htmlFile = open(homeDir +"/index.html", 'w')
    htmlFile.write("<!DOCTYPE html>\n")
    htmlFile.write("<html>\n")
    htmlFile.write("<head>\n")
    htmlFile.write("<meta charset=\"utf-8\">\n")
    htmlFile.write("<meta name=\"viewport\" content=\"width=device-width\">\n")
    htmlFile.write("<title>repl.it</title>\n")
    htmlFile.write("<link href=\"style.css\" rel=\"stylesheet\" type=\"text/css\" />\n")
    htmlFile.write("</head>\n")

    htmlFile.write("<body>\n")
    htmlFile.write("<script src=\"script.js\"></script>\n")
    htmlFile.write("<div class=\"txt\">\n")
    #Title
    price = theJson.get("price")
    if price == 0:
        htmlFile.write("<h1>" + theJson.get("itemName") +  " for free </h1>\n")
    else:
        htmlFile.write("<h1>" + theJson.get("itemName") +  " for $" + str(price) +" </h1>\n")
    #htmlFile.write("<br/>\n")

    #Despcription
    htmlFile.write("<h3>")
    htmlFile.write("Description:")
    htmlFile.write("<h3/>\n")
    #htmlFile.write("<br/>\n")
    htmlFile.write("<p>")
    htmlFile.write(theJson.get("description"))
    htmlFile.write("</p>")
    #htmlFile.write("<br/>\n")

    #Location Info
    htmlFile.write("<h3>")
    htmlFile.write("Location:")
    htmlFile.write("<h3/>\n")
    #htmlFile.write("<br/>\n")
    htmlFile.write("<p>")
    htmlFile.write(theJson.get("location"))
    htmlFile.write("</p>")
    #htmlFile.write("<br/>\n")

    #Contact Info
    htmlFile.write("<h3>")
    htmlFile.write("Contact Info:")
    htmlFile.write("<h3/>\n")
    #htmlFile.write("<br/>\n")
    htmlFile.write("<p>")
    htmlFile.write(theJson.get("contact"))
    htmlFile.write("</p>")
    htmlFile.write("<br/>\n")
    htmlFile.write("</div>\n")
    #Image
    #htmlFile.write("<div class=\"imgs\">\n")
    imageCnt = 0
    for imageFile in ["image0", "image1", "image2",  "image3", "image4", "image5"]:
        curImageFile = theJson.get(imageFile)
        if curImageFile:
            imageCnt += 1
            fileName = curImageFile.get("fileName")
            fileLength = curImageFile.get("length")
            imageFile = open(homeDir + "/" + fileName,"wb")
            while fileLength > 0:
                rdata = theFile.read(fileLength)
                if not rdata:
                    break
                imageFile.write(rdata)
                fileLength -= len(rdata)
            imageFile.close()
            htmlFile.write("<img src=\"")
            htmlFile.write(fileName)
            htmlFile.write("\" width = 240>\n")
        #For every 2 images, add a new line
        if imageCnt == 2:
            imageCnt = 0
            htmlFile.write("<br/>")
    #Social Images
    socialImageList = os.listdir("/var/www/images")
    socialImage = random.choice(socialImageList)
    shutil.copy2("/var/www/images/" + socialImage, homeDir)
    htmlFile.write("<img src=\"" + socialImage + "\" width = 240>\n")
    htmlFile.write("<br/>\n")
    #htmlFile.write("</div>\n")
    htmlFile.write("<a href=\"https://apps4si.org\">Promoted By Apps4Si</a>")
    htmlFile.write("<br/>\n")
    htmlFile.write("<a href=\"https://twitter.com/Apps4Si?ref_src=twsrc%5Etfw\" class=\"twitter-follow-button\" data-show-count=\"false\">Follow @Apps4Si</a><script async src=\"https://platform.twitter.com/widgets.js\" charset=\"utf-8\"></script>")
    htmlFile.write("<br/>\n")
    #closing Html
    htmlFile.write("</body>\n")
    htmlFile.write("</html>\n")
    htmlFile.close()
    shutil.copy2("style.css", homeDir)
    shutil.copy2("script.js", homeDir)

