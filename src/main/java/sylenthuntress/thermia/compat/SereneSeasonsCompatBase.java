package sylenthuntress.thermia.compat;

import net.minecraft.world.level.Level;
import sereneseasons.api.season.ISeasonState;

public interface SereneSeasonsCompatBase {
    ISeasonState getSeasonState(Level world);
}
