package chicken.strategies;

import chicken.Burrito;
import chicken.Cell;
import chicken.Constants;
import chicken.Field;
import chicken.History;
import chicken.Point;
import chicken.meadow.OwnField;
import chicken.meadow.Ship;

import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.Iterator;
import java.util.List;
import java.util.Random;

/**
 * Created by mley on 12.06.14.
 */
public class ProbabilityFlavour extends AbstractFlavour implements Constants {

    private final List<History> history;
    Random r = new Random();

    List<Ship> hitShips = new ArrayList<>();
    List<Ship> sunkShips = new ArrayList<>();

    List<Point> firstShots = new ArrayList<>();

    public ProbabilityFlavour(List<History> history) {
        this.history = history;

        firstShots.add(new Point(7, 7));
        firstShots.add(new Point(5, 5));
        firstShots.add(new Point(9, 9));
        firstShots.add(new Point(5, 9));
        firstShots.add(new Point(9, 5));
        firstShots.add(new Point(7, 4));
        firstShots.add(new Point(7, 4));
        firstShots.add(new Point(7, 10));
        firstShots.add(new Point(4, 7));
        firstShots.add(new Point(10, 7));
    }

    @Override
    public void placeShips() {
        new OwnField(bws).placeShips();
    }


    /**
     * Mark the cell which is dirs from c away is clear
     *
     * @param c
     * @param dirs
     */
    private void markKnownEmpty(Cell c, Field.Special... dirs) {
        c = c.get(dirs);
        if (c != null) {
            c.setKnownClear(true);
        }
    }


    private void calcProbabilities() {
        int hits = 0;
        int emptyCells = 0;

        // for every hit cell, we know that all diagonal cells are clear
        for (Cell c : f) {
            if (c.isHit()) {
                markKnownEmpty(c, Field.Special.North, Field.Special.West);
                markKnownEmpty(c, Field.Special.North, Field.Special.East);
                markKnownEmpty(c, Field.Special.South, Field.Special.West);
                markKnownEmpty(c, Field.Special.South, Field.Special.East);
            }
        }


        /**
         *  ..CC..
         *  CC??CC
         *  ..CC..
         *  if all around cell is clear, cell itself is also clear, because smallest ship is 2 cells
         */
        boolean foundClear = true;
        while (foundClear) {
            foundClear = false;
            for (Cell c : f) {
                if (!c.isHit() && !c.isKnownClear()) {
                    for (Field.Special s : Field.Special.DIRECTIONS) {
                        if (c.next(s) != null && !c.next(s).isKnownClear()) {
                            c = null;
                            break;
                        }
                    }
                    if (c != null) {
                        foundClear = true;
                        c.setKnownClear(true);
                    }
                }
            }
        }

        for (Cell c : f) {
            // reset all probabilities
            c.setProbability(-2);

            if (c.isShotAt()) {
                if (c.isHit()) {
                    hits++;
                    c.setProbability(1);
                } else {
                    emptyCells++;
                    c.setProbability(0);
                }

            } else if (c.isKnownClear()) {
                emptyCells++;
                c.setProbability(0);
            } else if (c.isObserved()) {
                double probModifier = c.getObservedPieces() / 9;
                //TODO check cells around c for definite chances
                //TODO set weighted probability
            }

        }


        int knownCells = hits + emptyCells;
        int unknownCells = 256 - knownCells;
        int unknownShipCells = 28 - hits;
        double avgUnknownCellProbability = ((double) unknownShipCells) / ((double) unknownCells);

        for (Cell c : f) {
            if (c.getProbability() == -2) {
                c.setProbability(avgUnknownCellProbability);
            }
        }


        for (Cell c : f) {
            if (c.isHit()) {
                testAndModifyProb(c);
            }
        }


    }


