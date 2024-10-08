import java.awt.geom.*;
import java.awt.*;
import java.util.*;
import java.awt.image.BufferedImage;
import javax.imageio.ImageIO;
import java.io.File;
import java.io.IOException;

/**
 * brooklyngrant (onyx username)
 * By Brooklyn Grant and Megan Aker
 */
public class brooklyngrant extends ClobberBot {
    private BufferedImage myImage;
    private Map<BotPoint2D, Point2D> previousPositions = new HashMap<>();
    ClobberBotAction currAction;

    private final int SAFE_DISTANCE = 30; 
    private final int SHOOT_DISTANCE = 200; 
    
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

    /**
     * Plan moves
     * Execution time is less than 1/100 of a second
     */
    public ClobberBotAction takeTurn(WhatIKnow currState) {        
        
        long startTime = System.currentTimeMillis(); // starting point of take turn logic
        
        Point2D me = currState.me;
        Vector<BulletPoint2D> bullets = currState.bullets;
        Vector<BotPoint2D> bots = currState.bots;
        
        updatePreviousPositions(bots); //update past bots position
    
        BulletPoint2D nearestBullet = findNearestBullet(me, bullets);
        if (nearestBullet != null && me.distance(nearestBullet) < SAFE_DISTANCE) {
            currAction = moveAwayFromBullet(me, nearestBullet);
            return currAction;
        }
    
        BotPoint2D nearestBot = findNearestBot(me, bots);
        if (nearestBot != null) {
            double distanceToBot = me.distance(nearestBot);
    
            if (distanceToBot < SAFE_DISTANCE) {
                // move away if too close
                currAction = moveAwayFromBot(me, nearestBot);
                return currAction;
            } else if (distanceToBot < SHOOT_DISTANCE) {
                // shoot if in range
                currAction = shootAtBot(me, nearestBot);
                return currAction;
            } else {
                // move to bot
                currAction = moveTowardBot(me, nearestBot);
                return currAction;
            }
        }

        long elapsedTime = System.currentTimeMillis() - startTime; // difference between end time and start is elapsed
        if (elapsedTime > 10) {
            System.out.println("The execution time was longer than 1/100 of a second."); // if this is called then it is too long!
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
     * @return nearest bullet
     */
    private BulletPoint2D findNearestBullet(Point2D me, Vector<BulletPoint2D> bullets) {
        BulletPoint2D nearest = null;
        double minDistance = Double.MAX_VALUE; // big value to set the first bullet to closest before going through
        for (BulletPoint2D bullet : bullets) {
            double distance = me.distance(bullet);
            if (distance < minDistance) { // if less than the nearest distance
                minDistance = distance;
                nearest = bullet;
            }
        }
        return nearest; // returning closest bullet
    }

    /**
     * Move from bullet!
     * @param me
     * @param bullet
     * @return
     */
    private ClobberBotAction moveAwayFromBullet(Point2D me, BulletPoint2D bullet) {
        double x = me.getX() - bullet.getX();
        double y = me.getY() - bullet.getY();
        if (Math.abs(x) > Math.abs(y)) { // if |x| > |y|, then you must move right or left
            return new ClobberBotAction(ClobberBotAction.MOVE, x > 0 ? ClobberBotAction.RIGHT : ClobberBotAction.LEFT);
        } else {
            return new ClobberBotAction(ClobberBotAction.MOVE, y > 0 ? ClobberBotAction.DOWN : ClobberBotAction.UP);
        }
    }
    
    /**
     * Move away from bot!
     * @param me
     * @param bot
     * @return action
     */
    private ClobberBotAction moveAwayFromBot(Point2D me, BotPoint2D bot) {
        double x = me.getX() - bot.getX();
        double y = me.getY() - bot.getY();
        if (Math.abs(x) > Math.abs(y)) { // if |x| > |y|, then you must move right or left
            return new ClobberBotAction(ClobberBotAction.MOVE, x > 0 ? ClobberBotAction.RIGHT : ClobberBotAction.LEFT);
        } else {
            return new ClobberBotAction(ClobberBotAction.MOVE, y > 0 ? ClobberBotAction.DOWN : ClobberBotAction.UP);
        }
    }

    /**
     * Move toward bot !
     * @param me
     * @param bot
     * @return action
     */
    private ClobberBotAction moveTowardBot(Point2D me, BotPoint2D bot) {
        double x = bot.getX() - me.getX();
        double y = bot.getY() - me.getY();
        if (Math.abs(x) > Math.abs(y)) { // if |x| > |y|, then you must move right or left
            return new ClobberBotAction(1, x > 0 ? ClobberBotAction.RIGHT : ClobberBotAction.LEFT);
        } else {
            return new ClobberBotAction(1, y > 0 ? ClobberBotAction.DOWN : ClobberBotAction.UP);
        }
    }


    /**
     * Find the closest bot to us
     * Same logic as findNearestBullet
     * @param me
     * @param bots
     * @return
     */
    private BotPoint2D findNearestBot(Point2D me, Vector<BotPoint2D> bots) {
        BotPoint2D nearest = null;
        double minDistance = Double.MAX_VALUE; // big value to set first to less
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
    
        // calculate velocity of bot if we have old positions to predict where it will move
        if (previousPosition != null) {
            botVelocityX = bot.getX() - previousPosition.getX();
            botVelocityY = bot.getY() - previousPosition.getY();
        }
    
        // predict the bot's future position
        double predictedX = bot.getX() + botVelocityX;
        double predictedY = bot.getY() + botVelocityY;
    
        // shoot this way
        double deltaX = predictedX - me.getX();
        double deltaY = predictedY - me.getY();
    
        if (Math.abs(deltaX) > 0 && Math.abs(deltaY) > 0) {
            if (deltaX > 0 && deltaY > 0) {
                return new ClobberBotAction(ClobberBotAction.SHOOT, ClobberBotAction.DOWN | ClobberBotAction.RIGHT);
            } else if (deltaX > 0 && deltaY < 0) {
                return new ClobberBotAction(ClobberBotAction.SHOOT, ClobberBotAction.UP | ClobberBotAction.RIGHT);
            } else if (deltaX < 0 && deltaY > 0) {
                return new ClobberBotAction(ClobberBotAction.SHOOT, ClobberBotAction.DOWN | ClobberBotAction.LEFT);
            } else {
                return new ClobberBotAction(ClobberBotAction.SHOOT, ClobberBotAction.UP | ClobberBotAction.LEFT);
            }
        }
        
        if (Math.abs(deltaX) > Math.abs(deltaY)) {
            return new ClobberBotAction(ClobberBotAction.SHOOT, deltaX > 0 ? ClobberBotAction.RIGHT : ClobberBotAction.LEFT);
        } else {
            return new ClobberBotAction(ClobberBotAction.SHOOT, deltaY > 0 ? ClobberBotAction.DOWN : ClobberBotAction.UP);
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
