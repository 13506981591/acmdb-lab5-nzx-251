package simpledb;

import java.io.*;

import java.util.*;
import java.util.concurrent.ConcurrentHashMap;

/**
 * BufferPool manages the reading and writing of pages into memory from
 * disk. Access methods call into it to retrieve pages, and it fetches
 * pages from the appropriate location.
 * <p>
 * The BufferPool is also responsible for locking;  when a transaction fetches
 * a page, BufferPool checks that the transaction has the appropriate
 * locks to read/write the page.
 * 
 * @Threadsafe, all fields are final
 */
public class BufferPool {
    /** Bytes per page, including header. */
    private static final int PAGE_SIZE = 4096;

    private static int pageSize = PAGE_SIZE;
    
    /** Default number of pages passed to the constructor. This is used by
    other classes. BufferPool should use the numPages argument to the
    constructor instead. */
    public static final int DEFAULT_PAGES = 50;

    
    private  final int numPages;
    private final Page[] pageArray;
    private final HashMap<PageId, Integer> pageIndexHashMap;
    private final LinkedList<PageId> LRUList;
    private final BitSet readyBitSet;
    private final LockManager LOCK_MANAGER = new LockManager();


    /**
     * Creates a BufferPool that caches up to numPages pages.
     *
     * @param numPages maximum number of pages in this buffer pool.
     */
    public BufferPool(int numPages) {
        // some code goes here
        this.numPages = numPages;
        this.pageArray = new Page[numPages];
        this.pageIndexHashMap = new HashMap<PageId, Integer>();
        this.LRUList = new LinkedList<PageId>();
        this.readyBitSet = new BitSet(numPages);

    }
    
    public static int getPageSize() {
      return pageSize;
    }

    // THIS FUNCTION SHOULD ONLY BE USED FOR TESTING!!
    public static void setPageSize(int pageSize) {
    	BufferPool.pageSize = pageSize;
    }
    
    // THIS FUNCTION SHOULD ONLY BE USED FOR TESTING!!
    public static void resetPageSize() {
    	BufferPool.pageSize = PAGE_SIZE;
    }

    /**
     * Retrieve the specified page with the associated permissions.
     * Will acquire a lock and may block if that lock is held by another
     * transaction.
     * <p>
     * The retrieved page should be looked up in the buffer pool.  If it
     * is present, it should be returned.  If it is not present, it should
     * be added to the buffer pool and returned.  If there is insufficient
     * space in the buffer pool, an page should be evicted and the new page
     * should be added in its place.
     *
     * Notice:
     *
     * @param tid the ID of the transaction requesting the page
     * @param pid the ID of the requested page
     * @param perm the requested permissions on the page
     */
    public Page getPage(TransactionId tid, PageId pid, Permissions perm)
        throws TransactionAbortedException, DbException {
        // some code goes here
        if (perm == Permissions.READ_ONLY)
        {
            LOCK_MANAGER.acquireLock(tid, pid, LockType.SHARED);
        }
        else if (perm == Permissions.READ_WRITE)
        {
            LOCK_MANAGER.acquireLock(tid, pid, LockType.EXCLUSIVE);
        }
        else
        {
            throw new DbException("Permission error in BufferPool getPage()");
        }

        synchronized (this)
        {
            Page targetPage;
            if (!this.pageIndexHashMap.containsKey(pid))
            {
                targetPage = Database.getCatalog().getDatabaseFile(pid.getTableId()).readPage(pid);
                int index = readyBitSet.nextClearBit(0);
                while (index >= this.numPages)
                {
                    this.evictPage();
                    index = readyBitSet.nextClearBit(0);
                }
                this.pageIndexHashMap.put(pid, index);
                this.LRUList.addLast(pid);
                this.readyBitSet.set(index, true);
                this.pageArray[index] = targetPage;
            }
            else
            {
                int pageArrayId = this.pageIndexHashMap.get(pid);
                this.LRUList.remove(pid);
                this.LRUList.addLast(pid); // move pid to the end of the LRU list
                targetPage = this.pageArray[pageArrayId];
            }
            return targetPage;
        }

    }

