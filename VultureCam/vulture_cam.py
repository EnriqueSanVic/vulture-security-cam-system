import socket
import os
from binary_utils import *
from streaming_system import *
from constants import *
import time

def main():

    global cameraName

    #se pone una prioridad máxima al proceso de este programa
    #Requiere de ser ejecutado con sudo 
    #os.nice(-20)

    client_socket:socket

    correctConexion = False

    #Intento de conexion cada n segundos en caso de conexion fallida con el servidor
    while not correctConexion:

        try:
            client_socket = socket.socket(socket.AF_INET, socket.SOCK_STREAM)
            client_socket.connect((SERVER_IP, SERVER_PORT))

            #En el primer envío se manda el ID del usuario al que pertenece esta cámara
            client_socket.send(intTobytes(ID_CLIENT))

            #El segundo envío son MAX_CAM_NAME_BYTES bytes del string del nombre de la cámara
            client_socket.send(cameraName)

            correctConexion = True
        except:
            time.sleep(RETRY_SERVER_CONEXION_SECS)
    
    
    streamingThread:StreamingThread = StreamingThread(client_socket=client_socket)
    leaseSocketThread:SocketListenerThread = SocketListenerThread(client_socket=client_socket)

    signalRouter:SignalRouter = SignalRouter(leaseSocketThread, streamingThread)

    leaseSocketThread.setSignalRouter(signalRouter)
    streamingThread.setSignalRouter(signalRouter)

    #se inicia la escucha de señales del servidor
    leaseSocketThread.start()


errorInit = False

cameraName = formatString(CAMERA_NAME, MAX_CAM_NAME_BYTES, BACKFILL_CHARACTER_STRINGS)

if cameraName is None:
    errorInit = True


if errorInit == False:

    main()

else:
    print("No se ha podido iniciar el streaming debido a las condiciones iniciales...")    