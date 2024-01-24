package kr.co.r2soft.infra;

public class Utils {

    public static String replacePath(String path, String settingsZoneUrl) {
        return settingsZoneUrl.replace("{path}", path);
    }

    public static String replacePathAndId(String eventUrl, String path, Long id) {
        return eventUrl.replace("{path}", path).replace("{id}", String.valueOf(id));
    }

    public static String replacePathAndIdAndEnrollmentId(String eventUrl, String path, Long id, Long enrollmentId) {
        return eventUrl.replace("{path}", path).replace("{id}", String.valueOf(id)).replace("{enrollmentId}", String.valueOf(enrollmentId));
    }
}
