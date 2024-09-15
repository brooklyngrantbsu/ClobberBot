
import java.awt.geom.*;
import java.awt.*;
import java.util.*;

/** This is a lot like ClobberBot3, but has an even stronger tendency to keep moving in the same direction.  Also,
 * I've given you an example of how to read the WhatIKnow state to see where all the bullets and other bots are. */
public class ExperimentBot extends ClobberBot
{
    private final int X = 0, Y = 1;
    private final double maxDanger = 200000D;
    private final double minDanger = maxDanger/4;
    private double botMinDist;
    private double botMidDist;
    private double botMaxDist;
    private double bulletMaxStep;
    private double bulletMinDist;
    private double bulletMidDist;
    private WhatIKnow lastState=null;
    private double w_BulletField=1.0;
    private double w_BotField=1.0;
    private double w_WallField=1.0;

    private static class tester
    {
        int j;
    }

    tester foo = new tester();

    ClobberBotAction currAction, shotAction;
    int shotclock;

    public void drawMe(Graphics page, Point2D me)
    {
        super.drawMe(page,me);
        if(lastState!=null) displayFields(lastState, page);
    }


    public ExperimentBot(Clobber game)
    {
        super(game);
        mycolor = Color.red;
    }

    private double getDist(double x1, double y1, double x2, double y2)
    {
        return getDist(x1-x2, y1-y2);
    }

    private double getDist(double p1, double p2)
    {
        return Math.sqrt(p1*p1+p2*p2);
    }

    //assumes a = b in a^2+b^2=c^2
    private double getDist(double p1)
    {
        return Math.sqrt(2*p1*p1);
    }


    private double getAngle(double fromX, double fromY, double toX, double toY)
    {
        return Math.atan2(toY-fromY, fromX-toX);
    }

    private double getAngle(double x, double y)
    {
        return Math.atan2(y, x);
    }

    private double getX(double angle)
    {
        return Math.cos(angle);
    }

    private double getY(double angle)
    {
        return Math.sin(angle);
    }

    private void displayFields(WhatIKnow currState, Graphics page)
    {
        lastState=currState;
        double[] me = new double[2];
        double[] delta = new double[2];
        double[] delta1 = new double[2];
        double[] delta2 = new double[2];
        double[] delta3 = new double[2];

        for(int x=game.getMinX();x<game.getMaxX();x+=20)
        for(int y=game.getMinY();y<game.getMaxY();y+=20)
        {
            delta[X]=0.0;
            delta[Y]=0.0;
            delta1[X]=0.0;
            delta1[Y]=0.0;
            delta2[X]=0.0;
            delta2[Y]=0.0;
            delta3[X]=0.0;
            delta3[Y]=0.0;
            me[X] = x;
            me[Y] = y;

            getBulletFieldForPoint(me, delta1, currState);
            getBotFieldForPoint(me, delta2, currState);
            getWallFieldForPoint(me, delta3, currState);
            delta[X]=w_BulletField*delta1[X] + w_BotField*delta2[X] + w_WallField*delta3[X];
            delta[Y]=w_BulletField*delta1[Y] + w_BotField*delta2[Y] + w_WallField*delta3[Y];
            //System.out.println("me x,y  = " + me[X] + ", " + me[Y]);
            int a,b,c,d;
            a=(int)me[X]; 
            b=(int)me[Y]; 
            c=(int)me[X]+(delta[X]>0?6:-6); 
            d=(int)me[Y]+(delta[Y]>0?-6:6);
            if(delta[X] != 0.0 || delta[Y] != 0.0)
            {
                //System.out.println("drawing line from " + a + "," + b + " to " + c + "," + d);
                page.drawLine(a,b,c,d);
                page.fillOval((int)me[X]-2, (int)me[Y]-2, 4, 4);
            }
        }
    }

