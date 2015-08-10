/**
 * @file ISDBrowseFilter.java
 * @brief An interface providing capabilities to filter resolved service data.
 */

package platform.dnssd.api.filter;

import java.util.List;

/**
 * An interface providing capabilities to filter resolved service data.
 * 
 * @author Bostjan Lasnik (bostjan.lasnik@hotmail.com)
 *
 */
public interface ISDResultFilter
{

    /**
     * Filter resolved service data. Filter provides capability to specify exact browse call specific requirements.
     * 
     * @param sdEntityBrowseEntryList
     *            - a {@link List} of {@link ServiceBrowseResult} object, representing a list of resolved service data.
     * @return - a {@link List} of {@link ServiceBrowseResult} filtered source service data.
     */
    List<ServiceBrowseResult> filter(List<ServiceBrowseResult> sdEntityBrowseEntryList);
}
