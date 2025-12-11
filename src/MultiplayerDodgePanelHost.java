// 호스트(Player 1)의 게임 화면 패널
// 클라이언트(Player 2)와 HP/게임 종료 상태를 네트워크로 주고받으며 실시간 멀티플레이 동작을 수행한다.

import move.ViewPanel;
import move.CollidableObject;
import move.ItemObject;
import java.awt.*;
import java.awt.event.*;
import java.util.ArrayList;
import javax.swing.*;

public class MultiplayerDodgePanelHost extends ViewPanel {

    // 폭탄 및 아이템 객체 리스트
    protected ArrayList<CollidableObject> bombs;
    protected ArrayList<ItemObject> items;

    // 캐릭터 HP, 필드 크기, 스코어
    protected int characterHP, width, height, score;

    // 폭탄/아이템 이미지 경로
    protected String imageBomb;
    protected String[] itemImages;

    // 아이템 종류별 점수 (0:10, 1:20, 2:30, 3:회복)
    protected int[] itemScores = {10, 20, 30, 0};

    // 서버(호스트)의 네트워크 객체
    protected GameServer gameServer;

    // 상대 HP
    protected int opponentHP = 5;

    // 게임 종료 관련
    protected String gameResult = "";
    protected boolean gameEnded = false;

    // 생성자
    public MultiplayerDodgePanelHost(CollidableObject character, final String imageBomb,
                                     final String[] itemImages, GameServer gameServer) {
        super(character);

        this.characterHP = 5;
        this.bombs = new ArrayList<>();
        this.items = new ArrayList<>();
        this.score = 0;
        this.imageBomb = imageBomb;
        this.itemImages = itemImages;
        this.width = character.backgroundWidth();
        this.height = character.backgroundHeight();
        this.gameServer = gameServer;

        // 패널 설정
        setBackground(Color.white);
        setPreferredSize(new Dimension(width + CollidableObject.IMGSIZE, height));

        // 상대방 메시지 수신 스레드 시작
        startNetworkListener();
    }

    /**
     * 네트워크 수신 스레드
     * - 클라이언트 메시지를 지속적으로 받음
     * - HP, GAME_END 처리
     */
    private void startNetworkListener() {
        Thread listenerThread = new Thread(() -> {
            try {
                String message;
                while (!gameEnded) {
                    try {
                        message = gameServer.receiveMessage();

                        if (message == null) {
                            Thread.sleep(50);
                            continue;
                        }

                        System.out.println("호스트 수신: " + message);

                        // 클라이언트의 HP 업데이트 메시지
                        if (message.startsWith("HP:")) {
                            try {
                                opponentHP = Integer.parseInt(message.substring(3));
                                System.out.println("상대 HP: " + opponentHP);
                            } catch (NumberFormatException e) {
                                System.out.println("HP 파싱 오류: " + e.getMessage());
                            }
                        }

                        // 클라이언트 측에서 GAME_END 발생
                        else if (message.startsWith("GAME_END:")) {
                            try {
                                opponentHP = Integer.parseInt(message.substring(9));
                                System.out.println("클라이언트 최종 HP: " + opponentHP);

                                // 게임 종료 처리
                                gameEnded = true;

                                // 승패 판단
                                if (characterHP > 0 && opponentHP <= 0) {
                                    gameResult = "YOU WIN!";
                                } else if (characterHP <= 0 && opponentHP > 0) {
                                    gameResult = "YOU LOSE!";
                                } else {
                                    gameResult = "DRAW!";
                                }

                                System.out.println("호스트 결과: " + gameResult);
                                timer.stop();
                            } catch (NumberFormatException e) {
                                System.out.println("GAME_END 파싱 오류: " + e.getMessage());
                            }
                        }
                    } catch (Exception e) {
                        System.out.println("수신 중 오류: " + e.getMessage());
                        Thread.sleep(50);
                    }
                }
            } catch (Exception e) {
                System.out.println("호스트 리스너 에러: " + e.getMessage());
                e.printStackTrace();
            }
        });

        listenerThread.setDaemon(true);
        listenerThread.start();
    }

