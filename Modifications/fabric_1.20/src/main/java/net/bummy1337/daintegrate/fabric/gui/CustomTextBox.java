package net.bummy1337.daintegrate.fabric.gui;

import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.Font;
import net.minecraft.client.gui.GuiGraphicsExtractor;
import net.minecraft.client.gui.components.AbstractWidget;
import net.minecraft.client.gui.narration.NarratedElementType;
import net.minecraft.client.gui.narration.NarrationElementOutput;
import net.minecraft.client.input.CharacterEvent;
import net.minecraft.client.input.KeyEvent;
import net.minecraft.client.input.MouseButtonEvent;
import net.minecraft.network.chat.Component;
import net.minecraft.network.chat.MutableComponent;
import net.minecraft.network.chat.Style;
import net.minecraft.util.FormattedCharSequence;

import org.jspecify.annotations.Nullable;
import java.util.Objects;
import java.util.function.BiFunction;
import java.util.function.Consumer;
import java.util.function.Predicate;

public class CustomTextBox extends AbstractWidget {
    public static final int BACKWARDS = -1;
    public static final int FORWARDS = 1;
    private static final int CURSOR_INSERT_WIDTH = 1;
    private static final int CURSOR_INSERT_COLOR = -3092272;
    private static final String CURSOR_APPEND_CHARACTER = "_";
    public static final int DEFAULT_TEXT_COLOR = 0xFFE0E0E0;
    private static final int BORDER_COLOR_FOCUSED = -1;
    private static final int BORDER_COLOR = -6250336;
    private static final int BACKGROUND_COLOR = -16777216;
    private final Font font;
    private String value = "";
    private int maxLength = 1024;
    private int frame;
    private boolean bordered = true;
    private boolean canLoseFocus = true;
    private boolean isEditable = true;
    private boolean shiftPressed;
    private int displayPos;
    private int cursorPos;
    private int highlightPos;
    private int textColor = DEFAULT_TEXT_COLOR;
    private int textColorUneditable = 0xFF707070;
    @Nullable
    private String suggestion;
    @Nullable
    private Consumer<String> responder;
    private Predicate<String> filter = Objects::nonNull;
    private BiFunction<String, Integer, FormattedCharSequence> formatter = (p_94147_, p_94148_) -> {
        return FormattedCharSequence.forward(p_94147_, Style.EMPTY);
    };
    public String tag;
    public int LineColor;

    public CustomTextBox(Font font, int x, int y, int width, int height, String text) {
        super(x, y, width, height, Component.literal(text));
        this.font = font;
    }

    public void setText(String value) {
        this.value = value;
    }

    public String getText() {
        return value;
    }

    public void setResponder(Consumer<String> responder) {
        this.responder = responder;
    }

    public void setFormatter(BiFunction<String, Integer, FormattedCharSequence> formatter) {
        this.formatter = formatter;
    }

    public void tick() {
        ++this.frame;
    }

    protected MutableComponent createNarrationMessage() {
        Component component = this.getMessage();
        return Component.translatable("gui.narrate.editBox", component, this.value);
    }

    public void setValue(String value) {
        if (this.filter.test(value)) {
            if (value.length() > this.maxLength) {
                this.value = value.substring(0, this.maxLength);
            } else {
                this.value = value;
            }
            this.moveCursorToEnd();
            this.setHighlightPos(this.cursorPos);
            this.onValueChange(value);
        }
    }

    public String getValue() {
        return this.value;
    }

    public String getHighlighted() {
        int i = Math.min(this.cursorPos, this.highlightPos);
        int j = Math.max(this.cursorPos, this.highlightPos);
        return this.value.substring(i, j);
    }

    public void setFilter(Predicate<String> filter) {
        this.filter = filter;
    }

    private String filterText(String input) {
        StringBuilder sb = new StringBuilder();
        for (int i = 0; i < input.length(); i++) {
            char c = input.charAt(i);
            if (new CharacterEvent(c).isAllowedChatCharacter()) {
                sb.append(c);
            }
        }
        return sb.toString();
    }

