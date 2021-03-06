package com.elmakers.mine.bukkit.magic.command;

import com.elmakers.mine.bukkit.api.magic.Mage;
import com.elmakers.mine.bukkit.api.magic.MageClass;
import com.elmakers.mine.bukkit.api.magic.MagicAPI;
import com.elmakers.mine.bukkit.api.magic.MagicProperties;
import com.elmakers.mine.bukkit.api.spell.Spell;
import com.elmakers.mine.bukkit.api.spell.SpellTemplate;
import com.elmakers.mine.bukkit.block.MaterialBrush;
import com.elmakers.mine.bukkit.magic.BaseMagicProperties;
import com.elmakers.mine.bukkit.utility.DeprecatedUtils;

import com.elmakers.mine.bukkit.utility.InventoryUtils;
import org.apache.commons.lang.StringUtils;
import org.bukkit.ChatColor;
import org.bukkit.command.Command;
import org.bukkit.command.CommandSender;
import org.bukkit.configuration.ConfigurationSection;
import org.bukkit.entity.Player;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.List;

public class MageCommandExecutor extends MagicConfigurableExecutor {
	public MageCommandExecutor(MagicAPI api) {
		super(api);
	}
	
	@Override
	public boolean onCommand(CommandSender sender, Command command, String label, String[] args) {
		if (args.length == 0)
		{
			if (!api.hasPermission(sender, "Magic.commands.mage")) {
				sendNoPermission(sender);
				return true;
			}
			return false;
		}
		
		String subCommand = args[0];
		if (sender instanceof Player)
		{
			if (!api.hasPermission(sender, "Magic.commands.mage." + subCommand)) {
				sendNoPermission(sender);
				return true;
			}
		}

		Player player = null;
		int argStart = 1;

		if (sender instanceof Player) {
			if (args.length > 1)
			{
				player = DeprecatedUtils.getPlayer(args[1]);
			}
			if (player == null)
			{
				player = (Player)sender;
			}
			else
			{
				argStart = 2;
			}
		} else {
			if (args.length <= 1) {
				sender.sendMessage("Must specify a player name");
				return true;
			}
			argStart = 2;
			player = DeprecatedUtils.getPlayer(args[1]);
			if (player == null) {
				sender.sendMessage("Can't find player " + args[1]);
				return true;
			}
			if (!player.isOnline()) {
				sender.sendMessage("Player " + args[1] + " is not online");
				return true;
			}
		}

		String[] args2 = Arrays.copyOfRange(args, argStart, args.length);

		if (subCommand.equalsIgnoreCase("check"))
		{
			return onMageCheck(sender, player, args2);
		}
        if (subCommand.equalsIgnoreCase("reset"))
        {
            return onMageReset(sender, player, args2);
        }
		if (subCommand.equalsIgnoreCase("debug"))
		{
			return onMageDebug(sender, player, args2);
		}
		if (subCommand.equalsIgnoreCase("getdata"))
		{
			return onMageGetData(sender, player, args2);
		}
		if (subCommand.equalsIgnoreCase("setdata"))
		{
			return onMageSetData(sender, player, args2);
		}
        if (subCommand.equalsIgnoreCase("unbind"))
        {
            return onMageUnbind(sender, player, args2);
        }
        if (subCommand.equalsIgnoreCase("configure"))
        {
            return onMageConfigure(sender, player, args2, false);
        }
        if (subCommand.equalsIgnoreCase("upgrade"))
        {
            return onMageConfigure(sender, player, args2, true);
        }
        if (subCommand.equalsIgnoreCase("describe"))
        {
            return onMageDescribe(sender, player, args2);
        }
        if (subCommand.equalsIgnoreCase("activate"))
        {
            return onMageActivate(sender, player, args2);
        }
        if (subCommand.equalsIgnoreCase("unlock"))
        {
            return onMageUnlock(sender, player, args2);
        }
        if (subCommand.equalsIgnoreCase("add"))
        {
            return onMageAdd(sender, player, args2);
        }
        if (subCommand.equalsIgnoreCase("remove"))
        {
            return onMageRemove(sender, player, args2);
        }

		sender.sendMessage("Unknown mage command: " + subCommand);
		return true;
	}
	
