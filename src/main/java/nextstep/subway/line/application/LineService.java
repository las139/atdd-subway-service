package nextstep.subway.line.application;

import nextstep.subway.exception.LineException;
import nextstep.subway.exception.LineNotFoundException;
import nextstep.subway.line.domain.line.Line;
import nextstep.subway.line.domain.line.LineRepository;
import nextstep.subway.line.domain.section.Section;
import nextstep.subway.line.dto.LineRequest;
import nextstep.subway.line.dto.LineResponse;
import nextstep.subway.line.dto.SectionRequest;
import nextstep.subway.station.application.StationService;
import nextstep.subway.station.domain.Station;
import nextstep.subway.station.dto.StationResponse;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.stream.Collectors;

@Service
@Transactional
public class LineService {
    private final LineRepository lineRepository;
    private final StationService stationService;

    public LineService(LineRepository lineRepository, StationService stationService) {
        this.lineRepository = lineRepository;
        this.stationService = stationService;
    }

    public List<LineResponse> findLines() {
        List<Line> persistLines = findAllLine();

        return persistLines.stream()
                .map(line -> {
                    List<StationResponse> stations = line.getStations().stream()
                            .map(it -> StationResponse.of(it))
                            .collect(Collectors.toList());
                    return LineResponse.of(line, stations);
                })
                .collect(Collectors.toList());
    }

    public LineResponse findLineResponseById(Long id) {
        Line persistLine = findOneLine(id);

        List<StationResponse> stations = persistLine.getStations().stream()
                .map(it -> StationResponse.of(it))
                .collect(Collectors.toList());
        return LineResponse.of(persistLine, stations);
    }

    public LineResponse saveLine(LineRequest request) {
        Station upStation = stationService.findById(request.getUpStationId());
        Station downStation = stationService.findById(request.getDownStationId());

        Line persistLine = lineRepository.save(new Line(request.getName(), request.getColor(), upStation, downStation, request.getDistance()));

        List<StationResponse> stations = persistLine.getStations()
                .stream()
                .map(it -> StationResponse.of(it))
                .collect(Collectors.toList());
        return LineResponse.of(persistLine, stations);
    }

    public void updateLine(Long id, LineRequest lineUpdateRequest) {
        Line persistLine = findOneLine(id);

        persistLine.update(new Line(lineUpdateRequest.getName(), lineUpdateRequest.getColor()));
    }

    public void deleteLineById(Long id) {
        lineRepository.deleteById(id);
    }

    public void addLineStation(Long lineId, SectionRequest request) {
        Line line = findOneLine(lineId);
        Station upStation = stationService.findStationById(request.getUpStationId());
        Station downStation = stationService.findStationById(request.getDownStationId());

        line.addSection(new Section(line, upStation, downStation, request.getDistance()));
    }

    public void removeLineStation(Long lineId, Long stationId) {
        Line line = findOneLine(lineId);
        Station removeStation = stationService.findStationById(stationId);

        line.removeSection(removeStation);
    }

    private List<Line> findAllLine() {
        return lineRepository.findAll();
    }

    private Line findOneLine(Long id) {
        return lineRepository.findLineWithSectionsById(id)
                .orElseThrow(() -> new LineNotFoundException("노선이 존재하지 않습니다."));
    }

}
