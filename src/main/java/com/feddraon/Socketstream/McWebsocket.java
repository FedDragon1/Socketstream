package com.feddraon.Socketstream;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.boot.json.JsonParser;
import org.springframework.boot.json.JsonParserFactory;
import org.springframework.stereotype.Component;

import javax.websocket.*;
import javax.websocket.server.ServerEndpoint;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.util.LinkedHashMap;
import java.util.LinkedList;
import java.util.Map;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.Future;

@Component
@ServerEndpoint("/websocket")
public class McWebsocket {
    private static final int BUFF_FRAME = 10;
    private int frameCount = 0;
    private final LinkedList<Future<String>> functionQueue = new LinkedList<>();
    private Session session;
    private final JsonParser parser = JsonParserFactory.getJsonParser();
    private final Logger logger = LoggerFactory.getLogger(McWebsocket.class);
    private boolean playingStream = false;
    private final Bridge bridge = new Bridge("C:\\Users\\fedel\\AppData\\Local\\Packages\\Microsoft.MinecraftUWP_8wekyb3d8bbwe\\LocalState\\games\\com.mojang\\minecraftWorlds\\KlgBY-NXAAA=\\behavior_packs\\PyStream\\functions",
            "rtmp://127.0.0.1:1935/live/test001");

    public McWebsocket() throws FileNotFoundException {
        new Thread(this::playStream).start();
    }

    @OnOpen
    public void OnOpen(Session session) {
        this.session = session;
        logger.info("连接成功");
    }

    @OnClose
    public void OnClose() {
        logger.info("连接已退出");
        this.renew(false);
    }

    @OnMessage
    public void OnMessage(String message) {
        Map<String, Object> map = parser.parseMap(message);
        Map<String, Object> body = (LinkedHashMap<String, Object>)map.get("body");
        if (body.containsKey("sender") && !(body.get("sender")).equals("外部")) {
            String[] arguments = ((String) body.get("message")).split(" ", 2);
            if (arguments[0].equals("stream")) {
                if (arguments.length != 1) {
                    if (arguments[1].equals("end")) {
                        renew(true);
                        return;
                    }
                    bridge.setRtmpURI(arguments[1]);
                }
                this.playingStream = true;
            }
        }

    }

    @OnError
    public void OnError(Throwable e) {
        logger.warn("新的错误：" + e);
    }

    private void playStream() {
        while (true) {
            try {
                tick();
            } catch (InterruptedException | ExecutionException e) {
                e.printStackTrace();
                break;
            }
        }
    }

    private void tick() throws InterruptedException, ExecutionException {
        if (playingStream) {
            long startTime = System.currentTimeMillis();
            functionQueue.add(bridge.produceNextFrame());
            dispatch();
            long endTime = System.currentTimeMillis();
            Thread.sleep(Math.max(90 - endTime + startTime, 0));
        } else {
            bridge.idle();
            Thread.sleep(50);
        }
    }

    private void renew(boolean sendFill) {
        playingStream = false;
        frameCount = BUFF_FRAME;
        functionQueue.forEach(fut -> fut.cancel(true));
        functionQueue.clear();
        try {
            Thread.sleep(200);
            if (sendFill) {
                session.getBasicRemote().sendText("/fill 0 -40 0 0 33 131 concrete 15");
            }
        } catch (IOException | InterruptedException e) {
            e.printStackTrace();
        }
    }

    private void dispatch() throws ExecutionException, InterruptedException {
        frameCount++;
        if (frameCount <= BUFF_FRAME) {
            return;
        }

        Future<String> fut = functionQueue.poll();
        assert fut != null;
        String command = fut.get();

        try {
            if ((frameCount - 1) % Bridge.N_FILES == 0) {
                session.getBasicRemote().sendText("/reload");
            }
            session.getBasicRemote().sendText(command);
        } catch (IOException e) {
            e.printStackTrace();
        }

    }
}



