package nextflow.tencentcloud.nio;

import com.google.common.base.Preconditions;
import com.qcloud.cos.model.COSObjectSummary;
import com.qcloud.cos.model.ListObjectsRequest;
import com.qcloud.cos.model.ObjectListing;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;

public class CosIterator implements Iterator<Path> {

//    private static final Logger log = LoggerFactory.getLogger(CosIterator.class);

    private final CosFileSystem cosFileSystem;
    private final String bucket;
    private final String key;

    private Iterator<CosPath> it;

    public CosIterator(CosFileSystem cosFileSystem, String bucket, String key) {

        Preconditions.checkArgument(key != null && key.endsWith("/"),
                "key %s should not be empty and should end with slash '/'", key);

        this.bucket = bucket;
        // list buckets content
        this.key = key.length() == 1 ? "" : key;
        this.cosFileSystem = cosFileSystem;
    }

    @Override
    public void remove() {
        throw new UnsupportedOperationException();
    }

    @Override
    public CosPath next() {
        return getIterator().next();
    }

    @Override
    public boolean hasNext() {
        return getIterator().hasNext();
    }

    private Iterator<CosPath> getIterator() {
        if (it == null) {
            List<CosPath> listPath = new ArrayList<>();
            ObjectListing current = cosFileSystem.getClient().listObjects(buildRequest());
            while (current.isTruncated()) {
                parseObjectListing(listPath, current);
                current = cosFileSystem.getClient().listNextBatchOfObjects(current);
            }
            parseObjectListing(listPath, current);
            it = listPath.iterator();
        }

        return it;
    }

    private ListObjectsRequest buildRequest() {
        ListObjectsRequest request = new ListObjectsRequest();
        request.setBucketName(bucket);
        request.setPrefix(key);
        request.setMarker(key);
        request.setDelimiter("/");
        return request;
    }

    private void parseObjectListing(List<CosPath> listPath, ObjectListing current) {
        for (final COSObjectSummary objectSummary : current.getObjectSummaries()) {
            final String key = objectSummary.getKey();

//            log.debug("parseObjectListing, CosPath, bucket: {}, Key: {}", bucket, key);
            final CosPath path = new CosPath(cosFileSystem, "/" + bucket, key.split("/"));
            path.setObjectSummary(objectSummary);
            listPath.add(path);
        }

        for (final String dir : current.getCommonPrefixes()) {
            if (dir.equals("/")) {
                continue;
            }
//            log.debug("parseObjectListing, CosPath, bucket: {}, dir: {}", bucket, dir);
            listPath.add(new CosPath(cosFileSystem, "/" + bucket, dir));
        }
    }
}