    private void getBotFieldForPoint(double[] me, double[] delta, WhatIKnow currState)
    {
        double dist, angle, otherAngle;
        double[] cBul = new double[2];
        double[] lBul = new double[2];
        double botGirth = Clobber.MAX_BOT_GIRTH;
        double bulletGirth = Clobber.BULLET_GIRTH;
        double bulletMinDist = getDist(bulletGirth+botGirth);
        double bulletMidDist = bulletMinDist*2;
        double angleVariance = Math.PI/2;

        //System.out.println("me x,y  = " + me[X] + ", " + me[Y]);
        for (BotPoint2D bullet:currState.bots)
        {
            cBul[X] = bullet.getX();
            cBul[Y] = bullet.getY();

            lBul[X] = 0;
            lBul[Y] = 0;

            dist = getDist(me[X], me[Y], cBul[X], cBul[Y]);
            angle = getAngle(me[X], me[Y], cBul[X], cBul[Y]);
            otherAngle = getAngle(cBul[X]+lBul[X], cBul[Y]+lBul[Y], cBul[X], cBul[Y]);
    
            botGirth = Clobber.MAX_BOT_GIRTH;
            bulletGirth = Clobber.BULLET_GIRTH;
            bulletMinDist = getDist(bulletGirth+botGirth);
            bulletMidDist = bulletMinDist*2;
            double swivel = ((otherAngle > angle)?-1:1)*angleVariance;
            if (dist < bulletMidDist)
            {
                if (dist < bulletMinDist)
                {
                    delta[X] += Math.signum(getX(angle+(swivel/2)))*maxDanger; 
                    delta[Y] += Math.signum(getY(angle+(swivel/2)))*maxDanger;
                }
                else
                {
                    delta[X] += getX(angle+swivel); 
                    delta[Y] += getY(angle+swivel);
                }
            }
        }
    }

    private void getWallFieldForPoint(double[] me, double[] delta, WhatIKnow currState)
    {
        double botGirth = Clobber.MAX_BOT_GIRTH;
        double maxX = 39;
        double maxY = 39;
        double minX = 0;
        double minY = 0;

        for (double i = minY; i <= maxY; i++){
            if (me[X] <= minX + (botGirth/2 + 6)) {
                delta[X] += 4;
            }
        }

        for (double i = minX; i <= maxX; i++){
            if (me[Y] <= minY + (botGirth/2 + 6)) {
                delta[Y] += -4;
            }
        }
    }

    private void getBotShotFieldForPoint(double[] me, double[] delta, WhatIKnow currState) {
        double[] closest = new double[2];
        double closestDist = Double.MAX_VALUE;
        for (BotPoint2D bot:currState.bots){
            double[] botCoords = new double[2];
            botCoords[0] = bot.getX();
            botCoords[1] = bot.getY();
            double dist =getDist(botCoords[0], botCoords[1], currState.me.getX(), currState.me.getY());
            if (dist < closestDist) {
                if(Math.abs(botCoords[0] - me[0]) < 25){ //Bot is on a vertical axis
                    closestDist = dist;
                    closest[0] = 0;
                    closest[1] = botCoords[1];
                } else if (Math.abs(botCoords[1] - me[1]) < 25){ //Bot is on a horizontal axis
                    closestDist = dist;
                    closest[0] = botCoords[0];
                    closest[1] = 0;
                } else if (Math.abs(botCoords[0]/botCoords[1] - me[0]/me[1]) < 0.4) { //Bot is on a diagonal axis
                    closestDist = dist;
                    closest[0] = botCoords[0];
                    closest[1] = botCoords[1];
                }
            }
        }
        delta[X] = closest[0];
        delta[Y] = closest[1];
    }

private void getBotShotFieldForPoint_old(double[] me, double[] delta, WhatIKnow currState) {
        double[] closest = new double[2];
        double closestDist = Double.MAX_VALUE;
        for (BotPoint2D bot:currState.bots){
            double[] botCoords = new double[2];
            botCoords[0] = bot.getX();
            botCoords[1] = bot.getY();
            double dist =getDist(botCoords[0], botCoords[1], currState.me.getX(), currState.me.getY());
            if (dist < closestDist) {
                if(Math.abs(botCoords[0] - me[0]) < 25){ //Bot is on a vertical axis
                    closestDist = dist;
                    closest[0] = 0;
                    closest[1] = botCoords[1];
                } else if (Math.abs(botCoords[1] - me[1]) < 25){ //Bot is on a horizontal axis
                    closestDist = dist;
                    closest[0] = botCoords[0];
                    closest[1] = 0;
                } else if (Math.abs(botCoords[0]/botCoords[1] - me[0]/me[1]) < 0.4) { //Bot is on a diagonal axis
                    closestDist = dist;
                    closest[0] = botCoords[0];
                    closest[1] = botCoords[1];
                }
            }
        }
        delta[X] = closest[0];
        delta[Y] = closest[1];
    }


