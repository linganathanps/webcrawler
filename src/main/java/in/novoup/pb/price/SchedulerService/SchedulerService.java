package in.novoup.pb.price.SchedulerService;

import in.novoup.pb.price.entity.Policy;
import in.novoup.pb.price.pojo.CertificateFormData;
import in.novoup.pb.price.repository.PolicyRepository;
import org.jsoup.Connection;
import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;
import org.jsoup.nodes.Element;
import org.jsoup.select.Elements;
import org.quartz.DisallowConcurrentExecution;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Service;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.Objects;

@Service
@DisallowConcurrentExecution
public class SchedulerService {

    @Autowired
    private PolicyRepository policyRepository;

    @Scheduled(cron = "0 */1 * ? * *")
    //@PostConstruct
    public void run() {
        long startTime = System.currentTimeMillis();
        String username = "testaccount@gmail.com";
        String password = "midway@123";

        // URL of the login page
        String loginUrl = "https://briskportal.azurewebsites.net/Login";

        String certificateUrl = "https://briskportal.azurewebsites.net/Dealer/IssueCertificate";

        try {
            //Login
            Map<String, String> cookies = login(loginUrl, username, password);

            Connection.Response certificateForm = Jsoup.connect(certificateUrl)
                    .method(Connection.Method.GET)
                    .cookies(cookies)
                    .execute();
            Document certificateDoc = certificateForm.parse();


            String certificateEventValidation = getValidation(certificateDoc);
            String certificateViewState = getViewState(certificateDoc);

			/*Connection.Response certificateFinalResponse = Jsoup.connect(certificateUrl)
					.data("__EVENTTARGET", "ctl00$ContentPlaceHolder1$planlist")
					.data("__VIEWSTATE", certificateViewState)
					.data("__EVENTVALIDATION", certificateEventValidation)
					.data("ctl00$ContentPlaceHolder1$vehicletype", "TW")
					.data("ctl00$ContentPlaceHolder1$ComboBox1", "BAJAJ")
					.data("ctl00$ContentPlaceHolder1$modeldrop", "Apache-petrol")
					.data("ctl00$ContentPlaceHolder1$planlist", "TWHRN30K3S11500")
					.data("ctl00$ContentPlaceHolder1$TextBox5", "12/17/2023")
					.cookies(loginForm.cookies())
					.method(Connection.Method.POST)
					.execute();

			Document certificateFinalDoc = certificateFinalResponse.parse();
			String finalPrice = certificateFinalDoc.getElementById("ContentPlaceHolder1_TextBox3").attr("value");

			// Now, you can parse the content of the logged-in page
			Document certificateResPage = certificateFinalResponse.parse();*/

            List<String> vehicleTypes = extractDropdownValues(certificateDoc, "ctl00$ContentPlaceHolder1$vehicletype");
            vehicleTypes.forEach(type -> {
                CertificateFormData certificateFormData = CertificateFormData.builder()
                        .validation(getValidation(certificateDoc))
                        .viewState(getViewState(certificateDoc))
                        .vehicleType(type)
                        .eventTarget("ctl00$ContentPlaceHolder1$vehicletype")
                        .build();

                Document certificatePage = submitForm(certificateUrl,  cookies, certificateFormData);
                List<String> makes = extractDropdownValues(certificatePage, "ctl00$ContentPlaceHolder1$ComboBox1");
                makes.forEach(make -> {
                    CertificateFormData makeFormData = CertificateFormData.builder()
                            .validation(getValidation(certificatePage))
                            .viewState(getViewState(certificatePage))
                            .vehicleType(type)
                            .make(make)
                            .eventTarget("ctl00$ContentPlaceHolder1$ComboBox1")
                            .build();

                    Document makePage = submitForm(certificateUrl,  cookies, makeFormData);
                    List<String> models = extractDropdownValues(makePage, "ctl00$ContentPlaceHolder1$modeldrop");
                    models.stream().filter(model -> !Objects.equals(model, "-Select Model-")).forEach(model -> {
                        CertificateFormData modelFormData = CertificateFormData.builder()
                                .validation(getValidation(makePage))
                                .viewState(getViewState(makePage))
                                .vehicleType(type)
                                .make(make)
                                .model(model)
                                .eventTarget("ctl00$ContentPlaceHolder1$modeldrop")
                                .build();

                        Document modelPage = submitForm(certificateUrl,  cookies, modelFormData);
                        List<String> plans = extractDropdownValues(modelPage, "ctl00$ContentPlaceHolder1$planlist");
                        plans.forEach(plan -> {
                            CertificateFormData planFormData = CertificateFormData.builder()
                                    .validation(getValidation(modelPage))
                                    .viewState(getViewState(modelPage))
                                    .vehicleType(type)
                                    .make(make)
                                    .model(model)
                                    .plan(plan)
                                    .eventTarget("ctl00$ContentPlaceHolder1$modeldrop")
                                    .build();

                            Document planPage = submitForm(certificateUrl,  cookies, planFormData);
                            String price = planPage.getElementById("ContentPlaceHolder1_TextBox3").attr("value");
                            policyRepository.save(Policy.builder().type(type).make(make).model(model).price(price).plan(plan).insurerName("brisk").build());
                            System.out.println("Type :: " + type + ",  make :: " + make + ",  model :: " + model + ",  plan :: " + plan + ", price :: " +price);
                        });

                    });
                });
            });
            long endTime = System.currentTimeMillis();
            long elapsedTimeMillis = endTime - startTime;
            System.out.println("Total time taken ::" + elapsedTimeMillis / (60 * 1000));

        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    private static Document submitForm(String certificateUrl, Map<String, String> cookies, CertificateFormData certificateFormData)  {
        Connection.Response	certificateResponse = null;
        try {
            certificateResponse = Jsoup.connect(certificateUrl)
                    .data("__EVENTTARGET", certificateFormData.getEventTarget())
                    .data("__VIEWSTATE", certificateFormData.getViewState())
                    .data("__EVENTVALIDATION", certificateFormData.getValidation())
                    .data("ctl00$ContentPlaceHolder1$vehicletype", certificateFormData.getVehicleType())
                    .data("ctl00$ContentPlaceHolder1$ComboBox1", certificateFormData.getMake() == null ? "" :certificateFormData.getMake())
                    .data("ctl00$ContentPlaceHolder1$modeldrop", certificateFormData.getModel() == null ? "" :certificateFormData.getModel())
                    .data("ctl00$ContentPlaceHolder1$planlist", certificateFormData.getPlan() == null ? "" :certificateFormData.getPlan())
                    .cookies(cookies)
                    .method(Connection.Method.POST)
                    .execute();
            return  certificateResponse.parse();
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
    }

    private static String getViewState(Document certificateDoc) {
        return certificateDoc.getElementById("__VIEWSTATE").attr("value");
    }

    private static String getValidation(Document certificateDoc) {
        return certificateDoc.getElementById("__EVENTVALIDATION").attr("value");
    }


    public static List<String> extractDropdownValues(Document document, String dropdownName) {
        List<String> dropdownValues = new ArrayList<>();

        // Select the dropdown element by its name attribute
        Element dropdown = document.select("select[name=" + dropdownName + "]").first();

        // Check if the dropdown element is found
        if (dropdown != null) {
            // Get all the option elements inside the dropdown
            Elements options = dropdown.select("option");

            // Iterate through the option elements and extract the values
            for (Element option : options) {
                String value = option.attr("value");
                // Exclude the default selected option if needed
                if (!value.isEmpty()) {
                    dropdownValues.add(value);
                }
            }
        }

        return dropdownValues;
    }

    public static Map<String, String> login(String loginUrl, String username, String password) throws IOException {

        Connection.Response loginForm = Jsoup.connect(loginUrl)
                .method(Connection.Method.GET)
                .execute();

        Document loginDoc = loginForm.parse();
        String eventValidation = loginDoc.getElementById("__EVENTVALIDATION").attr("value");
        String viewState = getViewState(loginDoc);

        // Build the login request
        Connection.Response loginResponse = Jsoup.connect(loginUrl)
                .data("TextBox1", username)
                .data("TextBox3", password)
                .data("__VIEWSTATE", viewState)
                .data("__EVENTVALIDATION", eventValidation)
                .data("Button2", "Login")
                .cookies(loginForm.cookies())
                .method(Connection.Method.POST)
                .execute();
        return loginForm.cookies();

    }
}
