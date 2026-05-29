package me.eren.antiserverscanner;

import com.github.retrooper.packetevents.PacketEvents;
import io.netty.handler.ipfilter.IpFilterRule;
import io.netty.handler.ipfilter.IpFilterRuleType;
import io.netty.handler.ipfilter.RuleBasedIpFilter;
import io.papermc.paper.network.ChannelInitializeListenerHolder;
import org.bukkit.NamespacedKey;
import org.bukkit.command.PluginCommand;
import org.bukkit.plugin.java.JavaPlugin;

import java.net.InetSocketAddress;
import java.util.List;
import java.util.Set;
import java.util.concurrent.ConcurrentHashMap;

public final class AntiServerScanner extends JavaPlugin {

    private final Set<String> blacklistedIps = ConcurrentHashMap.newKeySet();
    private final NamespacedKey ipBlockerKey = new NamespacedKey(this, "ip_blocker");

    @Override
    public void onEnable() {
        saveDefaultConfig();

        PluginCommand pluginCommand = getCommand("ipblocker");
        assert pluginCommand != null : "The build is broken.";

        IPBlockerCommand ipBlockerCommand = new IPBlockerCommand(blacklistedIps);
        pluginCommand.setExecutor(ipBlockerCommand);
        pluginCommand.setTabCompleter(ipBlockerCommand);

        blacklistedIps.addAll(getConfig().getStringList("blocked-ips"));
        boolean logBans = getConfig().getBoolean("log-bans");
        AntiBotMode mode = AntiBotMode.valueOf(getConfig().getString("mode"));
        String validServerAddressPattern = getConfig().getString("valid-server-address-pattern");
		var listener = new AntiBotPacketListener(getLogger(), blacklistedIps, logBans, mode, validServerAddressPattern);
        PacketEvents.getAPI().getEventManager().registerListener(listener);

        ChannelInitializeListenerHolder.addListener(ipBlockerKey, channel ->
                channel.pipeline().addFirst("AntiServerScanner IP Filter", new RuleBasedIpFilter(new IpFilterRule() {
                    @Override
                    public boolean matches(InetSocketAddress remoteAddress) {
                        String ip = remoteAddress.getAddress().getHostAddress();
                        return blacklistedIps.contains(ip);
                    }

                    @Override
                    public IpFilterRuleType ruleType() {
                        return IpFilterRuleType.REJECT;
                    }
                }))
        );

        getLogger().info("Loaded " + blacklistedIps.size() + " blocked IPs");
    }

    @Override
    public void onDisable() {
        ChannelInitializeListenerHolder.removeListener(ipBlockerKey);
        getConfig().set("blocked-ips", List.copyOf(blacklistedIps));
        saveConfig();
        getLogger().info("Saved " + blacklistedIps.size() + " blocked IPs.");
    }

}
