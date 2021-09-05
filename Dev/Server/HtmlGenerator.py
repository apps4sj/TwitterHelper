import os
import shutil
def generateHTML(theFile, theJson, htmlDir):
#defining the function of generateHTML later used in main.py
    id = theJson.get("id")
    homeDir = htmlDir + "/" + id
    os.mkdir(homeDir)
    #Create index.html
    htmlFile = open(homeDir +"/index.html", 'w')
    htmlFile.write("\n")
    htmlFile.write("<html>\n")
    htmlFile.write("<body>\n")
    #Title
    price = theJson.get("price")
    if price == 0:
        htmlFile.write("<h1>" + theJson.get("itemName") +  " for free </h1>\n")
    else:
        htmlFile.write("<h1>" + theJson.get("itemName") +  "for " + str(price) +" </h1>\n")
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

    #Images
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
            htmlFile.write("\" width = 200 height= 200>\n")
        #For every 2 images, add a new line
        if imageCnt == 2:
            imageCnt = 0
            htmlFile.write("<br/>")
    #Social Images
    shutil.copy2("/var/www/images/wearmask0.jpeg", homeDir)
    htmlFile.write("<img src=\"wearmask0.jpeg\" width = 200 height= 200>\n")
    htmlFile.write("<br/>\n")
    #closing Html
    htmlFile.write("</body>\n")
    htmlFile.write("</html>\n")
    htmlFile.close()

