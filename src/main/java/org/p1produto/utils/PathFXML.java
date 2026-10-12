package org.p1produto.utils;

import java.nio.file.Paths;

public class PathFXML {
    public static String pathFXML(){
        String path = "src/main/java/org/p1produto/view";
        return Paths.get (path).toAbsolutePath().toString();
    }
}
