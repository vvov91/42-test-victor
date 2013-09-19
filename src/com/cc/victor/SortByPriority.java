package com.cc.victor;

import java.util.Comparator;

/**
 * Comparator to sort friends by priority
 * 
 * @author Victor Vovchenko <vitek91@gmail.com>
 *
 */
public class SortByPriority implements Comparator<Friend> {

	@Override
	public int compare(Friend friend1, Friend friend2) {
		if (friend1.getPriority() > friend2.getPriority()) {
			return -1;
		} else if (friend1.getPriority() < friend2.getPriority()) {
			return 1;
		} else
			return 0;
	}

}
