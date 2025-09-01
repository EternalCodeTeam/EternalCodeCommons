package com.eternalcode.commons.progressbar;

public class ProgressBar {

    private final String filledToken;

    private final String emptyToken;

    private final boolean showBrackets;
    private final String leftBracket;
    private final String rightBracket;
    private final String bracketColor;

    private final int length;

    private ProgressBar(Builder builder) {
        this.leftBracket = builder.leftBracket;
        this.rightBracket = builder.rightBracket;
        this.bracketColor = builder.bracketColor;
        this.length = builder.length;
        this.showBrackets = builder.showBrackets;

        this.filledToken = builder.filledColor + builder.filledChar;
        this.emptyToken = builder.emptyColor + builder.emptyChar;
    }

    public String render(double progress) {
        double clampedProgress = Math.max(0, Math.min(1, progress));
        int filled = (int) (this.length * clampedProgress);

        StringBuilder bar = new StringBuilder();

        if (this.showBrackets) {
            bar.append(this.bracketColor).append(this.leftBracket);
        }

        bar.append(this.filledToken.repeat(filled));
        bar.append(this.emptyToken.repeat(this.length - filled));

        if (this.showBrackets) {
            bar.append(this.bracketColor).append(this.rightBracket);
        }

        return bar.toString();
    }

    public String render(int current, int max) {
        return this.render(current, (long) max);
    }

    public String render(long current, long max) {
        if (max <= 0) {
            return this.render(1.0);
        }
        double progress = (double) current / max;
        return this.render(progress);
    }

    public static Builder builder() {
        return new Builder();
    }

    public static class Builder {
        private String filledChar = "█";
        private String emptyChar = "░";
        private String filledColor = "";
        private String emptyColor = "";
        private String leftBracket = "[";
        private String rightBracket = "]";
        private String bracketColor = "";
        private int length = 10;
        private boolean showBrackets = true;

        public Builder filledChar(String filledChar) {
            this.filledChar = filledChar;
            return this;
        }

        public Builder emptyChar(String emptyChar) {
            this.emptyChar = emptyChar;
            return this;
        }

        public Builder filledColor(String filledColor) {
            this.filledColor = filledColor;
            return this;
        }

        public Builder emptyColor(String emptyColor) {
            this.emptyColor = emptyColor;
            return this;
        }

        public Builder leftBracket(String leftBracket) {
            this.leftBracket = leftBracket;
            return this;
        }

        public Builder rightBracket(String rightBracket) {
            this.rightBracket = rightBracket;
            return this;
        }

        public Builder brackets(String left, String right) {
            this.leftBracket = left;
            this.rightBracket = right;
            return this;
        }

        public Builder bracketColor(String bracketColor) {
            this.bracketColor = bracketColor;
            return this;
        }

        public Builder length(int length) {
            if (length <= 0) {
                throw new IllegalArgumentException("Length must be positive");
            }
            this.length = length;
            return this;
        }

        public Builder showBrackets(boolean showBrackets) {
            this.showBrackets = showBrackets;
            return this;
        }

        public Builder hideBrackets() {
            this.showBrackets = false;
            return this;
        }

        public ProgressBar build() {
            return new ProgressBar(this);
        }
    }
}
