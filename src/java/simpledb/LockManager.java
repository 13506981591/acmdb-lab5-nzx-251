package simpledb;

import java.util.HashSet;
import java.util.Hashtable;
import java.util.Random;
import java.util.Set;

public class LockManager
{
    private Hashtable<TransactionId, HashSet<Lock>> tidLockTable;
    private Hashtable<PageId, Lock> pidLockTable;

    public static final int BASE_TIMELIMIT = 500; // 200 ms for max waiting time base
    public static final int VAR_TIMELIMIT = 500; // 200 ms for varies waiting time
    public static final int WAIT_PERIOD = 50; // 50 ms per wait
    public static final Random RANDOM_GENERATOR = new Random();

    public LockManager()
    {
        this.tidLockTable = new Hashtable<TransactionId, HashSet<Lock>>();
        this.pidLockTable = new Hashtable<PageId, Lock>();
    }

    /**
     * Lock the page with pid, the type of lock is lockType, and the
     * transaction that launch the lock is tid.
     * <p>
     * Throws TransactionAbortedException if failed to get the lock.
     *<p>
     * ThreadSafe, the method is synchronized
     * @param tid the ID of the transaction requesting to lock the page
     * @param pid the ID of the being locked page
     * @param lockType the type of the lock
     */
    public synchronized void acquireLock(TransactionId tid, PageId pid, LockType lockType)
        throws TransactionAbortedException
    {
        Lock tmpLock;
        if (!pidLockTable.keySet().contains(pid))
        {
            tmpLock = new Lock(lockType, pid);
            pidLockTable.put(pid, tmpLock);
        }
        else
            tmpLock = pidLockTable.get(pid);

        if (!tidLockTable.keySet().contains(tid))
        {
            HashSet<Lock> newLockSet = new HashSet<Lock>();
            newLockSet.add(tmpLock);
            tidLockTable.put(tid, newLockSet);
        }
        else
            tidLockTable.get(tid).add(tmpLock);

        long startTime = System.currentTimeMillis();

        if (lockType == LockType.SHARED)
        {
            while (!tmpLock.getExclusiveTidSet().isEmpty())
            {
                if (tmpLock.getExclusiveTidSet().contains(tid))
                {
                    tmpLock.getDowngradeTagSet().add(tid);
                    tmpLock.setLockType(LockType.SHARED);
                    break;
                }
                long timeLimit = this.BASE_TIMELIMIT + this.RANDOM_GENERATOR.nextInt(this.VAR_TIMELIMIT);
                if (System.currentTimeMillis() - startTime > timeLimit)
                {
                    throw new TransactionAbortedException();
                }
                try
                {
                    wait(WAIT_PERIOD);
                }
                catch (Exception e)
                {
                    throw new TransactionAbortedException();
                }
            }
            tmpLock.getSharedTidSet().add(tid);
            this.pidLockTable.put(pid, tmpLock);
        }
        else
        {
            while (!tmpLock.getSharedTidSet().isEmpty() || !tmpLock.getExclusiveTidSet().isEmpty())
            {
                if (tmpLock.getExclusiveTidSet().contains(tid))
                    break;
                else if (tmpLock.getExclusiveTidSet().isEmpty())
                {
                    // only one shared tid --- it's itself!
                    if (tmpLock.getSharedTidSet().contains(tid) && tmpLock.getSharedTidSet().size() == 1)
                    {
                        tmpLock.getUpgradeTagSet().add(tid);
                        break;
                    }
                }
                long timeLimit = this.BASE_TIMELIMIT + this.RANDOM_GENERATOR.nextInt(this.VAR_TIMELIMIT);
                if (System.currentTimeMillis() - startTime > timeLimit)
                {
                    throw new TransactionAbortedException();
                }
                try
                {
                    wait(WAIT_PERIOD);
                }
                catch (Exception e)
                {
                    throw new TransactionAbortedException();
                }
            }
            tmpLock.getExclusiveTidSet().add(tid);
            tmpLock.setLockType(LockType.EXCLUSIVE);
            this.pidLockTable.put(pid, tmpLock);
        }
    }

