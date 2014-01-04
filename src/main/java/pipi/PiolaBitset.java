/*
 * Copyright (c) 1995, 2007, Oracle and/or its affiliates. All rights reserved.
 * ORACLE PROPRIETARY/CONFIDENTIAL. Use is subject to license terms.
 *
 *
 *
 *
 *
 *
 *
 *
 *
 *
 *
 *
 *
 *
 *
 *
 *
 *
 *
 *
 */

package pipi;

import java.io.IOException;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.io.ObjectStreamField;
import java.nio.ByteBuffer;
import java.nio.ByteOrder;
import java.nio.LongBuffer;
import java.util.Arrays;

/**
 * This class implements a vector of bits that grows as needed. Each component
 * of the bit set has a {@code boolean} value. The bits of a {@code BitSet} are
 * indexed by nonnegative integers. Individual indexed bits can be examined,
 * set, or cleared. One {@code BitSet} may be used to modify the contents of
 * another {@code BitSet} through logical AND, logical inclusive OR, and logical
 * exclusive OR operations.
 * 
 * <p>
 * By default, all bits in the set initially have the value {@code false}.
 * 
 * <p>
 * Every bit set has a current size, which is the number of bits of space
 * currently in use by the bit set. Note that the size is related to the
 * implementation of a bit set, so it may change with implementation. The length
 * of a bit set relates to logical length of a bit set and is defined
 * independently of implementation.
 * 
 * <p>
 * Unless otherwise noted, passing a null parameter to any of the methods in a
 * {@code BitSet} will result in a {@code NullPointerException}.
 * 
 * <p>
 * A {@code BitSet} is not safe for multithreaded use without external
 * synchronization.
 * 
 * @author Arthur van Hoff
 * @author Michael McCloskey
 * @author Martin Buchholz
 * @since JDK1.0
 */
public class PiolaBitset implements Cloneable, java.io.Serializable {
	/*
	 * BitSets are packed into arrays of "words." Currently a word is a long,
	 * which consists of 64 bits, requiring 6 address bits. The choice of word
	 * size is determined purely by performance concerns.
	 */
	public final static int ADDRESS_BITS_PER_WORD = 6;
	public final static int BITS_PER_WORD = 1 << ADDRESS_BITS_PER_WORD;
	private final static int BIT_INDEX_MASK = BITS_PER_WORD - 1;

	/* Used to shift left or right for a partial word mask */
	public static final long WORD_MASK = 0xffffffffffffffffL;

	/**
	 * @serialField
	 *                  bits long[]
	 * 
	 *                  The bits in this BitSet. The ith bit is stored in
	 *                  bits[i/64] at bit position i % 64 (where bit position 0
	 *                  refers to the least significant bit and 63 refers to the
	 *                  most significant bit).
	 */
	private static final ObjectStreamField[] serialPersistentFields = { new ObjectStreamField("bits", long[].class), };

	/**
	 * The internal field corresponding to the serialField "bits".
	 */
	public long[] words;

	/**
	 * The number of words in the logical size of this BitSet.
	 */
	/* use serialVersionUID from JDK 1.0.2 for interoperability */
	private static final long serialVersionUID = 7997698588986878753L;
	public int nbits;

	/**
	 * Given a bit index, return word index containing it.
	 */
	public static int wordIndex(int bitIndex) {
		return bitIndex >>> ADDRESS_BITS_PER_WORD;
	}

	// /**
	// * Creates a new bit set. All bits are initially {@code false}.
	// */
	// public PiolaBitset() {
	// initWords(BITS_PER_WORD);
	// sizeIsSticky = false;
	// }

	/**
	 * Creates a bit set whose initial size is large enough to explicitly
	 * represent bits with indices in the range {@code 0} through
	 * {@code nbits-1}. All bits are initially {@code false}.
	 * 
	 * @param nbits
	 *            the initial size of the bit set
	 * @throws NegativeArraySizeException
	 *             if the specified initial size is negative
	 */
	public PiolaBitset(int nbits) {
		this.nbits = nbits;
		int wordsInUse = (nbits - 1 >> ADDRESS_BITS_PER_WORD) + 1;
		// nbits can't be negative; size 0 is OK
		if (nbits < 0)
			throw new NegativeArraySizeException("nbits < 0: " + nbits);

		words = new long[wordsInUse];
	}

	/**
	 * Creates a bit set using words as the internal representation. The last
	 * word (if there is one) must be non-zero.
	 */
	private PiolaBitset(long[] words) {
		this.words = words;
	}

