package com.shiyanlou.chat;

import java.io.IOException;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

import javax.websocket.OnClose;
import javax.websocket.OnError;
import javax.websocket.OnMessage;
import javax.websocket.OnOpen;
import javax.websocket.Session;
import javax.websocket.server.PathParam;
import javax.websocket.server.ServerEndpoint;

import net.sf.json.JSONObject;

/**
 * 聊天服务器类
 * 
 * @author shiyanlou
 *
 */
@ServerEndpoint("/websocket")
public class ChatServer {
	// private static final SimpleDateFormat DATE_FORMAT = new
	// SimpleDateFormat("yyyy-MM-dd HH:mm"); // 日期格式化
	//
	// @OnOpen
	// public void open(Session session) {
	// // 添加初始化操作
	// System.out.println("连接成功");
	// }
	//
	// /**
	// * 接受客户端的消息，并把消息发送给所有连接的会话
	// *
	// * @param message
	// * 客户端发来的消息
	// * @param session
	// * 客户端的会话
	// */
	// @OnMessage
	// public void getMessage(String message, Session session) {
	// System.out.println("接收消息：" + message);
	// // 把客户端的消息解析为JSON对象
	// JSONObject jsonObject = JSONObject.fromObject(message);
	// // 在消息中添加发送日期
	// jsonObject.put("date", DATE_FORMAT.format(new Date()));
	// // 把消息发送给所有连接的会话
	// for (Session openSession : session.getOpenSessions()) {
	// // 添加本条消息是否为当前会话本身发的标志
	// jsonObject.put("isSelf", openSession.equals(session));
	// // 发送JSON格式的消息
	// openSession.getAsyncRemote().sendText(jsonObject.toString());
	// }
	// }
	//
	// @OnClose
	// public void close() {
	// // 添加关闭会话时的操作
	// }
	//
	// @OnError
	// public void error(Throwable t) {
	// // 添加处理错误的操作
	// }
	private static final SimpleDateFormat DATE_FORMAT = new SimpleDateFormat("yyyy-MM-dd HH:mm"); // 日期格式化
	private static int onlineCount = 0;
	private static Map<String, ChatServer> clients = new ConcurrentHashMap<String, ChatServer>();
	private Session session;
	private String username;

	@OnOpen
	public void onOpen(@PathParam("username") String username, Session session) throws IOException {

		this.username = username;
		this.session = session;
		// System.out.println(System.currentTimeMillis());
		username = System.currentTimeMillis() + "";
		// System.currentTimeMillis();
		addOnlineCount();
		clients.put(username, this);
		System.out.println("已连接");
	}

	@OnClose
	public void onClose() throws IOException {
		clients.remove(username);
		subOnlineCount();
	}

	@OnMessage
	public void onMessage(String message) throws IOException {
		System.out.println(message);
		sendMessageAll(message);
		// JSONObject jsonTo = JSONObject.fromObject(message);
		//
		// if (!jsonTo.get("To").equals("All")) {
		// sendMessageTo("给一个人", jsonTo.get("To").toString());
		// } else {
		// sendMessageAll("给所有人");
		// }
	}

	@OnError
	public void onError(Session session, Throwable error) {
		error.printStackTrace();
	}

	public void sendMessageTo(String message, String To) throws IOException {
		// session.getBasicRemote().sendText(message);
		// session.getAsyncRemote().sendText(message);
		for (ChatServer item : clients.values()) {
			if (item.username.equals(To))
				item.session.getAsyncRemote().sendText(message);
		}
	}

	public void sendMessageAll(String message) throws IOException {

		System.out.println("接收消息：" + message);
		// 把客户端的消息解析为JSON对象
		JSONObject jsonObject = JSONObject.fromObject(message);
		// 在消息中添加发送日期
		jsonObject.put("date", DATE_FORMAT.format(new Date()));
		// 把消息发送给所有连接的会话
		for (ChatServer item : clients.values()) {
			// 添加本条消息是否为当前会话本身发的标志
			jsonObject.put("isSelf", item.session.equals(session));
			// 发送JSON格式的消息
			item.session.getAsyncRemote().sendText(jsonObject.toString());
			// item.session.getAsyncRemote().sendText(message);
		}
		// for (Session openSession : session.getOpenSessions()) {
		// // 添加本条消息是否为当前会话本身发的标志
		// jsonObject.put("isSelf", openSession.equals(session));
		// // 发送JSON格式的消息
		// openSession.getAsyncRemote().sendText(jsonObject.toString());
		// }
	}

	public static synchronized int getOnlineCount() {
		return onlineCount;
	}

	public static synchronized void addOnlineCount() {
		ChatServer.onlineCount++;
	}

	public static synchronized void subOnlineCount() {
		ChatServer.onlineCount--;
	}

	public static synchronized Map<String, ChatServer> getClients() {
		return clients;
	}
}