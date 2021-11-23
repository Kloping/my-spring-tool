package test;

import io.github.kloping.MySpringTool.StarterApplication;
import io.github.kloping.MySpringTool.annotations.AutoStand;
import io.github.kloping.MySpringTool.annotations.CommentScan;
import test.interfaces.GiftShow;

@CommentScan(path = "test")
public class Main {
    @AutoStand
    static GiftShow giftShow;

    public static void main(String[] args) throws Exception {
        StarterApplication.run(Main.class);
//        System.out.println(giftShow.doc("王者荣耀"));
        System.out.println(giftShow.doc2("" +
                "" +
                "{\"operationName\":\"visionSearchPhoto\",\"variables\":{\"keyword\":\"王者荣耀\",\"pcursor\":\"\",\"page\":\"search\"},\"query\":\"query visionSearchPhoto($keyword: String, $pcursor: String, $searchSessionId: String, $page: String, $webPageArea: String) {\\n  visionSearchPhoto(keyword: $keyword, pcursor: $pcursor, searchSessionId: $searchSessionId, page: $page, webPageArea: $webPageArea) {\\n    result\\n    llsid\\n    webPageArea\\n    feeds {\\n      type\\n      author {\\n        id\\n        name\\n        following\\n        headerUrl\\n        headerUrls {\\n          cdn\\n          url\\n          __typename\\n        }\\n        __typename\\n      }\\n      tags {\\n        type\\n        name\\n        __typename\\n      }\\n      photo {\\n        id\\n        duration\\n        caption\\n        likeCount\\n        realLikeCount\\n        coverUrl\\n        photoUrl\\n        liked\\n        timestamp\\n        expTag\\n        coverUrls {\\n          cdn\\n          url\\n          __typename\\n        }\\n        photoUrls {\\n          cdn\\n          url\\n          __typename\\n        }\\n        animatedCoverUrl\\n        stereoType\\n        videoRatio\\n        __typename\\n      }\\n      canAddComment\\n      currentPcursor\\n      llsid\\n      status\\n      __typename\\n    }\\n    searchSessionId\\n    pcursor\\n    aladdinBanner {\\n      imgUrl\\n      link\\n      __typename\\n    }\\n    __typename\\n  }\\n}\\n\"}" +
                ""));
    }
}
