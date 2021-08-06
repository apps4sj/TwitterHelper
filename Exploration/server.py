import socket
import threading

HOST = "localhost"      # The IP that the server will be hosted on
PORT = 23512            # The port used by the server
UTF8 = "utf-8"          # Encoding used to transfer data through socket
HEADER_LENGTH = 8       # Length of header

"""
Data is transferred through a socket by first including a header. This header is just a number that tells
the server how long the received message is. Then, the server will read the next x characters from the 
client. An example would be "00000008hello!!!". The first 8 characters are the header (header length is 8)
and the message is "hello!!!" which is 8 characters.
"""


# Code that is run whenever a client connects to the server. It is run in a seperate thread (kind of) and a
# new thread is created for every connection
def handle_client(connection, address):
    try:
        while True:
            # Read the header (strip is for removing \n)
            header = connection.recv(HEADER_LENGTH).strip()

            # Make sure that the code does not continue when there is no header, which means no msg
            if header:
                # Decode the message from UTF8 and convert to int
                data_length = int(header.decode(UTF8))

                # Read the next _ characters (based on data_length) and then decode from UTF8
                data = connection.recv(data_length).decode(UTF8)

                # If the message is QUIT, then it will close the connection
                if not data or data == "QUIT":
                    print(f"[CLIENT] <{address[0]}:{address[1]}> Connection closed by client...")
                    print("[CONNECTIONS] Active connections: ", threading.active_count() - 2)
                    break

                # Print out the message that was received
                print(f"[CLIENT] <{address[0]}:{address[1]}> Received: {data}")

                # Simple command example, sending "GET name" will return "here is the data for name"
                if data.startswith("GET") and len(data.split()) > 1:
                    to_get = data.split()[1]
                    response = "here is the data for " + to_get

                # 7/25 Assignment: Add command to server (1/2) (raymond tian)
                elif data.startswith("TWEET") and len(data.split()) > 1:
                    to_tweet = " ".join(data.split()[1:])
                    response = f"tweeting {to_tweet}"

                # 7/25 Assignment: Add command to server (2/2) (raymond tian)
                elif data.startswith("SEARCH") and len(data.split()) > 1:
                    to_tweet = " ".join(data.split()[1:])
                    response = f"searching {to_tweet}"

                # If the message does not match any command
                else:
                    response = "nothing"

                # Encode the response to UTF8
                response = response.encode(UTF8)

                # Gets the header for the response. zfill takes 8 and turns it into 00000008
                response_header = str(len(response)).zfill(HEADER_LENGTH).encode(UTF8)

                # Print out the sent message
                print(f"[CLIENT] <{address[0]}:{address[1]}> Sending: {response.decode(UTF8)}")

                # Send the header and message
                connection.send(response_header + response)

    # Error that occurs when the client forcibly closes the connection (exits program)
    except ConnectionResetError as e:
        print(f"[CLIENT] <{address[0]}:{address[1]}> Connection closed by client...")
        print(f"[CONNECTIONS] Active connections: {threading.active_count() - 2}")

    # Any other error that may occur in the server (will probably add more except statements)
    except Exception as e:
        print(f"[SERVER] Error occured! {e}")
        print(f"[SERVER] Disconnecting from <{address[0]}:{address[1]}>")
        print(f"[CONNECTIONS] Active connections: {threading.active_count() - 2}")

    # Ends the connection with the client and the thread is closed
    connection.close()


# Function that initializes the server
def open_sockets():
    print("[SERVER] Server Starting...")

    # Create a new socket object
    # socket.AF_INET is the address family that the socket can communicate with (IPv4)
    # socket.SOCK_STREAM represents the type of socket, in this case, TCP
    s = socket.socket(socket.AF_INET, socket.SOCK_STREAM)

    # Binds the socket to an address. Address is a tuple in the form of (host, port)
    s.bind((HOST, PORT))
    print(f"[SERVER] Socket bound to {PORT}")

    # Enables the server to accept incoming connections
    s.listen()

    # Infinite loop because there isn't really a reason for the server to stop other than manually
    while True:
        # The code waits at s.accept() until a client sends a connection. The connection is then accepted and a thread
        # is started to accept the connection
        client_socket, address = s.accept()
        print(f"[CONNECTIONS] Connected to : {address[0]}:{address[1]}")

        # Accept the connection
        thread = threading.Thread(target=handle_client, args=(client_socket, address))
        thread.start()
        print("[CONNECTIONS] Active connections: ", threading.active_count() - 1)


if __name__ == '__main__':
    open_sockets()
