import io
import socket
import time
import picamera
import datetime as dt
from binary_utils import *


SERVER_IP = '192.168.1.103'
SERVER_PORT = 4548 

ID_CLIENT = 23
CAMERA_NAME = 'test'

'''
320x240
'''

WIDTH_FRAME = 640
HEIGHT_FRAME = 480

FRAME_FORMAT = 'jpeg'

BACKFILL_CHARACTER_STRINGS = "*"
MAX_CAM_NAME_BYTES = 20

CLOSE_CONNECTION_COMMAND = -2341
CAM_FRAMES_RECORD = 20

errorInit = False

framesCounter = 0
cameraName = formatString(CAMERA_NAME, MAX_CAM_NAME_BYTES, BACKFILL_CHARACTER_STRINGS)

if cameraName is None:
    errorInit = True

camera = picamera.PiCamera()

camera.resolution = (WIDTH_FRAME, HEIGHT_FRAME)
camera.framerate = 10

camera.annotate_background = picamera.Color('black')
camera.annotate_text_size = 20

client_socket = socket.socket(socket.AF_INET, socket.SOCK_STREAM)
client_socket.connect((SERVER_IP, SERVER_PORT))




def getTextInfoFrame()->str:
    return CAMERA_NAME + ' ' + dt.datetime.now().strftime('%d-%m-%Y %H:%M:%S')

def mainLoop():

    global framesCounter

    buffer = io.BytesIO()

    startTime = time.time()

    print("Client ID " + str(ID_CLIENT))
    print("Camera name " + CAMERA_NAME)

    
    #En el primer envío se manda el ID del usuario al que pertenece esta cámara
    client_socket.send(intTobytes(ID_CLIENT))

    #El segundo envío son MAX_CAM_NAME_BYTES bytes del string del nombre de la cámara
    client_socket.send(cameraName)

    while framesCounter < CAM_FRAMES_RECORD:

        camera.annotate_text = getTextInfoFrame()

        #Se limpia el buffer
        buffer.seek(0)
        buffer.truncate(0)

        camera.capture(output=buffer, format=FRAME_FORMAT)

        bufferBytes = buffer.getvalue()

        bufferSize = len(bufferBytes)

        print("Next frame size: " + str(bufferSize) + " bytes")

        signed32IntBytes = intTobytes(bufferSize)

        client_socket.send(signed32IntBytes)

        client_socket.send(bufferBytes)

        framesCounter+=1

        print("Frame nº" + str(framesCounter))

    endTime = time.time()   

    print("Total time: " + str(endTime - startTime)) 
    
    client_socket.send(intTobytes(CLOSE_CONNECTION_COMMAND))

    client_socket.close()

    print("Conexion cerrada...") 



if errorInit == False:
    mainLoop()
else:
    print("No se ha podido iniciar el streaming debido a las condiciones iniciales...")    