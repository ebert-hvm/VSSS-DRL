import threading

class SharedObject:
    def __init__(self, value):
        self.value = value
        self.lock = threading.Lock()

    def set_value(self, value):
        with self.lock:
            self.value = value

    def get_value(self):
        with self.lock:
            return self.value
