package com.twopeople.game;

import com.twopeople.game.blur.GaussianFilter;

import java.awt.*;
import java.awt.image.BufferStrategy;
import java.awt.image.BufferedImage;
import java.util.HashMap;

public class GameState extends Canvas implements Runnable {
    public static final int WIDTH = 640;
    public static final int HEIGHT = 560;
    public static final int ROUND_RADIUS = 8;
    public static final int FIELD_X = 116;
    public static final int FIELD_Y = 66;
    public static final int CELL_SPACING = 15;
    public static final int OVER_PLAQUE_WIDTH = 401 + 25 * 2;
    public static final int OVER_PLAQUE_HEIGHT = 401 / 2 - 30;

    private boolean running = false;
    private long fps, lastFpsTime;
    private BufferedImage buffer = new BufferedImage(WIDTH, HEIGHT, BufferedImage.TYPE_INT_ARGB);

    private GaussianFilter filter = new GaussianFilter(8);
    private InputHandler input = new InputHandler(this);
    public Level level;

    private boolean over = false;
    public int score = 0;

    public Main main;

    private HashMap<String, Button> buttons = new HashMap<String, Button>();

    public interface ButtonAdapter {
        public void click();
    }

    private class Button {
        public String contents;
        public int x, y;
        public int w, h;
        public boolean hasBorder = true;

        private ButtonAdapter actions;

        private Font font = new Font("Tahoma", Font.PLAIN, 14);
        private FontMetrics m = getFontMetrics(font);

        public Button(String contents, int x, int y, int xp, int yp) {
            this.contents = contents;
            this.x = x;
            this.y = y;

            this.w = xp * 2 + m.stringWidth(contents);
            this.h = yp * 2 + m.getHeight();
        }

        public void addActions(ButtonAdapter actions) {
            this.actions = actions;
        }

        public boolean isOver() {
            return new Rectangle(x, y, w, h).contains(input.getMouseX(), input.getMouseY());
        }

        public boolean isClicked() {
            return input.mouseLeftClicked && isOver();
        }

        private boolean wasOver = false;

        public void update() {
            if (actions != null && isClicked()) {
                actions.click();
            }

            if (isOver()) {
                main.setCursor(Cursor.HAND_CURSOR);
                wasOver = true;
            } else if (wasOver) {
                wasOver = false;
                main.setCursor(Cursor.DEFAULT_CURSOR);
            }
        }

        public void render(Graphics g) {
            g.setFont(font);

            if (hasBorder) {
                g.setColor(new Color(146, 146, 146, 125));
                g.drawRoundRect(x, y, w, h, ROUND_RADIUS, ROUND_RADIUS);
            }

            g.setColor(new Color(146, 146, 146));
            g.drawString(contents, x + w / 2 - m.stringWidth(contents) / 2, y + (h - m.getHeight()) / 2 - m.getHeight() / 5 + m.getDescent() + m.getAscent());
        }
    }

    public GameState(final Main main, int size) {
        setVisible(true);
        Dimension d = new Dimension(WIDTH, HEIGHT);
        setMinimumSize(d);
        setPreferredSize(d);
        setMaximumSize(d);

        this.main = main;
        this.level = new Level(this, size);

        Button dumb = new Button("", 0, 0, 0, 10);
        int y = HEIGHT / 2 - OVER_PLAQUE_HEIGHT / 2 + OVER_PLAQUE_HEIGHT - 15 - dumb.h;
        buttons.put("menu", new Button("Menu", 0, y, 10, 10));
        buttons.put("again", new Button("Try again", 0, y, 10, 10));

        buttons.get("menu").x = FIELD_X - 25 + OVER_PLAQUE_WIDTH / 2 - (buttons.get("menu").w + buttons.get("again").w + 15) / 2;
        buttons.get("again").x = buttons.get("menu").x + buttons.get("menu").w + 15;

        buttons.put("back", new Button("Back", 0, 5, 10, 10) {{
            this.hasBorder = false;
        }});

        buttons.get("menu").addActions(new ButtonAdapter() {
            @Override
            public void click() {
                main.enterState(Main.MENU_STATE);
            }
        });

        buttons.get("again").addActions(new ButtonAdapter() {
            @Override
            public void click() {
                reset();
            }
        });

        buttons.get("back").addActions(new ButtonAdapter() {
            @Override
            public void click() {
                main.back();
            }
        });
    }

    public void start() {
        running = true;
        new Thread(this).start();
    }

    public void stop() {
        running = false;
    }

    @Override
    public void run() {
        long lastLoopTime = System.nanoTime();
        final int TARGET_FPS = 60;
        final long OPTIMAL_TIME = 1000000000 / TARGET_FPS;

        init();

        while (running) {
            long now = System.nanoTime();
            long updateLength = now - lastLoopTime;
            lastLoopTime = now;
            double delta = updateLength / ((double) OPTIMAL_TIME);

            lastFpsTime += updateLength;
            fps++;

            if (lastFpsTime >= 1000000000) {
                //System.out.println("fps: " + fps);
                lastFpsTime = 0;
                fps = 0;
            }

            update(delta);
            render();

            try {
                Thread.sleep((lastLoopTime - System.nanoTime() + OPTIMAL_TIME) / 1000000);
            } catch (Exception e) {
            }
        }
    }

