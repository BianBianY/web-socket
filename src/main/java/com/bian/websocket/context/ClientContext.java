package com.bian.websocket.context;

import com.bian.websocket.entity.Client;
import com.bian.websocket.util.SpringContextUtil;
import com.bian.websocket.util.TasksUtil;
import lombok.extern.log4j.Log4j2;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.Future;

@Log4j2
public class ClientContext {
    private final static ClientContext INSTANCE = new ClientContext();
    private final Map<String, Client> clients = new ConcurrentHashMap<>();

    private ClientContext() {
    }

    public static ClientContext getClientContext() {
        return INSTANCE;
    }

    public int getOnlineSize() {
        return clients.size();
    }

    public void addClient(Client client) {
        if (!client.isAlive()) {
            throw new RuntimeException("client is not alive");
        }
        clients.put(client.getId(), client);
    }

    public void removeAndKillClient(String id) {
        Client client = clients.remove(id);
        if (client != null) {
            client.killClient();
        }
    }

    public boolean sendMessageByClientId(String id, String message) throws IOException {
        Client client = clients.get(id);
        if (client == null) {
            throw new RuntimeException("id is null");
        }
        return client.sendMessage(message);
    }

    public List<Future<Boolean>> sendMessageForAll(String message) {
        List<Future<Boolean>> results = new ArrayList<>();
        TasksUtil task = SpringContextUtil.getBeanByClass(TasksUtil.class);

        for (Client client : clients.values()) {
            Future<Boolean> future = task.doTask(() -> {
                try {
                    return client.sendMessage(message);
                } catch (IOException e) {
                    throw new RuntimeException(e);
                }
            });
            results.add(future);
        }
        return results;
    }

    public void refreshHeartbeatTime(String clientId) {
        Client client = clients.get(clientId);
        if (client != null) {
            client.refreshHeartbeatTime();
        }
    }
}
