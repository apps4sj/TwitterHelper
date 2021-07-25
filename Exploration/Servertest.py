import socketserver
class MyTCPHandler(socketserver.BaseRequestHandler):
    def handle(self):
        self.data=self.request.recv(1024).strip()
        print .__format__(self.client_address[0], self.client_address[1])
        print(self.data)
        self.request.sendall(self.data.lower())

server = socketserver.TCPServer(("192.168.2.139", 155), MyTCPHandler )
server.serve_foe
