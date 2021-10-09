package cz.osu.ukol;

import lombok.AllArgsConstructor;
import lombok.Getter;

@AllArgsConstructor
public class CodeReport {
    @Getter
    private boolean hasError;
    @Getter
    private boolean canBeFixed;
    @Getter
    private int bitFlipPosition;
}
