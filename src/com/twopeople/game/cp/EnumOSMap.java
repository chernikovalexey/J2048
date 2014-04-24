package com.twopeople.game.cp;

public class EnumOSMap {
    public static final int map[];

    static {
        map = new int[EnumOS.values().length];

        try {
            map[EnumOS.linux.ordinal()] = 1;
        } catch (NoSuchFieldError nosuchfielderror) {
        }

        try {
            map[EnumOS.solaris.ordinal()] = 2;
        } catch (NoSuchFieldError nosuchfielderror1) {
        }

        try {
            map[EnumOS.windows.ordinal()] = 3;
        } catch (NoSuchFieldError nosuchfielderror2) {
        }

        try {
            map[EnumOS.macos.ordinal()] = 4;
        } catch (NoSuchFieldError nosuchfielderror3) {
        }
    }
}