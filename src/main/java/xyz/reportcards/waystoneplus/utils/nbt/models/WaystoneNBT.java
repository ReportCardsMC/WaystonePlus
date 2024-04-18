package xyz.reportcards.waystoneplus.utils.nbt.models;

import xyz.reportcards.waystoneplus.WaystonePlus;
import xyz.reportcards.waystoneplus.configuration.WaystoneConfig;
import xyz.reportcards.waystoneplus.utils.strings.RandomString;

import java.util.ArrayList;

public class WaystoneNBT {

    public boolean publicWaystone = false;
    public String waystoneName;

    public WaystoneNBT(ArrayList<String> exclude) {
        WaystoneConfig.NameGeneration generation = WaystonePlus.getInstance().getWaystoneConfig().nameGeneration;
        if (generation == WaystoneConfig.NameGeneration.WORDS)
            waystoneName = RandomString.generateWithWords(2, 11, exclude);
        else if (generation == WaystoneConfig.NameGeneration.LETTERS)
            waystoneName = RandomString.generateOnlyLetters(6, exclude);
        else if (generation == WaystoneConfig.NameGeneration.NUMBERS)
            waystoneName = RandomString.generateOnlyNumbers(6, exclude);
    }

    public static WaystoneNBT fromString(String json) {
        return WaystonePlus.getInstance().getGson().fromJson(json, WaystoneNBT.class);
    }

    @Override
    public String toString() {
        return WaystonePlus.getInstance().getGson().toJson(this);
    }
}
