package com.example.demo.ServiceBoardgame;


import com.example.demo.DTO.BoardgameDTO;
import com.example.demo.DTO.BoardgameExtraDTO;
import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.RequiredArgsConstructor;
import org.springframework.core.io.ClassPathResource;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestTemplate;
import com.example.demo.DTO.BoardgameDetailDTO;
import org.w3c.dom.*;
import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;
import java.io.ByteArrayInputStream;
import java.nio.charset.StandardCharsets;
import java.util.ArrayList;

import java.util.Comparator;
import java.util.List;
import java.util.Map;
import java.util.HashMap;
import java.util.stream.Collectors;

import static org.codehaus.groovy.runtime.DefaultGroovyMethods.round;

@Service
@RequiredArgsConstructor
public class BoardgameServiceImpl implements BoardgameService {

    private List<BoardgameDTO> cachedAllGames; // 전체 게임 캐싱
    private final Map<String, BoardgameExtraDTO> randomExtraCache = new HashMap<>();

    // ---------------------------------------------------------
    // 🔥 1) 전체 게임 로드 (JSON + Hotness 이미지 + 랭킹 매칭)
    // ---------------------------------------------------------
    @Override
    public List<BoardgameDTO> getAllGames() {

        // 캐시가 있으면 그대로 반환
        if (cachedAllGames != null) {
            return cachedAllGames;
        }

        try {

            ObjectMapper mapper = new ObjectMapper();

            // 1) 로컬 JSON 로드
            ClassPathResource resource = new ClassPathResource("static/json/games_kr.json");
            List<BoardgameDTO> games =
                    mapper.readValue(resource.getInputStream(), new TypeReference<>() {});

            // 2) Hotness API 로드 (id → {images, rank})
            Map<String, Map<String, Object>> hotImages = loadHotnessImages();

            // 3) 각 게임에 이미지/랭킹 매칭
            for (BoardgameDTO game : games) {

                Map<String, Object> info = hotImages.get(game.getId()); // id로 매칭

                if (info != null) {

                    // 이미지
                    Map<String, Object> images = (Map<String, Object>) info.get("images");
                    if (images != null) {
                        game.setThumbnail(extractBestImage(images));
                    }

                    // 랭킹
                    if (info.get("rank") != null) {
                        game.setRank((Integer) info.get("rank"));
                    }
                }

                if (game.getThumbnail() == null || game.getThumbnail().isBlank()) {
                    game.setThumbnail("/image/noimage.png");
                }
            }

            cachedAllGames = games; // 캐싱 저장
            return games;

        } catch (Exception e) {
            throw new RuntimeException("게임 데이터 로딩 실패", e);
        }
    }

    // ---------------------------------------------------------
    // 🔥 2) 페이징 + 정렬
    // ---------------------------------------------------------
    @Override
    public List<BoardgameDTO> getPagedGames(int page, int size, String sort, String keyword) {

        List<BoardgameDTO> all = getAllGames(); // 캐시된 전체 리스트 사용


        // 🔍 검색 적용
        if (keyword != null && !keyword.isBlank()) {
            String lower = keyword.toLowerCase();

            all = all.stream()
                    .filter(g ->
                            (g.getKrName() != null && g.getKrName().contains(keyword)) ||
                                    (g.getName() != null && g.getName().toLowerCase().contains(lower))
                    )
                    .collect(Collectors.toList());   // 🔥 가변 리스트로 변환
        }

        // 🔽 정렬
        switch (sort) {
            case "name":
                all.sort(Comparator.comparing(BoardgameDTO::getName));
                break;
            case "new":
                all.sort(Comparator.comparing(BoardgameDTO::getYear).reversed());
                break;
            case "old":
                all.sort(Comparator.comparing(BoardgameDTO::getYear));
                break;
            default:
                all.sort(Comparator.comparing(BoardgameDTO::getRank));
        }

        // 페이징
        int start = (page - 1) * size;
        int end = Math.min(start + size, all.size());

        if (start >= all.size()) {
            return List.of();
        }

        return all.subList(start, end);
    }

    // ---------------------------------------------------------
    // 🔥 3) 전체 개수
    // ---------------------------------------------------------
    @Override
    public int getTotalCount(String keyword) {

        List<BoardgameDTO> all = getAllGames();

        if (keyword != null && !keyword.isBlank()) {
            String lower = keyword.toLowerCase();
            all = all.stream()
                    .filter(g ->
                            (g.getKrName() != null && g.getKrName().contains(keyword)) ||
                                    (g.getName() != null && g.getName().toLowerCase().contains(lower))
                    )
                    .toList();
        }

        return all.size();
    }

    // ---------------------------------------------------------
    // 🔥 Hotness API (이미지 + 랭킹) 가져오기
    // ---------------------------------------------------------
    private Map<String, Map<String, Object>> loadHotnessImages() {

        String url = "https://api.geekdo.com/api/hotness";
        RestTemplate rest = new RestTemplate();

        Map<String, Object> response = rest.getForObject(url, Map.class);
        List<Map<String, Object>> items = (List<Map<String, Object>>) response.get("items");

        Map<String, Map<String, Object>> result = new HashMap<>();

        for (Map<String, Object> item : items) {

            String id = item.get("id").toString();

            // -------------------------
            // 이미지
            // -------------------------
            Map<String, Object> images = (Map<String, Object>) item.get("images");

            // -------------------------
            // 랭킹 (Integer 또는 String 처리)
            // -------------------------
            Object rankObj = item.get("rank");
            Integer rank = null;

            if (rankObj instanceof Integer) {
                rank = (Integer) rankObj;
            } else if (rankObj instanceof String) {
                try {
                    rank = Integer.parseInt((String) rankObj);
                } catch (NumberFormatException ignored) {}
            }

            // -------------------------
            // info 구성
            // -------------------------
            Map<String, Object> info = new HashMap<>();
            info.put("images", images);
            info.put("rank", rank);

            result.put(id, info);
        }

        return result;
    }

