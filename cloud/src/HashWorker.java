import parcs.*;

/** АМ воркера: хешує свій шматок обох текстів і повертає результат батьківській точці. */
public class HashWorker implements AM {
    public void run(AMInfo info) {
        HashUtil.Job job = (HashUtil.Job) info.parent.readObject();
        HashUtil.JobResult res = HashUtil.computeChunk(job);
        info.parent.write(res);
    }
}
