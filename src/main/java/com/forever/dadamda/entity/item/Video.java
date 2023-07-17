package com.forever.dadamda.entity.item;

import com.forever.dadamda.entity.user.User;
import java.time.LocalDateTime;
import javax.persistence.Column;
import javax.persistence.Entity;
import lombok.Builder;
import lombok.NoArgsConstructor;

@Entity
@NoArgsConstructor
public class Video extends Item {

    @Column(length = 2083)
    private String embedUrl;

    @Column(length = 100)
    private String channelName;

    @Column(length = 2083)
    private String channelImageUrl;

    private Long watchedCnt;

    private Long playTime;

    private LocalDateTime publishedDate;

    @Column(length = 100)
    private String siteName;

    @Column(length = 100)
    private String genre;

    @Builder
    Video(User user, String pageUrl, String title, String thumbnailUrl, String description,
            String embedUrl,
            String channelName, String channelImageUrl, Long watchedCnt,
            Long playTime, LocalDateTime publishedDate, String siteName, String genre) {
        super(user, pageUrl, title, thumbnailUrl, description);
        this.embedUrl = embedUrl;
        this.channelName = channelName;
        this.channelImageUrl = channelImageUrl;
        this.watchedCnt = watchedCnt;
        this.playTime = playTime;
        this.publishedDate = publishedDate;
        this.siteName = siteName;
        this.genre = genre;
    }
}
