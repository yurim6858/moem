package com.metaverse.moem.matching.service;

// ✨ 필요한 클래스들을 import 합니다.
import com.fasterxml.jackson.databind.ObjectMapper;
import com.metaverse.moem.matching.domain.MeetingPreference;
import com.metaverse.moem.matching.domain.PreferenceRecommendRequest;
import com.metaverse.moem.matching.dto.RecommendationRequestDto;
import com.metaverse.moem.matching.dto.RecommendationResponseDto;
import com.metaverse.moem.matching.domain.User;
import com.metaverse.moem.matching.dto.TimeSlotDto;
import com.metaverse.moem.matching.dto.UserDto;
import com.metaverse.moem.matching.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.context.annotation.Profile;
import org.springframework.stereotype.Service;

import java.io.IOException;
import java.util.*;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
@Profile({"default","mock"})
public class AIMatchService {

    private static final double DEFAULT_WEIGHT_SKILL    = 0.40;
    private static final double DEFAULT_WEIGHT_INTEREST = 0.25;
    private static final double DEFAULT_WEIGHT_TIME     = 0.20;
    private static final double DEFAULT_WEIGHT_MEETING  = 0.10;

    private final UserRepository userRepository;
    private final GeminiService geminiService; // ✨ GeminiService를 주입받습니다.

    public List<User> getAllUsers() {
        return userRepository.findAll();
    }

    public RecommendationResponseDto recommendByNaturalLanguage(String query) throws IOException {
        String prompt = String.format("""
            사용자 문장을 분석해서 PreferenceRecommendRequest JSON 객체로 변환해줘.
            - skills: 기술 스택 배열
            - interests: 관심사 배열
            - JSON 외에 다른 설명은 절대 추가하지마.
            - 분석할 수 없는 필드는 null로 설정해.
            
            사용자 문장: "%s"
            
            JSON 형식:
            {
              "skills": ["..."],
              "interests": ["..."],
              "skillWeight": null,
              "interestWeight": null,
              "timeWeight": null,
              "meetingWeight": null,
              "availability": null,
              "meetingPreference": null,
              "limit": 5
            }
        """, query);

        String jsonResponse = geminiService.getCompletion(prompt);

        ObjectMapper objectMapper = new ObjectMapper();
        PreferenceRecommendRequest request = objectMapper.readValue(jsonResponse, PreferenceRecommendRequest.class);

        return recommendByPreference(request);
    }


    public RecommendationResponseDto recommend(Long baseUserId, int limit) {
        User base = userRepository.findById(baseUserId)
                .orElseThrow(() -> new NoSuchElementException("User not found: " + baseUserId));

        Set<String> baseTokens = tokens(base);

        List<RecommendationRequestDto> recommendation = userRepository.findAll().stream()
                .filter(user -> !Objects.equals(user.getId(), base.getId()))
                .map(user -> {
                    double score = jaccard(baseTokens, tokens(user));
                    return RecommendationRequestDto.builder()
                            .userId(user.getId())
                            .name(user.getName())
                            .skills(user.getSkills())
                            .interests(user.getInterests())
                            .score(round(score))
                            .build();
                })
                .sorted(Comparator.comparing(RecommendationRequestDto::getScore).reversed())
                .limit(limit)
                .collect(Collectors.toList());

        UserDto baseDto = UserDto.builder()
                .userId(base.getId())
                .name(base.getName())
                .skills(base.getSkills())
                .interests(base.getInterests())
                .build();

        return RecommendationResponseDto.builder()
                .baseUser(baseDto)
                .recommendations(recommendation)
                .build();
    }

