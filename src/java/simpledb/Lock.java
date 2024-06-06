package simpledb;

import java.util.*;

public class Lock
{
    private LockType lockType;
    private PageId lockedPageId;
    private Set<TransactionId> sharedTidSet;
    private Set<TransactionId> exclusiveTidSet;
    private Set<TransactionId> upgradeTagSet;
    private Set<TransactionId> downgradeTagSet;
    public Lock(LockType lockType, PageId pageId)
    {
        this.lockType = lockType;
        this.lockedPageId = pageId;
//        this.sharedTidSet = Collections.synchronizedSet(new HashSet<TransactionId>());
//        this.exclusiveTidSet = Collections.synchronizedSet(new HashSet<TransactionId>());
//        this.upgradeTagSet = Collections.synchronizedSet(new HashSet<TransactionId>());
//        this.downgradeTagSet = Collections.synchronizedSet(new HashSet<TransactionId>());
        this.sharedTidSet = new HashSet<TransactionId>();
        this.exclusiveTidSet = new HashSet<TransactionId>();
        this.upgradeTagSet = new HashSet<TransactionId>();
        this.downgradeTagSet = new HashSet<TransactionId>();
    }

    public LockType getLockType()
    {
        return lockType;
    }

    public boolean setLockType(LockType targetType)
    {
        this.lockType = targetType;
        return true;
    }

    public PageId getLockedPageId()
    {
        return lockedPageId;
    }

     public Set<TransactionId> getSharedTidSet()
    {
        return sharedTidSet;
    }

     public Set<TransactionId> getExclusiveTidSet()
    {
        return exclusiveTidSet;
    }

     public Set<TransactionId> getUpgradeTagSet()
    {
        return upgradeTagSet;
    }

     public Set<TransactionId> getDowngradeTagSet()
    {
        return downgradeTagSet;
    }

    @Override
    public boolean equals(Object o)
    {
        if (this == o)
            return true;
        if (o == null || getClass() != o.getClass())
            return false;
        Lock lock = (Lock) o;
        return lockType == lock.lockType && lockedPageId.equals(lock.lockedPageId);
    }

    @Override
    public int hashCode()
    {
        return Objects.hash(lockType, lockedPageId);
    }
}
