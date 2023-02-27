package uk.ac.man.cs.eventlite.dao;

import twitter4j.Status;
import twitter4j.TwitterException;

public interface TwitterService {

	public Iterable<Status> getPreviousFiveTweets();

	public void updateStatus(String message) throws TwitterException;
}