	@Override
	public Collection<String> onTabComplete(CommandSender sender, String commandName, String[] args) {
		List<String> options = new ArrayList<>();
		if (args.length == 1) {
            addIfPermissible(sender, options, "Magic.commands.mage.", "add");
            addIfPermissible(sender, options, "Magic.commands.mage.", "remove");
            addIfPermissible(sender, options, "Magic.commands.mage.", "configure");
            addIfPermissible(sender, options, "Magic.commands.mage.", "describe");
            addIfPermissible(sender, options, "Magic.commands.mage.", "upgrade");
            addIfPermissible(sender, options, "Magic.commands.mage.", "getdata");
            addIfPermissible(sender, options, "Magic.commands.mage.", "setdata");
            addIfPermissible(sender, options, "Magic.commands.mage.", "check");
            addIfPermissible(sender, options, "Magic.commands.mage.", "debug");
			addIfPermissible(sender, options, "Magic.commands.mage.", "reset");
            addIfPermissible(sender, options, "Magic.commands.mage.", "unbind");
            addIfPermissible(sender, options, "Magic.commands.mage.", "activate");
            addIfPermissible(sender, options, "Magic.commands.mage.", "unlock");
		} else if (args.length == 2) {
			String subCommand = args[0];
			String subCommandPNode = "Magic.commands.mage." + subCommand;

			options.addAll(api.getPlayerNames());
            if (subCommand.equalsIgnoreCase("configure") || subCommand.equalsIgnoreCase("describe") || subCommand.equalsIgnoreCase("upgrade")) {
                for (String key : BaseMagicProperties.PROPERTY_KEYS) {
                    options.add(key);
                }
            }

            if (subCommand.equalsIgnoreCase("add")) {
				Collection<SpellTemplate> spellList = api.getSpellTemplates(sender.hasPermission("Magic.bypass_hidden"));
				for (SpellTemplate spell : spellList) {
					addIfPermissible(sender, options, subCommandPNode, spell.getKey(), true);
				}
				addIfPermissible(sender, options, subCommandPNode, "brush", true);
			}

			if (subCommand.equalsIgnoreCase("remove")) {
                Mage mage = api.getMage(sender);
                MageClass mageClass = mage.getActiveClass();
                if (mageClass != null) {
                    options.addAll(mageClass.getSpells());
                }
                options.add("brush");
			}

		} else if (args.length == 3) {
		    String subCommand = args[0];
			String subCommandPNode = "Magic.commands.mage." + subCommand;
			if (subCommand.equalsIgnoreCase("setdata") || subCommand.equalsIgnoreCase("getdata")) {
                Player player = DeprecatedUtils.getPlayer(args[1]);
                if (player != null) {
                    Mage mage = api.getMage(player);
                    ConfigurationSection data = mage.getData();
                    options.addAll(data.getKeys(false));
                }
            }

			if (subCommand.equalsIgnoreCase("add")) {
                Collection<SpellTemplate> spellList = api.getSpellTemplates(sender.hasPermission("Magic.bypass_hidden"));
				for (SpellTemplate spell : spellList) {
					addIfPermissible(sender, options, subCommandPNode, spell.getKey(), true);
				}
				addIfPermissible(sender, options, subCommandPNode, "brush", true);
            }

            if (subCommand.equalsIgnoreCase("remove")) {
                Player player = DeprecatedUtils.getPlayer(args[1]);
                if (player != null) {
                    Mage mage = api.getMage(player);
                    MageClass mageClass = mage.getActiveClass();
                    if (mageClass != null) {
                        options.addAll(mageClass.getSpells());
                    }
                }
                options.add("brush");
            }
		} else if (args.length == 2) {
            if (args[0].equalsIgnoreCase("unlock") || args[0].equalsIgnoreCase("activate")) {
                options.addAll(api.getController().getMageClassKeys());
            }
        }
		return options;
	}

