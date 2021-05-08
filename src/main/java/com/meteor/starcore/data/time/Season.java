package com.meteor.starcore.data.time;

public enum Season {
    SPRING("春"),
    SUMMER("夏"),
    AUTUMN("秋"),
    WINTER("冬");

    private String name;
    Season(String name){
        this.name = name;
    }

    public String getName() {
        return name;
    }

    public static Season getNextSeason(Season season){
        switch (season){
            case SPRING:
                return SUMMER;
            case SUMMER:
                return AUTUMN;
            case AUTUMN:
                return WINTER;
            case WINTER:
                return SPRING;
        }
        return null;
    }
}
