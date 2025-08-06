package edu.duke.ece651.textclient;

import java.io.File;
import java.lang.reflect.Type;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.Scanner;
import java.util.concurrent.TimeUnit;

import org.springframework.core.ParameterizedTypeReference;
import org.springframework.messaging.converter.MappingJackson2MessageConverter;
import org.springframework.messaging.simp.stomp.StompFrameHandler;
import org.springframework.messaging.simp.stomp.StompHeaders;
import org.springframework.messaging.simp.stomp.StompSession;
import org.springframework.messaging.simp.stomp.StompSessionHandlerAdapter;
import org.springframework.scheduling.concurrent.ThreadPoolTaskScheduler;
import org.springframework.web.socket.client.standard.StandardWebSocketClient;
import org.springframework.web.socket.messaging.WebSocketStompClient;
import org.springframework.web.socket.sockjs.client.SockJsClient;
import org.springframework.web.socket.sockjs.client.Transport;
import org.springframework.web.socket.sockjs.client.WebSocketTransport;

import com.fasterxml.jackson.databind.ObjectMapper;

public class TextClient {

    public static void main(String[] args) throws Exception {
        try (Scanner in = new Scanner(System.in)) {
            System.out.print("Session ID: ");
            int sessionId = Integer.parseInt(in.nextLine().trim());

            ThreadPoolTaskScheduler scheduler = new ThreadPoolTaskScheduler();
            scheduler.setPoolSize(10);
            scheduler.afterPropertiesSet();

            List<Transport> transports = new ArrayList<>();
            transports.add(new WebSocketTransport(new StandardWebSocketClient()));
            SockJsClient sockJsClient = new SockJsClient(transports);

            WebSocketStompClient stompClient = new WebSocketStompClient(sockJsClient);
            stompClient.setMessageConverter(new MappingJackson2MessageConverter());
            stompClient.setTaskScheduler(scheduler);

            System.out.println("Connecting to http://localhost:8081/api/ws-command …");
            StompSession session = stompClient
                    .connect("http://localhost:8081/api/ws-command", new StompSessionHandlerAdapter() {
                    })
                    .get(5, TimeUnit.SECONDS);
            System.out.println("✔ Connected");

            subscribe(session, "/topic/textual/command-result");
            subscribe(session, "/topic/textual/newBuilding-result");
            subscribe(session, "/topic/textual/loadCommand-result");
            subscribe(session, "/topic/textual/loadSession-result");
            subscribe(session, "/topic/textual/newSession-result");

            while (true) {
                System.out.print("> ");
                String line = in.nextLine().trim();
                if (line.equalsIgnoreCase("quit") || line.equalsIgnoreCase("exit")) {
                    break;
                }

                if (line.startsWith("newSession")) {
                    session.send("/app/textual/newSession", sessionId);
                } else if (line.startsWith("newBuilding")) {
                    String[] parts = line.split(" ", 2);
                    if (parts.length < 2) {
                        System.out.println("Usage: newBuilding <filename>");
                        continue;
                    }
                    session.send("/app/textual/newBuilding", Map.of(
                            "id", sessionId,
                            "fileName", parts[1]));
                } else if (line.startsWith("load")) {
                    String[] parts = line.split(" ", 2);
                    if (parts.length < 2) {
                        System.out.println("Usage: load <filename>");
                        continue;
                    }
                    String filename = parts[1];
                    // String filename =
                    // "/Users/ofosuosei/Desktop/Duke/SE/simulationserver/app/src/main/resources/doors1.json";
                    String filePath = filename;
                    System.out.println("Working Directory: " + System.getProperty("user.dir"));
                    System.out.println("Loading file: " + filePath);
                    try {
                        File file = new File(filePath);
                        if (!file.exists()) {
                            System.err.println("File not found: " + filePath);
                            continue;
                        }

                        ObjectMapper mapper = new ObjectMapper();
                        Map<String, Object> jsonData = mapper.readValue(file, Map.class);

                        session.send("/app/loadCommand", Map.of(
                                "id", sessionId,
                                "jsonData", jsonData));
                    } catch (Exception e) {
                        System.err.println("Failed to read or parse " + filePath + ": " + e.getMessage());
                    }
                } else if (line.startsWith("loadSession")) {
                    session.send("/app/textual/loadSession", sessionId);
                } else {
                    session.send("/app/textual/command", Map.of(
                            "id", sessionId,
                            "command", line));
                }
            }

            // Clean up
            System.out.println("Disconnecting…");
            session.disconnect();
            stompClient.stop();
            scheduler.shutdown();
        }
    }

    private static void subscribe(StompSession session, String topic) {
        session.subscribe(topic, new StompFrameHandler() {
            @Override
            public Type getPayloadType(StompHeaders headers) {
                return new ParameterizedTypeReference<Map<String, Object>>() {
                }.getType();
            }

            @Override
            @SuppressWarnings("unchecked")
            public void handleFrame(StompHeaders headers, Object payload) {
                Map<String, Object> m = (Map<String, Object>) payload;
                String status = (String) m.get("status");
                if ("ok".equals(status)) {
                    Object out = m.get("output");
                    if (out instanceof Iterable<?>) {
                        for (Object line : (Iterable<?>) out) {
                            System.out.println(line);
                        }
                    } else {
                        System.out.println(out);
                    }
                } else {
                    Object msg = m.get("message") != null ? m.get("message") : m.get("error");
                    System.err.println(msg);
                }
            }
        });
    }

}