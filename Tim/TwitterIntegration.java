/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package Tim;

import java.util.logging.Level;
import java.util.logging.Logger;
import org.pircbotx.Colors;
import twitter4j.*;
import twitter4j.auth.AccessToken;

/**
 *
 * @author Matthew Walker
 */
public class TwitterIntegration extends StatusAdapter {
	Twitter twitter;
	AccessToken token;
	static User BotTimmy;
	static User NaNoWordSprints;
	static User NaNoWriMo;
	static User officeduckfrank;
	
	public TwitterIntegration() {
		token = new AccessToken(Tim.db.getSetting("twitter_access_key"), Tim.db.getSetting("twitter_access_secret"));
		twitter = new TwitterFactory().getInstance();
		twitter.setOAuthConsumer(Tim.db.getSetting("twitter_consumer_key"), Tim.db.getSetting("twitter_consumer_secret"));
		twitter.setOAuthAccessToken(token);

		try {
			BotTimmy = twitter.showUser("BotTimmy");
			Thread.sleep(250);
			NaNoWordSprints = twitter.showUser("NaNoWordSprints");
			Thread.sleep(250);
			NaNoWriMo = twitter.showUser("NaNoWriMo");
			Thread.sleep(250);
			officeduckfrank = twitter.showUser("officeduckfrank");
		} catch (TwitterException | InterruptedException ex) {
			Logger.getLogger(TwitterIntegration.class.getName()).log(Level.SEVERE, null, ex);
		}
	}
	
	public void sendTweet(String message) {
		try {
			if (message.length() > 118) {
				message = message.substring(0, 115) + "...";
			}

			StatusUpdate status = new StatusUpdate(message + " #NaNoWriMo #FearTimmy");
			twitter.updateStatus(status);
		} catch (TwitterException ex) {
			Logger.getLogger(TwitterIntegration.class.getName()).log(Level.SEVERE, null, ex);
		}
	}
	
	public void startStream() {
		long[] userIds = {NaNoWriMo.getId(), NaNoWordSprints.getId(), BotTimmy.getId(), officeduckfrank.getId()};
		String[] hashtags = {"#NaNoWriMo"};

		FilterQuery filter = new FilterQuery(0, userIds, hashtags);

		TwitterStream publicStream = new TwitterStreamFactory().getInstance();
		publicStream.setOAuthConsumer(Tim.db.getSetting("twitter_consumer_key"), Tim.db.getSetting("twitter_consumer_secret"));
		publicStream.setOAuthAccessToken(token);
		publicStream.addListener(publicListener);
		publicStream.filter(filter);

		TwitterStream userStream = new TwitterStreamFactory().getInstance();
		userStream.setOAuthConsumer(Tim.db.getSetting("twitter_consumer_key"), Tim.db.getSetting("twitter_consumer_secret"));
		userStream.setOAuthAccessToken(token);
		userStream.addListener(userListener);
		userStream.user();
	}

	StatusListener publicListener = new StatusListener() {
		@Override
		public void onStatus( Status status ) {
			String colorString;
			Relationship checkFriendship;

			if (status.getUser().getScreenName().equals("NaNoWriMo") && status.getInReplyToUserId() == -1) {
				colorString = Colors.BOLD + Colors.DARK_BLUE;
			} else if (status.getUser().getScreenName().equals("NaNoWordSprints") && status.getInReplyToUserId() == -1) {
				colorString = Colors.BOLD + Colors.DARK_GREEN;
			} else if (status.getUser().getScreenName().equals("BotTimmy") && status.getInReplyToUserId() == -1) {
				colorString = Colors.BOLD + Colors.RED;
			} else if (status.getUser().getScreenName().equals("officeduckfrank") && status.getInReplyToUserId() == -1) {
				colorString = Colors.BOLD + Colors.MAGENTA;
			} else {
				try {
					checkFriendship = twitter.showFriendship(BotTimmy.getId(), status.getUser().getId());
					if (status.getText().toLowerCase().contains("#nanowrimo") && Tim.rand.nextInt(100) < 3 && checkFriendship.isTargetFollowingSource() ) {
						int r = Tim.rand.nextInt(Tim.amusement.eightballs.size());
						StatusUpdate reply = new StatusUpdate("@" + status.getUser().getScreenName() + " " + Tim.amusement.eightballs.get(r) + " #NaNoWriMo #FearTimmy");
						reply.setInReplyToStatusId(status.getId());
						twitter.updateStatus(reply);
					}
				} catch (TwitterException ex) {
					Logger.getLogger(TwitterIntegration.class.getName()).log(Level.SEVERE, null, ex);
				}

				return;
			}

			String message = colorString + "@" + status.getUser().getScreenName() + ": " + Colors.NORMAL + status.getText();

			for (ChannelInfo channel : Tim.db.channel_data.values()) {
				if (status.getUser().getId() == NaNoWriMo.getId() && channel.relayNaNoWriMo) {
					Tim.bot.sendMessage(channel.channel, message);
				} else if (status.getUser().getId() == NaNoWordSprints.getId() && channel.relayNaNoWordSprints) {
					Tim.bot.sendMessage(channel.channel, message);
				} else if (status.getUser().getId() == BotTimmy.getId() && channel.relayBotTimmy) {
					Tim.bot.sendMessage(channel.channel, message);
				} else if (status.getUser().getId() == officeduckfrank.getId() && channel.relayofficeduckfrank) {
					Tim.bot.sendMessage(channel.channel, message);
				}
			}
		}

		@Override
		public void onDeletionNotice( StatusDeletionNotice sdn ) {
		}

		@Override
		public void onTrackLimitationNotice( int i ) {
		}

		@Override
		public void onScrubGeo( long l, long l1 ) {
		}

		@Override
		public void onException( Exception excptn ) {
		}

		@Override
		public void onStallWarning(StallWarning sw) {
		}

	};

