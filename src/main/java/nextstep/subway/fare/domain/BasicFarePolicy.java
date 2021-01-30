package nextstep.subway.fare.domain;

import org.springframework.stereotype.Component;

import nextstep.subway.fare.dto.Fare;
import nextstep.subway.path.dto.Path;

@Component
public class BasicFarePolicy implements FarePolicy {
	public static int BASIC_FARE = 1_250;

	@Override
	public Fare calculate(Path path) {
		return Fare.from(BASIC_FARE);
	}

}