    public boolean onMageCheck(CommandSender sender, Player player, String[] args)
    {
        Mage mage = api.getMage(player);
		mage.debugPermissions(sender, null);
        return true;
    }

    public boolean onMageReset(CommandSender sender, Player player, String[] args)
    {
        if (args.length == 0) {
            api.getController().deleteMage(player.getUniqueId().toString());
            sender.sendMessage(ChatColor.RED + "Reset player " + player.getName());
        } else {
            Mage mage = api.getMage(player);
            if (mage.removeClass(args[0])) {
                sender.sendMessage(ChatColor.RED + "Reset " + ChatColor.GOLD + "class " + args[0] + " for player " + player.getName());
            } else {
                sender.sendMessage(ChatColor.RED + "Player " + player.getName() + " does not have class " + args[0] + " unlocked");
            }
        }
        return true;
    }

    public boolean onMageDebug(CommandSender sender, Player player, String[] args)
    {
        Mage mage = api.getMage(player);
        if (args.length > 0) {
            try {
                int level = Integer.parseInt(args[0]);
                mage.setDebugLevel(level);
				if (level > 0) {
					mage.setDebugger(sender);
				} else {
					mage.setDebugger(null);
				}
                sender.sendMessage(ChatColor.GOLD + "Setting debug level for  " + ChatColor.AQUA + player.getDisplayName() + ChatColor.GOLD + " to " + ChatColor.GREEN + Integer.toString(level));
            } catch (Exception ex) {
                sender.sendMessage("Expecting integer, got: " + args[0]);
            }
            return true;
        }
        if (mage.getDebugLevel() > 0) {
            sender.sendMessage(ChatColor.GOLD + "Disabling debug for " + ChatColor.AQUA + player.getDisplayName());
            mage.setDebugLevel(0);
			mage.setDebugger(null);
        } else {
            sender.sendMessage(ChatColor.AQUA + "Enabling debug for " + ChatColor.AQUA + player.getDisplayName());
            mage.setDebugLevel(1);
			mage.setDebugger(sender);
        }
        return true;
    }

    public boolean onMageGetData(CommandSender sender, Player player, String[] args)
    {
        Mage mage = api.getMage(player);
        ConfigurationSection data = mage.getData();
        if (args != null && args.length > 0)
        {
            if (args[0].equals("*"))
            {
                sender.sendMessage(ChatColor.GOLD + "Mage data for " + ChatColor.AQUA + player.getDisplayName() + ChatColor.GOLD + ": ");
                Collection<Spell> spells = mage.getSpells();
                if (spells.size() == 0) {
                    sender.sendMessage(ChatColor.RED + "No spell casts!");
                    return true;
                }
                for (Spell spell : spells) {
                    sender.sendMessage(ChatColor.LIGHT_PURPLE + spell.getName() + ChatColor.AQUA + " Cast Count: " + ChatColor.GOLD + spell.getCastCount());
                }
                return true;
            }
            Spell spell = mage.getSpell(args[0]);
            if (spell != null)
            {
                sender.sendMessage(ChatColor.GOLD + "Mage data for " + ChatColor.AQUA + player.getDisplayName() + ChatColor.GOLD + ": " + ChatColor.LIGHT_PURPLE + spell.getName());
                sender.sendMessage(ChatColor.AQUA + " Cast Count: " + ChatColor.GOLD + spell.getCastCount());
                return true;
            }
            ConfigurationSection subSection = data.getConfigurationSection(args[0]);
            if (subSection == null) {
                sender.sendMessage(ChatColor.RED + "Unknown subsection or spell: " + args[0]);
                return true;
            }
            data = subSection;
        }
        Collection<String> keys = data.getKeys(false);
        sender.sendMessage(ChatColor.GOLD + "Mage data for " + ChatColor.AQUA + player.getDisplayName());
        for (String key : keys) {
            if (data.isConfigurationSection(key)) {
                ConfigurationSection subSection = data.getConfigurationSection(key);
                sender.sendMessage(ChatColor.AQUA + " " + key + ChatColor.DARK_AQUA + " (" + subSection.getKeys(true).size() + " items)");
            } else {
                String value = data.getString(key);
                if (value != null) {
                    sender.sendMessage(ChatColor.AQUA + " " + key + ChatColor.DARK_AQUA + " (" + value + ")");
                } else {
                    sender.sendMessage(ChatColor.AQUA + " " + key);
                }
            }
        }
        return true;
    }

