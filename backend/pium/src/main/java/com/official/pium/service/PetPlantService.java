package com.official.pium.service;

import com.official.pium.domain.DictionaryPlant;
import com.official.pium.domain.History;
import com.official.pium.domain.HistoryType;
import com.official.pium.domain.Member;
import com.official.pium.domain.PetPlant;
import com.official.pium.event.history.HistoryEvent;
import com.official.pium.event.history.HistoryEvents;
import com.official.pium.event.history.LastWaterDateEvent;
import com.official.pium.event.history.PetPlantHistory;
import com.official.pium.mapper.PetPlantMapper;
import com.official.pium.repository.DictionaryPlantRepository;
import com.official.pium.repository.HistoryRepository;
import com.official.pium.repository.PetPlantRepository;
import com.official.pium.service.dto.DataResponse;
import com.official.pium.service.dto.PetPlantCreateRequest;
import com.official.pium.service.dto.PetPlantResponse;
import com.official.pium.service.dto.PetPlantUpdateRequest;
import com.official.pium.service.dto.SinglePetPlantResponse;
import java.time.LocalDate;
import java.util.List;
import java.util.NoSuchElementException;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.ApplicationEventPublisher;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Slice;
import org.springframework.data.domain.Sort.Direction;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.multipart.MultipartFile;

@Service
@Transactional(readOnly = true)
@RequiredArgsConstructor
public class PetPlantService {

    @Value("${petPlant.image.directory}")
    private String workingDirectory;

    @Value("${aws.s3.root}")
    private String rootPath;

    private final PetPlantRepository petPlantRepository;
    private final DictionaryPlantRepository dictionaryPlantRepository;
    private final HistoryRepository historyRepository;
    private final PhotoManager photoManager;
    private final ApplicationEventPublisher publisher;

    @Transactional
    public PetPlantResponse create(PetPlantCreateRequest request, MultipartFile image, Member member) {
        DictionaryPlant dictionaryPlant = dictionaryPlantRepository.findById(request.getDictionaryPlantId())
                .orElseThrow(
                        () -> new NoSuchElementException("사전 식물이 존재하지 않습니다. id: " + request.getDictionaryPlantId()));

        String imagePath = saveImageIfExists(image, dictionaryPlant.getImageUrl());
        PetPlant petPlant = PetPlantMapper.toPetPlant(request, dictionaryPlant, imagePath, member);
        petPlantRepository.save(petPlant);

        createHistory(petPlant);

        long dday = petPlant.calculateDday(LocalDate.now());
        long daySince = petPlant.calculateDaySince(LocalDate.now());
        return PetPlantMapper.toPetPlantResponse(petPlant, dday, daySince);
    }

    private String saveImageIfExists(MultipartFile image, String imageDefaultUrl) {
        if (image == null || image.isEmpty()) {
            return imageDefaultUrl;
        }
        return photoManager.upload(image, workingDirectory);
    }

    private void createHistory(PetPlant petPlant) {
        PetPlantHistory petPlantHistory = PetPlantMapper.toPetPlantHistory(petPlant);
        List<HistoryEvent> historyEvents = petPlantHistory.generateCreateHistoryEvents(petPlant.getId(),
                LocalDate.now());

        publisher.publishEvent(HistoryEvents.from(historyEvents));
    }

    public PetPlantResponse read(Long id, Member member) {
        PetPlant petPlant = petPlantRepository.findById(id)
                .orElseThrow(() -> new NoSuchElementException("일치하는 반려 식물이 존재하지 않습니다. id: " + id));

        checkOwner(petPlant, member);

        Long dday = petPlant.calculateDday(LocalDate.now());
        Long daySince = petPlant.calculateDaySince(LocalDate.now());

        Slice<History> secondLastWaterDatePage = historyRepository.findAllByPetPlantIdAndHistoryCategoryHistoryType(
                petPlant.getId(), HistoryType.LAST_WATER_DATE, PageRequest.of(1, 1, Direction.DESC, "date"));
        if (!secondLastWaterDatePage.isEmpty()) {
            LocalDate secondLastWaterDate = secondLastWaterDatePage.getContent().get(0).getDate();
            return PetPlantMapper.toPetPlantResponse(petPlant, dday, daySince, secondLastWaterDate);
        }
        return PetPlantMapper.toPetPlantResponse(petPlant, dday, daySince);
    }

