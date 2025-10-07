package graph_routing_01.Finder.model;

import java.util.Collections;
import java.util.LinkedHashMap;
import java.util.Map;

import org.geotools.data.simple.SimpleFeatureCollection;
import org.geotools.feature.collection.DecoratingSimpleFeatureCollection;

public final class MyFeatureCollection extends DecoratingSimpleFeatureCollection {

    private final Map<String, Object> meta = new LinkedHashMap<>();

    public static MyFeatureCollection of(SimpleFeatureCollection delegate) {
        return new MyFeatureCollection(delegate);
    }

    private MyFeatureCollection(SimpleFeatureCollection delegate) {
        super(delegate);
    }

    // optional metadata helpers
    public MyFeatureCollection with(String k, Object v) { meta.put(k, v); return this; }
    public Map<String, Object> metadata() { return Collections.unmodifiableMap(meta); }

    // unwrap if library internals need the raw SFC
    public SimpleFeatureCollection delegate() { return this.delegate; }
}