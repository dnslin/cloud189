package in.dnsl.enums;

public enum OrderEnums {

    OrderByName(1, "filename"),

    OrderBySize(2, "filesize"),

    OrderByTime(3, "lastOpTime");


    private final int code;

    private final String name;

    OrderEnums(int i, String n) {
        this.code = i;
        this.name = n;
    }




    // 根据code获取枚举
    public static String getByCode(int code) {
        // 根据code获取枚举
        for (OrderEnums e : OrderEnums.values()) {
            if (e.getCode() == code) {
                return e.getName();
            }
        }
        return null;
    }

    private int getCode() {
        return code;
    }

    private String getName() {
        return name;
    }

}
