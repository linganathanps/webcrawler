package in.novoup.pb.price.service;

import in.novoup.pb.price.Response.PolicyResponse;
import in.novoup.pb.price.request.PolicyRequest;

import java.util.List;

public interface PolicyPriceService {

    List<PolicyResponse> calculatePolicyPrice(PolicyRequest policyRequest);
}
