import java.io.Serializable;

/** Поліноміальне хешування рядків (подвійний хеш) + генератор детермінованого "великого тексту". */
public final class HashUtil {
    public static final long M1 = 1_000_000_007L, M2 = 998_244_353L;
    public static final long P1 = 911_382_323L,   P2 = 972_663_749L;

    /** Результат для шматка тексту: хеш за двома модулями + довжина. */
    public static final class HashResult implements Serializable {
        public long h1, h2, len;
        public HashResult(long h1, long h2, long len) { this.h1 = h1; this.h2 = h2; this.len = len; }
        @Override public String toString() { return h1 + ":" + h2 + " (len=" + len + ")"; }
        @Override public boolean equals(Object o) {
            if (!(o instanceof HashResult)) return false;
            HashResult r = (HashResult) o; return h1 == r.h1 && h2 == r.h2 && len == r.len;
        }
        @Override public int hashCode() { return Long.hashCode(h1 * 31 + h2); }
    }

    /** Завдання для одного воркера: шматок [start, start+len) двох текстів A і B. */
    public static final class Job implements Serializable {
        public long seed, start, len, diffPos; // diffPos = -1 -> тексти однакові
        public Job(long seed, long start, long len, long diffPos) {
            this.seed = seed; this.start = start; this.len = len; this.diffPos = diffPos;
        }
    }

    /** Відповідь воркера: хеші шматка тексту A та тексту B. */
    public static final class JobResult implements Serializable {
        public HashResult a, b;
        public JobResult(HashResult a, HashResult b) { this.a = a; this.b = b; }
    }

    static long splitmix64(long x) {
        x += 0x9E3779B97F4A7C15L;
        x = (x ^ (x >>> 30)) * 0xBF58476D1CE4E5B9L;
        x = (x ^ (x >>> 27)) * 0x94D049BB133111EBL;
        return x ^ (x >>> 31);
    }

    /** Символ тексту A на позиції i (літери a..z). Текст "віртуальний": не зберігається в пам'яті. */
    public static int charA(long seed, long i) {
        return 'a' + (int) Long.remainderUnsigned(splitmix64(seed + i), 26);
    }

    /** Символ тексту B: такий самий, як A, окрім позиції diffPos. */
    public static int charB(long seed, long i, long diffPos) {
        int c = charA(seed, i);
        return (i == diffPos) ? 'a' + ((c - 'a' + 1) % 26) : c;
    }

    /** Хеш шматка [start, start+len) обох текстів за один прохід (схема Горнера). */
    public static JobResult computeChunk(Job j) {
        long a1 = 0, a2 = 0, b1 = 0, b2 = 0;
        long end = j.start + j.len;
        for (long i = j.start; i < end; i++) {
            int ca = charA(j.seed, i);
            int cb = (i == j.diffPos) ? 'a' + ((ca - 'a' + 1) % 26) : ca;
            a1 = (a1 * P1 + ca) % M1;  a2 = (a2 * P2 + ca) % M2;
            b1 = (b1 * P1 + cb) % M1;  b2 = (b2 * P2 + cb) % M2;
        }
        return new JobResult(new HashResult(a1, a2, j.len), new HashResult(b1, b2, j.len));
    }

    public static long powMod(long base, long exp, long mod) {
        long r = 1; base %= mod;
        while (exp > 0) { if ((exp & 1) == 1) r = r * base % mod; base = base * base % mod; exp >>= 1; }
        return r;
    }

    /** hash(XY) = hash(X) * p^|Y| + hash(Y). */
    public static HashResult combine(HashResult x, HashResult y) {
        long h1 = (x.h1 * powMod(P1, y.len, M1) + y.h1) % M1;
        long h2 = (x.h2 * powMod(P2, y.len, M2) + y.h2) % M2;
        return new HashResult(h1, h2, x.len + y.len);
    }

    /** Розбиття [0,n) на parts частин; повертає масив Job. */
    public static Job[] split(long seed, long n, long diffPos, int parts) {
        Job[] jobs = new Job[parts];
        long base = n / parts, rem = n % parts, start = 0;
        for (int i = 0; i < parts; i++) {
            long len = base + (i < rem ? 1 : 0);
            jobs[i] = new Job(seed, start, len, diffPos);
            start += len;
        }
        return jobs;
    }
}