    /**
     * Unlock the page with pid and the transaction that launch the unlock is tid.
     *<p>
     * ThreadSafe, the method is synchronized
     * @param tid the ID of the transaction requesting to unlock the page
     * @param pid the ID of the locked page
     */
    public synchronized void releaseLock(TransactionId tid, PageId pid)
    {
        if (this.tidLockTable.keySet().contains(tid) && pidLockTable.keySet().contains(pid))
        {
            Lock targetLock = pidLockTable.get(pid);
            if (targetLock.getExclusiveTidSet().contains(tid))
            {
                targetLock.getExclusiveTidSet().remove(tid);
                targetLock.getSharedTidSet().remove(tid);
                this.tidLockTable.get(tid).remove(targetLock);
                this.pidLockTable.remove(pid);

            }
            else if (targetLock.getSharedTidSet().contains(tid))
            {
                targetLock.getSharedTidSet().remove(tid);
                if (targetLock.getSharedTidSet().isEmpty() && targetLock.getExclusiveTidSet().isEmpty())
                {
                    this.tidLockTable.get(tid).remove(targetLock);
                    this.pidLockTable.remove(pid);
                }
            }
        }
        notifyAll();
    }

    /**
     * Unlock all the pages which are locked by transaction tid.
     *<p>
     * ThreadSafe, the method is synchronized
     * @param tid the ID of the transaction requesting to unlock the page
     */
    public synchronized void releaseAllLocks(TransactionId tid)
    {
        if (this.tidLockTable.keySet().contains(tid))
        {
            // remove exclusive locked pids
            Set<PageId> exclusiveLockedPids = this.getExclusiveLockedPids(tid);
            for (PageId pid : exclusiveLockedPids)
            {
                this.releaseLock(tid, pid);
            }

            // remove shared locked pids
            Set<PageId> sharedLockedPids = this.getSharedLockedPids(tid);
            for (PageId pid : sharedLockedPids)
            {
                this.releaseLock(tid, pid);
            }

            // remove tid index
            this.tidLockTable.remove(tid);
        }
        notifyAll();
    }

    /**
     * Get the set the id of pages which a transaction tid has locked using exclusive lock.
     *<p>
     * ThreadSafe, the method is synchronized
     * @param tid the ID of the transaction
     */
    synchronized public Set<PageId> getExclusiveLockedPids(TransactionId tid)
    {
        Set<PageId> resultSet = new HashSet<PageId>();
        if (this.tidLockTable.keySet().contains(tid))
        {
            for (Lock lock : this.tidLockTable.get(tid))
            {
                if (lock.getExclusiveTidSet().contains(tid) || lock.getDowngradeTagSet().contains(tid))
                    resultSet.add(lock.getLockedPageId());
            }
        }
        return resultSet;
    }

    /**
     * Get the set the id of pages which a transaction tid has locked using exclusive lock.
     *<p>
     * ThreadSafe, the method is synchronized
     * @param tid the ID of the transaction
     */
    synchronized public Set<PageId> getSharedLockedPids(TransactionId tid)
    {
        Set<PageId> resultSet = new HashSet<PageId>();
        if (this.tidLockTable.keySet().contains(tid))
        {
            for (Lock lock : this.tidLockTable.get(tid))
            {
                if (lock.getSharedTidSet().contains(tid) || lock.getUpgradeTagSet().contains(tid))
                    resultSet.add(lock.getLockedPageId());
            }
        }
        return resultSet;
    }

    /**
     * Check whether a transaction with tid holds a lock
     *<p>
     * ThreadSafe, the method is synchronized
     * @param tid the ID of the transaction
     */
    synchronized public boolean holdsLock(TransactionId tid)
    {
        return this.tidLockTable.keySet().contains(tid);
    }

}
