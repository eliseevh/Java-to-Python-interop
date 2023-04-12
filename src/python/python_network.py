import socket
import sys
import io
import threading

HOST = "127.0.0.1"
PORT = int(sys.argv[1]) if (len(sys.argv) > 1) else 2323

SEPARATOR = '\n'


class Message:
    def __init__(self, **kwargs):
        if 'type' in kwargs:
            self.type = kwargs['type']
            if 'text' in kwargs:
                self.text = kwargs['text']
            else:
                self.text = ''
        else:
            raise ValueError('Incorrect arguments')

    def write(self, connection):
        connection.sendall(f'{self.type}{SEPARATOR}{len(self.text)}{SEPARATOR}{self.text}'.encode('utf-8'))

    @staticmethod
    def ok(text=''):
        return Message(type='OK', text=text)

    @staticmethod
    def error(text):
        return Message(type='ERROR', text=text)


def handle_connection(connection):
    output_file = 'output.txt'
    with connection:
        while True:
            # Тут я полагаюсь, что всё сообщение целиком передается одним куском
            # (потому что connection.recv прекращает читать, если некоторое небольшое время данных нет)
            # и занимает не больше 4096 байт. В большинстве случаев это будет так(4096 байт - очень большое сообщение),
            # поэтому считаю разумным здесь на это положиться
            data = connection.recv(4096)
            if not data:
                break
            data = data.decode('utf-8')
            try:
                message_type, message_length, text = data.split(SEPARATOR, 2)
                if len(text) != int(message_length):
                    raise IOError('unexpected eof')
                message = Message(type=message_type, text=text)
                if message.type == 'TEXT':
                    with open(output_file, 'a') as f:
                        f.write(message.text)
                    Message.ok().write(connection)
                elif message.type == 'EXPRESSION':
                    result = str(eval(message.text))
                    Message.ok(result).write(connection)
                elif message.type == 'EXECUTE':
                    exec(message.text)
                    Message.ok().write(connection)
                elif message.type == 'SET_OUTPUT_FILE':
                    output_file = message.text
                    Message.ok().write(connection)
                else:
                    Message.error(f'UNKNOWN MESSAGE TYPE: {message.type}').write(connection)
            except Exception as e:
                Message.error(str(e)).write(connection)


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
