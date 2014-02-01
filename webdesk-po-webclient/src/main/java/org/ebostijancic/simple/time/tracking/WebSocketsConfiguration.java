package org.ebostijancic.simple.time.tracking;

import org.springframework.context.annotation.Configuration;
import org.springframework.web.servlet.HandlerMapping;
import org.springframework.web.socket.config.annotation.EnableWebSocket;
import org.springframework.web.socket.config.annotation.WebSocketConfigurationSupport;
import org.springframework.web.socket.config.annotation.WebSocketHandlerRegistry;

@Configuration
@EnableWebSocket
public class WebSocketsConfiguration extends WebSocketConfigurationSupport {

	@Override
	protected void registerWebSocketHandlers(WebSocketHandlerRegistry registry) {
		super.registerWebSocketHandlers(registry);
	}

	@Override
	public HandlerMapping webSocketHandlerMapping() {
		return super.webSocketHandlerMapping();
	}

}
