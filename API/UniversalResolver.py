import requests

class ClientWebResolver:
    
    def __init__(self, config):
        self.CONFIG = config 

    def resolve(self, did):
        configFile = open(self.CONFIG, 'r')
        lines = configFile.readlines()

        try:
            for line in lines:
                if not line.endswith('/'):
                    line += '/'
                url = line + did
                httpResponse = requests.get(url) 
                if httpResponse.status_code is 200:
                    return httpResponse.json()
            raise TypeError
        except TypeError:
             print("All web services tested. Please add a working web resolver to the config file and try again.")

    def modifier(self, did):
        pass