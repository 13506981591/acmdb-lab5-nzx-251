package simpledb;

import java.io.*;
import java.util.*;

/**
 * HeapFile is an implementation of a DbFile that stores a collection of tuples
 * in no particular order. Tuples are stored on pages, each of which is a fixed
 * size, and the file is simply a collection of those pages. HeapFile works
 * closely with HeapPage. The format of HeapPages is described in the HeapPage
 * constructor.
 * 
 * @see simpledb.HeapPage#HeapPage
 * @author Sam Madden
 */
public class HeapFile implements DbFile {

    /**
     * Constructs a heap file backed by the specified file.
     * 
     * @param f
     *            the file that stores the on-disk backing store for this heap
     *            file.
     */
    private final File f;
    private final TupleDesc td;
    public HeapFile(File f, TupleDesc td) {
        // some code goes here
        this.f = f;
        this.td = td;

    }
    /* 
    //  * Db file iterator
     */
    private static final class HeapFileIterator implements DbFileIterator {
        private final HeapFile heapFile;
       /*
         param tid  ==>the ID of the transaction requesting the page
       */
        private final TransactionId tid;

        
        private Iterator<Tuple> tupleIterator;
        private int index;

        public HeapFileIterator(HeapFile file,TransactionId tid){
            this.heapFile = file;
            this.tid = tid;
        }
        @Override
        public void open() throws DbException, TransactionAbortedException {
            //index recorde the "read position" of heap_file
            index = 0;
            //when open  only read one page!==>in case of mmemory out!
            tupleIterator = get_TupleIterator(index);
        }
        private Iterator<Tuple> get_TupleIterator(int pageNumber) throws TransactionAbortedException, DbException
        {
            if(pageNumber >= 0 && pageNumber < heapFile.numPages())
            {
                HeapPageId pid = new HeapPageId(heapFile.getId(),pageNumber);
                //getPage has implement the function -- check whether the page is in bufferbool
                HeapPage page = (HeapPage)Database.getBufferPool().getPage(tid, pid, Permissions.READ_ONLY);
                return page.iterator();
            }
            else
            {
                throw new DbException(String.format("page number %d not exists in heap file %d", pageNumber,heapFile.getId()));
            }
        }

        @Override
        public boolean hasNext() throws DbException, TransactionAbortedException
         {
            if(this.tupleIterator==null)
            {
                return false;
            }
            //if the current page tuple interator is not empty==>return ture
            if(this.tupleIterator.hasNext())
            {
                return true;
            }
            //to search next page
            while (!tupleIterator.hasNext() && index <this.heapFile.numPages() - 1){
                index++;
                PageId pageId = new HeapPageId(this.heapFile.getId(), index);
                tupleIterator = ((HeapPage) Database.getBufferPool().getPage(tid, pageId, Permissions.READ_ONLY)).iterator();
            }

            return tupleIterator.hasNext();
            
        }

        @Override
        public Tuple next() throws DbException, TransactionAbortedException, NoSuchElementException
         {
            if(tupleIterator == null || !tupleIterator.hasNext())
            {
                throw new NoSuchElementException("there is not next tuple in the iterator");
            }
            return tupleIterator.next();
        }

        @Override
        public void rewind() throws DbException, TransactionAbortedException
         {
            close();
            open();
        }

        @Override
        public void close() 
        {
            this.tupleIterator = null;
        }

    }
                        


    /**
     * Returns the File backing this HeapFile on disk.
     * 
     * @return the File backing this HeapFile on disk.
     */
    public File getFile() {
        // some code goes here
        return this.f;
    }

    /**
     * Returns an ID uniquely identifying this HeapFile. Implementation note:
     * you will need to generate this tableid somewhere ensure that each
     * HeapFile has a "unique id," and that you always return the same value for
     * a particular HeapFile. We suggest hashing the absolute file name of the
     * file underlying the heapfile, i.e. f.getAbsoluteFile().hashCode().
     * 
     * @return an ID uniquely identifying this HeapFile.
     */
    public int getId() {
        // some code goes here
        // throw new UnsupportedOperationException("implement this");
        return f.getAbsolutePath().hashCode();
    }

    /**
     * Returns the TupleDesc of the table stord in this DbFile.
     * 
     * @return TupleDesc of this DbFile.
     */
    public TupleDesc getTupleDesc() {
        // some code goes here
       // throw new UnsupportedOperationException("implement this");
       return this.td;
    }

