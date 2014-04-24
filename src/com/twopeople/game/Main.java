package com.twopeople.game;

import com.twopeople.game.cp.PathManager;

import javax.imageio.ImageIO;
import javax.swing.*;
import java.awt.*;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.io.BufferedWriter;
import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.Date;
import java.util.HashMap;
import java.util.Scanner;
import java.util.Stack;

public class Main extends JFrame {
    public static final int WIDTH = 640;
    public static final int HEIGHT = 560;

    public static final int MENU_STATE = 2;
    public static final int GAME_STATE = 3;
    public static final int LEADERBOARD_STATE = 4;
    public static final int MODE_SELECTION_STATE = 7;

    private class HistoryState {
        public int state;
        public Object[] params;
    }

    public int bestScore;

    public static PathManager pm = new PathManager();

    private Stack<HistoryState> statesHistory = new Stack<HistoryState>();

    private ActionListener backAction = new ActionListener() {
        @Override
        public void actionPerformed(ActionEvent e) {
            back();
        }
    };

    private abstract class GUIManager {
        public void addElements(Container pane) {}

        public void addElements(Container pane, Object[] params) {}
    }

    private HashMap<Integer, GUIManager> managers = new HashMap<Integer, GUIManager>() {{
        put(MENU_STATE, new GUIManager() {
            @Override
            public void addElements(Container pane) {
                for (Score score : getScores()) {
                    if (score.score > bestScore) {
                        bestScore = score.score;
                    }
                }

                //

                pane.setLayout(new GridBagLayout());
                GridBagConstraints gbc = new GridBagConstraints();

                JLabel label = new JLabel("");
                label.setForeground(new Color(97, 97, 97));
                label.setFont(new Font("Tahoma", Font.BOLD, 32));
                try {
                    Image image = ImageIO.read(getClass().getResourceAsStream("/logotype.png"));
                    label.setIcon(new ImageIcon(image));
                } catch (IOException e) {
                    e.printStackTrace();
                }
                gbc.gridy = 1;
                gbc.insets = new Insets(0, 0, 10, 0);
                pane.add(label, gbc);

                gbc.gridy = 2;
                gbc.insets = new Insets(0, 0, 50, 0);
                pane.add(UI.createSign("Join tiles to get to the 2048!", 13, false), gbc);

                gbc.insets = new Insets(0, 0, 0, 0);

                int gy = 3;
                if (hasSavedState()) {
                    JButton button = UI.createSimpleButton("Continue", 0, 0, 1, 0, 50);
                    button.addActionListener(new ActionListener() {
                        @Override
                        public void actionPerformed(ActionEvent event) {
                            enterState(GAME_STATE, new Object[]{0});
                        }
                    });
                    gbc.gridy = gy++;
                    pane.add(button, gbc);
                }

                JButton button = UI.createSimpleButton("New game", 0, 0, 1, 0, 50);
                button.addActionListener(new ActionListener() {
                    @Override
                    public void actionPerformed(ActionEvent event) {
                        enterState(MODE_SELECTION_STATE);
                    }
                });
                gbc.gridy = gy++;
                pane.add(button, gbc);

                JButton button2 = UI.createSimpleButton("Highest scores", 0, 0, 0, 0, 45);
                button2.addActionListener(new ActionListener() {
                    @Override
                    public void actionPerformed(ActionEvent event) {
                        enterState(LEADERBOARD_STATE);
                    }
                });
                gbc.gridy = gy++;
                pane.add(button2, gbc);
            }
        });

        put(MODE_SELECTION_STATE, new GUIManager() {
            @Override
            public void addElements(Container pane) {
                pane.setLayout(new BoxLayout(pane, BoxLayout.Y_AXIS));
                GridBagConstraints gbc = new GridBagConstraints();

                JPanel top = new JPanel();
                top.setLayout(new GridBagLayout());
                top.setMaximumSize(new Dimension(WIDTH, 60));
                top.setBorder(BorderFactory.createCompoundBorder(BorderFactory.createMatteBorder(0, 0, 1, 0, new Color(174, 174, 174, 75)), BorderFactory.createEmptyBorder(0, 0, 8, 0)));

                JButton back = UI.createSimpleButton("Back", 0, 0, 0, 0, 10);
                back.addActionListener(backAction);
                gbc.anchor = GridBagConstraints.WEST;
                gbc.weightx = 1;
                gbc.insets = new Insets(5, 0, 0, 0);
                top.add(back, gbc);

                JLabel label = new JLabel("Select mode", JLabel.CENTER);
                label.setBorder(BorderFactory.createEmptyBorder(0, 0, 0, 45));
                label.setForeground(new Color(174, 174, 174));
                label.setFont(new Font("Tahoma", Font.BOLD, 15));
                gbc.gridx = 1;
                gbc.insets = new Insets(8, 0, 0, 0);
                top.add(label, gbc);

                pane.add(top);

                gbc.insets = new Insets(0, 0, 0, 0);

                ActionListener listener = new ActionListener() {
                    @Override
                    public void actionPerformed(ActionEvent event) {
                        enterState(GAME_STATE, new Object[]{Integer.parseInt(event.getActionCommand())});
                    }
                };

                JPanel c = new JPanel();
                c.setLayout(new GridBagLayout());

                gbc.weightx = 0;
                gbc.gridx = 0;

                JButton button = UI.createSimpleButton("3x3", 0, 0, 1, 0, 50);
                button.setActionCommand("3");
                button.addActionListener(listener);
                gbc.gridy = 0;
                c.add(button, gbc);

                JButton button2 = UI.createSimpleButton("4x4", 0, 0, 1, 0, 50);
                button2.setActionCommand("4");
                button2.addActionListener(listener);
                gbc.gridy = 1;
                c.add(button2, gbc);

                JButton button3 = UI.createSimpleButton("5x5", 0, 0, 0, 0, 50);
                button3.setActionCommand("5");
                button3.addActionListener(listener);
                gbc.gridy = 2;
                c.add(button3, gbc);

                pane.add(c);
            }
        });

        put(LEADERBOARD_STATE, new GUIManager() {
            @Override
            public void addElements(Container pane) {
                pane.setLayout(new BoxLayout(pane, BoxLayout.Y_AXIS));
                GridBagConstraints gbc = new GridBagConstraints();

                JPanel top = new JPanel();
                top.setLayout(new GridBagLayout());
                top.setMaximumSize(new Dimension(WIDTH, 60));
                top.setBorder(BorderFactory.createCompoundBorder(BorderFactory.createMatteBorder(0, 0, 1, 0, new Color(174, 174, 174, 75)), BorderFactory.createEmptyBorder(0, 0, 8, 0)));

                JButton back = UI.createSimpleButton("Back", 0, 0, 0, 0, 10);
                back.addActionListener(backAction);
                gbc.anchor = GridBagConstraints.WEST;
                gbc.weightx = 1;
                gbc.insets = new Insets(5, 0, 0, 0);
                top.add(back, gbc);

                JLabel label = new JLabel("Your highest scores", JLabel.CENTER);
                label.setBorder(BorderFactory.createEmptyBorder(0, 0, 0, 50));
                label.setForeground(new Color(174, 174, 174));
                label.setFont(new Font("Tahoma", Font.BOLD, 15));
                gbc.gridx = 1;
                gbc.insets = new Insets(8, 0, 0, 0);
                top.add(label, gbc);

                pane.add(top);

                gbc.gridx = 1;
                gbc.gridwidth = 1;

                ArrayList<Score> scores = getScores();

                if (scores.size() > 0) {
                    Date currentDate = new Date();
                    DateFormat todayFormatter = new SimpleDateFormat("HH:mm");
                    DateFormat formatter = new SimpleDateFormat("dd.MM HH:mm");

                    Object[][] data = new Object[scores.size()][3];
                    int index = 0;
                    for (Score score : scores) {
                        Date date = new Date(score.date);

                        String sDate = "";

                        if (currentDate.getDate() == date.getDate() && currentDate.getMonth() == date.getMonth()) {
                            sDate = todayFormatter.format(date);
                        } else {
                            sDate = formatter.format(date);
                        }

                        data[index++] = new Object[]{
                                index, score.score, sDate
                        };
                    }

                    final JTable table = UI.createResultTable(data, new String[]{"#", "Score", "Date"}, new int[]{100, 400, 150}, new UI.TableCellCallback() {
                        @Override
                        public void click(JTable table, int row, int col) {
                        }
                    });

                    table.setRowSelectionAllowed(false);

                    JPanel p = new JPanel();
                    p.setLayout(new BoxLayout(p, BoxLayout.Y_AXIS));

                    table.setPreferredSize(new Dimension(WIDTH, HEIGHT - 60));
                    p.add(table.getTableHeader());
                    p.add(table);

                    JScrollPane scroll = new JScrollPane(p);
                    scroll.setBorder(BorderFactory.createEmptyBorder(0, 0, 0, 0));
                    scroll.setHorizontalScrollBarPolicy(JScrollPane.HORIZONTAL_SCROLLBAR_NEVER);

                    pane.add(scroll);
                } else {
                    JPanel p = new JPanel();
                    p.setLayout(new GridBagLayout());

                    p.add(UI.createSign("No records yet", 15, false));

                    pane.add(p);
                }
            }
        });

        put(GAME_STATE, new GUIManager() {
            @Override
            public void addElements(Container pane, Object[] params) {
                int size = (Integer) params[0];
                GameState game = new GameState(Main.this, size);
                pane.add(game);
                game.start();
            }
        });
    }};

