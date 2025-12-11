// MultiplayerDodgePanelClient.java
// 클라이언트 쪽 게임 화면 패널
// 폭탄 이동/아이템 생성/충돌 처리 + 서버와 HP 교환 + 게임 종료 판단을 수행한다.

import move.ViewPanel;
import move.CollidableObject;
import move.ItemObject;
import java.awt.*;
import java.util.ArrayList;
import java.io.IOException;
import javax.swing.*;

public class MultiplayerDodgePanelClient extends ViewPanel {

    // --- 게임 요소 목록 ---
    protected ArrayList<CollidableObject> bombs;   // 떨어지는 폭탄들
    protected ArrayList<ItemObject> items;         // 아이템들

    // --- 게임 상태 변수 ---
    protected int characterHP, width, height, score;
    protected String imageBomb;
    protected String[] itemImages;
    protected int[] itemScores = {10, 20, 30, 0};  // 아이템 점수: (빨, 파, 초록, 회복)
    protected GameClient gameClient;               // 서버와 연결된 클라이언트 객체
    protected int opponentHP = 5;                  // 서버 플레이어(호스트)의 HP
    protected String gameResult = "";              // “YOU WIN/LOSE/DRAW”
    protected boolean gameEnded = false;           // 게임 종료 여부

    // --- 생성자 ---
    public MultiplayerDodgePanelClient(
            CollidableObject character,
            final String imageBomb,
            final String[] itemImages,
            GameClient gameClient
    ) {
        super(character);

        this.characterHP = 5;
        this.bombs = new ArrayList<>();
        this.items = new ArrayList<>();
        this.score = 0;
        this.imageBomb = imageBomb;
        this.itemImages = itemImages;
        this.gameClient = gameClient;

        // 배경 크기 (캐릭터 생성 시 설정한 배경 크기 사용)
        this.width = character.backgroundWidth();
        this.height = character.backgroundHeight();

        setBackground(Color.white);
        setPreferredSize(new Dimension(width + CollidableObject.IMGSIZE, height));

        // 서버에서 오는 메시지를 지속적으로 듣기 시작
        startNetworkListener();
    }

    // ----------------------------
    // 서버 메시지 수신 스레드
    // ----------------------------
    private void startNetworkListener() {
        Thread listenerThread = new Thread(() -> {
            while (!gameEnded) {
                try {
                    // 서버로부터 메시지 한 줄 읽기
                    String message = gameClient.receiveMessage();
                    if (message == null) {
                        Thread.sleep(50);
                        continue;
                    }

                    System.out.println("클라이언트 수신: " + message);

                    // --- 상대 HP 갱신 ---
                    if (message.startsWith("HP:")) {
                        try {
                            opponentHP = Integer.parseInt(message.substring(3).trim());
                            System.out.println("상대 HP: " + opponentHP);
                        } catch (NumberFormatException e) {
                            System.out.println("HP 파싱 오류: " + e.getMessage());
                        }
                    }

                    // --- 게임 종료 신호 수신 ---
                    else if (message.startsWith("GAME_END:")) {
                        try {
                            opponentHP = Integer.parseInt(message.substring(9).trim());
                            System.out.println("호스트 최종 HP: " + opponentHP);

                            gameEnded = true;

                            // 최종 결과 판정
                            if (characterHP > 0 && opponentHP <= 0) gameResult = "YOU WIN!";
                            else if (characterHP <= 0 && opponentHP > 0) gameResult = "YOU LOSE!";
                            else gameResult = "DRAW!";

                            timer.stop(); // 게임 루프 정지
                        } catch (Exception e) {
                            System.out.println("GAME_END 파싱 오류: " + e.getMessage());
                        }
                    }

                } catch (Exception e) {
                    System.out.println("클라이언트 리스너 예외: " + e.getMessage());
                    try { Thread.sleep(100); } catch (InterruptedException ignored) {}
                }
            }
        });

        listenerThread.setDaemon(true);
        listenerThread.start();
    }