	/**
	 * Returns a new bit set containing all the bits in the given long array.
	 * 
	 * <p>
	 * More precisely, <br>
	 * {@code BitSet.valueOf(longs).get(n) == ((longs[n/64] & (1L<<(n%64))) != 0)}
	 * <br>
	 * for all {@code n < 64 * longs.length}.
	 * 
	 * <p>
	 * This method is equivalent to
	 * {@code BitSet.valueOf(LongBuffer.wrap(longs))}.
	 * 
	 * @param longs
	 *            a long array containing a little-endian representation of a
	 *            sequence of bits to be used as the initial bits of the new bit
	 *            set
	 * @since 1.7
	 */
	public static PiolaBitset valueOf(long[] longs) {
		int n;
		for (n = longs.length; n > 0 && longs[n - 1] == 0; n--)
			;
		return new PiolaBitset(Arrays.copyOf(longs, n));
	}

	/**
	 * Returns a new bit set containing all the bits in the given long buffer
	 * between its position and limit.
	 * 
	 * <p>
	 * More precisely, <br>
	 * {@code BitSet.valueOf(lb).get(n) == ((lb.get(lb.position()+n/64) & (1L<<(n%64))) != 0)}
	 * <br>
	 * for all {@code n < 64 * lb.remaining()}.
	 * 
	 * <p>
	 * The long buffer is not modified by this method, and no reference to the
	 * buffer is retained by the bit set.
	 * 
	 * @param lb
	 *            a long buffer containing a little-endian representation of a
	 *            sequence of bits between its position and limit, to be used as
	 *            the initial bits of the new bit set
	 * @since 1.7
	 */
	public static PiolaBitset valueOf(LongBuffer lb) {
		lb = lb.slice();
		int n;
		for (n = lb.remaining(); n > 0 && lb.get(n - 1) == 0; n--)
			;
		long[] words = new long[n];
		lb.get(words);
		return new PiolaBitset(words);
	}

	/**
	 * Returns a new bit set containing all the bits in the given byte array.
	 * 
	 * <p>
	 * More precisely, <br>
	 * {@code BitSet.valueOf(bytes).get(n) == ((bytes[n/8] & (1<<(n%8))) != 0)}
	 * <br>
	 * for all {@code n <  8 * bytes.length}.
	 * 
	 * <p>
	 * This method is equivalent to
	 * {@code BitSet.valueOf(ByteBuffer.wrap(bytes))}.
	 * 
	 * @param bytes
	 *            a byte array containing a little-endian representation of a
	 *            sequence of bits to be used as the initial bits of the new bit
	 *            set
	 * @since 1.7
	 */
	public static PiolaBitset valueOf(byte[] bytes) {
		return PiolaBitset.valueOf(ByteBuffer.wrap(bytes));
	}

	/**
	 * Returns a new bit set containing all the bits in the given byte buffer
	 * between its position and limit.
	 * 
	 * <p>
	 * More precisely, <br>
	 * {@code BitSet.valueOf(bb).get(n) == ((bb.get(bb.position()+n/8) & (1<<(n%8))) != 0)}
	 * <br>
	 * for all {@code n < 8 * bb.remaining()}.
	 * 
	 * <p>
	 * The byte buffer is not modified by this method, and no reference to the
	 * buffer is retained by the bit set.
	 * 
	 * @param bb
	 *            a byte buffer containing a little-endian representation of a
	 *            sequence of bits between its position and limit, to be used as
	 *            the initial bits of the new bit set
	 * @since 1.7
	 */
	public static PiolaBitset valueOf(ByteBuffer bb) {
		bb = bb.slice().order(ByteOrder.LITTLE_ENDIAN);
		int n;
		for (n = bb.remaining(); n > 0 && bb.get(n - 1) == 0; n--)
			;
		long[] words = new long[(n + 7) / 8];
		bb.limit(n);
		int i = 0;
		while (bb.remaining() >= 8)
			words[i++] = bb.getLong();
		for (int remaining = bb.remaining(), j = 0; j < remaining; j++)
			words[i] |= (bb.get() & 0xffL) << (8 * j);
		return new PiolaBitset(words);
	}

	/**
	 * Returns a new byte array containing all the bits in this bit set.
	 * 
	 * <p>
	 * More precisely, if <br>
	 * {@code byte[] bytes = s.toByteArray();} <br>
	 * then {@code bytes.length == (s.length()+7)/8} and <br>
	 * {@code s.get(n) == ((bytes[n/8] & (1<<(n%8))) != 0)} <br>
	 * for all {@code n < 8 * bytes.length}.
	 * 
	 * @return a byte array containing a little-endian representation of all the
	 *         bits in this bit set
	 * @since 1.7
	 */
	public byte[] toByteArray() {
		int n = this.words.length;
		if (n == 0)
			return new byte[0];
		int len = 8 * (n - 1);
		for (long x = words[n - 1]; x != 0; x >>>= 8)
			len++;
		byte[] bytes = new byte[len];
		ByteBuffer bb = ByteBuffer.wrap(bytes).order(ByteOrder.LITTLE_ENDIAN);
		for (int i = 0; i < n - 1; i++)
			bb.putLong(words[i]);
		for (long x = words[n - 1]; x != 0; x >>>= 8)
			bb.put((byte) (x & 0xff));
		return bytes;
	}

