'''
Este método transforma un tipo int de python 
en una respresentación binaria de un signed integer 32 bits
que en esencia es un array de 4 bytes de python, o un objeto bytes
'''
def intTobytes(integer:int)-> bytes:
    return integer.to_bytes(length=4, byteorder= 'big', signed=True)


'''
Este método prepara el envío binario de un str a su codificación en bytes.
También prepara para tener una longitud fija de tantos bytes/caracteres como 
indique la variable length. 

Si tiene el mismo número de bytes que el tamaño indicado
en length entonces solo se transforma en los bytes del str original.

Si tiene menos bytes que el tamaño indicado entonces e rellena la cadena por la derecha
con el caracter o la cadena de caracteres recebida en el parámetro char.

Ejemplo:
input params: (string="Hola que tal", length:20, char:"-")
return bytes("Hola que tal--------")

Si el string original supera la longitud establecida retorna None.
'''
def formatString(string:str, length:int, char:str)->bytes:

    if len(string) >= length:
        return None
    else:
        difference = length - len(string)
        string = string + (char*difference)
        return str.encode(string)
