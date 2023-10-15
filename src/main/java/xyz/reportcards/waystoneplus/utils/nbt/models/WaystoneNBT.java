package xyz.reportcards.waystoneplus.utils.nbt.models;

import xyz.reportcards.waystoneplus.WaystonePlus;
import xyz.reportcards.waystoneplus.utils.strings.RandomString;

public class WaystoneNBT {

    public boolean publicWaystone = false;
    public String waystoneName = "";

    public WaystoneNBT() {
        // TODO: Consider config
        waystoneName = RandomString.generateWithWords(2, 11);
    }

    @Override
    public String toString() {
        return WaystonePlus.getInstance().getGson().toJson(this);
    }

    public static WaystoneNBT fromString(String json) {
        return WaystonePlus.getInstance().getGson().fromJson(json, WaystoneNBT.class);
    }
}
