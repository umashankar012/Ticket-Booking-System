package ticket.booking.services;

import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;
import ticket.booking.entities.Train;

import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

public class TrainService {
    private static final String TRAIN_PATH = "src/main/java/ticket/booking/loacalDb/train.json";
    private final ObjectMapper objectMapper = new ObjectMapper();

    public List<Train> searchTrains(String src, String dest) throws IOException {
        List<Train> trains = objectMapper.readValue(new File(TRAIN_PATH), new TypeReference<List<Train>>() {});
        List<Train> result = new ArrayList<>();
        for (Train t : trains) {
            if (containsIgnoreCase(t.getStations(), src) && containsIgnoreCase(t.getStations(), dest)) {
                result.add(t);
            }
        }
        return result;
    }

    private boolean containsIgnoreCase(List<String> list, String value) {
        for (String item : list) {
            if (item.equalsIgnoreCase(value)) return true;
        }
        return false;
    }

}
