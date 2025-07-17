package graph_routing_01.model;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileReader;
import java.io.IOException;
import java.time.LocalTime;
import java.util.HashMap;
import java.util.Map;


// TODO : Make it singleton, or use static methods to access.
// TODO : Read from csv file. (Maybe from REST API?)
public class BusTimeTable {

    private static BusTimeTable instance;
    private boolean isLoaded = false;

    private BusTimeTable() {
        // private constructor to prevent instantiation
    }

    public static BusTimeTable getInstance() {
        if (instance == null) {
            instance = new BusTimeTable();
        }
        return instance;
    }

    // Data definition

    // bus name list
    private Map<Long, String> busNames = new HashMap<>();

    public String getBusName(Long routeId) {
        return busNames.get(routeId);
    }


    private Map<Long, busTime> busTimes = new HashMap<>();

    class busTime{
        
        private Long routeId;
        private LocalTime norStarTime;
        private LocalTime norEndTime;
        private LocalTime satStartTime;
        private LocalTime satEndTime;
        private LocalTime sunStartTime; 
        private LocalTime sunEndTime;
        
        private boolean onNor=true;
        private boolean onSat=true;
        private boolean onSun=true; 
        
        
        private int norMinInterval; // weekday minimum interval in minutes
        private int norMaxInterval; // weekday maximum interval in minutes  
        private int satMinInterval; // Saturday minimum interval
        private int satMaxInterval; // Saturday maximum interval
        private int sunMinInterval; // Sunday minimum interval
        private int sunMaxInterval; // Sunday maximum interval
    
    }

    public void loadBusTimeTable(File file){
        try (BufferedReader br = new BufferedReader(new FileReader(file))) {
            String line = br.readLine(); // skip header
            while ((line = br.readLine()) != null) {
                String[] tokens = line.split(",");
                // Example: adjust indices as per your CSV structure
                Long routeId = Long.parseLong(tokens[13]);
                String routeName = tokens[14];
                boolean onNor = true;
                boolean onSat = true;
                boolean onSun = true;
                for (int i = 1; i <= 6; i++) {
                    if (tokens[i] != null && tokens[i].startsWith("0:")) {
                        tokens[i] = "00:00";
                    }
                    if(tokens[i].trim().isEmpty()){
                        tokens[i] = "00:00";
                    }
                }
                if( tokens[1].equals("정보없음") || tokens[3].trim().isEmpty()){
                    tokens[1] = "00:00";
                    onNor = false;

                }
                if( tokens[3].equals("정보없음") || tokens[3].trim().isEmpty()){
                    tokens[3] = "00:00";
                    onSat = false;

                }
                if( tokens[5].equals("정보없음") || tokens[5].trim().isEmpty()){
                    tokens[5] = "00:00";
                    onSun = false;
                }



                LocalTime norStart = LocalTime.parse(tokens[1]);
                LocalTime norEnd = LocalTime.parse(tokens[2]);
                LocalTime satStart = LocalTime.parse(tokens[3]);
                LocalTime satEnd = LocalTime.parse(tokens[4]);
                LocalTime sunStart = LocalTime.parse(tokens[5]);
                LocalTime sunEnd = LocalTime.parse(tokens[6]);

                // Assuming the intervals are in minutes
                
                int norMin, norMax, satMin, satMax, sunMin, sunMax;
                // Set interval values to 120 if missing or empty
                if (tokens[7] == null || tokens[7].trim().isEmpty()) tokens[7] = "120";
                if (tokens[8] == null || tokens[8].trim().isEmpty()) tokens[8] = "120";
                if (tokens[9] == null || tokens[9].trim().isEmpty()) tokens[9] = "120";
                if (tokens[10] == null || tokens[10].trim().isEmpty()) tokens[10] = "120";
                if (tokens[11] == null || tokens[11].trim().isEmpty()) tokens[11] = "120";
                if (tokens[12] == null || tokens[12].trim().isEmpty()) tokens[12] = "120";
                try { norMin = Integer.parseInt(tokens[7]); } catch (NumberFormatException ex) { norMin = 120; }
                try { norMax = Integer.parseInt(tokens[8]); } catch (NumberFormatException ex) { norMax = 120; }
                try { satMin = Integer.parseInt(tokens[9]); } catch (NumberFormatException ex) { satMin = 120; }
                try { satMax = Integer.parseInt(tokens[10]); } catch (NumberFormatException ex) { satMax = 120; }
                try { sunMin = Integer.parseInt(tokens[11]); } catch (NumberFormatException ex) { sunMin = 120; }
                try { sunMax = Integer.parseInt(tokens[12]); } catch (NumberFormatException ex) { sunMax = 120; }

                busNames.put(routeId, routeName);

                busTime bt = new busTime();
                bt.routeId = routeId;
                bt.norStarTime = norStart;
                bt.norEndTime = norEnd;
                bt.satStartTime = satStart;
                bt.satEndTime = satEnd;
                bt.sunStartTime = sunStart;
                bt.sunEndTime = sunEnd;
                bt.norMinInterval = norMin;
                bt.norMaxInterval = norMax;
                bt.satMinInterval = satMin;
                bt.satMaxInterval = satMax;
                bt.sunMinInterval = sunMin;
                bt.sunMaxInterval = sunMax;

                bt.onNor = onNor;
                bt.onSat = onSat;
                bt.onSun = onSun;

                busTimes.put(routeId, bt);
            }
            isLoaded = true;
            System.out.println("Bus time table loaded successfully.");
        } catch (IOException | NumberFormatException e) {
            e.printStackTrace();
        }


        
    }

    // use hashmap, index should be routeId, 

    // public Map<Long,RouteTimeTable>
    
    // For each route, calculate the last time of bus at each bus stop.
    // Need to load route_interval_distance.csv  for distance between bus stops.
    // Need to load bus_timetable.csv for route ends and starts.
    // No need for locations.
    

    /**
     * 요일구분코드(01:평일, 02:토요일, 03:일요일)
     * @param dayType 1 , 2, 3, 평일 1, 토요일 2, 일요일 3
     * @param routeId bus route Id
     * @param busStopId bus stop Id
     * @return last bus in seconds of day.
     */
    public Long getLastTime(int dayType, Long routeId,Long busStopId){

        
        

        return Long.valueOf(1);
    }
    //sea

}
