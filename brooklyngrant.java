import java.awt.geom.*;
import java.awt.*;
import java.util.*;
import java.util.concurrent.TimeUnit;
import java.awt.image.BufferedImage;
import javax.imageio.ImageIO;
import java.io.File;
import java.io.IOException;

/**
 * brooklyngrant (onyx users)
 */
public class brooklyngrant extends ClobberBot {
    private BufferedImage myImage;
    private Map<BotPoint2D, Point2D> previousPositions = new HashMap<>();
    
    /**
     * Constructor
     * @param game
     */
    public brooklyngrant(Clobber game) {
        super(game);

        // load death star imageas avatar
        try {
            myImage = ImageIO.read(new File("deathstar.png"));
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    ClobberBotAction currAction;

    private final int SAFE_DISTANCE = 50; 
    private final int SHOOT_DISTANCE = 150; 

    /**
     * Plan moves
     * Execution time is less than 1/100 of a second
     */
    public ClobberBotAction takeTurn(WhatIKnow currState) {        
        
        long startTime = System.currentTimeMillis();
        
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
    
        updatePreviousPositions(bots); //update past bots position
    
        if (currAction == null || ((currAction.getAction() & ClobberBotAction.SHOOT) > 0) || rand.nextInt(10) > 8) {
            // random logic - better to shoot then to not!
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

        long estimatedTime = System.currentTimeMillis() - startTime;
        if (estimatedTime > 10) {
            System.out.println("The execution time was longer than 1/100 of a second.");
        }

        return currAction;
    }
    
    /**
     * Update the previous positions of bots - this 
     * is used to calculate velocities and future 
     * positions
     * @param bots
     */
    private void updatePreviousPositions(Vector<BotPoint2D> bots) {
        for (BotPoint2D bot : bots) {
            previousPositions.put(bot, new Point2D.Double(bot.getX(), bot.getY()));
        }
    }    

    /**
     * Locate the nearest bullet to dodge
     * @param me
     * @param bullets
     * @return
     */
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

    /**
     * Move from bullet!
     * @param me
     * @param bullet
     * @return
     */
    private ClobberBotAction moveAwayFromBullet(Point2D me, BulletPoint2D bullet) {
        double dx = me.getX() - bullet.getX();
        double dy = me.getY() - bullet.getY();
        if (Math.abs(dx) > Math.abs(dy)) {
            return new ClobberBotAction(1, dx > 0 ? ClobberBotAction.RIGHT : ClobberBotAction.LEFT);
        } else {
            return new ClobberBotAction(1, dy > 0 ? ClobberBotAction.DOWN : ClobberBotAction.UP);
        }
    }

    /**
     * Find the closest bot to us
     * @param me
     * @param bots
     * @return
     */
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

    /**
     * Shoot at bot
     * @param me
     * @param bot
     * @return
     */
    private ClobberBotAction shootAtBot(Point2D me, BotPoint2D bot) {
        Point2D previousPosition = previousPositions.get(bot);
        double botVelocityX = 0;
        double botVelocityY = 0;
    
        // calculate velocity of bot if we have old positions to predict moves
        if (previousPosition != null) {
            botVelocityX = bot.getX() - previousPosition.getX();
            botVelocityY = bot.getY() - previousPosition.getY();
        }
    
        double distance = me.distance(bot);
        double timeToImpact = distance;
    
        // predict the bot's future position
        double predictedX = bot.getX() + botVelocityX * timeToImpact;
        double predictedY = bot.getY() + botVelocityY * timeToImpact;
    
        // shoot this way
        double predictDx = predictedX - me.getX();
        double predictDy = predictedY - me.getY();
    
        if (Math.abs(predictDx) > Math.abs(predictDy)) {
            return new ClobberBotAction(1, predictDx > 0 ? (ClobberBotAction.SHOOT | ClobberBotAction.RIGHT) : (ClobberBotAction.SHOOT | ClobberBotAction.LEFT));
        } else {
            return new ClobberBotAction(1, predictDy > 0 ? (ClobberBotAction.SHOOT | ClobberBotAction.DOWN) : (ClobberBotAction.SHOOT | ClobberBotAction.UP));
        }
    }
    

    /**
     * Draw avatar
     */
    public void drawMe(Graphics page, Point2D me) {
        int x, y;
        x = (int) me.getX() - Clobber.MAX_BOT_GIRTH / 2 - 1;
        y = (int) me.getY() - Clobber.MAX_BOT_GIRTH / 2 - 1;

        if (myImage != null) {
            page.drawImage(myImage, x, y, Clobber.MAX_BOT_GIRTH, Clobber.MAX_BOT_GIRTH, null);
        }
    }

    public String toString() {
        return "Brooklyn Grant and Megan Aker";
    }
}
