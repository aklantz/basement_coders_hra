package com.model;

import java.util.Objects;

/**
 * A zip code affected by a hurricane.
 *
 * @author Austin Lantz
 */
public class ZipCode {

    private String code;

    /**
     * Creates a zip code.
     *
     * @param code the zip code, such as 29708 (extra spaces are removed)
     */
    public ZipCode(String code) {
        this.code = (code == null) ? null : code.trim();
    }

    /**
     * Gets the zip code.
     *
     * @return the zip code text
     */
    public String getCode() {
        return code;
    }

    /**
     * Checks whether a zip code written as text is this zip code. Shelters and users
     * store their zip codes as plain text, so this lets them be compared to a ZipCode.
     *
     * @param otherCode the zip code text to compare, such as a shelter's zip code
     * @return true if the text is the same zip code (extra spaces are ignored)
     */
    public boolean matches(String otherCode) {
        return code != null && otherCode != null && code.equals(otherCode.trim());
    }

    /**
     * Two zip codes are equal if they have the same code.
     *
     * @param other the object to compare with
     * @return true if the other object is a ZipCode with the same code
     */
    @Override
    public boolean equals(Object other) {
        if (this == other) {
            return true;
        }
        if (!(other instanceof ZipCode)) {
            return false;
        }
        return Objects.equals(code, ((ZipCode) other).code);
    }

    /**
     * Gets a hash code that agrees with equals.
     *
     * @return the hash code
     */
    @Override
    public int hashCode() {
        return Objects.hash(code);
    }

    /**
     * Gets the zip code as text, so printing a ZipCode shows the code.
     *
     * @return the zip code text
     */
    @Override
    public String toString() {
        return code;
    }
}