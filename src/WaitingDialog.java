// WaitingDialog.java
import javax.swing.*;

/**
 * 간단한 "대기 중" 메시지를 화면에 표시하는 유틸리티 클래스.
 * 별도의 다이얼로그 프레임을 만들지 않고 JOptionPane을 사용하여
 * 사용자에게 현재 대기 상태임을 알려준다.
 */
public class WaitingDialog {

    /**
     * 대기 메시지를 표시하는 정적 메서드.
     * 모달(modal) 메시지 박스이기 때문에 사용자가 확인을 누를 때까지
     * 해당 함수는 블록된다.
     *
     * @param message 화면에 표시할 메시지 문자열
     */
    public static void showWaiting(String message) {
        // JOptionPane.showMessageDialog:
        // - 첫 번째 인자: 부모 컴포넌트(null이면 화면 중앙)
        // - 두 번째 인자: 표시할 메시지
        // - 세 번째 인자: 타이틀(창 제목)
        // - 네 번째 인자: 아이콘 스타일 (정보)
        JOptionPane.showMessageDialog(
                null,
                message,
                "대기 중",
                JOptionPane.INFORMATION_MESSAGE
        );
    }
}