    // ----------------------------
    // 게임 루프: 한 프레임마다 호출됨
    // ----------------------------
    @Override
    protected void update() {
        if (gameEnded) return;

        // --- 폭탄 이동 ---
        for (int i = bombs.size() - 1; i >= 0; i--)
            bombs.get(i).move();

        // 폭탄 생성 (주기적)
        if ((System.currentTimeMillis() % 8) == 0)
            bombs.add(new CollidableObject(CollidableObject.STOP, CollidableObject.DOWN, imageBomb, width, height));

        // --- 아이템 이동 ---
        for (int i = items.size() - 1; i >= 0; i--)
            items.get(i).move();

        // 아이템 생성 (랜덤)
        if ((System.currentTimeMillis() % 20) == 0) {
            int type = (int)(Math.random() * 4);
            items.add(new ItemObject(type, itemImages[type], width, height));
        }

        // --- 캐릭터 이동 ---
        character.move(character.directionX(), CollidableObject.STOP);

        // --- 충돌 처리 ---
        updateHP();
        updateItems();

        // --- 자신의 HP 서버로 전송 ---
        gameClient.sendMessage("HP:" + characterHP);

        // --- 자신이 죽었는지 체크 ---
        if (characterHP <= 0 && !gameEnded) {
            System.out.println("클라이언트 게임 종료! HP: " + characterHP);

            gameEnded = true;

            // 서버에게 종료 알림
            gameClient.sendMessage("GAME_END:" + characterHP);
            timer.stop();

            // 결과 결정
            if (opponentHP > 0) gameResult = "YOU LOSE!";
            else gameResult = "DRAW!";
        }
    }

    // ----------------------------
    // 폭탄 충돌 → HP 감소
    // ----------------------------
    protected void updateHP() {
        for (int i = bombs.size() - 1; i >= 0; i--) {
            CollidableObject bomb = bombs.get(i);

            // 캐릭터와 충돌하면 HP 감소
            if (bomb.collide(character)) {
                characterHP--;
                bombs.remove(i);
                break;
            }

            // 화면 밖으로 나갔으면 제거
            else if (bomb.collide()) {
                bombs.remove(i);
                break;
            }
        }
    }

    // ----------------------------
    // 아이템 획득 → 점수 증가 또는 HP 회복
    // ----------------------------
    protected void updateItems() {
        for (int i = items.size() - 1; i >= 0; i--) {
            ItemObject item = items.get(i);
            int type = item.getType();

            if (item.collide(character)) {

                // 3번 아이템 → HP 회복
                if (type == 3) {
                    if (characterHP < 5) characterHP++;
                }
                // 나머지 → 점수 증가
                else {
                    score += itemScores[type];
                }

                items.remove(i);
                break;
            }

            // 화면 밖으로 나가면 제거
            else if (item.collide()) {
                items.remove(i);
                break;
            }
        }
    }

    // ----------------------------
    // 화면 그리기
    // ----------------------------
    @Override
    public void paintComponent(Graphics g) {
        super.paintComponent(g);

        // 배경
        g.setColor(Color.white);
        g.fillRect(0, 0, getWidth(), getHeight());

        // 캐릭터, 폭탄, 아이템 그리기
        character.paint(g);
        for (CollidableObject bomb : bombs) bomb.paint(g);
        for (ItemObject item : items) item.paint(g);

        // 상단 정보 → HP, 점수
        g.setColor(Color.black);
        g.setFont(new Font("Arial", Font.BOLD, 20));
        g.drawString("HP : " + hp(characterHP), 10, 30);
        g.drawString("Score : " + score, 10, 60);

        // ----------------------------
        // 게임 종료 시 결과 화면 오버레이
        // ----------------------------
        if (gameEnded && gameResult != null && !gameResult.isEmpty()) {
            g.setColor(new Color(0, 0, 0, 180));
            g.fillRect(0, 0, getWidth(), getHeight());

            // 결과 문구
            g.setColor(Color.white);
            g.setFont(new Font("Arial", Font.BOLD, 60));

            FontMetrics fm = g.getFontMetrics();
            int x = (getWidth() - fm.stringWidth(gameResult)) / 2;
            int y = (getHeight() + fm.getAscent()) / 2 - 50;
            g.drawString(gameResult, x, y);

            // 점수 출력
            g.setFont(new Font("Arial", Font.PLAIN, 20));
            g.drawString("Final Score: " + score, getWidth() / 2 - 80, getHeight() / 2 + 30);
        }
    }

    // HP를 ●○ 모양 문자열로 변환하여 UI 표시
    private String hp(int hp) {
        switch(hp) {
            case 5: return "●●●●●";
            case 4: return "●●●●○";
            case 3: return "●●●○○";
            case 2: return "●●○○○";
            case 1: return "●○○○○";
            default: return "○○○○○";
        }
    }
}
