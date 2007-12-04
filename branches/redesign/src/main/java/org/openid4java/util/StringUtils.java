
package org.openid4java.util;

import java.util.Collection;
import java.util.Iterator;

/**
 * String Utilities.
 */
public final class StringUtils {

    /** Private constructor. */
    private StringUtils() {

    }

    /**
     * Join a Collection using a delimiter.
     * 
     * @param collection objects to join
     * @param delimiter String used to join objects
     * @return joined string
     */
    public static String join(Collection<?> collection, String delimiter) {
        StringBuffer buffer = new StringBuffer();
        Iterator<?> iterator = collection.iterator();

        while (iterator.hasNext()) {
            buffer.append(iterator.next().toString());
            if (iterator.hasNext()) {
                buffer.append(delimiter);
            }
        }

        return buffer.toString();
    }
}