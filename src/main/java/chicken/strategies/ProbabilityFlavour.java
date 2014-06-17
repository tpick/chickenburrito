package chicken.strategies;

import chicken.Cell;
import chicken.Field;
import chicken.meadow.OwnField;

import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.Iterator;
import java.util.List;
import java.util.Random;

/**
 * Created by mley on 12.06.14.
 */
public class ProbabilityFlavour extends AbstractFlavour {

    Random r = new Random();

    @Override
    public void placeShips() {
        calcProbabilities();

        new OwnField(bws).placeShips();
    }

    private void markKnownEmpty(Cell c, Field.Special... dirs) {
        c = c.get(dirs);
        if (c != null) {
            c.setKnownClear(true);
        }
    }


    private void calcProbabilities() {
        int hits = 0;
        int emptyCells = 0;

        for (Cell c : f) {
            if (c.isHit()) {
                markKnownEmpty(c, Field.Special.North, Field.Special.West);
                markKnownEmpty(c, Field.Special.North, Field.Special.East);
                markKnownEmpty(c, Field.Special.South, Field.Special.West);
                markKnownEmpty(c, Field.Special.South, Field.Special.East);
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
        double avgUnknownCellProbability = ((double)unknownShipCells) /((double) unknownCells);

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

    private void testAndModifyProb(Cell c) {
        List<Field.Special> hitNeighbours = new ArrayList<>();
        // check if any cells around c are already hit
        for(Field.Special s : Field.Special.DIRECTIONS) {
            Cell n = c.next(s);
            if(n != null && n.isHit()) {
                hitNeighbours.add(s);
            }
        }

        if(hitNeighbours.size() == 0) {
            for(Field.Special s : Field.Special.DIRECTIONS) {
                Cell n = c.next(s);
                if(n != null && !n.isShotAt() && n.getProbability() != 0) {
                    // increase probability around single hit cells
                    n.setProbability(n.getProbability()+0.1);
                }
            }
        } else {
            for(Field.Special s : hitNeighbours) {
                Cell n = c.next(s.opposite());
                //TODO consider ship lengths
                if(n != null && !n.isShotAt() && !n.isKnownClear() && n.getProbability() != 0) {
                    n.setProbability(n.getProbability()+0.15);
                }
            }
        }
    }

    @Override
    public void play() {
        calcProbabilities();
        List<Cell> cells = new ArrayList<>();
        cells.addAll(f.getCellList());

        // remove hit cells
        for(Iterator<Cell> i = cells.iterator(); i.hasNext();) {
            Cell c = i.next();
            if(c.isHit()) {
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
        for(Cell c : cells) {
            if(c.getProbability() < max) {
                break;
            }
            maxCount++;
        }

        Cell fire = cells.get(r.nextInt(maxCount));
        f.print();
        f.fire(fire.getLocation(), Field.Special.None);

    }
}
