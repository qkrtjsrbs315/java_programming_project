package move;

import java.awt.*;
import java.util.ArrayList;

// 폭탄 피하기 게임의 메인 패널
public class DodgePanel extends ViewPanel {

    // 떨어지는 폭탄 객체 리스트
    protected ArrayList<CollidableObject> bombs;

    // 떨어지는 아이템 객체 리스트
    protected ArrayList<ItemObject> items;

    // 캐릭터 HP, 화면 크기, 점수
    protected int characterHP, width, height, score;

    // 폭탄 이미지 파일명
    protected String imageBomb;

    // 아이템 이미지 목록
    protected String[] itemImages;

    // 아이템 점수 (item0=10, item1=20, item2=30, item3=HP 회복)
    protected int[] itemScores = {10, 20, 30, 0};

    // 생성자
    public DodgePanel(CollidableObject character, final String imageBomb, final String[] itemImages) {
        super(character);

        this.characterHP = 3;                 // 캐릭터 시작 체력
        this.bombs = new ArrayList<>();       // 폭탄 리스트 초기화
        this.items = new ArrayList<>();       // 아이템 리스트 초기화
        this.score = 0;                       // 초기 점수
        this.imageBomb = imageBomb;           // 폭탄 이미지 파일명
        this.itemImages = itemImages;         // 아이템 이미지 배열

        this.width = character.backgroundWidth();   // 배경 폭
        this.height = character.backgroundHeight(); // 배경 높이

        setBackground(Color.white);
        setPreferredSize(new Dimension(width + CollidableObject.IMGSIZE, height));
    }

    // 게임 로직 업데이트 (매 프레임 호출)
    @Override
    protected void update() {

        // ---------------------------
        // 1. 폭탄 이동 및 생성
        // ---------------------------
        for (CollidableObject bomb : bombs)
            bomb.move();

        // 일정한 간격(시간 기반)으로 폭탄 생성
        if ((System.currentTimeMillis() % 8) == 0) {
            // STOP, DOWN 방향으로 폭탄을 떨어뜨림
            bombs.add(new CollidableObject(CollidableObject.STOP, CollidableObject.DOWN,
                    imageBomb, width, height));
        }

        // ---------------------------
        // 2. 아이템 이동 및 생성
        // ---------------------------
        for (ItemObject item : items)
            item.move();

        // 일정 시간 간격으로 아이템 생성
        if ((System.currentTimeMillis() % 20) == 0) {
            int type = (int)(Math.random() * 4); // 0~3 랜덤 아이템 생성
            items.add(new ItemObject(type, itemImages[type], width, height));
        }

        // ---------------------------
        // 3. 캐릭터 이동 (좌우 이동만 가능)
        // ---------------------------
        character.move(character.directionX(), CollidableObject.STOP);

        // ---------------------------
        // 4. 충돌 처리
        // ---------------------------
        updateHP();     // 폭탄 충돌
        updateItems();  // 아이템 충돌
    }

    // ---------------------------
    // 폭탄 충돌 처리
    // ---------------------------
    protected void updateHP() {
        for (int i = 0; i < bombs.size(); i++) {

            CollidableObject bomb = bombs.get(i);

            // 캐릭터와 폭탄 충돌 → HP 감소
            if (bomb.collide(character)) {
                characterHP--;
                bombs.remove(bomb); // 충돌한 폭탄 제거
            }

            // 화면 밖으로 벗어난 폭탄 삭제
            else if (bomb.collide()) {
                bombs.remove(bomb);
            }
        }
    }

    // ---------------------------
    // 아이템 충돌 처리
    // ---------------------------
    protected void updateItems() {
        for (int i = 0; i < items.size(); i++) {

            ItemObject item = items.get(i);
            int type = item.getType(); // 아이템 종류

            // 캐릭터가 아이템과 충돌한 경우
            if (item.collide(character)) {

                // 아이템 4번(type == 3) → HP 회복
                if (type == 3) {
                    if (characterHP < 5)
                        characterHP++;
                }

                // 아이템 1~3 → 점수 획득
                else {
                    score += itemScores[type];
                }

                items.remove(item);  // 아이템 제거
            }

            // 화면을 벗어난 아이템 제거
            else if (item.collide()) {
                items.remove(item);
            }
        }
    }

    // ---------------------------
    // 화면 그리기
    // ---------------------------
    @Override
    public void paint(Graphics g) {
        super.paint(g);

        // HP가 0 이하 → 게임 오버 처리
        if (this.characterHP <= 0) {
            g.setColor(Color.black);
            g.setFont(new Font("Arial", Font.BOLD, 40));
            g.drawString("Game Over!", width/2-100, height/2);
            timer.stop(); // 게임 종료
        }

        // 게임 진행 중이면 UI 및 객체 그리기
        else {
            g.setColor(Color.black);
            g.setFont(new Font("Arial", Font.BOLD, 20));

            // HP 및 점수 표시
            g.drawString("HP : " + hp(), 10, 30);
            g.drawString("Score : " + score, 10, 60);

            // 캐릭터 및 오브젝트 그리기
            character.paint(g);

            for (CollidableObject bomb : bombs)
                bomb.paint(g);

            for (ItemObject item : items)
                item.paint(g);
        }
    }

    // HP를 ●, ○ 아이콘으로 시각적 표시
    public String hp() {
        switch(this.characterHP) {
            case 5: return "● ● ● ● ●";
            case 4: return "● ● ● ● ○";
            case 3: return "● ● ● ○ ○";
            case 2: return "● ● ○ ○ ○";
            case 1: return "● ○ ○ ○ ○";
            default: return "○ ○ ○ ○ ○"; // HP 0
        }
    }
}