    public boolean onMageSetData(CommandSender sender, Player player, String[] args)
    {
        Mage mage = api.getMage(player);
        if (args.length == 1)
        {
            ConfigurationSection data = mage.getData();
            String key = args[0];
            if (!data.contains(key)) {
                sender.sendMessage(ChatColor.RED + "No data found with key " + ChatColor.AQUA + key + ChatColor.RED + " for " + ChatColor.DARK_AQUA + player.getDisplayName());
                return true;
            }
            data.set(key, null);
            sender.sendMessage(ChatColor.GOLD + "Removed data for key " + ChatColor.AQUA + key + ChatColor.GOLD  + " for " + ChatColor.DARK_AQUA + player.getDisplayName());
            return true;
        }
        if (args.length != 2)
        {
            return false;
        }
        if (args[0].equals("*"))
        {
            long value = 0;
            try {
                value = Long.parseLong(args[1]);
            } catch (Exception ex) {
                sender.sendMessage(ChatColor.RED + "Cast count must be a number");
                return true;
            }
            Collection<Spell> spells = mage.getSpells();
            for (Spell spell : spells)
            {
                spell.setCastCount(value);
            }
            sender.sendMessage(ChatColor.GOLD + "Set all spell cast counts to " + ChatColor.AQUA + value + ChatColor.GOLD + " for " + ChatColor.DARK_AQUA + player.getDisplayName());
            return true;
        }
        Spell spell = mage.getSpell(args[0]);
        if (spell != null)
        {
            long value = 0;
            try {
                value = Long.parseLong(args[1]);
            } catch (Exception ex) {
                sender.sendMessage(ChatColor.RED + "Cast count must be a number");
                return true;
            }
            spell.setCastCount(value);
            sender.sendMessage(ChatColor.GOLD + "Set " + ChatColor.AQUA + spell.getName() + ChatColor.GOLD + " cast count to " + ChatColor.AQUA + value + ChatColor.GOLD + " for " + ChatColor.DARK_AQUA + player.getDisplayName());
            return true;
        }

        ConfigurationSection data = mage.getData();
        String key = args[0];
        String value = args[1];
        data.set(key, value);
        sender.sendMessage(ChatColor.GOLD + "Set " + ChatColor.AQUA + key + ChatColor.GOLD + " to " + ChatColor.AQUA + value + ChatColor.GOLD + " for " + ChatColor.DARK_AQUA + player.getDisplayName());
        return true;
    }

    public boolean onMageUnbind(CommandSender sender, Player player, String[] parameters)
    {
        Mage mage = api.getMage(player);
        if (parameters.length > 0) {
            String template = parameters[0];
            if (mage.unbind(template)) {
                mage.sendMessage(api.getMessages().get("wand.unbound"));
                if (sender != player) {
                    sender.sendMessage(api.getMessages().getParameterized("wand.player_unbound", "$name", player.getName()));
                }
            } else {
                mage.sendMessage(api.getMessages().get("wand.notunbound").replace("$wand", parameters[0]));
                if (sender != player) {
                    sender.sendMessage(api.getMessages().getParameterized("wand.player_notunbound", "$name", player.getName()).replace("$wand", parameters[0]));
                }
            }
            return true;
        }

        mage.unbindAll();

        mage.sendMessage(api.getMessages().get("wand.unboundall"));
        if (sender != player) {
            sender.sendMessage(api.getMessages().getParameterized("wand.player_unboundall", "$name", player.getName()));
        }
        return true;
    }

