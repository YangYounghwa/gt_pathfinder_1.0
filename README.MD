# GT Pathfinder

GT Pathfinder is a Java-based pathfinding library that calculates the shortest path between two points, considering both road networks and public bus routes. It uses the GeoTools library to handle geospatial data and a custom graph implementation with Dijkstra's algorithm to find the optimal path.

## Features

* **Graph-Based Routing:** Constructs a routable graph from road network shapefiles.
* **Multi-Modal Pathfinding:** Integrates bus stop locations and bus schedules into the graph to allow for multi-modal routing (walking and bus).
* **Time-Based Penalties:** Applies penalties for bus transfers, considering bus schedules to provide more realistic travel times.
* **Coordinate System Transformation:** Handles different coordinate reference systems (CRS), including WGS84 (latitude/longitude) and other projected systems.
* **Path Export:** The calculated path can be exported to a shapefile for visualization in GIS software like QGIS.

## Getting Started

### Prerequisites

* Java 21 or higher
* Gradle

### Dependencies

The project uses the following major libraries:

* **GeoTools:** For handling and processing geospatial data.
* **JTS (Java Topology Suite):** For geometric operations.
* **OpenCSV:** For reading data from CSV files.

All dependencies are managed through Gradle.

### Installation

1.  Clone the repository:
    ```bash
    git clone https://your-repository-url/gt_pathfinder.git
    ```
2.  Build the project using Gradle:
    ```bash
    cd gt_pathfinder
    ./gradlew build
    ```

## How to use this.

You can use this library in your own projects by publishing it to your local Maven repository.

1.  **Publish to Maven Local:**

    Run the following Gradle command to build the library and publish it to your local Maven repository (`~/.m2/repository`):
    ```bash
    ./gradlew --refresh-dependencies :finderLib:clean :finderLib:publishToMavenLocal
    ```

2.  **Add as a Dependency:**

    Now, you can add the library as a dependency in your project.

    **For Gradle projects (`build.gradle`):**
    ```groovy
    repositories {
        mavenLocal()
        mavenCentral() // and other repositories
    }

    dependencies {
        implementation 'apri:finderLib:1.0.0'
    }
    ```

    **For Maven projects (`pom.xml`):**
    ```xml
    <dependencies>
      <dependency>
        <groupId>apri</groupId>
        <artifactId>finderLib</artifactId>
        <version>1.0.0</version>
      </dependency>
    </dependencies>
    ```

## Usage Example

To use the library, you need to instantiate the `ApriPathFinder` class and provide the necessary data files.

### Data Requirements

* **Road Network Shapefile:** A shapefile (`.shp`) containing the road network. The roads should be split at intersections.
* **Bus Stop Locations CSV:** A CSV file with bus stop information, including ID, name, and coordinates.
* **Bus Timetable CSV:** A CSV file containing bus schedule information, such as route ID, operating times, and service intervals.
* **Bus Route Interval Distance CSV:** A CSV file defining the sequence of bus stops for each bus route.

### Example Code

Here is an example of how to use the `ApriPathFinder` to find a path:

```java
import graph_routing_01.Finder.ApriPathFinder;
import graph_routing_01.Finder.model.ApriPath;
import java.io.File;

public class Main {
    public static void main(String[] args) throws Exception {
        ApriPathFinder apf = new ApriPathFinder();

        // 1. Build the base graph from the road network shapefile
        apf.buildBaseGraph(apf.readSHP("data_folder/simplified_split2.shp"));

        // 2. Add bus stop nodes to the graph
        apf.addBusstopNodes("data_folder/busstop_location.csv");

        // 3. Initialize the bus timetable
        apf.initBusTimeTable(new File("data_folder/bus_timetable.csv"));

        // 4. Add bus route edges to the graph
        apf.addBusRouteEdges("data_folder/route_interval_distance.csv");

        // 5. Define start and end coordinates (WGS84)
        double stLat = 37.449910;
        double stLon = 126.670039;
        double endLat = 37.536697;
        double endLon = 126.728416;

        // 6. Find the path
        System.out.println("Finding Path");
        ApriPath path = apf.findPath(stLon, stLat, endLon, endLat);
        System.out.println("Path Found");

        if (path == null) {
            System.err.println("Failed to find the path.");
            return;
        }

        // 7. (Optional) Save the path to a shapefile
        apf.pathToShp(path, "result/path.shp");

        // 8. (Optional) Convert path to WGS84 for mapping
        ApriPath pathWGS84 = apf.pathToWGS84(path);

        // Print path details
        System.out.println("Total Time (s): " + pathWGS84.totalTime);
    }
}
