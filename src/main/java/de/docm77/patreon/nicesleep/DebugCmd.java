package de.docm77.patreon.nicesleep;

import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;

import java.util.logging.ConsoleHandler;
import java.util.logging.Handler;
import java.util.logging.Level;
import java.util.logging.Logger;

class DebugCmd implements CommandExecutor
{
  private final LoggerUtil logger;

  public DebugCmd(LoggerUtil logger)
  {
    this.logger = logger;
  }

  @Override
  public boolean onCommand(CommandSender commandSender, Command command, String s, String[] args)
  {
    if(args.length != 1)
    {
      return false;
    }
    String arg = args[0];
    if(arg.equals("on"))
    {
      this.logger.setLogLevel(Level.ALL);
      this.logger.info("Enabling extended logging");
    }
    else if(arg.equals("off"))
    {
      this.logger.setLogLevel(Level.INFO);
      this.logger.info("Disabling extended logging");
    }
    else
    {
      return false;
    }
    return true;
  }

}
