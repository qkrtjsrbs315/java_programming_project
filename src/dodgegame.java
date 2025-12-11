import javax.swing.*;
import move.*;

public class dodgegame {
    public static void main(String[] args) {
        final String imagePath = "C:\\Users\\insilicox_dev\\IdeaProjects\\javagame2\\src\\move\\image\\";
        final int WIDTH = 300;
        final int HEIGHT = 600;

        // 아이템 이미지 배열 준비
        String[] itemImages = {
            imagePath + "item1.png",
            imagePath + "item2.png",
            imagePath + "item3.png",
            imagePath + "item4.png"
        };

        // 캐릭터 생성
        CollidableObject character = new CollidableObject(
            imagePath + "character.png",
            WIDTH/2,
            HEIGHT - CollidableObject.IMGSIZE,
            WIDTH,
            HEIGHT
        );

        // DodgePanel 생성 (배열 전달)
        DodgePanel panel = new DodgePanel(character, imagePath + "bomb.png", itemImages);

        // 프레임 설정
        JFrame frame = new JFrame("go yang yi");
        frame.getContentPane().add(panel);
        frame.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        frame.pack();
        frame.setVisible(true);
    }
}