    private void getBulletFieldForPoint(double[] me, double[] delta, WhatIKnow currState)
    {
        double dist, angle, otherAngle;
        double[] cBul = new double[2];
        double[] lBul = new double[2];
        double botGirth = Clobber.MAX_BOT_GIRTH;
        double bulletGirth = Clobber.BULLET_GIRTH;
        double bulletMinDist = getDist(bulletGirth+botGirth);
        double bulletMidDist = bulletMinDist*2;
        double angleVariance = Math.PI/2;

        //System.out.println("me x,y  = " + me[X] + ", " + me[Y]);
        for (BulletPoint2D bullet:currState.bullets)
        {
            cBul[X] = bullet.getX();
            cBul[Y] = bullet.getY();

            lBul[X] = bullet.getXPlus();
            lBul[Y] = bullet.getYPlus();

            dist = getDist(me[X], me[Y], cBul[X], cBul[Y]);
            angle = getAngle(me[X], me[Y], cBul[X], cBul[Y]);
            otherAngle = getAngle(cBul[X]+lBul[X], cBul[Y]+lBul[Y], cBul[X], cBul[Y]);
    
            botGirth = Clobber.MAX_BOT_GIRTH;
            bulletGirth = Clobber.BULLET_GIRTH;
            bulletMinDist = getDist(bulletGirth+botGirth);
            bulletMidDist = bulletMinDist*2;
            double swivel = ((otherAngle > angle)?-1:1)*angleVariance;
            if (dist < bulletMidDist)
            {
                if (dist < bulletMinDist)
                {
                    delta[X] += Math.signum(getX(angle+(swivel/2)))*maxDanger; 
                    delta[Y] += Math.signum(getY(angle+(swivel/2)))*maxDanger;
                }
                else
                {
                    delta[X] += getX(angle+swivel); 
                    delta[Y] += getY(angle+swivel);
                }
            }
        }
    }


    /** Here's an example of how to read the WhatIKnow data structure */
    private void showWhatIKnow(WhatIKnow currState)
    {
        System.out.println("My id is " + ((ImmutablePoint2D)(currState.me)).getID() + ", I'm at position (" + 
                           currState.me.getX() + ", " + currState.me.getY() + ")");
        System.out.print("Bullets: ");
        Iterator<BulletPoint2D> it = currState.bullets.iterator();
        while(it.hasNext())
        {
            ImmutablePoint2D p = (ImmutablePoint2D)(it.next());
            System.out.print(p + ", ");
        }
        System.out.println();

        System.out.print("Bots: ");
        Iterator<BotPoint2D> bit = currState.bots.iterator();
        while(bit.hasNext())
        {
            System.out.print(bit.next() + ", ");
        }
        System.out.println();
    }

