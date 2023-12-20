package in.novoup.pb.price.controller;

import in.novoup.pb.price.Response.PolicyResponse;
import in.novoup.pb.price.request.PolicyRequest;
import in.novoup.pb.price.service.PolicyPriceService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

import static in.novoup.pb.price.constant.ApiNames.CALCULATE_POLICY_PRICE;

@RestController
public class PolicyPriceController {

    private final PolicyPriceService policyPriceService;

    public PolicyPriceController(PolicyPriceService policyPriceService) {
        this.policyPriceService = policyPriceService;
    }

    @PostMapping(CALCULATE_POLICY_PRICE)
    public ResponseEntity<List<PolicyResponse>> calculatePolicyPrice(@RequestBody @Validated PolicyRequest policyRequest) {
        return new ResponseEntity<>(policyPriceService.calculatePolicyPrice(policyRequest), HttpStatus.OK);
    }

}
