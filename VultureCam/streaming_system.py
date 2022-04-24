
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

    def __init__(self, socketListenerThread, streamingThread, closeSocketFunction):
        
        self.socketListenerThread = socketListenerThread
        self.streamingThread = streamingThread
        self.closeSocketFunction = closeSocketFunction

    def processSignal(self, signalBytes:bytes):
        
        #se transforman en el numero entero de la señal a la que representan
        signal:int = bytesToInt(signalBytes)

        print("Señal recibida: " + str(signal))

        if signal == STOP_STREAMING_FROM_SERVER_SIGNAL:
            self.streamingThread.stopStreaming()

        elif signal == START_STREAMING_FROM_SERVER_SIGNAL:
            self.streamingThread.startStreaming()
        elif signal == SHUTDOWN_CAMERA_FROM_SERVER_SIGNAL:
            self.streamingThread.shutdownStreaming()
            self.socketListenerThread.stopLease()
            


class SocketListenerThread(Thread):

    def __init__(self, client_socket:socket):
        
        Thread.__init__(self,target=self.initLeaseSocket, args=())

        self.client_socket = client_socket
        self.active = True

    def setSignalRouter(self, signalRouter:SignalRouter):
        self.signalRouter = signalRouter

    def stopLease(self):
        self.active = False

    def initLeaseSocket(self):

        signalBytes:bytes

        try:

            while self.active:

                print("Escuchando señales")

                signalBytes = self.client_socket.recv(4)

                self.signalRouter.processSignal(signalBytes)
        
        except:
            pass


class StreamingThread(Thread):

    def __init__(self, client_socket:socket):

        Thread.__init__(self,target=self.streamingLoop, args=())

        self.client_socket = client_socket

        self.activo = False
        self.shutdown = False
        
        

    def setSignalRouter(self, signalRouter:SignalRouter):
        self.signalRouter = signalRouter

    def shutdownStreaming(self):
        self.stopStreaming()
        self.shutdown = True

    def stopStreaming(self):
        self.activo = False

    def startStreaming(self):
        
        if self.activo == False:
            self.activo = True
            self.start()
           

    def getTextInfoFrame(self)->str:
        return CAMERA_NAME + ' ' + dt.datetime.now().strftime('%d-%m-%Y %H:%M:%S')


    def streamingLoop(self):

        #Configuración de Pi Camera
        self.camera = picamera.PiCamera()
        self.camera.resolution = (WIDTH_FRAME, HEIGHT_FRAME)
        self.camera.framerate = FRAME_RATE
        #self.camera.iso = ISO
        self.camera.annotate_background = picamera.Color('black')
        self.camera.annotate_text_size = 20

        buffer = io.BytesIO()

        startTime = time.time()

        print("Client ID " + str(ID_CLIENT))
        print("Camera name " + CAMERA_NAME)

        try: 

            for iter in self.camera.capture_continuous(output=buffer, format=FRAME_FORMAT, use_video_port=True):

                print("captura finalizada")
                #condicion de salida
                if not self.activo:
                    break

                
                self.camera.annotate_text = self.getTextInfoFrame()

                buffer.seek(0)
                bufferBytes = buffer.read()

                bufferSize = len(bufferBytes)

                print("Next frame size: " + str(bufferSize) + " bytes")

                print("Exposure speed: " + str(self.camera.exposure_speed ))

                signed32IntBytes = intTobytes(bufferSize)

                self.client_socket.send(signed32IntBytes)

                self.client_socket.send(bufferBytes)

                
                #Se limpia el buffer
                buffer.seek(0)
                buffer.truncate()

                
                print("captura iniciada")

            self.camera.close()

            endTime = time.time()   

            print("Total time: " + str(endTime - startTime)) 
            
            #envío de señal de cierre
            if not self.shutdown:
                self.client_socket.send(intTobytes(CONFIRM_STOP_STREAMING_TO_SERVER_SIGNAL))
            elif self.shutdown:
                self.client_socket.send(intTobytes(CONFIRM_SHUTDOWN_CAMERA_TO_SERVER_SIGNAL))

            print("Conexion cerrada...") 

        except:
            pass
    

