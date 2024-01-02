package org.telegram.helperbot.service;

import org.springframework.stereotype.Service;

import java.io.IOException;
import java.net.InetSocketAddress;
import java.net.Socket;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.TimeUnit;

@Service
public class PortScanService {
    private static final int MIN_PORT_NUMBER = 0;
    private static final int MAX_PORT_NUMBER = 65535;
    private static final int TIMEOUT = 100;
    private static final int THREADS = 100;

    public List<Integer> scan(String host) {
        ExecutorService executorService = Executors.newFixedThreadPool(THREADS);
        List<Integer> openedPorts = new ArrayList<>();
        for (int i = MIN_PORT_NUMBER; i <= MAX_PORT_NUMBER; i++) {
            final int port = i;
            executorService.execute(() -> {
                InetSocketAddress inetSocketAddress = new InetSocketAddress(host, port);
                try (Socket socket = new Socket()) {
                    socket.connect(inetSocketAddress, TIMEOUT);
                    openedPorts.add(port);
                } catch (IOException ignored) {
                    //System.err.println(ignored.getMessage());
                }
            });
        }
        executorService.shutdown();
        try {
            executorService.awaitTermination(1, TimeUnit.MINUTES);
        } catch (InterruptedException exception) {
            throw new RuntimeException(exception);
        }
        return openedPorts;
    }

    public String getInfo(List<Integer> ports) {
        StringBuilder stringBuilder = new StringBuilder();
        for (Integer port : ports) {
            stringBuilder.append(port).append("\n");
        }
        return stringBuilder.toString();
    }
}
