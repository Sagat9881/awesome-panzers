package apzakharov.awesomepanzer.server.config;


import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Configuration;
import org.springframework.web.socket.config.annotation.EnableWebSocket;
import org.springframework.web.socket.config.annotation.WebSocketConfigurer;
import org.springframework.web.socket.config.annotation.WebSocketHandlerRegistry;
import apzakharov.awesomepanzer.server.ws.WebSocketHandler;

@Configuration
@EnableWebSocket

public class WebSocketConfig implements WebSocketConfigurer {
    private final WebSocketHandler handler;

    @Autowired
    public WebSocketConfig(WebSocketHandler handler) {
        this.handler = handler;
    }

    @Override
    public void registerWebSocketHandlers(WebSocketHandlerRegistry registry) {
        registry.addHandler(handler,"/ws").setAllowedOrigins("*");
    }
}
