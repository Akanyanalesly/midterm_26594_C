package com.example.Gas_Station_Management_System.entity;


public enum LocationType {
    PROVINCE("Province"),
    DISTRICT("District"),
    SECTOR("Sector"),
    CELL("Cell"),
    VILLAGE("Village");

    private final String displayName;

    LocationType(String displayName) {
        this.displayName = displayName;
    }

    public String getDisplayName() {
        return displayName;
    }

    
    public LocationType getChildType() {
        return switch (this) {
            case PROVINCE -> DISTRICT;
            case DISTRICT -> SECTOR;
            case SECTOR -> CELL;
            case CELL -> VILLAGE;
            case VILLAGE -> null;
        };
    }

    
    public LocationType getParentType() {
        return switch (this) {
            case PROVINCE -> null;
            case DISTRICT -> PROVINCE;
            case SECTOR -> DISTRICT;
            case CELL -> SECTOR;
            case VILLAGE -> CELL;
        };
    }
}
