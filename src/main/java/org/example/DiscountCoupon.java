package org.example;

import javafx.beans.property.BooleanProperty;
import javafx.beans.property.DoubleProperty;
import javafx.beans.property.IntegerProperty;
import javafx.beans.property.SimpleBooleanProperty;
import javafx.beans.property.SimpleDoubleProperty;
import javafx.beans.property.SimpleIntegerProperty;
import javafx.beans.property.SimpleStringProperty;
import javafx.beans.property.StringProperty;

public class DiscountCoupon {
    private final StringProperty code = new SimpleStringProperty();
    private final StringProperty description = new SimpleStringProperty();
    private final DoubleProperty discountPercent = new SimpleDoubleProperty();
    private final StringProperty targetUsers = new SimpleStringProperty();
    private final StringProperty conditions = new SimpleStringProperty();
    private final IntegerProperty quantity = new SimpleIntegerProperty();
    private final BooleanProperty active = new SimpleBooleanProperty(true);

    public DiscountCoupon(String code, String description, double discountPercent, String targetUsers, String conditions, boolean active) {
        this(code, description, discountPercent, targetUsers, conditions, 0, active);
    }

    public DiscountCoupon(String code, String description, double discountPercent, String targetUsers, String conditions, int quantity, boolean active) {
        this.code.set(code);
        this.description.set(description);
        this.discountPercent.set(discountPercent);
        this.targetUsers.set(targetUsers);
        this.conditions.set(conditions);
        this.quantity.set(Math.max(quantity, 0));
        this.active.set(active);
    }

    public StringProperty codeProperty() { return code; }
    public StringProperty descriptionProperty() { return description; }
    public DoubleProperty discountPercentProperty() { return discountPercent; }
    public StringProperty targetUsersProperty() { return targetUsers; }
    public StringProperty conditionsProperty() { return conditions; }
    public IntegerProperty quantityProperty() { return quantity; }
    public BooleanProperty activeProperty() { return active; }
}
