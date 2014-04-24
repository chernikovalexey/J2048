package com.twopeople.game.cp;

import java.io.File;
import java.io.IOException;

public class PathManager {
    private File gameDir = null;

    public File getAbsolutePathForFile(String name) {
        if (gameDir == null) {
            gameDir = getAppDir("j2048");
        }

        File file = new File(gameDir.getAbsolutePath() + "/" + name);

        if (!file.exists()) {
            try {
                file.createNewFile();
            } catch (IOException e) {
                e.printStackTrace();
            }
        }

        return file;
    }

    public File getAppDir(String s) {
        String s1 = System.getProperty("user.home", ".");
        File file;

        switch (EnumOSMap.map[Os.getOs().ordinal()]) {
            case 1:
            case 2:
                file = new File(s1, (new StringBuilder()).append('.').append(s).append('/').toString());
                break;

            case 3:
                String s2 = System.getenv("APPDATA");
                if (s2 != null) {
                    file = new File(s2, (new StringBuilder()).append(".").append(s).append('/').toString());
                } else {
                    file = new File(s1, (new StringBuilder()).append('.').append(s).append('/').toString());
                }
                break;

            case 4:
                file = new File(s1, (new StringBuilder()).append("Library/Application Support/").append(s).toString());
                break;

            default:
                file = new File(s1, (new StringBuilder()).append(s).append('/').toString());
                break;
        }

        if (!file.exists() && !file.mkdirs()) {
            throw new RuntimeException((new StringBuilder()).append("The working directory could not be created: ").append(file).toString());
        } else {
            return file;
        }
    }
}