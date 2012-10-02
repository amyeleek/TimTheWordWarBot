/**
 * This file is part of Timmy, the Wordwar Bot.
 *
 * Timmy is free software: you can redistribute it and/or modify it under the terms of the GNU General Public License as
 * published by the Free Software Foundation, either version 3 of the License, or (at your option) any later version.
 *
 * Timmy is distributed in the hope that it will be useful, but WITHOUT ANY WARRANTY; without even the implied warranty
 * of MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE. See the GNU General Public License for more details.
 *
 * You should have received a copy of the GNU General Public License along with Timmy. If not, see
 * <http://www.gnu.org/licenses/>.
 */
package Tim;

import java.util.logging.Level;
import java.util.logging.Logger;
import org.pircbotx.Channel;
import org.pircbotx.Colors;
import org.pircbotx.hooks.ListenerAdapter;
import org.pircbotx.hooks.events.MessageEvent;

/**
 *
 * @author mwalker
 */
public class UserCommandListener extends ListenerAdapter {
	
	@Override
	public void onMessage(MessageEvent event) {
		String message = Colors.removeFormattingAndColors(event.getMessage());
		Channel channel = event.getChannel();

		if (message.charAt(0) == '!') {
			String command;
			String[] args = null;

			int space = message.indexOf(" ");
			if (space > 0) {
				command = message.substring(1, space).toLowerCase();
				args = message.substring(space + 1).split(" ", 0);
			} else {
				command = message.substring(1).toLowerCase();
			}

			if (command.equals("startwar")) {
				if (args != null && args.length > 1) {
					Tim.warticker.startWar(event, args);
				} else {
					event.respond("Usage: !startwar <duration in min> [<time to start in min> [<name>]]");
				}
			} else if (command.equals("endwar")) {
				Tim.warticker.endWar(event, args);
			} else if (command.equals("listwars")) {
				Tim.warticker.listWars(event, false);
			} else if (command.equals("listall")) {
				Tim.warticker.listAllWars(event);
			} else if (command.equals("boxodoom")) {
				Tim.amusement.boxodoom(event, args);
			} else if (command.equals("eggtimer")) {
				double time = 15;
				if (args != null) {
					try {
						time = Double.parseDouble(args[0]);
					} catch (Exception e) {
						event.respond("Could not understand first parameter. Was it numeric?");
						return;
					}
				}

				event.respond("Your timer has been set.");
				try {
					Thread.sleep((long) (time * 60 * 1000));
				} catch (InterruptedException ex) {
					Logger.getLogger(UserCommandListener.class.getName()).log(Level.SEVERE, null, ex);
				}
				event.respond("Your timer has expired!");
			} else if (command.equals("help")) {
				this.printCommandList(event);
			} else if (command.equals("credits")) {
				event.respond(
					"I was created by MysteriousAges in 2008 using PHP, and ported to the Java PircBot library in 2009. "
					+ "Utoxin started helping during NaNoWriMo 2010. Sourcecode is available here: "
					+ "https://github.com/MysteriousAges/TimTheWordWarBot, and my NaNoWriMo profile page is here: "
					+ "http://www.nanowrimo.org/en/participants/timmybot");
			} else if (Tim.story.parseUserCommand(event)) {
			} else if (Tim.challenge.parseUserCommand(event)) {
			} else if (Tim.amusement.parseUserCommand(event)) {
			} else {
				event.respond("!" + command + " was not part of my training.");
			}
		}
	}

	private void printCommandList( MessageEvent event ) {
		Tim.bot.sendAction(event.getChannel(), "whispers something to " + event.getUser().getNick() + ". (Check for a new window or tab with the help text.)");

		String[] strs = {"I am a robot trained by the WordWar Monks of Honolulu. You have "
						 + "never heard of them. It is because they are awesome.",
						 "Core Commands:",
						 "    !startwar <duration> <time to start> <an optional name> - Starts a word war",
						 "    !listwars - I will tell you about the wars currently in progress.",
						 "    !boxodoom <difficulty> <duration> - Difficulty is easy/average/hard, duration in minutes.",
						 "    !eggtimer <time> - I will send you a message after <time> minutes.",
						 "    !settopic <topic> - If able, I will try to set the channel's topic.",
						 "    !credits - Details of my creators, and where to find my source code.",
		};
		for (int i = 0; i < strs.length; ++i) {
			Tim.bot.sendNotice(event.getUser(), strs[i]);
		}

		Tim.story.helpSection(event);
		Tim.challenge.helpSection(event);
		Tim.amusement.helpSection(event);
		
		String[] post = {"I... I think there might be other tricks I know... You'll have to find them!",
						 "I will also respond to the /invite command if you would like to see me in another channel. "
		};
		for (int i = 0; i < post.length; ++i) {
			Tim.bot.sendNotice(event.getUser(), post[i]);
		}
	}
}
