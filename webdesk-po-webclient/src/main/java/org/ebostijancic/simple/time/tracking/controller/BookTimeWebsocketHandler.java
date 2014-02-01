package org.ebostijancic.simple.time.tracking.controller;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.web.socket.CloseStatus;
import org.springframework.web.socket.TextMessage;
import org.springframework.web.socket.WebSocketMessage;
import org.springframework.web.socket.WebSocketSession;
import org.springframework.web.socket.handler.TextWebSocketHandler;


public class BookTimeWebsocketHandler extends TextWebSocketHandler {
	
	private static final Logger logger = LoggerFactory.getLogger(BookTimeWebsocketHandler.class);

	public BookTimeWebsocketHandler() {}
	
	@Override
	public void afterConnectionClosed(WebSocketSession session,
			CloseStatus status) throws Exception {
		logger.debug("Session Closed: " + session.getId() +  " " + status.getReason());
	}
	
	@Override
	public void handleMessage(WebSocketSession session,
			WebSocketMessage<?> message) throws Exception {
		super.handleMessage(session, message);
	}

	@Override
	public void handleTransportError(WebSocketSession session,
			Throwable exception) throws Exception {
		super.handleTransportError(session, exception);
	}

	@Override
	public void afterConnectionEstablished(WebSocketSession session)
			throws Exception {
		logger.debug("Session established: " + session.getId());
	}

	@Override
	protected void handleTextMessage(WebSocketSession session,
			TextMessage message) throws Exception {
		logger.debug(message.getPayload());
	}
}