    /**
     * Releases the lock on a page.
     * Calling this is very risky, and may result in wrong behavior. Think hard
     * about who needs to call this and why, and why they can run the risk of
     * calling it.
     *
     * @param tid the ID of the transaction requesting the unlock
     * @param pid the ID of the page to unlock
     */
    public void releasePage(TransactionId tid, PageId pid) {
        // some code goes here
        // not necessary for lab1|lab2
        LOCK_MANAGER.releaseLock(tid, pid);
    }

    /**
     * Release all locks associated with a given transaction.
     *
     * @param tid the ID of the transaction requesting the unlock
     */
    public void transactionComplete(TransactionId tid) throws IOException {
        // some code goes here
        // not necessary for lab1|lab2
        this.transactionComplete(tid, true);
    }

    /** Return true if the specified transaction has a lock on the specified page */
    public boolean holdsLock(TransactionId tid, PageId p) {
        // some code goes here
        // not necessary for lab1|lab2
        return LOCK_MANAGER.holdsLock(tid);
    }

    /**
     * Commit or abort a given transaction; release all locks associated to
     * the transaction.
     *
     * @param tid the ID of the transaction requesting the unlock
     * @param commit a flag indicating whether we should commit or abort
     */
    public synchronized void transactionComplete(TransactionId tid, boolean commit)
        throws IOException {
        // some code goes here
        // not necessary for lab1|lab2
        if (commit)
        {
            this.flushPages(tid);
        }
        else
        {
       
            if (this.LOCK_MANAGER.holdsLock(tid))
            {
                Set<PageId> modifiedPage = this.LOCK_MANAGER.getExclusiveLockedPids(tid);
                for (PageId pid : modifiedPage)
                {
                    this.discardPage(pid);
                }
            }
        }

        if (!this.LOCK_MANAGER.holdsLock(tid))
        {
            notifyAll();
            return;
        }

        this.LOCK_MANAGER.releaseAllLocks(tid);
    }

    /**
     * Add a tuple to the specified table on behalf of transaction tid.  Will
     * acquire a write lock on the page the tuple is added to and any other 
     * pages that are updated (Lock acquisition is not needed for lab2). 
     * May block if the lock(s) cannot be acquired.
     * 
     * Marks any pages that were dirtied by the operation as dirty by calling
     * their markDirty bit, and adds versions of any pages that have 
     * been dirtied to the cache (replacing any existing versions of those pages) so 
     * that future requests see up-to-date pages. 
     *
     * @param tid the transaction adding the tuple
     * @param tableId the table to add the tuple to
     * @param t the tuple to add
     */
    public void insertTuple(TransactionId tid, int tableId, Tuple t)
        throws DbException, IOException, TransactionAbortedException {
        // some code goes here
        // not necessary for lab1
        DbFile file = Database.getCatalog().getDatabaseFile(tableId);
        ArrayList<Page> dirtyPageList = file.insertTuple(tid, t);

       
        synchronized (this)
        {

            for (Page dirtyPage : dirtyPageList)
            {
                PageId pid = dirtyPage.getId();
                if (!this.pageIndexHashMap.containsKey(pid))
                {
                    Page targetPage;
                    targetPage = dirtyPage;
                    int index = readyBitSet.nextClearBit(0);
                    while (index >= this.numPages)
                    {
                        this.evictPage();
                        index = readyBitSet.nextClearBit(0);
                    }
                    this.pageIndexHashMap.put(pid, index);
                    this.LRUList.addLast(pid);
                    this.readyBitSet.set(index, true);
                    this.pageArray[index] = targetPage;
                }
                else
                {
                    int pageArrayId = this.pageIndexHashMap.get(pid);
                    this.LRUList.remove(pid);
                    this.LRUList.addLast(pid);
                    this.pageArray[pageArrayId] = dirtyPage;
                }
                dirtyPage.markDirty(true, tid);
            }
        }
    }