	/**
	 * Returns a new long array containing all the bits in this bit set.
	 * 
	 * <p>
	 * More precisely, if <br>
	 * {@code long[] longs = s.toLongArray();} <br>
	 * then {@code longs.length == (s.length()+63)/64} and <br>
	 * {@code s.get(n) == ((longs[n/64] & (1L<<(n%64))) != 0)} <br>
	 * for all {@code n < 64 * longs.length}.
	 * 
	 * @return a long array containing a little-endian representation of all the
	 *         bits in this bit set
	 * @since 1.7
	 */
	public long[] toLongArray() {
		return Arrays.copyOf(words, this.words.length);
	}


	/**
	 * Checks that fromIndex ... toIndex is a valid range of bit indices.
	 */
	private static void checkRange(int fromIndex, int toIndex) {
		if (fromIndex < 0)
			throw new IndexOutOfBoundsException("fromIndex < 0: " + fromIndex);
		if (toIndex < 0)
			throw new IndexOutOfBoundsException("toIndex < 0: " + toIndex);
		if (fromIndex > toIndex)
			throw new IndexOutOfBoundsException("fromIndex: " + fromIndex + " > toIndex: " + toIndex);
	}

	/**
	 * Sets the bit at the specified index to the complement of its current
	 * value.
	 * 
	 * @param bitIndex
	 *            the index of the bit to flip
	 * @throws IndexOutOfBoundsException
	 *             if the specified index is negative
	 * @since 1.4
	 */
	public void flip(int bitIndex) {
		if (bitIndex < 0)
			throw new IndexOutOfBoundsException("bitIndex < 0: " + bitIndex);

		int wordIndex = wordIndex(bitIndex);

		words[wordIndex] ^= (1L << bitIndex);

	}

	/**
	 * Sets each bit from the specified {@code fromIndex} (inclusive) to the
	 * specified {@code toIndex} (exclusive) to the complement of its current
	 * value.
	 * 
	 * @param fromIndex
	 *            index of the first bit to flip
	 * @param toIndex
	 *            index after the last bit to flip
	 * @throws IndexOutOfBoundsException
	 *             if {@code fromIndex} is negative, or {@code toIndex} is
	 *             negative, or {@code fromIndex} is larger than {@code toIndex}
	 * @since 1.4
	 */
	public void flip(int fromIndex, int toIndex) {
		checkRange(fromIndex, toIndex);

		if (fromIndex == toIndex)
			return;

		int startWordIndex = wordIndex(fromIndex);
		int endWordIndex = wordIndex(toIndex - 1);

		long firstWordMask = WORD_MASK << fromIndex;
		long lastWordMask = WORD_MASK >>> -toIndex;
		if (startWordIndex == endWordIndex) {
			// Case 1: One word
			words[startWordIndex] ^= (firstWordMask & lastWordMask);
		} else {
			// Case 2: Multiple words
			// Handle first word
			words[startWordIndex] ^= firstWordMask;

			// Handle intermediate words, if any
			for (int i = startWordIndex + 1; i < endWordIndex; i++)
				words[i] ^= WORD_MASK;

			// Handle last word
			words[endWordIndex] ^= lastWordMask;
		}

	}

	/**
	 * Sets the bit at the specified index to {@code true}.
	 * 
	 * @param bitIndex
	 *            a bit index
	 * @throws IndexOutOfBoundsException
	 *             if the specified index is negative
	 * @since JDK1.0
	 */
	public void set(int bitIndex) {
		if (bitIndex < 0)
			throw new IndexOutOfBoundsException("bitIndex < 0: " + bitIndex);

		if (bitIndex >= this.nbits)
			throw new IndexOutOfBoundsException("bitIndex >= nbits: " + bitIndex);

		int wordIndex = wordIndex(bitIndex);

		setWord(wordIndex, bitIndex);

		// checkInvariants();
	}

	public void setWord(int wordIndex, int bitIndex) {
		long mask = 1L << bitIndex;
		this.words[wordIndex] |= mask; // Restores invariants
	}

	/**
	 * Sets the bit at the specified index to the specified value.
	 * 
	 * @param bitIndex
	 *            a bit index
	 * @param value
	 *            a boolean value to set
	 * @throws IndexOutOfBoundsException
	 *             if the specified index is negative
	 * @since 1.4
	 */
	public void set(int bitIndex, boolean value) {
		if (value)
			set(bitIndex);
		else
			clear(bitIndex);
	}

