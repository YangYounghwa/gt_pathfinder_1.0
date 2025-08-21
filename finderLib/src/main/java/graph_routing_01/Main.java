package graph_routing_01;

import java.io.File;
import java.io.FileInputStream;
import java.io.InputStreamReader;
import java.io.Serializable;
import java.net.MalformedURLException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.PriorityQueue;
import java.util.Stack;
import java.io.Reader;


import org.locationtech.jts.geom.Coordinate;

import org.locationtech.jts.geom.LineString;

import graph_routing_01.Finder.ApriPathFinder;
import graph_routing_01.Finder.model.ApriPath;
import graph_routing_01.Finder.model.ApriPathDTO;
import graph_routing_01.Finder.util.ApriPathMapper;


public class Main {

    public static void main(String[] args) throws Exception {

        // TODO  List : Make exceptions for strange cases...  and how to handle those.  
        // handling should be done on the server side. 


        ApriPathFinder apf = new ApriPathFinder();
    
        // simplified_split2.shp를 통해서 그래프를 생성합니다.
        // 이 단계는 전처리가 완료된 shp파일이 필요합니다.
        // 이 파일은 수치지도에서 branch와 intersection을 모두 분리해서 도로가 만나는 지점마다 선을 잘라서 그래프를 만들 수 있게 해둔 도로 수치지도입니다.
        // 그 후 도로의 양끝점은 유지 한 채로 중간을 단순화 하여 데이터를 단순화 하였습니다. 
        apf.buildBaseGraph(apf.readSHP("data_folder\\simplified_split2.shp"));


        // 정류소의 위치를 도로에 추가해줍니다. 
        apf.addBusstopNodes("data_folder\\busstop_location.csv");
        // problem occured here

        apf.initBusTimeTable(new File("data_folder\\bus_timetable.csv"));

        apf.addBusRouteEdges("data_folder\\route_interval_distance.csv");



        System.out.println("Saving files."); 
        
        // 이게 꽤나 오래 걸리는 구간.
        apf.saveAllEdgesToShp("result\\all_edges.shp");


        // 버스 구간 추가 구현이 미완성입니다. 
        // 현재 인천시에 버스 구간 노드 정보 요청했고 업데이트 및 검토 후 업로드 된다고 답변 받았습니다.
        // 음... 신규 버스 데이터를 받긴 했는데..  버스 경로 node는 없습니다. 따라서 결과도 버스 구간은 직선으로만 나올 예정입니다.



        // 37.449910,126.670039
        //37.536697,126.728416
        double stLat = 37.449910;
        double stLon = 126.670039;
        double endLat = 37.536697;
        double endLon = 126.728416;
        //경도 latitude 위도 latitude,
        //시작점과 종료점의 위도와 경도를 입력하면 경로 클래스를 반환합니다.
        // 주의점은 위도와 경도의 위치가 일반적인 맵과 반대입니다. 
        // gis 시스템들의 custom인 듯 합니다.
        System.out.println("Finding Path");
        ApriPath path = apf.findPath(stLon,stLat,endLon,endLat);

        // Path to DTO using ApriPathMapper.
        ApriPathDTO pathDTO = ApriPathMapper.toDTO(path);

        System.out.println("Path Found");
        if (path == null) 
            System.err.println("경로를 탐색 실패");

        // 카카오 맵과 연동을 위해서 위도 및 경도를 바꾸어줍니다.
        // 반환 path2는 coordinate가 X가 latitude, Y가 longitude 입니다.
        ApriPath path2 = apf.pathToWGS84(path);

        

        // shp파일로 저장하여 qgis를 통해서 열어볼 수 있습니다.
        apf.pathToShp(path, "result\\path.shp");





        // 사용 후 얻은 path에서 데이터 추출하기.
        // 도로의 좌표
        // 도로의 이름
        // 도로의 종류
        // 1m/s 기준 총 이동 시간 (횡단보도의 내용이 없기 때문에 1로 잡았습니다.)

        //path2.lineSeg<LineString> List오브젝트에서 각각의 LineString을 꺼낸 다음
        // 좌표를 얻을 수 있습니다
        // 도로에서 분기점이 없는 구간은 하나의 LineString으로 표시됩니다. 분기점마다 잘라서 새로운 linestring이 됩니다. 예를 들어 'ㅏ' 모양은 LineString이 3개 입니다.
        for (int j =0; j<path2.lineSeg.size() & j<path2.roadName.size() & j<path2.roadType.size();j++){
            LineString ls = path2.lineSeg.get(j);
            String roadName = path2.roadName.get(j);
            String roadType = path2.roadType.get(j);
            Coordinate[] cords = ls.getCoordinates();
            System.out.println("도로명 : "+roadName+" 타입 : "+ roadType);
            for(int i =0 ; i < cords.length; i++){
                // System.out.print("("+cords[i].getX()+","+cords[i].getY()+ ")");
            }
            // System.out.println();
        }
        System.out.print("총 시간 (s) : "+ path2.totalTime);
    }

}
