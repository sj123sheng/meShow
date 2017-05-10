package com.melot.kktv.util;

public class FamilyRankingEnum
{
  public static final int RANKING_DAILY = 0;
  public static final int RANKING_WEEKLY = 1;
  public static final int RANKING_MONTHLY = 2;
  public static final int RANKING_TOTAL = 3;
  public static final int RANKING_TYPE_FAMILY_RICH = 4;
  public static final int RANKING_TYPE_FAMILY_POPULAR = 5;
  public static final int RANKING_TYPE_FAMILY_TOTALLIVE = 6;
  public static final int RANKING_TYPE_FAMILY_CROWN = 7;
  public static final int RANKING_TYPE_FAMILY_DIAMOND = 8;
  public static final int RANKING_TYPE_FAMILY_HEART = 9;
  public static final int RANKING_TYPE_FAMILY_MEDAL = 10;
  public static final int RANKING_TYPE_FAMILY_WEEKLYCONSUME = 11;

  public static String getCollection(Integer rankType, Integer slotType)
  {
    switch (rankType.intValue())
    {
    case 4:
      return getFamilyRichCollection(slotType);
    case 5:
      return getFamilyPopularCollection(slotType);
    case 6:
      return getFamilyTotalLiveCollection(slotType);
    }
    return null;
  }

  public static String getFamilyRichCollection(Integer slotType) {
    switch (slotType.intValue())
    {
    case 0:
      return "dailyFamilyRichRanking";
    case 1:
      return "weeklyFamilyRichRanking";
    case 2:
      return "monthlyRichRanking";
    }
    return null;
  }

  public static String getFamilyPopularCollection(Integer slotType) {
    switch (slotType.intValue())
    {
    case 0:
      return "dailyFamilyPopularRanking";
    case 1:
      return "weeklyFamilyPopularRanking";
    case 2:
      return "monthlyFamilyPopularRanking";
    }
    return null;
  }

  public static String getFamilyTotalLiveCollection(Integer slotType) {
    switch (slotType.intValue())
    {
    case 0:
      return "dailyFamilyTotalLiveRanking";
    case 1:
      return "weeklyFamilyTotalLiveRanking";
    case 2:
      return "monthlyFamilyTotalLiveRanking";
    }
    return null;
  }
}