	/**
	 * Sets the bits from the specified {@code fromIndex} (inclusive) to the
	 * specified {@code toIndex} (exclusive) to {@code true}.
	 * 
	 * @param fromIndex
	 *            index of the first bit to be set
	 * @param toIndex
	 *            index after the last bit to be set
	 * @throws IndexOutOfBoundsException
	 *             if {@code fromIndex} is negative, or {@code toIndex} is
	 *             negative, or {@code fromIndex} is larger than {@code toIndex}
	 * @since 1.4
	 */
	public void set(int fromIndex, int toIndex) {
		checkRange(fromIndex, toIndex);

		if (fromIndex == toIndex)
			return;

		// Increase capacity if necessary
		int startWordIndex = wordIndex(fromIndex);
		int endWordIndex = wordIndex(toIndex - 1);

		long firstWordMask = WORD_MASK << fromIndex;
		long lastWordMask = WORD_MASK >>> -toIndex;
		if (startWordIndex == endWordIndex) {
			// Case 1: One word
			words[startWordIndex] |= (firstWordMask & lastWordMask);
		} else {
			// Case 2: Multiple words
			// Handle first word
			words[startWordIndex] |= firstWordMask;

			// Handle intermediate words, if any
			for (int i = startWordIndex + 1; i < endWordIndex; i++)
				words[i] = WORD_MASK;

			// Handle last word (restores invariants)
			words[endWordIndex] |= lastWordMask;
		}

	}

	/**
	 * Sets the bits from the specified {@code fromIndex} (inclusive) to the
	 * specified {@code toIndex} (exclusive) to the specified value.
	 * 
	 * @param fromIndex
	 *            index of the first bit to be set
	 * @param toIndex
	 *            index after the last bit to be set
	 * @param value
	 *            value to set the selected bits to
	 * @throws IndexOutOfBoundsException
	 *             if {@code fromIndex} is negative, or {@code toIndex} is
	 *             negative, or {@code fromIndex} is larger than {@code toIndex}
	 * @since 1.4
	 */
	public void set(int fromIndex, int toIndex, boolean value) {
		if (value)
			set(fromIndex, toIndex);
		else
			clear(fromIndex, toIndex);
	}

	/**
	 * Sets the bit specified by the index to {@code false}.
	 * 
	 * @param bitIndex
	 *            the index of the bit to be cleared
	 * @throws IndexOutOfBoundsException
	 *             if the specified index is negative
	 * @since JDK1.0
	 */
	public void clear(int bitIndex) {
		if (bitIndex < 0)
			throw new IndexOutOfBoundsException("bitIndex < 0: " + bitIndex);

		int wordIndex = wordIndex(bitIndex);

		clearWord(wordIndex, bitIndex);

	}

	public void clearWord(int wordIndex, int bitIndex) {
		words[wordIndex] &= ~(1L << bitIndex);
	}

	/**
	 * Sets the bits from the specified {@code fromIndex} (inclusive) to the
	 * specified {@code toIndex} (exclusive) to {@code false}.
	 * 
	 * @param fromIndex
	 *            index of the first bit to be cleared
	 * @param toIndex
	 *            index after the last bit to be cleared
	 * @throws IndexOutOfBoundsException
	 *             if {@code fromIndex} is negative, or {@code toIndex} is
	 *             negative, or {@code fromIndex} is larger than {@code toIndex}
	 * @since 1.4
	 */
	public void clear(int fromIndex, int toIndex) {
		checkRange(fromIndex, toIndex);

		if (fromIndex == toIndex)
			return;

		int startWordIndex = wordIndex(fromIndex);
		if (startWordIndex >= this.words.length)
			return;

		int endWordIndex = wordIndex(toIndex - 1);
		if (endWordIndex >= this.words.length) {
			toIndex = length();
			endWordIndex = this.words.length - 1;
		}

		long firstWordMask = WORD_MASK << fromIndex;
		long lastWordMask = WORD_MASK >>> -toIndex;
		if (startWordIndex == endWordIndex) {
			// Case 1: One word
			words[startWordIndex] &= ~(firstWordMask & lastWordMask);
		} else {
			// Case 2: Multiple words
			// Handle first word
			words[startWordIndex] &= ~firstWordMask;

			// Handle intermediate words, if any
			for (int i = startWordIndex + 1; i < endWordIndex; i++)
				words[i] = 0;

			// Handle last word
			words[endWordIndex] &= ~lastWordMask;
		}
	}

	public void clearTo(int toIndex) {
		if (toIndex == 0) {
			return;
		}
		int endWordIndex = wordIndex(toIndex - 1);

		long lastWordMask = WORD_MASK >>> -toIndex;
		for (int i = 0; i < endWordIndex; i++) {
			words[i] = 0;
		}
		words[endWordIndex] &= ~lastWordMask;
	}

	public void clearFrom(int fromIndex) {
		int startWordIndex = wordIndex(fromIndex);
		if (startWordIndex >= this.words.length)
			return;

		long firstWordMask = WORD_MASK << fromIndex;
		words[startWordIndex] &= ~firstWordMask;
		for (int i = startWordIndex + 1; i < this.words.length; i++) {
			words[i] = 0;
		}
	}

	/**
	 * Sets all of the bits in this BitSet to {@code false}.
	 * 
	 * @since 1.4
	 */
	public void clear() {
		for (int i = 0; i < this.words.length; i++) {
			this.words[i] = 0;

		}
	}

