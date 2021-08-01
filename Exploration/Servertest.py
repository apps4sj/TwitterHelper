import socketserver
from htmlgenerate import generateHTML
bytesToReceive = 0
fileNameToSave = str("")


class MyTCPHandler(socketserver.BaseRequestHandler):
    bytesToReceive = 0

    def handle(self):
        # self.request is the TCP socket connected to the client
        global bytesToReceive
        global fileNameToSave
        if bytesToReceive <= 0:
            # Since there is no new number of binary bites to read, wait for receiving
            # file info
            fileName, fileSize = self.request.recv(1024).strip().split()
            fileSize = int(fileSize)
            bytesToReceive = fileSize
            fileNameToSave = fileName.decode()
            fileNameToSave = "/Users/KennethXu/Sites/" + fileNameToSave
            print("Received fileName", fileName, " FileSize=", fileSize, "\n")
        else:
            print("Now receive binary file \n")
            # Open a file to record received data
            file = open(fileNameToSave, "wb")
            while bytesToReceive > 0:
                # Keep looping till enough bytes received.
                # Try to buffer 256 bytes
                rdata = self.request.recv(256)
                # Check the length of data received
                if len(rdata) == 0:
                    # Connection broken. Quit looping.
                    print("No more bytes to receive\n")
                    break
                else:
                    print("received ", len(rdata), "bytes\n")
                    # Deduct number of bytes to receive by the number of
                    # newly received data
                    bytesToReceive -= len(rdata)
                    file.write(rdata)
            print("File saved\n")
            bytesToReceive = 0
            fileNameToSave = ""
            # Close file
            file.close()
            generateHTML()


HOST = "192.168.2.139"
PORT = 32421
myServer = socketserver.TCPServer((HOST, PORT), MyTCPHandler)
myServer.serve_forever()
