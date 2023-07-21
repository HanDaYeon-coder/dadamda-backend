package com.forever.dadamda.service.scrap;

import com.forever.dadamda.dto.ErrorCode;
import com.forever.dadamda.dto.scrap.CreateScrapResponse;
import com.forever.dadamda.dto.scrap.GetScrapResponse;
import com.forever.dadamda.entity.scrap.Scrap;
import com.forever.dadamda.entity.user.User;
import com.forever.dadamda.exception.InvalidException;
import com.forever.dadamda.exception.NotFoundException;
import com.forever.dadamda.repository.ScrapRepository;
import com.forever.dadamda.service.WebClientService;
import com.forever.dadamda.service.user.UserService;
import org.springframework.data.domain.Pageable;
import java.time.LocalDateTime;
import java.util.Optional;
import lombok.RequiredArgsConstructor;
import net.minidev.json.JSONObject;
import net.minidev.json.parser.ParseException;
import org.springframework.data.domain.Slice;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
public class ScrapService {

    private final ScrapRepository scrapRepository;
    private final VideoService videoService;
    private final ArticleService articleService;
    private final ProductService productService;
    private final WebClientService webClientService;
    private final UserService userService;

    @Transactional
    public CreateScrapResponse createScraps(String email, String pageUrl) throws ParseException {
        User user = userService.validateUser(email);

        //1. 해당 링크 중복 확인 (삭제된 링크면 중복 X)
        boolean isPresentItem = scrapRepository
                .findByPageUrlAndUserAndDeletedDateIsNull(pageUrl, user).isPresent();
        if (isPresentItem) {
            throw new InvalidException(ErrorCode.INVALID_DUPLICATED_SCRAP);
        }

        saveScraps(user, pageUrl);

        return CreateScrapResponse.of(pageUrl);
    }

    @Transactional
    public Scrap saveScraps(User user, String pageUrl) throws ParseException {
        //2. 람다에게 api 요청
        JSONObject crawlingResponse = webClientService.crawlingItem(pageUrl);
        String type = crawlingResponse.get("type").toString();

        //3. DB 저장
        switch (type) {
            case "video":
                return videoService.saveVideo(crawlingResponse, user, pageUrl);
            case "article":
                return articleService.saveArticle(crawlingResponse, user, pageUrl);
            case "product":
                return productService.saveProduct(crawlingResponse, user, pageUrl);
            case "other":
                return saveOther(crawlingResponse, user, pageUrl);
        }

        return null;
    }

    @Transactional
    public Scrap saveOther(JSONObject crawlingResponse, User user, String pageUrl) {
        Scrap other = new Scrap(user, pageUrl, crawlingResponse.get("title").toString(),
                crawlingResponse.get("thumbnail_url").toString(),
                crawlingResponse.get("description").toString(), null);
        return scrapRepository.save(other);
    }

    @Transactional
    public void deleteScraps(String email, Long scrapId) {
        User user = userService.validateUser(email);

        Scrap item = scrapRepository.findByIdAndUserAndDeletedDateIsNull(scrapId, user).orElseThrow(
                () -> new NotFoundException(ErrorCode.NOT_EXISTS_SCRAP)
        );

        item.updateDeletedDate(LocalDateTime.now());
    }

    @Transactional
    public Slice<GetScrapResponse> getScraps(String email, Pageable pageable) {
        User user = userService.validateUser(email);

        Slice<Scrap> scrapSlice = scrapRepository.findAllByUserAndDeletedDateIsNull(
                user, pageable).orElseThrow(
                () -> new NotFoundException(ErrorCode.NOT_EXISTS_SCRAP)
        );

        Slice<GetScrapResponse> getScrapResponseSlice = scrapSlice.map(GetScrapResponse::of);
        return getScrapResponseSlice;
    }
}
