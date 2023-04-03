import websocket

url = 'ws://localhost:15000'
ws = websocket.create_connection(url)

data = 'ping'

ws.send(data)
print(ws.recv())

ws.close()
