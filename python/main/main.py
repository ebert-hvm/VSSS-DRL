import json
from urllib.request import urlopen
from ai import AI

if __name__ == "__main__":
    try:
        resource_path = "../resources/parameters.json"
        #parameters = json.load(resource_path)
        ai = AI()
        ai.start()
    except Exception as ex:
        print(ex)