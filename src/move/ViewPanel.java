package move;

import java.awt.*;
import java.awt.event.*;
import javax.swing.*;

/**
 * 게임의 기본 View 패널.
 * 캐릭터(ObjectByKey)를 포함하고 있으며, 타이머를 통해 주기적으로 화면을 업데이트한다.
 *
 * - 키 입력은 character(ObjectByKey)가 처리한다.
 * - update()는 100ms마다 호출 → 객체 상태 갱신 후 repaint()
 * - DodgePanel 등에서 이 클래스를 상속하여 update() 내용을 확장한다.
 */
public class ViewPanel extends JPanel
{
    // 방향키로 움직이는 캐릭터(공통 기능)
    protected ObjectByKey character;

    // 주기적으로 update()를 호출하는 Swing Timer
    protected Timer timer;

    /**
     * ViewPanel 생성자
     * @param character 사용자가 방향키로 조작하는 캐릭터
     */
    public ViewPanel( ObjectByKey character ){
        // 패널의 키 이벤트를 받기 위한 설정
        this.character = character;

        // 방향키 이벤트를 캐릭터가 처리하도록 등록
        addKeyListener( character );
        setFocusable( true );    // 키 입력 포커스를 받을 수 있게 함
        requestFocus();          // 패널 생성 시 포커스 강제 획득

        // 패널 크기를 캐릭터 이동 범위에 맞게 설정
        setPreferredSize( new Dimension(
                character.backgroundWidth(),
                character.backgroundHeight()
        ));

        // 타이머 설정: 100ms마다 update() 실행 → 화면 갱신
        timer = new Timer( 100, new ActionListener() {
            @Override
            public void actionPerformed( ActionEvent event ) {
                update();   // 객체 변화 적용
                repaint();  // 화면 다시 그리기
            }
        });

        // 타이머 시작
        timer.start();
    }

    /**
     * 최신 정보 업데이트 (상속하여 수정 가능)
     * 기본 기능은 캐릭터 이동뿐
     */
    protected void update() {
        character.move();   // 캐릭터 이동
    }

    /**
     * 화면 출력 메서드
     * @param g Graphics 객체 (Java 기본 그림 객체)
     */
    @Override
    public void paint( Graphics g ){
        super.paint( g );        // 배경 등 기본 구성 먼저 그림
        character.paint( g );    // 캐릭터 이미지 출력
    }
}
