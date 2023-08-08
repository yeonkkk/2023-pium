package com.official.pium.service;

import com.official.pium.IntegrationTest;
import com.official.pium.domain.History;
import com.official.pium.domain.Member;
import com.official.pium.domain.PetPlant;
import com.official.pium.repository.HistoryRepository;
import com.official.pium.service.dto.HistoryResponse;
import org.assertj.core.api.SoftAssertions;
import org.junit.jupiter.api.DisplayNameGeneration;
import org.junit.jupiter.api.DisplayNameGenerator;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Sort;

import java.time.LocalDate;
import java.util.NoSuchElementException;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

@DisplayNameGeneration(DisplayNameGenerator.ReplaceUnderscores.class)
@SuppressWarnings("NonAsciiCharacters")
public class HistoryServiceTest extends IntegrationTest {

    @Autowired
    private HistoryService historyService;

    @Autowired
    private HistoryRepository historyRepository;

    @Test
    void 반려_식물_단건_히스토리_조회() {
        PetPlant petPlant = petPlantSupport.builder().build();
        History history1 = createHistory(petPlant, LocalDate.now());
        History history2 = createHistory(petPlant, LocalDate.now().plusDays(1));
        History history3 = createHistory(petPlant, LocalDate.now().plusDays(2));
        History history4 = createHistory(petPlant, LocalDate.now().plusDays(3));
        HistoryResponse historyResponse1 = historyService.read(petPlant.getId(), PageRequest.of(0, 2, Sort.Direction.DESC, "waterDate"), petPlant.getMember());
        HistoryResponse historyResponse2 = historyService.read(petPlant.getId(), PageRequest.of(1, 2, Sort.Direction.DESC, "waterDate"), petPlant.getMember());

        SoftAssertions.assertSoftly(
                softly -> {
                    softly.assertThat(historyResponse1.getWaterDateList().get(0)).isEqualTo(history4.getWaterDate());
                    softly.assertThat(historyResponse1.getWaterDateList().get(1)).isEqualTo(history3.getWaterDate());
                    softly.assertThat(historyResponse1.getPage()).isEqualTo(0);
                    softly.assertThat(historyResponse1.getSize()).isEqualTo(2);
                    softly.assertThat(historyResponse1.getElementSize()).isEqualTo(4L);
                    softly.assertThat(historyResponse1.isHasNext()).isTrue();
                    softly.assertThat(historyResponse2.getWaterDateList().get(0)).isEqualTo(history2.getWaterDate());
                    softly.assertThat(historyResponse2.getWaterDateList().get(1)).isEqualTo(history1.getWaterDate());
                    softly.assertThat(historyResponse2.getPage()).isEqualTo(1);
                    softly.assertThat(historyResponse2.getSize()).isEqualTo(2);
                    softly.assertThat(historyResponse2.getElementSize()).isEqualTo(4L);
                    softly.assertThat(historyResponse2.isHasNext()).isFalse();
                }
        );
    }

    @Test
    void 반려식물_id에_해당하는_반려식물이_없으면_예외발생() {
        PetPlant petPlant = petPlantSupport.builder().build();

        assertThatThrownBy(
                () -> historyService.read(2L, PageRequest.of(0, 2, Sort.Direction.DESC, "waterDate"), petPlant.getMember())
        ).isInstanceOf(NoSuchElementException.class)
                .hasMessage("일치하는 반려 식물이 존재하지 않습니다. id :" + 2L);
    }

    @Test
    void 반려식물의_소유자와_파라미터의_멤버가_같지_않으면_예외발생() {
        PetPlant petPlant = petPlantSupport.builder().build();
        Member member = memberSupport.builder().build();

        assertThatThrownBy(
                () -> historyService.read(petPlant.getId(), PageRequest.of(0, 2, Sort.Direction.DESC, "waterDate"), member)
        ).isInstanceOf(IllegalArgumentException.class)
                .hasMessage("요청 사용자와 반려 식물의 사용자가 일치하지 않습니다. id :" + member.getId());
    }

    @Test
    void 조회하는_페이지가_마지막_페이지면_hasNext_값이_false() {
        PetPlant petPlant = petPlantSupport.builder().build();
        historySupport.builder().petPlant(petPlant).build();
        historySupport.builder().petPlant(petPlant).build();

        HistoryResponse historyResponse = historyService.read(petPlant.getId(), PageRequest.of(0, 2, Sort.Direction.DESC, "waterDate"), petPlant.getMember());

        assertThat(historyResponse.isHasNext()).isFalse();
    }

    private History createHistory(PetPlant petPlant, LocalDate date) {
        History history = History.builder()
                .petPlant(petPlant)
                .waterDate(date)
                .build();
        return historyRepository.save(history);
    }
}