package ecommerce.platform.common.constants;

public enum ClothingSize implements ProductOption<ClothingSize>{
    XS,
    S,
    M,
    L,
    XL;

    @Override
    public boolean equalsTo(ClothingSize clothingSize) {
        return clothingSize == this;
    }
}
