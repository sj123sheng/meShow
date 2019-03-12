package com.melot.kktv.util;

public class RankingEnum {

	public static final int RANKING_DAILY = 0; // 日榜类型/粉丝榜本场
	public static final int RANKING_WEEKLY = 1; // 周榜类型
	public static final int RANKING_MONTHLY = 2; // 月榜类型
	public static final int RANKING_TOTAL = 3; // 总榜类型

	public static final int RANKING_LAST_WEEK = 1; //上周
	public static final int RANKING_THIS_WEEK = 0; //本周
	
	public static final int RANKING_TYPE_ACTOR = 0; // 明星排行榜
	public static final int RANKING_TYPE_RICH = 1; // 富豪排行榜
	public static final int RANKING_TYPE_POPULAR = 2; // 人气红人排行榜
	public static final int RANKING_TYPE_FANS = 3; // 房间粉丝排行榜
	public static final int RANKING_TYPE_ACTOR_POPULAR = 4; // 阳光排行榜
	public static final int RANKING_TYPE_RICH_POPULAR = 5; // 阳光粉丝排行榜
	public static final int RANKING_TYPE_RICH_GOLDCOIN = 6; //金币富豪排行榜
    public static final int RANKING_TYPE_ACTOR_GOLDCOIN = 7; //金币明星排行榜
    public static final int RANKING_TYPE_HOUR = 12; //小时榜

	public static final int THIS_WEEK_GIFT_RANKING = 0; //本周礼物排行榜
	public static final int LAST_WEEK_GIFT_RANKING = -1; //上周礼物排行榜
	
	public static final int RANKING_LAST_HOUR = -1; //上一小时
    public static final int RANKING_THIS_HOUR = 0; //本小时
	
	public static String getCollection(Integer rankType, Integer slotType) {
		switch (rankType) {
		case RANKING_TYPE_ACTOR:
			return getActorCollection(slotType);
		case RANKING_TYPE_RICH:
			return getRichCollection(slotType);
		case RANKING_TYPE_ACTOR_POPULAR:
			return getActorPopularCollection(slotType);
		case RANKING_TYPE_RICH_POPULAR:
			return getRichPopularCollection(slotType);
		case RANKING_TYPE_FANS:
			return getFansCollection(slotType);
		case RANKING_TYPE_RICH_GOLDCOIN:
            return getGoldCoinRichCollection(slotType);
		case RANKING_TYPE_ACTOR_GOLDCOIN:
            return getGoldCoinActorCollection(slotType);
		case RANKING_TYPE_HOUR:
		    return getHourActorCollection(slotType);
		default:
			return null;
		}
	}

    private static String getActorCollection(Integer slotType) {
		switch (slotType) {
		case RANKING_DAILY:
			return "dailyActorRankingNew";
		case RANKING_WEEKLY:
			return "weeklyActorRankingNew";
		case RANKING_MONTHLY:
			return "monthlyActorRankingNew";
		case RANKING_TOTAL:
			return "totalActorRankingNew";
		default:
			return null;
		}
	}

	private static String getRichCollection(Integer slotType) {
		switch (slotType) {
		case RANKING_DAILY:
			return "dailyRichRankingNew";
		case RANKING_WEEKLY:
			return "weeklyRichRankingNew";
		case RANKING_MONTHLY:
			return "monthlyRichRankingNew";
		case RANKING_TOTAL:
			return "totalRichRankingNew";
		default:
			return null;
		}
	}

	private static String getActorPopularCollection(Integer slotType) {
		switch (slotType) {
		case RANKING_LAST_WEEK:
			return "lastWeekActorPopularRankingNew";
		case RANKING_THIS_WEEK:
			return "thisWeekActorPopularRankingNew";
		default:
			return null;
		}
	}
	
	private static String getRichPopularCollection(Integer slotType) {
		switch (slotType) {
		case RANKING_LAST_WEEK:
			return "lastWeekRichPopularRankingNew";
		case RANKING_THIS_WEEK:
			return "thisWeekRichPopularRankingNew";
		default:
			return null;
		}
	}
	
	private static String getFansCollection(Integer slotType) {
		switch (slotType) {
		case RANKING_WEEKLY:
			return CollectionEnum.WEEKLYFANSRANKING;
		case RANKING_MONTHLY:
			return CollectionEnum.MONTHLYFANSRANKING;
		case RANKING_TOTAL:
			return CollectionEnum.TOTALFANSRANKING;
		default:
			return null;
		}
	}
	
    private static String getGoldCoinRichCollection(Integer slotType) {
        switch (slotType) {
        case RANKING_LAST_WEEK:
            return "lastWeekGoldCoinRichRankingNew";
        case RANKING_THIS_WEEK:
            return "thisWeekGoldCoinRichRankingNew";
        default:
            return null;
        }
    }
    
    private static String getGoldCoinActorCollection(Integer slotType) {
        switch (slotType) {
        case RANKING_LAST_WEEK:
            return "lastWeekGoldCoinActorRankingNew";
        case RANKING_THIS_WEEK:
            return "thisWeekGoldCoinActorRankingNew";
        default:
            return null;
        }
    }
    
    private static String getHourActorCollection(Integer slotType) {
        switch (slotType) {
        case RANKING_LAST_HOUR:
            return "lastHourActorRanking";
        case RANKING_THIS_HOUR:
            return "thisHourActorRanking";
        default:
            return null;
        }
    }

}