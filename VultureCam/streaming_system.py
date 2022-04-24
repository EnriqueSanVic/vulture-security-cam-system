
from threading import Thread
import socket
from binary_utils import *
import picamera
import socket
from threading import Thread
import time
import io
import socket
import datetime as dt
from constants import *

class SignalRouter:

    def __init__(self, socketListenerThread, streamingThread):
        
        self.socketListenerThread = socketListenerThread
        self.streamingThread = streamingThread

    def processSignal(self, signal:int):

        print("Señal recibida: " + str(signal))

        if signal == STOP_STREAMING_FROM_SERVER_SIGNAL:
            self.streamingThread.stopStreaming()

        elif signal == START_STREAMING_FROM_SERVER_SIGNAL:
            self.streamingThread.startStreaming()


class SocketListenerThread(Thread):

    def __init__(self, client_socket:socket):

        Thread.__init__(self,target=self.initLeaseSocket, args=())

        self.client_socket = client_socket

    def setSignalRouter(self, signalRouter:SignalRouter):
        self.signalRouter = signalRouter

    def initLeaseSocket(self):

        signalBytes:bytes

        signal:int

        try:

            fileSocket = self.client_socket.makefile()

            while True:

                print("Escuchando señales")

                signalBytes = self.client_socket.recv(4)

                self.signalRouter.processSignal(bytesToInt(signalBytes))
        
        except:
            pass


class StreamingThread(Thread):

    def __init__(self, client_socket:socket):

        Thread.__init__(self,target=self.streamingLoop, args=())

        self.client_socket = client_socket

        self.activo = False

        #Configuración de Pi Camera
        self.camera = picamera.PiCamera()
        self.camera.resolution = (WIDTH_FRAME, HEIGHT_FRAME)
        self.camera.framerate = 25
        self.camera.iso = 800
        self.camera.annotate_background = picamera.Color('black')
        self.camera.annotate_text_size = 20

    def setSignalRouter(self, signalRouter:SignalRouter):
        self.signalRouter = signalRouter

    def stopStreaming(self):
        self.activo = False

    def startStreaming(self):
        
        if self.activo == False:
            self.activo = True
            self.start()
           

    def getTextInfoFrame(self)->str:
        return CAMERA_NAME + ' ' + dt.datetime.now().strftime('%d-%m-%Y %H:%M:%S')


    def streamingLoop(self):

        buffer = io.BytesIO()

        startTime = time.time()

        print("Client ID " + str(ID_CLIENT))
        print("Camera name " + CAMERA_NAME)

        try: 

            while self.activo:

                self.camera.annotate_text = self.getTextInfoFrame()

                #Se limpia el buffer
                buffer.seek(0)
                buffer.truncate(0)

                self.camera.capture(output=buffer, format=FRAME_FORMAT)

                bufferBytes = buffer.getvalue()

                bufferSize = len(bufferBytes)

                print("Next frame size: " + str(bufferSize) + " bytes")

                print("Exposure speed: " + str(self.camera.exposure_speed ))

                signed32IntBytes = intTobytes(bufferSize)

                self.client_socket.send(signed32IntBytes)

                self.client_socket.send(bufferBytes)

            endTime = time.time()   

            print("Total time: " + str(endTime - startTime)) 
            
            self.client_socket.send(intTobytes(CONFIRM_STOP_STREAMING_TO_SERVER_SIGNAL))

            print("Conexion cerrada...") 

        except:
            pass
    

