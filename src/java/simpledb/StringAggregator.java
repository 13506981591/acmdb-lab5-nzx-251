package simpledb;

import java.util.ArrayList;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

/**
 * Knows how to compute some aggregate over a set of StringFields.
 */
public class StringAggregator implements Aggregator {

    private static final long serialVersionUID = 1L;
    private int gbfield;
    private int afield;
    private Type gbfieldtype;
    private Op aggreOp;
    private TupleDesc tupleDesc;
    private Map<Field, Integer> Counts;

    /**
     * Aggregate constructor
     * 
     * @param gbfield     the 0-based index of the group-by field in the tuple, or
     *                    NO_GROUPING if there is no grouping
     * @param gbfieldtype the type of the group by field (e.g., Type.INT_TYPE), or
     *                    null if there is no grouping
     * @param afield      the 0-based index of the aggregate field in the tuple
     * @param what        aggregation operator to use -- only supports COUNT
     * @throws IllegalArgumentException if what != COUNT
     */

    public StringAggregator(int gbfield, Type gbfieldtype, int afield, Op what) {
        // some code goes here
        if (what != Op.COUNT)
            throw new UnsupportedOperationException("String only support COUNT");
        this.gbfield = gbfield;
        this.gbfieldtype = gbfieldtype;
        this.afield = afield;
        this.aggreOp = what;
        Counts = new ConcurrentHashMap<>();
        if (gbfield == Aggregator.NO_GROUPING)
        {
            this.tupleDesc = new TupleDesc(new Type[] { Type.INT_TYPE }, new String[] { "aggregateValue" });
        } 
        else
        {
            this.tupleDesc = new TupleDesc(new Type[] { gbfieldtype, Type.INT_TYPE },
                    new String[] { "groupValue", "aggregateValue" });
        }
    }

    /**
     * Merge a new tuple into the aggregate, grouping as indicated in the
     * constructor
     * 
     * @param tup the Tuple containing an aggregate field and a group-by field
     */
    public void mergeTupleIntoGroup(Tuple tup) {
        // some code goes here
        Field groupfield;
        if (gbfield == Aggregator.NO_GROUPING)
            groupfield = null;
        else
            groupfield = tup.getField(gbfield);

        Integer cnt = Counts.getOrDefault(groupfield, 0);
        Counts.put(groupfield, cnt + 1);
    }

    /**
     * Create a DbIterator over group aggregate results.
     *
     * @return a DbIterator whose tuples are the pair (groupVal,
     *         aggregateVal) if using group, or a single (aggregateVal) if no
     *         grouping. The aggregateVal is determined by the type of
     *         aggregate specified in the constructor.
     */
    public DbIterator iterator() {
        // some code goes here
        // throw new UnsupportedOperationException("please implement me for lab3");
        ArrayList<Tuple> tuples = new ArrayList<>();
        for (Map.Entry<Field, Integer> entry : Counts.entrySet())
        {
            Tuple tuple = new Tuple(tupleDesc);
            Integer value = entry.getValue();

            if (gbfield == Aggregator.NO_GROUPING)
             {
                tuple.setField(0, new IntField(value));
            } 
            else
            {
                tuple.setField(0, entry.getKey());
                tuple.setField(1, new IntField(value));
            }
            tuples.add(tuple);
        }
        return new TupleIterator(tupleDesc, tuples);
    }


}
