package com.eternalcode.commons.adventure;

import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.TextComponent;
import net.kyori.adventure.text.format.TextDecoration;
import net.kyori.adventure.text.serializer.legacy.LegacyComponentSerializer;

public class AdventureUtil {

    public static final LegacyComponentSerializer SECTION_SERIALIZER = LegacyComponentSerializer.builder()
        .character('§')
        .hexColors()
        .useUnusualXRepeatedCharacterHexFormat()
        .build();

    private static final LegacyComponentSerializer AMPERSAND_SERIALIZER = LegacyComponentSerializer.builder()
        .character('&')
        .hexColors()
        .useUnusualXRepeatedCharacterHexFormat()
        .build();

    private static final Component RESET_ITALIC = Component.text().decoration(TextDecoration.ITALIC, false).build();

    private AdventureUtil() {
        throw new UnsupportedOperationException("This is a utility class and cannot be instantiated");
    }

    public static Component component(String text) {
        return AMPERSAND_SERIALIZER.deserialize(text);
    }

    public static Component resetItalic(Component component) {
        return RESET_ITALIC.append(component);
    }

    public static String componentToRawString(Component text) {
        StringBuilder builder = new StringBuilder();

        if (text instanceof TextComponent textComponent) {
            builder.append(textComponent.content());
        }

        for (Component child : text.children()) {
            builder.append(componentToRawString(child));
        }

        return builder.toString();
    }
}
