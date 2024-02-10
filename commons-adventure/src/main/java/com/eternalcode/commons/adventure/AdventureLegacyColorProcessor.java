package com.eternalcode.commons.adventure;

import java.util.function.UnaryOperator;
import java.util.regex.Pattern;
import net.kyori.adventure.text.Component;

public class AdventureLegacyColorProcessor implements UnaryOperator<Component> {

    @Override
    public Component apply(Component component) {
        return component.replaceText(builder -> builder.match(Pattern.compile(".*")).replacement(
            (result, input) -> AdventureUtil.component(result.group())));
    }
}