    public void insertText(String text) {
        int i = Math.min(this.cursorPos, this.highlightPos);
        int j = Math.max(this.cursorPos, this.highlightPos);
        int k = this.maxLength - this.value.length() - (i - j);
        String s = filterText(text);
        int l = s.length();
        if (k < l) {
            s = s.substring(0, k);
            l = k;
        }
        String s1 = (new StringBuilder(this.value)).replace(i, j, s).toString();
        if (this.filter.test(s1)) {
            this.value = s1;
            this.setCursorPosition(i + l);
            this.setHighlightPos(this.cursorPos);
            this.onValueChange(this.value);
        }
    }

    private void onValueChange(String value) {
        if (this.responder != null) {
            this.responder.accept(value);
        }
    }

    private void deleteText(int direction, KeyEvent event) {
        if (event.hasControlDown()) {
            this.deleteWords(direction);
        } else {
            this.deleteChars(direction);
        }
    }

    public void deleteWords(int direction) {
        if (!this.value.isEmpty()) {
            if (this.highlightPos != this.cursorPos) {
                this.insertText("");
            } else {
                this.deleteChars(this.getWordPosition(direction) - this.cursorPos);
            }
        }
    }

    public void deleteChars(int direction) {
        if (!this.value.isEmpty()) {
            if (this.highlightPos != this.cursorPos) {
                this.insertText("");
            } else {
                int i = this.getCursorPos(direction);
                int j = Math.min(i, this.cursorPos);
                int k = Math.max(i, this.cursorPos);
                if (j != k) {
                    String s = (new StringBuilder(this.value)).delete(j, k).toString();
                    if (this.filter.test(s)) {
                        this.value = s;
                        this.moveCursorTo(j);
                    }
                }
            }
        }
    }

    public int getWordPosition(int direction) {
        return this.getWordPosition(direction, this.getCursorPosition());
    }

    private int getWordPosition(int direction, int pos) {
        return this.getWordPosition(direction, pos, true);
    }

    private int getWordPosition(int direction, int pos, boolean skipSpaces) {
        int i = pos;
        boolean flag = direction < 0;
        int j = Math.abs(direction);
        for (int k = 0; k < j; ++k) {
            if (!flag) {
                int l = this.value.length();
                i = this.value.indexOf(32, i);
                if (i == -1) {
                    i = l;
                } else {
                    while (skipSpaces && i < l && this.value.charAt(i) == ' ') {
                        ++i;
                    }
                }
            } else {
                while (skipSpaces && i > 0 && this.value.charAt(i - 1) == ' ') {
                    --i;
                }
                while (i > 0 && this.value.charAt(i - 1) != ' ') {
                    --i;
                }
            }
        }
        return i;
    }

    public void moveCursor(int delta) {
        this.moveCursorTo(this.getCursorPos(delta));
    }

    private int getCursorPos(int delta) {
        try {
            return this.value.offsetByCodePoints(this.cursorPos, delta);
        } catch (IndexOutOfBoundsException e) {
            return delta > 0 ? this.value.length() : 0;
        }
    }

    public void moveCursorTo(int pos) {
        this.setCursorPosition(pos);
        if (!this.shiftPressed) {
            this.setHighlightPos(this.cursorPos);
        }
        this.onValueChange(this.value);
    }

    public void setCursorPosition(int pos) {
        this.cursorPos = Math.min(Math.max(pos, 0), this.value.length());
    }

    public void moveCursorToStart() {
        this.moveCursorTo(0);
    }

    public void moveCursorToEnd() {
        this.moveCursorTo(this.value.length());
    }

    public boolean keyPressed(KeyEvent event) {
        if (!this.canConsumeInput()) {
            return false;
        } else {
            this.shiftPressed = event.hasShiftDown();
            if (event.isSelectAll()) {
                this.moveCursorToEnd();
                this.setHighlightPos(0);
                return true;
            } else if (event.isCopy()) {
                Minecraft.getInstance().keyboardHandler.setClipboard(this.getHighlighted());
                return true;
            } else if (event.isPaste()) {
                if (this.isEditable) {
                    this.insertText(Minecraft.getInstance().keyboardHandler.getClipboard());
                }
                return true;
            } else if (event.isCut()) {
                Minecraft.getInstance().keyboardHandler.setClipboard(this.getHighlighted());
                if (this.isEditable) {
                    this.insertText("");
                }
                return true;
            } else {
                switch (event.key()) {
                    case 259:
                        if (this.isEditable) {
                            this.shiftPressed = false;
                            this.deleteText(-1, event);
                            this.shiftPressed = event.hasShiftDown();
                        }
                        return true;
                    case 261:
                        if (this.isEditable) {
                            this.shiftPressed = false;
                            this.deleteText(1, event);
                            this.shiftPressed = event.hasShiftDown();
                        }
                        return true;
                    case 262:
                        if (event.hasControlDown()) {
                            this.moveCursorTo(this.getWordPosition(1));
                        } else {
                            this.moveCursor(1);
                        }
                        return true;
                    case 263:
                        if (event.hasControlDown()) {
                            this.moveCursorTo(this.getWordPosition(-1));
                        } else {
                            this.moveCursor(-1);
                        }
                        return true;
                    case 268:
                        this.moveCursorToStart();
                        return true;
                    case 269:
                        this.moveCursorToEnd();
                        return true;
                }
                return false;
            }
        }
    }

