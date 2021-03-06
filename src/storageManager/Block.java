package storageManager;

import java.io.Serializable;
import java.util.ArrayList;

/* A disk or memory block contains a number of records/tuples that 
 * belong to the same relation. 
 * A tuple CANNOT be split and stored in more than one blocks.
 * Each block is defined to hold as most FIELDS_PER_BLOCK fields. 
 * Therefore, the max number of tuples held in a block can be 
 *   calculated from the size of a tuple, 
 *   which is the number of fields in a tuple.
 * You can get the number by calling Schema::getTuplesPerBlock().
 *
 * The max number of tuples held in a block = 
 *       FIELDS_PER_BLOCK / num_of_fields_in_tuple
 *
 * Usage: Blocks already reside in the memory and disk. 
 *          You don't need to create blocks manually.
 *        Most time when you need to use the Block class is 
 *          to access a block of the main memory
 *        and to get or modify the tuples in the memory block.
 *        First be sure to get a pointer to a memory block 
 *          from the Memory class.
 */

public class Block implements Serializable {
    private ArrayList<Tuple> tuples;

    // for internal use only: you DO NOT need this constructor.
    // Use the blocks in Memory or Relation directly
    protected Block() {
        tuples = new ArrayList<>();
    }

    protected Block(Block b) {
        tuples = new ArrayList<>();
        for (Tuple tuple : b.tuples) {
            tuples.add(new Tuple(tuple));
        }
    }

    public boolean isFull() {
        return !tuples.isEmpty() && tuples.size() == tuples.get(0).getTuplesPerBlock();
    }

    public boolean isEmpty() {
        return tuples.isEmpty();
    }

    // empty the block
    public void clear() {
        tuples.clear();
    }

    // returns current number of tuples inside this block
    public int getNumTuples() {
        int count = 0;
        for (Tuple tuple : tuples) {
            if (!tuple.isNull()) {
                count++;
            }
        }
        return count;
    }

    // gets the tuple at tuple_index;
    // returns empty Tuple if tuple_index out of bound
    public Tuple getTuple(int tuple_offset) {
        if (!tuples.isEmpty() && tuple_offset >= tuples.get(0).getTuplesPerBlock()) {
            System.err.print("getTuple ERROR: tuple offset " + tuple_offset + " out of bound of the block\n");
            return new Tuple();
        }
        if (tuple_offset < 0 || tuple_offset >= tuples.size()) {
            System.err.print("getTuple ERROR: tuple offset " + tuple_offset + " out of bound\n");
            return new Tuple();
        }
        return new Tuple(tuples.get(tuple_offset));
    }

    // returns all the tuples inside this block
    public ArrayList<Tuple> getTuples() {
        // return (ArrayList<Tuple>) DeepCopy.copy(tuples);
        ArrayList<Tuple> tuples = new ArrayList<>(this.tuples.size());
        for (Tuple tuple : this.tuples) {
            tuples.add(new Tuple(tuple));
        }
        return tuples;
    }

    // sets new tuple value at tuple_index;
    // returns false if tuple_index out of bound
    public boolean setTuple(int tuple_offset, Tuple tuple) {
        Schema s = tuple.getSchema();
        if (!tuples.isEmpty()) {
            if (tuple_offset >= tuples.get(0).getTuplesPerBlock()) {
                System.err.print("setTuple ERROR: tuple offset " + tuple_offset + " out of bound of the block\n");
                return false;
            }
            for (Tuple t : tuples) {
                if (!s.equals(t.getSchema())) {
                    System.err.print("setTuple ERROR: tuples' schemas " + "do not match\n");
                    return false;
                }
            }
        }
        if (tuple_offset < 0 || tuple_offset >= s.getTuplesPerBlock()) {
            System.err.print("setTuple ERROR: tuple offset " + tuple_offset + " out of bound\n");
            return false;
        }
        if (tuple_offset >= tuples.size()) {
            // If there is a gap before the offset,
            // filled it with invalid tuples
            Tuple t = new Tuple(tuple.schemaManager, tuple.schemaIndex);
            t.invalidate();
            for (int i = tuples.size(); i < tuple_offset; i++) {
                tuples.add(t);
            }
            tuples.add(new Tuple(tuple));
        } else {
            tuples.set(tuple_offset, new Tuple(tuple));
        }
        return true;
    }

    // remove all the tuples; sets new tuples for the block;
    // returns false if number of input tuples exceeds the space limit
    public boolean setTuples(ArrayList<Tuple> tuples) {
        if (tuples.size() > tuples.get(0).getTuplesPerBlock()) {
            System.err.print("setTuples ERROR: number of tuples " + "exceed space limit of the block\n");
            return false;
        }
        // this.tuples=(ArrayList<Tuple>) DeepCopy.copy(tuples);
        this.tuples.clear();
        for (Tuple tuple : tuples) {
            this.tuples.add(tuple);
        }
        return true;
    }

    // remove all the tuples; sets new tuples for the block;
    // returns false if number of input tuples exceeds the space limit
    public boolean setTuples(ArrayList<Tuple> tuples, int start_index, int end_index) {
        if ((end_index - start_index) > tuples.get(0).getTuplesPerBlock()) {
            System.err.print("setTuples ERROR: number of tuples " + "exceed space limit of the block\n");
            return false;
        }
        this.tuples.clear();
        for (int i = start_index; i < end_index; i++) {
            this.tuples.add(new Tuple(tuples.get(i)));
        }
        return true;
    }

    // appends one tuple to the end of the block;
    // returns false if total number of tuples exceeds the space limit
    public boolean appendTuple(Tuple tuple) {
        if (isFull()) {
            System.err.print("appendTuple ERROR: the block is full\n");
            return false;
        }
        this.tuples.add(new Tuple(tuple));
        return true;
    }

    // invalidates the tuple at the offset
    public boolean invalidateTuple(int tuple_offset) {
        if (tuple_offset < 0 || tuple_offset >= tuples.size()) {
            System.err.print("nullTuple ERROR: tuple offset " + tuple_offset + " out of bound\n");
            return false;
        }
        tuples.get(tuple_offset).invalidate();
        return true;
    }

    // empty all the tuples in the block
    public boolean invalidateTuples() {
        for (Tuple tuple : tuples) {
            tuple.invalidate();
        }
        return true;
    }

    public String toString() {
        StringBuilder sb = new StringBuilder();
        if (!tuples.isEmpty()) {
            for (Tuple tuple : tuples) {
                if (tuple.isNull()) {
                    sb.append("(hole)");
                } else {
                    sb.append(tuple.toString());
                }
                sb.append("\n");
            }
        }
        return sb.toString();
    }
}
