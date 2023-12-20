package in.novoup.pb.price.service;

import in.novoup.pb.price.Response.PolicyResponse;
import in.novoup.pb.price.request.PolicyRequest;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
public class PolicyPriceServiceImpl implements PolicyPriceService {
    @Override
    public List<PolicyResponse> calculatePolicyPrice(PolicyRequest policyRequest) {
        return null;
    }
}
