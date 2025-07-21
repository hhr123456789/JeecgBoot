package org.jeecg.modules.energy.util;

import org.influxdb.InfluxDB;
import org.influxdb.InfluxDBFactory;
import org.influxdb.dto.BatchPoints;
import org.influxdb.dto.Point;
import org.influxdb.dto.Query;
import org.influxdb.dto.QueryResult;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

import javax.annotation.PostConstruct;
import javax.annotation.PreDestroy;
import java.util.List;
import java.util.Map;
import java.util.concurrent.TimeUnit;

/**
 * InfluxDB 工具类
 */
@Component
public class InfluxDBUtil {

    private static final Logger logger = LoggerFactory.getLogger(InfluxDBUtil.class);

    @Value("${influxdb.url:http://localhost:8086}")
    private String influxDbUrl;

    @Value("${influxdb.username:}")
    private String username;

    @Value("${influxdb.password:}")
    private String password;

    @Value("${influxdb.database:defaultdb}")
    private String database;

    @Value("${influxdb.retention-policy:autogen}")
    private String retentionPolicy;

    private InfluxDB influxDB;

    @PostConstruct
    public void init() {
        try {
            if (username != null && !username.isEmpty()) {
                influxDB = InfluxDBFactory.connect(influxDbUrl, username, password);
            } else {
                influxDB = InfluxDBFactory.connect(influxDbUrl);
            }

            // 设置数据库
            influxDB.setDatabase(database);
            influxDB.setRetentionPolicy(retentionPolicy);

            // 启用批处理
            influxDB.enableBatch(100, 1000, TimeUnit.MILLISECONDS);

            logger.info("InfluxDB 连接成功: {}", influxDbUrl);
        } catch (Exception e) {
            logger.error("InfluxDB 连接失败: ", e);
            throw new RuntimeException("InfluxDB 初始化失败", e);
        }
    }





    @PreDestroy
    public void close() {
        if (influxDB != null) {
            influxDB.close();
            logger.info("InfluxDB 连接已关闭");
        }
    }

    /**
     * 创建数据库
     */
    public void createDatabase(String database) {
        try {
            influxDB.query(new Query("CREATE DATABASE " + database));
            logger.info("数据库创建成功: {}", database);
        } catch (Exception e) {
            logger.error("创建数据库失败: {}", database, e);
            throw new RuntimeException("创建数据库失败", e);
        }
    }

    /**
     * 删除数据库
     */
    public void deleteDatabase(String database) {
        try {
            influxDB.query(new Query("DROP DATABASE " + database));
            logger.info("数据库删除成功: {}", database);
        } catch (Exception e) {
            logger.error("删除数据库失败: {}", database, e);
            throw new RuntimeException("删除数据库失败", e);
        }
    }

    /**
     * 插入单条数据
     */
    public void insert(String measurement, Map<String, String> tags, Map<String, Object> fields) {
        insert(measurement, tags, fields, System.currentTimeMillis(), TimeUnit.MILLISECONDS);
    }

    /**
     * 插入单条数据（指定时间）
     */
    public void insert(String measurement, Map<String, String> tags, Map<String, Object> fields,
                       long time, TimeUnit timeUnit) {
        try {
            Point.Builder builder = Point.measurement(measurement)
                    .time(time, timeUnit);

            if (tags != null) {
                builder.tag(tags);
            }

            if (fields != null) {
                builder.fields(fields);
            }

            influxDB.write(builder.build());
        } catch (Exception e) {
            logger.error("插入数据失败: measurement={}", measurement, e);
            throw new RuntimeException("插入数据失败", e);
        }
    }

    /**
     * 批量插入数据
     */
    public void batchInsert(List<Point> points) {
        try {
            BatchPoints batchPoints = BatchPoints.database(database)
                    .retentionPolicy(retentionPolicy)
                    .points(points)
                    .build();

            influxDB.write(batchPoints);
        } catch (Exception e) {
            logger.error("批量插入数据失败", e);
            throw new RuntimeException("批量插入数据失败", e);
        }
    }

    /**
     * 查询数据
     */
    public QueryResult query(String command) {
        return query(command, database);
    }

    /**
     * 查询指定数据库的数据
     */
    public QueryResult query(String command, String database) {
        try {
            return influxDB.query(new Query(command, database));
        } catch (Exception e) {
            logger.error("查询数据失败: command={}", command, e);
            throw new RuntimeException("查询数据失败", e);
        }
    }

