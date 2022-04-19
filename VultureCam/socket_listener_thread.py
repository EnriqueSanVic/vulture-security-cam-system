from threading import Thread
import socket


class SocketListenerThread(Thread):

    def __init__(self, client_socket:socket):

        Thread.__init__(self,target=self.initLeaseSocket, args=())

        self.client_socket = client_socket



    def initLeaseSocket():

        print("escuchando")
