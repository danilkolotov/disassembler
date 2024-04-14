package disassembler.util;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

public class IntUtils {
    public static int LEToInt(List<Byte> a) {
        List<Byte> reversed = new ArrayList<>(a);
        Collections.reverse(reversed);
        return reversed.stream().mapToInt(Byte::toUnsignedInt).reduce((acc, cur) -> acc * (1 << 8) + cur).orElse(0);
    }

    public static int mask(int start, int end) {
        int mask = 0;
        for (int i = start; i < end; i++) mask += 1 << i;
        return mask;
    }

    public static int extract(int x, int start, int end) {
        return (mask(start, end) & x) >>> start;
    }

    public static int extract(int x, int start) {
        return extract(x, start, start + 1);
    }

    public static class BitBuilder {
        int result;

        public BitBuilder fill(int start, int end, int x) {
            result += (x % (1 << (end - start))) << start;
            return this;
        }

        public BitBuilder place(int index, int x) {
            return fill(index, index + 1, x);
        }

        public BitBuilder repeat(int start, int end, int d) {
            d = d % 2;
            return fill(start, end, d * mask(0, end - start));
        }

        public int build() {
            return result;
        }
    }
}
