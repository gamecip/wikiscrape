package wikiscrape;

import java.util.Arrays;
import java.util.Collections;
import java.util.Iterator;
import java.util.List;

/**
 * Specialized {@link Iterator} class that emits {@link List<T>} instances of a specified size.
 * <p>
 * If the underlying {@link List}'s length is not an integer multiple of the batch size, it will
 * emit a shorter list during the final iteration.
 * 
 * @author Malcolm Riley
 * @param <T>
 */
public class BatchIterator<T> implements Iterator<List<T>>, Iterable<List<T>>{
	
	private List<T> LIST;
	private final int BATCH_SIZE;
	private int INDEX = 0;
	
	/**
	 * Constructs a new {@link BatchIterator} instance using the passed batch size.
	 * <p>
	 * {@link List} instances obtained from this {@link BatchIterator} instance will be of that size.
	 * 
	 * @param passedGenerationList - The list to back this {@link BatchIterator} with
	 * @param passedBatchSize - The batch size to use.
	 * @throws IllegalArgumentException - If {@code passedBatchSize} is less than or equal to zero.
	 */
	public BatchIterator(List<T> passedGenerationList, int passedBatchSize) {
		if (passedBatchSize <= 0) {
			throw new IllegalArgumentException("Batch size must be greater than zero");
		}
		this.LIST = Collections.unmodifiableList(passedGenerationList);
		this.BATCH_SIZE = passedBatchSize;
	}
	
	/**
	 * Constructs a new {@link BatchIterator} instance using the passed batch size.
	 * <p>
	 * {@link List} instances obtained from this {@link BatchIterator} instance will be of that size.
	 * 
	 * @param passedGenerationList - The array to back this {@link BatchIterator} with
	 * @param passedBatchSize - The batch size to use.
	 * @throws IllegalArgumentException - If {@code passedBatchSize} is less than or equal to zero.
	 */
	public BatchIterator(T[] passedGenerationList, int passedBatchSize) {
		this(Arrays.asList(passedGenerationList), passedBatchSize);
	}

	@Override
	public boolean hasNext() {
		return this.INDEX < this.LIST.size();
	}

	@Override
	public List<T> next() {
		List<T> sublist = this.LIST.subList(this.INDEX, Math.min(this.INDEX + this.BATCH_SIZE, this.LIST.size()));
		this.INDEX += sublist.size();
		return sublist;
	}

	@Override
	public Iterator<List<T>> iterator() {
		return this;
	}
	
	/* Supertype Override Method */
	
	@Override
	public void remove() {
        throw new UnsupportedOperationException("May not remove elements from a BatchIterator.");
    }

}