	StatusListener userListener = new UserStreamListener() {
		@Override
		public void onStatus( Status status ) {
			boolean sendReply = false;
			boolean getItem = false;
			if (status.getInReplyToUserId() == TwitterIntegration.BotTimmy.getId()) {
				sendReply = true;
				if (Tim.rand.nextInt(100) < 15) {
					getItem = true;
				}
			} else if (status.getText().toLowerCase().contains("@bottimmy") && Tim.rand.nextInt(100) < 50) {
				sendReply = true;
				if (Tim.rand.nextInt(100) < 25) {
					getItem = true;
				}
			} else if (Tim.rand.nextInt(100) < 2) {
				sendReply = true;
				if (Tim.rand.nextInt(100) < 50) {
					getItem = true;
				}
			}

			if (status.getUser().getId() == TwitterIntegration.BotTimmy.getId()) {
				sendReply = false;
			}

			if (sendReply) {
				try {
					StatusUpdate reply;
					if (getItem) {
						int r = Tim.rand.nextInt(Tim.amusement.approved_items.size());
						reply = new StatusUpdate("@" + status.getUser().getScreenName() + " Here, have " + Tim.amusement.approved_items.get(r) + " #NaNoWriMo #FearTimmy");
					} else {
						int r = Tim.rand.nextInt(Tim.amusement.eightballs.size());
						reply = new StatusUpdate("@" + status.getUser().getScreenName() + " " + Tim.amusement.eightballs.get(r) + " #NaNoWriMo #FearTimmy");
					}
					reply.setInReplyToStatusId(status.getId());
					twitter.updateStatus(reply);
				} catch (TwitterException ex) {
					Logger.getLogger(TwitterIntegration.class.getName()).log(Level.SEVERE, null, ex);
				}
			}
		}

		@Override
		public void onDeletionNotice( StatusDeletionNotice sdn ) {
		}

		@Override
		public void onTrackLimitationNotice( int i ) {
		}

		@Override
		public void onScrubGeo( long l, long l1 ) {
		}

		@Override
		public void onException( Exception excptn ) {
		}

		@Override
		public void onDeletionNotice( long l, long l1 ) {
		}

		@Override
		public void onFriendList( long[] longs ) {
		}

		@Override
		public void onFavorite( User user, User user1, Status status ) {
		}

		@Override
		public void onUnfavorite( User user, User user1, Status status ) {
		}

		@Override
		public void onFollow( User user, User user1 ) {
		}

		@Override
		public void onDirectMessage( DirectMessage dm ) {
		}

		@Override
		public void onUserListMemberAddition( User user, User user1, UserList ul ) {
		}

		@Override
		public void onUserListMemberDeletion( User user, User user1, UserList ul ) {
		}

		@Override
		public void onUserListSubscription( User user, User user1, UserList ul ) {
		}

		@Override
		public void onUserListUnsubscription( User user, User user1, UserList ul ) {
		}

		@Override
		public void onUserListCreation( User user, UserList ul ) {
		}

		@Override
		public void onUserListUpdate( User user, UserList ul ) {
		}

		@Override
		public void onUserListDeletion( User user, UserList ul ) {
		}

		@Override
		public void onUserProfileUpdate( User user ) {
		}

		@Override
		public void onBlock( User user, User user1 ) {
		}

		@Override
		public void onUnblock( User user, User user1 ) {
		}

		@Override
		public void onStallWarning(StallWarning sw) {
		}
	};
}

/*
 * Exception in thread "main" java.lang.IllegalStateException: UserStreamListener is not set.
        at twitter4j.TwitterStreamImpl.ensureUserStreamListenerIsSet(TwitterStreamImpl.java:327)
        at twitter4j.TwitterStreamImpl.user(TwitterStreamImpl.java:196)
        at twitter4j.TwitterStreamImpl.user(TwitterStreamImpl.java:187)
        at Tim.TwitterIntegration.startStream(TwitterIntegration.java:271)
        at Tim.Tim.<init>(Tim.java:86)
        at Tim.Tim.main(Tim.java:39)

 */