package move;

/**
 * ItemObject
 * - 게임 중 떨어지는 아이템 객체
 * - CollidableObject(충돌 가능한 오브젝트)를 상속받아 이동 및 충돌 기능을 그대로 사용
 * - type 값을 통해 아이템 종류를 구분함
 *   (0 = 점수 아이템1, 1 = 점수 아이템2, 2 = 점수 아이템3, 3 = HP 회복 아이템)
 */
public class ItemObject extends CollidableObject {

    // 아이템 종류(0~3)
    private int type;

    /**
     * 생성자
     * @param type  아이템 종류 (0~3)
     * @param image 아이템 이미지 파일명
     * @param width 게임 화면 폭
     * @param height 게임 화면 높이
     *
     * - CollidableObject의 방향을 STOP, DOWN으로 설정 → 아이템이 위에서 아래로 떨어짐
     */
    public ItemObject(int type, String image, int width, int height) {
        // CollidableObject의 (directionX=STOP, directionY=DOWN) 생성자를 호출하여 아래로 떨어지게 설정
        super(CollidableObject.STOP, CollidableObject.DOWN, image, width, height);
        this.type = type;
    }

    /**
     * 아이템 종류 반환
     * @return type (0~3)
     */
    public int getType() {
        return type;
    }
}
