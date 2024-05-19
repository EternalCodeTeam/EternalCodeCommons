package com.eternalcode.commons.adventure;

import java.util.function.UnaryOperator;
import java.util.regex.Pattern;
import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.TextReplacementConfig;

public class AdventureLegacyColorPostProcessor implements UnaryOperator<Component> {

    private static final TextReplacementConfig LEGACY_REPLACEMENT_CONFIG = TextReplacementConfig.builder()
        .match(Pattern.compile(".*"))
        .replacement((matchResult, build) -> AdventureUtil.component(matchResult.group()))
        .build();

    @Override
    public Component apply(Component component) {
        return component.replaceText(LEGACY_REPLACEMENT_CONFIG);
    }

}
