package uk.chromis.pos.inventory.ui;

import java.awt.Color;
import java.awt.Component;
import java.awt.Cursor;
import javax.swing.BorderFactory;
import javax.swing.DefaultListCellRenderer;
import javax.swing.JButton;
import javax.swing.JComboBox;
import javax.swing.JList;

/**
 * Helpers de UI compartidos por los paneles CRUD de administración
 * (Productos, Categorías, Impuestos), que repetían este mismo código.
 */
public final class CrudUiUtils {

    private CrudUiUtils() {
    }

    public static JButton makeBtn(String text, Color bg) {
        JButton b = new JButton(text);
        b.setBackground(bg);
        b.setForeground(Color.WHITE);
        b.setFocusPainted(false);
        b.setBorder(BorderFactory.createEmptyBorder(6, 12, 6, 12));
        b.setCursor(Cursor.getPredefinedCursor(Cursor.HAND_CURSOR));
        return b;
    }

    /**
     * Combo cuyos items son pares {id, etiquetaVisible} (String[2]), mostrando solo la etiqueta.
     */
    @SuppressWarnings("unchecked")
    public static JComboBox<String[]> newIdLabelCombo() {
        JComboBox<String[]> combo = new JComboBox<>();
        combo.setRenderer(new DefaultListCellRenderer() {
            @Override
            public Component getListCellRendererComponent(JList<?> list, Object value, int index,
                                                            boolean isSelected, boolean cellHasFocus) {
                super.getListCellRendererComponent(list, value, index, isSelected, cellHasFocus);
                if (value instanceof String[]) {
                    setText(((String[]) value)[1]);
                }
                return this;
            }
        });
        return combo;
    }
}
