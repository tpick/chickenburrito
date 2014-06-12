package chicken;

import lombok.Data;

import java.util.ArrayList;
import java.util.List;


@Data
public class Bean {

    public Bean(Point p, Tortilla f) {
        this.location = p;
        this.f = f;
    }

    Tortilla f;
    Point location;
    boolean shotAt = false;
    boolean hit = false;
    boolean observed = false;
    boolean knownClear = false;
    int observedPieces = 0;
    double probability;

    public Bean north() {
        return f.bean(location.neighbour(0, -1));
    }

    public Bean west() {
        return f.bean(location.neighbour(1, 0));
    }

    public Bean south() {
        return f.bean(location.neighbour(0, 1));
    }

    public Bean east() {
        return f.bean(location.neighbour(-1, 0));
    }

    public Bean next(Tortilla.Special direction) {
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

    public List<Bean> around(boolean withSelf) {
        List<Bean> beans = new ArrayList<>();

        add(beans, this, Tortilla.Special.North, Tortilla.Special.West);
        add(beans, this, Tortilla.Special.North);
        add(beans, this, Tortilla.Special.North, Tortilla.Special.East);


        add(beans, this, Tortilla.Special.West);
        if (withSelf) {
            beans.add(this);
        }
        add(beans, this, Tortilla.Special.East);


        add(beans, this, Tortilla.Special.South, Tortilla.Special.West);
        add(beans, this, Tortilla.Special.South);
        add(beans, this, Tortilla.Special.South, Tortilla.Special.East);

        return beans;
    }

    private void add(List<Bean> beans, Bean b, Tortilla.Special... dirs) {
        for (Tortilla.Special d : dirs) {
            if (b != null) {
                b = b.next(d);
            }
        }
        if (b != null) {
            beans.add(b);
        }
    }

}
