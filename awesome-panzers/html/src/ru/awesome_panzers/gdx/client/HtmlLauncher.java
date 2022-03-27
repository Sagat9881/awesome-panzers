package ru.awesome_panzers.gdx.client;

import com.badlogic.gdx.ApplicationListener;
import com.badlogic.gdx.backends.gwt.GwtApplication;
import com.badlogic.gdx.backends.gwt.GwtApplicationConfiguration;
import com.google.gwt.user.client.Timer;
import ru.awesome_panzers.gdx.Starter;
import ru.awesome_panzers.gdx.client.ws.EventListenerCallback;
import ru.awesome_panzers.gdx.client.ws.WebSocket;
import ru.awesome_panzers.gdx.dto.InputStateImpl;

import java.util.concurrent.atomic.AtomicBoolean;

public class HtmlLauncher extends GwtApplication {

    @Override
    public GwtApplicationConfiguration getConfig() {
        // Resizable application, uses available space in browser
        return new GwtApplicationConfiguration(true);
        // Fixed size application:
        //return new GwtApplicationConfiguration(480, 320);
    }

    private native WebSocket getWebSocket(String url)
                /*-{
                    return new WebSocket(url);
                }-*/
    ;

    private native void log(Object obj)
                /*-{
                    console.log(obj);
                }-*/

    ;

    private native String toJson(Object obj)
                /*-{
                    return JSON.stringify(obj);
                }-*/

    ;

    @Override
    public ApplicationListener createApplicationListener() {
        WebSocket client = getWebSocket("ws://localhost:8888/ws");
        AtomicBoolean once = new AtomicBoolean();
        Starter starter = new Starter(new InputStateImpl());
        starter.setSender(message->{
            client.send(toJson(message));
        });

        Timer timer = new Timer() {

            @Override
            public void run() {
                starter.handleTimer();
            }
        };
        timer.scheduleRepeating(1000/30);

        EventListenerCallback eventListenerCallback = event -> {
            if(!once.get()){
                client.send("hello");
                once.set(true);
            }
            log(event.getData());
        };
        client.addEventListener("open", eventListenerCallback);
        client.addEventListener("close", eventListenerCallback);
        client.addEventListener("error", eventListenerCallback);
        client.addEventListener("message", eventListenerCallback);

        return starter;
    }
}