    public boolean onMageConfigure(CommandSender sender, Player player, String[] parameters, boolean safe)
    {
        Mage mage = api.getMage(player);
        MageClass activeClass = mage.getActiveClass();
        return onConfigure("mage", activeClass == null ? mage.getProperties() : activeClass, sender, player, parameters, safe);
    }

    public boolean onMageUnlock(CommandSender sender, Player player, String[] parameters)
    {
        if (parameters.length < 1) {
            sender.sendMessage(ChatColor.RED + "Usage: " + ChatColor.WHITE + "/mage unlock [player] <class>");
            return true;
        }
        Mage mage = api.getMage(player);
        String classKey = parameters[0];
        MageClass mageClass = mage.unlockClass(classKey);
        if (mageClass == null) {
            sender.sendMessage(ChatColor.RED + "Invalid class: " + ChatColor.WHITE + classKey);
        } else {
            sender.sendMessage("Unlocked class " + classKey + " for " + player.getName());
        }
        return true;
    }

    public boolean onMageActivate(CommandSender sender, Player player, String[] parameters)
    {
        Mage mage = api.getMage(player);
        String classKey = parameters.length == 0 ? null : parameters[0];
        if (mage.setActiveClass(classKey)) {
            if (classKey == null) {
                sender.sendMessage("Cleared active class for " + player.getName());
            } else {
                sender.sendMessage("Activated class " + classKey + " for " + player.getName());
            }
        } else {
            sender.sendMessage(ChatColor.RED + player.getName() + " does not have class: " + ChatColor.WHITE + classKey + ChatColor.RED + " unlocked");
        }
        return true;
    }

    public boolean onMageDescribe(CommandSender sender, Player player, String[] parameters) {
        Mage mage = api.getMage(player);
        MageClass activeClass = mage.getActiveClass();
        MagicProperties mageProperties = activeClass;
        if (mageProperties == null) {
            mageProperties = mage.getProperties();
        }

        if (parameters.length == 0) {
            sender.sendMessage(ChatColor.BLUE + "Use " + ChatColor.AQUA + "/mage describe <property>" + ChatColor.BLUE + " for specific properties");
            sender.sendMessage(ChatColor.BLUE + "Use " + ChatColor.AQUA + "/mage activate" + ChatColor.BLUE + " to change or clear the active class");
            Collection<String> classKeys = mage.getClassKeys();
            if (classKeys.size() > 0) {
                sender.sendMessage(ChatColor.AQUA + "Classes: " + ChatColor.GREEN + StringUtils.join(classKeys, ","));
            }
            if (activeClass != null) {
                sender.sendMessage(ChatColor.AQUA + "Active class: " + ChatColor.GREEN + activeClass.getKey());
            } else {
                sender.sendMessage(ChatColor.DARK_GREEN + "No active class");
            }
            mageProperties.describe(sender, BaseMagicProperties.HIDDEN_PROPERTY_KEYS);
        } else {
            Object property = mageProperties.getProperty(parameters[0]);
            if (property == null) {
                sender.sendMessage(ChatColor.DARK_AQUA + parameters[0] + ChatColor.GRAY + ": " + ChatColor.RED + "(Not Set)");
            } else {
                sender.sendMessage(ChatColor.DARK_AQUA + parameters[0] + ChatColor.GRAY + ": " + ChatColor.WHITE + InventoryUtils.describeProperty(property));
            }
        }

        return true;
    }