    public ClobberBotAction takeTurn(WhatIKnow currState)
    {
        //showWhatIKnow(currState); // @@@ Uncomment this line to see it print out all bullet and bot positions every turn

        lastState=currState;
        shotclock--;

        double me[] = new double[2];
        double delta1[] = new double[2];
        double delta2[] = new double[2];
        double delta3[] = new double[2];
        double delta[] = new double[2];
        me[X] = currState.me.getX();
        me[Y] = currState.me.getY();
        if(shotclock<=0)
        {
            delta[X]=0;
            delta[Y]=0;
            getBotShotFieldForPoint(me, delta, currState);
            shotclock=game.getShotFrequency()+1;
            if(delta[X]!=0 || delta[Y]!=0)
            {
                int shotDir = 0;

                if (delta[X] < 0) shotDir |= ClobberBotAction.LEFT;
                else if (0 < delta[X]) shotDir |= ClobberBotAction.RIGHT;

                if (delta[Y] < 0) shotDir |= ClobberBotAction.DOWN;
                else if (0 < delta[Y]) shotDir |= ClobberBotAction.UP;

                shotAction = new ClobberBotAction(ClobberBotAction.SHOOT, shotDir);
            }
            else switch(rand.nextInt(8))
            {
                case 0:
                    shotAction = new ClobberBotAction(ClobberBotAction.SHOOT, ClobberBotAction.UP);
                break;
                case 1:
                    shotAction = new ClobberBotAction(ClobberBotAction.SHOOT, ClobberBotAction.DOWN);
                break;
                case 2:
                    shotAction = new ClobberBotAction(ClobberBotAction.SHOOT, ClobberBotAction.LEFT);
                break;
                case 3:
                    shotAction = new ClobberBotAction(ClobberBotAction.SHOOT, ClobberBotAction.RIGHT);
                break;
                case 4:
                    shotAction = new ClobberBotAction(ClobberBotAction.SHOOT, ClobberBotAction.UP | ClobberBotAction.LEFT);
                break;
                case 5:
                    shotAction = new ClobberBotAction(ClobberBotAction.SHOOT, 
                            ClobberBotAction.UP | ClobberBotAction.RIGHT | ClobberBotAction.DOWN | ClobberBotAction.LEFT);
                break;
                case 6:
                    shotAction = new ClobberBotAction(ClobberBotAction.SHOOT, ClobberBotAction.DOWN | ClobberBotAction.LEFT);
                break;
                default:
                    shotAction = new ClobberBotAction(ClobberBotAction.SHOOT, ClobberBotAction.DOWN | ClobberBotAction.RIGHT);
                break;
            }
            return shotAction;
        }
        else
        {
            delta[X]=0.0;
            delta[Y]=0.0;
            delta1[X]=0.0;
            delta1[Y]=0.0;
            delta2[X]=0.0;
            delta2[Y]=0.0;
            delta3[X]=0.0;
            delta3[Y]=0.0;
            getBulletFieldForPoint(me, delta1, currState);
            getBotFieldForPoint(me, delta2, currState);
            getWallFieldForPoint(me, delta3, currState);
            delta[X]=w_BulletField*delta1[X] + w_BotField*delta2[X] + w_WallField*delta3[X];
            delta[Y]=w_BulletField*delta1[Y] + w_BotField*delta2[Y] + w_WallField*delta3[Y];
            if(delta[X]!=0 || delta[Y]!=0)
            {
                int moveDir = 0;

                if (delta[X] < 0) moveDir |= ClobberBotAction.LEFT;
                else if (0 < delta[X]) moveDir |= ClobberBotAction.RIGHT;

                if (delta[Y] < 0) moveDir |= ClobberBotAction.DOWN;
                else if (0 < delta[Y]) moveDir |= ClobberBotAction.UP;

                currAction = new ClobberBotAction(ClobberBotAction.MOVE, moveDir);
            }
            else //if(currAction==null || rand.nextInt(20)>18)
            {
                switch(rand.nextInt(4))
                {
                    case 0:
                        currAction = new ClobberBotAction(ClobberBotAction.MOVE, ClobberBotAction.UP);
                    break;
                    case 1:
                        currAction = new ClobberBotAction(ClobberBotAction.MOVE, ClobberBotAction.DOWN);
                    break;
                    case 2:
                        currAction = new ClobberBotAction(ClobberBotAction.MOVE, ClobberBotAction.LEFT);
                    break;
                    case 3:
                        currAction = new ClobberBotAction(ClobberBotAction.MOVE, ClobberBotAction.RIGHT);
                    break;
                    case 4:
                        currAction = new ClobberBotAction(ClobberBotAction.MOVE, ClobberBotAction.UP | ClobberBotAction.LEFT);
                    break;
                    case 5:
                        currAction = new ClobberBotAction(ClobberBotAction.MOVE, ClobberBotAction.UP | ClobberBotAction.RIGHT);
                    break;
                    case 6:
                        currAction = new ClobberBotAction(ClobberBotAction.MOVE, ClobberBotAction.DOWN | ClobberBotAction.LEFT);
                    break;
                    default:
                        currAction = new ClobberBotAction(ClobberBotAction.MOVE, ClobberBotAction.DOWN | ClobberBotAction.RIGHT);
                    break;
                }
                currAction = new ClobberBotAction(ClobberBotAction.NONE, 0);
            }
        }
        return currAction;
    }

    public String toString()
    {
        return "ExperimentBot by Tim Andersen";
    }
}


