package chicken;

import lombok.Data;

import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;

@Data
public class Tortilla implements Iterable<Bean> {

    @Override
    public Iterator<Bean> iterator() {
        return beanList.iterator();
    }

    public static enum Special {
        None(""),
        Cluster("+"),
        Fire("*"),
        Drone("#"),
        North("N"),
        West("W"),
        South("S"),
        East("O");

        Special(String s) {
            this.s = s;
        }

        public final String s;
    }


    private Plate bws;
    private Bean[][] beans = new Bean[10][10];
    private List<Bean> beanList = new ArrayList<>();
    private Point lastShot;
    private Special lastSpecial;

    public Tortilla(Plate bws) {
        this.bws = bws;
        for (int x = 0; x < beans.length; x++) {
            for (int y = 0; y < beans[x].length; y++) {
                Bean c = new Bean(new Point(x, y), this);
                beans[x][y] = c;
                beanList.add(c);
            }
        }
    }

    public void fire(Point p, Special special) {
        if (special == null) {
            special = Special.None;
        }
        bean(p).setShotAt(true);
        bws.send(special.s + p);
        lastShot = p;
        lastSpecial = special;
    }

    public void lastShotHit(boolean hit, String msg) {
        Point hitAt = null;
        int atIndex = msg == null ? -1 : msg.indexOf(" at ");
        if (atIndex > 0) {
            hitAt = new Point(msg.substring(atIndex + 4, atIndex + 6));
        }
        if (hitAt != null) {
            bean(hitAt).setHit(hit);
        } else {
            bean(lastShot).setHit(hit);
            if(!hit) {
                bean(lastShot).setKnownClear(true);
            }
        }

        switch (lastSpecial) {
            // mark fields between lastShot and shotAt as observed and clear
            case North:
            case West:
            case South:
            case East:
                markTorpedo(lastShot, hitAt, lastSpecial);
                break;
        }

    }


    private void markTorpedo(Point lastShot, Point hitAt, Special lastSpecial) {
        // fields between our sub and impact are clear
        Bean clear = bean(lastShot).next(lastSpecial);
        while(clear != null && clear.getLocation().equals(hitAt)) {
            clear.setKnownClear(true);
            clear = clear.next(lastSpecial);
        }
    }

    public void lastShotSunkShip(String msg) {
        bean(lastShot).setHit(true);
    }

    public void observed(String msg) {
        Bean c = bean(lastShot);
        c.setObservedPieces(Integer.parseInt(msg.substring(16, 17)));
    }

    public Bean bean(Point p) {
        if(p.x < 0 || p.x >=10 || p.y < 0 || p.y >= 10 ) {
            return null;
        }
        return beans[p.x][p.y];
    }

    public void print() {
        for(int x=0; x<beans.length; x++) {
            for(int y=0; y<beans[x].length; y++) {
                int i =  beans[x][y].getObservedPieces();
                System.out.print(i>0?""+i:" ");
            }
            System.out.println();
        }
    }
}
