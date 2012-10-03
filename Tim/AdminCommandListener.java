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

import org.pircbotx.Colors;
import org.pircbotx.hooks.ListenerAdapter;
import org.pircbotx.hooks.events.MessageEvent;

/**
 *
 * @author Matthew Walker
 */
public class AdminCommandListener extends ListenerAdapter {
	@Override
	public void onMessage(MessageEvent event) {
		String message = Colors.removeFormattingAndColors(event.getMessage());

		if (message.charAt(0) == '$') {
			if (Tim.db.admin_list.contains(event.getUser().getNick().toLowerCase()) || Tim.db.admin_list.contains(event.getChannel().getName().toLowerCase())) {
				String command;
				String[] args = null;

				int space = message.indexOf(" ");
				if (space > 0) {
					command = message.substring(0, space).toLowerCase();
					args = message.substring(space + 1).split(" ", 0);
				} else {
					command = message.substring(0).toLowerCase();
				}

				if (command.equals("setadultflag")) {
					if (args != null && args.length == 2) {
						String target = args[0].toLowerCase();
						if (Tim.db.channel_data.containsKey(target)) {
							boolean flag = false;
							if (!"0".equals(args[1])) {
								flag = true;
							}

							Tim.db.setChannelAdultFlag(target, flag);
							event.respond("Channel adult flag updated for " + target);
						} else {
							event.respond("I don't know about " + target);
						}
					} else {
						event.respond("Usage: $setadultflag <#channel> <0/1>");
					}
				} else if (command.equals("setmuzzleflag")) {
					if (args != null && args.length == 2) {
						String target = args[0].toLowerCase();
						if (Tim.db.channel_data.containsKey(target)) {
							boolean flag = false;
							if (!"0".equals(args[1])) {
								flag = true;
							}

							Tim.db.setChannelMuzzledFlag(target, flag);
							event.respond("Channel muzzle flag updated for " + target);
						} else {
							event.respond("I don't know about " + target);
						}
					} else {
						event.respond("Usage: $setmuzzleflag <#channel> <0/1>");
					}
				} else if (command.equals("shutdown")) {
					event.respond("Shutting down...");
					Tim.bot.shutdown();
				} else if (command.equals("reload") || command.equals("refreshdb")) {
					event.respond("Reading database tables ...");
					Tim.db.refreshDbLists();
					event.respond("Tables reloaded.");
				} else if (command.equals("ignore")) {
					if (args != null && args.length > 0) {
						String users = "";
						for (int i = 0; i < args.length; ++i) {
							users += " " + args[i];
							Tim.db.ignore_list.add(args[i]);
							Tim.db.saveIgnore(args[i]);
						}
						event.respond("The following users have been ignored:" + users);
					} else {
						event.respond("Usage: $ignore <user 1> [ <user 2> [<user 3> [...] ] ]");
					}
				} else if (command.equals("unignore")) {
					if (args != null && args.length > 0) {
						String users = "";
						for (int i = 0; i < args.length; ++i) {
							users += " " + args[i];
							Tim.db.ignore_list.remove(args[i]);
							Tim.db.deleteIgnore(args[i]);
						}
						event.respond("The following users have been unignored:" + users);
					} else {
						event.respond("Usage: $unignore <user 1> [ <user 2> [<user 3> [...] ] ]");
					}
				} else if (command.equals("listignores")) {
					event.respond("There are " + Tim.db.ignore_list.size() + " users ignored.");

					for (String item : Tim.db.ignore_list) {
						event.respond(item);
					}
				} else if (command.equals("help")) {
					this.printAdminCommandList(event);
				} else if (Tim.amusement.parseAdminCommand(event)) {
				} else if (Tim.challenge.parseAdminCommand(event)) {
				} else if (Tim.markhov.parseAdminCommand(event)) {
				} else {
					event.respond("$" + command + " is not a valid admin command - try $help");
				}
			} else {
				event.respond("You are not an admin. Only Admins have access to that command.");
			}
		}
	}

	private void printAdminCommandList( MessageEvent event ) {
		Tim.bot.sendAction(event.getChannel(), "whispers in " + event.getUser().getNick() + "'s ear. (Check for a new windor or tab with the help text.)");

		String[] helplines = {"Core Admin Commands:",
							  "    $setadultflag <#channel> <0/1> - clears/sets adult flag on channel",
							  "    $setmuzzleflag <#channel> <0/1> - clears/sets muzzle flag on channel",
							  "    $shutdown - Forces bot to exit",
							  "    $reload - Reloads data from MySQL (also $refreshdb)",
							  "    $reset - Resets internal timer for wars, and reloads data from MySQL",
							  "    $listitems [ <page #> ] - lists all currently approved !get/!getfor items",
							  "    $listpending [ <page #> ] - lists all unapproved !get/!getfor items",
							  "    $approveitem <item # from $listpending> - removes item from pending list and marks as approved for !get/!getfor",
							  "    $disapproveitem <item # from $listitems> - removes item from approved list and marks as pending for !get/!getfor",
							  "    $deleteitem <item # from $listpending> - permanently removes an item from the pending list for !get/!getfor",
							  "    $ignore <username> - Places user on the bot's ignore list",
							  "    $unignore <username> - Removes user from bot's ignore list",
							  "    $listignores - Prints the list of ignored users"
		};

		for (int i = 0; i < helplines.length; ++i) {
			Tim.bot.sendNotice(event.getUser(), helplines[i]);
		}

		Tim.challenge.adminHelpSection(event);
		Tim.markhov.adminHelpSection(event);
	}

}
