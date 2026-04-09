package ecommerce.platform.common.constants;

public enum ShoeSize implements ProductOption<ShoeSize> {
    SIZE_230(230),
    SIZE_240(240),
    SIZE_250(250),
    SIZE_260(260),
    SIZE_270(270),
    SIZE_280(280);

    private final int sizeValue;

    ShoeSize(int sizeValue) {
        this.sizeValue = sizeValue;
    }
}
