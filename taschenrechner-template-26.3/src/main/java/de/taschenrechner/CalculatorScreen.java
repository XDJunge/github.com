package de.taschenrechner;

import java.math.BigDecimal;

import net.minecraft.client.gui.components.Button;
import net.minecraft.client.gui.components.EditBox;
import net.minecraft.client.gui.screens.Screen;
import net.minecraft.network.chat.Component;

public class CalculatorScreen extends Screen {
    private EditBox display;
    private String current = "0";
    private double stored = 0;
    private char op = 0;
    private boolean fresh = true;

    public CalculatorScreen() {
        super(Component.literal("Taschenrechner"));
    }

    @Override
    protected void init() {
        int bw = 40, bh = 24, gap = 4;
        int w = 4 * bw + 3 * gap;
        int totalH = 22 + gap + 5 * bh + 4 * gap;
        int left = (this.width - w) / 2;
        int top = (this.height - totalH) / 2;

        display = new EditBox(this.font, left, top, w, 22, Component.literal("Anzeige"));
        display.setMaxLength(32);
        display.setEditable(false);
        display.setValue(current);
        addRenderableWidget(display);

        int y = top + 22 + gap;

        addRenderableWidget(Button.builder(Component.literal("C"), b -> clear())
                .bounds(left, y, w, bh).build());

        String[][] rows = {
                {"7", "8", "9", "÷"},
                {"4", "5", "6", "×"},
                {"1", "2", "3", "-"},
                {"0", ",", "=", "+"}
        };
        for (int r = 0; r < rows.length; r++) {
            for (int c = 0; c < 4; c++) {
                String label = rows[r][c];
                int x = left + c * (bw + gap);
                int by = y + (r + 1) * (bh + gap);
                addRenderableWidget(Button.builder(Component.literal(label), b -> press(label))
                        .bounds(x, by, bw, bh).build());
            }
        }
    }

    private void press(String label) {
        char ch = label.charAt(0);
        if (Character.isDigit(ch)) {
            if (fresh || current.equals("0")) {
                current = label;
                fresh = false;
            } else if (current.length() < 15) {
                current += label;
            }
        } else if (ch == ',') {
            if (fresh) {
                current = "0.";
                fresh = false;
            } else if (!current.contains(".")) {
                current += ".";
            }
        } else if (ch == '=') {
            if (op != 0) {
                calculate();
                op = 0;
            }
        } else {
            if (op != 0 && !fresh) {
                calculate();
            } else {
                stored = Double.parseDouble(current);
            }
            op = ch;
            fresh = true;
        }
        show();
    }

    private void calculate() {
        double b = Double.parseDouble(current);
        double result;
        switch (op) {
            case '+' -> result = stored + b;
            case '-' -> result = stored - b;
            case '×' -> result = stored * b;
            case '÷' -> {
                if (b == 0) {
                    error();
                    return;
                }
                result = stored / b;
            }
            default -> result = b;
        }
        stored = result;
        current = format(result);
        fresh = true;
    }

    private void error() {
        current = "0";
        stored = 0;
        op = 0;
        fresh = true;
        display.setValue("Fehler");
    }

    private void clear() {
        current = "0";
        stored = 0;
        op = 0;
        fresh = true;
        show();
    }

    private void show() {
        display.setValue(current.replace('.', ','));
    }

    private static String format(double v) {
        if (v == Math.rint(v) && Math.abs(v) < 1e15) {
            return String.valueOf((long) v);
        }
        String s = BigDecimal.valueOf(v).stripTrailingZeros().toPlainString();
        return s.length() > 16 ? s.substring(0, 16) : s;
    }

    @Override
    public boolean isPauseScreen() {
        return false;
    }
}
