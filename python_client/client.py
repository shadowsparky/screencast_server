from subprocess import Popen, PIPE, STDOUT
import socket

SERVER_IP = "192.168.43.115"
SERVER_PORT = 1337 

def connectToServer(): 
    sock = socket.socket(socket.AF_INET, socket.SOCK_STREAM)
    sock.connect((SERVER_IP, SERVER_PORT))
    handleReceivedData(sock)

def handleReceivedData(sock): 
    player = Popen(['ffplay', '-framerate', '60', '-'], stdin=PIPE, stdout=PIPE)
    try:
        while True: 
            try:
                data = sock.recv(1024)
                player.stdin.write(data)
            except: 
                print 'LOL'
    finally:
        sock.close()
    
if __name__ == "__main__":
    connectToServer()