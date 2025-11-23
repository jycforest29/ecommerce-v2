package ecommerce.platform.common.constants;

public enum Category implements ProductOption<Category> {
    ALL,
    OUTER,
    KNIT,
    SHIRTS,
    PANTS,
    SKIRT,
    SHOES,
    ACCESSORY,
    COSMETICS,
    BAGS;

    @Override
    public boolean equalsTo(Category category) {
        return this == Category.ALL || this == category;
    }
}