package chicken.strategies;

import chicken.Cell;

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

    }
}
