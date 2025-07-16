package graph_routing_01.model;

import java.util.HashMap;
import java.util.Map;

public class BusTimeTable {

    // TODO : get Last, first time, load csv or setter for datasets that runs line by line. ? 
    // TODO : make row data class
    // use hashmap, index should be routeId, 
    class RouteTimeTable{
        //for each routeId, a single RouteTimeTable
        class RowData{
            Long norday;

        }
        // for each busStopId a single row data.
        // public Map<Long,RowData> ;


    } 
    // public Map<Long,RouteTimeTable>
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
