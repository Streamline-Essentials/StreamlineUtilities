package tv.quaint.essentials;

import net.streamline.api.savables.users.StreamlinePlayer;

import java.util.concurrent.ConcurrentSkipListSet;
import java.util.concurrent.atomic.AtomicReference;

public class EssentialsManager {
    static ConcurrentSkipListSet<TPARequest> pendingTPARequests = new ConcurrentSkipListSet<>();

    public static void addTPARequest(TPARequest request) {
        pendingTPARequests.add(request);
    }

    public static void removeTPARequest(TPARequest request) {
        pendingTPARequests.remove(request);
    }

    public static TPARequest getTPARequest(String senderUuid, String receiverUuid) {
        AtomicReference<TPARequest> atomicReference = new AtomicReference<>();
        pendingTPARequests.forEach((request) -> {
            if (request.getSender().getUuid().equals(senderUuid) && request.getReceiver().getUuid().equals(receiverUuid)) {
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

    public static void acceptTPA(StreamlinePlayer senderPlayer, StreamlinePlayer otherPlayer) {
        TPARequest request = getTPARequest(senderPlayer.getUuid(), otherPlayer.getUuid());
        if (request != null) {
            request.perform();
        }
    }

    public static void denyTPA(StreamlinePlayer senderPlayer, StreamlinePlayer otherPlayer) {
        TPARequest request = getTPARequest(senderPlayer.getUuid(), otherPlayer.getUuid());
        if (request != null) {
            request.deny();
        }
    }
}
