// MultiplayerDodgePanelHost.java
import move.ViewPanel;
import move.CollidableObject;
import move.ItemObject;
import java.awt.*;
import java.awt.event.*;
import java.util.ArrayList;
import javax.swing.*;

public class MultiplayerDodgePanelHost extends ViewPanel {
    protected ArrayList<CollidableObject> bombs;
    protected ArrayList<ItemObject> items;
    protected int characterHP, width, height, score;
    protected String imageBomb;
    protected String[] itemImages;
    protected int[] itemScores = {10, 20, 30, 0};
    protected GameServer gameServer;
    protected int opponentHP = 5;
    protected String gameResult = "";
    protected boolean gameEnded = false;

    public MultiplayerDodgePanelHost(CollidableObject character, final String imageBomb, final String[] itemImages, GameServer gameServer) {
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
        setBackground(Color.white);
        setPreferredSize(new Dimension(width + CollidableObject.IMGSIZE, height));

        startNetworkListener();
    }

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

                        if (message.startsWith("HP:")) {
                            try {
                                opponentHP = Integer.parseInt(message.substring(3));
                                System.out.println("상대 HP: " + opponentHP);
                            } catch (NumberFormatException e) {
                                System.out.println("HP 파싱 오류: " + e.getMessage());
                            }
                        } else if (message.startsWith("GAME_END:")) {
                            try {
                                opponentHP = Integer.parseInt(message.substring(9));
                                System.out.println("클라이언트 최종 HP: " + opponentHP);
                                gameEnded = true;

                                // 결과 결정
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

    @Override
    protected void update() {
        if (gameEnded) return;

        // 폭탄 이동 및 생성
        for (int i = bombs.size() - 1; i >= 0; i--) {
            bombs.get(i).move();
        }
        if ((System.currentTimeMillis() % 8) == 0) {
            bombs.add(new CollidableObject(CollidableObject.STOP, CollidableObject.DOWN, imageBomb, width, height));
        }

        // 아이템 이동 및 생성
        for (int i = items.size() - 1; i >= 0; i--) {
            items.get(i).move();
        }
        if ((System.currentTimeMillis() % 20) == 0) {
            int type = (int)(Math.random() * 4);
            items.add(new ItemObject(type, itemImages[type], width, height));
        }

        // 캐릭터 이동
        character.move(character.directionX(), CollidableObject.STOP);

        // HP 및 아이템 충돌 처리
        updateHP();
        updateItems();

        // 자신의 HP 상태 전송
        gameServer.sendMessage("HP:" + characterHP);

        // 게임 종료 확인
        if (characterHP <= 0 && !gameEnded) {
            System.out.println("호스트 게임 종료! HP: " + characterHP);
            gameEnded = true;
            gameServer.sendMessage("GAME_END:" + characterHP);
            timer.stop();

            // 결과 결정
            if (opponentHP > 0) {
                gameResult = "YOU LOSE!";
            } else {
                gameResult = "DRAW!";
            }

            System.out.println("호스트 결과: " + gameResult);
        }
    }

    protected void updateHP() {
        for (int i = bombs.size() - 1; i >= 0; i--) {
            CollidableObject bomb = bombs.get(i);
            if (bomb.collide(character)) {
                characterHP--;
                bombs.remove(i);
                break;
            } else if (bomb.collide()) {
                bombs.remove(i);
                break;
            }
        }
    }

    protected void updateItems() {
        for (int i = items.size() - 1; i >= 0; i--) {
            ItemObject item = items.get(i);
            int type = item.getType();

            if (item.collide(character)) {
                if (type == 3) {
                    if (characterHP < 5) characterHP++;
                } else {
                    score += itemScores[type];
                }
                items.remove(i);
                break;
            } else if (item.collide()) {
                items.remove(i);
                break;
            }
        }
    }

    @Override
    public void paintComponent(Graphics g) {
        super.paintComponent(g);

        // 배경 채우기
        g.setColor(Color.white);
        g.fillRect(0, 0, getWidth(), getHeight());

        character.paint(g);
        for (CollidableObject bomb : bombs)
            bomb.paint(g);
        for (ItemObject item : items)
            item.paint(g);

        // 상단 정보 (본인 HP와 Score만)
        g.setColor(Color.black);
        g.setFont(new Font("Arial", Font.BOLD, 20));
        g.drawString("HP : " + hp(characterHP), 10, 30);
        g.drawString("Score : " + score, 10, 60);

        // 게임 종료 화면
        if (gameEnded && gameResult != null && !gameResult.isEmpty()) {
            System.out.println("paintComponent에서 게임 종료 화면 그리기: " + gameResult);
            g.setColor(new Color(0, 0, 0, 180));
            g.fillRect(0, 0, getWidth(), getHeight());
            g.setColor(Color.white);
            g.setFont(new Font("Arial", Font.BOLD, 60));
            FontMetrics fm = g.getFontMetrics();
            int x = (getWidth() - fm.stringWidth(gameResult)) / 2;
            int y = (getHeight() + fm.getAscent()) / 2 - 50;
            g.drawString(gameResult, x, y);

            g.setFont(new Font("Arial", Font.PLAIN, 20));
            g.drawString("Final Score: " + score, getWidth() / 2 - 80, getHeight() / 2 + 30);
        }
    }

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