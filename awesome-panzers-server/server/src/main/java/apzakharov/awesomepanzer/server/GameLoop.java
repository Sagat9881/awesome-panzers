package apzakharov.awesomepanzer.server;

import apzakharov.awesomepanzer.server.actors.Panzer;
import apzakharov.awesomepanzer.server.ws.WebSocketHandler;
import com.badlogic.gdx.ApplicationAdapter;
import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.utils.Array;
import com.badlogic.gdx.utils.Json;
import com.badlogic.gdx.utils.ObjectMap;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;
import org.springframework.web.socket.adapter.standard.StandardWebSocketSession;

import java.io.IOException;
import java.util.concurrent.ForkJoinPool;

@Component
public class GameLoop extends ApplicationAdapter {
    private static final float frameRate = 1 / 30f;
    private final WebSocketHandler handler;
    private float lastRender = 0;
    private final Json json;
    private final ForkJoinPool pool = ForkJoinPool.commonPool();

    private final ObjectMap<String, Panzer> panzers = new ObjectMap<>();
    private final Array<Panzer> stateToSend = new Array<>();

    @Autowired
    public GameLoop(WebSocketHandler handler, Json json) {
        this.handler = handler;
        this.json = json;
    }

    @Override
    public void create() {
        handler.setConnectListener(session -> {
            Panzer panzer = new Panzer();
            panzer.setId(session.getId());
            panzers.put(session.getId(), panzer);
            try {
                session
                        .getNativeSession()
                        .getBasicRemote()
                        .sendText(
                                String.format("{\"class\": \"sessionKey\", \"id\": \"%s\"}", session.getId())
                        );
            } catch (IOException e) {
                e.printStackTrace();
            }
        });
        handler.setDisConnectListener(session -> {
            sendAll(
                    String.format("{\"class\": \"evict\", \"id\": \"%s\"}", session.getId())
            );
            panzers.remove(session.getId());
        });
        handler.setMessageListener(((session, message) -> {
            pool.execute(() -> {
                String type = message.get("type").asText();
                switch (type) {
                    case "state":
                        Panzer panzer = panzers.get(session.getId());
                        panzer.setLeftPressed(message.get("leftPressed").asBoolean());
                        panzer.setRightPressed(message.get("rightPressed").asBoolean());
                        panzer.setUpPressed(message.get("upPressed").asBoolean());
                        panzer.setDownPressed(message.get("downPressed").asBoolean());
                        panzer.setAngel((float) message.get("angle").asDouble());
                        break;
                    default:
                        throw new RuntimeException("Unknown WS object type: " + type);
                }
            });
        }));
    }

    @Override
    public void render() {
        lastRender += Gdx.graphics.getDeltaTime();
        if (lastRender >= frameRate) {
            stateToSend.clear();
            for (ObjectMap.Entry<String, Panzer> panzerEntry : panzers) {
                Panzer panzer = panzerEntry.value;
                panzer.act(lastRender);
                stateToSend.add(panzer);
            }

            lastRender = 0;
            String statesJson = json.toJson(stateToSend);
            sendAll(statesJson);
        }

    }

    private void sendAll(String json) {
        pool.execute(() -> {

            for (StandardWebSocketSession session : handler.getSessions()) {
                try {
                    if (session.isOpen()) {
                        session.getNativeSession().getBasicRemote().sendText(json);
                    }
                } catch (IOException e) {
                    e.printStackTrace();
                }
            }
        });
    }
}
