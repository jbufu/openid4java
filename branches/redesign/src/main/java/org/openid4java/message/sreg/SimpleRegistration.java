
package org.openid4java.message.sreg;

/**
 * Simple Registration OpenID Extension.
 * 
 * @see <a href="http://openid.net/specs/openid-simple-registration-extension-1_1-01.html">Simple Registration spec</a>
 */
public final class SimpleRegistration {

    /**
     * Namespace URI for Simple Registration version 1.0.
     */
    public static final String SREG_10_NS = "http://openid.net/extensions/sreg/1.0";

    /**
     * Namespace URI for Simple Registration version 1.1.
     */
    public static final String SREG_11_NS = "http://openid.net/extensions/sreg/1.1";

    /**
     * Simple Registration Parameters.
     */
    public static enum Parameter {
        /**
         * Parameter used to designate required SimpleRegistration fields.
         */
        required,

        /**
         * Parameter used to designate optional SimpleRegistration fields.
         */
        optional,

        /**
         * Parameter used for the relying party's policy URL.
         */
        policy_url;
    }
    
    
    /**
     * Valid Simple Registration fields.
     */
    public static enum Field {
        /** String user wants to use as a nickname. */
        nickname,

        /** Email address. */
        email,

        /** Free text representation of the user's full name. */
        fullname,

        /**
         * The user's date of birth as YYYY-MM-DD. Values should be zero-padded as necessary, so that this value is
         * always 10 characters long. If the user wishes not to reveal any particular component of the value, it must be
         * set to zero. For example, "1980-00-00" indicates only the year of birth.
         */
        dob,

        /** User's gender, "M" for male, "F" for female. */
        gender,

        /** Free text that should conform to the user's country's postal system. */
        postcode,

        /**
         * User's country of residence as specified by ISO3166. For example, "US" for United States.
         * 
         * @see <a href="http://www.iso.org/iso/country_codes">ISO 3166 - Country Codes</a>
         */
        country,

        /**
         * User's preferred language as specified by ISO639. For example, "en" for English.
         * 
         * @see <a href="http://www.w3.org/WAI/ER/IG/ert/iso639.htm">ISO 639 - Language Codes</a>
         */
        language,

        /**
         * User's timezone string. For example, "America/Los Angeles" for the Pacific timezone in the United States.
         * 
         * @see <a href="http://en.wikipedia.org/wiki/List_of_zoneinfo_timezones">Zoneinfo Timezone</a>
         */
        timezone;
    };

}