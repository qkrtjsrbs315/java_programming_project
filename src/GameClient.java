// GameClient.java
import java.io.*;
import java.net.*;

public class GameClient {
    private Socket socket;
    private PrintWriter out;
    private BufferedReader in;
    private boolean isConnected = false;

    public GameClient(String host, int port) throws IOException {
        socket = new Socket(host, port);
        out = new PrintWriter(socket.getOutputStream(), true);
        in = new BufferedReader(new InputStreamReader(socket.getInputStream()));
        isConnected = true;
    }

    public void sendMessage(String message) {
        if (isConnected) {
            out.println(message);
        }
    }

    public String receiveMessage() throws IOException {
        return in.readLine();
    }

    public boolean isConnected() {
        return isConnected;
    }

    public void close() {
        try {
            isConnected = false;
            socket.close();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }
}
