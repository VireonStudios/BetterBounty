package dev.vireon.bounty.bounty;

import java.util.*;

// ngl, fully AI generated sloppy class, but it does the job.
// at least, we are not re-sorting the entire list every time someone views & changes the order.

// This class manages a collection of bounties, allowing for efficient retrieval and sorting by various fields.
// It supports operations such as adding, removing, updating bounties, and listing them sorted by amount, last updated time, or name.
public class BountyIndex {

    private final Map<UUID, Bounty> byId = new HashMap<>();

    private final NavigableMap<Long, LinkedHashSet<UUID>> byAmount = new TreeMap<>(Comparator.reverseOrder());
    private final NavigableMap<Long, LinkedHashSet<UUID>> byLastUpdated = new TreeMap<>(Comparator.reverseOrder());
    private final NavigableMap<String, LinkedHashSet<UUID>> byName = new TreeMap<>(String.CASE_INSENSITIVE_ORDER);

    public boolean contains(UUID id) {
        return byId.containsKey(id);
    }

    public Optional<Bounty> get(UUID id) {
        return Optional.ofNullable(byId.get(id));
    }

    public int size() {
        return byId.size();
    }

    public List<Bounty> listAll(SortField field) {
        return collectAllFromIndex(indexFor(field));
    }

    public List<Bounty> listByName() {
        return listAll(SortField.NAME);
    }

    public List<Bounty> listAllByAmount() {
        return listAll(SortField.AMOUNT);
    }

    public List<Bounty> listAllByLastUpdated() {
        return listAll(SortField.LAST_UPDATED);
    }

    public void put(Bounty bounty) {
        Objects.requireNonNull(bounty, "bounty");
        UUID id = bounty.getPlayerId();
        Bounty old = byId.get(id);
        if (old != null) removeFromIndices(id, old.getPlayerName(), old.getAmount(), old.getLastUpdated());

        byId.put(id, bounty);
        addToIndices(id, bounty.getPlayerName(), bounty.getAmount(), bounty.getLastUpdated());
    }

    public boolean remove(UUID id) {
        Bounty old = byId.remove(id);
        if (old == null) return false;
        removeFromIndices(id, old.getPlayerName(), old.getAmount(), old.getLastUpdated());
        return true;
    }

    public void updateName(UUID id, String newName) {
        Bounty b = byId.get(id);
        if (b == null) return;
        String oldName = b.getPlayerName();
        if (Objects.equals(oldName, newName)) return;
        removeFromIndices(id, oldName, b.getAmount(), b.getLastUpdated());
        b.setPlayerName(newName);
        addToIndices(id, newName, b.getAmount(), b.getLastUpdated());
    }

    public void updateAmount(UUID id, long newAmount) {
        Bounty b = byId.get(id);
        if (b == null) return;
        long old = b.getAmount();
        if (old == newAmount) return;
        removeFromIndices(id, b.getPlayerName(), old, b.getLastUpdated());
        b.setAmount(newAmount);
        addToIndices(id, b.getPlayerName(), newAmount, b.getLastUpdated());
    }

    public void touchLastUpdated(UUID id, long newLastUpdated) {
        Bounty b = byId.get(id);
        if (b == null) return;
        long old = b.getLastUpdated();
        if (old == newLastUpdated) return;
        removeFromIndices(id, b.getPlayerName(), b.getAmount(), old);
        b.setLastUpdated(newLastUpdated);
        addToIndices(id, b.getPlayerName(), b.getAmount(), newLastUpdated);
    }

    public void addAmount(UUID id, long delta) {
        Bounty b = byId.get(id);
        if (b == null) return;
        updateAmount(id, b.getAmount() + delta);
    }

    public void clear() {
        byId.clear();
        byAmount.clear();
        byLastUpdated.clear();
        byName.clear();
    }

    private NavigableMap<?, LinkedHashSet<UUID>> indexFor(SortField f) {
        return switch (f) {
            case AMOUNT -> byAmount;
            case LAST_UPDATED -> byLastUpdated;
            default -> byName;
        };
    }

    private <T> List<Bounty> collectAllFromIndex(NavigableMap<T, LinkedHashSet<UUID>> index) {
        List<Bounty> res = new ArrayList<>(byId.size());
        for (Map.Entry<T, LinkedHashSet<UUID>> e : index.entrySet()) {
            for (UUID id : e.getValue()) {
                Bounty b = byId.get(id);
                if (b != null) res.add(b);
            }
        }
        return res;
    }

    private void addToIndices(UUID id, String name, long amount, long updated) {
        byAmount.computeIfAbsent(amount, k -> new LinkedHashSet<>()).add(id);
        byLastUpdated.computeIfAbsent(updated, k -> new LinkedHashSet<>()).add(id);
        byName.computeIfAbsent(name == null ? "" : name, k -> new LinkedHashSet<>()).add(id);
    }

    private void removeFromIndices(UUID id, String name, long amount, long updated) {
        removeIdFromBucket(byAmount, amount, id);
        removeIdFromBucket(byLastUpdated, updated, id);
        removeIdFromBucket(byName, name == null ? "" : name, id);
    }

    private <T> void removeIdFromBucket(NavigableMap<T, LinkedHashSet<UUID>> map, T key, UUID id) {
        LinkedHashSet<UUID> set = map.get(key);
        if (set == null) return;
        set.remove(id);
        if (set.isEmpty()) map.remove(key);
    }

    public enum SortField {
        AMOUNT,
        LAST_UPDATED,
        NAME;

        public SortField next() {
            return switch (this) {
                case AMOUNT -> LAST_UPDATED;
                case LAST_UPDATED -> NAME;
                case NAME -> AMOUNT;
            };
        }
    }

}