    /**
     * check hit cells and modify probabilities around hit cells
     *
     * @param c
     */
    private void testAndModifyProb(Cell c) {
        List<Field.Special> hitNeighbours = new ArrayList<>();
        // check if any cells around c are already hit
        for (Field.Special s : Field.Special.DIRECTIONS) {
            Cell n = c.next(s);
            if (n != null && n.isHit()) {
                hitNeighbours.add(s);
            }
        }

        if (hitNeighbours.size() == 0) {
            for (Field.Special s : Field.Special.DIRECTIONS) {
                Cell n = c.next(s);
                if (n != null && !n.isShotAt() && n.getProbability() != 0) {
                    // increase probability around single hit cells
                    n.setProbability(n.getProbability() + 0.1);
                }
            }
        } else {
            for (Field.Special s : hitNeighbours) {
                Cell n = c.next(s.opposite());
                //TODO consider ship lengths
                if (n != null && !n.isShotAt() && !n.isKnownClear() && n.getProbability() != 0) {
                    n.setProbability(n.getProbability() + 0.15);
                }
            }
        }
    }

    @Override
    public void play() {
        calcProbabilities();
        List<Cell> cells = new ArrayList<>();
        cells.addAll(f.getCellList());

        // remove hit and clear cells
        for (Iterator<Cell> i = cells.iterator(); i.hasNext(); ) {
            Cell c = i.next();
            if (c.isHit() || c.isKnownClear()) {
                i.remove();
            }
        }

        // sort cells by probability
        Collections.sort(cells, new Comparator<Cell>() {
            @Override
            public int compare(Cell o, Cell o2) {
                double diff = o.getProbability() - o2.getProbability();
                if (diff > 0) {
                    return -1;
                } else if (diff < 0) {
                    return 1;
                }
                return 0;
            }
        });


        double max = cells.get(0).getProbability();
        int maxCount = 0;
        for (Cell c : cells) {
            if (c.getProbability() < max) {
                break;
            }
            maxCount++;
        }
        f.print();
        Cell fire = null;
        Field.Special special = Field.Special.None;
        if (maxCount == cells.size()) {
            // if all cells have the same probability, we use another strategy:


            double avgBorderAffinity = History.getAverage("borderAffinity", history);
            boolean preferBorderLine = false;

            while (firstShots.size() > 0 && fire == null) {
                // first shots are pre-defined
                if (f.isHasCluster()) {
                    special = Field.Special.Cluster;
                }
                Point s = firstShots.remove(0);
                fire = f.bean(s);
                if (fire.isKnownClear() || fire.isHit()) {
                    fire = null;
                } else {
                    if (f.isHasCluster()) {
                        // if we have a cluster bomb left, we check if it will be effective
                        for (Field.Special sp : Field.Special.DIRECTIONS) {
                            if (fire.next(sp).isHit() && fire.next(sp).isKnownClear()) {
                                fire = null;
                                break;
                            }
                        }
                    }
                }

            }

            if (avgBorderAffinity > 0.4) {
                // turns out, preferring border takes more shots to win pct85Border scenario
                //preferBorderLine = true;
            }


            // if first shots are used up, shoot the longest unknown lines
            if (fire == null) {
                List<Line> lines = findLines();

                if (preferBorderLine) {
                    for (Iterator<Line> i = lines.iterator(); i.hasNext(); ) {
                        Line line = i.next();
                        if (!line.isBorder()) {
                            i.remove();
                        }
                    }
                }

                if (lines.size() > 0) {
                    // find longest lines and pick random
                    int maxLength = lines.get(0).getLength();
                    int maxIndex = 0;
                    for (Line l : lines) {
                        if (l.length < maxLength) {
                            break;
                        }
                        maxIndex++;
                    }
                    List<Cell> candidates = new ArrayList<>();

                    if (lines.get(0).getLength() <= 3) {
                        // if only short lines left, we try to find crosses and Ts
                        for (int i = 0; i < maxCount; i++) {
                            Cell c = cells.get(i);
                            if (c.isUnknownNWSE() >= 2) {
                                candidates.add(c);
                            }
                        }

                    }

                    if (candidates.isEmpty()) {

                        for (int i = 0; i < maxIndex; i++) {
                            Line l = lines.get(i);
                            if (l.getLength() > 2) {
                                // ▒▒  ▒▒  ▒▒
                                //   ▒▒  ▒▒
                                // ▒▒  ▒▒  ▒▒
                                // interleaved / twothirds strategy doesn't really add something, but also does not play worse
                                for (int j = -(l.getLength() / 3 - 1); j <= (l.getLength() / 3); j++) {
                                    Cell c = f.bean(l.getMiddle(j));
                                    if (c != null && c.isUnknownNWSE() >= 3) {
                                        candidates.add(c);
                                    }
                                }
                            }
                        }
                    }

                    if (candidates.size() > 0) {
                        int maxUI = candidates.size();

                        Collections.sort(candidates, new Comparator<Cell>() {
                            @Override
                            public int compare(Cell cell, Cell cell2) {
                                return cell.isUnknownNWSE() - cell2.isUnknownNWSE();
                            }
                        });
                        int maxUnknown = candidates.get(0).isUnknownNWSE();
                        maxUI = 0;
                        for(Cell c : candidates) {
                            if(c.isUnknownNWSE() != maxUnknown) {
                                break;
                            }
                            maxUI++;
                        }

                        fire = candidates.get(r.nextInt(maxUI));
                    } else {
                        fire = f.bean(lines.get(r.nextInt(maxIndex)).getMiddle(0));
                    }

                }

            }


        }


        // pick a random cell of the cells with max probability
        if (fire == null) {
            fire = cells.get(r.nextInt(maxCount));
        }


        f.fire(fire.getLocation(), special);

    }

