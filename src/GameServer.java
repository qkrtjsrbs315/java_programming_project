// GameServer.java
import java.io.*;
import java.net.*;

public class GameServer {
    private ServerSocket serverSocket;
    private Socket clientSocket;
    private PrintWriter clientOut;
    private BufferedReader clientIn;
    private int port;
    private volatile boolean connected = false;

    public GameServer(int port) throws IOException {
        this.port = port;
        serverSocket = new ServerSocket(port);
        System.out.println("게임 서버 시작. 포트: " + port);
    }

    public boolean waitForClient() throws IOException {
        System.out.println("클라이언트 대기중...");
        clientSocket = serverSocket.accept();
        clientOut = new PrintWriter(clientSocket.getOutputStream(), true);
        clientIn = new BufferedReader(new InputStreamReader(clientSocket.getInputStream()));
        System.out.println("클라이언트 연결됨!");
        connected = true;

        // 게임 시작 신호
        clientOut.println("START");
        return true;
    }

    public void sendMessage(String message) {
        if (connected && clientOut != null) {
            clientOut.println(message);
        }
    }

    public String receiveMessage() throws IOException {
        if (connected && clientIn != null) {
            try {
                return clientIn.readLine();
            } catch (IOException e) {
                connected = false;
                throw e;
            }
        }
        return null;
    }

    public boolean isConnected() {
        return connected;
    }

    public void close() {
        try {
            connected = false;
            if (clientSocket != null) clientSocket.close();
            if (serverSocket != null) serverSocket.close();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }
}