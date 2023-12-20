package in.novoup.pb.price.pojo;

import lombok.Builder;
import lombok.Data;

@Builder
@Data
public class CertificateFormData {

    private final String vehicleType;

    private final String make;

    private final String model;

    private final String plan;

    private final String viewState;

    private final String validation;

    private final String eventTarget;
}
