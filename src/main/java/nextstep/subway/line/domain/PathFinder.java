package nextstep.subway.line.domain;

import nextstep.subway.exception.NotEnrollStationInGraphException;
import nextstep.subway.station.domain.Station;
import nextstep.subway.station.domain.Stations;
import org.jgrapht.GraphPath;
import org.jgrapht.alg.shortestpath.DijkstraShortestPath;
import org.jgrapht.graph.DefaultWeightedEdge;
import org.jgrapht.graph.WeightedMultigraph;

public class PathFinder {
    private final WeightedMultigraph<Station, DefaultWeightedEdge> graph;

    public PathFinder() {
        this.graph = new WeightedMultigraph<>(DefaultWeightedEdge.class);
    }

    public PathFinder enrollPaths(Sections sections) {
        return createGraph(sections);
    }

    public GraphPath<Station, DefaultWeightedEdge> findPaths(final Station source, final Station target) {
        if (isNotEnrolledStation(source) || isNotEnrolledStation(target)) {
            throw new NotEnrollStationInGraphException();
        }
        DijkstraShortestPath<Station, DefaultWeightedEdge> dijkstraShortestPath = new DijkstraShortestPath<>(graph);
        return dijkstraShortestPath.getPath(source, target);
    }

    private boolean isNotEnrolledStation(final Station station) {
        return !graph.containsVertex(station);
    }

    private PathFinder createGraph(Sections sections) {
        addGraphVertex(sections.getAllStationsBySections());
        sections.getSections()
                .forEach(this::setSectionDistance);
        return this;
    }

    private void setSectionDistance(final Section section) {
        graph.setEdgeWeight(addSection(section), section.getDistance());
    }

    private void addGraphVertex(final Stations stations) {
        stations.getStations()
                .forEach(graph::addVertex);
    }

    private DefaultWeightedEdge addSection(final Section section) {
        return graph.addEdge(section.getUpStation(), section.getDownStation());
    }

    public WeightedMultigraph<Station, DefaultWeightedEdge> getGraph() {
        return graph;
    }
}