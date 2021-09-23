package etn.app.danghoc.eatitserver.EventBus;

public class ChangeMenuClick {
    private boolean isFromFoodList;

    public ChangeMenuClick(boolean isFromFoodList) {
        this.isFromFoodList = isFromFoodList;
    }

    public ChangeMenuClick() {
    }

    public boolean isFromFoodList() {
        return isFromFoodList;
    }

    public void setFromFoodList(boolean fromFoodList) {
        isFromFoodList = fromFoodList;
    }
}
