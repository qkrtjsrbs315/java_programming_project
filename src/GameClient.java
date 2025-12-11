// GameClient.java
// 클라이언트(참가자) 역할을 수행하는 클래스
// 서버에 접속하고, 서버와 메시지를 주고받는 기능을 제공한다.

import java.io.*;
import java.net.*;

public class GameClient {
    private Socket socket;           // 서버와 연결되는 소켓
    private PrintWriter out;         // 서버로 메시지를 보내는 출력 스트림
    private BufferedReader in;       // 서버로부터 메시지를 읽는 입력 스트림
    private boolean isConnected = false; // 서버 연결 상태

    // --- 클라이언트 생성자 ---
    // host(IP), port(포트 번호)를 받아 서버에 접속을 시도한다.
    public GameClient(String host, int port) throws IOException {
        // 서버에 연결 시도 (연결 실패 시 IOException 발생)
        socket = new Socket(host, port);

        // 서버로 메시지를 보내는 PrintWriter (auto-flush = true)
        out = new PrintWriter(socket.getOutputStream(), true);

        // 서버로부터 메시지를 받는 BufferedReader
        in = new BufferedReader(new InputStreamReader(socket.getInputStream()));

        isConnected = true;   // 성공적으로 연결됨
    }

    // --- 서버로 메시지 전송 ---
    public void sendMessage(String message) {
        if (isConnected) {
            out.println(message);    // println → 문자열 + 개행 문자 전송
        }
    }

    // --- 서버의 메시지 수신 ---
    // 서버에서 한 줄(line) 단위로 메시지를 읽는다.
    public String receiveMessage() throws IOException {
        return in.readLine();        // 서버가 연결을 끊으면 null 반환
    }

    // --- 현재 연결 여부 확인 ---
    public boolean isConnected() {
        return isConnected;
    }

    // --- 연결 종료 ---
    // 소켓을 닫고 연결 상태를 false로 만든다.
    public void close() {
        try {
            isConnected = false;
            socket.close();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }
}
