/** Послідовний алгоритм + самоперевірка коректності комбінування шматків. */
public class Sequential {
    public static void main(String[] args) {
        long n = args.length > 0 ? Long.parseLong(args[0]) : 200_000_000L;
        long diff = args.length > 1 ? Long.parseLong(args[1]) : -1;
        long seed = 12345;

        long t0 = System.nanoTime();
        HashUtil.JobResult r = HashUtil.computeChunk(new HashUtil.Job(seed, 0, n, diff));
        long ms = (System.nanoTime() - t0) / 1_000_000;
        System.out.println("hash(A) = " + r.a);
        System.out.println("hash(B) = " + r.b);
        System.out.println("A == B ? " + r.a.equals(r.b));
        System.out.println("sequential time = " + ms + " ms");

        // Самоперевірка: хеш, зібраний з k шматків, має збігатися з послідовним
        for (int k : new int[]{1, 2, 3, 4, 7, 8}) {
            HashUtil.Job[] jobs = HashUtil.split(seed, n, diff, k);
            HashUtil.HashResult a = null, b = null;
            for (HashUtil.Job j : jobs) {
                HashUtil.JobResult jr = HashUtil.computeChunk(j);
                a = (a == null) ? jr.a : HashUtil.combine(a, jr.a);
                b = (b == null) ? jr.b : HashUtil.combine(b, jr.b);
            }
            System.out.println("check k=" + k + ": " + (a.equals(r.a) && b.equals(r.b) ? "OK" : "FAIL"));
        }
    }
}
