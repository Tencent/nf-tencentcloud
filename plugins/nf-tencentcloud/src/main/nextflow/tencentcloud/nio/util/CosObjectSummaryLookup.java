package nextflow.tencentcloud.nio.util;

import com.qcloud.cos.exception.CosServiceException;
import com.qcloud.cos.model.COSObject;
import com.qcloud.cos.model.COSObjectSummary;
import com.qcloud.cos.model.ListObjectsRequest;
import com.qcloud.cos.model.ObjectListing;
import com.qcloud.cos.model.ObjectMetadata;
import java.io.IOException;
import java.nio.file.NoSuchFileException;
import java.util.List;
import nextflow.tencentcloud.nio.CosClient;
import nextflow.tencentcloud.nio.CosPath;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class CosObjectSummaryLookup {

    private static final Logger log = LoggerFactory.getLogger(COSObjectSummary.class);

    /**
     * lookup
     * lookup object summary, check if file exists
     *
     * @param cosPath {@link CosPath}
     * @throws java.nio.file.NoSuchFileException if not found the path and any child
     */
    public COSObjectSummary lookup(CosPath cosPath) throws NoSuchFileException {

        /*
         * check is object summary has been cached
         */
        COSObjectSummary summary = cosPath.fetchObjectSummary();
        if (summary != null) {
            return summary;
        }

        final CosClient client = cosPath.getFileSystem().getClient();

        /*
         * when `key` is an empty string retrieve the object meta-data of the bucket
         */
        if ("".equals(cosPath.getKey())) {
            ObjectMetadata meta = client.getObjectMetadata(cosPath.getBucket(), "");
            if (meta == null) {
                throw new NoSuchFileException("cos://" + cosPath.getBucket());
            }

            summary = new COSObjectSummary();
            summary.setBucketName(cosPath.getBucket());
            summary.setETag(meta.getETag());
            summary.setKey(cosPath.getKey());
            summary.setLastModified(meta.getLastModified());
            summary.setSize(meta.getContentLength());
            // TODO summary.setOwner(?);
            // TODO summary.setStorageClass(?);
            return summary;
        }

        /*
         * Lookup for the object summary for the specified object key
         * by using a `listObjects` request
         */
        String marker = null;
        while (true) {
            ListObjectsRequest request = new ListObjectsRequest();
            request.setBucketName(cosPath.getBucket());
            request.setPrefix(cosPath.getKey());
            request.setMaxKeys(250);
            if (marker != null) {
                request.setMarker(marker);
            }

            ObjectListing listing = client.listObjects(request);
            List<COSObjectSummary> results = listing.getObjectSummaries();

            if (results.isEmpty()) {
                break;
            }

            for (COSObjectSummary item : results) {
                if (matchName(cosPath.getKey(), item)) {
                    return item;
                }
            }

            if (listing.isTruncated()) {
                marker = listing.getNextMarker();
            } else {
                break;
            }
        }

        throw new NoSuchFileException("cos://" + cosPath.getBucket() + "/" + cosPath.getKey());
    }

    private boolean matchName(String fileName, COSObjectSummary summary) {
        String foundKey = summary.getKey();

        // they are different names return false
        if (!foundKey.startsWith(fileName)) {
            return false;
        }

        // when they are the same length, they are identical
        if (foundKey.length() == fileName.length()) {
            return true;
        }

        return foundKey.charAt(fileName.length()) == '/';
    }

    public ObjectMetadata getCosObjectMetadata(CosPath cosPath) {
        CosClient client = cosPath.getFileSystem().getClient();
        try {
            return client.getObjectMetadata(cosPath.getBucket(), cosPath.getKey());
        } catch (CosServiceException e) {
            if (e.getStatusCode() != 404) {
                throw e;
            }
            return null;
        }
    }

    /**
     * get CosObject represented by this CosPath try to access with or without end slash '/'
     *
     * @param cosPath CosPath
     * @return CosObject or null if it does not exist
     */
    @Deprecated
    private COSObject getCosObject(CosPath cosPath) {

        CosClient client = cosPath.getFileSystem()
                .getClient();

        COSObject object = getCosObject(cosPath.getBucket(), cosPath.getKey(), client);

        if (object != null) {
            return object;
        } else {
            return getCosObject(cosPath.getBucket(), cosPath.getKey() + "/", client);
        }
    }

    /**
     * get CosObject with CosObject#getObjectContent closed
     *
     * @param bucket String bucket
     * @param key String key
     * @param client CosClient client
     * @return CosObject
     */
    private COSObject getCosObject(String bucket, String key, CosClient client) {
        try {
            COSObject object = client.getObject(bucket, key);
            if (object.getObjectContent() != null) {
                try {
                    object.getObjectContent().close();
                } catch (IOException e) {
                    log.debug("Error while closing CosObject for bucket: `{}` and key: `{}` -- Cause: {}",
                            bucket, key, e.getMessage());
                }
            }
            return object;
        } catch (CosServiceException e) {
            if (e.getStatusCode() != 404) {
                throw e;
            }
            return null;
        }
    }
}
