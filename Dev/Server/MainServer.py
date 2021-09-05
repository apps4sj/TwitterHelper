import socketserver
import random
import os
from HtmlGenerator import generateHTML
from ProcessFile import ProcessFile
fileNameToSave = str("")

# Assuming the beginning 10 bytes are integer, which is the total byte count of
# the message, including the beginning 10 bytes
class MyTCPHandler(socketserver.BaseRequestHandler):
    bytesToReceive = 0

    def handle(self):
        bytesToReceive = 0;
        rdata = self.request.recv(10)
        bytesToReceive = int(rdata.strip()) - 10
        fileName = "./"
        if  bytesToReceive > 0:
            fileName = fileName + str(random.randint(1000, 9999))
            fileName = fileName + str(random.randint(1000, 9999))
            fileName = fileName + str(random.randint(1000, 9999))
            fileName = fileName + ".bin"
        print(bytesToReceive)
        print(fileName)
        file = open(fileName, "wb")
        fullyDownloaded = True;
        while bytesToReceive > 0:
            bufferLen = 256
            if  bytesToReceive < bufferLen:
                bufferLen = bytesToReceive
            rdata = self.request.recv(bufferLen)
            # Check the length of data received
            if len(rdata) == 0:
                # Connection broken. Quit looping.
                print("No more bytes to receive\n")
                fullyDownloaded = False;
                break
            else:
                print("received ", len(rdata), "bytes\n")
                # Deduct number of bytes to receive by the number of
                # newly received data
                bytesToReceive -= len(rdata)
                file.write(rdata)
        file.close()
        if fullyDownloaded == True:
            response = ProcessFile(fileName)
            if response == "deleted":
                self.request.sendall(bytes(response + "\n", 'ascii'))
            else:
                self.request.sendall(bytes("http://34.102.29.133/" + response + "\n", 'ascii'))            
        else:
            self.request.sendall(bytes("Not Fully Downloaded\n", 'ascii'))
        #Temp file should always be deleted
        os.remove(fileName)



HOST = "0.0.0.0"
PORT = 32421
myServer = socketserver.TCPServer((HOST, PORT), MyTCPHandler)
myServer.serve_forever()

