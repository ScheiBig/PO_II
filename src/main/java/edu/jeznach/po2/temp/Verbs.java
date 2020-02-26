package edu.jeznach.po2.temp;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.util.Arrays;
import java.util.regex.Pattern;
import java.util.stream.Collectors;

public class Verbs {

    public static void main(String[] args) throws IOException {
        String verbs = Arrays.stream(new String(
                Files.readAllBytes(
                        Paths.get("src/main/java/edu/jeznach/po2/temp/verb")
                )
        ).replaceAll("\\s", "")
         .replaceAll(Pattern.quote("(Brit)/"), ",")
         .replaceAll(Pattern.quote("(Brit),"), ",")
         .replaceAll("\\([0-9]*\\).", ",")
         .split(",")
        ).filter(s -> s.matches("[a-z]+e$"))
         .map(s -> s.concat("\n"))
         .collect(Collectors.joining())
         .toString();
        System.out.println(verbs);
    }
}