    public Main() {
        super("J2048");

        try {
            UIManager.setLookAndFeel("com.sun.java.swing.plaf.windows.WindowsLookAndFeel");
            setIconImage(ImageIO.read(getClass().getResource("/icon.png")));
        } catch (Exception ex) {
            ex.printStackTrace();
        }
    }

    public void createAndShowGUI() {
        setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        setResizable(false);
        Dimension d = new Dimension(WIDTH, HEIGHT);
        setMinimumSize(d);
        setPreferredSize(d);
        setMaximumSize(d);

        enterState(MENU_STATE);

        pack();
        setVisible(true);
    }

    public void enterState(final int s, final Object[] p) {
        Container pane = getContentPane();

        pane.removeAll();
        pane.repaint();

        GUIManager manager = managers.get(s);
        manager.addElements(pane);
        manager.addElements(pane, p);

        statesHistory.push(new HistoryState() {{
            this.state = s;
            this.params = p;
        }});

        validate();
    }

    public void enterState(int state) {
        enterState(state, null);
    }

    public void back() {
        statesHistory.pop();
        HistoryState prevState = statesHistory.pop();
        if (prevState != null) {
            enterState(prevState.state, prevState.params);
        }
    }

    public class Score {
        public int score;
        public long date;
    }

    public ArrayList<Score> getScores() {
        ArrayList<Score> scores = new ArrayList<Score>();

        try {
            Scanner scanner = new Scanner(pm.getAbsolutePathForFile("scores.dat"));
            while (scanner.hasNextLine()) {
                final String[] parts = scanner.nextLine().split(" ");
                scores.add(new Score() {{
                    this.score = Integer.parseInt(parts[0]);
                    this.date = Long.parseLong(parts[1]);
                }});
            }
            scanner.close();
        } catch (Exception e) {
            e.printStackTrace();
        }

        Collections.sort(scores, new Comparator<Score>() {
            @Override
            public int compare(Score o1, Score o2) {
                if (o1.score > o2.score) { return -1; }
                if (o1.score < o2.score) { return 1; }
                return 0;
            }
        });

        return scores;
    }

    public void saveScore(int score) {
        try {
            BufferedWriter bw = new BufferedWriter(new FileWriter(pm.getAbsolutePathForFile("scores.dat"), true));
            bw.write(score + " " + System.currentTimeMillis());
            bw.newLine();
            bw.flush();
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    public static boolean hasSavedState() {
        File file = pm.getAbsolutePathForFile("game.dat");
        return file.length() > 0;
    }

    public static void main(String[] args) {
        SwingUtilities.invokeLater(new Runnable() {
            @Override
            public void run() {
                new Main().createAndShowGUI();
            }
        });
    }
}