	/**
	 * Returns the value of the bit with the specified index. The value is
	 * {@code true} if the bit with the index {@code bitIndex} is currently set
	 * in this {@code BitSet}; otherwise, the result is {@code false}.
	 * 
	 * @param bitIndex
	 *            the bit index
	 * @return the value of the bit with the specified index
	 * @throws IndexOutOfBoundsException
	 *             if the specified index is negative
	 */
	public boolean get(int bitIndex) {
		if (bitIndex < 0)
			throw new IndexOutOfBoundsException("bitIndex < 0: " + bitIndex);

		int wordIndex = wordIndex(bitIndex);
		return getWord(wordIndex, bitIndex);
	}

	public boolean getWord(int wordIndex, int bitIndex) {
		return ((words[wordIndex] & (1L << bitIndex)) != 0);
	}

	/**
	 * Returns a new {@code BitSet} composed of bits from this {@code BitSet}
	 * from {@code fromIndex} (inclusive) to {@code toIndex} (exclusive).
	 * 
	 * @param fromIndex
	 *            index of the first bit to include
	 * @param toIndex
	 *            index after the last bit to include
	 * @return a new {@code BitSet} from a range of this {@code BitSet}
	 * @throws IndexOutOfBoundsException
	 *             if {@code fromIndex} is negative, or {@code toIndex} is
	 *             negative, or {@code fromIndex} is larger than {@code toIndex}
	 * @since 1.4
	 */
	public PiolaBitset get(int fromIndex, int toIndex) {
		checkRange(fromIndex, toIndex);

		int len = length();

		// If no set bits in range return empty bitset
		if (len <= fromIndex || fromIndex == toIndex)
			return new PiolaBitset(0);

		// An optimization
		if (toIndex > len)
			toIndex = len;

		PiolaBitset result = new PiolaBitset(toIndex - fromIndex);
		int targetWords = wordIndex(toIndex - fromIndex - 1) + 1;
		int sourceIndex = wordIndex(fromIndex);
		boolean wordAligned = ((fromIndex & BIT_INDEX_MASK) == 0);

		// Process all words but the last word
		for (int i = 0; i < targetWords - 1; i++, sourceIndex++)
			result.words[i] = wordAligned ? words[sourceIndex] : (words[sourceIndex] >>> fromIndex)
					| (words[sourceIndex + 1] << -fromIndex);

		// Process the last word
		long lastWordMask = WORD_MASK >>> -toIndex;
		result.words[targetWords - 1] = ((toIndex - 1) & BIT_INDEX_MASK) < (fromIndex & BIT_INDEX_MASK) ? /*
																										 * straddles
																										 * source
																										 * words
																										 */
		((words[sourceIndex] >>> fromIndex) | (words[sourceIndex + 1] & lastWordMask) << -fromIndex)
				: ((words[sourceIndex] & lastWordMask) >>> fromIndex);

		// Set wordsInUse correctly
		return result;
	}

	/**
	 * Returns the index of the first bit that is set to {@code true} that
	 * occurs on or after the specified starting index. If no such bit exists
	 * then {@code -1} is returned.
	 * 
	 * <p>
	 * To iterate over the {@code true} bits in a {@code BitSet}, use the
	 * following loop:
	 * 
	 * <pre>
	 * {@code
	 * for (int i = bs.nextSetBit(0); i >= 0; i = bs.nextSetBit(i+1)) {
	 *     // operate on index i here
	 * }}
	 * </pre>
	 * 
	 * @param fromIndex
	 *            the index to start checking from (inclusive)
	 * @return the index of the next set bit, or {@code -1} if there is no such
	 *         bit
	 * @throws IndexOutOfBoundsException
	 *             if the specified index is negative
	 * @since 1.4
	 */
	public int nextSetBit(int fromIndex) {
		if (fromIndex < 0)
			throw new IndexOutOfBoundsException("fromIndex < 0: " + fromIndex);

		int wordIndex = wordIndex(fromIndex);

		return nextSetBitWord(wordIndex, fromIndex);
	}

	public int nextSetBitWord(int wordIndex, int fromIndex) {
		return nextSetBitWord(wordIndex, fromIndex, -1);
	}

	public int nextSetBitWord(int wordIndex, int fromIndex, int failureValue) {
		long word = words[wordIndex] & (WORD_MASK << fromIndex);
		
		while (true) {
			if (word != 0)
				return (wordIndex * BITS_PER_WORD) + Long.numberOfTrailingZeros(word);
			if (++wordIndex == this.words.length) {
				return failureValue;
			}
			word = words[wordIndex];
		}
	}

