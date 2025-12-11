package move;

import java.awt.*;
import java.awt.event.*;
import javax.swing.*;

/**
 * ObjectByKey
 * - 방향키 또는 WASD 입력으로 상하좌우로 이동할 수 있는 객체
 * - KeyAdapter를 상속하여 키 입력(press/release)을 직접 처리
 * - 이동 범위를(minX~maxX, minY~maxY) 지정하여 벗어나지 않도록 제한
 */
public class ObjectByKey extends KeyAdapter {

    // 출력할 이미지
    private Image image;

    // 현재 위치 (x, y)
    protected int x, y;

    // 이동 방향 (X축, Y축)
    // LEFT = -1, RIGHT = 1, UP = -1, DOWN = 1, STOP = 0
    protected int directionX, directionY;

    // 이동 가능한 최소/최대 좌표(충돌 또는 화면 벗어남 방지)
    protected int minX, minY, maxX, maxY;

    // 상수 정의
    public static final int LEFT = -1, RIGHT = 1;
    public static final int UP = -1, DOWN = 1;
    public static final int STOP = 0;
    public static final int IMGSIZE = 40; // 이미지 출력 크기

    /**
     * 생성자
     * @param image  캐릭터 이미지 파일명
     * @param x      시작 위치 x
     * @param y      시작 위치 y
     * @param minX   이동 가능한 최소 x
     * @param minY   이동 가능한 최소 y
     * @param maxX   이동 가능한 최대 x
     * @param maxY   이동 가능한 최대 y
     */
    public ObjectByKey(String image, int x, int y, int minX, int minY, int maxX, int maxY) {
        this.image = new ImageIcon(image).getImage();
        this.x = x;
        this.y = y;
        this.minX = minX;
        this.minY = minY;
        this.maxX = maxX;
        this.maxY = maxY;
        this.directionX = STOP;
        this.directionY = STOP;
    }

    /**
     * 방향키 또는 WASD 키를 눌렀을 때 이동 방향 설정
     */
    @Override
    public void keyPressed(KeyEvent event) {
        switch(event.getKeyCode()) {

            // ESC → 프로그램 종료
            case KeyEvent.VK_ESCAPE:
                System.exit(0);
                break;

            // 왼쪽 이동
            case KeyEvent.VK_LEFT:
            case 'A': case 'a':
                directionX = LEFT;
                break;

            // 오른쪽 이동
            case KeyEvent.VK_RIGHT:
            case 'D': case 'd':
                directionX = RIGHT;
                break;

            // 위 이동
            case KeyEvent.VK_UP:
            case 'W': case 'w':
                directionY = UP;
                break;

            // 아래 이동
            case KeyEvent.VK_DOWN:
            case 'S': case 's':
                directionY = DOWN;
                break;
        }
    }

    /**
     * 키를 떼면 이동을 멈춤
     * - 좌우 키를 떼면 directionX 초기화
     * - 상하 키를 떼면 directionY 초기화
     */
    @Override
    public void keyReleased(KeyEvent event) {
        switch(event.getKeyCode()) {

            // 좌우 해제 → X 방향 STOP
            case KeyEvent.VK_LEFT: case 'A': case 'a':
            case KeyEvent.VK_RIGHT: case 'D': case 'd':
                directionX = STOP;
                break;

            // 상하 해제 → Y 방향 STOP
            case KeyEvent.VK_UP: case 'W': case 'w':
            case KeyEvent.VK_DOWN: case 'S': case 's':
                directionY = STOP;
                break;
        }
    }

    /**
     * 지정된 방향X, 방향Y로 이동 요청
     * (기본 move()는 현재 directionX(), directionY()값을 이용)
     */
    public void move() {
        move(directionX, directionY);
    }

    /**
     * 이동 처리
     * - x += directionX
     * - y += directionY
     * - 이동 후 범위(minX~maxX, minY~maxY) 안에 위치하도록 조정(벽 충돌 등)
     */
    public void move(int directionX, int directionY) {
        this.x += directionX;
        this.y += directionY;

        // X축 범위 제한
        this.x = (this.x <= minX) ? minX : this.x;
        this.x = (this.x >= maxX) ? maxX : this.x;

        // Y축 범위 제한
        this.y = (this.y <= minY) ? minY : this.y;
        this.y = (this.y >= maxY) ? maxY : this.y;
    }

    /**
     * 현재 위치에 이미지 출력
     */
    public void paint(Graphics g) {
        g.drawImage(image, x, y, IMGSIZE, IMGSIZE, null);
    }

    /**
     * 현재 X축 이동 방향 반환
     */
    public int directionX() {
        return directionX;
    }

    /**
     * 현재 Y축 이동 방향 반환
     */
    public int directionY() {
        return directionY;
    }

    /**
     * 배경(맵) 가로 크기 반환
     */
    public int backgroundWidth() {
        return maxX;
    }

    /**
     * 배경(맵) 세로 크기 반환
     */
    public int backgroundHeight() {
        return maxY;
    }
}
