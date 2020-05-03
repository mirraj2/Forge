package forge;

import java.util.Collection;
import java.util.List;

import com.google.common.collect.Lists;
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

  public int[] getWords32() {
    int[] ret = new int[words.length * 2];
    for (int i = 0; i < words.length; i++) {
      long word = words[i];
      ret[i * 2] = (int) word;
      ret[i * 2 + 1] = (int) (word >> 32);
    }
    return ret;
  }

  public static BitSet fromInts(List<Integer> ints) {
    List<Long> data = Lists.newArrayListWithCapacity(ints.size() / 2);
    for (int i = 0; i < ints.size(); i += 2) {
      int a = ints.get(i);
      long b = ints.get(i + 1);
      data.add((b << 32) | (a & 0xffffffffL));
    }
    return new BitSet(data);
  }

  // public void setAllToTrue() {
  // for (int i = 0; i < words.length; i++) {
  // words[i] = -1L;
  // }
  // }

}