    /**
     * 게임 내부 업데이트 로직
     * - 폭탄 이동/생성
     * - 아이템 이동/생성
     * - 충돌 처리
     * - HP/게임 종료 정보 전송
     */
    @Override
    protected void update() {
        if (gameEnded) return;

        // ===== 폭탄 이동 =====
        for (int i = bombs.size() - 1; i >= 0; i--) {
            bombs.get(i).move();
        }

        // 폭탄 생성 (확률 랜덤)
        if ((System.currentTimeMillis() % 8) == 0) {
            bombs.add(new CollidableObject(CollidableObject.STOP, CollidableObject.DOWN,
                    imageBomb, width, height));
        }

        // ===== 아이템 이동 =====
        for (int i = items.size() - 1; i >= 0; i--) {
            items.get(i).move();
        }

        // 아이템 생성
        if ((System.currentTimeMillis() % 20) == 0) {
            int type = (int)(Math.random() * 4);
            items.add(new ItemObject(type, itemImages[type], width, height));
        }

        // ===== 캐릭터 이동 =====
        character.move(character.directionX(), CollidableObject.STOP);

        // ===== 충돌 처리 =====
        updateHP();      // 폭탄 충돌
        updateItems();   // 아이템 충돌

        // ===== 자신의 HP 네트워크 전송 =====
        gameServer.sendMessage("HP:" + characterHP);

        // ===== 게임 종료 처리 =====
        if (characterHP <= 0 && !gameEnded) {
            System.out.println("호스트 게임 종료! HP: " + characterHP);

            gameEnded = true;
            gameServer.sendMessage("GAME_END:" + characterHP);

            timer.stop();

            // 승패 계산
            if (opponentHP > 0) {
                gameResult = "YOU LOSE!";
            } else {
                gameResult = "DRAW!";
            }

            System.out.println("호스트 결과: " + gameResult);
        }
    }

    /** 폭탄 충돌 처리 */
    protected void updateHP() {
        for (int i = bombs.size() - 1; i >= 0; i--) {
            CollidableObject bomb = bombs.get(i);

            // 캐릭터와 충돌 → HP 감소
            if (bomb.collide(character)) {
                characterHP--;
                bombs.remove(i);
                break;
            }
            // 화면 아래 벗어나면 삭제
            else if (bomb.collide()) {
                bombs.remove(i);
                break;
            }
        }
    }

    /** 아이템 충돌 처리 */
    protected void updateItems() {
        for (int i = items.size() - 1; i >= 0; i--) {
            ItemObject item = items.get(i);
            int type = item.getType();

            // 캐릭터가 아이템을 먹음
            if (item.collide(character)) {
                if (type == 3) { // HP 회복 아이템
                    if (characterHP < 5) characterHP++;
                } else {
                    score += itemScores[type]; // 점수 아이템
                }
                items.remove(i);
                break;
            }
            // 화면 벗어나면 삭제
            else if (item.collide()) {
                items.remove(i);
                break;
            }
        }
    }

    /**
     * 화면 렌더링
     */
    @Override
    public void paintComponent(Graphics g) {
        super.paintComponent(g);

        // 흰 배경
        g.setColor(Color.white);
        g.fillRect(0, 0, getWidth(), getHeight());

        // 캐릭터 + 오브젝트들 그리기
        character.paint(g);
        for (CollidableObject bomb : bombs) bomb.paint(g);
        for (ItemObject item : items) item.paint(g);

        // ===== 상단 정보 표시 (HP + Score) =====
        g.setColor(Color.black);
        g.setFont(new Font("Arial", Font.BOLD, 20));
        g.drawString("HP : " + hp(characterHP), 10, 30);
        g.drawString("Score : " + score, 10, 60);

        // ===== 게임 종료 화면 =====
        if (gameEnded && gameResult != null && !gameResult.isEmpty()) {
            System.out.println("paintComponent에서 게임 종료 화면 그리기: " + gameResult);

            // 어두운 반투명 배경
            g.setColor(new Color(0, 0, 0, 180));
            g.fillRect(0, 0, getWidth(), getHeight());

            // 승/패 메시지
            g.setColor(Color.white);
            g.setFont(new Font("Arial", Font.BOLD, 60));
            FontMetrics fm = g.getFontMetrics();
            int x = (getWidth() - fm.stringWidth(gameResult)) / 2;
            int y = (getHeight() + fm.getAscent()) / 2 - 50;
            g.drawString(gameResult, x, y);

            // 최종 점수
            g.setFont(new Font("Arial", Font.PLAIN, 20));
            g.drawString("Final Score: " + score,
                    getWidth() / 2 - 80,
                    getHeight() / 2 + 30);
        }
    }

    /** HP를 동그라미(●/○)로 변환 */
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