    // ---------------------------------------------------------
    // 🔥 최적 이미지 선택
    // ---------------------------------------------------------
    private String extractBestImage(Map<String, Object> images) {

        try {
            Map<String, Object> square100 = (Map<String, Object>) images.get("square100");
            if (square100 != null) {
                if (square100.get("src@2x") != null) return square100.get("src@2x").toString();
                if (square100.get("src") != null) return square100.get("src").toString();
            }

            Map<String, Object> mediacard = (Map<String, Object>) images.get("mediacard");
            if (mediacard != null) {
                if (mediacard.get("src@2x") != null) return mediacard.get("src@2x").toString();
                if (mediacard.get("src") != null) return mediacard.get("src").toString();
            }

        } catch (Exception ignored) {}

        return null;
    }

    @Override
    public List<BoardgameDTO> getSimilarGames(String gameId) {

        List<BoardgameDTO> all = getAllGames();

        BoardgameDTO current = all.stream()
                .filter(g -> g.getId().equals(gameId))
                .findFirst()
                .orElse(null);

        if (current == null) return List.of();

        int year = current.getYear();

        return all.stream()
                .filter(g ->
                        !g.getId().equals(gameId) &&
                                (g.getYear() == year || g.getYear() == year - 1 || g.getYear() == year + 1)
                )
                .limit(4)
                .toList();
    }

    private double round(double value, int pos) {
        double scale = Math.pow(10, pos);
        return Math.round(value * scale) / scale;
    }

    private String randomDesigner() {
        String[] designers = {
                "Reiner Knizia",
                "Uwe Rosenberg",
                "Shems Phillips",
                "Wolfgang Warsch",
                "Mock Designer"
        };
        return designers[(int)(Math.random() * designers.length)];
    }

    private String randomPublisher() {
        String[] pubs = {
                "Kosmos",
                "Garphill Games",
                "Indie Boards",
                "Fantasy Flight",
                "Mock Publisher"
        };
        return pubs[(int)(Math.random() * pubs.length)];
    }

    @Override
    public BoardgameExtraDTO getOrCreateExtra(String id) {

        // ✅ 이미 랜덤 데이터가 있으면 그대로 반환
        if (randomExtraCache.containsKey(id)) {
            return randomExtraCache.get(id);
        }

        // ✅ 없으면 새로 생성
        BoardgameExtraDTO dto = new BoardgameExtraDTO();

        dto.setRating(round(7 + Math.random() * 2, 1));      // 7.0 ~ 9.0
        dto.setWeight(round(2 + Math.random() * 2, 1));      // 2.0 ~ 4.0

        dto.setMinPlayers(1 + (int) (Math.random() * 2));    // 1 ~ 2
        dto.setMaxPlayers(3 + (int) (Math.random() * 4));    // 3 ~ 6
        dto.setPlayingTime(30 + (int) (Math.random() * 90)); // 30 ~ 120
        dto.setMinAge(8 + (int) (Math.random() * 8));        // 8 ~ 16

        dto.setDesigners(List.of(randomDesigner()));
        dto.setPublishers(List.of(randomPublisher()));

        // ⭐⭐ 상세 검색용 데이터 추가 ⭐⭐
        dto.setCategory(randomCategory());
        dto.setTheme(randomTheme());
        dto.setLanguage(randomLanguage());

        // ✅ 캐시에 저장
        randomExtraCache.put(id, dto);

        return dto;
    }

    private String randomCategory() {
        String[] arr = {"전략", "가족", "파티", "추상", "어린이"};
        return arr[(int)(Math.random() * arr.length)];
    }

    private String randomTheme() {
        String[] arr = {"동물", "판타지", "SF", "경제", "전쟁", "모험"};
        return arr[(int)(Math.random() * arr.length)];
    }

    private String randomLanguage() {
        String[] arr = {"텍스트 없음", "적음", "보통", "많음"};
        return arr[(int)(Math.random() * arr.length)];
    }

    @Override
    public List<BoardgameDTO> searchGames(
            List<String> categories,
            List<String> themes,
            List<String> languages,
            List<Integer> minPlayers,
            List<Integer> maxPlayers,
            List<Integer> maxTime,
            List<Double> maxWeight
    ) {

        return getAllGames().stream()
                .filter(g -> {
                    BoardgameExtraDTO ex = getOrCreateExtra(g.getId());

                    // 카테고리
                    if (categories != null && !categories.isEmpty()
                            && !categories.contains(ex.getCategory())) return false;

                    // 테마
                    if (themes != null && !themes.isEmpty()
                            && !themes.contains(ex.getTheme())) return false;

                    // 언어
                    if (languages != null && !languages.isEmpty()
                            && !languages.contains(ex.getLanguage())) return false;

                    // 플레이 인원
                    if (minPlayers != null && !minPlayers.isEmpty()
                            && minPlayers.stream().noneMatch(p -> ex.getMaxPlayers() >= p)) return false;

                    if (maxPlayers != null && !maxPlayers.isEmpty()
                            && maxPlayers.stream().noneMatch(p -> ex.getMinPlayers() <= p)) return false;

                    // 플레이 시간
                    if (maxTime != null && !maxTime.isEmpty()
                            && maxTime.stream().noneMatch(t -> ex.getPlayingTime() <= t)) return false;

                    // 난이도(Weight)
                    if (maxWeight != null && !maxWeight.isEmpty()
                            && maxWeight.stream().noneMatch(w -> ex.getWeight() <= w)) return false;

                    return true;
                })
                .collect(Collectors.toList());
    }
}


