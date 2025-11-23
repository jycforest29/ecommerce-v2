package ecommerce.platform.common.constants;

public enum Color implements ProductOption<Color>{
    BLACK,
    WHITE,
    RED,
    PINK,
    ORANGE,
    YELLOW,
    GREEN,
    PURPLE,
    BROWN,
    BLUE,
    NAVY;

    @Override
    public boolean equalsTo(Color color) {
        return color == this;
    }
}
