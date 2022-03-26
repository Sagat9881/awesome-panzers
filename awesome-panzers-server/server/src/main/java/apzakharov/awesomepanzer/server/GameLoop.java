package apzakharov.awesomepanzer.server;

import apzakharov.awesomepanzer.server.ws.WebSocketHandler;
import com.badlogic.gdx.ApplicationAdapter;
import com.badlogic.gdx.utils.Array;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;
import org.springframework.web.socket.TextMessage;
import org.springframework.web.socket.WebSocketSession;

import java.io.IOException;

@Component
public class GameLoop extends ApplicationAdapter {

    private final WebSocketHandler handler;
    private final Array<String> events = new Array<>();

    @Autowired
    public GameLoop(WebSocketHandler handler) {
        this.handler = handler;

    }

    @Override
    public void create() {
        handler.setConnectListener(session -> {
            events.add(session.getId() + " присоединился к игре");
        });
        handler.setDisConnectListener(session -> {
            events.add(session.getId() + " покинул игру");
        });
        handler.setMessageListener(((session, message) -> {
            events.add(session.getId() + " базарит: " + message);
        }));
    }

    @Override
    public void render() {
        for (WebSocketSession session : handler.getSessions()) {
            try {
                for (String event : events) {

                    session.sendMessage(new TextMessage(event));
                }
            } catch (IOException e) {
                e.printStackTrace();
            }
            events.clear();
        }
    }
}
