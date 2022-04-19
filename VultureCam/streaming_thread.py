import picamera
import socket
from threading import Thread
import time
import io
import socket
import datetime as dt
from binary_utils import *
from constants import *

class StreamingThread(Thread):

    def __init__(self, client_socket:socket, cameraName:str):

        Thread.__init__(self,target=self.streamingLoop, args=())

        self.cameraName = cameraName

        self.client_socket = client_socket

        self.activo = True

        #Configuración de Pi Camera
        self.camera = picamera.PiCamera()
        self.camera.resolution = (WIDTH_FRAME, HEIGHT_FRAME)
        self.camera.framerate = 10
        self.camera.annotate_background = picamera.Color('black')
        self.camera.annotate_text_size = 20


    def getTextInfoFrame(self)->str:
        return CAMERA_NAME + ' ' + dt.datetime.now().strftime('%d-%m-%Y %H:%M:%S')

    def stopStreaming(self):
        self.activo = False

    def streamingLoop(self):

        buffer = io.BytesIO()

        startTime = time.time()

        print("Client ID " + str(ID_CLIENT))
        print("Camera name " + CAMERA_NAME)

        
        #En el primer envío se manda el ID del usuario al que pertenece esta cámara
        self.client_socket.send(intTobytes(ID_CLIENT))

        #El segundo envío son MAX_CAM_NAME_BYTES bytes del string del nombre de la cámara
        self.client_socket.send(self.cameraName)

        while self.activo:

            self.camera.annotate_text = self.getTextInfoFrame()

            #Se limpia el buffer
            buffer.seek(0)
            buffer.truncate(0)

            self.camera.capture(output=buffer, format=FRAME_FORMAT)

            bufferBytes = buffer.getvalue()

            bufferSize = len(bufferBytes)

            print("Next frame size: " + str(bufferSize) + " bytes")

            signed32IntBytes = intTobytes(bufferSize)

            self.client_socket.send(signed32IntBytes)

            self.client_socket.send(bufferBytes)

        endTime = time.time()   

        print("Total time: " + str(endTime - startTime)) 
        
        self.client_socket.send(intTobytes(CLOSE_CONNECTION_COMMAND))

        print("Conexion cerrada...") 