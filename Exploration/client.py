import socket

HOST = "localhost"      # The IP that the server will be hosted on
PORT = 23512            # The port used by the server
UTF8 = "utf-8"          # Encoding used to transfer data through socket
HEADER_LENGTH = 8       # Length of header


# Sends a message to the server
def send(client, msg):
    print("[CLIENT] Sending Message: " + msg)
    # Encode the message
    message = msg.encode(UTF8)

    # Gets the header for the message. zfill takes 8 and turns it into 00000008
    message_header = str(len(message)).zfill(HEADER_LENGTH).encode(UTF8)

    # Sends the message and header to the server
    client.sendall(message_header + message)


# Waits for a message from the server
def receive(client):
    # Waits for the server to send a message and decodes the header
    data_length = int(client.recv(HEADER_LENGTH).decode(UTF8))

    # Read the next _ characters (based on data_length) and then decode from UTF8
    data = client.recv(data_length).decode(UTF8)

    print("[CLIENT] Received Message: " + data)
    return data


# Connects the client to the server
def connect():
    # Create a new socket object
    # socket.AF_INET is the address family that the socket can communicate with (IPv4)
    # socket.SOCK_STREAM represents the type of socket, in this case, TCP
    client = socket.socket(socket.AF_INET, socket.SOCK_STREAM)

    # Connects the socket to an address. Address is a tuple in the form of (host, port)
    client.connect((HOST, PORT))
    return client


if __name__ == '__main__':
    # Connects the client to the server
    client = connect()

    # Continually takes input from user. This is just for testing purposes
    while True:
        # Waits for keyboard input
        to_send = input("Send Message: ")

        if to_send:
            # Sends message to server
            send(client, to_send)

            # Ends connection if user types QUIT
            if to_send == "QUIT":
                break

            # Waits for a message to be receieved
            receive(client)

    # Closes the connection
    client.close()
