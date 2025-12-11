package move;

public class ItemObject extends CollidableObject {
    private int type;

    public ItemObject(int type, String image, int width, int height) {
        super(CollidableObject.STOP, CollidableObject.DOWN, image, width, height);
        this.type = type;
    }

    public int getType() {
        return type;
    }
}
