package simpledb;

import java.io.Serializable;

/**
 * A RecordId is a reference to a specific tuple on a specific page of a
 * specific table.
 */
public class RecordId implements Serializable {

    private static final long serialVersionUID = 1L;

    /**
     * Creates a new RecordId referring to the specified PageId and tuple
     * number.
     * 
     * @param pid
     *            the pageid of the page on which the tuple resides
     * @param tupleno
     *            the tuple number within the page.
     */
    private PageId pid;
    private int tupleno;

    public RecordId(PageId pid, int tupleno) {
        // some code goes here
        this.pid = pid;
        this.tupleno = tupleno;
    }

    /**
     * @return the tuple number this RecordId references.
     */
    public int tupleno() {
        // some code goes here
        return this.tupleno;
    }

    /**
     * @return the page id this RecordId references.
     */
    public PageId getPageId() {
        // some code goes here
        return this.pid;
    }

    /**
     * Two RecordId objects are considered equal if they represent the same
     * tuple.
     * 
     * @return True if this and o represent the same tuple
     */
    @Override
    public boolean equals(Object o) {
        // some code goes here
        if (!(o instanceof RecordId))
        {
            return false;
        }
        RecordId record_id = (RecordId) o;
        if (this.pid.equals(record_id.getPageId())&& this.tupleno == record_id.tupleno)
        {
            return true;
        }
        // throw new UnsupportedOperationException("implement this");
        return false;
    }

    /**
     * You should implement the hashCode() so that two equal RecordId instances
     * (with respect to equals()) have the same hashCode().
     * 
     * @return An int that is the same for equal RecordId objects.
     */
    @Override
    public int hashCode() {
        // some code goes here
        // throw new UnsupportedOperationException("implement this");
        //see the HeapPageID hash code==> Integer.toString(this.getTableId()*521+this.pageNumber()*1314).hashCode();
        //this fucntion is just like it! (with tuple no* 15 to add)
        return Integer.toString(this.pid.getTableId()*521+this.pid.pageNumber()*1314+this.tupleno()*15).hashCode();
    }

}
