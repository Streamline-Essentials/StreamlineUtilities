package host.plas.essentials;

import host.plas.StreamlineUtilities;
import host.plas.essentials.users.UtilitiesUser;
import lombok.Getter;
import lombok.Setter;
import net.streamline.api.data.console.StreamSender;
import net.streamline.api.data.players.StreamPlayer;
import net.streamline.api.messages.proxied.ProxiedMessage;
import net.streamline.api.scheduler.ModuleDelayedRunnable;

import java.util.Optional;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.ConcurrentSkipListSet;
import java.util.concurrent.atomic.AtomicReference;

public class EssentialsManager {
    @Setter
    @Getter
    public static class TeleportRunner extends ModuleDelayedRunnable {
        private ProxiedMessage message;

        public TeleportRunner(long ticksDelayed, ProxiedMessage message) {
            super(StreamlineUtilities.getInstance(), ticksDelayed);
            this.message = message;
        }

        public TeleportRunner(ProxiedMessage message) {
            this(StreamlineUtilities.getConfigs().getTPADelayTicks(), message);
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

    public static void requestTPA(StreamPlayer sender, StreamPlayer receiver) {
        TPARequest request = new TPARequest(sender, receiver, TPARequest.TransportType.SENDER_TO_RECEIVER);
        addTPARequest(request);
    }

    public static void requestTPAHere(StreamPlayer sender, StreamPlayer receiver) {
        TPARequest request = new TPARequest(sender, receiver, TPARequest.TransportType.RECEIVER_TO_SENDER);
        addTPARequest(request);
    }

    public static void acceptTPA(StreamPlayer senderPlayer, StreamPlayer otherPlayer, TPARequest.TransportType transportType) {
        TPARequest request = getTPARequest(senderPlayer.getUuid(), otherPlayer.getUuid(), transportType);
        if (request != null) {
            request.perform();
        }
    }

    public static void denyTPA(StreamPlayer senderPlayer, StreamPlayer otherPlayer, TPARequest.TransportType transportType) {
        TPARequest request = getTPARequest(senderPlayer.getUuid(), otherPlayer.getUuid(), transportType);
        if (request != null) {
            request.deny();
        }
    }

    public static ConcurrentSkipListSet<TPARequest> getPendingTPARequests(StreamPlayer asPlayerSentTo, TPARequest.TransportType transportType) {
        ConcurrentSkipListSet<TPARequest> requests = new ConcurrentSkipListSet<>();
        pendingTPARequests.forEach((request) -> {
            if (request.getReceiver().getUuid().equals(asPlayerSentTo.getUuid()) && request.getTransportType() == transportType) {
                requests.add(request);
            }
        });
        return requests;
    }

    public static TPARequest getLatestPendingTPARequest(StreamPlayer asPlayerSentTo, TPARequest.TransportType transportType) {
        ConcurrentSkipListSet<TPARequest> requests = getPendingTPARequests(asPlayerSentTo, transportType);
        if (requests.size() > 0) {
            return requests.last();
        }
        return null;
    }
}