    public boolean canConsumeInput() {
        return this.isVisible() && this.isFocused() && this.isEditable();
    }

    public boolean charTyped(CharacterEvent event) {
        if (!this.canConsumeInput()) {
            return false;
        } else if (event.isAllowedChatCharacter()) {
            if (this.isEditable) {
                this.insertText(event.codepointAsString());
            }
            return true;
        } else {
            return false;
        }
    }

    public boolean mouseClicked(MouseButtonEvent event, boolean doubleClick) {
        if (!this.isVisible()) {
            return false;
        } else {
            boolean flag = event.x() >= (double) this.getX() && event.x() < (double) (this.getX() + this.width) && event.y() >= (double) this.getY() && event.y() < (double) (this.getY() + this.height);
            if (this.canLoseFocus) {
                this.setFocus(flag);
            }
            if (this.isFocused() && flag && event.input() == 0) {
                int i = (int) Math.floor(event.x()) - this.getX();
                if (this.bordered) {
                    i -= 4;
                }
                String s = this.font.plainSubstrByWidth(this.value.substring(this.displayPos), this.getInnerWidth());
                this.moveCursorTo(this.font.plainSubstrByWidth(s, i).length() + this.displayPos);
                return true;
            } else {
                return false;
            }
        }
    }

    public void setFocus(boolean focused) {
        this.setFocused(focused);
    }

    public void renderButton(GuiGraphicsExtractor graphics) {
        if (this.isVisible()) {
            if (this.isBordered()) {
                int i = LineColor;
                graphics.fill(this.getX() - 1, this.getY() - 1, this.getX() + this.width + 1, this.getY() + this.height + 1, i);
                graphics.fill(this.getX(), this.getY(), this.getX() + this.width, this.getY() + this.height, -16777216);
            }
            graphics.text(font, tag, getX(), getY() - 10, Palette.WHITE, false);
            int i2 = this.isEditable ? this.textColor : this.textColorUneditable;
            int j = this.cursorPos - this.displayPos;
            int k = this.highlightPos - this.displayPos;
            String s = this.font.plainSubstrByWidth(this.value.substring(this.displayPos), this.getInnerWidth());
            boolean flag = j >= 0 && j <= s.length();
            boolean flag1 = this.isFocused() && this.frame / 6 % 2 == 0 && flag;
            int l = this.bordered ? this.getX() + 4 : this.getX();
            int i1 = this.bordered ? this.getY() + (this.height - 8) / 2 : this.getY();
            int j1 = l;
            if (k > s.length()) {
                k = s.length();
            }
            if (!s.isEmpty()) {
                String s1 = flag ? s.substring(0, j) : s;
                var formatted = this.formatter.apply(s1, this.displayPos);
                graphics.text(font, formatted, l, i1, i2, true);
                j1 = l + font.width(formatted);
            }
            boolean flag2 = this.cursorPos < this.value.length() || this.value.length() >= this.getMaxLength();
            int k1 = j1;
            if (!flag) {
                k1 = j > 0 ? l + this.width : l;
            } else if (flag2) {
                k1 = j1 - 1;
                --j1;
            }
            if (!s.isEmpty() && flag && j < s.length()) {
                var formatted = this.formatter.apply(s.substring(j), this.cursorPos);
                graphics.text(font, formatted, j1, i1, i2, true);
            }
            if (!flag2 && this.suggestion != null) {
                graphics.text(font, this.suggestion, k1 - 1, i1, -8355712, true);
            }
            if (flag1) {
                if (flag2) {
                    graphics.fill(k1, i1 - 1, k1 + 1, i1 + 1 + 9, CURSOR_INSERT_COLOR);
                } else {
                    graphics.text(font, "_", k1, i1, i2, true);
                }
            }
            if (k != j) {
                int l1 = l + this.font.width(s.substring(0, k));
                this.renderHighlight(graphics, k1, i1 - 1, l1 - 1, i1 + 1 + 9);
            }
        }
    }

