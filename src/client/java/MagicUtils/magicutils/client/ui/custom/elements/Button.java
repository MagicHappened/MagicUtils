package MagicUtils.magicutils.client.ui.custom.elements;


import net.minecraft.client.gui.widget.ButtonWidget;
import net.minecraft.text.Text;

public class Button extends ButtonWidget {

    public Button(int x, int y, int width, int height, int number, PressAction onPress) {
        super(x, y, width, height, Text.literal(String.valueOf(number)), onPress, DEFAULT_NARRATION_SUPPLIER);
    }
}
