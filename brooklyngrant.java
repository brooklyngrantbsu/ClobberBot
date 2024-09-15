
import java.awt.geom.*;
import java.awt.*;
import java.util.*;

public class brooklyngrant extends ClobberBot {
    
    public brooklyngrant(Clobber game) {
        super(game);

    }

    ClobberBotAction currAction;

    public ClobberBotAction takeTurn(WhatIKnow currState)
    {
        if(currAction==null || ((currAction.getAction() & ClobberBotAction.SHOOT)>0) || rand.nextInt(10)>8)
        {
            switch(rand.nextInt(8))
            {
                case 0:
                    currAction = new ClobberBotAction(rand.nextInt(2)+1, ClobberBotAction.UP);
                break;
                case 1:
                    currAction = new ClobberBotAction(rand.nextInt(2)+1, ClobberBotAction.DOWN);
                break;
                case 2:
                    currAction = new ClobberBotAction(rand.nextInt(2)+1, ClobberBotAction.LEFT);
                break;
                case 3:
                    currAction = new ClobberBotAction(rand.nextInt(2)+1, ClobberBotAction.RIGHT);
                break;
                case 4:
                    currAction = new ClobberBotAction(rand.nextInt(2)+1, ClobberBotAction.UP | ClobberBotAction.LEFT);
                break;
                case 5:
                    currAction = new ClobberBotAction(rand.nextInt(2)+1, ClobberBotAction.UP | ClobberBotAction.RIGHT);
                break;
                case 6:
                    currAction = new ClobberBotAction(rand.nextInt(2)+1, ClobberBotAction.DOWN | ClobberBotAction.LEFT);
                break;
                default:
                    currAction = new ClobberBotAction(rand.nextInt(2)+1, ClobberBotAction.DOWN | ClobberBotAction.RIGHT);
                break;
            }
        }
        return currAction;
    }

    
    /** Draws the clobber bot to the screen.  The drawing should be centered at the point me, and should not be bigger than 9x9 pixels */
    public void drawMe(Graphics page, Point2D me)
    {
        int x,y;
        x=(int)me.getX() - Clobber.MAX_BOT_GIRTH/2 -1;
        y=(int)me.getY() - Clobber.MAX_BOT_GIRTH/2 -1;
        page.setColor(mycolor);
        page.fillOval(x,y, Clobber.MAX_BOT_GIRTH,Clobber.MAX_BOT_GIRTH);
    }

    public String toString()
    {
        return "Brooklyn Grant";
    }
}