	public int nextSetBit(int fromIndex, int toIndex) {
		if (fromIndex < 0)
			throw new IndexOutOfBoundsException("fromIndex < 0: " + fromIndex);

		int u = wordIndex(fromIndex);
		int to = wordIndex(toIndex);

		if (u >= this.words.length)
			return toIndex;

		long word = words[u] & (WORD_MASK << fromIndex);

		while (true) {
			if (word != 0)
				return Math.min((u * BITS_PER_WORD) + Long.numberOfTrailingZeros(word), toIndex);
			++u;
			if (u == this.words.length || u == to)
				return toIndex;
			word = words[u];
		}
	}

	/**
	 * Returns the index of the first bit that is set to {@code false} that
	 * occurs on or after the specified starting index.
	 * 
	 * @param fromIndex
	 *            the index to start checking from (inclusive)
	 * @return the index of the next clear bit
	 * @throws IndexOutOfBoundsException
	 *             if the specified index is negative
	 * @since 1.4
	 */
	public int nextClearBit(int fromIndex) {
		// Neither spec nor implementation handle bitsets of maximal length.
		// See 4816253.
		if (fromIndex < 0)
			throw new IndexOutOfBoundsException("fromIndex < 0: " + fromIndex);

		int u = wordIndex(fromIndex);
		if (u >= this.words.length)
			return fromIndex;

		long word = ~words[u] & (WORD_MASK << fromIndex);

		while (true) {
			if (word != 0)
				return (u * BITS_PER_WORD) + Long.numberOfTrailingZeros(word);
			if (++u == this.words.length)
				return this.words.length * BITS_PER_WORD;
			word = ~words[u];
		}
	}

	/**
	 * Returns the index of the nearest bit that is set to {@code true} that
	 * occurs on or before the specified starting index. If no such bit exists,
	 * or if {@code -1} is given as the starting index, then {@code -1} is
	 * returned.
	 * 
	 * <p>
	 * To iterate over the {@code true} bits in a {@code BitSet}, use the
	 * following loop:
	 * 
	 * <pre>
	 * {@code
	 * for (int i = bs.length(); (i = bs.previousSetBit(i-1)) >= 0; ) {
	 *     // operate on index i here
	 * }}
	 * </pre>
	 * 
	 * @param fromIndex
	 *            the index to start checking from (inclusive)
	 * @return the index of the previous set bit, or {@code -1} if there is no
	 *         such bit
	 * @throws IndexOutOfBoundsException
	 *             if the specified index is less than {@code -1}
	 * @since 1.7
	 */
	public int previousSetBit(int fromIndex) {
		if (fromIndex < 0) {
			if (fromIndex < -1) {
				throw new IndexOutOfBoundsException("fromIndex < -1: " + fromIndex);
			}
			return -1;
		}

		int wordIndex = wordIndex(fromIndex);

		return previousSetBitWord(wordIndex, fromIndex);
	}

	public int previousSetBitWord(int wordIndex, int fromIndex) {
		long word = words[wordIndex] & (WORD_MASK >>> -(fromIndex + 1));

		while (true) {
			if (word != 0)
				return (wordIndex + 1) * BITS_PER_WORD - 1 - Long.numberOfLeadingZeros(word);
			if (wordIndex-- == 0)
				return -1;
			word = words[wordIndex];
		}
	}

	/**
	 * Returns the index of the nearest bit that is set to {@code false} that
	 * occurs on or before the specified starting index. If no such bit exists,
	 * or if {@code -1} is given as the starting index, then {@code -1} is
	 * returned.
	 * 
	 * @param fromIndex
	 *            the index to start checking from (inclusive)
	 * @return the index of the previous clear bit, or {@code -1} if there is no
	 *         such bit
	 * @throws IndexOutOfBoundsException
	 *             if the specified index is less than {@code -1}
	 * @since 1.7
	 */
	public int previousClearBit(int fromIndex) {
		if (fromIndex < 0) {
			if (fromIndex == -1)
				return -1;
			throw new IndexOutOfBoundsException("fromIndex < -1: " + fromIndex);
		}

		int u = wordIndex(fromIndex);
		if (u >= this.words.length)
			return fromIndex;

		long word = ~words[u] & (WORD_MASK >>> -(fromIndex + 1));

		while (true) {
			if (word != 0)
				return (u + 1) * BITS_PER_WORD - 1 - Long.numberOfLeadingZeros(word);
			if (u-- == 0)
				return -1;
			word = ~words[u];
		}
	}

	/**
	 * Returns the "logical size" of this {@code BitSet}: the index of the
	 * highest set bit in the {@code BitSet} plus one. Returns zero if the
	 * {@code BitSet} contains no set bits.
	 * 
	 * @return the logical size of this {@code BitSet}
	 * @since 1.2
	 */
	public int length() {
		if (this.words.length == 0)
			return 0;

		return BITS_PER_WORD * (this.words.length - 1)
				+ (BITS_PER_WORD - Long.numberOfLeadingZeros(words[this.words.length - 1]));
	}

