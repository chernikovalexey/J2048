package com.twopeople.game;

import javax.swing.*;
import javax.swing.border.Border;
import javax.swing.border.CompoundBorder;
import javax.swing.border.EmptyBorder;
import javax.swing.table.DefaultTableCellRenderer;
import javax.swing.table.DefaultTableModel;
import java.awt.*;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;

/**
 * Created by Alexey
 * At 11:19 AM on 4/23/14
 */

public class UI {
    public static interface TableCellCallback {
        public void click(JTable table, int row, int col);
    }

    public static JButton createSimpleButton(String text, int t, int r, int b, int l, int xmargin) {
        Border b1 = BorderFactory.createMatteBorder(t, r, b, l, new Color(174, 174, 174, 125));
        Border b2 = new EmptyBorder(10, xmargin, 10, xmargin);

        final Color idle = new Color(146, 146, 146);
        final Color rollover = new Color(97, 97, 97);

        final JButton button = new JButton(text);

        button.setFont(new Font("Tahoma", Font.PLAIN, 14));
        button.setForeground(new Color(146, 146, 146));
        button.setBorder(new CompoundBorder(b1, b2));
        button.setFocusPainted(false);
        button.setContentAreaFilled(false);
        button.setCursor(new Cursor(Cursor.HAND_CURSOR));

        button.addMouseListener(new MouseAdapter() {
            @Override
            public void mouseEntered(MouseEvent e) {
                button.setForeground(rollover);
            }

            @Override
            public void mouseExited(MouseEvent e) {
                button.setForeground(idle);
            }
        });

        return button;
    }

    public static JTable createResultTable(Object[][] data, String[] header, int[] cols, final TableCellCallback callback) {
        final JTable table = new JTable(new DefaultTableModel(data, header)) {
            @Override
            public boolean isCellEditable(int row, int column) {
                return false;
            }
        };

        table.getTableHeader().setDefaultRenderer(new DefaultTableCellRenderer() {
            @Override
            public Component getTableCellRendererComponent(JTable table, Object value, boolean isSelected, boolean hasFocus, int row, int column) {
                Component result = super.getTableCellRendererComponent(table, value, isSelected, hasFocus, row, column);

                if (result instanceof JLabel) {
                    JLabel label = (JLabel) result;
                    label.setFont(new Font("Tahoma", Font.BOLD, 12));
                    label.setHorizontalAlignment(SwingConstants.CENTER);
                    Border b = BorderFactory.createCompoundBorder(BorderFactory.createMatteBorder(0, 0, 1, 0, new Color(174, 174, 174, 75)), BorderFactory.createEmptyBorder(10, 10, 10, 10));
                    label.setBorder(b);
                }

                return result;
            }
        });
        table.getTableHeader().setResizingAllowed(false);

        table.setDefaultRenderer(Object.class, new DefaultTableCellRenderer() {
            @Override
            public Component getTableCellRendererComponent(JTable table, Object value, boolean isSelected, boolean hasFocus, int row, int column) {
                Component result = super.getTableCellRendererComponent(table, value, isSelected, hasFocus, row, column);

                if (result instanceof JLabel) {
                    JLabel label = (JLabel) result;
                    label.setFont(new Font("Tahoma", Font.PLAIN, 14));
                    label.setHorizontalAlignment(SwingConstants.CENTER);
                }

                return result;
            }
        });
        table.setRowHeight(35);
        table.setSelectionMode(ListSelectionModel.SINGLE_SELECTION);
        table.setShowGrid(false);

        JPanel pane = new JPanel();
        pane.setLayout(new BoxLayout(pane, BoxLayout.Y_AXIS));

        table.getTableHeader().setReorderingAllowed(false);

        for (int i = 0; i < cols.length; ++i) {
            table.getColumnModel().getColumn(i).setMaxWidth(cols[i]);
        }

        table.addMouseListener(new MouseAdapter() {
            @Override
            public void mouseClicked(MouseEvent event) {
                int row = table.rowAtPoint(event.getPoint());
                int col = table.columnAtPoint(event.getPoint());
                callback.click(table, row, col);
            }
        });

        return table;
    }

    public static JLabel createSign(String text, int size, boolean bold) {
        final Font font = new Font("Tahoma", bold ? Font.BOLD : Font.PLAIN, size);
        return new JLabel(text) {{
            setFont(font);
            setForeground(new Color(174, 174, 174));
        }};
    }
}