package protocolo;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.Map;

public class MyMap extends HashMap<Integer, Map.Entry<String, Boolean>> {

    public int getLeader(int indexLider) {
        ArrayList<Integer> keys =new ArrayList<>();
        for (Entry<Integer, Entry<String, Boolean>> entry : this.entrySet()) {
            Integer index = entry.getKey();
            keys.add(index);
        }
        return keys.get(indexLider);
    }


    public int activeSize() {
        int count = 0;
        HashMap<Integer, Map.Entry<String, Boolean>> active = new HashMap<>();
        for (Entry<Integer, Entry<String, Boolean>> entry : this.entrySet()) {
            if (entry.getValue().getValue()) {
                count++;
            }
        }
        return count;
    }

    @Override
    public String toString() {
        HashMap<Integer, Map.Entry<String, Boolean>> active = new HashMap<>();
        for (Entry<Integer, Entry<String, Boolean>> entry : this.entrySet()) {
            Integer index = entry.getKey();
            Entry<String, Boolean> values = entry.getValue();
            if (values.getValue()) {
                active.put(index, values);
            }
        }
        return active.toString();
    }
}