    private List<Line> findLines() {
        List<Line> lines = new ArrayList<>();
        for (int i = 0; i < 16; i++) {
            lines.addAll(findLineIn(i, true));
            lines.addAll(findLineIn(i, false));
        }

        Collections.sort(lines);

        return lines;
    }

    private List<Line> findLineIn(int i, boolean horizontal) {
        List<Line> lines = new ArrayList<>();
        Cell start = null;
        Field.Special dir = horizontal ? Field.Special.East : Field.Special.South;
        if (horizontal) {
            start = f.bean(new Point(0, i));
        } else {
            start = f.bean(new Point(i, 0));
        }

        while (start != null) {
            start = findNextLine(lines, start, dir);
        }

        return lines;
    }

    private Cell findNextLine(List<Line> lines, Cell start, Field.Special dir) {
        while (start != null && (start.isHit() || start.isKnownClear())) {
            start = start.next(dir);
        }
        if (start == null) {
            return null;
        }
        Cell end = start;
        while (end.next(dir) != null && !end.next(dir).isKnownClear() && !end.next(dir).isHit()) {
            end = end.next(dir);
        }
        if (start == end) {
            return start.next(dir);
        }

        Line l = new Line();
        l.setP(start.getLocation());
        if (dir == Field.Special.East) {
            l.setHorizontal(true);
            l.setLength(end.getLocation().x - start.getLocation().x + 1);
        } else {
            l.setHorizontal(false);
            l.setLength(end.getLocation().y - start.getLocation().y + 1);
        }
        lines.add(l);

        return end.next(dir);

    }

    @Override
    public void hit(Cell shotAt, Cell hitAt, Field.Special lastSpecial) {
        if (hitAt != null) {
            shotAt = hitAt;
        }

        Ship hitShip = null;
        for (Cell c : shotAt.hitNeighbours()) {
            // look for hits around current hit to find already hit ships
            for (Ship s : hitShips) {
                if (s.liesOn(c.getLocation())) {
                    hitShip = s;
                    break;
                }
            }
            if (hitShip != null) {
                break;
            }
        }
        if (hitShip == null) {
            hitShip = new Ship();
            hitShips.add(hitShip);
        }
        hitShip.hit(shotAt);


    }

    @Override
    public void sunkShip(Cell c, String msg) {
        Ship sunk = null;
        for (Cell n : c.hitNeighbours()) {
            for (Ship s : hitShips) {
                if (s.liesOn(n.getLocation())) {
                    sunk = s;
                    break;
                }
            }
            if (sunk != null) {
                break;
            }
        }

        if (sunk != null) {
            sunk.sink(c);

            hitShips.remove(sunk);
            sunkShips.add(sunk);
        } else {
            Burrito.out.println("Failed to find sunk ship around " + c);
        }
    }

    public History gameOver(boolean win) {
        int hitCells = 0;
        int unknownCells = 0;
        int hitsOnBorder = 0;
        for (Cell c : f) {
            if (c.isHit()) {
                hitCells++;
                if (c.isBorder()) {
                    hitsOnBorder++;
                }
            }
            if (!c.isHit() && !c.isKnownClear()) {
                unknownCells++;
            }
        }


        double borderAffinity = (double) hitsOnBorder / 28;

        History h = new History();
        h.setBorderAffinity(borderAffinity);
        return h;

    }

}
