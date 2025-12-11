package move;

import java.awt.*;
import java.util.ArrayList;

// 폭탄 피하기 패널
public class DodgePanel extends ViewPanel {
    protected ArrayList<CollidableObject> bombs;
    protected ArrayList<ItemObject> items;
    protected int characterHP, width, height, score;
    protected String imageBomb;
    protected String[] itemImages;
    protected int[] itemScores = {10, 20, 30, 0}; // item1=10, item2=20, item3=30, item4=HP회복

    public DodgePanel(CollidableObject character, final String imageBomb, final String[] itemImages) {
        super(character);
        this.characterHP = 3; // 시작 체력 3
        this.bombs = new ArrayList<>();
        this.items = new ArrayList<>();
        this.score = 0;
        this.imageBomb = imageBomb;
        this.itemImages = itemImages; // {"item1.png", "item2.png", "item3.png", "item4.png"}
        this.width = character.backgroundWidth();
        this.height = character.backgroundHeight();
        setBackground(Color.white);
        setPreferredSize(new Dimension(width + CollidableObject.IMGSIZE, height));
    }

    // 최신 정보 업데이트(Override)
    @Override
    protected void update() {
        // 폭탄 이동 및 생성
        for (CollidableObject bomb : bombs)
            bomb.move();
        if ((System.currentTimeMillis() % 8) == 0) {
            bombs.add(new CollidableObject(CollidableObject.STOP, CollidableObject.DOWN, imageBomb, width, height));
        }

        // 아이템 이동 및 생성
        for (ItemObject item : items)
            item.move();
        if ((System.currentTimeMillis() % 20) == 0) {
            int type = (int)(Math.random() * 4); // 0~3 랜덤
            items.add(new ItemObject(type, itemImages[type], width, height));
        }

        // 캐릭터 이동
        character.move(character.directionX(), CollidableObject.STOP);

        // HP 및 아이템 충돌 처리
        updateHP();
        updateItems();
    }

    // 캐릭터와 폭탄이 충돌하면 캐릭터 HP 감소
    protected void updateHP() {
        for (int i = 0; i < bombs.size(); i++) {
            CollidableObject bomb = bombs.get(i);
            if (bomb.collide(character)) {
                characterHP--;
                bombs.remove(bomb);
            }
            // 이동 허용 범위를 벗어난 폭탄은 제거
            else if (bomb.collide()) {
                bombs.remove(bomb);
            }
        }
    }

    // 아이템 충돌 처리
    protected void updateItems() {
        for (int i = 0; i < items.size(); i++) {
            ItemObject item = items.get(i);
            int type = item.getType();

            if (item.collide(character)) {
                if (type == 3) { // item4 → HP 회복
                    if (characterHP < 5) characterHP++;
                } else {
                    score += itemScores[type]; // item1~3 → 점수 추가
                }
                items.remove(item);
            } else if (item.collide()) {
                items.remove(item);
            }
        }
    }

    // 화면 출력(Override)
    @Override
    public void paint(Graphics g) {
        super.paint(g);
        if (this.characterHP <= 0) {
            g.setColor(Color.black);
            g.setFont(new Font("Arial", Font.BOLD, 40));
            g.drawString("Game Over!", width/2-100, height/2);
            timer.stop();
        } else {
            g.setColor(Color.black);
            g.setFont(new Font("Arial", Font.BOLD, 20));
            g.drawString("HP : " + hp(), 10, 30);
            g.drawString("Score : " + score, 10, 60);
            character.paint(g);
            for (CollidableObject bomb : bombs)
                bomb.paint(g);
            for (ItemObject item : items)
                item.paint(g);
        }
    }

    public String hp() {
        switch(this.characterHP) {
            case 5: return "● ● ● ● ●";
            case 4: return "● ● ● ● ○";
            case 3: return "● ● ● ○ ○";
            case 2: return "● ● ○ ○ ○";
            case 1: return "● ○ ○ ○ ○";
            default: return "○ ○ ○ ○ ○";
        }
    }
}
