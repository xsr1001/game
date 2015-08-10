/**
 * @file ISDSingleEntryBrowseResultFilter.java
 * @brief Represents a single result filter. Users invoking browse operation may provide a single entry result filter which
 * will automatically stop with the browse operation after at least one resolved services have been notifier to the
 * listener.
 */

package platform.dnssd.api.filter;

/**
 * Represents a single result filter. Users invoking browse operation may provide a single entry result filter which
 * will automatically stop with the browse operation after at least one resolved services have been notifier to the
 * listener.
 * 
 * @author Bostjan Lasnik (bostjan.lasnik@hotmail.com)
 *
 */
public interface ISDSingleResultFilter extends ISDResultFilter
{}
