#
# Created by shadowsparky in 2019
#

import socket
from subprocess import Popen, PIPE, STDOUT

SERVER_IP = "192.168.43.78" # IPV4 from android app 
SERVER_PORT = 1337 

def connectToServer(): 
    sock = socket.socket(socket.AF_INET, socket.SOCK_STREAM)
    sock.connect((SERVER_IP, SERVER_PORT))
    handleReceivedData(sock)

def handleReceivedData(sock): 
    player = Popen(['ffplay', '-framerate', '60', '-window_title', 'Supreme Original Content', '-'], stdin=PIPE, stdout=PIPE)
    try:
        while True: 
            try:
                data = sock.recv(1024)
                player.stdin.write(data)
            except: 
                print 'An error has occurred...'
                return
    finally:
        sock.close()
    
if __name__ == "__main__":
    connectToServer()