	/**
	 * Returns true if this {@code BitSet} contains no bits that are set to
	 * {@code true}.
	 * 
	 * @return boolean indicating whether this {@code BitSet} is empty
	 * @since 1.4
	 */
	public boolean isEmpty() {
		for (long word : this.words) {
			if (word != 0) {
				return false;
			}
		}
		return true;
	}

	/**
	 * Returns true if the specified {@code BitSet} has any bits set to
	 * {@code true} that are also set to {@code true} in this {@code BitSet}.
	 * 
	 * @param set
	 *            {@code BitSet} to intersect with
	 * @return boolean indicating whether this {@code BitSet} intersects the
	 *         specified {@code BitSet}
	 * @since 1.4
	 */
	public boolean intersects(PiolaBitset set) {
		for (int i = Math.min(this.words.length, set.words.length) - 1; i >= 0; i--)
			if ((words[i] & set.words[i]) != 0)
				return true;
		return false;
	}

	/**
	 * Returns the number of bits set to {@code true} in this {@code BitSet}.
	 * 
	 * @return the number of bits set to {@code true} in this {@code BitSet}
	 * @since 1.4
	 */
	public int cardinality() {
		int sum = 0;
		for (int i = 0; i < this.words.length; i++)
			sum += Long.bitCount(words[i]);
		return sum;
	}

	/**
	 * Performs a logical <b>AND</b> of this target bit set with the argument
	 * bit set. This bit set is modified so that each bit in it has the value
	 * {@code true} if and only if it both initially had the value {@code true}
	 * and the corresponding bit in the bit set argument also had the value
	 * {@code true}.
	 * 
	 * @param set
	 *            a bit set
	 */
	public void and(PiolaBitset set) {
		throw new RuntimeException("No anda");
		// if (this == set)
		// return;
		//
		// while (this.words.length > set.words.length)
		// words[--wordsInUse] = 0;
		//
		// // Perform logical AND on words in common
		// for (int i = 0; i < wordsInUse; i++)
		// words[i] &= set.words[i];

	}

	/**
	 * Performs a logical <b>OR</b> of this bit set with the bit set argument.
	 * This bit set is modified so that a bit in it has the value {@code true}
	 * if and only if it either already had the value {@code true} or the
	 * corresponding bit in the bit set argument has the value {@code true}.
	 * 
	 * @param set
	 *            a bit set
	 */
	public void or(PiolaBitset set) {
		throw new RuntimeException("No anda");
		// if (this == set)
		// return;
		//
		// int wordsInCommon = Math.min(wordsInUse, set.wordsInUse);
		//
		// if (wordsInUse < set.wordsInUse) {
		// ensureCapacity(set.wordsInUse);
		// wordsInUse = set.wordsInUse;
		// }
		//
		// // Perform logical OR on words in common
		// for (int i = 0; i < wordsInCommon; i++)
		// words[i] |= set.words[i];
		//
		// // Copy any remaining words
		// if (wordsInCommon < set.wordsInUse)
		// System.arraycopy(set.words, wordsInCommon, words, wordsInCommon,
		// wordsInUse - wordsInCommon);
		//
		// // recalculateWordsInUse() is unnecessary
		// checkInvariants();
	}

	/**
	 * Performs a logical <b>XOR</b> of this bit set with the bit set argument.
	 * This bit set is modified so that a bit in it has the value {@code true}
	 * if and only if one of the following statements holds:
	 * <ul>
	 * <li>The bit initially has the value {@code true}, and the corresponding
	 * bit in the argument has the value {@code false}.
	 * <li>The bit initially has the value {@code false}, and the corresponding
	 * bit in the argument has the value {@code true}.
	 * </ul>
	 * 
	 * @param set
	 *            a bit set
	 */
	public void xor(PiolaBitset set) {
		throw new RuntimeException("No anda");

		// int wordsInCommon = Math.min(wordsInUse, set.wordsInUse);
		//
		// if (wordsInUse < set.wordsInUse) {
		// ensureCapacity(set.wordsInUse);
		// wordsInUse = set.wordsInUse;
		// }
		//
		// // Perform logical XOR on words in common
		// for (int i = 0; i < wordsInCommon; i++)
		// words[i] ^= set.words[i];
		//
		// // Copy any remaining words
		// if (wordsInCommon < set.wordsInUse)
		// System.arraycopy(set.words, wordsInCommon, words, wordsInCommon,
		// set.wordsInUse - wordsInCommon);
		//
		// recalculateWordsInUse();
		// checkInvariants();
	}

	/**
	 * Clears all of the bits in this {@code BitSet} whose corresponding bit is
	 * set in the specified {@code BitSet}.
	 * 
	 * @param set
	 *            the {@code BitSet} with which to mask this {@code BitSet}
	 * @since 1.2
	 */
	public void andNot(PiolaBitset set) {
		// Perform logical (a & !b) on words in common
		throw new RuntimeException("No anda");
		// for (int i = Math.min(wordsInUse, set.wordsInUse) - 1; i >= 0; i--)
		// words[i] &= ~set.words[i];
		//
		// recalculateWordsInUse();
		// checkInvariants();
	}

