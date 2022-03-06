import mysql.connector
import socketserver
import random
import os
import time
from pyvirtualdisplay import Display
from ProcessFile import ProcessFile
display = Display()
display.start()

# Assuming the beginning 10 bytes are integer, which is the total byte count of
# the message, including the beginning 10 bytes
class MyTCPHandler(socketserver.BaseRequestHandler):

    def handle(self):
        random.seed(round(time.time() * 1000000))
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
                #print("received ", len(rdata), "bytes\n")
                # Deduct number of bytes to receive by the number of
                # newly received data
                bytesToReceive -= len(rdata)
                file.write(rdata)
        file.close()
        if fullyDownloaded == True:
            response = ProcessFile(fileName)
            if response == "deleted":
                self.request.sendall(bytes(response + "\n", 'ascii'))
            if response.endswith("index.html"):
                self.request.sendall(bytes("http://apps4sj.org/" + response + "\n", 'ascii'))
            if response.endswith(".jpg"):
               #send image file length in 10bytes
               imageLength=os.path.getsize(response) + 10
               lengthString = format(imageLength, '010d')
               print(lengthString)
               self.request.sendall(bytes(lengthString,'ascii'))
               #send image file itself
               imageFile = open(response,"br")
               imageData = imageFile.read()
               imageFile.close()
               self.request.sendall(imageData)
               time.sleep(4)
               os.remove(response)
            if response == "extended":
                self.request.sendall(bytes("extended",'ascii'))
            if response == "cleaned":
                self.request.sendall(bytes("cleaned",'ascii'))
        else:
            self.request.sendall(bytes("Not Fully Downloaded\n", 'ascii'))
        #Temp file should always be deleted
        os.remove(fileName)



HOST = "0.0.0.0"
PORT = 32421
myServer = socketserver.ForkingTCPServer((HOST, PORT), MyTCPHandler)
myServer.serve_forever()

