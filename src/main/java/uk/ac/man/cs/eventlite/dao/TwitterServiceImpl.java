package uk.ac.man.cs.eventlite.dao;

import java.util.ArrayList;

import org.springframework.stereotype.Service;

import twitter4j.ResponseList;
import twitter4j.Status;
import twitter4j.Twitter;
import twitter4j.TwitterException;
import twitter4j.TwitterFactory;
import twitter4j.conf.ConfigurationBuilder;

@Service
public class TwitterServiceImpl implements TwitterService {

	private Twitter twitter;

	public TwitterServiceImpl() {
		ConfigurationBuilder cb = new ConfigurationBuilder();
		cb.setDebugEnabled(true).setOAuthConsumerKey("OD8YBShodAPOYdIKEZZnQeyAV")
				.setOAuthConsumerSecret("847wMB7qaeTMywRRgZz3D3p5fVcC7bCFsaJBjFIXw10vGySFpg")
				.setOAuthAccessToken("1508846932202033166-uDIr06NyPsqz4ltnYJEkZ3c1UwzFwy")
				.setOAuthAccessTokenSecret("30pqmHT1e9TOIG8nZZ4LKbypkJwbTiwfqizbphgWccDL9");
		TwitterFactory tf = new TwitterFactory(cb.build());
		twitter = tf.getInstance();
	}

	@Override
	public Iterable<Status> getPreviousFiveTweets() {
		ArrayList<Status> toReturn = new ArrayList<Status>();
		try {
			ResponseList<Status> responses = twitter.getUserTimeline();
			int numOfTweets = 5;
			if (responses.size() < 5)
				numOfTweets = responses.size();
			for (int i = 0; i < numOfTweets; i++) {
				toReturn.add(responses.get(i));
			}
		} catch (TwitterException e) {
			e.printStackTrace();
		}
		return toReturn;
	}

	@Override
	public void updateStatus(String message) throws TwitterException {
		Status status = twitter.updateStatus(message);
		System.out.println(message);
		System.out.println("Status has been updated to [" + status.getText() + "].");
	}

}
