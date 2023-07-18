package com.forever.dadamda.service.scrap;

import com.forever.dadamda.dto.ErrorCode;
import com.forever.dadamda.dto.scrap.CreateScrapResponse;
import com.forever.dadamda.entity.scrap.Scrap;
import com.forever.dadamda.entity.user.User;
import com.forever.dadamda.exception.InvalidException;
import com.forever.dadamda.exception.NotFoundException;
import com.forever.dadamda.repository.ScrapRepository;
import com.forever.dadamda.service.WebClientService;
import com.forever.dadamda.service.user.UserService;
import java.time.LocalDateTime;
import lombok.RequiredArgsConstructor;
import net.minidev.json.JSONObject;
import net.minidev.json.parser.ParseException;
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

        //2. 람다에게 api 요청
        JSONObject crawlingResponse = webClientService.crawlingItem(pageUrl);
        String type = crawlingResponse.get("type").toString();

        //3. DB 저장
        switch (type) {
            case "video":
                videoService.saveVideo(crawlingResponse, user, pageUrl);
                break;
            case "article":
                articleService.saveArticle(crawlingResponse, user, pageUrl);
                break;
            case "product":
                productService.saveProduct(crawlingResponse, user, pageUrl);
                break;
            case "other":
                saveOther(crawlingResponse, user, pageUrl);
                break;
        }

        return CreateScrapResponse.of(pageUrl);
    }

    @Transactional
    public void saveOther(JSONObject crawlingResponse, User user, String pageUrl) {
        Scrap other = new Scrap(user, pageUrl, crawlingResponse.get("title").toString(),
                crawlingResponse.get("thumbnail_url").toString(),
                crawlingResponse.get("description").toString(), null);

        scrapRepository.save(other);
    }

    @Transactional
    public void deleteScraps(String email, Long scrapId) {
        User user = userService.validateUser(email);

        Scrap item = scrapRepository.findByIdAndUserAndDeletedDateIsNull(scrapId, user).orElseThrow(
                () -> new NotFoundException(ErrorCode.NOT_EXISTS_SCRAP)
        );

        item.updateDeletedDate(LocalDateTime.now());
    }
}
