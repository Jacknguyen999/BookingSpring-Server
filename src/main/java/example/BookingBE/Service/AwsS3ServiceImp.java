package example.BookingBE.Service;


import com.amazonaws.auth.AWSStaticCredentialsProvider;
import com.amazonaws.auth.BasicAWSCredentials;
import com.amazonaws.regions.Regions;
import com.amazonaws.services.s3.AmazonS3;
import com.amazonaws.services.s3.AmazonS3Client;
import com.amazonaws.services.s3.AmazonS3ClientBuilder;
import com.amazonaws.services.s3.model.ObjectMetadata;
import com.amazonaws.services.s3.model.PutObjectRequest;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;

import java.io.InputStream;

@Service
public class AwsS3ServiceImp {
    private final String bucketName = "thomas-hotel-img";

    @Value("${aws.s3.access.key}")
    private String Aws3accessKey;

    @Value("${aws.s3.secret.key}")
    private String Aws3secretKey;

    public String saveImagestoS3 (MultipartFile photo) {
        String s3LocationImage = null;

        try{
            String s3FileName = photo.getOriginalFilename();
            BasicAWSCredentials awsCreds = new BasicAWSCredentials(Aws3accessKey, Aws3secretKey);
            AmazonS3 s3Client = AmazonS3ClientBuilder.standard()
                    .withCredentials(new AWSStaticCredentialsProvider(awsCreds))
                    .withRegion(Regions.AP_SOUTHEAST_2)
                    .build();

            InputStream inputStream = photo.getInputStream();
            ObjectMetadata metadata = new ObjectMetadata();
            metadata.setContentType("image/jpeg");

            PutObjectRequest putObjectRequest = new PutObjectRequest(bucketName, s3FileName, inputStream, metadata);
            s3Client.putObject(putObjectRequest);
            s3LocationImage = "https://" + bucketName + ".s3-ap-southeast-2.amazonaws.com/" + s3FileName;



        } catch (Exception e) {
            e.printStackTrace();
            throw  new RuntimeException("Error while uploading image to S3" + e.getMessage());
        }
        return s3LocationImage;
    }

}
