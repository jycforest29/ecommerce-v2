package ecommerce.platform.common.constants;

public enum Brand implements ProductOption<Brand> {
    ALL,
    A,
    B,
    C,
    D,
    E,
    F,
    G,
    H,
    K,
    I,
    L,
    M,
    N,
    O;

    @Override
    public boolean equalsTo(Brand target) {
        return this == Brand.ALL || this == target;
    }
}
