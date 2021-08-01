def generateHTML():
#defining the function of generateHTML later used in main.py
    htmlFile = open("/Users/KennethXu/Sites/index.html", 'w')
    configFile=open("/Users/KennethXu/test/config.txt", 'r')
    text=configFile.readline()
    text = configFile.readline()


    htmlFile.write("\n")
    #format with line insert
    htmlFile.write("<html>\n")
    htmlFile.write("<body>\n")
    htmlFile.write("<h1>Test Web </h1>\n")
    #title
    htmlFile.write("<img src=\"index2.jpeg\" width = 200 height= 200>")
    #opening of file saved in local storage
    htmlFile.write("   ")
    #spacing in between images
    htmlFile.write("<img src=\"index2.jpeg\" width = 200 height= 200>")
    htmlFile.write("<p> </p>\n")
    htmlFile.write("<img src=\"index2.jpeg\" width = 200 height= 200>")
    htmlFile.write("  ")
    htmlFile.write("<img src=\"test.png\" width = 200 height= 200>")
    #presenting the locally saved image from the main server program (4th image bottom right)
    htmlFile.write("<p>contact info </p>\n" )
    htmlFile.write("</body>\n")
    htmlFile.write("</html>\n")
    htmlFile.close()
