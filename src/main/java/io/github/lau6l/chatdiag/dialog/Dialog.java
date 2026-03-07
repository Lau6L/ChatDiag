package io.github.lau6l.chatdiag.dialog;

import org.jspecify.annotations.NonNull;

import java.util.Arrays;
import java.util.List;

public record Dialog (List<String> lines, int delayMultiplier, String prefix, String suffix, String soundId, float soundPitch) {
    public static Dialog createLine(String line, String soundId, float soundPitch) {
        return createLine(line, "", "", soundId, soundPitch);
    }
    public static Dialog createLine(String line, String prefix, String suffix, String soundId, float soundPitch) {
        return new Dialog(
                List.of(line),
                1,
                prefix,
                suffix,
                soundId,
                soundPitch
        );
    }

    public String line(int index) {
        return (prefix == null ? "" : prefix) + lines.get(index) + (suffix == null ? "" : suffix);
    }
    public int wordsInLine(int index) {
        String l = lines.get(index);
        boolean inWord = false;
        int wordCount = 0;
        for (int i = 0; i < l.length(); i++) {
            if (Character.isWhitespace(l.charAt(i))) {
                inWord = false;
            } else if (!inWord) {
                wordCount++;
                inWord = true;
            }
        }
        return wordCount;
    }

    @Override
    public @NonNull String toString() {
        return "Dialog{" +
                "soundId=" + soundId +
                ", soundPitch=" + soundPitch +
                ", prefix='" + prefix + '\'' +
                ", suffix='" + suffix + '\'' +
                ", delayMultiplier=" + delayMultiplier +
                ", lines=" + Arrays.toString(lines.toArray()) +
                '}';
    }
}