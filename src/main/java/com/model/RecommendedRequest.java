package com.model;

/**
 * An open aid request shown to a volunteer, with whether it is recommended
 * for them and the reason why.
 *
 * @author Austin Lantz
 */
public class RecommendedRequest {

    private AidRequest request;
    private boolean recommended;
    private double drivingMinutes;
    private String reason;

    /**
     * Creates a recommendation.
     *
     * @param request the open request
     * @param recommended true if the request is recommended for this volunteer
     * @param drivingMinutes the driving time in minutes from the volunteer to the request
     * @param reason a sentence explaining why it is (or is not) recommended
     */
    public RecommendedRequest(AidRequest request, boolean recommended,
                              double drivingMinutes, String reason) {
        this.request = request;
        this.recommended = recommended;
        this.drivingMinutes = drivingMinutes;
        this.reason = reason;
    }

    /**
     * Gets the request.
     *
     * @return the request
     */
    public AidRequest getRequest() {
        return request;
    }

    /**
     * Checks whether the request is recommended for the volunteer.
     *
     * @return true if recommended
     */
    public boolean isRecommended() {
        return recommended;
    }

    /**
     * Gets the driving time in minutes from the volunteer to the request.
     *
     * @return the driving time in minutes (Double.MAX_VALUE if unknown)
     */
    public double getDrivingMinutes() {
        return drivingMinutes;
    }

    /**
     * Gets why the request is or is not recommended.
     *
     * @return the reason
     */
    public String getReason() {
        return reason;
    }
}