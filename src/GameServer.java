// GameServer.java
// 호스트(서버) 역할을 담당하는 클래스
// 클라이언트 연결을 기다리고, 메시지를 주고받는 기능을 제공한다.

import java.io.*;
import java.net.*;

public class GameServer {
    private ServerSocket serverSocket;       // 클라이언트 연결을 기다리는 서버 소켓
    private Socket clientSocket;             // 연결된 클라이언트 소켓
    private PrintWriter clientOut;           // 클라이언트에게 메시지를 보내는 출력 스트림
    private BufferedReader clientIn;         // 클라이언트에게서 메시지를 받는 입력 스트림
    private int port;                        // 서버가 받을 포트 번호
    private volatile boolean connected = false; // 클라이언트 연결 여부(멀티스레드 안전)

    // --- 서버 생성자 ---
    public GameServer(int port) throws IOException {
        this.port = port;
        serverSocket = new ServerSocket(port);   // 서버 소켓을 포트에 바인딩
        System.out.println("게임 서버 시작. 포트: " + port);
    }

    // --- 클라이언트 접속 대기 ---
    public boolean waitForClient() throws IOException {
        System.out.println("클라이언트 대기중...");

        // accept()는 클라이언트가 접속할 때까지 블로킹된다.
        clientSocket = serverSocket.accept();

        // 클라이언트로 데이터를 보내는 스트림
        clientOut = new PrintWriter(clientSocket.getOutputStream(), true);

        // 클라이언트로부터 데이터를 받는 스트림
        clientIn = new BufferedReader(new InputStreamReader(clientSocket.getInputStream()));

        System.out.println("클라이언트 연결됨!");
        connected = true;

        // --- 게임 시작 신호 전송 ---
        // 클라이언트는 이 START 메시지를 받으면 게임 화면으로 전환하게 된다.
        clientOut.println("START");

        return true;
    }

    // --- 클라이언트에게 메시지 전송 ---
    public void sendMessage(String message) {
        if (connected && clientOut != null) {
            clientOut.println(message);  // println 사용: 자동으로 줄바꿈(\n)이 포함됨
        }
    }

    // --- 클라이언트로부터 메시지 수신 ---
    public String receiveMessage() throws IOException {
        if (connected && clientIn != null) {
            try {
                return clientIn.readLine();  // 클라이언트 메시지를 한 줄 단위로 읽기
            } catch (IOException e) {
                // 연결이 끊기면 connected false로 설정
                connected = false;
                throw e;
            }
        }
        return null;
    }

    // --- 현재 연결 상태 반환 ---
    public boolean isConnected() {
        return connected;
    }

    // --- 서버 종료 및 리소스 정리 ---
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