	/**
	 * Returns the hash code value for this bit set. The hash code depends only
	 * on which bits are set within this {@code BitSet}.
	 * 
	 * <p>
	 * The hash code is defined to be the result of the following calculation:
	 * 
	 * <pre>
	 * {@code
	 * public int hashCode() {
	 *     long h = 1234;
	 *     long[] words = toLongArray();
	 *     for (int i = words.length; --i >= 0; )
	 *         h ^= words[i] * (i + 1);
	 *     return (int)((h >> 32) ^ h);
	 * }}
	 * </pre>
	 * 
	 * Note that the hash code changes if the set of bits is altered.
	 * 
	 * @return the hash code value for this bit set
	 */
	public int hashCode() {
		long h = 1234;
		for (int i = this.words.length; --i >= 0;)
			h ^= words[i] * (i + 1);

		return (int) ((h >> 32) ^ h);
	}

	/**
	 * Returns the number of bits of space actually in use by this
	 * {@code BitSet} to represent bit values. The maximum element in the set is
	 * the size - 1st element.
	 * 
	 * @return the number of bits currently in this bit set
	 */
	public int size() {
		return this.nbits;
	}

	/**
	 * Compares this object against the specified object. The result is
	 * {@code true} if and only if the argument is not {@code null} and is a
	 * {@code Bitset} object that has exactly the same set of bits set to
	 * {@code true} as this bit set. That is, for every nonnegative {@code int}
	 * index {@code k},
	 * 
	 * <pre>
	 * ((BitSet) obj).get(k) == this.get(k)
	 * </pre>
	 * 
	 * must be true. The current sizes of the two bit sets are not compared.
	 * 
	 * @param obj
	 *            the object to compare with
	 * @return {@code true} if the objects are the same; {@code false} otherwise
	 * @see #size()
	 */
	public boolean equals(Object obj) {
		if (!(obj instanceof PiolaBitset))
			return false;
		if (this == obj)
			return true;

		PiolaBitset set = (PiolaBitset) obj;

		if (this.words.length != set.words.length)
			return false;

		// Check words in use by both BitSets
		for (int i = 0; i < this.words.length; i++)
			if (words[i] != set.words[i])
				return false;

		return true;
	}

	/**
	 * Cloning this {@code BitSet} produces a new {@code BitSet} that is equal
	 * to it. The clone of the bit set is another bit set that has exactly the
	 * same bits set to {@code true} as this bit set.
	 * 
	 * @return a clone of this bit set
	 * @see #size()
	 */
	public Object clone() {
		return copy();
	}

	public PiolaBitset copy() {
		try {
			PiolaBitset result = (PiolaBitset) super.clone();
			result.words = words.clone();
			return result;
		} catch (CloneNotSupportedException e) {
			throw new InternalError();
		}
	}

	/**
	 * Attempts to reduce internal storage used for the bits in this bit set.
	 * Calling this method may, but is not required to, affect the value
	 * returned by a subsequent call to the {@link #size()} method.
	 */

	/**
	 * Save the state of the {@code BitSet} instance to a stream (i.e.,
	 * serialize it).
	 */

	/**
	 * Returns a string representation of this bit set. For every index for
	 * which this {@code BitSet} contains a bit in the set state, the decimal
	 * representation of that index is included in the result. Such indices are
	 * listed in order from lowest to highest, separated by ",&nbsp;" (a comma
	 * and a space) and surrounded by braces, resulting in the usual
	 * mathematical notation for a set of integers.
	 * 
	 * <p>
	 * Example:
	 * 
	 * <pre>
	 * BitSet drPepper = new BitSet();
	 * </pre>
	 * 
	 * Now {@code drPepper.toString()} returns "{@code ".
	 * <p>
	 * 
	 * <pre>
	 * drPepper.set(2);
	 * </pre>Now {@code drPepper.toString()} returns "{@code 2} ".
	 * <p>
	 * 
	 * <pre>
	 * drPepper.set(4);
	 * drPepper.set(10);
	 * </pre>
	 * 
	 * Now {@code drPepper.toString()} returns "{@code 2, 4, 10} ".
	 * 
	 * @return a string representation of this bit set
	 */
	public String toString() {

		int numBits = (this.words.length > 128) ? cardinality() : this.words.length * BITS_PER_WORD;
		StringBuilder b = new StringBuilder(6 * numBits + 2);
		b.append('{');

		int i = nextSetBit(0);
		if (i != -1) {
			b.append(i);
			for (i = nextSetBit(i + 1); i >= 0; i = nextSetBit(i + 1)) {
				int endOfRun = nextClearBit(i);
				do {
					b.append(", ").append(i);
				} while (++i < endOfRun);
			}
		}

		b.append('}');
		return b.toString();
	}
}
