package de.docm77.patreon.nicesleep;

import java.util.Set;

import org.bukkit.command.ConsoleCommandSender;
import org.bukkit.conversations.Conversation;
import org.bukkit.conversations.ConversationAbandonedEvent;
import org.bukkit.conversations.ManuallyAbandonedConversationCanceller;
import org.bukkit.permissions.PermissibleBase;
import org.bukkit.permissions.Permission;
import org.bukkit.permissions.PermissionAttachment;
import org.bukkit.permissions.PermissionAttachmentInfo;
import org.bukkit.Server;
import org.bukkit.plugin.Plugin;

public class CustomCommandSender implements ConsoleCommandSender {

  // private final PermissibleBase perm;
  private ConsoleCommandSender sender;

  public String lastMessage;

  CustomCommandSender(ConsoleCommandSender sender) {
    this.sender = sender;
  }

  @Override
  public void sendMessage(String message) {
    this.sender.getServer().getLogger().info("Sending msg: " + message);
    this.lastMessage = message;
    this.sender.sendMessage(message);
  }

  @Override
  public void sendMessage(String[] messages) {
    this.lastMessage = String.join(" ", messages);
    this.sender.getServer().getLogger().info("Sending msgs: " + String.join(" ", messages));
    this.sender.sendMessage(messages);
  }

  @Override
  public Server getServer() {
    return this.sender.getServer();
  }

  @Override
  public String getName() {
    return this.sender.getName();
  }

  @Override
  public boolean isPermissionSet(String name) {
    return this.sender.isPermissionSet(name);
  }

  @Override
  public boolean isPermissionSet(Permission perm) {
    return this.sender.isPermissionSet(perm);
  }

  @Override
  public boolean hasPermission(String name) {
    return this.sender.hasPermission(name);
  }

  @Override
  public boolean hasPermission(Permission perm) {
    return this.sender.hasPermission(perm);
  }

  @Override
  public PermissionAttachment addAttachment(Plugin plugin, String name, boolean value) {
    return this.sender.addAttachment(plugin, name, value);
  }

  @Override
  public PermissionAttachment addAttachment(Plugin plugin) {
    return this.sender.addAttachment(plugin);
  }

  @Override
  public PermissionAttachment addAttachment(Plugin plugin, String name, boolean value, int ticks) {
    return this.sender.addAttachment(plugin, name, value, ticks);
  }

  @Override
  public PermissionAttachment addAttachment(Plugin plugin, int ticks) {
    return this.sender.addAttachment(plugin, ticks);
  }

  @Override
  public void removeAttachment(PermissionAttachment attachment) {
    this.sender.removeAttachment(attachment);

  }

  @Override
  public void recalculatePermissions() {
    this.sender.recalculatePermissions();
  }

  @Override
  public Set<PermissionAttachmentInfo> getEffectivePermissions() {
    return this.sender.getEffectivePermissions();
  }

  @Override
  public boolean isOp() {
    return this.sender.isOp();
  }

  @Override
  public void setOp(boolean value) {
    this.sender.setOp(value);
  }

  @Override
  public Spigot spigot() {
    return this.sender.spigot();
  }

  @Override
  public boolean isConversing() {
    return sender.isConversing();
  }

  @Override
  public void acceptConversationInput(String input) {
    sender.acceptConversationInput(input);
  }

  @Override
  public boolean beginConversation(Conversation conversation) {
    return sender.beginConversation(conversation);
  }

  @Override
  public void abandonConversation(Conversation conversation) {
    sender.abandonConversation(conversation);
  }

  @Override
  public void abandonConversation(Conversation conversation, ConversationAbandonedEvent details) {
    sender.abandonConversation(conversation, details);
  }

  @Override
  public void sendRawMessage(String message) {
    lastMessage = message;
    this.sender.getServer().getLogger().info("Sending raw msg: " + message);
    sender.sendRawMessage(message);
  }
}