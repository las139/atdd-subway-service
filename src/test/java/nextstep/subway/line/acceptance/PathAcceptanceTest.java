package nextstep.subway.line.acceptance;

import static java.util.stream.Collectors.toList;
import static nextstep.subway.auth.acceptance.AuthAcceptanceTest.로그인_요청;
import static nextstep.subway.line.acceptance.LineAcceptanceTest.지하철_노선_등록되어_있음;
import static nextstep.subway.member.MemberAcceptanceTest.AGE;
import static nextstep.subway.member.MemberAcceptanceTest.EMAIL;
import static nextstep.subway.member.MemberAcceptanceTest.PASSWORD;
import static nextstep.subway.member.MemberAcceptanceTest.회원_생성을_요청;
import static org.assertj.core.api.Assertions.assertThat;

import io.restassured.RestAssured;
import io.restassured.response.ExtractableResponse;
import io.restassured.response.Response;
import java.util.List;
import java.util.stream.Collectors;
import nextstep.subway.AcceptanceTest;
import nextstep.subway.auth.dto.TokenResponse;
import nextstep.subway.line.dto.LineResponse;
import nextstep.subway.station.StationAcceptanceTest;
import nextstep.subway.station.dto.StationResponse;
import org.assertj.core.api.Assertions;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;


@DisplayName("지하철 경로 조회")
public class PathAcceptanceTest extends AcceptanceTest {

    private LineResponse 신분당선;
    private LineResponse 이호선;
    private LineResponse 삼호선;
    private StationResponse 강남역;
    private StationResponse 양재역;
    private StationResponse 교대역;
    private StationResponse 남부터미널역;

    /**
     * 교대역    --- *2호선* ---   강남역
     * |                        |
     * *3호선*                   *신분당선*
     * |                        |
     * 남부터미널역  --- *3호선* ---   양재
     */
    @BeforeEach
    public void setUp() {
        super.setUp();

        강남역 = StationAcceptanceTest.지하철역_등록되어_있음("강남역").as(StationResponse.class);
        양재역 = StationAcceptanceTest.지하철역_등록되어_있음("양재역").as(StationResponse.class);
        교대역 = StationAcceptanceTest.지하철역_등록되어_있음("교대역").as(StationResponse.class);
        남부터미널역 = StationAcceptanceTest.지하철역_등록되어_있음("남부터미널역").as(StationResponse.class);

        신분당선 = 지하철_노선_등록되어_있음("신분당선", "bg-red-600", 강남역, 양재역, 10);
        이호선 = 지하철_노선_등록되어_있음("이호선", "bg-red-600", 교대역, 강남역, 10);
        삼호선 = 지하철_노선_등록되어_있음("삼호선", "bg-red-600", 교대역, 남부터미널역, 5);
    }

    /**
     * 교대역    --- *2호선* ---   강남역
     * |                        |
     * *3호선*                   *신분당선*
     * |                        |
     * 남부터미널역  --- *3호선* ---   양재
     */
    @Test
    @DisplayName("가중치에 가장빠른 거리를 조회한다.")
    void 최단_경로_조회() {

        // when
        ExtractableResponse<Response> lineResponse = 지하철_경로를_조회(교대역, 양재역);

        // then
        지하철_경로_응답됨(lineResponse);
        가장_빠른_지하철노선_조회(lineResponse, 교대역, 강남역, 양재역);
        지하철_이용_요금(lineResponse, 1450L);
    }

    @Test
    @DisplayName("가중치에 가장빠른 거리를 조회한다.")
    void 로그인한_유저가_최단_경로_조회() {

        회원_생성을_요청(EMAIL, PASSWORD, 13);
        TokenResponse tokenResponse = 로그인_요청(EMAIL, PASSWORD).as(TokenResponse.class);

        // when
        ExtractableResponse<Response> lineResponse = 지하철_경로를_조회(tokenResponse, 교대역, 양재역);

        // then
        지하철_경로_응답됨(lineResponse);
        가장_빠른_지하철노선_조회(lineResponse, 교대역, 강남역, 양재역);
        지하철_이용_요금(lineResponse,900L);
    }


    private void 지하철_이용_요금(ExtractableResponse<Response> lineResponse, Long expectedFare) {
        Long fare = lineResponse.jsonPath().getObject("fare", Long.class);
        Assertions.assertThat(fare).isEqualTo(expectedFare);
    }

    public static ExtractableResponse<Response> 지하철_경로를_조회(StationResponse upStation , StationResponse downStation) {
        return RestAssured
            .given().log().all()
            .param("source", upStation.getId())
            .param("target", downStation.getId())
            .when().get("/paths")
            .then().log().all().extract();
    }

    public static ExtractableResponse<Response> 지하철_경로를_조회(TokenResponse tokenResponse, StationResponse upStation , StationResponse downStation) {
        return RestAssured
            .given().log().all()
            .auth().oauth2(tokenResponse.getAccessToken())
            .param("source", upStation.getId())
            .param("target", downStation.getId())
            .when().get("/paths")
            .then().log().all().extract();
    }

    public static void 지하철_경로_응답됨(ExtractableResponse<Response> response) {
        assertThat(response.statusCode()).isEqualTo(HttpStatus.OK.value());
    }

    public void 가장_빠른_지하철노선_조회(ExtractableResponse<Response> response, StationResponse... stationResponse) {
        List<StationResponse> stations = response.jsonPath()
            .getList("stations", StationResponse.class);
        Double distance = response.jsonPath().getObject("distance", Double.class);
        List<String> stationsName = stations.stream()
            .map(StationResponse::getName)
            .collect(toList());
        Assertions.assertThat(stationsName).containsExactly("교대역", "강남역", "양재역");
        Assertions.assertThat(distance).isEqualTo(20);
    }
}