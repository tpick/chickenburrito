package chicken;

import lombok.Data;

import java.util.ArrayList;
import java.util.List;


@Data
public class Cell {

    public Cell(Point p, Field f) {
        this.location = p;
        this.f = f;
    }

    Field f;
    Point location;
    boolean shotAt = false;
    boolean hit = false;
    boolean observed = false;
    boolean knownClear = false;
    int observedPieces = 0;
    double probability;

    public Cell north() {
        return f.bean(location.neighbour(0, -1));
    }

    public Cell west() {
        return f.bean(location.neighbour(1, 0));
    }

    public Cell south() {
        return f.bean(location.neighbour(0, 1));
    }

    public Cell east() {
        return f.bean(location.neighbour(-1, 0));
    }

    public Cell next(Field.Special direction) {
        switch (direction) {
            case North:
                return north();
            case West:
                return west();
            case South:
                return south();
            case East:
                return east();
            default:
                throw new IllegalArgumentException("not a direction");
        }
    }

    public List<Cell> around(boolean withSelf) {
        List<Cell> cells = new ArrayList<>();

        add(cells, this, Field.Special.North, Field.Special.West);
        add(cells, this, Field.Special.North);
        add(cells, this, Field.Special.North, Field.Special.East);


        add(cells, this, Field.Special.West);
        if (withSelf) {
            cells.add(this);
        }
        add(cells, this, Field.Special.East);


        add(cells, this, Field.Special.South, Field.Special.West);
        add(cells, this, Field.Special.South);
        add(cells, this, Field.Special.South, Field.Special.East);

        return cells;
    }

    private void add(List<Cell> cells, Cell b, Field.Special... dirs) {
        for (Field.Special d : dirs) {
            if (b != null) {
                b = b.next(d);
            }
        }
        if (b != null) {
            cells.add(b);
        }
    }

}
