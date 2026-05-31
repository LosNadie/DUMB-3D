package com.dumb.integration.lastfm;

import com.dumb.config.LastFmProperties;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;
import org.springframework.util.StringUtils;
import org.springframework.web.util.UriComponentsBuilder;

import java.net.URI;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
import java.time.Duration;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;
import java.util.Optional;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

@Slf4j
@Component
public class LastFmClient {

    private static final Pattern YEAR_PATTERN = Pattern.compile("(19|20)\\d{2}");
    private static final String USER_AGENT = "Mozilla/5.0 (Windows NT 10.0; Win64; x64) Cursor-Dumb/1.0";

    private final LastFmProperties properties;
    private final ObjectMapper objectMapper;
    private final HttpClient httpClient;

    public LastFmClient(LastFmProperties properties, ObjectMapper objectMapper) {
        this.properties = properties;
        this.objectMapper = objectMapper;
        this.httpClient = HttpClient.newBuilder()
            .connectTimeout(Duration.ofSeconds(15))
            .build();
    }

    public Optional<LastFmAlbumContext> findAlbum(String artist, String albumTitle) {
        if (!properties.isEnabled() || !StringUtils.hasText(properties.getApiKey())) {
            log.debug("Last.fm 未启用或未配置 apiKey");
            return Optional.empty();
        }
        if (!StringUtils.hasText(artist) || !StringUtils.hasText(albumTitle)) {
            return Optional.empty();
        }
        String artistInput = artist.trim();
        String albumInput = albumTitle.trim();
        Optional<LastFmAlbumContext> direct = fetchAlbumInfo(artistInput, albumInput);
        if (direct.isPresent()) {
            return direct;
        }
        return searchAndFetchAlbumInfo(artistInput, albumInput);
    }

    private Optional<LastFmAlbumContext> fetchAlbumInfo(String artist, String albumTitle) {
        try {
            String url = UriComponentsBuilder.fromUriString(properties.getBaseUrl())
                .queryParam("method", "album.getInfo")
                .queryParam("api_key", properties.getApiKey())
                .queryParam("artist", artist)
                .queryParam("album", albumTitle)
                .queryParam("autocorrect", properties.isAutocorrect() ? 1 : 0)
                .queryParam("format", "json")
                .build()
                .encode()
                .toUriString();
            JsonNode root = getJson(url);
            if (root.has("error")) {
                log.info("Last.fm album.getInfo 未命中: artist={}, album={}, code={}, message={}",
                    artist, albumTitle, root.path("error").asText(""), root.path("message").asText(""));
                return Optional.empty();
            }
            return parseAlbumContext(root.path("album"));
        } catch (Exception e) {
            log.warn("Last.fm album.getInfo 查询失败: artist={}, album={}, reason={}", artist, albumTitle, e.getMessage());
            return Optional.empty();
        }
    }

    private Optional<LastFmAlbumContext> searchAndFetchAlbumInfo(String artist, String albumTitle) {
        try {
            String url = UriComponentsBuilder.fromUriString(properties.getBaseUrl())
                .queryParam("method", "album.search")
                .queryParam("api_key", properties.getApiKey())
                .queryParam("album", albumTitle)
                .queryParam("format", "json")
                .build()
                .encode()
                .toUriString();
            log.info("Last.fm 开始搜索候选: artist={}, album={}", artist, albumTitle);
            JsonNode root = getJson(url);
            if (root.has("error")) {
                log.info("Last.fm album.search 失败: album={}, code={}, message={}",
                    albumTitle, root.path("error").asText(""), root.path("message").asText(""));
                return Optional.empty();
            }
            JsonNode matchesNode = root.path("results").path("albummatches").path("album");
            if (matchesNode.isMissingNode() || matchesNode.isNull()) {
                return Optional.empty();
            }
            List<JsonNode> matches = new ArrayList<>();
            if (matchesNode.isArray()) {
                matchesNode.forEach(matches::add);
            } else if (matchesNode.isObject()) {
                matches.add(matchesNode);
            }
            if (matches.isEmpty()) {
                return Optional.empty();
            }
            log.info("Last.fm album.search 命中候选: artist={}, album={}, count={}", artist, albumTitle, matches.size());
            matches.sort(Comparator.comparingDouble((JsonNode node) -> scoreMatch(node, artist, albumTitle)).reversed());
            for (JsonNode candidate : matches) {
                String candidateArtist = textOrEmpty(candidate.path("artist")).trim();
                String candidateAlbum = textOrEmpty(candidate.path("name")).trim();
                String candidateMbid = textOrEmpty(candidate.path("mbid")).trim();
                if (!StringUtils.hasText(candidateArtist) || !StringUtils.hasText(candidateAlbum)) {
                    continue;
                }
                log.info("Last.fm 尝试候选专辑: inputArtist={}, inputAlbum={}, candidateArtist={}, candidateAlbum={}, mbidPresent={}",
                    artist, albumTitle, candidateArtist, candidateAlbum, StringUtils.hasText(candidateMbid));
                Optional<LastFmAlbumContext> resolved = StringUtils.hasText(candidateMbid)
                    ? fetchAlbumInfoByMbid(candidateMbid).or(() -> fetchAlbumInfo(candidateArtist, candidateAlbum))
                    : fetchAlbumInfo(candidateArtist, candidateAlbum);
                if (resolved.isPresent() && resolved.get().getTrackNames() != null && !resolved.get().getTrackNames().isEmpty()) {
                    return resolved;
                }
            }
            return Optional.empty();
        } catch (Exception e) {
            log.warn("Last.fm album.search 查询失败: artist={}, album={}, reason={}", artist, albumTitle, e.getMessage());
            return Optional.empty();
        }
    }

