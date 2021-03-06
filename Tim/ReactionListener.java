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

import java.util.ArrayList;
import java.util.regex.Pattern;
import org.pircbotx.Channel;
import org.pircbotx.Colors;
import org.pircbotx.User;
import org.pircbotx.hooks.ListenerAdapter;
import org.pircbotx.hooks.events.ActionEvent;
import org.pircbotx.hooks.events.MessageEvent;

public class ReactionListener extends ListenerAdapter {
	@Override
	public void onMessage(MessageEvent event) {
		String message = Colors.removeFormattingAndColors(event.getMessage());
		ChannelInfo cdata = Tim.db.channel_data.get(event.getChannel().getName().toLowerCase());

		if (cdata.muzzled) {
			return;
		}
		
		updateOdds(cdata);

		if (!Tim.db.ignore_list.contains(event.getUser().getNick().toLowerCase()) &&
			!Tim.db.soft_ignore_list.contains(event.getUser().getNick().toLowerCase())) {
			if (message.charAt(0) != '$' && message.charAt(0) != '!') {
				if (!message.equals(":(") && !message.equals("):")) {
					cdata.lastSpeaker = event.getUser();
					cdata.lastSpeakerTime = System.currentTimeMillis();
				} else if (cdata.lastSpeakerTime <= (System.currentTimeMillis() - (60 * 1000))) {
					cdata.lastSpeaker = event.getUser();
				}

				if (message.toLowerCase().contains("how many lights") && cdata.chatter_enabled.get("silly_reactions")) {
					if (Tim.rand.nextInt(100) < cdata.lights_odds) {
						event.getChannel().send().message("There are FOUR LIGHTS!");
						cdata.lights_odds--;
					}
				} else if (message.toLowerCase().contains("what does the fox say") && cdata.chatter_enabled.get("silly_reactions")) {
					if (Tim.rand.nextInt(100) < cdata.fox_odds) {
						event.respond("Foxes don't talk. Sheesh.");
						cdata.fox_odds--;
					}
				} else if (message.toLowerCase().contains("when will then be now") && cdata.chatter_enabled.get("silly_reactions")) {
					if (Tim.rand.nextInt(100) < cdata.soon_odds) {
						event.respond("Soon.");
						cdata.soon_odds--;
					}
				} else if (message.toLowerCase().contains("velociraptor") && cdata.chatter_enabled.get("velociraptor")) {
					if (Tim.rand.nextInt(100) < cdata.velociraptor_odds) {
						cdata.recordVelociraptorSighting();
						event.respond("Velociraptor sighted! Incident has been logged.");
						cdata.velociraptor_odds--;
						
						if (cdata.activeVelociraptors > 10) {
							int odds = (cdata.activeVelociraptors - 10);
							if (odds > 50) {
								odds = 50;
							}
							
							if (Tim.rand.nextInt(100) < odds) {
								String attack = Tim.db.getRandomChannelWithVelociraptors(cdata.channel);
								
								if (!attack.equals("")) {
									ChannelInfo victimCdata = Tim.db.channel_data.get(attack);
									int attackCount = Tim.rand.nextInt(cdata.activeVelociraptors / 2);
									int defendingCount = Tim.rand.nextInt(victimCdata.activeVelociraptors / 2) + (victimCdata.activeVelociraptors / 2);
									double killPercent = (((double) attackCount / (double) defendingCount) * 25.0) + Tim.rand.nextInt(attackCount);

									if (killPercent > 100) {
										killPercent = 100;
									}

									int killCount = (int) (defendingCount * (killPercent / 100.0));

									cdata.recordSwarmKills(attackCount, killCount);
									victimCdata.recordSwarmDeaths(killCount);
									
									event.getChannel().send().message(String.format("Suddenly, %d of the velociraptors go charging off to attack a group in %s! "
										+ "After a horrific battle, they manage to kill %d of them...", attackCount, attack, killCount));

									if (victimCdata.chatter_enabled.get("velociraptor") && victimCdata.muzzled == false) {
										Tim.bot.sendIRC().message(victimCdata.channel, String.format("A swarm of %d velociraptors suddenly appears from the direction of %s. "
											+ "The local raptors do their best to fight them off, and %d of them die before the swarm disappears.", attackCount, cdata.channel, killCount));
									}
								}
							}
						}
					}
					Tim.markov.process_markov(message, "say", event.getUser().getNick());
				} else if (message.toLowerCase().contains("cheeseburger") && cdata.chatter_enabled.get("silly_reactions")) {
					if (Tim.rand.nextInt(100) < cdata.cheeseburger_odds) {
						event.respond("I can has cheezburger?");
						cdata.cheeseburger_odds--;
					}
				} else if (message.toLowerCase().startsWith("test") && cdata.chatter_enabled.get("silly_reactions")) {
					if (Tim.rand.nextInt(100) < cdata.test_odds) {
						event.respond("After due consideration, your test earned a: " + pickGrade());
						cdata.test_odds--;
					}
				} else if ((message.contains(":(") || message.contains("):")) && cdata.chatter_enabled.get("silly_reactions")) {
					if (Tim.rand.nextInt(100) < cdata.hug_odds) {
						event.getChannel().send().action("gives " + cdata.lastSpeaker.getNick() + " a hug.");
						cdata.hug_odds--;
					}
				} else if (message.contains(":'(") && cdata.chatter_enabled.get("silly_reactions")) {
					if (Tim.rand.nextInt(100) < cdata.tissue_odds) {
						event.getChannel().send().action("passes " + event.getUser().getNick() + " a tissue.");
						cdata.tissue_odds--;
					}
				} else if (Pattern.matches("(?i).*how do (i|you) (change|set) ?(my|your)? (nick|name).*", message) && cdata.chatter_enabled.get("helpful_reactions")) {
					event.respond("To change your name type the following, putting the name you want instead of NewNameHere: /nick NewNameHere");
				} else if (Pattern.matches("(?i).*are you (thinking|pondering) what i.*m (thinking|pondering).*", message) && cdata.chatter_enabled.get("silly_reactions")) {
					if (Tim.rand.nextInt(100) < cdata.aypwip_odds) {
						int i = Tim.rand.nextInt(Tim.amusement.aypwips.size());
						event.getChannel().send().message(String.format(Tim.amusement.aypwips.get(i), event.getUser().getNick()));
						cdata.aypwip_odds--;
					}
				} else if (Pattern.matches("(?i).*what.*is.*the.*answer.*", message) && cdata.chatter_enabled.get("silly_reactions")) {
					if (Tim.rand.nextInt(100) < cdata.answer_odds) {
						event.respond("The answer is 42. Everyone knows that.");
						cdata.answer_odds--;
					}
				} else if (Pattern.matches("(?i)" + Tim.bot.getNick() + ".*[?]", message) && cdata.chatter_enabled.get("silly_reactions")) {
					if (Tim.rand.nextInt(100) < cdata.eightball_odds) {
						Tim.amusement.eightball(event.getChannel(), event.getUser(), false, message);
						cdata.eightball_odds--;
					}
				} else {
					this.interact(event.getUser(), event.getChannel(), message, "say");
					Tim.markov.process_markov(message, "say", event.getUser().getNick());
				}
			}
		}
	}

