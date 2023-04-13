import sys
import io

SEPARATOR = '\n'


class Message:
    def __init__(self, **kwargs):
        if 'reader' in kwargs:
            reader = kwargs['reader']
            self.type = reader.readline()[:-len(SEPARATOR)]
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

    def write(self):
        sys.stdout.write(f'{self.type}{SEPARATOR}{len(self.text.encode("utf-16-le")) // 2}{SEPARATOR}{self.text}')
        sys.stdout.flush()

    @staticmethod
    def read():
        return Message(reader=sys.stdin)

    @staticmethod
    def ok(text=''):
        return Message(type='OK', text=text)

    @staticmethod
    def error(text):
        return Message(type='ERROR', text=text)


def wrap_io():
    class EmptyIOWrapper:
        def __enter__(self):
            self.stdin = sys.stdin
            self.stdout = sys.stdout
            sys.stdin = io.StringIO()
            sys.stdin.close()
            sys.stdout = io.StringIO()

        def __exit__(self, exc_type, exc_val, exc_tb):
            sys.stdin = self.stdin
            sys.stdout = self.stdout
    return EmptyIOWrapper()


if __name__ == '__main__':
    sys.stdin.reconfigure(newline=SEPARATOR)
    output_file = 'output.txt'
    while True:
        try:
            message = Message.read()
            if message.type == 'TEXT':
                with open(output_file, 'a') as f:
                    f.write(message.text)
                Message.ok().write()
            elif message.type == 'EXPRESSION':
                with wrap_io():
                    result = str(eval(message.text))
                Message.ok(result).write()
            elif message.type == 'EXECUTE':
                with wrap_io():
                    exec(message.text)
                Message.ok().write()
            elif message.type == 'SET_OUTPUT_FILE':
                output_file = message.text
                Message.ok().write()
            else:
                Message.error(f'UNKNOWN MESSAGE TYPE: {message.type}').write()
        except Exception as e:
            Message.error(str(e)).write()
