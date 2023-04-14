import socket
import sys
import io
import threading

HOST = "127.0.0.1"
PORT = int(sys.argv[1]) if (len(sys.argv) > 1) else 2323

SEPARATOR = '\n'


class Message:
    def __init__(self, **kwargs):
        if 'reader' in kwargs:
            reader = kwargs['reader']
            self.type = reader.readline()
            # Чтобы понять, что соединение закрылось
            if not self.type:
                return
            self.type = self.type[:-len(SEPARATOR)]
            length = int(reader.readline()[:-len(SEPARATOR)])
            self.text = reader.read(length)
            if len(self.text) != length:
                raise IOError('Unexpected eof')
        elif 'type' in kwargs:
            self.type = kwargs['type']
            if 'text' in kwargs:
                self.text = kwargs['text']
            else:
                self.text = ''
        else:
            raise ValueError('Incorrect arguments')

    @staticmethod
    def read(reader):
        return Message(reader=reader)

    def write(self, connection):
        connection.write(
            f'{self.type}{SEPARATOR}{len(self.text.encode("utf-16-le")) // 2}{SEPARATOR}{self.text}')
        connection.flush()

    @staticmethod
    def ok(text=''):
        return Message(type='OK', text=text)

    @staticmethod
    def error(text):
        return Message(type='ERROR', text=text)


def handle_connection(connection):
    output_file = 'output.txt'
    with connection.makefile(mode='rw', encoding='utf-8', newline=SEPARATOR) as sock_file:
        while True:
            try:
                message = Message.read(reader=sock_file)
                if not message.type:
                    break
                if message.type == 'TEXT':
                    with open(output_file, 'a') as f:
                        f.write(message.text)
                    Message.ok().write(sock_file)
                elif message.type == 'EXPRESSION':
                    result = str(eval(message.text))
                    Message.ok(result).write(sock_file)
                elif message.type == 'EXECUTE':
                    exec(message.text)
                    Message.ok().write(sock_file)
                elif message.type == 'SET_OUTPUT_FILE':
                    output_file = message.text
                    Message.ok().write(sock_file)
                else:
                    Message.error(f'UNKNOWN MESSAGE TYPE: {message.type}').write(sock_file)
            except Exception as e:
                Message.error(str(e)).write(sock_file)

if __name__ == '__main__':
    # Сразу закрываем возможность пользоваться IO, чтобы не было проблем с print() и input() внутри eval() и exec()
    sys.stdin = io.StringIO()
    sys.stdin.close()
    sys.stdout = io.StringIO()

    with socket.socket(socket.AF_INET, socket.SOCK_STREAM) as s:
        s.bind((HOST, PORT))
        s.listen()
        while True:
            conn, addr = s.accept()
            threading.Thread(target=handle_connection, args=(conn,)).start()