    public void renderButton(GuiGraphicsExtractor graphics, int x, int y, float partialTicks) {
        this.setX(x);
        this.setY(y + 10);
        renderButton(graphics);
    }

    private void renderHighlight(GuiGraphicsExtractor graphics, int x1, int y1, int x2, int y2) {
        if (x1 < x2) {
            int i = x1;
            x1 = x2;
            x2 = i;
        }
        if (y1 < y2) {
            int j = y1;
            y1 = y2;
            y2 = j;
        }
        if (x2 > this.getX() + this.width) {
            x2 = this.getX() + this.width;
        }
        if (x1 > this.getX() + this.width) {
            x1 = this.getX() + this.width;
        }
        graphics.fill(x1, y1, x2, y2, CURSOR_INSERT_COLOR);
    }

    public void setMaxLength(int maxLength) {
        this.maxLength = maxLength;
        if (this.value.length() > maxLength) {
            this.value = this.value.substring(0, maxLength);
            this.onValueChange(this.value);
        }
    }

    private int getMaxLength() {
        return this.maxLength;
    }

    public int getCursorPosition() {
        return this.cursorPos;
    }

    private boolean isBordered() {
        return this.bordered;
    }

    public void setBordered(boolean bordered) {
        this.bordered = bordered;
    }

    public void setTextColor(int textColor) {
        this.textColor = textColor;
    }

    public void setTextColorUneditable(int textColorUneditable) {
        this.textColorUneditable = textColorUneditable;
    }

    public boolean isMouseOver(double mouseX, double mouseY) {
        return this.visible && mouseX >= (double) this.getX() && mouseX < (double) (this.getX() + this.width) && mouseY >= (double) this.getY() && mouseY < (double) (this.getY() + this.height);
    }

    protected void onFocusedChanged(boolean focused) {
        if (focused) {
            this.frame = 0;
        }
    }

    private boolean isEditable() {
        return this.isEditable;
    }

    public void setEditable(boolean editable) {
        this.isEditable = editable;
    }

    public int getInnerWidth() {
        return this.isBordered() ? this.width - 8 : this.width;
    }

    public void setHighlightPos(int pos) {
        int i = this.value.length();
        this.highlightPos = Math.min(Math.max(pos, 0), i);
        if (this.font != null) {
            if (this.displayPos > i) {
                this.displayPos = i;
            }
            int j = this.getInnerWidth();
            String s = this.font.plainSubstrByWidth(this.value.substring(this.displayPos), j);
            int k = s.length() + this.displayPos;
            if (this.highlightPos == this.displayPos) {
                this.displayPos -= this.font.plainSubstrByWidth(this.value, j, true).length();
            }
            if (this.highlightPos > k) {
                this.displayPos += this.highlightPos - k;
            } else if (this.highlightPos <= this.displayPos) {
                this.displayPos -= this.displayPos - this.highlightPos;
            }
            this.displayPos = Math.min(Math.max(this.displayPos, 0), i);
        }
    }

    public void setCanLoseFocus(boolean canLoseFocus) {
        this.canLoseFocus = canLoseFocus;
    }

    public boolean isVisible() {
        return this.visible;
    }

    public void setVisible(boolean visible) {
        this.visible = visible;
    }

    public void setSuggestion(@Nullable String suggestion) {
        this.suggestion = suggestion;
    }

    public int getScreenX(int pos) {
        return pos > this.value.length() ? this.getX() : this.getX() + this.font.width(this.value.substring(0, pos));
    }

    @Override
    protected void updateWidgetNarration(NarrationElementOutput output) {
        output.add(NarratedElementType.TITLE, Component.translatable("narration.edit_box", this.getValue()));
    }

    @Override
    protected void extractWidgetRenderState(GuiGraphicsExtractor graphics, int mouseX, int mouseY, float delta) {
        renderButton(graphics);
    }
}