	@Override
	public void onAction(ActionEvent event) {
		String message = Colors.removeFormattingAndColors(event.getMessage());
		ChannelInfo cdata = Tim.db.channel_data.get(event.getChannel().getName().toLowerCase());

		if (event.getUser().getLogin().toLowerCase().equals("utoxin")) {
			if (message.equalsIgnoreCase("punches " + Tim.bot.getNick() + " in the face!")) {
				event.getChannel().send().action("falls over and dies.  x.x");
				Tim.shutdown();
			}
		}

		if (cdata.muzzled) {
			return;
		}

		updateOdds(cdata);

		if (!Tim.db.ignore_list.contains(event.getUser().getNick().toLowerCase()) &&
			!Tim.db.soft_ignore_list.contains(event.getUser().getNick().toLowerCase())
		) {
			if (message.toLowerCase().contains("how many lights") && cdata.chatter_enabled.get("silly_reactions")) {
				if (Tim.rand.nextInt(100) < cdata.lights_odds) {
					event.getChannel().send().message("There are FOUR LIGHTS!");
					cdata.lights_odds--;
				}
			} else if (message.toLowerCase().contains("what does the fox say") && cdata.chatter_enabled.get("silly_reactions")) {
				if (Tim.rand.nextInt(100) < cdata.fox_odds) {
					event.respond("mutters under his breath. \"Foxes don't talk. Sheesh.\"");
					cdata.fox_odds--;
				}
			} else if (message.toLowerCase().contains("when will then be now") && cdata.chatter_enabled.get("silly_reactions")) {
				if (Tim.rand.nextInt(100) < cdata.soon_odds) {
					event.respond("replies with certainty, \"Soon.\"");
					cdata.soon_odds--;
				}
			} else if (message.toLowerCase().contains("velociraptor") && cdata.chatter_enabled.get("velociraptor")) {
				if (Tim.rand.nextInt(100) < cdata.velociraptor_odds) {
					cdata.recordVelociraptorSighting();
					event.respond("jots down a note in a red notebook labeled 'Velociraptor Sighting Log'.");
					cdata.velociraptor_odds--;
						
					if (cdata.activeVelociraptors > 10) {
						int odds = (int) (Math.log(cdata.activeVelociraptors) * 20);

						if (Tim.rand.nextInt(100) < odds) {
							String attack = Tim.db.getRandomChannelWithVelociraptors(cdata.channel);

							if (!attack.equals("")) {
								ChannelInfo victimCdata = Tim.db.channel_data.get(attack);
								int attackCount = Tim.rand.nextInt(cdata.activeVelociraptors / 2);
								int defendingCount = Tim.rand.nextInt(victimCdata.activeVelociraptors / 2) + (victimCdata.activeVelociraptors / 2);
								double killPercent = (((double) attackCount / (double) defendingCount) * 25.0) + Tim.rand.nextInt(attackCount);

								if (killPercent > 100) {
									killPercent = 100;
								}

								int killCount = (int) (defendingCount * (killPercent / 100.0));
								
								cdata.recordSwarmKills(attackCount, killCount);
								victimCdata.recordSwarmDeaths(killCount);

								event.getChannel().send().message(String.format("Suddenly, %d of the velociraptors go charging off to attack a group in %s! "
									+ "After a horrific battle, they manage to kill %d of them...", attackCount, attack, killCount));

								if (victimCdata.chatter_enabled.get("velociraptor") && victimCdata.muzzled == false) {
									Tim.bot.sendIRC().message(victimCdata.channel, String.format("A swarm of %d velociraptors suddenly appears from the direction of %s. "
										+ "The local raptors do their best to fight them off, and %d of them die before the swarm disappears.", attackCount, cdata.channel, killCount));
								}
							}
						}
					}
				}
				Tim.markov.process_markov(message, "emote", event.getUser().getNick());
			} else if (message.toLowerCase().contains("cheeseburger") && cdata.chatter_enabled.get("silly_reactions")) {
				if (Tim.rand.nextInt(100) < cdata.cheeseburger_odds) {
					event.respond("sniffs the air, and peers around. \"Can has cheezburger?\"");
					cdata.cheeseburger_odds--;
				}
			} else if ((message.contains(":(") || message.contains("):")) && cdata.chatter_enabled.get("silly_reactions")) {
				if (Tim.rand.nextInt(100) < cdata.hug_odds) {
					event.getChannel().send().action("gives " + event.getUser().getNick() + " a hug.");
					cdata.hug_odds--;
				}
			} else if (message.toLowerCase().startsWith("tests") && cdata.chatter_enabled.get("silly_reactions")) {
				if (Tim.rand.nextInt(100) < cdata.test_odds) {
					event.respond("considers, and gives " + event.getUser().getNick() + " a grade: " + pickGrade());
					cdata.test_odds--;
				}
			} else if (message.contains(":'(") && cdata.chatter_enabled.get("silly_reactions")) {
				if (Tim.rand.nextInt(100) < cdata.tissue_odds) {
					event.getChannel().send().action("passes " + event.getUser().getNick() + " a tissue.");
					cdata.tissue_odds--;
				}
			} else if (Pattern.matches("(?i).*how do (i|you) (change|set) ?(my|your)? (nick|name).*", message) && cdata.chatter_enabled.get("helpful_reactions")) {
				event.respond("To change your name type the following, putting the name you want instead of NewNameHere: /nick NewNameHere");
			} else if (Pattern.matches("(?i).*are you (thinking|pondering) what i.*m (thinking|pondering).*", message) && cdata.chatter_enabled.get("silly_reactions")) {
				if (Tim.rand.nextInt(100) < cdata.aypwip_odds) {
					int i = Tim.rand.nextInt(Tim.amusement.aypwips.size());
					event.getChannel().send().message(String.format(Tim.amusement.aypwips.get(i), event.getUser().getNick()));
					cdata.aypwip_odds--;
				}
			} else if (Pattern.matches("(?i).*what.*is.*the.*answer.*", message) && cdata.chatter_enabled.get("silly_reactions")) {
				if (Tim.rand.nextInt(100) < cdata.answer_odds) {
					event.respond("sighs at the question. \"The answer is 42. I thought you knew that...\"");
					cdata.answer_odds--;
				}
			} else if (Pattern.matches("(?i)" + Tim.bot.getNick() + ".*[?]", message) && cdata.chatter_enabled.get("silly_reactions")) {
				if (Tim.rand.nextInt(100) < cdata.eightball_odds) {
					Tim.amusement.eightball(event.getChannel(), event.getUser(), false, message);
					cdata.eightball_odds--;
				}
			} else {
				this.interact(event.getUser(), event.getChannel(), message, "emote");
				Tim.markov.process_markov(message, "emote", event.getUser().getNick());
			}
		}
	}

