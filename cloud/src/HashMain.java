import parcs.*;
import java.io.File;
import java.util.Scanner;

/** Початковий модуль. Запускається на app, створює одну точку-координатор. */
public class HashMain {
    public static void main(String[] args) throws Exception {
        task curtask = new task();
        curtask.addJarFile("HashWorker.jar");

        long n, diff; int workers;
        try (Scanner sc = new Scanner(new File(curtask.findFile("input")))) {
            n = sc.nextLong(); workers = sc.nextInt(); diff = sc.nextLong();
        }

        long t0 = System.nanoTime();
        AMInfo info = new AMInfo(curtask, null);
        point p = info.createPoint();
        channel c = p.createChannel();
        p.execute("HashCoord");
        c.write(new long[]{n, workers, diff});
        HashUtil.JobResult r = (HashUtil.JobResult) c.readObject();
        long ms = (System.nanoTime() - t0) / 1_000_000;

        System.out.println("N=" + n + " workers=" + workers);
        System.out.println("hash(A) = " + r.a);
        System.out.println("hash(B) = " + r.b);
        System.out.println("A == B ? " + r.a.equals(r.b));
        System.out.println("parallel time = " + ms + " ms");
        curtask.end();
    }
}
