package ru.practicum.main.location.constants;

public final class LocationConstants {

    private LocationConstants() {
    }

    public static final String LOCATION_NOT_FOUND = "Location with id %d not found";
    public static final String LOCATION_NAME_EXISTS = "Location with name %s already exists";
    public static final String CANNOT_DELETE_LOCATION_IN_USE = "Cannot delete location because it is used by events";
    public static final String INVALID_COORDINATES = "Invalid coordinates: lat must be between -90 and 90, lon between -180 and 180";

    public static final float MIN_LAT = -90.0f;
    public static final float MAX_LAT = 90.0f;
    public static final float MIN_LON = -180.0f;
    public static final float MAX_LON = 180.0f;

    public static final int DEFAULT_FROM = 0;
    public static final int DEFAULT_SIZE = 10;
    public static final int MAX_SIZE = 100;
}