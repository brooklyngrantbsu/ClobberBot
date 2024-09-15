import java.awt.geom.*;
import java.awt.*;
import java.util.*;
import java.awt.image.BufferedImage;
import javax.imageio.ImageIO;
import java.io.File;
import java.io.IOException;

public class brooklyngrant extends ClobberBot {
    private BufferedImage myImage;
    
    public brooklyngrant(Clobber game) {
        super(game);

        try {
            myImage = ImageIO.read(new File("deathstar.png"));
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    ClobberBotAction currAction;

    private final int SAFE_DISTANCE = 50; 
    private final int SHOOT_DISTANCE = 150; 

    public ClobberBotAction takeTurn(WhatIKnow currState) {
        Point2D me = currState.me;
        Vector<BulletPoint2D> bullets = currState.bullets;
        Vector<BotPoint2D> bots = currState.bots;

        BulletPoint2D nearestBullet = findNearestBullet(me, bullets);
        if (nearestBullet != null && me.distance(nearestBullet) < SAFE_DISTANCE) {
            currAction = moveAwayFromBullet(me, nearestBullet);
            return currAction;
        }

        BotPoint2D nearestBot = findNearestBot(me, bots);
        if (nearestBot != null && me.distance(nearestBot) < SHOOT_DISTANCE) {
            currAction = shootAtBot(me, nearestBot);
            return currAction;
        }

        if (currAction == null || ((currAction.getAction() & ClobberBotAction.SHOOT) > 0) || rand.nextInt(10) > 8) {
            switch (rand.nextInt(8)) {
                case 0: currAction = new ClobberBotAction(rand.nextInt(2) + 1, ClobberBotAction.UP); break;
                case 1: currAction = new ClobberBotAction(rand.nextInt(2) + 1, ClobberBotAction.DOWN); break;
                case 2: currAction = new ClobberBotAction(rand.nextInt(2) + 1, ClobberBotAction.LEFT); break;
                case 3: currAction = new ClobberBotAction(rand.nextInt(2) + 1, ClobberBotAction.RIGHT); break;
                case 4: currAction = new ClobberBotAction(rand.nextInt(2) + 1, ClobberBotAction.UP | ClobberBotAction.LEFT); break;
                case 5: currAction = new ClobberBotAction(rand.nextInt(2) + 1, ClobberBotAction.UP | ClobberBotAction.RIGHT); break;
                case 6: currAction = new ClobberBotAction(rand.nextInt(2) + 1, ClobberBotAction.DOWN | ClobberBotAction.LEFT); break;
                default: currAction = new ClobberBotAction(rand.nextInt(2) + 1, ClobberBotAction.DOWN | ClobberBotAction.RIGHT); break;
            }
        }
        return currAction;
    }

    private BulletPoint2D findNearestBullet(Point2D me, Vector<BulletPoint2D> bullets) {
        BulletPoint2D nearest = null;
        double minDistance = Double.MAX_VALUE;
        for (BulletPoint2D bullet : bullets) {
            double distance = me.distance(bullet);
            if (distance < minDistance) {
                minDistance = distance;
                nearest = bullet;
            }
        }
        return nearest;
    }

    private ClobberBotAction moveAwayFromBullet(Point2D me, BulletPoint2D bullet) {
        double dx = me.getX() - bullet.getX();
        double dy = me.getY() - bullet.getY();
        if (Math.abs(dx) > Math.abs(dy)) {
            return new ClobberBotAction(1, dx > 0 ? ClobberBotAction.RIGHT : ClobberBotAction.LEFT);
        } else {
            return new ClobberBotAction(1, dy > 0 ? ClobberBotAction.DOWN : ClobberBotAction.UP);
        }
    }

    private BotPoint2D findNearestBot(Point2D me, Vector<BotPoint2D> bots) {
        BotPoint2D nearest = null;
        double minDistance = Double.MAX_VALUE;
        for (BotPoint2D bot : bots) {
            double distance = me.distance(bot);
            if (distance < minDistance) {
                minDistance = distance;
                nearest = bot;
            }
        }
        return nearest;
    }

    // private ClobberBotAction shootAtBot(Point2D me, BotPoint2D bot) {
    //     double dx = bot.getX() - me.getX();
    //     double dy = bot.getY() - me.getY();
    //     if (Math.abs(dx) > Math.abs(dy)) {
    //         return new ClobberBotAction(1, dx > 0 ? ClobberBotAction.SHOOT_RIGHT : ClobberBotAction.SHOOT_LEFT);
    //     } else {
    //         return new ClobberBotAction(1, dy > 0 ? ClobberBotAction.SHOOT_DOWN : ClobberBotAction.SHOOT_UP);
    //     }
    // }

    private ClobberBotAction shootAtBot(Point2D me, BotPoint2D bot) {
        double dx = bot.getX() - me.getX();
        double dy = bot.getY() - me.getY();
        if (Math.abs(dx) > Math.abs(dy)) {
            return new ClobberBotAction(1, dx > 0 ? (ClobberBotAction.SHOOT | ClobberBotAction.RIGHT) : (ClobberBotAction.SHOOT | ClobberBotAction.LEFT));
        } else {
            return new ClobberBotAction(1, dy > 0 ? (ClobberBotAction.SHOOT | ClobberBotAction.DOWN) : (ClobberBotAction.SHOOT | ClobberBotAction.UP));
        }
    }
    

    public void drawMe(Graphics page, Point2D me) {
        int x, y;
        x = (int) me.getX() - Clobber.MAX_BOT_GIRTH / 2 - 1;
        y = (int) me.getY() - Clobber.MAX_BOT_GIRTH / 2 - 1;

        if (myImage != null) {
            page.drawImage(myImage, x, y, Clobber.MAX_BOT_GIRTH, Clobber.MAX_BOT_GIRTH, null);
        }
    }

    public String toString() {
        return "Brooklyn Grant";
    }
}