    // see DbFile.java for javadocs
    public Page readPage(PageId pid)
     {
        // some code goes here
        int tableId = pid.getTableId();
        int pgNo = pid.pageNumber();
        int offset = pgNo * BufferPool.getPageSize();
        // for random acess 
        RandomAccessFile randomAccessFile = null;


        try{
            randomAccessFile = new RandomAccessFile(f,"r");
            // the size of  file must be larger of equal the size pgNo * szie of per buffer pool page size
            if((long) (pgNo + 1) *BufferPool.getPageSize() > randomAccessFile.length())
            {
                randomAccessFile.close();
                throw new IllegalArgumentException(String.format("the pgNo %d of  Table(%d) exceeds the length of fill",  pgNo,tableId));
            }

            byte[] bytes = new byte[BufferPool.getPageSize()];
            randomAccessFile.seek(offset);
            int read = randomAccessFile.read(bytes,0,BufferPool.getPageSize());
            //memory out
            if(read != BufferPool.getPageSize())
            {
                throw new IllegalArgumentException(String.format("The page(Table %d Page %d) has not been filled fully",tableId,pgNo));
            }
            HeapPageId id = new HeapPageId(tableId,pgNo);
            return new HeapPage(id,bytes);
        }
        catch (IOException e)
        {
            e.printStackTrace();
        }
        // whetherever there is errors, we should close the file !
        finally 
        {
            try
            {
                if(randomAccessFile != null){
                    randomAccessFile.close();
                }
            }
            catch (Exception e)
            {
                e.printStackTrace();
            }
        }

        throw new IllegalArgumentException(String.format("The page(Table %d Page %d) still remains  some errors!", tableId, pgNo));
    }



    // see DbFile.java for javadocs
    public void writePage(Page page) throws IOException {
        // some code goes here
        // not necessary for lab1
        PageId pid = page.getId();
        int length = BufferPool.getPageSize();
        int offset = pid.pageNumber() * length;

        RandomAccessFile randomAccessFile;
        try {
            randomAccessFile = new RandomAccessFile(f, "rw");
            randomAccessFile.seek(offset);
            randomAccessFile.write(page.getPageData());
        } catch (IOException e) {
            throw new IllegalArgumentException();
        }
    }

    /**
     * Returns the number of pages in this HeapFile.
     */
    public int numPages() {
        // some code goes here
        //because the buffer pool will be fully used!
        //thus the tuples whose size less then  buffer_pool_size will not be pushed intot the buffer pool==>floor
        return (int) Math.floor((double)getFile().length()/BufferPool.getPageSize());
    }

    // see DbFile.java for javadocs
    public ArrayList<Page> insertTuple(TransactionId tid, Tuple t)
            throws DbException, IOException, TransactionAbortedException {
        // some code goes here
        // not necessary for lab1
        ArrayList <Page> affectPages = new ArrayList<>();
        for (int i = 0; i < numPages(); i++){
            HeapPageId pid = new HeapPageId(getId(), i);
            HeapPage page = (HeapPage) Database.getBufferPool().getPage(tid, pid, Permissions.READ_WRITE);
            if (page.getNumEmptySlots() != 0){
                page.insertTuple(t);
                affectPages.add(page);
                return affectPages;
            }
        }

        HeapPageId pid = new HeapPageId(getId(), numPages());
        HeapPage page = new HeapPage(pid, HeapPage.createEmptyPageData());
        page.insertTuple(t);
        writePage(page);
        affectPages.add(page);
        return affectPages;
    }

    // see DbFile.java for javadocs
    public ArrayList<Page> deleteTuple(TransactionId tid, Tuple t) throws DbException,
            TransactionAbortedException {
        // some code goes here
        // not necessary for lab1
        ArrayList <Page> affectPages = new ArrayList<>();
        PageId pid = t.getRecordId().getPageId();
        HeapPage page = (HeapPage) Database.getBufferPool().getPage(tid, pid, Permissions.READ_WRITE);
        page.deleteTuple(t);
        affectPages.add(page);
        return affectPages;
  
    }

    // see DbFile.java for javadocs





public DbFileIterator iterator(TransactionId tid) {
    // some code goes here
    return new HeapFileIterator(this,tid);
}






}

