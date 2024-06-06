package simpledb;

import java.io.Serializable;
import java.util.Arrays;
import java.util.Iterator;
import java.util.concurrent.CopyOnWriteArrayList;

/**
 * Tuple maintains information about the contents of a tuple. Tuples have a
 * specified schema specified by a TupleDesc object and contain Field objects
 * with the data for each field.
 */
public class Tuple implements Serializable {

    private static final long serialVersionUID = 1L;
    /**
     * Create a new tuple with the specified schema (type).
     *
     * @param td
     *            the schema of this tuple. It must be a valid TupleDesc
     *            instance with at least one field.
     */

    // td record the information  of schama
    TupleDesc td;
    //fields record the field of each tuple
    CopyOnWriteArrayList<Field>fields;
    //the location in disk
    RecordId rid;

    

    public Tuple(TupleDesc td) {
        // some code goes here

        this.td= td;
        this.fields  = new CopyOnWriteArrayList<>();
        this.rid =null;

    }

    /**
     * @return The TupleDesc representing the schema of this tuple.
     */
    public TupleDesc getTupleDesc() {
        // some code goes here
        return this.td;
    }

    /**
     * @return The RecordId representing the location of this tuple on disk. May
     *         be null.
     */
    public RecordId getRecordId() {
        // some code goes here
        return this.rid;
    }

    /**
     * Set the RecordId information for this tuple.
     *
     * @param rid
     *            the new RecordId for this tuple.
     */
    public void setRecordId(RecordId rid) {
        // some code goes here
        this.rid = rid;
    }

    /**
     * Change the value of the ith field of this tuple.
     *
     * @param i
     *            index of the field to change. It must be a valid index.
     * @param f
     *            new value for the field.
     */
    public void setField(int i, Field f) {
        // some code goes here
        /*when i is larger than the size of fields(just more than one), we should add it in the end;
            or just replace it
        */
        
        if (i<fields.size() && i>=0)
        {
            fields.set(i,f);
        }
        else if (i==fields.size() && i>=0) {
            fields.add(f);
        }
        
    }

    /**
     * @return the value of the ith field, or null if it has not been set.
     *
     * @param i
     *            field index to return. Must be a valid index.
     */
    public Field getField(int i) {
        // some code goes here
        /*
         when i is larger than the size of fields or less then 0, we return null;
            or just return by get function
        */
        if (i<0||i>=fields.size()||fields ==null)
        {
            return null;
        }
        else
        {
            return fields.get(i);
        }

        
    }

    /**
     * Returns the contents of this Tuple as a string. Note that to pass the
     * system tests, the format needs to be as follows:
     *
     * column1\tcolumn2\tcolumn3\t...\tcolumnN
     *
     * where \t is any whitespace (except a newline)
     */
    public String toString() {
        // some code goes here
        // throw new UnsupportedOperationException("Implement this");
        StringBuilder tuple_string = new StringBuilder();
        Iterator<TupleDesc.TDItem>  schama_items = this.td.iterator();
        int index = 0;
        while(schama_items.hasNext())
        {
            TupleDesc.TDItem item = schama_items.next();
            tuple_string.append(item.fieldName).append("(");
            tuple_string.append(this.fields.get(index).toString()).append(")");
            tuple_string.append("\t");
            index++;
        }
        return tuple_string.toString();
    }

    /**
     * @return
     *        An iterator which iterates over all the fields of this tuple
     * */
    public Iterator<Field> fields()
    {
        // some code goes here
        return fields.iterator();
    }

    /**
     * reset the TupleDesc of thi tuple
     * */
    public void resetTupleDesc(TupleDesc td)
    {
        
        // some code goes here
        this.td = td;
    }
}
    