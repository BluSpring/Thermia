package sylenthuntress.thermia.compat.impl;

import net.minecraft.world.level.Level;
import sereneseasons.api.season.ISeasonState;
import sereneseasons.api.season.SeasonHelper;
import sylenthuntress.thermia.compat.SereneSeasonsCompatBase;

public class SereneSeasonsCompat implements SereneSeasonsCompatBase {
    @Override
    public ISeasonState getSeasonState(Level world) {
        return SeasonHelper.getSeasonState(world);
    }
}