    private Optional<LastFmAlbumContext> fetchAlbumInfoByMbid(String mbid) {
        try {
            String url = UriComponentsBuilder.fromUriString(properties.getBaseUrl())
                .queryParam("method", "album.getInfo")
                .queryParam("api_key", properties.getApiKey())
                .queryParam("mbid", mbid)
                .queryParam("format", "json")
                .build()
                .encode()
                .toUriString();
            JsonNode root = getJson(url);
            if (root.has("error")) {
                return Optional.empty();
            }
            return parseAlbumContext(root.path("album"));
        } catch (Exception e) {
            log.warn("Last.fm album.getInfo(mbid) 查询失败: mbid={}, reason={}", mbid, e.getMessage());
            return Optional.empty();
        }
    }

    private JsonNode getJson(String url) throws Exception {
        HttpRequest request = HttpRequest.newBuilder(URI.create(url))
            .timeout(Duration.ofSeconds(30))
            .header("Accept", "application/json")
            .header("User-Agent", USER_AGENT)
            .GET()
            .build();
        HttpResponse<String> response = httpClient.send(request, HttpResponse.BodyHandlers.ofString());
        return objectMapper.readTree(response.body());
    }

    private Optional<LastFmAlbumContext> parseAlbumContext(JsonNode album) {
        if (album == null || album.isMissingNode() || album.isNull()) {
            return Optional.empty();
        }
        List<String> tracks = parseTracks(album.path("tracks").path("track"));
        String releaseDate = extractReleaseDate(album);
        return Optional.of(LastFmAlbumContext.builder()
            .albumName(textOrEmpty(album.path("name")))
            .artistName(textOrEmpty(album.path("artist")))
            .releaseDate(releaseDate)
            .releaseYear(extractYear(releaseDate))
            .url(textOrEmpty(album.path("url")))
            .mbid(textOrEmpty(album.path("mbid")))
            .coverImageUrl(extractBestImageUrl(album.path("image")))
            .trackNames(tracks)
            .build());
    }

    private static String extractBestImageUrl(JsonNode imageNode) {
        if (imageNode == null || imageNode.isMissingNode() || imageNode.isNull()) {
            return "";
        }
        if (!imageNode.isArray()) {
            return "";
        }
        String[] preferred = {"mega", "extralarge", "large", "medium", "small"};
        for (String size : preferred) {
            String url = findImageBySize(imageNode, size);
            if (StringUtils.hasText(url)) {
                return url;
            }
        }
        for (JsonNode n : imageNode) {
            String url = textOrEmpty(n.path("#text")).trim();
            if (StringUtils.hasText(url)) {
                return url;
            }
        }
        return "";
    }

    private static String findImageBySize(JsonNode imageNode, String size) {
        for (JsonNode n : imageNode) {
            String nodeSize = textOrEmpty(n.path("size")).trim();
            if (!size.equalsIgnoreCase(nodeSize)) {
                continue;
            }
            String url = textOrEmpty(n.path("#text")).trim();
            if (StringUtils.hasText(url)) {
                return url;
            }
        }
        return "";
    }

    private static double scoreMatch(JsonNode candidate, String artist, String albumTitle) {
        String candidateAlbum = normalize(textOrEmpty(candidate.path("name")));
        String candidateArtist = normalize(textOrEmpty(candidate.path("artist")));
        String expectedAlbum = normalize(albumTitle);
        String expectedArtist = normalize(artist);
        double score = 0;
        if (!expectedAlbum.isEmpty()) {
            if (candidateAlbum.equals(expectedAlbum)) {
                score += 8;
            } else if (candidateAlbum.contains(expectedAlbum) || expectedAlbum.contains(candidateAlbum)) {
                score += 4;
            }
        }
        if (!expectedArtist.isEmpty()) {
            if (candidateArtist.equals(expectedArtist)) {
                score += 6;
            } else if (candidateArtist.contains(expectedArtist) || expectedArtist.contains(candidateArtist)) {
                score += 3;
            }
        }
        return score;
    }

    private static List<String> parseTracks(JsonNode tracksNode) {
        List<String> tracks = new ArrayList<>();
        if (tracksNode == null || tracksNode.isMissingNode() || tracksNode.isNull()) {
            return tracks;
        }
        if (tracksNode.isArray()) {
            for (JsonNode track : tracksNode) {
                String name = textOrEmpty(track.path("name")).trim();
                if (!name.isEmpty()) {
                    tracks.add(name);
                }
            }
            return tracks;
        }
        if (tracksNode.isObject()) {
            String name = textOrEmpty(tracksNode.path("name")).trim();
            if (!name.isEmpty()) {
                tracks.add(name);
            }
        }
        return tracks;
    }

    private static String extractReleaseDate(JsonNode album) {
        String releaseDate = textOrEmpty(album.path("releasedate")).trim();
        if (StringUtils.hasText(releaseDate)) {
            return releaseDate;
        }
        return textOrEmpty(album.path("wiki").path("published")).trim();
    }

    private static String extractYear(String text) {
        if (!StringUtils.hasText(text)) {
            return "";
        }
        Matcher matcher = YEAR_PATTERN.matcher(text);
        return matcher.find() ? matcher.group() : "";
    }

    private static String normalize(String text) {
        if (!StringUtils.hasText(text)) {
            return "";
        }
        return text.toLowerCase().replaceAll("\\s+", " ").trim();
    }

    private static String textOrEmpty(JsonNode node) {
        if (node == null || node.isMissingNode() || node.isNull()) {
            return "";
        }
        return node.asText("");
    }
}
