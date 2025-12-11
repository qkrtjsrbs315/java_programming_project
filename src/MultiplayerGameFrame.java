// 멀티플레이 게임 화면을 표시하는 JFrame 클래스
// Host 또는 Client 가 전달하는 ViewPanel(게임 패널)을 화면에 출력하는 역할을 담당한다.

import move.ViewPanel;
import javax.swing.*;

public class MultiplayerGameFrame extends JFrame {

    /**
     * 게임 프레임 생성자
     *
     * @param title  - 창 제목
     * @param panel  - 게임 화면(ViewPanel) 객체
     *
     * 이 클래스는 단순히 전달된 게임 패널을 JFrame에 붙이고 화면에 보여주는 용도로 사용된다.
     */
    public MultiplayerGameFrame(String title, ViewPanel panel) {

        // 창 제목 설정
        setTitle(title);

        // 창 닫을 때 프로그램 종료
        setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);

        // 전달된 게임 패널을 프레임의 콘텐츠로 설정
        setContentPane(panel);

        // Panel의 PreferredSize에 맞춰 프레임 크기 자동 조절
        pack();

        // 화면 중앙에 배치
        setLocationRelativeTo(null);

        // 창 표시
        setVisible(true);
    }
}