    public DataResponse<List<SinglePetPlantResponse>> readAll(Member member) {
        List<PetPlant> petPlants = petPlantRepository.findAllByMemberId(member.getId());

        List<SinglePetPlantResponse> singlePetPlantResponses = petPlants.stream()
                .map(petPlant -> PetPlantMapper.toSinglePetPlantResponse(petPlant,
                        petPlant.calculateDaySince(LocalDate.now())))
                .toList();

        return DataResponse.<List<SinglePetPlantResponse>>builder().data(singlePetPlantResponses).build();
    }

    @Transactional
    public void update(Long id, PetPlantUpdateRequest updateRequest, MultipartFile image, Member member) {
        PetPlant petPlant = petPlantRepository.findById(id)
                .orElseThrow(() -> new NoSuchElementException("일치하는 반려 식물이 존재하지 않습니다. id: " + id));

        checkOwner(petPlant, member);

        validateLastWaterDate(updateRequest, petPlant);

        String imageUrl = updateImage(image, petPlant.getImageUrl());
        PetPlantHistory previousPetPlantHistory = PetPlantMapper.toPetPlantHistory(petPlant);
        petPlant.updatePetPlant(
                updateRequest.getNickname(), updateRequest.getLocation(),
                updateRequest.getFlowerpot(), updateRequest.getLight(),
                updateRequest.getWind(), updateRequest.getWaterCycle(),
                updateRequest.getBirthDate(), updateRequest.getLastWaterDate(),
                imageUrl
        );
        PetPlantHistory currentPetPlantHistory = PetPlantMapper.toPetPlantHistory(petPlant);
        publishPetPlantHistories(petPlant, previousPetPlantHistory, currentPetPlantHistory);
    }

    private String updateImage(MultipartFile image, String originalImageUrl) {
        if (image == null || image.isEmpty()) {
            return originalImageUrl;
        }
        deleteImageIfExists(originalImageUrl);
        return photoManager.upload(image, workingDirectory);
    }

    private void deleteImageIfExists(String originalImageUrl) {
        if (originalImageUrl.contains(rootPath + "/petPlant")) {
            String fileName = originalImageUrl.substring(rootPath.length() - 1);
            photoManager.delete(fileName);
        }
    }

    private void publishPetPlantHistories(PetPlant petPlant, PetPlantHistory previousPetPlantHistory,
                                          PetPlantHistory currentPetPlantHistory) {
        List<HistoryEvent> historyEvents = previousPetPlantHistory.generateUpdateHistoryEvents(petPlant.getId(),
                currentPetPlantHistory, LocalDate.now());
        for (HistoryEvent historyEvent : historyEvents) {
            publisher.publishEvent(historyEvent);
        }

        LastWaterDateEvent lastWaterDateEvent = previousPetPlantHistory.generateUpdateLastWaterDateHistoryEvent(
                petPlant.getId(), petPlant.getLastWaterDate());
        if (lastWaterDateEvent != null) {
            publisher.publishEvent(lastWaterDateEvent);
        }
    }

    @Transactional
    public void delete(Long id, Member member) {
        PetPlant petPlant = petPlantRepository.findById(id)
                .orElseThrow(() -> new NoSuchElementException("일치하는 반려 식물이 존재하지 않습니다. id: " + id));

        checkOwner(petPlant, member);

        historyRepository.deleteAllByPetPlantId(petPlant.getId());
        petPlantRepository.deleteById(petPlant.getId());
    }

    private void checkOwner(PetPlant petPlant, Member member) {
        if (petPlant.isNotOwnerOf(member)) {
            throw new IllegalArgumentException("요청 사용자와 반려 식물의 사용자가 일치하지 않습니다. memberId: " + member.getId());
        }
    }

    private void validateLastWaterDate(PetPlantUpdateRequest updateRequest, PetPlant petPlant) {
        int pageNumber = 1;
        int pageSize = 1;
        Slice<History> currentHistory = historyRepository.findAllByPetPlantIdAndHistoryCategoryHistoryType(
                petPlant.getId(), HistoryType.LAST_WATER_DATE,
                PageRequest.of(pageNumber, pageSize, Direction.DESC, "date"));
        List<History> content = currentHistory.getContent();

        if (!content.isEmpty() && petPlant.isDifferentLastWaterDate(updateRequest.getLastWaterDate())) {
            History history = content.get(0);
            LocalDate prevDate = history.getDate();
            if (updateRequest.getLastWaterDate().isBefore(prevDate) || updateRequest.getLastWaterDate()
                    .isEqual(prevDate)) {
                throw new IllegalArgumentException(
                        "마지막으로 물 준 날짜는 직전 값과 같거나 이전일 수 없습니다. date: " + updateRequest.getLastWaterDate());
            }
        }
    }
}
