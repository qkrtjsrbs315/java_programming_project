// GameMain.java - 통합 메인 클래스
import move.CollidableObject;

import javax.swing.*;
import java.io.IOException;

public class GameMain {
    public static void main(String[] args) {
        SwingUtilities.invokeLater(() -> {
            try {
                // 역할 선택 다이얼로그
                String[] options = {"호스트로 시작 (서버)", "게임 참가 (클라이언트)", "종료"};
                int choice = JOptionPane.showOptionDialog(
                        null,
                        "역할을 선택하세요:",
                        "폭탄 피하기 - 1v1 멀티플레이",
                        JOptionPane.YES_NO_CANCEL_OPTION,
                        JOptionPane.QUESTION_MESSAGE,
                        null,
                        options,
                        options[0]
                );

                if (choice == 2 || choice == -1) { // 종료
                    System.exit(0);
                }

                final String imagePath = "C:\\Users\\insilicox_dev\\IdeaProjects\\javagame2\\src\\move\\image\\";

                // 이미지 경로 설정
                String imageCharacter = imagePath + "character.png";
                String imageBomb = imagePath + "bomb.png";
                String[] itemImages = {
                        imagePath + "item1.png",
                        imagePath + "item2.png",
                        imagePath + "item3.png",
                        imagePath + "item4.png"
                };

                if (choice == 0) { // 호스트(서버)
                    startAsHost(imageCharacter, imageBomb, itemImages);
                } else if (choice == 1) { // 클라이언트
                    joinAsClient(imageCharacter, imageBomb, itemImages);
                }
            } catch (Exception e) {
                JOptionPane.showMessageDialog(null, "오류 발생: " + e.getMessage(), "오류", JOptionPane.ERROR_MESSAGE);
                e.printStackTrace();
            }
        });
    }

    private static void startAsHost(String imageCharacter, String imageBomb, String[] itemImages) {
        // 포트 입력
        String portStr = JOptionPane.showInputDialog(
                null,
                "게임 포트를 입력하세요",
                "5000"
        );

        int port = 5000;
        if (portStr != null && !portStr.trim().isEmpty()) {
            try {
                port = Integer.parseInt(portStr.trim());
            } catch (NumberFormatException e) {
                JOptionPane.showMessageDialog(null, "포트 번호가 유효하지 않습니다. 기본값(5000)을 사용합니다.", "알림", JOptionPane.WARNING_MESSAGE);
                port = 5000;
            }
        }

        final int finalPort = port;

        // 로딩 창 표시
        JFrame loadingFrame = new JFrame("호스트 준비 중...");
        loadingFrame.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        JLabel label = new JLabel("<html>게임 호스트가 준비되었습니다.<br/>상대방이 참가할 때까지 기다리는 중입니다.<br/><br/>포트: " + port + "</html>");
        label.setHorizontalAlignment(SwingConstants.CENTER);
        loadingFrame.add(label);
        loadingFrame.setSize(400, 200);
        loadingFrame.setLocationRelativeTo(null);
        loadingFrame.setVisible(true);

        // 서버를 별도 스레드에서 시작
        Thread serverThread = new Thread(() -> {
            try {
                System.out.println("호스트 모드 시작...");
                GameServer server = new GameServer(finalPort);
                System.out.println("클라이언트 대기 중...");
                server.waitForClient();
                System.out.println("클라이언트 연결됨! 게임 시작!");

                SwingUtilities.invokeLater(() -> {
                    loadingFrame.dispose();
                    try {
                        System.out.println("호스트 게임 UI 시작");
                        CollidableObject character = new CollidableObject(imageCharacter, 150, 300, 300, 400);
                        MultiplayerDodgePanelHost panel = new MultiplayerDodgePanelHost(character, imageBomb, itemImages, server);
                        new MultiplayerGameFrame("폭탄 피하기 - 호스트", panel);
                    } catch (Exception e) {
                        e.printStackTrace();
                    }
                });
            } catch (IOException e) {
                System.out.println("호스트 에러: " + e.getMessage());
                e.printStackTrace();
            }
        });
        serverThread.setDaemon(true);
        serverThread.start();
    }

    private static void joinAsClient(String imageCharacter, String imageBomb, String[] itemImages) {
        // 호스트 IP 입력 다이얼로그
        String hostIP = JOptionPane.showInputDialog(
                null,
                "호스트 IP를 입력하세요\n(같은 컴퓨터: localhost 또는 127.0.0.1)",
                "localhost"
        );

        if (hostIP == null || hostIP.trim().isEmpty()) {
            hostIP = "localhost";
        }

        // 호스트 포트 입력 다이얼로그
        String portStr = JOptionPane.showInputDialog(
                null,
                "호스트 포트를 입력하세요",
                "5000"
        );

        int port = 5000;
        if (portStr != null && !portStr.trim().isEmpty()) {
            try {
                port = Integer.parseInt(portStr.trim());
            } catch (NumberFormatException e) {
                JOptionPane.showMessageDialog(null, "포트 번호가 유효하지 않습니다. 기본값(5000)을 사용합니다.", "알림", JOptionPane.WARNING_MESSAGE);
                port = 5000;
            }
        }

        // 연결 시도
        JFrame loadingFrame = new JFrame("게임 참가 중...");
        loadingFrame.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        JLabel label = new JLabel("호스트에 연결 중... (" + hostIP + ":" + port + ")");
        label.setHorizontalAlignment(SwingConstants.CENTER);
        loadingFrame.add(label);
        loadingFrame.setSize(400, 150);
        loadingFrame.setLocationRelativeTo(null);
        loadingFrame.setVisible(true);

        String finalHostIP = hostIP;
        int finalPort = port;
        Thread clientThread = new Thread(() -> {
            try {
                System.out.println("호스트 연결 시도: " + finalHostIP + ":" + finalPort);
                GameClient client = new GameClient(finalHostIP, finalPort);
                System.out.println("호스트에 연결됨!");

                // 게임 시작 신호 대기
                System.out.println("START 신호 대기 중...");
                String signal = client.receiveMessage();
                System.out.println("받은 신호: " + signal);

                if ("START".equals(signal)) {
                    System.out.println("게임 시작!");
                    SwingUtilities.invokeLater(() -> {
                        loadingFrame.dispose();
                        try {
                            CollidableObject character = new CollidableObject(imageCharacter, 150, 300, 300, 400);
                            MultiplayerDodgePanelClient panel = new MultiplayerDodgePanelClient(character, imageBomb, itemImages, client);
                            new MultiplayerGameFrame("폭탄 피하기 - 클라이언트", panel);
                        } catch (Exception e) {
                            e.printStackTrace();
                        }
                    });
                }
            } catch (IOException e) {
                System.out.println("연결 실패: " + e.getMessage());
                e.printStackTrace();
                SwingUtilities.invokeLater(() -> {
                    loadingFrame.dispose();
                    JOptionPane.showMessageDialog(null,
                            "호스트 연결 실패: " + e.getMessage() + "\n\n호스트가 실행 중인지 확인하세요.",
                            "연결 오류",
                            JOptionPane.ERROR_MESSAGE);
                });
            }
        });
        clientThread.setDaemon(true);
        clientThread.start();
    }
}