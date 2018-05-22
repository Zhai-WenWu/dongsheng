package amodule.main.delegate;

import amodule.main.StatisticData;

public interface IStatistics {
    public final String type_click = "click";
    public final String type_show = "show";
    void setStatisticsData(String type, StatisticData statisticData);
}
