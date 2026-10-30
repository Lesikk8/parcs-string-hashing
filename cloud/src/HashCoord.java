import parcs.*;

/** Координатор: запускається як точка, сам створює воркерів (як у прикладі Bluck). */
public class HashCoord implements AM {
    public void run(AMInfo info) {
        long[] prm = (long[]) info.parent.readObject();   // {n, workers, diffPos}
        long n = prm[0]; int workers = (int) prm[1]; long diff = prm[2];

        HashUtil.Job[] jobs = HashUtil.split(12345, n, diff, workers);
        point[] p = new point[workers];
        channel[] c = new channel[workers];
        for (int i = 0; i < workers; i++) {
            p[i] = info.createPoint();
            c[i] = p[i].createChannel();
            p[i].execute("HashWorker");
            c[i].write(jobs[i]);
        }
        HashUtil.HashResult a = null, b = null;
        for (int i = 0; i < workers; i++) {
            HashUtil.JobResult r = (HashUtil.JobResult) c[i].readObject();
            a = (a == null) ? r.a : HashUtil.combine(a, r.a);
            b = (b == null) ? r.b : HashUtil.combine(b, r.b);
        }
        info.parent.write(new HashUtil.JobResult(a, b));
    }
}