	private void interact(User sender, Channel channel, String message, String type) {
		ChannelInfo cdata = Tim.db.channel_data.get(channel.getName().toLowerCase());

		if (cdata.randomChatterLevel <= 0) {
			return;
		}

		long odds = cdata.reactiveChatterLevel;

		if (message.toLowerCase().contains(Tim.bot.getNick().toLowerCase())) {
			odds = odds * cdata.chatterNameMultiplier;
		}

		if (Tim.rand.nextInt(100) < odds) {
			ArrayList<String> enabled_actions = new ArrayList<>(16);

			if (cdata.chatter_enabled.get("markov")) {
				enabled_actions.add("markov");
				enabled_actions.add("markov");
				enabled_actions.add("markov");
			}
			if (cdata.amusement_chatter_available()) {
				enabled_actions.add("amusement");
				enabled_actions.add("amusement");
				enabled_actions.add("amusement");
			}
			if (cdata.chatter_enabled.get("challenge")) {
				enabled_actions.add("challenge");
			}

			if (enabled_actions.isEmpty()) {
				return;
			}

			String action = enabled_actions.toArray(new String[enabled_actions.size()])[Tim.rand.nextInt(enabled_actions.size())];

			switch (action) {
				case "markov":
					if ("say".equals(type) && Tim.rand.nextBoolean()) {
						type = "mutter";
					} else if ("emote".equals(type) && Tim.rand.nextBoolean()) {
						type = "mutter";
					}
					Tim.markov.randomAction(channel.getName().toLowerCase(), type, message);
					break;
				case "challenge":
					Tim.challenge.randomAction(sender, cdata.channel);
					break;
				case "amusement":
					Tim.amusement.randomAction(sender, cdata.channel);
					break;
			}
		}
	}