	public boolean onMageAdd(CommandSender sender, Player player, String[] parameters)
	{
		if (parameters.length < 1) {
			sender.sendMessage("Use: /mage add <spell|material> [material:data]");
			return true;
		}

		Mage mage = api.getMage(player);
        MageClass activeClass = mage.getActiveClass();
		if (activeClass == null) {
		    sender.sendMessage("Can't modify player " + player.getName());
			return true;
		}

		String spellName = parameters[0];
		if (spellName.equals("material") || spellName.equals("brush")) {
			if (parameters.length < 2) {
				sender.sendMessage("Use: /mage add brush <material:data>");
				return true;
			}

			String materialKey = parameters[1];
			if (!MaterialBrush.isValidMaterial(materialKey, false)) {
				sender.sendMessage(materialKey + " is not a valid brush");
				return true;
			}

			if (activeClass.addBrush(materialKey)) {
				if (sender != player) {
					sender.sendMessage("Added brush '" + materialKey + "' to " + player.getName());
				} else {
                    sender.sendMessage(api.getMessages().get("mage.brush_added").replace("$name", materialKey));
                }
			}

			return true;
		}
		Spell spell = mage.getSpell(spellName);
		if (spell == null)
		{
			sender.sendMessage("Spell '" + spellName + "' unknown, Use /spells for spell list");
			return true;
		}

        SpellTemplate currentSpell = activeClass.getBaseSpell(spellName);
		if (activeClass.addSpell(spellName)) {
            if (currentSpell != null) {
                String levelDescription = spell.getLevelDescription();
                if (levelDescription == null || levelDescription.isEmpty()) {
                    levelDescription = spell.getName();
                }
                if (sender != player) {
                    sender.sendMessage(api.getMessages().get("mage.player_spell_upgraded").replace("$player", player.getName()).replace("$name", currentSpell.getName()).replace("$level", levelDescription));
                } else {
                    sender.sendMessage(api.getMessages().get("mage.spell_upgraded").replace("$player", player.getName()).replace("$name", currentSpell.getName()).replace("$level", levelDescription));
                }
            } else {
                if (sender != player) {
                    sender.sendMessage("Added '" + spell.getName() + "' to " + player.getName());
                } else {
                    sender.sendMessage(api.getMessages().get("mage.spell_added").replace("$name", spell.getName()));
                }
            }
		} else if (sender != player) {
            sender.sendMessage("Could not add " + spellName + " to " + player.getName());
        }
		return true;
	}

	public boolean onMageRemove(CommandSender sender, Player player, String[] parameters)
	{
		if (parameters.length < 1) {
			sender.sendMessage("Use: /mage remove <spell|material> [material:data]");
			return true;
		}

		Mage mage = api.getMage(player);
        MageClass activeClass = mage.getActiveClass();
		if (activeClass == null) {
		    sender.sendMessage("Can't modify player " + player.getName());
			return true;
		}

		String spellName = parameters[0];
		if (spellName.equals("material") || spellName.equals("brush")) {
			if (parameters.length < 2) {
				sender.sendMessage("Use: /mage remove brush <material:data>");
				return true;
			}
			String materialKey = parameters[1];

			if (activeClass.removeBrush(materialKey)) {
				mage.sendMessage("Brush '" + materialKey + "' has been removed");
				if (sender != player) {
					sender.sendMessage("Removed brush '" + materialKey + "' from " + player.getName());
				}
			} else {
				if (sender != player) {
					sender.sendMessage(player.getName() + " does not have brush " + materialKey);
				}
			}

			return true;
		}

		if (activeClass.removeSpell(spellName)) {
            SpellTemplate template = api.getSpellTemplate(spellName);
            if (template != null) {
                spellName = template.getName();
            }
			mage.sendMessage("Spell '" + spellName + "' has been removed");
			if (sender != player) {
				sender.sendMessage("Removed '" + spellName + "' from " + player.getName());
			}
		} else {
			if (sender != player) {
				sender.sendMessage(player.getName() + " does not have " + spellName);
			}
		}

		return true;
	}
}
