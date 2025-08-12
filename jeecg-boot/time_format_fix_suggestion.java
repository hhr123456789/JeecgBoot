// 在Excel导出的时间处理部分添加年查询的特殊处理
private String formatTimeForDisplay(String originalTime, String timeType) {
    if (originalTime == null || originalTime.isEmpty()) {
        return "";
    }
    
    // 先转换为北京时间
    String beijingTime = timeZoneUtil.convertUTCToBeijing(originalTime);
    
    // 如果是年查询，只显示年月
    if ("year".equals(timeType)) {
        try {
            LocalDateTime dateTime = LocalDateTime.parse(beijingTime, 
                DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss"));
            return dateTime.format(DateTimeFormatter.ofPattern("yyyy-MM"));
        } catch (Exception e) {
            log.warn("时间格式化失败：{}", beijingTime, e);
            return beijingTime;
        }
    }
    
    return beijingTime;
}

// 在Excel导出时使用
row.set最大功率发生时间(formatTimeForDisplay(stats.getMaxPowerTime(), query.getTimeType()));
row.set最小功率发生时间(formatTimeForDisplay(stats.getMinPowerTime(), query.getTimeType()));