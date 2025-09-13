package com.metaverse.moem.matching.service;

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
    private static final double DEFAULT_WEIGHT_GEO      = 0.05;

    private static final double DISTANCE_MAX_KM = 30.0;

    private final UserRepository userRepository;

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
        double regionWeight = Optional.ofNullable(preferenceRecommendRequest.regionWeight()).orElse(DEFAULT_WEIGHT_GEO);

        List<RecommendationRequestDto> recommendationRequestDto = userRepository.findAll().stream()
                .map(user -> {
                    double selectedSkill = jaccard(selectedSkills, toLowerSet(user.getSkills()));
                    double selectedInterest = jaccard(selectedInterests, toLowerSet(user.getInterests()));
                    double selectedTime = scoreTimeAgainstWanted(user, preferenceRecommendRequest.availability());
                    double selectedMeeting = scoreMeetingAgainstPreferred(user, preferenceRecommendRequest.meetingPreference());
                    double selectedRegion = scoreRegionAgainstSelected(user, preferenceRecommendRequest.meetingPreference(), preferenceRecommendRequest.regionCode());

                    double score = skillWeight * selectedSkill + interestWeight *  selectedInterest + timeWeight * selectedTime + meetingWeight * selectedMeeting +  regionWeight * selectedRegion;

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

    private String getUserRegionCode(User user) {
        return null;
    }

    private double scoreRegionAgainstSelected(User user, MeetingPreference meetingPreference, String regionCode) {
        if (meetingPreference != MeetingPreference.OFFLINE) return 0.0;
        if (regionCode == null || regionCode.isBlank()) return 0.0;

        String candidateRegion = getUserRegionCode(user);
        if (candidateRegion != null && regionCode.equalsIgnoreCase(candidateRegion)) return 1.0;
        return 0.0;
    }

    private static double haversine(double latitudeHome, double longitudeHome, double latitudeSelected, double longitudeSelected) {
        double EarthRadius = 6371.0;
        double distanceLatitude =  Math.toRadians(latitudeSelected - latitudeHome);
        double distanceLongitude = Math.toRadians(longitudeSelected - longitudeHome);
        double haversine = Math.sin(distanceLatitude/2) * Math.sin(distanceLatitude/2) +
                        Math.cos(Math.toRadians(latitudeHome)) * Math.cos(Math.toRadians(latitudeSelected)) *
                        Math.sin(distanceLongitude/2) * Math.sin(distanceLongitude/2);

        double centralAngleRadian = 2 * Math.atan2(Math.sqrt(haversine), Math.sqrt(1.0 - haversine));
        return EarthRadius * centralAngleRadian;
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
