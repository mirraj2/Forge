package forge;

import java.util.Collection;

import com.google.common.primitives.Longs;

public class BitSet {

  public final long[] words;

  public BitSet(long numBits) {
    words = new long[(int) ((numBits + 63) / 64)];
  }

  public BitSet(Collection<Long> words) {
    this.words = Longs.toArray(words);
  }

  public int size() {
    return words.length * 64;
  }

  public boolean get(int index) {
    long word = words[index / 64];
    return (word & (1L << index)) != 0;
  }

  public void set(int index, boolean b) {
    if (b) {
      words[index / 64] |= (1L << index);
    } else {
      words[index / 64] &= ~(1L << index);
    }
  }

  public BitSet invert() {
    for (int i = 0; i < words.length; i++) {
      words[i] = ~words[i];
    }
    return this;
  }

  public BitSet or(BitSet b) {
    for (int i = 0; i < words.length; i++) {
      words[i] = words[i] | b.words[i];
    }
    return this;
  }

  // public void setAllToTrue() {
  // for (int i = 0; i < words.length; i++) {
  // words[i] = -1L;
  // }
  // }

}
