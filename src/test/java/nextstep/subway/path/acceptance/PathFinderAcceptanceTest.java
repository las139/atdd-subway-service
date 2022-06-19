package nextstep.subway.path.acceptance;

import io.restassured.RestAssured;
import io.restassured.response.ExtractableResponse;
import io.restassured.response.Response;
import nextstep.subway.AcceptanceTest;
import nextstep.subway.line.dto.LineRequest;
import nextstep.subway.line.dto.LineResponse;
import nextstep.subway.path.dto.PathResponse;
import nextstep.subway.station.StationAcceptanceTest;
import nextstep.subway.station.dto.StationResponse;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;

import java.time.LocalDateTime;
import java.util.Arrays;
import java.util.List;
import java.util.stream.Collectors;

import static nextstep.subway.line.acceptance.LineAcceptanceTest.지하철_노선_등록되어_있음;
import static nextstep.subway.line.acceptance.LineSectionAcceptanceTest.지하철_노선에_지하철역_등록_요청;
import static org.assertj.core.api.Assertions.assertThat;


@DisplayName("지하철 경로 조회")
class PathFinderAcceptanceTest extends AcceptanceTest {

    private LineResponse 신분당선;
    private LineResponse 이호선;
    private LineResponse 삼호선;
    private StationResponse 강남역;
    private StationResponse 양재역;
    private StationResponse 교대역;
    private StationResponse 남부터미널역;

    /**
     * 10
     * 교대역    --- *2호선* ---   강남역
     * |                        |
     * 3 * *3호선*                   *신분당선* 10
     * |               2        |
     * 남부터미널역  --- *3호선* ---   양재
     */
    @BeforeEach
    public void setUp() {
        super.setUp();

        강남역 = StationAcceptanceTest.지하철역_등록되어_있음("강남역").as(StationResponse.class);
        양재역 = StationAcceptanceTest.지하철역_등록되어_있음("양재역").as(StationResponse.class);
        교대역 = StationAcceptanceTest.지하철역_등록되어_있음("교대역").as(StationResponse.class);
        남부터미널역 = StationAcceptanceTest.지하철역_등록되어_있음("남부터미널역").as(StationResponse.class);

        신분당선 = 지하철_노선_등록되어_있음(new LineRequest("신분당선", "bg-red-600", 강남역.getId(), 양재역.getId(), 10)).as(LineResponse.class);
        이호선 = 지하철_노선_등록되어_있음(new LineRequest("이호선", "bg-red-600", 교대역.getId(), 강남역.getId(), 10)).as(LineResponse.class);
        삼호선 = 지하철_노선_등록되어_있음(new LineRequest("삼호선", "bg-red-600", 교대역.getId(), 양재역.getId(), 5)).as(LineResponse.class);
        지하철_노선에_지하철역_등록_요청(삼호선, 남부터미널역, 양재역, 2);
    }

    @Test
    void 같은_호선의_목적지로_경로_조회_하여_최적경로를_구할_수_있다() {
        // when
        ExtractableResponse<Response> response = 경로_조회_요청(교대역, 양재역);

        // then
        경로_조회가_최적거리로_조회됨(response, Arrays.asList(교대역, 남부터미널역, 양재역), 5);
    }

    @Test
    void 다른_호선의_목적지로_경로_조회_하여_최적경로를_구할_수_있다() {
        // when
        ExtractableResponse<Response> response = 경로_조회_요청(강남역, 남부터미널역);

        // then
        경로_조회가_최적거리로_조회됨(response, Arrays.asList(강남역, 양재역, 남부터미널역), 12);
    }

    @Test
    void 존재하지_않은_출발역에_대한_최단거리를_구할_수_없다() {
        StationResponse 없는역 = new StationResponse(9999L, "없는역", LocalDateTime.now(), LocalDateTime.now());
        // when
        ExtractableResponse<Response> response = 경로_조회_요청(없는역, 남부터미널역);

        // then
        역을_찾지_못하여_경로조회를_실패함(response);
    }

    @Test
    void 존재하지_않은_도착역에_대한_최단거리를_구할_수_없다() {
        StationResponse 없는역 = new StationResponse(9999L, "없는역", LocalDateTime.now(), LocalDateTime.now());

        // when
        ExtractableResponse<Response> response = 경로_조회_요청(강남역, 없는역);

        // then
        역을_찾지_못하여_경로조회를_실패함(response);
    }

    private ExtractableResponse<Response> 경로_조회_요청(StationResponse sourceResponse, StationResponse targetResponse) {
        return RestAssured
                .given().log().all()
                .accept(MediaType.APPLICATION_JSON_VALUE)
                .when().get("/paths?source={sourceId}&target={targetId}", sourceResponse.getId(), targetResponse.getId())
                .then().log().all()
                .extract();
    }

    private void 경로_조회가_최적거리로_조회됨(ExtractableResponse<Response> response, List<StationResponse> expectedStations, int distance) {
        PathResponse path = response.as(PathResponse.class);

        List<Long> stationIds = path.getStations().stream()
                .map(StationResponse::getId)
                .collect(Collectors.toList());

        List<Long> expectedStationIds = expectedStations.stream()
                .map(StationResponse::getId)
                .collect(Collectors.toList());

        assertThat(stationIds).containsExactlyElementsOf(expectedStationIds);
        assertThat(path.getDistance()).isEqualTo(distance);
    }

    private void 역을_찾지_못하여_경로조회를_실패함(ExtractableResponse<Response> response) {
        assertThat(response.statusCode()).isEqualTo(HttpStatus.NOT_FOUND.value());
    }
}
