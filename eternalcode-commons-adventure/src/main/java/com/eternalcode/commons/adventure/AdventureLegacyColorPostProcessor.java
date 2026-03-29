package com.eternalcode.commons.adventure;

import java.util.ArrayList;
import java.util.List;
import java.util.function.UnaryOperator;
import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.TextComponent;

public class AdventureLegacyColorPostProcessor implements UnaryOperator<Component> {

    @Override
    public Component apply(Component component) {
        return this.processComponent(component);
    }

    private Component processComponent(Component component) {
        if (component instanceof TextComponent textComponent) {
            String content = textComponent.content();
            if (!content.isEmpty()) {
                Component processed = AdventureUtil.component(content);
                List<Component> processedChildren = new ArrayList<>(component.children().size());
                for (Component child : component.children()) {
                    processedChildren.add(this.processComponent(child));
                }
                return processed.children(processedChildren).style(component.style());
            }
        }

        List<Component> processedChildren = new ArrayList<>(component.children().size());
        for (Component child : component.children()) {
            processedChildren.add(this.processComponent(child));
        }
        return component.children(processedChildren);
    }
}
