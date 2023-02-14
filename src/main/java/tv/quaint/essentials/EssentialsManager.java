package tv.quaint.essentials;

import lombok.Getter;
import lombok.Setter;
import net.streamline.api.messages.proxied.ProxiedMessage;
import net.streamline.api.savables.users.StreamlinePlayer;
import net.streamline.api.scheduler.ModuleDelayedRunnable;
import tv.quaint.StreamlineUtilities;

import java.util.concurrent.ConcurrentSkipListSet;
import java.util.concurrent.atomic.AtomicReference;

public class EssentialsManager {
    public static class TeleportRunner extends ModuleDelayedRunnable {
        @Getter
        @Setter
        private ProxiedMessage message;

        public TeleportRunner(long ticksDelayed, ProxiedMessage message) {
            super(StreamlineUtilities.getInstance(), ticksDelayed);
            this.message = message;
        }

        public TeleportRunner(ProxiedMessage message) {
            this(StreamlineUtilities.getConfigs().getTPAWaitTime(), message);
        }

        @Override
        public void runDelayed() {
            this.message.send();
        }
    }

    static ConcurrentSkipListSet<TPARequest> pendingTPARequests = new ConcurrentSkipListSet<>();

    public static void addTPARequest(TPARequest request) {
        pendingTPARequests.add(request);
    }

    public static void removeTPARequest(TPARequest request) {
        pendingTPARequests.remove(request);
    }

    public static TPARequest getTPARequest(String senderUuid, String receiverUuid, TPARequest.TransportType transportType) {
        AtomicReference<TPARequest> atomicReference = new AtomicReference<>();
        pendingTPARequests.forEach((request) -> {
            if (request.getSender().getUuid().equals(senderUuid) && request.getReceiver().getUuid().equals(receiverUuid) && request.getTransportType().equals(transportType)) {
                atomicReference.set(request);
            }
        });
        return atomicReference.get();
    }

    public static void requestTPA(StreamlinePlayer sender, StreamlinePlayer receiver) {
        TPARequest request = new TPARequest(sender, receiver, TPARequest.TransportType.SENDER_TO_RECEIVER);
        addTPARequest(request);
    }

    public static void requestTPAHere(StreamlinePlayer sender, StreamlinePlayer receiver) {
        TPARequest request = new TPARequest(sender, receiver, TPARequest.TransportType.RECEIVER_TO_SENDER);
        addTPARequest(request);
    }

    public static void acceptTPA(StreamlinePlayer senderPlayer, StreamlinePlayer otherPlayer, TPARequest.TransportType transportType) {
        TPARequest request = getTPARequest(senderPlayer.getUuid(), otherPlayer.getUuid(), transportType);
        if (request != null) {
            request.perform();
        }
    }

    public static void denyTPA(StreamlinePlayer senderPlayer, StreamlinePlayer otherPlayer, TPARequest.TransportType transportType) {
        TPARequest request = getTPARequest(senderPlayer.getUuid(), otherPlayer.getUuid(), transportType);
        if (request != null) {
            request.deny();
        }
    }

    public static ConcurrentSkipListSet<TPARequest> getPendingTPARequests(StreamlinePlayer asPlayerSentTo, TPARequest.TransportType transportType) {
        ConcurrentSkipListSet<TPARequest> requests = new ConcurrentSkipListSet<>();
        pendingTPARequests.forEach((request) -> {
            if (request.getReceiver().getUuid().equals(asPlayerSentTo.getUuid()) && request.getTransportType() == transportType) {
                requests.add(request);
            }
        });
        return requests;
    }

    public static TPARequest getLatestPendingTPARequest(StreamlinePlayer asPlayerSentTo, TPARequest.TransportType transportType) {
        ConcurrentSkipListSet<TPARequest> requests = getPendingTPARequests(asPlayerSentTo, transportType);
        if (requests.size() > 0) {
            return requests.last();
        }
        return null;
    }
}
