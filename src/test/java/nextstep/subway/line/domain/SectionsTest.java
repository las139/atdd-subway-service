package nextstep.subway.line.domain;

import nextstep.subway.station.domain.Station;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.junit.jupiter.api.Assertions.assertAll;

class SectionsTest {

    private final static Line 신분당선 = new Line("신분당선", "red");
    private final static Station 신논현역 = new Station("신논현");
    private final static Station 강남역 = new Station("강남역");
    private final static Station 양재역 = new Station("양재역");
    private final static Station 오금역 = new Station("오금역");
    private final static Station 송파역 = new Station("송파역");

    @Test
    void 정렬된_역_반환() {
        // given
        Sections sections = new Sections();
        Section section1 = new Section(신분당선, 신논현역, 강남역, 5);
        Section section2 = new Section(신분당선, 강남역, 양재역, 3);
        sections.add(section1);
        sections.add(section2);

        // when
        List<Station> actual = sections.getStations();

        // then
        assertThat(actual).containsExactly(신논현역, 강남역, 양재역);
    }

    @Test
    void 역_사이에_새로운_역을_등록할_경우() {
        // given
        Sections sections = new Sections();
        Section section1 = new Section(신분당선, 신논현역, 양재역, 5);
        sections.add(section1);

        // when
        Section section2 = new Section(신분당선, 신논현역, 강남역, 3);
        sections.add(section2);

        // then
        assertAll(
                () -> assertThat(section1.getDistance()).isEqualTo(new Distance(2)),
                () -> assertThat(sections.getStations()).containsExactly(신논현역, 강남역, 양재역)
        );
    }

    @Test
    void 새로운_역을_상행_또는_하행_종점으로_등록할_경우() {
        // given
        Sections sections = new Sections();
        Section section1 = new Section(신분당선, 신논현역, 강남역, 5);
        sections.add(section1);

        // when
        Section section2 = new Section(신분당선, 강남역, 양재역, 3);
        sections.add(section2);

        // then
        assertThat(sections.getStations()).containsExactly(신논현역, 강남역, 양재역);
    }


    @DisplayName("역 사이에 새로운 역을 등록할 경우 기존 역 사이 길이보다 크거나 같으면 등록을 할 수 없음")
    @Test
    void 구간_등록_시_예외_케이스_1() {
        // given
        Sections sections = new Sections();
        Section section1 = new Section(신분당선, 신논현역, 양재역, 5);
        sections.add(section1);

        // when & then
        Section section2 = new Section(신분당선, 신논현역, 강남역, 5);
        assertThatThrownBy(() -> sections.add(section2)).isInstanceOf(IllegalArgumentException.class);
    }

    @DisplayName("상행역과 하행역이 이미 노선에 모두 등록되어 있다면 추가할 수 없음")
    @Test
    void 구간_등록_시_예외_케이스_2() {
        // given
        Sections sections = new Sections();
        Section section1 = new Section(신분당선, 신논현역, 강남역, 5);
        Section section2 = new Section(신분당선, 강남역, 양재역, 3);
        sections.add(section1);
        sections.add(section2);

        // when & then
        Section section3 = new Section(신분당선, 신논현역, 양재역, 2);
        assertThatThrownBy(() -> sections.add(section3)).isInstanceOf(IllegalArgumentException.class);

    }

    @DisplayName("상행역과 하행역 둘 중 하나도 포함되어있지 않으면 추가할 수 없음")
    @Test
    void 구간_등록_시_예외_케이스_3() {
        // given
        Sections sections = new Sections();
        Section section1 = new Section(신분당선, 신논현역, 강남역, 5);
        Section section2 = new Section(신분당선, 강남역, 양재역, 3);
        sections.add(section1);
        sections.add(section2);

        // when & then
        Section section3 = new Section(신분당선, 오금역, 송파역, 2);
        assertThatThrownBy(() -> sections.add(section3)).isInstanceOf(IllegalArgumentException.class);
    }

    @Test
    void 종점이_제거될_경우_다음으로_오던_역이_종점이_됨() {
        // given
        Sections sections = new Sections();
        Section section1 = new Section(신분당선, 신논현역, 강남역, 5);
        Section section2 = new Section(신분당선, 강남역, 양재역, 3);
        sections.add(section1);
        sections.add(section2);

        // when
        sections.deleteStation(양재역, 신분당선);

        // then
        assertAll(
                () -> assertThat(sections.getStations()).containsExactly(신논현역, 강남역),
                () -> assertThat(sections.getSections()).hasSize(1)
        );
    }

    @Test
    void 중간역이_제거될_경우_재배치를_함() {
        // given
        Sections sections = new Sections();
        Section section1 = new Section(신분당선, 신논현역, 강남역, 5);
        Section section2 = new Section(신분당선, 강남역, 양재역, 3);
        sections.add(section1);
        sections.add(section2);

        // when
        sections.deleteStation(강남역, 신분당선);

        // then
        assertAll(
                () -> assertThat(sections.getStations()).containsExactly(신논현역, 양재역),
                () -> assertThat(sections.getSections()).hasSize(1)
        );
    }

    @DisplayName("노선에 등록되어있지 않은 역을 제거")
    @Test
    void 구간_역_삭제_시_예외_케이스_1() {
        // given
        Sections sections = new Sections();
        Section section1 = new Section(신분당선, 신논현역, 강남역, 5);
        Section section2 = new Section(신분당선, 강남역, 양재역, 3);
        sections.add(section1);
        sections.add(section2);

        // when & then
        assertThatThrownBy(() -> sections.deleteStation(오금역, 신분당선)).isInstanceOf(IllegalArgumentException.class);
    }

    @DisplayName("구간이 하나인 노선에서 마지막 구간을 제거")
    @Test
    void 구간_역_삭제_시_예외_케이스_2() {
        // given
        Sections sections = new Sections();
        Section section = new Section(신분당선, 신논현역, 강남역, 5);
        sections.add(section);

        // when & then
        assertThatThrownBy(() -> sections.deleteStation(신논현역, 신분당선)).isInstanceOf(IllegalArgumentException.class);
        assertThatThrownBy(() -> sections.deleteStation(강남역, 신분당선)).isInstanceOf(IllegalArgumentException.class);
    }

}