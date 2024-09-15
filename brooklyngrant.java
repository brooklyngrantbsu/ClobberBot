import java.awt.geom.*;
import java.awt.*;
import java.util.*;
import java.awt.image.BufferedImage;
import javax.imageio.ImageIO;
import java.io.File;
import java.io.IOException;

public class brooklyngrant extends ClobberBot {
    private BufferedImage myImage;
    private static final double SAFE_DISTANCE = 50.0; // Distance to maintain from bullets and bots

    public brooklyngrant(Clobber game) {
        super(game);
        try {
            myImage = ImageIO.read(new File("deathstar.png"));
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    ClobberBotAction currAction;

    public ClobberBotAction takeTurn(WhatIKnow currState) {
        long startTime = System.currentTimeMillis();

        Point2D myPos = currState.me;
        Vector<BulletPoint2D> bullets = currState.bullets;
        Vector<BotPoint2D> bots = currState.bots;

        // Avoid bullets
        Point2D avoidVector = calculateAvoidanceVector(bullets, myPos);

        // Decide movement or shooting
        if (shouldShoot(currState)) {
            currAction = new ClobberBotAction(1, ClobberBotAction.SHOOT);
        } else {
            currAction = decideMovement(avoidVector, myPos);
        }

        long endTime = System.currentTimeMillis();
        long elapsedTime = endTime - startTime;

        if (elapsedTime > 10) { // if time is bigger than 10ms (1/100 of a second)
            System.out.println("TOO SLOW: Elapsed time: " + elapsedTime + " milliseconds");
        }

        return currAction;
    }

    private Point2D calculateAvoidanceVector(Vector<BulletPoint2D> bullets, Point2D myPos) {
        double xAvoid = 0;
        double yAvoid = 0;

        for (BulletPoint2D bullet : bullets) {
            double distance = myPos.distance(bullet);
            if (distance < SAFE_DISTANCE) {
                // Repel from the bullet
                xAvoid += myPos.getX() - bullet.getX();
                yAvoid += myPos.getY() - bullet.getY();
            }
        }
        return new Point2D.Double(xAvoid, yAvoid);
    }

    private boolean shouldShoot(WhatIKnow currState) {
        // Basic strategy: shoot if any bot is in a nearby range
        Point2D myPos = currState.me;
        for (BotPoint2D bot : currState.bots) {
            double distance = myPos.distance(bot);
            if (distance < SAFE_DISTANCE) {
                return true; // Target acquired, shoot
            }
        }
        return false;
    }

    private ClobberBotAction decideMovement(Point2D avoidVector, Point2D myPos) {
        if (avoidVector.distance(0, 0) > 0) {
            // Move in the direction away from bullets
            return determineMoveDirection(avoidVector);
        } else {
            // Random move when there is no immediate danger
            return randomMove();
        }
    }

    private ClobberBotAction determineMoveDirection(Point2D avoidVector) {
        // Determine the best move direction based on avoidance vector
        if (Math.abs(avoidVector.getX()) > Math.abs(avoidVector.getY())) {
            if (avoidVector.getX() > 0) {
                return new ClobberBotAction(1, ClobberBotAction.RIGHT);
            } else {
                return new ClobberBotAction(1, ClobberBotAction.LEFT);
            }
        } else {
            if (avoidVector.getY() > 0) {
                return new ClobberBotAction(1, ClobberBotAction.DOWN);
            } else {
                return new ClobberBotAction(1, ClobberBotAction.UP);
            }
        }
    }

    private ClobberBotAction randomMove() {
        switch (rand.nextInt(4)) {
            case 0:
                return new ClobberBotAction(1, ClobberBotAction.UP);
            case 1:
                return new ClobberBotAction(1, ClobberBotAction.DOWN);
            case 2:
                return new ClobberBotAction(1, ClobberBotAction.LEFT);
            case 3:
            default:
                return new ClobberBotAction(1, ClobberBotAction.RIGHT);
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

