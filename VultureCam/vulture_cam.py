import socket
import os
from binary_utils import *
from streaming_system import *
from constants import *
import time

client_socket:socket

def closeSocketConnection():
    global client_socket
    client_socket.close()

def main():

    global cameraName
    global client_socket

    #se pone una prioridad máxima al proceso de este programa
    #Requiere de ser ejecutado con sudo 
    #os.nice(-10)

    correctConexion = False

    #Intento de conexion cada n segundos en caso de conexion fallida con el servidor
    while not correctConexion:

        try:
            client_socket = socket.socket(socket.AF_INET, socket.SOCK_STREAM)
            client_socket.connect((SERVER_IP, SERVER_PORT))

            #En el primer envío se manda el ID del usuario al que pertenece esta cámara
            client_socket.send(intTobytes(ID_CLIENT))

            #El segundo envío se manda el ID de la cámara del usuario
            client_socket.send(intTobytes(ID_CAMERA))

            #El tercer envío son MAX_CAM_NAME_BYTES bytes del string del nombre de la cámara
            client_socket.send(cameraName)

            correctConexion = True
            
        except:
            time.sleep(RETRY_SERVER_CONEXION_SECS)
    
    
    leaseSocketThread:SocketListenerThread = SocketListenerThread(client_socket=client_socket)
    
    signalRouter:SignalRouter = SignalRouter(leaseSocketThread, closeSocketConnection, client_socket)
    
    leaseSocketThread.setSignalRouter(signalRouter)


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