    /**
     * 检查数据库是否存在
     */
    public boolean databaseExists(String database) {
        try {
            QueryResult result = influxDB.query(new Query("SHOW DATABASES"));
            List<QueryResult.Series> series = result.getResults().get(0).getSeries();

            if (series != null && !series.isEmpty()) {
                List<List<Object>> values = series.get(0).getValues();
                if (values != null) {
                    return values.stream()
                            .anyMatch(value -> database.equals(value.get(0)));
                }
            }
            return false;
        } catch (Exception e) {
            logger.error("检查数据库是否存在失败: {}", database, e);
            return false;
        }
    }

    /**
     * 获取 InfluxDB 实例（用于高级操作）
     */
    public InfluxDB getInfluxDB() {
        return influxDB;
    }

    /**
     * 设置数据库
     */
    public void setDatabase(String database) {
        this.database = database;
        if (influxDB != null) {
            influxDB.setDatabase(database);
        }
    }

    /**
     * 获取当前数据库名
     */
    public String getDatabase() {
        return database;
    }

    /**
     * 解析QueryResult为List<Map<String, Object>>
     */
    public static List<Map<String, Object>> parseQueryResult(QueryResult queryResult) {
        logger.info("🔍 开始解析InfluxDB查询结果");
        List<Map<String, Object>> resultList = new java.util.ArrayList<>();
        if (queryResult == null || queryResult.getResults() == null) {
            logger.warn("❌ queryResult 或 results 为 null");
            return resultList;
        }

        logger.info("🔍 查询结果数量: {}", queryResult.getResults().size());

        try {
            for (QueryResult.Result result : queryResult.getResults()) {
                if (result == null || result.getSeries() == null) {
                    logger.warn("⚠️ result 或 series 为 null，跳过");
                    continue;
                }

                logger.info("🔍 series 数量: {}", result.getSeries().size());

                for (QueryResult.Series series : result.getSeries()) {
                    List<String> columns = series.getColumns();
                    List<List<Object>> values = series.getValues();
                    Map<String, String> tags = series.getTags(); // 获取tag信息

                    logger.info("🔍 columns: {}", columns);
                    logger.info("🔍 tags: {}", tags);
                    logger.info("🔍 values 数量: {}", (values != null ? values.size() : 0));

                    if (values == null) continue;
                    for (List<Object> valueRow : values) {
                        Map<String, Object> map = new java.util.HashMap<>();

                        // 添加列数据
                        for (int i = 0; i < columns.size(); i++) {
                            if (i < valueRow.size()) {
                                map.put(columns.get(i), valueRow.get(i));
                            }
                        }

                        // 添加tag数据 (包括tagname)
                        if (tags != null) {
                            map.putAll(tags);
                        }

                        resultList.add(map);
                    }
                }
            }
        } catch (Exception e) {
            logger.error("❌ 解析过程中出现异常: {}", e.getMessage(), e);
            throw e;
        }

        logger.info("✅ 解析完成，返回数据条数: {}", resultList.size());
        return resultList;
    }

    /**
     * 从tagname中提取模块ID
     * tagname格式: YJ0001_14#HZ 或 yj0001_14#HZ -> 统一转换为 yj0001_14
     * 支持大小写不敏感的匹配
     */
    public static String extractModuleIdFromTagname(String tagname) {
        if (tagname == null || !tagname.contains("#")) {
            return null;
        }
        String moduleIdPart = tagname.split("#")[0];
        // 统一转换为小写，确保与MySQL中的module_id格式一致
        return moduleIdPart.toLowerCase();
    }

    /**
     * 从tagname中提取点位名称
     * tagname格式: YJ0001_14#HZ -> HZ
     * 点位名称保持原始大小写
     */
    public static String extractPointNameFromTagname(String tagname) {
        if (tagname == null || !tagname.contains("#")) {
            return null;
        }
        String[] parts = tagname.split("#");
        return parts.length > 1 ? parts[1] : null;
    }
    /**
     * 构建InfluxDB查询用的tagname
     * 将MySQL的moduleId转换为InfluxDB的tagname格式
     * @param moduleId MySQL中的模块ID (如: yj0001_14)
     * @param pointName 点位名称 (如: HZ)
     * @return InfluxDB的tagname (如: YJ0001_14#HZ)
     */
    public static String buildInfluxTagname(String moduleId, String pointName) {
        if (moduleId == null || pointName == null) {
            return null;
        }
        // 将moduleId转换为大写，构建InfluxDB的tagname格式
        return moduleId.toUpperCase() + "#" + pointName.toUpperCase();
    }

    /**
     * 构建InfluxDB查询的模块ID模式
     * @param moduleId MySQL中的模块ID (如: yj0001_14)
     * @return InfluxDB查询模式 (如: YJ0001_14#.*)
     */
    public static String buildInfluxModulePattern(String moduleId) {
        if (moduleId == null) {
            return null;
        }
        return moduleId.toUpperCase() + "#.*";
    }

}
