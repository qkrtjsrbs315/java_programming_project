package move;

// 충돌 가능한 이동 객체 (폭탄, 총알 등)
// ObjectByKey 를 상속하며 방향키 기반 이동 기능을 확장한다.
public class CollidableObject extends ObjectByKey
{
    /**
     * (1) 기본 생성자
     * - 이미지, 시작 위치(x,y), 이동 가능 범위를 설정한다.
     * - directionX, directionY 는 0 (정지 상태)
     *
     * @param image   사용할 이미지 파일명
     * @param x       초기 X 좌표
     * @param y       초기 Y 좌표
     * @param width   이동 가능 영역의 가로 범위
     * @param height  이동 가능 영역의 세로 범위
     */
    public CollidableObject(final String image, int x, int y, int width, int height) {
        // super(image, x, y, 초기 방향X, 초기 방향Y, 배경 최대너비, 최대높이)
        super(image, x, y, 0, 40, width, height);
    }

    /**
     * (2) 방향 기반 생성자
     * - directionX, directionY 를 지정하여 객체의 이동 방향을 설정한다.
     * - 폭탄/총알 등의 초기 위치를 자동 지정한다.
     *
     * @param directionX  이동 방향 X (STOP, LEFT, RIGHT)
     * @param directionY  이동 방향 Y (STOP, UP, DOWN)
     * @param image       이미지 파일명
     * @param width       이동 가능 영역 가로
     * @param height      이동 가능 영역 세로
     */
    public CollidableObject(int directionX, int directionY, final String image, int width, int height) {

        // 기본 시작 위치 (x=0, y=40)
        this(image, 0, 40, width, height);

        this.directionX = directionX;
        this.directionY = directionY;

        // 📌 폭탄 초기화: 화면 상단 임의 위치에서 아래로 떨어짐
        if ((directionX == STOP) && (directionY == DOWN)) {
            // x 좌표를 랜덤하게 설정
            this.x = (int)(Math.random() * this.maxX);
        }

        // 📌 총알 초기화: 왼쪽 임의 위치에서 오른쪽으로 이동
        else if ((directionX == RIGHT) && (directionY == STOP)) {
            // y 좌표를 랜덤하게 설정 (상단 메뉴바 높이 = IMGSIZE)
            this.y = IMGSIZE + (int)(Math.random() * (this.maxY - IMGSIZE));
        }
    }


    /**
     * (3) move() — 일정 속도로 이동 처리
     *
     * directionX, directionY 값(상하좌우, 정지)을 받아
     * SPEED 를 곱해서 실제 이동량으로 적용한다.
     *
     * @param directionX  이동 방향 X (-1, 0, 1)
     * @param directionY  이동 방향 Y (-1, 0, 1)
     */
    @Override
    public void move(int directionX, int directionY) {
        final int SPEED = 20;  // 모든 CollidableObject의 기본 속도
        super.move(directionX * SPEED, directionY * SPEED);
    }


    /**
     * (4) 다른 객체와의 충돌 여부 판단
     * 두 객체의 x,y 좌표 차이가 이미지 크기(IMGSIZE)보다 작으면 충돌로 판단한다.
     *
     * @param that  충돌을 검사할 다른 객체
     * @return true: 충돌한 경우
     */
    public boolean collide(ObjectByKey that) {
        return (Math.abs(this.x - that.x) < IMGSIZE)
                && (Math.abs(this.y - that.y) < IMGSIZE);
    }


    /**
     * (5) 이동 가능 범위를 벗어났는지 검사
     * 화면 밖으로 나가면 true 반환 → 객체 삭제 처리에 사용됨
     *
     * @return true: 화면 밖으로 벗어남
     */
    public boolean collide() {
        return (this.x < minX) || (maxX < this.x)
                || (this.y < minY) || (maxY < this.y);
    }
}