    public RecommendationResponseDto recommendByPreference(PreferenceRecommendRequest preferenceRecommendRequest) {
        int limit = preferenceRecommendRequest.limit() != null ? Math.max(1, Math.min(preferenceRecommendRequest.limit(),20)) : 5;

        Set<String> selectedSkills = toLowerSet(preferenceRecommendRequest.skills());
        Set<String> selectedInterests = toLowerSet(preferenceRecommendRequest.interests());

        double skillWeight = Optional.ofNullable(preferenceRecommendRequest.skillWeight()).orElse(DEFAULT_WEIGHT_SKILL);
        double interestWeight = Optional.ofNullable(preferenceRecommendRequest.interestWeight()).orElse(DEFAULT_WEIGHT_INTEREST);
        double timeWeight = Optional.ofNullable(preferenceRecommendRequest.timeWeight()).orElse(DEFAULT_WEIGHT_TIME);
        double meetingWeight = Optional.ofNullable(preferenceRecommendRequest.meetingWeight()).orElse(DEFAULT_WEIGHT_MEETING);

        List<RecommendationRequestDto> recommendationRequestDto = userRepository.findAll().stream()
                .map(user -> {
                    double selectedSkill = jaccard(selectedSkills, toLowerSet(user.getSkills()));
                    double selectedInterest = jaccard(selectedInterests, toLowerSet(user.getInterests()));
                    double selectedTime = scoreTimeAgainstWanted(user, preferenceRecommendRequest.availability());
                    double selectedMeeting = scoreMeetingAgainstPreferred(user, preferenceRecommendRequest.meetingPreference());

                    double score = skillWeight * selectedSkill + interestWeight * selectedInterest + timeWeight * selectedTime + meetingWeight * selectedMeeting;

                    return RecommendationRequestDto.builder()
                            .userId(user.getId())
                            .name(user.getName())
                            .skills(user.getSkills())
                            .interests(user.getInterests())
                            .score(round(score))
                            .build();
                })
                .sorted(Comparator.comparing(RecommendationRequestDto::getScore).reversed())
                .limit(limit)
                .toList();

        return RecommendationResponseDto.builder()
                .baseUser(null)
                .recommendations(recommendationRequestDto)
                .build();
    }

    private static Set<String> toLowerSet(Collection<String> collection) {
        if (collection == null) return Set.of();
        return collection.stream().filter(Objects::nonNull).map(String::toLowerCase).collect(Collectors.toSet());
    }

    private double scoreTimeAgainstWanted(User user, List<TimeSlotDto> wantedTime){
        if (wantedTime == null || wantedTime.isEmpty()) return 0.0;

        int intersection = 0, wanted = 0;
        for (TimeSlotDto want : wantedTime) {
            wanted += Math.max(0, want.endMin() - want.startMin());
            intersection += overlapMinutes(userSlotsByDay(user, want.dayOfWeek()), want.startMin(), want.endMin());
        }

        return wanted <= 0 ? 0.0 : clamp01((double) intersection / (double) wanted);
    }

    private List<Slot> userSlotsByDay(User user, int dayOfWeek) {
        return List.of();
    }

    private int overlapMinutes(List<Slot> slots, int start, int end) {
        int sum = 0;
        for (Slot slot : slots){
            int startTime = Math.max(start, slot.start());
            int endTime = Math.min(end, slot.end());
            if (endTime > startTime) sum += (endTime - startTime);
        }
        return sum;
    }

    private MeetingPreference getUserMeetingPreference(User user) {
        return null;
    }

    private double scoreMeetingAgainstPreferred(User user, MeetingPreference meetingPreference) {
        MeetingPreference candidate = getUserMeetingPreference(user);
        if (meetingPreference == null || candidate == null) return 0.0;
        if (meetingPreference == candidate) return 1.0;
        if (meetingPreference == MeetingPreference.HYBRID || candidate == MeetingPreference.HYBRID) return 0.5;
        return 0.0;
    }

    private static double clamp01(double value) {
        if (Double.isNaN(value) || Double.isInfinite(value)) return 0.0;
        return value < 0.0 ? 0.0 : (value > 1.0 ? 1.0 : value);
    }

    private record Slot(int start, int end) {}

    private Set<String> tokens(User user) {
        Set<String> tokens = new HashSet<>();
        if (user.getSkills() != null) tokens
                .addAll(user.getSkills().stream().map(String::toLowerCase).toList());
        if (user.getInterests() != null) tokens
                .addAll(user.getInterests().stream().map(String::toLowerCase).toList());
        return tokens;
    }

    private Double jaccard(Set<String> baseTokens, Set<String> tokens) {
        if (baseTokens.isEmpty() && tokens.isEmpty()) return 0.0;
        Set<String> intersection = new HashSet<>(baseTokens);
        intersection.retainAll(tokens);
        Set<String> union = new HashSet<>(baseTokens);
        union.addAll(tokens);
        return (double) intersection.size() / (double) union.size();
    }

    private double round(double value) {
        return Math.round(value * 1000.0) / 1000.0;
    }
}