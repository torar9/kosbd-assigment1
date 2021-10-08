package cz.osu.ukol;

import lombok.Getter;

public class CodeType {
    @Getter private int length;
    @Getter private int numberOfInfoBits;
    @Getter private int redundancy;

    CodeType(int length, int numberOfInfoBits) {
        this.length = length;
        this.numberOfInfoBits = numberOfInfoBits;
        redundancy = length - numberOfInfoBits;
    }

    @Override
    public String toString() {
        return "(" + length + ", " + numberOfInfoBits + ")";
    }

}
