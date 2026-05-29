package me.eren.antiserverscanner;

import com.github.retrooper.packetevents.event.PacketListenerAbstract;
import com.github.retrooper.packetevents.event.PacketReceiveEvent;
import com.github.retrooper.packetevents.protocol.packettype.PacketType;
import com.github.retrooper.packetevents.wrapper.handshaking.client.WrapperHandshakingClientHandshake;
import io.netty.channel.Channel;

import java.net.InetSocketAddress;
import java.util.Set;
import java.util.logging.Logger;
import java.util.regex.Pattern;

public class AntiBotPacketListener extends PacketListenerAbstract {

	private final Logger logger;
	private final Set<String> blockedIps;
	private final boolean logBans;
	private final AntiBotMode mode;
	private final Pattern validServerAddressPattern;

	public AntiBotPacketListener(Logger logger, Set<String> blockedIps, boolean logBans,
								 AntiBotMode mode, String validServerAddressRegex) {
		this.logger = logger;
		this.blockedIps = blockedIps;
		this.logBans = logBans;
		this.mode = mode;
		this.validServerAddressPattern = Pattern.compile(validServerAddressRegex);
	}

	@Override
	public void onPacketReceive(PacketReceiveEvent event) {
		if (event.getPacketType() != PacketType.Handshaking.Client.HANDSHAKE) {
			return;
		}

		WrapperHandshakingClientHandshake handshake = new WrapperHandshakingClientHandshake(event);

		if (handshake.getProtocolVersion() < 1 || handshake.getServerAddress().isEmpty()) {
			handleScanner(event, "Invalid Handshake");
			return;
		}

		if (!validServerAddressPattern.matcher(handshake.getServerAddress()).matches()) {
			handleScanner(event, "Invalid Server Address");
		}
	}

	private void handleScanner(PacketReceiveEvent event, String reason) {
		Channel channel = (Channel) event.getChannel();
		InetSocketAddress remote = (InetSocketAddress) channel.remoteAddress();
		String clientIp = remote.getAddress().getHostAddress();

		if (mode == AntiBotMode.BAN) {
			blockedIps.add(clientIp);

			if (logBans) {
				logger.warning("Blacklisted " + clientIp + " for being a scanner (" + reason + ")");
			}
		}

		event.setCancelled(true);
	}

}