	private String pickGrade() {
		int grade = Tim.rand.nextInt(50) + 51;

		if (grade < 60) {
			return "F";
		} else if (grade < 63) {
			return "D-";
		} else if (grade < 67) {
			return "D";
		} else if (grade < 70) {
			return "D+";
		} else if (grade < 73) {
			return "C-";
		} else if (grade < 77) {
			return "C";
		} else if (grade < 80) {
			return "C+";
		} else if (grade < 83) {
			return "B-";
		} else if (grade < 87) {
			return "B";
		} else if (grade < 90) {
			return "B+";
		} else if (grade < 93) {
			return "A-";
		} else if (grade < 97) {
			return "A";
		} else {
			return "A+";
		}
	}
	
	private void updateOdds(ChannelInfo cdata) {
		if (Tim.rand.nextInt(100) == 0) {
			if (cdata.lights_odds < cdata.max_lights_odds) {
				cdata.lights_odds++;
			}
			if (cdata.fox_odds < cdata.max_fox_odds) {
				cdata.fox_odds++;
			}
			if (cdata.cheeseburger_odds < cdata.max_cheeseburger_odds) {
				cdata.cheeseburger_odds++;
			}
			if (cdata.test_odds < cdata.max_test_odds) {
				cdata.test_odds++;
			}
			if (cdata.hug_odds < cdata.max_hug_odds) {
				cdata.hug_odds++;
			}
			if (cdata.tissue_odds < cdata.max_tissue_odds) {
				cdata.tissue_odds++;
			}
			if (cdata.aypwip_odds < cdata.max_aypwip_odds) {
				cdata.aypwip_odds++;
			}
			if (cdata.answer_odds < cdata.max_answer_odds) {
				cdata.answer_odds++;
			}
			if (cdata.eightball_odds < cdata.max_eightball_odds) {
				cdata.eightball_odds++;
			}
			if (cdata.soon_odds < cdata.max_soon_odds) {
				cdata.soon_odds++;
			}
			if (cdata.velociraptor_odds < cdata.max_velociraptor_odds) {
				cdata.velociraptor_odds++;
			}
		}
	}
}
