package chicken;

import lombok.Data;

import java.util.List;

/**
 * Created by mley on 01.07.14.
 */
@Data
public class History {


    double borderAffinity;

    public static double getAverage(String field, List<History> hs) {
        double d = 0;
        for(History h : hs) {
            d += h.getField(field);
        }
        return d/hs.size();
    }

    public double getField(String name) {
        try {
            return this.getClass().getDeclaredField(name).getDouble(this);
        } catch (Exception e) {
            throw new RuntimeException(e);
        }
    }
}
