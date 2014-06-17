package chicken;

import lombok.Data;

import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;

@Data
public class Field implements Iterable<Cell> {

    @Override
    public Iterator<Cell> iterator() {
        return cellList.iterator();
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

        public static final List<Special> DIRECTIONS;
        static {
            DIRECTIONS = new ArrayList<>();
            DIRECTIONS.add(North);
            DIRECTIONS.add(West);
            DIRECTIONS.add(South);
            DIRECTIONS.add(East);
        }

        public Special opposite() {
            switch(this) {
                case North: return South;
                case West: return East;
                case South: return North;
                case East: return West;
                default:
                    throw new IllegalStateException("not a direction: "+this);
            }
        }

        public final String s;
    }


    private WSHandler bws;
    private Cell[][] cells = new Cell[16][16];
    private List<Cell> cellList = new ArrayList<>();
    private Point lastShot;
    private Special lastSpecial;

    public Field(WSHandler bws) {
        this.bws = bws;
        for (int x = 0; x < cells.length; x++) {
            for (int y = 0; y < cells[x].length; y++) {
                Cell c = new Cell(new Point(x, y), this);
                cells[x][y] = c;
                cellList.add(c);
            }
        }
    }

    public void fire(Point p, Special special) {
        if (special == null) {
            special = Special.None;
        }
        bean(p).setShotAt(true);
        bws.send(special.s + p);
        System.out.println("Firing: "+special.s + p);
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
        Cell clear = bean(lastShot).next(lastSpecial);
        while(clear != null && clear.getLocation().equals(hitAt)) {
            clear.setKnownClear(true);
            clear = clear.next(lastSpecial);
        }
    }

    public void lastShotSunkShip(String msg) {
        Cell c = bean(lastShot);
        c.setHit(true);
        boolean horizontal = false;
        if((c.east() != null && c.east().isHit()) ||(c.west() != null && c.west().isHit()) ) {
            horizontal = true;
        }

        if(horizontal) {
             markSunkShip(c, Special.East, Special.West);
        } else {
            markSunkShip(c, Special.North, Special.South);
        }

    }

    private void markSunkShip(Cell c, Special... dirs) {

        for(Special s : dirs) {
            Cell a = c;
            while(a != null && a.isHit()) {
                a = a.next(s);
                if(a != null && !a.isHit()) {
                    a.setKnownClear(true);
                }
            }
        }
    }

    public void observed(String msg) {
        Cell c = bean(lastShot);
        c.setObservedPieces(Integer.parseInt(msg.substring(16, 17)));
    }

    public Cell bean(Point p) {
        if(p.x < 0 || p.x >=16 || p.y < 0 || p.y >= 16 ) {
            return null;
        }
        return cells[p.x][p.y];
    }

    public void print() {
        for(int x=0; x< cells.length; x++) {
            System.out.print("|");
            for(int y=0; y< cells[x].length; y++) {
                Cell c = cells[x][y];
                String s = "▒▒";
                if(c.isKnownClear()) {
                    s = "  ";
                } else if(c.isHit()) {
                    s = "▉▉";
                }

                System.out.print(s);
            }
            System.out.println("|");
        }
    }
}