    /**
     * Remove the specified tuple from the buffer pool.
     * Will acquire a write lock on the page the tuple is removed from and any
     * other pages that are updated. May block if the lock(s) cannot be acquired.
     *
     * Marks any pages that were dirtied by the operation as dirty by calling
     * their markDirty bit, and adds versions of any pages that have 
     * been dirtied to the cache (replacing any existing versions of those pages) so 
     * that future requests see up-to-date pages. 
     *
     * @param tid the transaction deleting the tuple.
     * @param t the tuple to delete
     */
    public void deleteTuple(TransactionId tid, Tuple t)
        throws DbException, IOException, TransactionAbortedException {
        // some code goes here
        // not necessary for lab1
        DbFile file = Database.getCatalog().getDatabaseFile(t.getRecordId().getPageId().getTableId());
        ArrayList<Page> dirtyPageList = file.deleteTuple(tid, t);
        synchronized (this)
        {
            for (Page dirtyPage : dirtyPageList)
            {
                PageId pid = dirtyPage.getId();
                if (!this.pageIndexHashMap.containsKey(pid))
                {
                    Page targetPage;
                    targetPage = dirtyPage;
                    int index = readyBitSet.nextClearBit(0);
                    while (index >= this.numPages)
                    {
                        this.evictPage();
                        index = readyBitSet.nextClearBit(0);
                    }
                    this.pageIndexHashMap.put(pid, index);
                    this.LRUList.addLast(pid);
                    this.readyBitSet.set(index, true);
                    this.pageArray[index] = targetPage;
                }
                else
                {
                    int pageArrayId = this.pageIndexHashMap.get(pid);
                    this.LRUList.remove(pid);
                    this.LRUList.addLast(pid); 
                    this.pageArray[pageArrayId] = dirtyPage;
                }
                dirtyPage.markDirty(true, tid);
            }
        }
    }

    /**
     * Flush all dirty pages to disk.
     * NB: Be careful using this routine -- it writes dirty data to disk so will
     *     break simpledb if running in NO STEAL mode.
     */
    public synchronized void flushAllPages() throws IOException {
        // some code goes here
        // not necessary for lab1
        for (PageId pid : pageIndexHashMap.keySet())
        {
            flushPage(pid);
        }
    }

    /** Remove the specific page id from the buffer pool.
        Needed by the recovery manager to ensure that the
        buffer pool doesn't keep a rolled back page in its
        cache.
        
        Also used by B+ tree files to ensure that deleted pages
        are removed from the cache so they can be reused safely
    */
    public synchronized void discardPage(PageId pid) {
        // some code goes here
        // not necessary for lab1
        if (pageIndexHashMap.containsKey(pid))
        {
//            this.dirtyBitSet.set(pageIndexHashMap.get(pid), false);
            this.readyBitSet.set(pageIndexHashMap.get(pid), false);
            this.LRUList.remove(pid);
            this.pageIndexHashMap.remove(pid);
        }
    }

    /**
     * Flushes a certain page to disk
     * @param pid an ID indicating the page to flush
     */
    private synchronized void flushPage(PageId pid) throws IOException {
        // some code goes here
        // not necessary for lab1
        if (this.pageIndexHashMap.containsKey(pid))
        {
            int pageArrayIndex = pageIndexHashMap.get(pid);
            Database.getCatalog().getDatabaseFile(pid.getTableId()).writePage(pageArray[pageArrayIndex]);
        }
    }

    /** Write all pages of the specified transaction to disk.
     */
    public synchronized  void flushPages(TransactionId tid) throws IOException {
        // some code goes here
        // not necessary for lab1|lab2
        if (this.LOCK_MANAGER.holdsLock(tid))
        {
            Set<PageId> flushSet = this.LOCK_MANAGER.getExclusiveLockedPids(tid);
            for (PageId pid : flushSet)
            {
                this.flushPage(pid);
            }
        }
    }

    /**
     * Discards a page from the buffer pool.
     * Flushes the page to disk to ensure dirty pages are updated on disk.
     */
    private synchronized void evictPage() throws DbException {
        // some code goes here
        // not necessary for lab1
        Iterator<PageId> iter = LRUList.iterator();
        PageId evictPid = null;
        while (iter.hasNext())
        {
            PageId nextPageId = iter.next();
            int index = pageIndexHashMap.get(nextPageId);
            Page page = this.pageArray[index];
            if (page.isDirty() == null)
            {
                evictPid = nextPageId;
                break;
            }
        }
        if (evictPid == null)
        {
            throw new DbException("All pages in BufferPool are dirty, cannot evict\n");
        }

        int pageArrayId = pageIndexHashMap.get(evictPid);
        try
        {
            flushPage(evictPid);
        }catch (IOException e)
        {
            e.printStackTrace();
            System.err.println("IOException happens in evictPage()\n");
        }
        this.readyBitSet.set(pageArrayId, false);
        this.pageIndexHashMap.remove(evictPid);
        this.LRUList.remove(evictPid);

    }

}
