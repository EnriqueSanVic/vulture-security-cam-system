import socket
from binary_utils import *
from streaming_thread import StreamingThread
from socket_listener_thread import SocketListenerThread
from constants import *


def main():

    global cameraName

    client_socket = socket.socket(socket.AF_INET, socket.SOCK_STREAM)
    client_socket.connect((SERVER_IP, SERVER_PORT))
    
    
    streamingThread = StreamingThread(client_socket=client_socket, cameraName=cameraName)
    leaseSocketThread = SocketListenerThread(client_socket=client_socket)
    streamingThread.start()
    leaseSocketThread.start()


errorInit = False

cameraName = formatString(CAMERA_NAME, MAX_CAM_NAME_BYTES, BACKFILL_CHARACTER_STRINGS)

if cameraName is None:
    errorInit = True


if errorInit == False:

    main()

else:
    print("No se ha podido iniciar el streaming debido a las condiciones iniciales...")    