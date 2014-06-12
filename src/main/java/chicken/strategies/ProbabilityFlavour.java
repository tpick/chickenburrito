package chicken.strategies;

import chicken.Cell;
import chicken.Field;

import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.List;

/**
 * Created by mley on 12.06.14.
 */
public class ProbabilityFlavour extends AbstractFlavour {

    @Override
    public void placeShips() {
        calcProbabilities();
    }

    private void calcProbabilities() {
        int hits = 0;
        int emptyCells = 0;

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
            } else if(c.isObserved()) {
                double probModifier = c.getObservedPieces()/9;
                //TODO check cells around c for definite chances
                //TODO set weighted probability
            }

        }

        int knownCells = hits + emptyCells;
        int unknownCells = 100-knownCells;
        int unknownShipCells = 18-hits;
        double avgUnknownCellProbability = unknownShipCells/unknownCells;

        for(Cell c : f) {
            if(c.getProbability() == -2) {
                c.setProbability(avgUnknownCellProbability);
            }
        }


    }

    @Override
    public void play() {
        calcProbabilities();
        List<Cell> cells = new ArrayList<>();
        cells.addAll(f.getCellList());
        // sort cells by probability
        Collections.sort(cells, new Comparator<Cell>() {
            @Override
            public int compare(Cell o, Cell o2) {
                double diff = o.getProbability() - o2.getProbability();
                if( diff > 0) {
                    return 1;
                } else if(diff < 0) {
                    return -1;
                }
                return 0;
            }
        });

        f.fire(cells.get(0).getLocation(), Field.Special.None);

    }
}
