package etn.app.danghoc.eatitserver.EventBus;

import java.util.List;

import etn.app.danghoc.eatitserver.model.AddonModel;

public class UpdateAddonModel {
    private List<AddonModel>addonModel;


    public UpdateAddonModel() {

    }

    public List<AddonModel> getAddonModel() {
        return addonModel;
    }

    public void setAddonModel(List<AddonModel> addonModel) {
        this.addonModel = addonModel;
    }
}
