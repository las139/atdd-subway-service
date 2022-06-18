package nextstep.subway.path.application;

import nextstep.subway.auth.domain.LoginMember;
import nextstep.subway.line.domain.Line;
import nextstep.subway.line.domain.LineRepository;
import nextstep.subway.line.domain.Sections;
import nextstep.subway.path.domain.AgeDiscountPolicy;
import nextstep.subway.path.domain.AgeType;
import nextstep.subway.path.domain.DijkstraShortestPathStrategy;
import nextstep.subway.path.domain.DiscountPolicy;
import nextstep.subway.path.domain.FareType;
import nextstep.subway.path.domain.Path;
import nextstep.subway.path.domain.PathFinder;
import nextstep.subway.path.dto.PathResponse;
import nextstep.subway.station.application.StationService;
import nextstep.subway.station.domain.Station;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.stream.Collectors;

@Service
public class PathService {
    private final StationService stationService;
    private final LineRepository lineRepository;
    private final PathFinder pathFinder;

    public PathService(StationService stationService, LineRepository lineRepository, PathFinder pathFinder) {
        this.stationService = stationService;
        this.lineRepository = lineRepository;
        this.pathFinder = pathFinder;
    }

    public PathResponse findShortestPath(LoginMember loginMember, Long sourceStationId, Long targetStationId) {
        Station sourceStation = stationService.findById(sourceStationId);
        Station targetStation = stationService.findById(targetStationId);
        List<Sections> sections = findAllSections();
        pathFinder.decideShortestPathStrategy(new DijkstraShortestPathStrategy());
        Path shortestPath = pathFinder.findShortestPath(sections, sourceStation, targetStation);
        shortestPath.calculate(new AgeDiscountPolicy(), loginMember);

        return PathResponse.of(shortestPath);
    }

    private List<Sections> findAllSections() {
        List<Line> lines = lineRepository.findAll();
        return lines.stream()
                .map(Line::getSections)
                .collect(Collectors.toList());
    }
}
