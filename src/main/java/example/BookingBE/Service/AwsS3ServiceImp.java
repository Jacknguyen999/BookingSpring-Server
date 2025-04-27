package example.BookingBE.Service;

import com.amazonaws.auth.AWSStaticCredentialsProvider;
import com.amazonaws.auth.BasicAWSCredentials;
import com.amazonaws.services.s3.AmazonS3;
import com.amazonaws.services.s3.AmazonS3ClientBuilder;
import com.amazonaws.services.s3.model.ObjectMetadata;
import com.amazonaws.services.s3.model.PutObjectRequest;
import jakarta.annotation.PostConstruct;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

@Service
public class AwsS3ServiceImp {
    
    @Value("${bucket-name}")
    private String bucketName;

    @Value("${aws.s3.access.key}")
    private String awsAccessKey;

    @Value("${aws.s3.secret.key}")
    private String awsSecretKey;

    private AmazonS3 s3Client;

    @PostConstruct
    public void initialize() {
        BasicAWSCredentials awsCredentials = new BasicAWSCredentials(awsAccessKey, awsSecretKey);
        
        this.s3Client = AmazonS3ClientBuilder.standard()
                .withRegion("ap-southeast-2")
                .withCredentials(new AWSStaticCredentialsProvider(awsCredentials))
                .build();
    }

    public List<String> saveImagestoS3(List<MultipartFile> photos) {
        List<String> imageUrls = new ArrayList<>();

        try {
            for (MultipartFile photo : photos) {
                if (photo != null && !photo.isEmpty()) {
                    String originalFilename = photo.getOriginalFilename();
                    String uniqueFilename = UUID.randomUUID().toString() + 
                            originalFilename.substring(originalFilename.lastIndexOf("."));

                    ObjectMetadata metadata = new ObjectMetadata();
                    metadata.setContentType(photo.getContentType());
                    metadata.setContentLength(photo.getSize());

                    PutObjectRequest request = new PutObjectRequest(
                            bucketName,
                            uniqueFilename,
                            photo.getInputStream(),
                            metadata
                    );

                    s3Client.putObject(request);

                    String imageUrl = String.format("https://%s.s3.ap-southeast-2.amazonaws.com/%s",
                            bucketName, uniqueFilename);
                    imageUrls.add(imageUrl);
                }
            }
        } catch (IOException e) {
            throw new RuntimeException("Error while uploading images to S3: " + e.getMessage(), e);
        }

        return imageUrls;
    }

    public String saveImagetoS3(MultipartFile photo) {
        if (photo == null || photo.isEmpty()) {
            throw new RuntimeException("No photo provided");
        }
        return saveImagestoS3(List.of(photo)).get(0);
    }
}