    private void init() {
        requestFocus();
    }

    private void update(double delta) {
        for (Button btn : buttons.values()) {
            btn.update();
        }

        if (level.isOver() && !over) {
            over = true;

            level.resetState();
            main.saveScore(score);

            // update the best result in case if current is better
            if (score > main.bestScore) {
                main.bestScore = score;
            }
        }

        if (!over) {
            if (input.up.isClicked()) {
                level.move(0, -1);
            } else if (input.down.isClicked()) {
                level.move(0, 1);
            } else if (input.left.isClicked()) {
                level.move(-1, 0);
            } else if (input.right.isClicked()) {
                level.move(1, 0);
            }
        }

        level.update(delta);
        input.update();
    }


    private void render() {
        BufferStrategy bs = getBufferStrategy();
        if (bs == null) {
            createBufferStrategy(2);
            return;
        }
        Graphics bg = buffer.getGraphics();

        Graphics2D g2d = (Graphics2D) bg;
        g2d.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
        g2d.setRenderingHint(RenderingHints.KEY_TEXT_ANTIALIASING, RenderingHints.VALUE_TEXT_ANTIALIAS_LCD_HRGB);

        bg.setColor(new Color(238, 238, 238));
        bg.fillRect(0, 0, getWidth(), getHeight());

        bg.setColor(Color.white);
        bg.fillRoundRect(FIELD_X, FIELD_Y, 401, 401, ROUND_RADIUS, ROUND_RADIUS);

        drawScore(bg, FIELD_X + 401 / 2 - 25 - 160, score, "score");

        if (main.bestScore > 0) {
            drawScore(bg, FIELD_X + 401 / 2 - 150 + 70, main.bestScore, "best");
        }

        level.render(bg);
        buttons.get("back").render(bg);

        if (over) {
            bg.drawImage(filter.filter(buffer, null), 0, 0, null);
            setOpacity(bg, 0.65f);
            bg.setColor(Color.black);
            bg.fillRect(0, 0, WIDTH, HEIGHT);
            setOpacity(bg, 1.0f);

            int oy = HEIGHT / 2 - OVER_PLAQUE_HEIGHT / 2;
            int ow = 401 + 25 * 2;

            bg.setColor(Color.white);
            bg.fillRoundRect(FIELD_X - 25, oy, 401 + 25 * 2, OVER_PLAQUE_HEIGHT, ROUND_RADIUS, ROUND_RADIUS);

            bg.setColor(new Color(174, 174, 174));

            Font fEarned = new Font("Tahoma", Font.PLAIN, 15);
            FontMetrics me = bg.getFontMetrics(fEarned);
            String earned = "You have earned";
            int we = me.stringWidth(earned);

            bg.setFont(fEarned);
            bg.drawString(earned, FIELD_X - 25 + ow / 2 - we / 2, oy + 15 + me.getAscent() + me.getDescent());

            Font fScore = new Font("Tahoma", Font.BOLD, 38);
            FontMetrics ms = bg.getFontMetrics(fScore);
            String sc = "" + score;
            int ws = ms.stringWidth(sc);

            bg.setFont(fScore);
            bg.drawString(sc, FIELD_X - 25 + ow / 2 - ws / 2, oy + me.getAscent() + me.getDescent() + me.getHeight() + 50);

            buttons.get("menu").render(bg);
            buttons.get("again").render(bg);
        }

        try {
            Graphics g = bs.getDrawGraphics();
            g.drawImage(buffer, 0, 0, getWidth(), getHeight(), null);
            bs.show();
            g.dispose();
        } catch (Exception e) {
        }
    }

    private void drawScore(Graphics bg, int xo, int sc, String caption) {
        bg.setColor(new Color(174, 174, 174));

        Font fBold = new Font("Tahoma", Font.BOLD, 24);
        Font fPlain = new Font("Tahoma", Font.PLAIN, 12);
        FontMetrics mbold = bg.getFontMetrics(fBold);
        FontMetrics mplain = bg.getFontMetrics(fPlain);

        String sVal1 = "" + sc;
        String sVal2 = caption;

        int sw1 = mbold.stringWidth(sVal1);
        int sw2 = mplain.stringWidth(sVal2);

        bg.setFont(fBold);
        bg.drawString(sVal1, FIELD_X + xo + sw2 / 2 - sw1 / 2, 24 + 10);

        bg.setFont(fPlain);
        bg.drawString(sVal2, FIELD_X + xo, 24 + 10 + 16);
    }

    public void reset() {
        over = false;
        score = 0;
        level.restart();
    }

    public static void setOpacity(Graphics g, float opacity) {
        ((Graphics2D) g).setComposite(AlphaComposite.getInstance(AlphaComposite.SRC_OVER, opacity));
    }
}