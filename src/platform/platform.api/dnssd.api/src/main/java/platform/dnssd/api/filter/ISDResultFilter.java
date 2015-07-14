/**
 * @file ISDBrowseFilter.java
 * @brief <description>
 */

package platform.dnssd.api.filter;

import java.util.List;

public interface ISDResultFilter
{
    List<SDEntityBrowseEntry> filter(List<SDEntityBrowseEntry> sdEntityBrowseEntryList);
}
