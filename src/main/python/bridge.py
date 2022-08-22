import threading

import websocket

from base_websocket import *


class Server:
    def __init__(self, server_ip, port):
        self.processor = MessageProcessor(server=self)
        self.console = Console()
        self.server_ip = server_ip
        self.port = port
        self.websocket = None

    async def proxy(self, websocket):
        self.console.log("连接成功")
        self.websocket = websocket
        await self.subscribe("commandRequest", "PlayerMessage")

        async for msg in self.websocket:
            """游戏发包"""
            self.processor.message = msg

    async def subscribe(self, *events):
        for event in events:
            await self.websocket.send(
                json.dumps({
                    "header": {
                        "version": 1,
                        "requestId": f'{uuid4()}',
                        "messageType": f"CommandRequest",
                        "messagePurpose": "subscribe"
                    },
                    "body": {
                        "eventName": f"{event}"
                    },
                })
            )
            self.console.log(f"已成功订阅事件：{event}")

    async def send(self, message):
        await self.websocket.send(
            json.dumps({
                "header": {
                    "version": 1,
                    "requestId": f'{uuid4()}',
                    "messagePurpose": "commandRequest",
                    "messageType": "commandRequest",
                },
                "body": {
                    "version": 1,
                    "commandLine": f'{message}',
                    "sender": "Player"
                }
            })
        )
        # self.console.log(f"已发送 {message!r} 至服务器")

    async def run(self):
        async with ws.serve(self.proxy, self.server_ip, self.port):
            self.console.log(f"服务器已在 {self.server_ip}:{self.port} 开启")
            await asyncio.Future()


class Client:
    def __init__(self, server_uri):
        self.processor = MessageProcessor(client=self)
        self.uri = server_uri
        self.websocket = websocket.WebSocketApp(server_uri, on_message=self.on_message)

    def run(self):
        self.websocket.run_forever()

    def on_message(self, ws, message):
        """服务器发包"""
        self.processor.message = message

    def send(self, message):
        self.websocket.send(message)


class MessageProcessor:
    __instance = None
    __client = None
    __server = None
    __message = None
    loop = None

    def __new__(cls, *, client=None, server=None):
        if cls.__instance is None:
            cls.__instance = super().__new__(cls)

        if client is not None and cls.__client is None:
            cls.__client = client

        if server is not None and cls.__server is None:
            cls.__server = server

        return cls.__instance

    @property
    def message(self):
        return self.__message

    @message.setter
    def message(self, value):
        try:
            json.loads(value)
            self.__client.send(value)
        except json.decoder.JSONDecodeError:
            asyncio.run(self.__server.send(value))

        self.__message = value


def run_server(ip, port):
    server = Server(ip, port)
    asyncio.run(server.run())


def run_client(uri):
    client = Client(uri)
    client.run()


def main():
    mc_to_spring = threading.Thread(target=run_server, args=("192.168.31.148", 8765))
    spring_to_mc = threading.Thread(target=run_client, args=("ws://localhost:8080/websocket", ))
    mc_to_spring.start()
    spring_to_mc.start()

    mc_to_spring.join()
    spring_to_mc.join()


if __name__ == "__main__":
    main()
